package ch.claude_martin.enumbitset;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

@SuppressWarnings("static-method")
public class PairTest {

  @Test
  public final void testClone() {
    final var p = Pair.of("foo", 42);
    assertTrue(p == p.clone());
  }

  @Test
  public final void testCurry() {
    { // Basic test of curry and uncurry.
      final var p = Pair.of("foo", 42);
      final var snd1 = p.applyTo(Pair.curry(x -> x.second));
      final var snd2 = Pair.uncurry((final String a, final Integer b) -> b).apply(p);
      final var snd3 = p.applyTo(Pair.curry(Pair::_2));
      assertEquals(snd1, snd2);
      assertEquals(snd1, snd3);
    }
    {
      final var p = Pair.of("bla", 1);
      final var set = new HashSet<Pair<Object, String, Integer>>();
      final var add = Pair.curry(set::add);
      p.applyTo(add);
      assertEquals(new HashSet<>(asList(p)), set);

      final BiFunction<String, Integer, Boolean> contains = Pair.curry(set::contains);
      // Three ways to see if 'set' contains 'p':
      assertTrue(contains.apply(p.first, p.second));
      assertTrue(p.applyTo(contains));
      assertTrue(Pair.uncurry(contains).apply(p));
    }
    { // With more complex types:
      final Domain<? extends Number> domain = DefaultDomain.of(asList(1, 2, 3));
      final List<? super Integer> list = new ArrayList<>(asList(1, 2, 3));
      final Pair<?, Domain<? extends Number>, List<? super Integer>> p = Pair.of(domain, list);

      final Set<Pair<Object, Domain<? extends Number>, List<? super Integer>>> set = new HashSet<>();
      final var add = Pair .curry(set::add);
      p.applyTo(add);
      assertEquals(new HashSet<>(asList(p)), set);
    }
  }

  @Test
  public final void testEqualsObject() {
    final var p1 = Pair.of("foo", Integer.MAX_VALUE - 4321);
    final var p2 = Pair.of(new String("foo"), Integer.valueOf(Integer.MAX_VALUE - 4321));
    assertEquals(p1, p1);
    assertEquals(p1, p2);
    assertEquals(p2, p1);
    assertEquals(p2, p2);
    assertFalse(p1.equals(null));
    assertFalse("X".equals(p1));
  }

  @Test
  public final void testHashCode() {
    final var p1 = Pair.of("foo", Integer.MAX_VALUE - 1234);
    final var p2 = Pair.of(new String("foo"), Integer.valueOf(Integer.MAX_VALUE - 1234));
    assertEquals(p1.hashCode(), p1.hashCode());
    assertEquals(p1.hashCode(), p2.hashCode());
    assertEquals(p2.hashCode(), p2.hashCode());
  }

  @Test
  public void testIterator() throws Exception {
    final var p = Pair.of(Math.PI, 42);
    final var list = Arrays.asList(p.toArray());
    for (final Number num : p)
      assertTrue(list.contains(num));

    assertThrows(NoSuchElementException.class, () -> {
      final var itr = p.iterator();
      for (int i = 0; i < 19; i++)
        itr.next();
    });

    final Object[] array = p.stream().distinct().toArray();
    assertArrayEquals(p.toArray(), array);
  }

  @Test
  public void testOf() throws Exception {
    {
      final var p1 = Pair.of(1, 0.5);
      final var p2 = Pair.of(Number.class, 1, 0.5);
      assertEquals(p1, p2);
      final var p3 = Pair.of(Pair.of(1, 2), Pair.of(3, 4));
      final var p4 = Pair.of(Pair.class, Pair.of(Integer.class, 1, 2),
          Pair.of(Integer.class, 3, 4));
      assertEquals(p3, p4);
    }
  }

  @Test
  public final void testPair() {
    Pair.of("foo", "bar");

    final var p2 = Pair.of(EnumBitSetTest.Alphabet.A,
        EnumBitSetTest.Planet.EARTH);
    for (final var e : p2)
      assertNotNull(e);

    assertThrows(NullPointerException.class, () -> Pair.of("foo", null));
    assertThrows(NullPointerException.class, () -> Pair.of(null, "foo"));
  }

