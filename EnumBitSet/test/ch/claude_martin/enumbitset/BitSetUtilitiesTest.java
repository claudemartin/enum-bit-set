package ch.claude_martin.enumbitset;

import static org.junit.Assert.fail;

import java.math.BigInteger;
import java.util.BitSet;

import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("static-method")
public class BitSetUtilitiesTest {

  @Test
  public void testAsBigIntegerBitSet() {
    final BitSet bitset = new BitSet();
    BigInteger bi = BigInteger.valueOf(0L);
    for (int i = 0; i < 10000; i++) {
      Assert.assertEquals(bi, BitSetUtilities.asBigInteger(bitset));
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
    Assert.assertEquals(BigInteger.ZERO, BitSetUtilities.asBigInteger(0L));
    Assert.assertEquals(BigInteger.ONE, BitSetUtilities.asBigInteger(1L));
    Assert.assertEquals(BigInteger.TEN, BitSetUtilities.asBigInteger(10L));
    Assert.assertEquals(BigInteger.valueOf(Long.MAX_VALUE),
        BitSetUtilities.asBigInteger(Long.MAX_VALUE));
    for (long l = 100; l > 0; l += 92_233_720_368_547_758L)
      Assert.assertEquals(BigInteger.valueOf(l), BitSetUtilities.asBigInteger(l));

    final BigInteger allOnes = BigInteger.valueOf(2L).pow(64).subtract(BigInteger.ONE);
    Assert.assertEquals(allOnes, BitSetUtilities.asBigInteger(-1L));

    final BigInteger maxPlus1 = BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE);
    Assert.assertEquals(maxPlus1, BitSetUtilities.asBigInteger(Long.MAX_VALUE + 1));
  }

  @Test
  public void testAsBitSetBigInteger() {
    final BitSet bitset = new BitSet();
    BigInteger bi = BigInteger.valueOf(0L);
    for (int i = 0; i < 10000; i++) {
      Assert.assertEquals(bitset, BitSetUtilities.asBitSet(bi));
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
      Assert.assertEquals(bitset, BitSetUtilities.asBitSet(l));
      bitset.set(i);
      l |= 1L << i;
      i++;
    } while (i < 64);
    Assert.assertEquals(-1, l);
  }

  @Test
  public void testAsLongBigInteger() {
    Assert.assertEquals(0L, BitSetUtilities.asLong(BigInteger.ZERO));
    Assert.assertEquals(1L, BitSetUtilities.asLong(BigInteger.ONE));
    Assert.assertEquals(10L, BitSetUtilities.asLong(BigInteger.TEN));
    Assert.assertEquals(Long.MAX_VALUE, BitSetUtilities.asLong(BigInteger.valueOf(Long.MAX_VALUE)));

    for (long l = 0; l < 1000000; l += 127)
      Assert.assertEquals(l, BitSetUtilities.asLong(BigInteger.valueOf(l)));

    // 1000000000000000000000000000000000000000000000000000000000000000
    final long maxPlus1 = BitSetUtilities.asLong(BigInteger.valueOf(Long.MAX_VALUE).add(
        BigInteger.ONE));
    Assert.assertEquals(Long.MAX_VALUE + 1L, maxPlus1);

    // 1111111111111111111111111111111111111111111111111111111111111111
    final long allOnes = BitSetUtilities.asLong(BigInteger.valueOf(2L).pow(64)
        .subtract(BigInteger.ONE));
    Assert.assertEquals(-1L, allOnes);

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
    Assert.assertEquals(Long.MAX_VALUE + 1L, BitSetUtilities.asLong(bitset));
    bitset.clear();

    long l = 0L;
    int i = 0;
    do {
      Assert.assertEquals(l, BitSetUtilities.asLong(bitset));
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

}
