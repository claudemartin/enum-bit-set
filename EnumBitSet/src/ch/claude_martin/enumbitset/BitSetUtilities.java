package ch.claude_martin.enumbitset;

import static java.util.Objects.requireNonNull;

import java.math.BigInteger;
import java.util.BitSet;

public class BitSetUtilities {

  /** Creates a BigInteger of a given bit set. The value is a positive value with the same "value" as
   * the bit set.
   * 
   * @param bitset
   *          A bit set.
   * @return The bit mask. */
  public static BigInteger asBigInteger(final BitSet bitset) {
    if (requireNonNull(bitset).isEmpty())
      return BigInteger.ZERO;
    final BigInteger result = new BigInteger(1, reverse(bitset.toByteArray()));
    assert asBitSet(result).equals(bitset);
    return result;
  }

  /** Converts a long to a positive (unsigned) integer.
   * 
   * @param mask
   *          A long value that represents a bit set.
   * @return Positive (unsigned) value. */
  public static BigInteger asBigInteger(final long mask) {
    if (mask >= 0)// Positive already:
      return BigInteger.valueOf(mask);
    // Negative value as "unsigned" BigInteger:
    final BigInteger result = new BigInteger(1, longToBytes(mask));
    assert result.equals(BigInteger.valueOf(mask & Long.MAX_VALUE).add(
        BigInteger.valueOf(2).pow(63)));
    assert asLong(result) == mask;
    return result;
  }

  /** Creates a BitSet of a given mask.
   * 
   * @param mask
   *          A bit mask, must be positive.
   * @return New BitSet, equal to the given bit mask. */
  public static BitSet asBitSet(final BigInteger mask) {
    if (requireNonNull(mask).signum() == -1)
      throw new IllegalArgumentException("The mask must not be negative!");
    return BitSet.valueOf(reverse(mask.toByteArray()));
  }

  /** Creates a BitSet of a given bit mask.
   * 
   * <p>
   * This method can not check if there are more than 64 elements on the enum type, as the enum type
   * is not known. It simply converts a long to a bitset.
   * 
   * @param <X>
   *          Enum type of the elements.
   * @param mask
   *          A bit mask.
   * @return A BitSet that represents the given set. */
  public static <X extends Enum<X> & EnumBitSetHelper<X>> BitSet asBitSet(final long mask) {
    return BitSet.valueOf(new long[] { mask });
  }

  /** Converts a BigInteger to long.
   * 
   * @param mask
   *          A bit mask, must be positive and lower than 2<sup>64</sup>.
   * @throws IllegalArgumentException
   *           Only positive values with up to 64 bits are allowed.
   * @return A long value representing the given BigInteger, if it is valid (bit length = 64). */
  public static long asLong(final BigInteger mask) {
    if (mask.signum() < 0)
      throw new IllegalArgumentException("Negative value not permitted.");
    if (mask.bitLength() > 64)
      throw new IllegalArgumentException("Value is too large (more than 64 bits).");
    final long result = mask.longValue();// lower 64 bits
    return result;
  }

  /** Converts a BitSet to long.
   * 
   * @param bitset
   *          A bit set.
   * @throws IllegalArgumentException
   *           Only bit sets with using up to 64 bits are allowed.
   * @return A long value representing the given bit set. */
  public static long asLong(final BitSet bitset) {
    if (bitset.length() > 64)
      throw new IllegalArgumentException("The bitset contains more than 64 elements.");
    if (bitset.isEmpty())
      return 0L;
    return bitset.toLongArray()[0];
  }

  /** Returns the intersection of two sets.
   * 
   * @param <T>
   *          The type of the domain.
   * @param set1
   *          The first set.
   * @param set2
   *          The second set.
   * @see java.util.function.BinaryOperator
   * @see DomainBitSet#intersect(Iterable)
   * @return The intersection of both sets. */
  public static <T> DomainBitSet<T> intersect(final DomainBitSet<T> set1, final DomainBitSet<T> set2) {
    return set1.intersect(set2);
  }

  private static byte[] longToBytes(final long value) {
    long l = value;
    final byte[] bytes = new byte[8];
    for (int i = 7; i >= 0; i--) {
      bytes[i] = (byte) (l & 0xff);
      l >>= 8;
    }
    return bytes;
  }

  /** Returns the relative complement of two sets.
   * 
   * @param <T>
   *          The type of the domain.
   * @param set1
   *          The first set.
   * @param set2
   *          The second set.
   * @see java.util.function.BinaryOperator
   * @see DomainBitSet#minus(Iterable)
   * @return The relative complement of both sets. */
  public static <T> DomainBitSet<T> minus(final DomainBitSet<T> set1, final DomainBitSet<T> set2) {
    return set1.minus(set2);
  }

  /** Convert little-endian to big-endian and vice versa.
   * 
   * @return The same array, with its contents reversed. */
  private static byte[] reverse(final byte[] array) {
    int i = 0, j = array.length - 1;
    if (j <= 0)
      return array;
    byte tmp;
    while (j > i) {
      tmp = array[j];
      array[j--] = array[i];
      array[i++] = tmp;
    }
    return array;
  }

  /** Returns the union of two sets.
   * 
   * @param <T>
   *          The type of the domain.
   * @param set1
   *          The first set.
   * @param set2
   *          The second set.
   * @see java.util.function.BinaryOperator
   * @see DomainBitSet#union(Iterable)
   * @return The union of both sets. */
  public static <T> DomainBitSet<T> union(final DomainBitSet<T> set1, final DomainBitSet<T> set2) {
    return set1.union(set2);
  }
}
