package ch.claude_martin.enumbitset;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigInteger;
import java.util.BitSet;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

@SuppressWarnings("static-method")
public class EnumBitSetTest {
  static enum Alphabet implements EnumBitSetHelper<Alphabet> {
    A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z;
  }

  public enum Element implements EnumBitSetHelper<Element> {
    H, C, N, O, D, T, He, Li, Be, B, F, Ne, Na, Mg, Al, Si, P, S, Cl, Ar, K, Ca, Sc, Ti, V, Cr, Mn, Fe, Co, Ni, Cu, Zn, Ga, Ge, As, Se, Br, Kr, Rb, Sr, Y, Zr, Nb, Mo, Tc, Ru, Rh, Pd, Ag, Cd, In, Sn, Sb, Te, I, Xe, Cs, Ba, La, Ce, Pr, Nd, Pm, Sm, Eu, Gd, Tb, Dy, Ho, Er, Tm, Yb, Lu, Hf, Ta, W, Re, Os, Ir, Pt, Au, Hg, Tl, Pb, Bi, Po, At, Rn, Fr, Ra, Ac, Th, Pa, U, Np, Pu, Am, Cm, Bk, Cf, Es, Fm, Md, No, Lr, R;
  }

  static enum Planet implements EnumBitSetHelper<Planet> {
    MERCURY, VENUS, EARTH, MARS, JUPITER, SATURN, URANUS, NEPTUNE;
    // Sorry, Pluto!
  }

  public static enum Rank implements EnumBitSetHelper<Rank> {
    ACE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING;
    public int getRank() {
      return this.ordinal() + 1;
    }
  }

  public static enum Suit implements EnumBitSetHelper<Suit> {
    CLUBS, DIAMONDS, HEARTS, SPADES;
    static {
      assert values().length <= 64 : "This enum type is not indented do contain more than 64 constants.";
    }
  }

  @Test
  public void testAddedToArray() {
    EnumBitSet<Element> set = Element.C.union(Element.Mg, Element.N);
    assertEquals(3, set.size());
    set = Element.C.union();
    assertEquals(1, set.size());
    assertEquals(Element.C.bitmask(), set.toBigInteger());
  }

  @Test
  public void testAddedToEnumSet() {
    {
      EnumSet<Element> set = EnumSet.noneOf(Element.class);
      assertEquals(0, set.size());
      set = Element.C.union(set);
      assertEquals(1, set.size());
      set = Element.Mg.union(set);
      assertEquals(2, set.size());
      set = Element.N.union(set);
      assertEquals(3, set.size());
    }
  }

  @Test
  public void testAllOf() throws Exception {
    assertEquals(26, EnumBitSet.allOf(Alphabet.class).size());
    assertFalse(EnumBitSet.allOf(Element.class).isEmpty());
  }

  @Test
  public void testAsBigInteger() {
    final BigInteger bigInt = BigInteger.valueOf(0b0110);
    final BitSet bitset = new BitSet();
    bitset.set(1);
    bitset.set(2);
    final BigInteger bigInt2 = BitSetUtilities.asBigInteger(bitset);
    assertEquals(bigInt, bigInt2);

    final BitSet bitset2 = new BitSet();
    final BigInteger bigInt3 = BitSetUtilities.asBigInteger(bitset2);
    assertEquals(BigInteger.ZERO, bigInt3);
  }

  @Test
  public void testAsBigIntegerArray() {
    BigInteger bitmask = EnumBitSet.asBigInteger(Alphabet.A, Alphabet.B);
    assertEquals(BigInteger.valueOf(1 + 2), bitmask);

    bitmask = EnumBitSet.asBigInteger();
    assertEquals(BigInteger.ZERO, bitmask);

    bitmask = Alphabet.A.others().toBigInteger();
    assertEquals(BigInteger.ZERO, bitmask.and(BigInteger.ONE));
    for (int i = Alphabet.B.ordinal(); i <= Alphabet.Z.ordinal(); i++) {
      final BigInteger m = BigInteger.ONE.shiftLeft(i);
      assertEquals(m, bitmask.and(m));
    }
  }