  @Test
  public final void testSwap() {
    final var p = Pair.of("foo", 42);
    assertNotEquals(p, p.swap());
    assertEquals(p, p.swap().swap());
    assertEquals(p, p.swap().swap().swap().swap());
    assertEquals(p.swap(), p.swap());
    final var swap = p.swap();
    assertEquals(p.first, swap.second);
    assertEquals(p.second, swap.first);
  }

  @Test
  public final void testToArray() {
    final var p = Pair.of("foo", 42);
    final Object[] array = p.toArray();
    assertEquals(2, array.length);
    assertEquals(p.first, array[0]);
    assertEquals(p.second, array[1]);
  }

  @Test
  public final void testToString() {
    final var p = Pair.of("foo", 42);
    final String string = p.toString();
    assertTrue(string.startsWith(Pair.class.getSimpleName()));
    assertTrue(string.contains(p.first));
    assertTrue(string.contains(p.second.toString()));
  }

  @Test
  public final void testCompareTo() throws Exception {
    final var a = Pair.of(5, "A");
    final var b = Pair.of(-7, "B");
    final var c = Pair.of(5, "C");

    final Object[] array = asList(a, b, c).stream().sorted().toArray();
    // b, a, c
    assertSame(b, array[0]);
    assertSame(a, array[1]);
    assertSame(c, array[2]);
  }

  @Test
  public final void testComparing() throws Exception {
    final var a = Pair.of(42, Math.PI);
    final var b = Pair.of(-7, Math.E);
    final var c = Pair.of(0, Math.sqrt(2));
    final var list = asList(a,b,c);
    
    list.sort(Pair.comparingByFirst());
    assertArrayEquals(new Object[] { b, c, a }, list.toArray());
    list.sort(Pair.comparingBySecond());
    assertArrayEquals(new Object[] { c, b, a }, list.toArray());
    list.sort(Pair.comparingByFirst((x, y) -> Integer.compare(y, x)));
    assertArrayEquals(new Object[] { a, c, b }, list.toArray());
    list.sort(Pair.comparingBySecond((x, y) -> Double.compare(y, x)));
    assertArrayEquals(new Object[] { a, b, c }, list.toArray());
  }

  @Test
  public final void testToMap() throws Exception {
    final Pair<String, String, String> p = Pair.of("a", "b");
    final Map<Boolean, String> map = p.toMap();
    assertEquals("a", map.get(false));
    assertEquals("b", map.get(true));
    assertEquals(2, map.size());
    assertThrows(UnsupportedOperationException.class, () -> map.put(true, "x"),
        "Pair.toMap must return immutable map");

    for (final var b : map.keySet()) {
      assertNotNull(b);
      assertNotNull(map.get(b));
    }
    for (final var s : map.values())
      assertNotNull(s);
    for (final var e : map.entrySet())
      assertEquals(map.get(e.getKey()), e.getValue());
  }

  @Test
  public final void testOfMap() throws Exception {
    {
      Pair<String, String, String> p1, p2;
      p1 = Pair.of("a", "b");
      p2 = Pair.ofMap(p1.toMap());
      assertEquals(p1, p2);
      assertThrows(NullPointerException.class, () ->Pair.ofMap(null));

      final Map<Boolean, String> map = new HashMap<>();
      assertThrows(NullPointerException.class, () ->Pair.ofMap(map));

      map.put(false, "a");
      map.put(true, "b");

      p1 = Pair.ofMap(map);
      assertEquals(p2, p1);
    }

    {
      Pair<List<String>, List<String>, List<String>> p1, p2;
      p1 = Pair.of(asList("a"), asList("b"));
      final var map = Stream.of("a", "b")
          .collect(Collectors.partitioningBy(e -> "b".equals(e)));
      p2 = Pair.ofMap(map);
      assertEquals(p1, p2);
    }

  }
}
