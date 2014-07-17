package ch.claude_martin.enumbitset;

import static ch.claude_martin.enumbitset.GeneralDomainBitSet.allOf;
import static ch.claude_martin.enumbitset.GeneralDomainBitSet.noneOf;
import static ch.claude_martin.enumbitset.GeneralDomainBitSet.of;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.junit.Test;

import ch.claude_martin.enumbitset.EnumBitSetTest.Element;
import ch.claude_martin.enumbitset.EnumBitSetTest.Planet;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressWarnings("static-method")
public class GeneralDomainBitSetTest {

  final GeneralDomainBitSet<Integer> none      = noneOf(1, 2, 3, 4);
  final GeneralDomainBitSet<Integer> oneTo4    = allOf(1, 2, 3, 4);
  final GeneralDomainBitSet<Integer> oneTwo    = of(this.none.getDomain(), asList(1, 2));
  final GeneralDomainBitSet<Integer> twoThree  = of(this.none.getDomain(), asList(2, 3));
  final GeneralDomainBitSet<Integer> threeFour = of(this.none.getDomain(), asList(3, 4));

  @Test
  public void testAdd() {
    GeneralDomainBitSet<Integer> clone = this.oneTo4.clone();
    clone.add(1);
    assertEquals(this.oneTo4, clone);
    try {
      clone.add(-1);
      fail("Only elements from the domain should be allowed.");
    } catch (final IllegalArgumentException e) {
    }
    clone = this.oneTwo.clone();
    clone.add(3);
    assertEquals(this.oneTwo.unionVarArgs(3, 3), clone);

  }

  @Test
  public void testAddAll() {
    final GeneralDomainBitSet<Integer> clone = this.none.clone();
    clone.addAll(Collections.emptySet());
    assertEquals(clone, this.none);

    clone.addAll(this.twoThree);
    assertEquals(clone, this.twoThree);
    try {
      clone.addAll(asList(2, 3, 4, 5));
      fail("Only elements from the domain should be allowed.");
    } catch (final IllegalArgumentException e) {
      // IllegalArgumentException is expected!
    }
  }

  @Test
  public void testAllOf() {
    // already tested by testGeneralDomainBitSetListOfT()
  }

  @Test
  public void testAllOfListOfT() {
    final GeneralDomainBitSet<Integer> set = allOf(asList(1, 2, 3, 4));
    assertEquals(this.oneTo4, set);
  }

  @Test
  public void testClear() {
    final GeneralDomainBitSet<Integer> clone = this.oneTo4.clone();
    clone.clear();
    assertEquals(this.none, clone);
  }

  @Test
  public void testClone() {
    assertEquals(this.oneTo4.clone(), this.oneTo4.clone());
    assertEquals(this.oneTo4, this.oneTo4.clone());
    assertFalse(this.oneTo4 == this.oneTo4.clone());
  }

  @Test
  public void testComplement() {
    assertEquals(this.none, this.oneTo4.complement());
    assertEquals(this.oneTo4, this.none.complement());
    assertEquals(this.oneTwo, this.threeFour.complement());
    assertEquals(this.threeFour, this.oneTwo.complement());
  }

  @Test
  public void testContains() {
    assertFalse(this.none.contains(1));
    assertFalse(this.oneTo4.contains(5));
    assertTrue(this.oneTo4.contains(Integer.valueOf(1)));
  }

  @Test
  public void testContainsAll() {
    assertTrue(this.oneTo4.containsAll(this.oneTwo));
    assertTrue(this.oneTo4.containsAll(this.twoThree));
    assertTrue(this.oneTo4.containsAll(this.threeFour));
    assertFalse(this.oneTwo.containsAll(this.oneTo4));
  }