  @Test
  public void testAsBigIntegerBitSet() {
    assertEquals(BigInteger.ZERO, BitSetUtilities.asBigInteger(new BitSet()));
    BitSet set = Alphabet.A.toBitSet();
    BigInteger bitmask = BitSetUtilities.asBigInteger(set);
    assertEquals(BigInteger.ONE, bitmask);

    set = Element.H.others().toBitSet(); // They go from H ro R.
    bitmask = BitSetUtilities.asBigInteger(set);
    assertEquals(BigInteger.ZERO, bitmask.and(BigInteger.ONE));
    for (int i = Alphabet.C.ordinal(); i <= Alphabet.R.ordinal(); i++) {
      final BigInteger m = BigInteger.ONE.shiftLeft(i);
      assertEquals(m, bitmask.and(m));
    }
  }

  @Test
  public void testAsBigIntegerEnumSet() {
    final EnumSet<Alphabet> set = Alphabet.A.toEnumSet();
    final BigInteger bitmask = EnumBitSet.asBigInteger(set);
    assertEquals(BigInteger.ONE, bitmask);
  }

  @Test
  public void testAsBitSet() {
    final BitSet set = Element.Xe.toBitSet();
    assertEquals(1, set.cardinality());
    assertTrue(Element.Xe.elementOf(set));
  }

  @Test
  public void testAsBitSetArray() {
    BitSet bitset = EnumBitSet.asBitSet(Alphabet.A, Alphabet.B);
    assertTrue(bitset.get(0));
    assertTrue(bitset.get(1));
    assertFalse(bitset.get(2));

    bitset = EnumBitSet.asBitSet(new Alphabet[] {});
    assertFalse(bitset.get(0));
    assertFalse(bitset.get(1));
    assertFalse(bitset.get(2));
  }

  @Test
  public void testAsBitSetBigInteger() {
    BitSet bitset = BitSetUtilities.asBitSet(BigInteger.ZERO);
    assertTrue(bitset.isEmpty());

    bitset = BitSetUtilities.asBitSet(BigInteger.ONE);
    assertTrue(bitset.get(0));
    assertFalse(bitset.get(1));
    assertFalse(bitset.get(2));
    assertFalse(bitset.get(3));

    bitset = BitSetUtilities.asBitSet(BigInteger.ZERO);
    assertTrue(bitset.isEmpty());
    try {
      bitset = BitSetUtilities.asBitSet(BigInteger.valueOf(-1));
      fail("negative integer is not a mask!");
    } catch (final IllegalArgumentException e) {
      // OK
    }

    final BigInteger mask = Element.H.others().toBigInteger();
    bitset = BitSetUtilities.asBitSet(mask);
    assertFalse(bitset.get(Element.H.ordinal()));
    for (int index = Element.H.ordinal() + 1; index <= Element.R.ordinal(); index++)
      assertTrue(bitset.get(index));
  }

  @Test
  public void testAsBitSetEnumSet() {
    final EnumBitSet<Alphabet> set = Alphabet.A.others();
    final BitSet bitset = set.toBitSet();
    assertFalse(bitset.get(0));
    assertTrue(bitset.get(1));
    assertTrue(bitset.get(2));
    assertTrue(bitset.get(3));
  }

  @Test
  public void testAsBitSetLong() {
    final long mask = 0b1010_0110L;
    final BitSet bitset = BitSetUtilities.asBitSet(mask);
    assertFalse(bitset.get(0));
    assertTrue(bitset.get(1));
    assertTrue(bitset.get(2));
    assertFalse(bitset.get(3));
    assertFalse(bitset.get(4));
    assertTrue(bitset.get(5));
    assertFalse(bitset.get(6));
    assertTrue(bitset.get(7));
    assertEquals(4, bitset.cardinality());
  }

  @Test
  public void testAsEnumBitSetLongType() throws Exception {
    final EnumBitSet<Alphabet> set = EnumBitSet.asEnumBitSet(0b0110L, Alphabet.class);
    assertFalse(set.contains(Alphabet.A));// index 0
    assertTrue(set.contains(Alphabet.B));// index 1
    assertTrue(set.contains(Alphabet.C));// index 2
    assertFalse(set.contains(Alphabet.D));// index 3
  }

