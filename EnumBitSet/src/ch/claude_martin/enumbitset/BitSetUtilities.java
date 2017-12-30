package ch.claude_martin.enumbitset;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ch.claude_martin.enumbitset.annotations.CheckReturnValue;
import ch.claude_martin.enumbitset.annotations.DefaultAnnotationForParameters;
import ch.claude_martin.enumbitset.annotations.NonNull;
import ch.claude_martin.enumbitset.annotations.Nonnegative;
import ch.claude_martin.enumbitset.annotations.SuppressFBWarnings;


/** A collection of utility methods for {@link DomainBitSet}s.
 * 
 * @author <a href="http://claude-martin.ch/enumbitset/">Copyright &copy; 2014 Claude Martin</a> */
@DefaultAnnotationForParameters({ NonNull.class })
public final class BitSetUtilities {

  /** Creates a BigInteger of a given bit set. The value is a positive value with the same "value" as
   * the bit set.
   * 
   * @param bitset
   *          A bit set.
   * @return The bit mask. */
  @NonNull
  @Nonnegative
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
  @Nonnegative
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
  public static BitSet asBitSet(@Nonnegative final BigInteger mask) {
    if (requireNonNull(mask, "mask").signum() == -1)
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
  @NonNull
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
  public static long asLong(@Nonnegative final BigInteger mask) {
    requireNonNull(mask, "mask");
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

  /** Returns the Cartesian Product of two sets with the same kind of elements. Use
   * {@link #cross(DomainBitSet, DomainBitSet, Class)} if they are not of the same type.
   * <p>
   * The returned set has a size of <code>this.size() * set.size()</code>.
   * 
   * @param <T>
   *          The type of all elements.
   * @param set1
   *          A set.
   * @param set2
   *          Another set.
   * @return the Cartesian Product.
   * @see #cross(DomainBitSet, DomainBitSet, Class)
   * @see DomainBitSet#cross(DomainBitSet)
   * @see DomainBitSet#cross(DomainBitSet, BiConsumer) */
  @SuppressFBWarnings({ "rawtypes", "unchecked" })
  @NonNull
  @CheckReturnValue
  public static <T> Set<Pair<T, T, T>> cross(final DomainBitSet<T> set1, final DomainBitSet<T> set2) {
    requireNonNull(set1, "set1");
    requireNonNull(set2, "set2");
    return (Set) set1.cross(set2);
  }

  /** Returns the Cartesian Product. This makes it easy to use some common supertype of both types.
   * <p>
   * Example: If the types are {@link Integer} and {@link Double} then the supertype {@link Number}
   * can be used.
   * 
   * <p>
   * The returned set has a size of <code>this.size() * set.size()</code>. The given type has to be
   * a super type of both other types.
   * 
   * @param <C>
   *          A common base type.
   * @param <T1>
   *          The type of the elements in the first set.
   * @param <T2>
   *          The type of the elements in the second set.
   * @param set1
   *          A set.
   * @param set2
   *          Another set.
   * @return the Cartesian Product.
   * @see DomainBitSet#cross(DomainBitSet)
   * @see DomainBitSet#cross(DomainBitSet, BiConsumer) */
  @SuppressFBWarnings({ "rawtypes", "unchecked" })
  @NonNull
  @CheckReturnValue
  public static <C, T1 extends C, T2 extends C> Set<Pair<C, T1, T2>> cross(
      final DomainBitSet<T2> set1, final DomainBitSet<T2> set2, final Class<C> type) {
    requireNonNull(set1, "set1");
    requireNonNull(set2, "set2");
    requireNonNull(type, "type");
    return (Set) set1.cross(set2);
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
  @CheckReturnValue
  @NonNull
  public static <T> DomainBitSet<T> intersect(final DomainBitSet<T> set1,
      @NonNull final DomainBitSet<T> set2) {
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
  @CheckReturnValue
  @NonNull
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

  /** Collector to convert a {@link Stream} to a {@link DomainBitSet}.
   * 
   * @param <T>
   *          Type of elements.
   * @param domain
   *          the {@link Domain} to create the {@link DomainBitSet}.
   * @see Domain#factory()
   * @see Collectors#toCollection(Supplier)
   * @return New Collector to collect elements into a DomainBitSet. */
  @CheckReturnValue
  @NonNull
  public static <T> Collector<T, Set<T>, DomainBitSet<T>> toDomainBitSet(final Domain<T> domain) {
    requireNonNull(domain, "domain");
    return toDomainBitSet(domain.factory());
  }

  /** Collector to convert a {@link Stream} to a {@link DomainBitSet}.
   * 
   * @param bitsetFactory
   *          Creates a bitset from a {@link Collection}.
   * @param <T>
   *          Type of elements.
   * @param <D>
   *          Type of DomainBitSet.
   * @see Collectors#toCollection(Supplier)
   * @return New Collector to collect elements into a DomainBitSet. */
  @CheckReturnValue
  @NonNull
  public static <T, D extends DomainBitSet<T>> Collector<T, Set<T>, D> toDomainBitSet(
      final Function<Collection<T>, D> bitsetFactory) {
    requireNonNull(bitsetFactory, "bitsetFactory");
    return new Collector<T, Set<T>, D>() {

      @Override
      public BiConsumer<Set<T>, T> accumulator() {
        return (set, t) -> set.add(t);
      }

      @Override
      public Set<Characteristics> characteristics() {
        return Collections.unmodifiableSet(EnumSet.of(Collector.Characteristics.UNORDERED));
      }

      @Override
      public BinaryOperator<Set<T>> combiner() {
        return (s1, s2) -> {
          s1.addAll(s2);
          return s1;
        };
      }

      @Override
      public Function<Set<T>, D> finisher() {
        return bitsetFactory::apply;
      }

      @Override
      public Supplier<Set<T>> supplier() {
        return HashSet::new;
      }

    };
  }

  /** Collector to convert a {@link Stream} of {@link Pair}s to a {@link TreeMap}.
   * <p>
   * A stream with multiple entries for the same position will cause a {@link IllegalStateException}.
   * 
   * @param <T>
   *          the type of the elements.
   * @see DomainBitSet#zipWithPosition()
   * 
   * @return a Collector */
  @CheckReturnValue
  @NonNull
  public static <T> Collector<Pair<Object, Integer, T>, ?, TreeMap<Integer, T>> toTreeMap() {
    // Note: A collision should not occur, unless this is applied to an invalid stream.
    return Collectors.toMap(Pair::_1, Pair::_2, (u, v) -> {
      throw new IllegalStateException();
    }, TreeMap::new);
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
  @CheckReturnValue
  @NonNull
  public static <T> DomainBitSet<T> union(final DomainBitSet<T> set1, final DomainBitSet<T> set2) {
    return set1.union(set2);
  }

  /** Returns a string representation of the object. All arrays and iterables are processed as such.
   * In case of loops in the object graph this will return a partial result, but will not throw a
   * {@link StackOverflowError}. This should only be used for debugging.
   * 
   * @param object
   *          the object to be converted to string.
   * @param timeout
   *          timeout in milliseconds
   * @return A character sequence that represents the given object. */
  @SuppressFBWarnings("deprecation")
  @NonNull
  public static CharSequence deepToString(final Object object, @Nonnegative final int timeout) {
    if (timeout < 0)
      throw new IllegalArgumentException("timeout");
    if (timeout == 0)
      return deepToString(object);
    final StringBuilder buffer = new StringBuilder();
    final Set<Object> dejavu = Collections.<Object> newSetFromMap(new IdentityHashMap<>());
    final Thread thread = new Thread(() -> deepToString(object, buffer, dejavu), "deepToString");
    thread.setDaemon(true);
    // Ingore everything! This includes interrupted, stack overflow and out of memory.
    thread.setUncaughtExceptionHandler((t, e) -> { });
    thread.start();
    try {
      thread.join(timeout);
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    thread.interrupt();
    if(!thread.isAlive())
      return buffer;
    thread.stop();
    return buffer.toString();
  }

  /** Returns a string representation of the object. All arrays and iterables are processed as such.
   * In case of loops in the object graph this will return a partial result, but will not throw a
   * {@link StackOverflowError}. Still, this may not return a result quickly and it may throw other
   * unchecked exceptions. This should only be used for debugging.
   * 
   * @param object
   *          the object to be converted to string.
   * @return A character sequence that represents the given object. */
  public static CharSequence deepToString(final Object object) {
    final StringBuilder buffer = new StringBuilder();
    {
      final Set<Object> dejavu = Collections.<Object> newSetFromMap(new IdentityHashMap<>());
      try {
        deepToString(object, buffer, dejavu);
      } catch (OutOfMemoryError | StackOverflowError e) {
        // at least dejavu can be freed at this point
      }
    }
    return buffer;
  }

  /** Creates a string representation of the object. If the given set "dejavu" gets too large this
   * method will stop traversal of the object graph. */
  private static void deepToString(final Object o, final StringBuilder buffer,
      final Set<Object> dejavu) {
    if (Thread.currentThread().isInterrupted()) {
      Thread.currentThread().interrupt();
      return;
    }
    if (o == null) {
      buffer.append("null");
      return;
    }
    if (dejavu.size() > 1000) {
      buffer.append("...");
      return;
    }
    final Class<? extends Object> oClass = o.getClass();
    final String simpleName = oClass.isSynthetic() && oClass.getSimpleName().contains("$$Lambda$")
        && Arrays.stream(oClass.getDeclaredMethods())
            .filter(m -> Modifier.isPublic(m.getModifiers())).count() == 1
        ? "λ" : oClass.getSimpleName();
    if (dejavu.contains(o)) {
      buffer.append(simpleName);
      buffer.append('@');
      buffer.append(Integer.toHexString(System.identityHashCode(o)));
      return;
    }
    dejavu.add(o);
    if (oClass.isArray()) {
      if (o instanceof Object[]) {
        buffer.append('[');
        for (final Object e : (Object[]) o) {
          deepToString(e, buffer, dejavu);
          buffer.append(", ");
        }
        buffer.setLength(buffer.length() - 2);
        buffer.append(']');
      } else
        try {
          buffer.append(
              Arrays.class.getDeclaredMethod("toString", oClass).invoke(null, o).toString());
        } catch (final Exception e) {
          throw new RuntimeException("Can't invoke Arrays.toString(" + simpleName + ")",
              e);
        }
    } else if (o instanceof Pair) {
      buffer.append("Pair(");
      deepToString(((Pair<?, ?, ?>) o).first, buffer, dejavu);
      buffer.append(", ");
      deepToString(((Pair<?, ?, ?>) o).second, buffer, dejavu);
      buffer.append(')');
    } else if (o instanceof Iterable) {
      buffer.append(simpleName);
      buffer.append('[');
      for (final Object e : (Iterable<?>) o) {
        deepToString(e, buffer, dejavu);
        buffer.append(", ");
      }
      buffer.setLength(buffer.length() - 2);
      buffer.append(']');
    } else if (o instanceof AtomicReference) {
      buffer.append(simpleName);
      buffer.append('(');
      deepToString(((AtomicReference<?>) o).get(), buffer, dejavu);
      buffer.append(')');
    } else if (o instanceof Optional) {
      buffer.append(simpleName);
      buffer.append('[');
      deepToString(((Optional<?>) o).get(), buffer, dejavu);
      buffer.append(']');
    } else
      buffer.append(o.toString());
  }

}
