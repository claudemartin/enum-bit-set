package ch.claude_martin.enumbitset;

import static ch.claude_martin.enumbitset.EnumBitSetTest.Alphabet.A;
import static ch.claude_martin.enumbitset.EnumBitSetTest.Alphabet.C;
import static ch.claude_martin.enumbitset.EnumBitSetTest.Alphabet.D;
import static ch.claude_martin.enumbitset.EnumBitSetTest.Alphabet.E;
import static ch.claude_martin.enumbitset.EnumBitSetTest.Alphabet.L;
import static ch.claude_martin.enumbitset.EnumBitSetTest.Alphabet.U;
import static ch.claude_martin.enumbitset.SmallDomainBitSet.allOf;
import static ch.claude_martin.enumbitset.SmallDomainBitSet.noneOf;
import static ch.claude_martin.enumbitset.SmallDomainBitSet.of;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.claude_martin.enumbitset.EnumBitSetTest.Alphabet;
import ch.claude_martin.enumbitset.EnumBitSetTest.Element;

@SuppressWarnings("static-method")
public class SmallDomainBitSetTest {

  private static SmallDomainBitSet<Alphabet> claude;

  @BeforeClass
  public static void setup() {
    SmallDomainBitSetTest.claude = SmallDomainBitSet
        .of(asList(Alphabet.values()), C, L, A, U, D, E);
  }

  SmallDomainBitSet<Integer> none      = noneOf(1, 2, 3, 4);
  SmallDomainBitSet<Integer> oneTo4    = allOf(1, 2, 3, 4);
  SmallDomainBitSet<Integer> oneTwo    = of(this.none.getDomain(), asList(1, 2));
  SmallDomainBitSet<Integer> twoThree  = of(this.none.getDomain(), asList(2, 3));

  SmallDomainBitSet<Integer> threeFour = of(this.none.getDomain(), asList(3, 4));

  @Test
  public void testAllOf() {
    final SmallDomainBitSet<Integer> allOf = allOf(1, 2, 3, 4, 5, 6);
    assertEquals(6, allOf.size());
    assertEquals(0, allOf.complement().size());
  }

  @Test
  public void testComplement() {
    final DomainBitSet<Alphabet> complement = claude.complement();
    for (final Alphabet letter : claude)
      assertFalse(complement.contains(letter));
    for (final Alphabet letter : complement)
      assertFalse(claude.contains(letter));
    assertEquals(Alphabet.values().length, claude.size() + complement.size());
  }

  @Test
  public void testCross() {
    assertEquals(this.oneTo4.cross(this.oneTo4), this.oneTo4.cross(this.oneTo4));
    assertEquals(emptySet(), this.none.cross(this.none));
    assertEquals(emptySet(), this.oneTo4.cross(this.none));
    assertEquals(emptySet(), this.none.cross(this.oneTo4));
  }

  @Test
  public void testGetDomain() {
    assertEquals(claude.getDomain(), claude.getDomain());
    assertEquals(claude.getDomain(), claude.complement().getDomain());
    assertEquals(claude.getDomain(), claude.union(1).getDomain());
    assertTrue(claude.getDomain().containsAll(asList(Alphabet.values())));
  }

  @Test
  public void testIntersectBigInteger() {
    assertEquals(this.none, this.oneTwo.intersect(this.threeFour.toBigInteger()));
    assertEquals(this.none, this.threeFour.intersect(this.oneTwo.toBigInteger()));
    assertEquals(singleton(2), this.oneTwo.intersect(this.twoThree.toBigInteger()).toSet());
    assertEquals(singleton(3), this.twoThree.intersect(this.threeFour.toBigInteger()).toSet());
    assertEquals(this.oneTwo, this.oneTwo.intersect(this.oneTwo.toBigInteger()));
  }

  @Test
  public void testIntersectBitSet() {
    assertEquals(this.none, this.oneTwo.intersect(this.threeFour.toBitSet()));
    assertEquals(this.none, this.threeFour.intersect(this.oneTwo.toBitSet()));
    assertEquals(singleton(2), this.oneTwo.intersect(this.twoThree.toBitSet()).toSet());
    assertEquals(singleton(3), this.twoThree.intersect(this.threeFour.toBitSet()).toSet());
    assertEquals(this.oneTwo, this.oneTwo.intersect(this.oneTwo.toBitSet()));
  }