  @Test
  public void testAsEnumSet() {
    final EnumSet<Element> set = Element.Xe.toEnumSet();
    assertEquals(1, set.size());
    assertTrue(Element.Xe.elementOf(set));
  }

  @Test
  public void testAsEnumSetBigInteger() {
    final EnumSet<Alphabet> set = EnumBitSet.asEnumSet(BigInteger.valueOf(0b1001_0110L),
        Alphabet.class);
    assertFalse(set.contains(Alphabet.A));
    assertTrue(set.contains(Alphabet.B));
    assertTrue(set.contains(Alphabet.C));
    assertFalse(set.contains(Alphabet.D));
  }

  @Test
  public void testAsEnumSetBitSet() {
    final BitSet bitset = new BitSet(8);
    bitset.set(Alphabet.B.ordinal());
    bitset.set(Alphabet.C.ordinal());
    final EnumSet<Alphabet> set = EnumBitSet.asEnumSet(bitset, Alphabet.class);
    assertFalse(set.contains(Alphabet.A));// index 0
    assertTrue(set.contains(Alphabet.B));// index 1
    assertTrue(set.contains(Alphabet.C));// index 2
    assertFalse(set.contains(Alphabet.D));// index 3
  }

  @Test
  public void testAsEnumSetLong() {
    final EnumSet<Alphabet> set = EnumBitSet.asEnumSet(0b1001_0110L, Alphabet.class);
    assertFalse(set.contains(Alphabet.A));
    assertTrue(set.contains(Alphabet.B));
    assertTrue(set.contains(Alphabet.C));
    assertFalse(set.contains(Alphabet.D));
  }

  @Test
  public void testAsLongArray() {
    long bitmask64 = EnumBitSet.asLong(Alphabet.A);
    assertEquals(1, bitmask64);

    bitmask64 = EnumBitSet.asLong();
    assertEquals(0, bitmask64);
  }

  @Test
  public void testAsLongEnumSet() {
    final EnumSet<Alphabet> set = Alphabet.A.toEnumSet();
    final long bitmask64 = EnumBitSet.asLong(set);
    assertEquals(1, bitmask64);
  }

  @Test
  public void testBitmask() {
    final BigInteger bitmask = Element.Xe.bitmask();
    assertEquals(1, bitmask.bitCount());
  }

  @Test
  public void testBitmask64() {
    final long bitmask64 = Element.Xe.bitmask64();
    assertEquals(Element.Xe.bitmask64(), bitmask64);
    assertEquals(1L << Element.Xe.ordinal(), bitmask64);
  }

  @Test
  public void testClone() throws Exception {
    final EnumBitSet<Alphabet> alphabet = EnumBitSet.allOf(Alphabet.class);
    final EnumBitSet<Alphabet> clone = alphabet.clone();
    assertEquals(alphabet, clone);
    assertEquals(clone, alphabet);

    // Must be a new, independent set:
    clone.remove(Alphabet.B);
    assertFalse(clone.equals(alphabet));
    assertFalse(alphabet.equals(clone));
  }

  @Test
  public void testComplement() throws Exception {
    final EnumBitSet<Alphabet> complement = EnumBitSet.of(Alphabet.A, Alphabet.B, Alphabet.C)
        .complement();
    assertFalse(complement.contains(Alphabet.A));
    assertFalse(complement.contains(Alphabet.B));
    assertFalse(complement.contains(Alphabet.C));
    assertTrue(complement.contains(Alphabet.D));
    assertTrue(complement.contains(Alphabet.Z));
  }

