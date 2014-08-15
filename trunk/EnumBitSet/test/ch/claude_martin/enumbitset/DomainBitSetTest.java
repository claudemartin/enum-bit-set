package ch.claude_martin.enumbitset;

import static ch.claude_martin.enumbitset.EnumBitSetTest.Element.Ac;
import static ch.claude_martin.enumbitset.EnumBitSetTest.Element.Ba;
import static ch.claude_martin.enumbitset.EnumBitSetTest.Element.Pr;
import static ch.claude_martin.enumbitset.EnumBitSetTest.Element.Zr;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collector.Characteristics;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import org.junit.Before;
import org.junit.Test;

import ch.claude_martin.enumbitset.EnumBitSetTest.Alphabet;
import ch.claude_martin.enumbitset.EnumBitSetTest.Planet;
import ch.claude_martin.enumbitset.EnumBitSetTest.Rank;

@SuppressWarnings("static-method")
public class DomainBitSetTest {

  static final class TestBitSet<T> implements DomainBitSet<T> {

    public static <T> TestBitSet<T> of(Domain<T> domain, Collection<T> set) {
      Map<Integer, T> m = set.stream().map(x -> Pair.of(domain.indexOf(x), x))
          .collect(Collectors.toMap(Pair::_1, Pair::_2));
      return new TestBitSet<>(domain, m);
    }

    private TestBitSet(Domain<T> domain, Map<Integer, T> map) {
      super();
      this.domain = domain;
      this.map = Collections.unmodifiableMap(map);
    }

    final Domain<T>       domain;
    final Map<Integer, T> map;

    @Override
    public TestBitSet<T> clone() {
      return (TestBitSet<T>) DomainBitSet.super.clone();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object o) {
      return this == o || o instanceof DomainBitSet && this.ofEqualDomain((DomainBitSet<T>) o)
          && this.ofEqualElements((DomainBitSet<T>) o);
    }

    @Override
    public int hashCode() {
      return this.map.hashCode();
    }

    @Override
    public boolean getBit(int bitIndex) throws IndexOutOfBoundsException {
      return this.map.containsKey(bitIndex);
    }

    @Override
    public Domain<T> getDomain() {
      return this.domain;
    }

    @Override
    public DomainBitSet<T> intersect(BigInteger mask) {
      return this.intersect(BitSetUtilities.asBitSet(mask));
    }

    @Override
    public DomainBitSet<T> intersect(BitSet set) {
      Map<Integer, T> m = this.map.keySet().stream().filter(i -> set.get(i))
          .collect(Collectors.toMap(Function.identity(), this.map::get));
      return new TestBitSet<>(this.domain, m);
    }

    @Override
    public DomainBitSet<T> intersect(Iterable<T> set) throws IllegalArgumentException {
      Map<Integer, T> m = new HashMap<>();
      StreamSupport.stream(set.spliterator(), false).filter(this.map.values()::contains)
          .forEach(x -> m.put(this.domain.indexOf(x), x));
      return new TestBitSet<>(this.domain, m);
    }

    @Override
    public DomainBitSet<T> intersect(long mask) throws MoreThan64ElementsException {
      return this.intersect(BitSetUtilities.asBigInteger(mask));
    }

    @Override
    public Iterator<T> iterator() {
      return this.map.values().iterator();
    }

    @Override
    public DomainBitSet<T> minus(BigInteger mask) {
      return this.minus(BitSetUtilities.asBitSet(mask));
    }

    @Override
    public DomainBitSet<T> minus(BitSet set) {
      Map<Integer, T> m = this.map.keySet().stream().filter(set::get)
          .collect(Collectors.toMap(Function.identity(), this.map::get));
      return new TestBitSet<>(this.domain, m);
    }

    @Override
    public DomainBitSet<T> minus(Iterable<T> set) {
      Set<T> s = new HashSet<>();
      set.forEach(s::add);
      Map<Integer, T> m = this.map.values().stream().filter(t -> !s.contains(t))
          .collect(Collectors.toMap(this.domain::indexOf, Function.identity()));
      return new TestBitSet<>(this.domain, m);
    }

    @Override
    public DomainBitSet<T> minus(long mask) throws MoreThan64ElementsException {
      return this.minus(BitSetUtilities.asBigInteger(mask));
    }

    @Override
    public int size() {
      return this.map.size();
    }

    @Override
    public BigInteger toBigInteger() {
      return BitSetUtilities.asBigInteger(this.toBitSet());
    }

    @Override
    public BitSet toBitSet() {
      BitSet bs = new BitSet();
      this.map.keySet().forEach(bs::set);
      return bs;
    }

    @Override
    public long toLong() throws MoreThan64ElementsException {
      return BitSetUtilities.asLong(this.toBigInteger());
    }