  @Test
  public void testIntersectDomainBitSetOfT() {
    assertEquals(this.none, this.oneTo4.intersect(this.none));
    assertEquals(this.none, this.none.intersect(this.oneTo4));
    assertEquals(this.none, this.oneTwo.intersect(this.none));
    assertEquals(this.none, this.none.intersect(this.oneTwo));
    assertEquals(this.none, this.twoThree.intersect(this.none));
    assertEquals(this.none, this.none.intersect(this.twoThree));
    assertEquals(this.none, this.threeFour.intersect(this.none));
    assertEquals(this.none, this.none.intersect(this.threeFour));
    assertEquals(this.none, this.none.intersect(this.none));

    assertEquals(this.oneTwo, this.oneTo4.intersect(this.oneTwo));
    assertEquals(this.twoThree, this.oneTo4.intersect(this.twoThree));
    assertEquals(this.threeFour, this.oneTo4.intersect(this.threeFour));

    assertEquals(singleton(2), this.oneTwo.intersect(this.twoThree).toSet());
    assertEquals(singleton(3), this.twoThree.intersect(this.threeFour).toSet());
  }

  @Test
  public void testIntersectLong() {
    assertEquals(emptySet(), this.oneTwo.intersect(this.threeFour.toLong()).toSet());
    assertEquals(emptySet(), this.threeFour.intersect(this.oneTwo.toLong()).toSet());
    assertEquals(singleton(2), this.oneTwo.intersect(this.twoThree.toLong()).toSet());
    assertEquals(singleton(3), this.twoThree.intersect(this.threeFour.toLong()).toSet());
    assertEquals(this.oneTwo, this.oneTwo.intersect(this.oneTwo.toLong()));
  }

  @Test
  public void testIntersectTArray() {
    assertEquals(emptySet(), this.oneTwo.intersectVarArgs(3, 4).toSet());
    assertEquals(emptySet(), this.threeFour.intersectVarArgs(1, 2).toSet());
    assertEquals(singleton(2), this.oneTwo.intersectVarArgs(2, 3).toSet());
    assertEquals(singleton(3), this.twoThree.intersectVarArgs(3, 4).toSet());
    assertEquals(this.oneTwo, this.oneTwo.intersectVarArgs(1, 2));
  }

  @Test
  public void testIsEmpty() {
    assertTrue(this.none.isEmpty());
    assertFalse(this.oneTwo.isEmpty());
    assertFalse(this.twoThree.isEmpty());
    assertFalse(this.threeFour.isEmpty());
    assertFalse(this.oneTo4.isEmpty());
  }

  @Test
  public void testIterator() {
    final SmallDomainBitSet<String> foo = of(asList("a", "b", "c"), "b", "c");
    for (final String string : foo) {
      assertFalse(string.equals("a"));
      assertTrue(string.equals("b") || string.equals("c"));
    }
  }

  @Test
  public void testMap() throws Exception {
    final Domain<Integer> domain = allOf(1, 2, 3, 4, 5).getDomain();

    DomainBitSet<Integer> mapped;
    mapped = this.none.map(domain);
    assertEquals(SmallDomainBitSet.noneOf(domain), mapped);

    for (final SmallDomainBitSet<Integer> set : asList(this.oneTwo, this.twoThree, this.oneTo4)) {
      // Map to the same number:
      mapped = set.map(domain);
      assertEquals(set.toSet(), mapped.toSet());

      // Map all to 1:
      mapped = set.map(domain, (x) -> 1);
      assertEquals(SmallDomainBitSet.of(domain, asList(1)), mapped);
    }
  }

  @Test
  public void testMinusBigInteger() {
    assertEquals(this.oneTo4, this.oneTo4.minus(this.none.toBigInteger()));
    assertEquals(this.threeFour, this.oneTo4.minus(this.oneTwo.toBigInteger()));
    assertEquals(this.none, this.oneTwo.minus(this.oneTo4.toBigInteger()));
  }

  @Test
  public void testMinusBitSet() {
    assertEquals(this.oneTo4, this.oneTo4.minus(this.none.toBitSet()));
    assertEquals(this.threeFour, this.oneTo4.minus(this.oneTwo.toBitSet()));
    assertEquals(this.none, this.oneTwo.minus(this.oneTo4.toBitSet()));
  }