  @Test
  public void testCross() throws Exception {
    final EnumBitSet<Element> el = EnumBitSet.of(Element.Ar, Element.Br, Element.Cr);
    final EnumBitSet<Planet> pl = EnumBitSet.of(Planet.MERCURY, Planet.EARTH, Planet.SATURN);

    final Collection<Pair<EnumBitSetHelper<?>, Element, Planet>> cross1 = el.cross(pl);
    assertEquals(el.size() * pl.size(), cross1.size()); // 9
    cross1.forEach(p -> p.forEach(e -> assertTrue(e.bitmask64() > 0)));

    assertTrue(cross1.contains(Pair.of(Element.Ar, Planet.SATURN)));
    final Collection<Pair<EnumBitSetHelper<?>, Planet, Element>> cross2 = pl.cross(el);
    assertTrue(cross2.contains(Pair.of(Planet.EARTH, Element.Br)));
    assertEquals(9, cross2.size());
    // swapping twice should result in the same set:
    final Set<Pair<EnumBitSetHelper<?>, Planet, Element>> cross3 = cross2.parallelStream()//
        .map(Pair::swap).map(Pair::swap).collect(Collectors.toSet());
    assertEquals(cross2, cross3);

    {
      final EnumBitSet<Element> elements = EnumBitSet.allOf(Element.class);
      final EnumBitSet<Alphabet> abc = EnumBitSet.allOf(Alphabet.class);
      final Set<Pair<?, Element, Alphabet>> set1 = new HashSet<>(elements.cross(abc));
      final Set<Pair<?, Element, Alphabet>> set2 = abc.cross(elements).stream().map(Pair::swap)
          .collect(Collectors.toSet());
      assertEquals(set1, set2);
    }
  }

  @Test
  public void testElementOfBigInteger() {
    final BigInteger mask = BigInteger.valueOf(2).pow(160).subtract(BigInteger.ONE);
    assertTrue(Element.C.elementOf(mask));
    assertTrue(Element.Sm.elementOf(mask));
    assertTrue(Element.Eu.elementOf(mask));
    assertTrue(Element.R.elementOf(mask));
    for (final Element e : Element.values())
      assertTrue(e.elementOf(mask));
  }

