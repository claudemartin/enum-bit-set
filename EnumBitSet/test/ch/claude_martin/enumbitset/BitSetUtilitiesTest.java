package ch.claude_martin.enumbitset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import org.junit.Test;

import ch.claude_martin.enumbitset.EnumBitSetTest.Element;
import ch.claude_martin.enumbitset.annotations.SuppressFBWarnings;

@SuppressFBWarnings("static-method")
public class BitSetUtilitiesTest {

  @Test
  public void testAsBigIntegerBitSet() {
    final BitSet bitset = new BitSet();
    BigInteger bi = BigInteger.valueOf(0L);
    for (int i = 0; i < 10000; i++) {
      assertEquals(bi, BitSetUtilities.asBigInteger(bitset));
      bitset.set(i);
      bi = bi.setBit(i);
    }
    try {
      BitSetUtilities.asBigInteger(null);
      fail();
    } catch (final NullPointerException e) {
    }
  }

  @Test
  public void testAsBigIntegerLong() {
    assertEquals(BigInteger.ZERO, BitSetUtilities.asBigInteger(0L));
    assertEquals(BigInteger.ONE, BitSetUtilities.asBigInteger(1L));
    assertEquals(BigInteger.TEN, BitSetUtilities.asBigInteger(10L));
    assertEquals(BigInteger.valueOf(Long.MAX_VALUE), BitSetUtilities.asBigInteger(Long.MAX_VALUE));
    for (long l = 100; l > 0; l += 92_233_720_368_547_758L)
      assertEquals(BigInteger.valueOf(l), BitSetUtilities.asBigInteger(l));

    final BigInteger allOnes = BigInteger.valueOf(2L).pow(64).subtract(BigInteger.ONE);
    assertEquals(allOnes, BitSetUtilities.asBigInteger(-1L));

    final BigInteger maxPlus1 = BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE);
    assertEquals(maxPlus1, BitSetUtilities.asBigInteger(Long.MAX_VALUE + 1));
  }

  @Test
  public void testAsBitSetBigInteger() {
    final BitSet bitset = new BitSet();
    BigInteger bi = BigInteger.valueOf(0L);
    for (int i = 0; i < 10000; i++) {
      assertEquals(bitset, BitSetUtilities.asBitSet(bi));
      bitset.set(i);
      bi = bi.setBit(i);
    }
    try {
      BitSetUtilities.asBitSet(BigInteger.valueOf(-1));
      fail();
    } catch (final IllegalArgumentException e) {
    }
  }

  @Test
  public void testAsBitSetLong() {
    final BitSet bitset = new BitSet();
    long l = 0L;
    int i = 0;
    do {
      assertEquals(bitset, BitSetUtilities.asBitSet(l));
      bitset.set(i);
      l |= 1L << i;
      i++;
    } while (i < 64);
    assertEquals(-1, l);
  }

  @Test
  public void testAsLongBigInteger() {
    assertEquals(0L, BitSetUtilities.asLong(BigInteger.ZERO));
    assertEquals(1L, BitSetUtilities.asLong(BigInteger.ONE));
    assertEquals(10L, BitSetUtilities.asLong(BigInteger.TEN));
    assertEquals(Long.MAX_VALUE, BitSetUtilities.asLong(BigInteger.valueOf(Long.MAX_VALUE)));

    for (long l = 0; l < 1000000; l += 127)
      assertEquals(l, BitSetUtilities.asLong(BigInteger.valueOf(l)));

    // 1000000000000000000000000000000000000000000000000000000000000000
    final long maxPlus1 = BitSetUtilities.asLong(BigInteger.valueOf(Long.MAX_VALUE).add(
        BigInteger.ONE));
    assertEquals(Long.MAX_VALUE + 1L, maxPlus1);

    // 1111111111111111111111111111111111111111111111111111111111111111
    final long allOnes = BitSetUtilities.asLong(BigInteger.valueOf(2L).pow(64)
        .subtract(BigInteger.ONE));
    assertEquals(-1L, allOnes);

    try {
      BitSetUtilities.asBitSet(BigInteger.valueOf(-1));
      fail();
    } catch (final IllegalArgumentException e) {
    }
    try {
      BitSetUtilities.asBitSet(null);
      fail();
    } catch (final NullPointerException e) {
    }
  }

  @Test
  public void testAsLongBitSet() {
    final BitSet bitset = new BitSet();
    bitset.set(63);
    assertEquals(Long.MAX_VALUE + 1L, BitSetUtilities.asLong(bitset));
    bitset.clear();

    long l = 0L;
    int i = 0;
    do {
      assertEquals(l, BitSetUtilities.asLong(bitset));
      bitset.set(i);
      l |= 1L << i;
      i++;
    } while (i < 64);

    try {
      BitSetUtilities.asLong((BitSet) null);
      fail();
    } catch (final NullPointerException e) {
    }
  }

  @Test
  public void testToTreeMap() throws Exception {
    {
      final GeneralDomainBitSet<String> set = GeneralDomainBitSet.allOf("0", "1", "2");
      final TreeMap<Integer, String> map = set.zipWithPosition().collect(
          BitSetUtilities.toTreeMap());
      map.forEach((p, e) -> {
        assertEquals(set.getElement(p).get(), e);
        assertTrue(Objects.toString(p).equals(e));
      });
      set.remove("1");
      final TreeMap<Integer, String> expected = new TreeMap<>();
      expected.put(0, "0");
      expected.put(2, "2");
      assertEquals(expected, set.zipWithPosition().collect(BitSetUtilities.toTreeMap()));
    }
    {
      final EnumBitSet<Element> elements = EnumBitSet.allOf(Element.class);
      final TreeMap<Integer, Element> map = elements.zipWithPosition().collect(
          BitSetUtilities.toTreeMap());
      assertEquals(elements.size(), map.size());
      map.forEach((p, e) -> {
        assertEquals((int) p, e.ordinal());
      });
    }
  }

  private static String deepToString(final Object o) {
    return BitSetUtilities.deepToString(o).toString();
  }

  private static String deepToString(final Object o, final int timeout) {
    return BitSetUtilities.deepToString(o, timeout).toString();
  }

  @Test(timeout = 1000)
  public void testDeepToString1() throws Exception {
    assertEquals("null", deepToString(null).toString());
    assertEquals("foo", deepToString("foo").toString());
    assertEquals("[1, 2, 3]", deepToString(new int[] { 1, 2, 3 }).toString());

    assertEquals("null", deepToString(null, 100).toString());
    assertEquals("foo", deepToString("foo", 100).toString());
    assertEquals("[1, 2, 3]", deepToString(new int[] { 1, 2, 3 }, 100).toString());

    assertEquals("Pair([1, 2, 3], [x, y, z])",
        deepToString(Pair.of(new int[] { 1, 2, 3 }, new char[] { 'x', 'y', 'z' })).toString());
  }

  @Test(timeout = 1000)
  public void testDeepToString2() throws Exception {
    final Pair<List<?>, List<Object>, List<Object>> pair;
    pair = Pair.of(new ArrayList<>(), new ArrayList<>());
    pair.first.add(pair);
    pair.second.add(pair);
    try {
      deepToString(pair);
      deepToString(pair, 100);
    } catch (final AssertionError e) {
      throw e;
    } catch (final Throwable e) {
      fail("Pair.toString failed: " + e);
    }
  }

  @Test(timeout = 1000)
  public void testDeepToString3() throws Exception {
    final Pair<List<?>, List<Object>, List<Object>> pair;
    pair = Pair.of(new ArrayList<>(), new ArrayList<>());

    for (int i = 0; i < 100; i++) {
      pair.first.add("abcd" + i);
      pair.first.add(GeneralDomainBitSet.allOf(pair.second));
      pair.first.add(IntStream.range(i, i + 10).toArray());
      pair.second.add(DoubleStream.of(i / 3d, 2d * i / 7d).toArray());
      pair.second.add(new Object[] { GeneralDomainBitSet.allOf(pair.first),
          Pair.of(pair, new char[] { 'x' }) });
      pair.second.add(new Object() {
        @Override
        public String toString() {
          try {
            Thread.sleep(100);
          } catch (final Exception e) {
          }
          return "sleepy";
        }
      });
    }

    try {
      final String string = deepToString(pair, 200);
      assertTrue(string.contains("abcd0"));
      assertTrue(string.contains("GeneralDomainBitSet"));
    } catch (final AssertionError e) {
      throw e;
    } catch (final Throwable e) {
      fail("Pair.toString failed: " + e);
    }
  }

  @Test(timeout = 1000)
  public void testDeepToString4() throws Exception {
    @SuppressFBWarnings("unchecked")
    final Supplier<Iterable<Object>>[] createItr = new Supplier[1];
    createItr[0] = () -> {
      return () -> new Iterator<Object>() {

        @Override
        public Object next() {
          return createItr[0].get();
        }

        @Override
        public boolean hasNext() {
          return true;
        }
      };
    };
    final Iterable<Object> iterable = createItr[0].get();
    try {
      final String string = deepToString(iterable, 200);
      assertTrue(string.contains("λ["));
      assertTrue(string.contains(","));
    } catch (final AssertionError e) {
      throw e;
    } catch (final Throwable e) {
      fail("Pair.toString failed: " + e);
    }
  }
}
