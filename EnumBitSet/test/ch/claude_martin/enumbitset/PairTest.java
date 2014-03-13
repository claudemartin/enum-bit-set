package ch.claude_martin.enumbitset;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import ch.claude_martin.enumbitset.EnumBitSetTest.Alphabet;
import ch.claude_martin.enumbitset.EnumBitSetTest.Planet;

@SuppressWarnings("static-method")
public class PairTest {

  @Test
  public final void testClone() {
    final Pair<?, String, Integer> p = Pair.of("foo", 42);
    assertTrue(p == p.clone());
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Test
  public final void testCurry() {
    final Pair p = Pair.of("foo", 42);
    final Object snd1 = p.applyTo(Pair.curry(x -> x.second));
    final Object snd2 = Pair.uncurry((a, b) -> b).apply(p);
    final Object snd3 = p.applyTo(Pair.curry(Pair::_2));
    assertEquals(snd1, snd2);
    assertEquals(snd1, snd3);
  }

  @Test
  public final void testEqualsObject() {
    final Pair<?, String, Integer> p1 = Pair.of("foo", 42);
    final Pair<?, String, Integer> p2 = Pair.of(new String("foo"), 42);
    assertEquals(p1, p1);
    assertEquals(p1, p2);
    assertEquals(p2, p1);
    assertEquals(p2, p2);
    assertFalse(p1.equals(null));
    assertFalse(p1.equals("X"));
  }

  @Test
  public final void testHashCode() {
    final Pair<?, String, Integer> p1 = Pair.of("foo", 42);
    final Pair<?, String, Integer> p2 = Pair.of(new String("foo"), 42);
    assertEquals(p1.hashCode(), p1.hashCode());
    assertEquals(p1.hashCode(), p2.hashCode());
    assertEquals(p2.hashCode(), p2.hashCode());
  }

  @Test
  public void testIterator() throws Exception {
    final Pair<Number, Double, Integer> p = Pair.of(Math.PI, 42);
    final List<?> list = Arrays.asList(p.toArray());
    for (final Number num : p)
      assertTrue(list.contains(num));

    try {
      final Iterator<Number> itr = p.iterator();
      for (int i = 0; i < 19; i++)
        itr.next();
      fail("expected: NoSuchElementException ");
    } catch (final NoSuchElementException e) {
    }

    final Object[] array = p.stream().distinct().toArray();
    assertArrayEquals(p.toArray(), array);
  }

  @Test
  public void testOf() throws Exception {
    {
      final Pair<Number, Integer, Double> p1 = Pair.of(1, 0.5);
      final Pair<Number, Integer, Double> p2 = Pair.of(Number.class, 1, 0.5);
      assertEquals(p1, p2);
      final Pair<?, ?, ?> p3 = Pair.of(Pair.of(1, 2), Pair.of(3, 4));
      final Pair<?, ?, ?> p4 = Pair.of(Pair.class, Pair.of(Integer.class, 1, 2),
          Pair.of(Integer.class, 3, 4));
      assertEquals(p3, p4);
    }
  }

  @Test
  public final void testPair() {
    Pair.of("foo", "bar");

    final Pair<Enum<?>, Alphabet, Planet> p2 = Pair.of(EnumBitSetTest.Alphabet.A,
        EnumBitSetTest.Planet.EARTH);
    for (final Enum<?> e : p2)
      assertNotNull(e);

    try {
      Pair.of("foo", null);
      fail("null");
    } catch (final NullPointerException e) {

    }
    try {
      Pair.of(null, "foo");
      fail("null");
    } catch (final NullPointerException e) {

    }
  }

  @Test
  public final void testSwap() {
    final Pair<?, String, Integer> p = Pair.of("foo", 42);
    assertEquals(p, p.swap().swap());
    assertEquals(p.swap(), p.swap());
    final Pair<?, Integer, String> swap = p.swap();
    assertEquals(p.first, swap.second);
    assertEquals(p.second, swap.first);
  }

  @Test
  public final void testToArray() {
    final Pair<?, String, Integer> p = Pair.of("foo", 42);
    final Object[] array = p.toArray();
    assertEquals(2, array.length);
    assertEquals(p.first, array[0]);
    assertEquals(p.second, array[1]);
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Test
  public final void testToString() {
    final Pair<?, String, Integer> p = Pair.of("foo", 42);
    final String string = p.toString();
    assertTrue(string.startsWith(Pair.class.getSimpleName()));
    assertTrue(string.contains(p.first));
    assertTrue(string.contains(p.second.toString()));

    // Recursion:
    final Pair<Object, ArrayList, AtomicReference<Object>> p2 = Pair.of(new ArrayList(),
        new AtomicReference<>());
    p2.first.add(p2);
    p2.second.set(p2);
    try {
      p2.toString();
    } catch (final Throwable e) {
      fail("Pair.toString failed: " + e);
    }
  }
}