  @Test
  public void testElementOfBitSet() {
    final BitSet bitset = new BitSet(160);
    bitset.set(0, 159, true);
    assertTrue(Element.C.elementOf(bitset));
    assertTrue(Element.Sm.elementOf(bitset));
    assertTrue(Element.Eu.elementOf(bitset));
    assertTrue(Element.R.elementOf(bitset));
    for (final Element e : Element.values())
      assertTrue(e.elementOf(bitset));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testElementOfEnum() {
    assertTrue(Alphabet.P.elementOf(Alphabet.P));
    assertTrue(Element.P.elementOf(Element.P));
    assertTrue(Element.P.elementOf(Element.Mg, Element.Ar, Element.Co, Element.Ni, Element.P,
        Element.S));
    assertTrue(!Element.P.elementOf(Element.Mg));

    assertFalse(Alphabet.P.elementOf());
    assertFalse(Element.P.elementOf());
  }

  @Test
  public void testElementOfLong() {
    assertTrue(Element.H.elementOf(1));
    assertFalse(Element.H.elementOf(0));

    assertEquals(63, Element.Sm.ordinal());
    assertTrue(Element.Sm.elementOf(-9223372036854775808L));
    assertTrue(Element.H.elementOf(-1));
    assertTrue(Element.C.elementOf(-1));
    assertTrue(Element.Sm.elementOf(-1));
    // Eu has ordinal 64 and can't be in any 64bit mask:
    try {
      assertFalse(Element.Eu.elementOf(-1));
      fail("Element.Eu should have ordinal 64");
    } catch (final RuntimeException e) {
      // ok
    }

  }

  @Test
  public void testElementOfSetOfEnum() {
    {
      final EnumSet<Alphabet> set = EnumSet.allOf(Alphabet.class);
      assertTrue(Alphabet.A.elementOf(set));
      assertTrue(Alphabet.B.elementOf(set));
    }
    {
      final EnumSet<Planet> set = EnumSet.allOf(Planet.class);
      assertTrue(Planet.EARTH.elementOf(set));
      assertTrue(Planet.MARS.elementOf(set));
    }
    {
      final EnumSet<Element> set = EnumSet.noneOf(Element.class);
      assertFalse(Element.C.elementOf(set));
      set.add(Element.C);
      assertTrue(Element.C.elementOf(set));
    }
  }

  @Test
  public void testGetEnumTypeSize() throws Exception {
    assertEquals(26, EnumBitSet.noneOf(Alphabet.class).getEnumTypeSize());
    assertEquals(8, EnumBitSet.allOf(Planet.class).getEnumTypeSize());
  }

  @Test
  public void testIntersect() throws Exception {
    final EnumBitSet<Alphabet> none = EnumBitSet.noneOf(Alphabet.class);
    final EnumBitSet<Alphabet> a = EnumBitSet.of(Alphabet.A);
    final EnumBitSet<Alphabet> ab = EnumBitSet.of(Alphabet.A, Alphabet.B);
    final EnumBitSet<Alphabet> bc = EnumBitSet.of(Alphabet.B, Alphabet.C);
    final EnumBitSet<Alphabet> abc = EnumBitSet.of(Alphabet.A, Alphabet.B, Alphabet.C);
    final EnumBitSet<Alphabet> bcd = EnumBitSet.of(Alphabet.B, Alphabet.C, Alphabet.D);
    final EnumBitSet<Alphabet> abcd = EnumBitSet.of(Alphabet.A, Alphabet.B, Alphabet.C, Alphabet.D);

    assertEquals(a.toBigInteger(), Alphabet.A.intersect(BigInteger.ONE));
    assertEquals(none, Alphabet.A.intersect(Alphabet.B));

    assertEquals(none, Alphabet.A.intersect());
    assertEquals(none, Alphabet.A.intersect(Alphabet.B, Alphabet.C));
    assertEquals(a, Alphabet.A.intersect(abc));
    assertEquals(none.toLong(), Alphabet.A.intersect(0b0110L));
    assertEquals(a.toLong(), Alphabet.A.intersect(0b0111L));
    assertEquals(a.toEnumSet(), Alphabet.A.intersect(abc.toEnumSet()));
    assertEquals(a.toBitSet(), Alphabet.A.intersect(abc.toBitSet()));

    assertEquals(abc, abc.intersect(abc));
    assertEquals(none, abc.intersect(EnumBitSet.noneOf(Alphabet.class)));

    assertEquals(abc, abc.intersect(abc));
    assertEquals(abc, abc.intersect(abc.toBigInteger()));
    assertEquals(abc, abc.intersect(abc.toBitSet()));
    assertEquals(abc, abc.intersect(abc.toEnumSet()));
    assertEquals(abc, abc.intersect(abc.toLong()));

    assertEquals(ab, abc.intersect(ab));
    assertEquals(ab, abc.intersect(ab.toBigInteger()));
    assertEquals(ab, abc.intersect(ab.toBitSet()));
    assertEquals(ab, abc.intersect(ab.toEnumSet()));
    assertEquals(ab, abc.intersect(ab.toLong()));

    assertEquals(none, abc.intersect(none));
    assertEquals(none, abc.intersect(none.toBigInteger()));
    assertEquals(none, abc.intersect(none.toBitSet()));
    assertEquals(none, abc.intersect(none.toEnumSet()));
    assertEquals(none, abc.intersect(none.toLong()));

    assertEquals(abc, abc.intersect(abcd));
    assertEquals(abc, abc.intersect(abcd.toBigInteger()));
    assertEquals(abc, abc.intersect(abcd.toBitSet()));
    assertEquals(abc, abc.intersect(abcd.toEnumSet()));
    assertEquals(abc, abc.intersect(abcd.toLong()));

    assertEquals(bc, abc.intersect(bcd));
    assertEquals(bc, abc.intersect(bcd.toBigInteger()));
    assertEquals(bc, abc.intersect(bcd.toBitSet()));
    assertEquals(bc, abc.intersect(bcd.toEnumSet()));
    assertEquals(bc, abc.intersect(bcd.toLong()));

    assertEquals(bc, bcd.intersect(abc));
    assertEquals(bc, bcd.intersect(abc.toBigInteger()));
    assertEquals(bc, bcd.intersect(abc.toBitSet()));
    assertEquals(bc, bcd.intersect(abc.toEnumSet()));
    assertEquals(bc, bcd.intersect(abc.toLong()));

  }

  @Test
  public void testJust() throws Exception {
    final EnumBitSet<Alphabet> justA = EnumBitSet.just(Alphabet.A);
    assertEquals(1, justA.size());
    assertTrue(justA.contains(Alphabet.A));
  }

  @Test
  public void testMinus() throws Exception {
    final EnumBitSet<Alphabet> none = EnumBitSet.noneOf(Alphabet.class);
    final EnumBitSet<Alphabet> a = EnumBitSet.of(Alphabet.A);
    final EnumBitSet<Alphabet> c = EnumBitSet.of(Alphabet.C);
    final EnumBitSet<Alphabet> d = EnumBitSet.of(Alphabet.D);
    final EnumBitSet<Alphabet> ab = EnumBitSet.of(Alphabet.A, Alphabet.B);
    final EnumBitSet<Alphabet> bc = EnumBitSet.of(Alphabet.B, Alphabet.C);
    final EnumBitSet<Alphabet> abc = EnumBitSet.of(Alphabet.A, Alphabet.B, Alphabet.C);
    final EnumBitSet<Alphabet> bcd = EnumBitSet.of(Alphabet.B, Alphabet.C, Alphabet.D);
    final EnumBitSet<Alphabet> abcd = EnumBitSet.of(Alphabet.A, Alphabet.B, Alphabet.C, Alphabet.D);

    assertEquals(none, abc.minus(abc));
    assertEquals(abc, abc.minus(none));
    assertEquals(bc, bc.minus(none));
    assertEquals(abcd, abcd.minus(none));

    assertEquals(none, abc.minus(abc));
    assertEquals(none, abc.minus(abc.toBigInteger()));
    assertEquals(none, abc.minus(abc.toBitSet()));
    assertEquals(none, abc.minus(abc.toEnumSet()));
    assertEquals(none, abc.minus(abc.toLong()));

    assertEquals(c, abc.minus(ab));
    assertEquals(c, abc.minus(ab.toBigInteger()));
    assertEquals(c, abc.minus(ab.toBitSet()));
    assertEquals(c, abc.minus(ab.toEnumSet()));
    assertEquals(c, abc.minus(ab.toLong()));

    assertEquals(abc, abc.minus(none));
    assertEquals(abc, abc.minus(none.toBigInteger()));
    assertEquals(abc, abc.minus(none.toBitSet()));
    assertEquals(abc, abc.minus(none.toEnumSet()));
    assertEquals(abc, abc.minus(none.toLong()));

    assertEquals(none, none.minus(abc));
    assertEquals(none, none.minus(abc.toBigInteger()));
    assertEquals(none, none.minus(abc.toBitSet()));
    assertEquals(none, none.minus(abc.toEnumSet()));
    assertEquals(none, none.minus(abc.toLong()));

    assertEquals(d, abcd.minus(abc));
    assertEquals(d, abcd.minus(abc.toBigInteger()));
    assertEquals(d, abcd.minus(abc.toBitSet()));
    assertEquals(d, abcd.minus(abc.toEnumSet()));
    assertEquals(d, abcd.minus(abc.toLong()));

    assertEquals(a, abc.minus(bcd));
    assertEquals(a, abc.minus(bcd.toBigInteger()));
    assertEquals(a, abc.minus(bcd.toBitSet()));
    assertEquals(a, abc.minus(bcd.toEnumSet()));
    assertEquals(a, abc.minus(bcd.toLong()));

    assertEquals(d, bcd.minus(abc));
    assertEquals(d, bcd.minus(abc.toBigInteger()));
    assertEquals(d, bcd.minus(abc.toBitSet()));
    assertEquals(d, bcd.minus(abc.toEnumSet()));
    assertEquals(d, bcd.minus(abc.toLong()));

  }

  @Test
  public void testNoneOf() throws Exception {
    assertEquals(0, EnumBitSet.noneOf(Alphabet.class).size());
    assertTrue(EnumBitSet.noneOf(Element.class).isEmpty());
  }

  @Test
  public void testOf() throws Exception {
    final EnumBitSet<Alphabet> abc = EnumBitSet.of(Alphabet.A, Alphabet.B, Alphabet.C);
    assertTrue(abc.contains(Alphabet.A));
    assertTrue(abc.contains(Alphabet.B));
    assertTrue(abc.contains(Alphabet.C));
    assertFalse(abc.contains(Alphabet.D));
    assertFalse(abc.contains(Alphabet.Z));
  }

  @Test
  public void testOr64Long() {
    final long mask = Alphabet.B.union(0b0101L);
    assertEquals(1 + 2 + 4, mask);
  }

  @Test
  public void testOthers() {
    final EnumBitSet<Element> set = Element.C.others();
    assertTrue(Element.Ag.elementOf(set));
    assertFalse(Element.C.elementOf(set));
  }

  @Test
  public void testRemovedFromArray() {
    EnumBitSet<Element> set;
    set = Element.C.removedFrom(Element.C, Element.D, Element.Eu);
    assertEquals(2, set.size());

    set = Element.C.removedFrom(Element.Xe, Element.Y, Element.Zr);
    assertEquals(3, set.size());

    set = Element.Ag.removedFrom();
    assertEquals(0, set.size());
  }

  @Test
  public void testRemovedFromBigInteger() {
    assertEquals(BigInteger.ZERO, Alphabet.A.removedFrom(BigInteger.ONE));
    assertEquals(BigInteger.ONE, Alphabet.B.removedFrom(BigInteger.ONE));
  }

  @Test
  public void testRemovedFromEnumSetOfE() {
    EnumSet<Element> set = EnumSet.allOf(Element.class);
    final int total = set.size();
    set = Element.C.removedFrom(set);
    assertEquals(total - 1, set.size());
    set = Element.C.removedFrom(set);
    assertEquals(total - 1, set.size());
    set = Element.Ag.removedFrom(set);
    assertEquals(total - 2, set.size());
    set = Element.Xe.removedFrom(set);
    assertEquals(total - 3, set.size());
  }

  @Test
  public void testToBigInteger() throws Exception {
    final EnumBitSet<Alphabet> set = Alphabet.A.toEnumBitSet();
    final BigInteger bigInt = set.toBigInteger();
    assertEquals(BigInteger.ONE, bigInt);
  }

  @Test
  public void testToBinaryString() throws Exception {
    final EnumBitSet<Alphabet> abc = Alphabet.A.union(Alphabet.B, Alphabet.D);
    final String binary = abc.toBinaryString();
    assertTrue(binary.matches("^0+1011$"));
    assertEquals(EnumBitSet.allOf(Alphabet.class).size(), binary.length());
  }

  @Test
  public void testToEnumSet() throws Exception {
    final EnumBitSet<Alphabet> set = Alphabet.A.toEnumBitSet();
    final EnumSet<Alphabet> set2 = set.toEnumSet();
    assertEquals(set.toSet(), set2);
    assertNotEquals(set, set2);// set2 has no domain
    assertEquals(set2, set.toEnumSet());
    assertNotEquals(set2, set);// set is not a j.u.Set
    // Must be a new, independent set:
    set2.add(Alphabet.B);
    assertFalse(set2.equals(set.toSet()));
    assertFalse(set.toSet().equals(set2));
  }

  @Test
  public void testToString() throws Exception {
    final EnumBitSet<Alphabet> abc = Alphabet.A.union(Alphabet.B, Alphabet.C);
    assertEquals(abc.toString(), abc.toEnumSet().toString());
  }

  @Test
  public void testUnion() throws Exception {
    final EnumBitSet<Alphabet> none = EnumBitSet.noneOf(Alphabet.class);
    final EnumBitSet<Alphabet> a = EnumBitSet.of(Alphabet.A);
    final EnumBitSet<Alphabet> ab = EnumBitSet.of(Alphabet.A, Alphabet.B);
    final EnumBitSet<Alphabet> abc = EnumBitSet.of(Alphabet.A, Alphabet.B, Alphabet.C);
    final EnumBitSet<Alphabet> bcd = EnumBitSet.of(Alphabet.B, Alphabet.C, Alphabet.D);
    final EnumBitSet<Alphabet> abcd = EnumBitSet.of(Alphabet.A, Alphabet.B, Alphabet.C, Alphabet.D);

    assertEquals(a.toBigInteger(), Alphabet.A.union(BigInteger.ONE));
    assertEquals(ab, Alphabet.A.union(Alphabet.B));

    assertEquals(a, Alphabet.A.union());
    assertEquals(abc, Alphabet.A.union(Alphabet.B, Alphabet.C));
    assertEquals(abc, Alphabet.A.union(abc));
    assertEquals(abc.toLong(), Alphabet.A.union(0b0110L));
    assertEquals(abc.toLong(), Alphabet.A.union(0b0111L));
    assertEquals(abc.toEnumSet(), Alphabet.A.union(abc.toEnumSet()));
    assertEquals(abc.toBitSet(), Alphabet.A.union(abc.toBitSet()));

    assertEquals(abc, abc.union(abc));
    assertEquals(abc, abc.union(EnumBitSet.noneOf(Alphabet.class)));

    assertEquals(abc, abc.union(abc));
    assertEquals(abc, abc.union(abc.toBigInteger()));
    assertEquals(abc, abc.union(abc.toBitSet()));
    assertEquals(abc, abc.union(abc.toEnumSet()));
    assertEquals(abc, abc.union(abc.toLong()));

    assertEquals(abc, abc.union(ab));
    assertEquals(abc, abc.union(ab.toBigInteger()));
    assertEquals(abc, abc.union(ab.toBitSet()));
    assertEquals(abc, abc.union(ab.toEnumSet()));
    assertEquals(abc, abc.union(ab.toLong()));

    assertEquals(abc, abc.union(none));
    assertEquals(abc, abc.union(none.toBigInteger()));
    assertEquals(abc, abc.union(none.toBitSet()));
    assertEquals(abc, abc.union(none.toEnumSet()));
    assertEquals(abc, abc.union(none.toLong()));

    assertEquals(abcd, abc.union(abcd));
    assertEquals(abcd, abc.union(abcd.toBigInteger()));
    assertEquals(abcd, abc.union(abcd.toBitSet()));
    assertEquals(abcd, abc.union(abcd.toEnumSet()));
    assertEquals(abcd, abc.union(abcd.toLong()));

    assertEquals(abcd, abc.union(bcd));
    assertEquals(abcd, abc.union(bcd.toBigInteger()));
    assertEquals(abcd, abc.union(bcd.toBitSet()));
    assertEquals(abcd, abc.union(bcd.toEnumSet()));
    assertEquals(abcd, abc.union(bcd.toLong()));

    assertEquals(abcd, bcd.union(abc));
    assertEquals(abcd, bcd.union(abc.toBigInteger()));
    assertEquals(abcd, bcd.union(abc.toBitSet()));
    assertEquals(abcd, bcd.union(abc.toEnumSet()));
    assertEquals(abcd, bcd.union(abc.toLong()));

  }

  @Test
  public void testXor() {
    assertEquals(BigInteger.ZERO, Alphabet.A.xor(BigInteger.ONE));
    assertEquals(BigInteger.valueOf(3), Alphabet.B.xor(BigInteger.ONE));
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Test
  public <E extends Enum<E> & EnumBitSetHelper<E>> void testZipWithPosition() throws Exception {
    final List<DomainBitSet<E>> list = new LinkedList<>();
    for (final Class c : asList(Alphabet.class, Element.class, Suit.class)) {
      final EnumBitSet<E> allOf = EnumBitSet.allOf(c);
      list.add(allOf);
      list.add(allOf.complement());
      list.add(allOf.minus(BigInteger.valueOf(0b1010L)));
      list.add(allOf.minus(BigInteger.valueOf(0b0101L)));
      list.add(allOf.minus(allOf.stream().limit(10).collect(Collectors.toList())));
      list.add(allOf.minus(allOf.stream().filter(x -> x.bitmask().isProbablePrime(1))
          .collect(Collectors.toList())));
    }
    for (final DomainBitSet<? extends Enum<?>> s : list) {
      s.zipWithPosition().forEach(p -> assertEquals((int) p.first, p.second.ordinal()));
      assertEquals(s.size(), s.zipWithPosition().count());
    }
  }
}
