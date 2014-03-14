package ch.claude_martin.enumbitset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigInteger;
import java.util.BitSet;
import java.util.Objects;
import java.util.TreeMap;

import org.junit.Test;

import ch.claude_martin.enumbitset.EnumBitSetTest.Element;

@SuppressWarnings("static-method")
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

}