  @Test
  public void testMinusDomainBitSetOfT() {
    assertEquals(this.oneTo4, this.oneTo4.minus(this.none));
    assertEquals(this.none, this.none.minus(this.oneTo4));
    assertEquals(this.oneTwo, this.oneTwo.minus(this.none));
    assertEquals(this.none, this.none.minus(this.oneTwo));
    assertEquals(this.twoThree, this.twoThree.minus(this.none));
    assertEquals(this.none, this.none.minus(this.twoThree));
    assertEquals(this.threeFour, this.threeFour.minus(this.none));
    assertEquals(this.none, this.none.minus(this.threeFour));
    assertEquals(this.none, this.none.minus(this.none));

    assertEquals(this.none, this.oneTo4.minus(this.oneTo4));
    assertEquals(this.threeFour, this.oneTo4.minus(this.oneTwo));
    assertEquals(this.oneTwo, this.oneTo4.minus(this.threeFour));

    assertEquals(singleton(1), this.oneTwo.minus(this.twoThree).toSet());
    assertEquals(singleton(2), this.twoThree.minus(this.threeFour).toSet());
  }

  @Test
  public void testMinusLong() {
    assertEquals(this.oneTo4, this.oneTo4.minus(this.none.toLong()));
    assertEquals(this.threeFour, this.oneTo4.minus(this.oneTwo.toLong()));
    assertEquals(this.none, this.oneTwo.minus(this.oneTo4.toLong()));

    try {
      assertEquals(this.none, this.oneTwo.minus(-1L));
      fail("minus should not accept bit mask with too many ones.");
    } catch (final Exception e) {
    }

    final DomainBitSet<Element> set = of(asList(Element.values()).subList(0, 64));
    assertTrue(set.minus(-1).isEmpty());
  }

  @Test
  public void testMinusTArray() {
    assertEquals(this.oneTo4, this.oneTo4.minusVarArgs());
    assertEquals(this.threeFour, this.oneTo4.minusVarArgs(1, 2));
    assertEquals(this.none, this.oneTwo.minusVarArgs(1, 2, 3, 4));
  }

  @Test
  public void testNoneOf() {
    final SmallDomainBitSet<Integer> noneOf = noneOf(1, 2, 3, 4, 5, 6);
    assertEquals(0, noneOf.size());
    assertEquals(6, noneOf.complement().size());
  }

  @Test
  public void testOfEqualDomain() throws Exception {
    assertTrue(this.threeFour.ofEqualDomain(this.threeFour));
    assertTrue(this.threeFour.ofEqualDomain(this.threeFour.clone()));
    assertTrue(this.threeFour.ofEqualDomain(this.twoThree));
    assertFalse(this.threeFour.ofEqualDomain(allOf(3, 4)));
    assertTrue(allOf().ofEqualDomain(noneOf()));
  }

  @Test
  public void testOfEqualElements() throws Exception {
    assertTrue(this.threeFour.ofEqualElements(this.threeFour));
    assertTrue(this.threeFour.ofEqualElements(this.threeFour.clone()));
    assertFalse(this.threeFour.ofEqualElements(this.twoThree));
    assertTrue(this.threeFour.ofEqualElements(allOf(3, 4)));
    assertTrue(this.none.ofEqualElements(allOf()));
  }

  @Test
  public void testOfListOfT_() {
    final SmallDomainBitSet<String> of1 = of(asList("a", "b", "c"), asList("b", "c"));
    assertTrue(of1.contains("b"));
    final SmallDomainBitSet<String> of2 = of(asList("a", "b", "c"), "b", "c");
    assertTrue(of2.contains("b"));
    assertEquals(of1, of2);
  }

  @Test
  public void testOfListOfTLong() {
    final List<Integer> domain = asList(0, 1, 2, 3);
    final DomainBitSet<Integer> set = of(domain, 0b11L);
    assertTrue(set.getBit(0));
    assertTrue(set.getBit(1));
    assertFalse(set.getBit(2));
    assertFalse(set.getBit(3));
    of(DefaultDomain.of(domain), 15L);

    try {
      of(DefaultDomain.of(domain), -1L);
      fail();
    } catch (final IllegalArgumentException e) {
    }

  }

  @Test
  public void testSize() {
    assertEquals(0, this.none.size());
    assertEquals(2, this.oneTwo.size());
    assertEquals(2, this.twoThree.size());
    assertEquals(2, this.threeFour.size());
    assertEquals(4, this.oneTo4.size());
  }

  @Test
  public void testToBigInteger() {

    assertEquals(claude.toLong(), claude.toBigInteger().longValue());

  }

  @Test
  public void testToBitSet() {
    BitSet bitSet = this.oneTo4.toBitSet();
    for (int i = 0; i < 4; i++)
      assertTrue(bitSet.get(i));

    bitSet = of(asList(0, 1, 2, 3, 4), singleton(3)).toBitSet();
    assertTrue(bitSet.get(3));
    assertEquals(1, bitSet.cardinality());
  }