    @Override
    public Set<T> toSet() {
      return new HashSet<>(this.map.values());
    }

    @Override
    public String toString() {
      return this.map.values().toString();
    }

    @Override
    public DomainBitSet<T> union(BigInteger mask) {
      return this.union(BitSetUtilities.asBitSet(mask));
    }

    @Override
    public DomainBitSet<T> union(BitSet set) {
      Map<Integer, T> m = new HashMap<>(this.map);
      set.stream().mapToObj(i -> Pair.of(i, this.domain.get(i))).filter(p -> set.get(p.first))
          .forEach(p -> m.put(p.first, p.second));
      return new TestBitSet<>(this.domain, m);
    }

    @Override
    public DomainBitSet<T> union(Iterable<T> set) {
      Map<Integer, T> m = new HashMap<>(this.map);
      StreamSupport.stream(set.spliterator(), false).map(x -> Pair.of(this.domain.indexOf(x), x))
          .forEach(p -> m.put(p.first, p.second));
      return new TestBitSet<>(this.domain, m);
    }

    @Override
    public DomainBitSet<T> union(long mask) throws MoreThan64ElementsException {
      return this.union(BitSetUtilities.asBigInteger(mask));
    }

  }

  Domain<Integer>            domain1234;
  DomainBitSet<Integer>      oneTo4, none, twoThree, oneTwo, threeFour;
  Set<DomainBitSet<Integer>> all;

  @Before
  public void before() {
    this.domain1234 = DefaultDomain.of(asList(1, 2, 3, 4));
    this.oneTo4 = TestBitSet.of(this.domain1234, asList(1, 2, 3, 4));
    this.twoThree = TestBitSet.of(this.domain1234, asList(2, 3));
    this.oneTwo = TestBitSet.of(this.domain1234, asList(1, 2));
    this.threeFour = TestBitSet.of(this.domain1234, asList(3, 4));
    this.none = TestBitSet.of(this.domain1234, asList());
    this.all = new HashSet<>(asList(this.oneTo4, this.none, this.twoThree, this.oneTwo,
        this.threeFour));
  }

  @Test
  public final void testAllOf() {
    DomainBitSet<Integer> a1 = DomainBitSet.allOf(asList(1, 2, 3));
    DomainBitSet<Integer> a2 = DomainBitSet.allOf(1, 2, 3);
    assertEquals(a1, a2);
  }

  @Test
  public final void testCreateMultiEnumBitSet() {
    DomainBitSet<Enum<?>> s1 = DomainBitSet.createMultiEnumBitSet(Alphabet.class, Planet.class,
        Rank.class);
    assertTrue(s1.isEmpty());
    assertEquals(s1.complement(), s1.union(s1.getDomain()));
  }

  @Test
  public final void testNoneOf() {
    DomainBitSet<Integer> n1 = DomainBitSet.noneOf(asList(1, 2, 3));
    DomainBitSet<Integer> n2 = DomainBitSet.noneOf(1, 2, 3);
    assertEquals(n1, n2);
    assertTrue(n1.isEmpty());
  }

  @Test
  public final void testClone() {
    for (DomainBitSet<Integer> s : this.all) {
      DomainBitSet<Integer> s2 = s.clone();
      assertNotSame(s, s2);
      assertEquals(s, s2);
    }
  }

  @Test
  public final void testComplement() {
    for (DomainBitSet<Integer> s : this.all) {
      assertEquals(s, s.complement().complement());
    }
    assertEquals(this.oneTwo, this.threeFour.complement());
  }

  @Test
  public final void testContains() {
    for (DomainBitSet<Integer> s : this.all)
      for (Integer i : s.getDomain())
        assertEquals(s.toSet().contains(i), s.contains(i));
    assertTrue(this.oneTo4.contains(1));
    assertFalse(this.none.contains(1));
  }

  @Test
  public final void testContainsAll() {
    assertTrue(this.oneTo4.containsAll(this.oneTo4.getDomain()));
    assertFalse(this.none.containsAll(this.oneTo4.getDomain()));
  }

  @Test
  public final void testCross() {
    assertTrue(this.none.cross(this.oneTo4).isEmpty());
    assertTrue(this.oneTo4.cross(this.none).isEmpty());
    assertTrue(this.oneTwo.cross(this.twoThree).contains(Pair.of(1, 3)));
    Set<Pair<Integer, Integer, Integer>> set = new HashSet<>();
    this.oneTwo.cross(this.twoThree, Pair.curry(set::add)::apply);
    assertEquals(set, this.oneTwo.cross(this.twoThree));
  }