  @Test
  public <X extends Enum<X>> void testCreateMultiEnumBitSet() {
    @SuppressWarnings("unchecked")
    final GeneralDomainBitSet<X> set = (GeneralDomainBitSet<X>) DomainBitSet.createMultiEnumBitSet(
        Element.class, Planet.class);
    @SuppressWarnings("unchecked")
    final List<X> planets = (List<X>) asList(EnumBitSetTest.Planet.values());
    set.addAll(planets);
    assertEquals(set.size(), planets.size());
    assertEquals(set.complement().size(), Element.values().length);
  }

  @Test
  public void testCross() {
    assertEquals(this.oneTo4.cross(this.oneTo4), this.oneTo4.cross(this.oneTo4));
    assertEquals(emptySet(), this.none.cross(this.none));
    assertEquals(emptySet(), this.oneTo4.cross(this.none));
    assertEquals(emptySet(), this.none.cross(this.oneTo4));
  }

  @Test
  public void testDomainContains() {
    for (int i = 1; i <= 4; i++)
      assertTrue(this.oneTo4.domainContains(i));
  }

  @Test
  public void testEqualsObject() {
    assertTrue(this.oneTwo.equals(this.oneTwo));
    assertFalse(this.oneTwo.equals(this.twoThree));
    assertTrue(this.oneTwo.clone().equals(this.oneTwo));
  }

  @Test
  public void testGetBit() throws Exception {
    final GeneralDomainBitSet<Integer> set = of(asList(0, 1, 2, 3, 4), Collections.singleton(3));
    assertTrue(set.getBit(3));
    assertFalse(set.getBit(0));
    assertFalse(set.getBit(1));
    assertFalse(set.getBit(2));
    assertFalse(set.getBit(4));
    for (int i = 0; i < set.getDomain().size(); i++)
      assertEquals(set.getBit(i), set.toBigInteger().testBit(i));

    try {
      assertFalse(set.getBit(5));
      fail("expected: IndexOutOfBoundsException");
    } catch (final java.lang.IndexOutOfBoundsException e) {
      // expected
    }

    try {
      assertFalse(set.getBit(-1));
      fail("expected: IndexOutOfBoundsException");
    } catch (final java.lang.IndexOutOfBoundsException e) {
      // expected
    }
  }

  @Test
  public void testGetDomain() {
    final Set<Integer> domain = this.oneTo4.getDomain();
    assertEquals(4, domain.size());
    for (int i = 1; i <= 4; i++)
      assertTrue(domain.contains(i));
    try {
      domain.add(123);
      fail("The domain must be unmodifiable.");
    } catch (final Exception e) {
    }
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
    assertEquals(this.none, this.oneTwo.intersect(this.threeFour.toLong()));
    assertEquals(this.none, this.threeFour.intersect(this.oneTwo.toLong()));
    assertEquals(singleton(2), this.oneTwo.intersect(this.twoThree.toLong()).toSet());
    assertEquals(singleton(3), this.twoThree.intersect(this.threeFour.toLong()).toSet());
    assertEquals(this.oneTwo, this.oneTwo.intersect(this.oneTwo.toLong()));
  }

  @Test
  public void testIntersectVarArgs() {
    assertEquals(this.none, this.oneTwo.intersectVarArgs(3, 4));
    assertEquals(this.none, this.threeFour.intersectVarArgs(1, 2));
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
    for (final Integer i : this.none)
      fail("should not exsist: " + i);
    int sum = 0;
    for (final Integer i : this.oneTo4)
      sum += i;
    final int expected = 1 + 2 + 3 + 4;// =10
    assertEquals(expected, sum);
    assertEquals(this.oneTo4.size(), this.oneTo4.stream().count());
    assertEquals(expected, (int) this.oneTo4.stream().reduce(0, Integer::sum));
  }