  @Test
  public void testToLong() {
    final List<Element> el64 = new ArrayList<>(64);
    final List<Element> el32 = new ArrayList<>(32);
    final Element[] values = Element.values();
    for (int i = 0; i < 64; i++)
      el64.add(values[i]);
    for (int i = 0; i < 32; i++)
      el32.add(values[i]);

    final SmallDomainBitSet<Element> none64 = of(DefaultDomain.of(el64), 0L);
    assertEquals(0L, none64.toLong());
    assertEquals(-1L, none64.complement().toLong());

    final SmallDomainBitSet<Element> none32 = of(DefaultDomain.of(el32), 0L);
    assertEquals(0L, none32.toLong());
    assertEquals(-1L >>> 32, none32.complement().toLong());
  }

  @Test
  public void testToSet() {
    final Set<Integer> set1 = this.oneTo4.toSet();
    final Set<Integer> set2 = new TreeSet<>();
    this.oneTo4.forEach(set2::add);
    assertEquals(set1, set2);
  }

  @Test
  public void testUnionBigInteger() {
    assertEquals(this.oneTo4, this.oneTwo.union(this.threeFour.toBigInteger()));
    assertEquals(this.oneTo4, this.threeFour.union(this.oneTwo.toBigInteger()));
    assertEquals(this.oneTwo, this.oneTwo.union(this.oneTwo.toBigInteger()));
  }

  @Test
  public void testUnionBitSet() {
    assertEquals(this.oneTo4, this.oneTwo.union(this.threeFour.toBitSet()));
    assertEquals(this.oneTo4, this.threeFour.union(this.oneTwo.toBitSet()));
    assertEquals(this.oneTwo, this.oneTwo.union(this.oneTwo.toBitSet()));
  }

  @Test
  public void testUnionDomainBitSetOfT() {
    assertEquals(this.oneTo4, this.oneTo4.union(this.none));
    assertEquals(this.oneTo4, this.none.union(this.oneTo4));
    assertEquals(this.oneTo4, this.oneTo4.union(this.oneTwo));
    assertEquals(this.oneTo4, this.oneTwo.union(this.oneTo4));
    assertEquals(this.oneTo4, this.oneTo4.union(this.twoThree));
    assertEquals(this.oneTo4, this.twoThree.union(this.oneTo4));
    assertEquals(this.oneTo4, this.oneTo4.union(this.threeFour));
    assertEquals(this.oneTo4, this.threeFour.union(this.oneTo4));

    assertEquals(this.oneTo4, this.oneTwo.union(this.threeFour));
    assertEquals(this.oneTo4, this.threeFour.union(this.oneTwo));

    assertEquals(this.oneTwo, this.oneTwo.union(this.oneTwo));
    assertEquals(this.none, this.none.union(this.none));
  }

  @Test
  public void testUnionLong() {
    assertEquals(this.oneTo4, this.oneTwo.union(this.threeFour.toLong()));
    assertEquals(this.oneTo4, this.threeFour.union(this.oneTwo.toLong()));
    assertEquals(this.oneTwo, this.oneTwo.union(this.oneTwo.toLong()));
  }

  @Test
  public void testUnionTArray() {
    assertEquals(this.oneTo4, this.oneTwo.unionVarArgs(3, 4));
    assertEquals(this.oneTo4, this.threeFour.unionVarArgs(1, 2));
    assertEquals(this.oneTwo, this.oneTwo.unionVarArgs(1, 2));
  }

  @Test
  public void testZipWithPosition() throws Exception {
    for (final DomainBitSet<Integer> s : asList(this.oneTo4, this.none, this.oneTwo, this.twoThree,
        this.threeFour)) {
      s.zipWithPosition().forEach(p -> assertEquals((int) p.first, p.second - 1));
      s.complement().zipWithPosition().forEach(p -> assertEquals((int) p.first, p.second - 1));
      assertEquals(s.size(), s.zipWithPosition().count());
    }
  }

  @SuppressWarnings({ "rawtypes" })
  @Test
  public void testSerialize() throws Exception {

    for (final DomainBitSet set : asList(this.none, this.oneTo4, this.oneTwo, this.twoThree,
        this.threeFour)) {

      final byte[] data;
      try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
        try (ObjectOutputStream obj = new ObjectOutputStream(out)) {
          obj.writeObject(set);
          data = out.toByteArray();
        }
      }

      final DomainBitSet set2;

      try (ByteArrayInputStream in = new ByteArrayInputStream(data)) {
        try (ObjectInputStream obj = new ObjectInputStream(in)) {
          set2 = (DomainBitSet) obj.readObject();
        }
      }

      assertNotSame(set, set2);
      assertEquals(set, set2);
    }
  }
}