  @Test
  public final void testDomainContains() {
    for (DomainBitSet<Integer> s : this.all)
      for (Integer i : s.getDomain())
        assertTrue(s.domainContains(i));
    assertFalse(this.oneTo4.domainContains(7));
  }

  @Test
  public final void testGetElement() {
    for (DomainBitSet<Integer> s : this.all) {
      Domain<Integer> d = s.getDomain();
      for (Integer i : d) {
        Optional<Integer> element = s.getElement(d.indexOf(i));
        assertTrue(element.isPresent() == s.contains(i));
        if (element.isPresent())
          assertSame(i, element.get());
      }
    }
  }

  @Test
  public final void testIntersectVarArgs() {
    DomainBitSet<Integer> s;
    s = this.none.intersectVarArgs(1, 2, 3);
    assertEquals(this.none, s);
    s = this.oneTo4.intersectVarArgs(1, 2);
    assertEquals(this.oneTwo, s);
    s = this.threeFour.intersectVarArgs(2, 3);
    assertEquals(TestBitSet.of(this.domain1234, asList(3)), s);
  }

  @Test
  public final void testIsEmpty() {
    assertTrue(this.none.isEmpty());
    assertFalse(this.oneTo4.isEmpty());
    assertFalse(this.oneTwo.isEmpty());
  }

  @Test
  public final void testMap() {
    DefaultDomain<Character> domainABCD = DefaultDomain.of(asList('A', 'B', 'C', 'D'));
    DomainBitSet<Character> set;

    set = this.none.map(domainABCD);
    assertEquals(Collections.emptySet(), set.toSet());

    set = this.oneTo4.map(domainABCD);
    assertEquals(TestBitSet.of(domainABCD, domainABCD), set);

    set = this.twoThree.map(domainABCD);
    assertEquals(TestBitSet.of(domainABCD, asList('B', 'C')), set);
  }

  @Test
  public final void testMinusVarArgs() {
    DomainBitSet<Integer> set;
    set = this.none.minusVarArgs(1, 2, 3, 4);
    assertEquals(this.none, set);
    set = this.oneTo4.minusVarArgs(1, 2, 3, 4);
    assertEquals(this.none, set);
    set = this.oneTo4.minusVarArgs(1, 2);
    assertEquals(this.threeFour, set);
    set = this.oneTo4.minusVarArgs(3, 4);
    assertEquals(this.oneTwo, set);
    set = this.oneTo4.minusVarArgs(1, 4);
    assertEquals(this.twoThree, set);
  }

  @Test
  public final void testOfEqualDomain() {
    for (DomainBitSet<Integer> s1 : this.all)
      for (DomainBitSet<Integer> s2 : this.all)
        assertTrue(s1.ofEqualDomain(s2));
    assertTrue(this.none.ofEqualDomain(TestBitSet.of(
        DefaultDomain.of(asList(this.domain1234.toArray(new Integer[4]))), Collections.emptySet())));
    assertFalse(this.none.ofEqualDomain(TestBitSet.of(DefaultDomain.of(asList(6, 7, 8)),
        Collections.emptySet())));
  }

  @Test
  public final void testOfEqualElements() {
    DefaultDomain<Integer> domain12345 = DefaultDomain.of(asList(1, 2, 3, 4, 5));
    assertTrue(this.none.ofEqualElements(TestBitSet.of(domain12345, Collections.emptySet())));
    assertTrue(this.oneTo4.ofEqualElements(TestBitSet.of(domain12345, asList(1, 2, 3, 4))));
    for (DomainBitSet<Integer> s : this.all)
      assertTrue(s.ofEqualElements(s));
  }

  @Test
  public final void testSemijoin() {
    for (DomainBitSet<Integer> s : this.all) {
      DomainBitSet<Integer> s2;
      s2 = s.semijoin(this.oneTo4, (a, b) -> this.oneTo4.contains(a));
      assertEquals(s, s2);
      s2 = s.semijoin(this.oneTo4, (a, b) -> false);
      assertEquals(this.none, s2);
      s2 = s.semijoin(this.oneTo4, (a, b) -> true);
      assertEquals(s, s2);
    }
  }

  @Test
  public final void testUnionVarArgs() {
    for (DomainBitSet<Integer> s : this.all) {
      assertEquals(this.oneTo4, s.unionVarArgs(1, 2, 3, 4));
      assertEquals(s, s.unionVarArgs());
    }
  }

  @Test
  public final void testZipWithPosition() {
    for (DomainBitSet<Integer> s : this.all) {
      Collector<Integer, ?, Set<Integer>> toSet = Collectors.toSet();
      assertEquals(s.toSet(), s.zipWithPosition().map(Pair::_2).collect(toSet));
      assertEquals(s.stream().map(s.getDomain()::indexOf).collect(toSet),
          s.zipWithPosition().map(Pair::_1).collect(toSet));
    }
  }