  @Test
  public void testMap() throws Exception {
    final Domain<Integer> domain = allOf(1, 2, 3, 4, 5).getDomain();

    DomainBitSet<Integer> mapped;
    mapped = this.none.map(domain);
    assertEquals(SmallDomainBitSet.noneOf(domain), mapped);

    for (final DomainBitSet<Integer> set : asList(this.oneTwo, this.twoThree, this.oneTo4)) {
      // Map to the same number:
      mapped = set.map(domain);
      assertEquals(set.toSet(), mapped.toSet());
      // Map all to 1:
      mapped = set.map(domain, (x) -> 1);
      assertEquals(SmallDomainBitSet.of(domain, asList(1)), mapped);
    }

    try {
      this.oneTo4.map(allOf(1, 2).getDomain());
      fail("map: (1,2,3,4) -> (1,2) is not possible!");
    } catch (final IllegalArgumentException e) {
      // expected!
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
    } catch (final IllegalArgumentException e) {
    }

    final DomainBitSet<Element> set = allOf(asList(Element.values()).subList(0, 64));
    assertEquals(64, set.size());
    assertTrue(set.minus(-1L).isEmpty());
  }

  @Test
  public void testMinusVarArgs() {
    assertEquals(this.oneTo4, this.oneTo4.minusVarArgs());
    assertEquals(this.threeFour, this.oneTo4.minusVarArgs(1, 2));
    assertEquals(this.none, this.oneTwo.minusVarArgs(1, 2, 3, 4));
  }

  @Test
  public void testNoneOf() {
    final GeneralDomainBitSet<Integer> set1 = noneOf(1, 2, 3, 4);
    final GeneralDomainBitSet<Integer> set2 = noneOf(asList(1, 2, 3, 4));
    assertEquals(set2, set1);
    assertTrue(set1.isEmpty());
    assertTrue(set2.isEmpty());
  }

  @Test
  public void testNoneOfLinkedHashSet() {
    final LinkedHashSet<Integer> set1 = new LinkedHashSet<>();
    set1.add(1);
    set1.add(2);
    set1.add(3);
    set1.add(4);
    final GeneralDomainBitSet<Integer> set2 = noneOf(set1);
    assertEquals(this.none, set2);
  }

  @Test
  public void testNoneOfListOfT() {
    final GeneralDomainBitSet<Integer> set = noneOf(asList(1, 2, 3, 4));
    assertEquals(this.none, set);
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
  public void testOfLinkedHashSetOfTSetOfT() {
    final LinkedHashSet<Integer> set1 = new LinkedHashSet<>();
    set1.add(1);
    set1.add(2);
    set1.add(3);
    set1.add(4);
    final GeneralDomainBitSet<Integer> set2 = of(set1, set1);
    assertEquals(this.oneTo4, set2);

    final GeneralDomainBitSet<Integer> set3 = of(set1, new HashSet<>());
    assertEquals(noneOf(1, 2, 3, 4), set3);
  }

  @Test
  public void testOfListOfTSetOfT() {
    final GeneralDomainBitSet<Integer> set = of(asList(1, 2, 3, 4), singleton(3));
    assertTrue(set.contains(3));
    assertFalse(set.contains(1));
    assertFalse(set.contains(2));
    assertFalse(set.contains(4));

    assertFalse(set.contains("3"));
    try {
      assertFalse(set.contains(null));
      fail("contains() should not accept null");
    } catch (final NullPointerException e) {
      // expected
    }
  }

  @Test
  public void testParallelStream() {
    final Set<Element> copy = new ConcurrentSkipListSet<>();
    final GeneralDomainBitSet<Element> elements = allOf(Element.values());
    elements.parallelStream().forEach(copy::add);
    assertEquals(elements.toSet(), copy);
  }

  @Test
  @SuppressFBWarnings(value = "GC_UNRELATED_TYPES", justification = "it's not a bug, it's a test.")
  public void testRemove() {
    final GeneralDomainBitSet<Integer> clone = this.oneTo4.clone();
    clone.remove(1);
    clone.remove(4);
    assertEquals(this.twoThree, clone);
    try {
      clone.remove("Elephant");
    } catch (final IllegalArgumentException e) {
      fail("remove(Object) should do nothing if the object is not of the domain.");
    }
    assertEquals(this.twoThree, clone);
  }

  @Test
  public void testRemoveAll() {
    final GeneralDomainBitSet<Integer> clone = this.oneTo4.clone();
    clone.removeAll(this.oneTwo);
    assertEquals(this.threeFour, clone);
  }

  @Test
  public void testRemoveIf() {
    final GeneralDomainBitSet<Integer> clone = this.oneTo4.clone();
    clone.removeIf(x -> x > 2);
    assertEquals(this.oneTwo, clone);
  }

  @Test
  public void testRetainAll() {
    final GeneralDomainBitSet<Integer> clone = this.oneTo4.clone();
    clone.retainAll(this.oneTwo);
    assertEquals(this.oneTwo, clone);
    clone.retainAll(this.twoThree);
    assertEquals(this.oneTwo.intersect(this.twoThree), clone);
    clone.retainAll(this.threeFour);
    assertEquals(this.none, clone);
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
  public void testStream() {
    assertEquals(Integer.valueOf(1), this.oneTo4.stream().findFirst().get());
    // [1,2,3,4] is already distinct and sorted, so let's test this:
    // (note: Java 8 beta still needs a lot of type information for the collector.)
    final Collector<Integer, ?, LinkedHashSet<Integer>> collector = Collectors
        .<Integer, LinkedHashSet<Integer>> toCollection(LinkedHashSet<Integer>::new);
    assertEquals(this.oneTo4.toLinkedHashSet(),
        this.oneTo4.stream().distinct().sorted().collect(collector));
  }

  @Test
  public void testToArray() {
    assertArrayEquals(this.oneTo4.toArray(), this.oneTo4.toArray());
    assertArrayEquals(new Object[0], this.none.toArray());
    assertArrayEquals(new Object[] { 1, 2 }, this.oneTwo.toArray());
  }

  @Test
  public void testToArrayXArray() {
    final Object[] array = new Object[4];
    this.oneTo4.toArray(array);
    assertArrayEquals(this.oneTo4.toArray(), array);
  }

  @Test
  public void testToBigInteger() {
    assertEquals(BigInteger.valueOf(15), this.oneTo4.toBigInteger());
    final BigInteger i = of(asList(0, 1, 2, 3, 4), singleton(3)).toBigInteger();
    assertEquals(BigInteger.valueOf(0b01000L), i);
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
  public void testToLinkedHashSet() {
    final LinkedHashSet<Integer> set = this.oneTo4.toLinkedHashSet();
    assertEquals(this.oneTo4.toSet(), set);
  }

  @Test
  public void testToLong() {
    assertEquals(0b1111L, this.oneTo4.toLong());

    {
      // set with domain of 64 elements and then test "negative" long values.
      EnumBitSet<Element> enum64 = EnumBitSet.allOf(EnumBitSetTest.Element.class);
      enum64 = enum64.intersect(BitSetUtilities.asBigInteger(-1));
      final ArrayList<Element> domain = new ArrayList<>(enum64);
      GeneralDomainBitSet<Element> set = of(domain, enum64);
      assertEquals(-1L, set.toLong());
      set = set.intersect(Long.MIN_VALUE);
      assertEquals(Long.MIN_VALUE, set.toLong());
      set = set.complement();
      assertEquals(Long.MAX_VALUE, set.toLong());
    }

    try {
      final GeneralDomainBitSet<Element> elements = noneOf(asList(Element.values()));
      elements.toLong();
      fail("Even an empty set with a large domain should throw MoreThan64ElementsException");
    } catch (final MoreThan64ElementsException e) {
    }
  }

  @Test
  public void testToSet() {
    final Set<Integer> set = this.oneTo4.toSet();
    assertEquals(this.oneTo4.toSet(), set);
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
  public void testUnionVarArgs() {
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
}