  @Test
  public void testPowerset() throws Exception {

    {
      GeneralDomainBitSet<?> genrl = GeneralDomainBitSet.allOf(1, 2, 3);
      EnumBitSet<?> enum1 = EnumBitSet.allOf(EnumBitSetTest.Suit.class);
      EnumBitSet<?> enum2 = EnumBitSet.allOf(EnumBitSetTest.Rank.class);
      EnumBitSet<?> enum3 = EnumBitSet.of(Ac, Ba, Pr, Zr);
      Object[] zeroTo15 = IntStream.rangeClosed(0, 15).mapToObj(Integer::valueOf).toArray();
      SmallDomainBitSet<?> small1 = SmallDomainBitSet.allOf(zeroTo15);
      SmallDomainBitSet<?> small2 = SmallDomainBitSet.of(small1.getDomain(), 0b101010L);
      SmallDomainBitSet<?> empty = SmallDomainBitSet.noneOf(small1);

      ArrayList<DomainBitSet<?>> powerset = new ArrayList<>();
      for (DomainBitSet<?> set : asList(genrl, enum1, enum2, enum3, small1, small2, empty)) {
        powerset.clear();
        set.powerset().forEach(powerset::add);
        // System.out.println(powerset);
        // Correct would be this but we don't test such large powersets:
        // final BigInteger size = BigInteger.ONE.shiftLeft(DomainBitSet.this.size());
        long size = 1L << set.size();
        assertEquals(size, powerset.size());

        // Count sets with size 2:
        List<?> list = StreamSupport
            .stream(Spliterators.spliterator(set.powerset().iterator(), size, 0), false)
            .filter(s -> s.size() == 2).collect(Collectors.toList());
        // Can be calculated in O(1):
        assertEquals((set.size() * (set.size() - 1)) / 2, list.size());
      }
    }
    {
      // Test powerset of large set (65 or more elements) must fail!
      Object[] zeroTo64 = IntStream.rangeClosed(0, 64).mapToObj(Integer::valueOf).toArray();
      DomainBitSet<?> large = GeneralDomainBitSet.allOf(zeroTo64);

      try {
        large.powerset();
        fail("powerset of [0..64] is too large!");
      } catch (MoreThan64ElementsException e) {
        // expected
      }
      
      try {
        large.powerset(s->{}, true);
        fail("powerset of [0..64] is too large!");
      } catch (MoreThan64ElementsException e) {
        // expected
      }
    }
    
    {
      // parallel powerset should be rather fast with 16 elements:
      Object[] zeroTo63 = IntStream.range(0, 64).mapToObj(Integer::valueOf).toArray();
      DomainBitSet<?> large = SmallDomainBitSet.noneOf(zeroTo63);
      large = large.union(0b1000100010001000100010001000100010001000100010001000100010001000L);
      final AtomicReference<BigInteger> actual = new AtomicReference<>(BigInteger.ZERO);
      final BigInteger expeted = BigInteger.ONE.shiftLeft(large.size());
      large.powerset(s->{
        actual.updateAndGet(i->i.add(BigInteger.ONE));
      }, true);
      assertEquals(expeted, actual.get());
    }
    {
      SmallDomainBitSet<Integer> set = SmallDomainBitSet.of(asList(1, 2, 3, 4, 5, 6), 0b101010L);
      // set.powerset() should be:
      // {{}, {2}, {4}, {2, 4}, {6}, {2, 6}, {4, 6}, {2, 4, 6}}
      
      // sequential:
      ArrayList<DomainBitSet<Integer>> powerset1 = new ArrayList<>();
      set.powerset().forEach(powerset1::add);
      
      // parallel:
      List<DomainBitSet<Integer>> powerset2 = Collections.synchronizedList(new ArrayList<>());
      set.powerset(powerset2::add, true);
      for (List<DomainBitSet<Integer>> powerset : asList(powerset1, powerset2)) {
        long size = 1L << set.size();
        assertEquals(size, powerset.size());
        
        assertTrue(powerset.contains(set.intersect(0b000000L)));
        assertTrue(powerset.contains(set.intersect(0b000010L)));
        assertTrue(powerset.contains(set.intersect(0b001000L)));
        assertTrue(powerset.contains(set.intersect(0b001010L)));
        assertTrue(powerset.contains(set.intersect(0b100000L)));
        assertTrue(powerset.contains(set.intersect(0b100010L)));
        assertTrue(powerset.contains(set.intersect(0b101000L)));
        assertTrue(powerset.contains(set.intersect(0b101010L)));
      }
    }
    

  }

}
