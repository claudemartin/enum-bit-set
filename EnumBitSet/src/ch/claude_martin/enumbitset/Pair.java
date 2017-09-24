package ch.claude_martin.enumbitset;

import static java.util.Objects.requireNonNull;
import static java.util.Spliterator.IMMUTABLE;
import static java.util.Spliterator.NONNULL;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterator.SIZED;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

/** An immutable, ordered pair (2-tuple) of two non-null elements. This can be used in a Cartesian
 * product.
 * <p>
 * A special characteristic is that each pair has a generic type argument that is a supertype of
 * both elements. As a Cartesian product often uses two related types this can make it easier to
 * work with such a pair.
 * <p>
 * A pair should never be recursive, that is it should not contain itself directly or indirectly.
 * Some methods will throw a {@link StackOverflowError} on recursion.
 * 
 * @param <T>
 *          A common type of both elements. <code>Object.class</code> always works.
 * @param <X>
 *          The type of the first element. Extends &lt;T&gt;.
 * @param <Y>
 *          The type of the second element. Extends &lt;T&gt;.
 * @author <a href="http://claude-martin.ch/enumbitset/">Copyright &copy; 2014 Claude Martin</a> */
@Immutable
@ParametersAreNonnullByDefault
public final class Pair<T, X extends T, Y extends T> implements Iterable<T>, Cloneable,
    Serializable, Map.Entry<X, Y>, Comparable<Pair<T, X, Y>> {
  private static final long serialVersionUID = -5888335755613555933L;

  /** Converts a {@link Function function on pairs} to a {@link BiFunction function on two elements}.
   * 
   * <p>
   * Note: The compiler should be able to infer all types. If not then a lambda should be used
   * instead (see example below). The returned value is a {@link BiFunction}, not a function that
   * allows partial application. For that it would have to return
   * {@code Function<A, Function<B, C>>} instead of {@code BiFunction<A, B, C>}.
   * 
   * <p>
   * Example:<br>
   * Given the following set:<br>
   * {@code Set<Pair<A, B, C>> set = new HashSet<>();} <br>
   * We can <i>curry</i> the <i>add</i> method:<br>
   * {@code Pair.curry(set::add)}<br>
   * This is equivalent to this lambda: <br>
   * {@code (a, b) -> set.add(Pair.of(a, b))}
   * 
   * @see #uncurry(BiFunction)
   * @param <TT>
   *          Common type. This isn't actually used.
   * @param <TX>
   *          Type of first element. Extends TT.
   * @param <TY>
   *          Type of second element. Extends TT.
   * @param <P>
   *          Actual type of the Pair. This is exactly {@code Pair<TT, TX, TY>}.
   * @param <R>
   *          Return type of the given function <i>f</i>.
   * @param f
   *          A function that takes a Pair.
   * @return A BiFunction that takes two elements and applies a created Pair on the given Function. */
  @SuppressWarnings("all")
  @Nonnull
  public static//
  <TT, TX extends TT, TY extends TT, P extends Pair<TT, TX, TY>, R> //
  BiFunction<TX, TY, R> curry(final Function<P, R> f) {
    requireNonNull(f, "curry: function must not be null");
    return (x, y) -> f.apply((P) Pair.of(x, y));
  }

  /** This creates the Pair and checks the types of both values.
   * <p>
   * The common type is checked at construction, but not available later.
   * 
   * @param <TT>
   *          Common type
   * @param <TX>
   *          Type of first element. Extends TT.
   * @param <TY>
   *          Type of second element. Extends TT.
   * @param commonType
   *          The type that both elements implement.
   * @param first
   *          The first element.
   * @param second
   *          The second element.
   * @throws ClassCastException
   *           If and of the two elements is not assignable to <tt>commonType</tt>.
   * @throws NullPointerException
   *           If any of the elements is <tt>null</tt>.
   * @return A new pair of the given elements. */
  @Nonnull
  public static <TT, TX extends TT, TY extends TT> Pair<TT, TX, TY> of(final Class<TT> commonType,
      final TX first, final TY second) {
    requireNonNull(commonType, "commonType");
    requireNonNull(first, "first");
    requireNonNull(second, "second");
    if (!commonType.isAssignableFrom(first.getClass())
        || !commonType.isAssignableFrom(second.getClass()))
      throw new ClassCastException();
    return new Pair<>(first, second);
  }

  /** Creates a new pair.
   * 
   * @param <TT>
   *          Common type
   * @param <TX>
   *          Type of first element. Extends TT.
   * @param <TY>
   *          Type of second element. Extends TT.
   * @param first
   *          The first element.
   * @param second
   *          The second element.
   * @throws NullPointerException
   *           If any of the elements is <tt>null</tt>.
   * @return A new pair of the given elements. */
  @Nonnull
  public static <TT, TX extends TT, TY extends TT> Pair<TT, TX, TY> of(//
      final TX first, final TY second) {
    return new Pair<>(requireNonNull(first, "first"), requireNonNull(second, "second"));
  }

  /** Creates a new pair from an {@link Map.Entry}.
   * 
   * @param <TX>
   *          Type of first element.
   * @param <TY>
   *          Type of second element.
   * @param entry
   *          The entry of a map.
   * @throws NullPointerException
   *           If the entry or its key or value is <tt>null</tt>.
   * @return A new pair made of the key and value of the entry. */
  @Nonnull
  public static <TX, TY> Pair<Object, TX, TY> of(final Map.Entry<TX, TY> entry) {
    requireNonNull(entry, "entry");
    return new Pair<>(//
        requireNonNull(entry.getKey(), "key"), //
        requireNonNull(entry.getValue(), "value"));
  }

  /** Converts a {@link BiFunction function on two elements} to a {@link Function function on pairs}.
   * 
   * @see #curry(Function)
   * @see #applyTo(BiFunction)
   * @param <TX>
   *          Type of first element.
   * @param <TY>
   *          Type of second element.
   * @param <R>
   *          Return type of <i>f</i>.
   * @param f
   *          A BiFunction that takes two elements.
   * @return A Function that takes a pair and applies both elements on the given Function. */
  @Nonnull
  public static <TX, TY, R> Function<Pair<?, TX, TY>, R> uncurry(final BiFunction<TX, TY, R> f) {
    requireNonNull(f, "uncurry: function must not be null");
    return (p) -> f.apply(p.first, p.second);
  }

  /** The first value of this pair. Not null.
   * 
   * <p>
   * This is also known as the <i>first coordinate</i> or the <i>left projection</i> of the pair. */
  @Nonnull
  public final X           first;

  /** The second value of this pair. Not null.
   * 
   * <p>
   * This is also known as the <i>second coordinate</i> or the <i>right projection</i> of the pair. */
  @Nonnull
  public final Y           second;

  private Pair(final X first, final Y second) {
    assert null != first : "first == null";
    assert null != second : "second == null";
    this.first = first;
    this.second = second;
  }

  /** Scala-style getter for {@link #first}.
   * 
   * @see #first
   * @see #getKey()
   * @return the first element (not null). */
  @Nonnull
  public X _1() {
    return this.first;
  }

  /** Scala-style getter for {@link #second}.
   * 
   * @see #second
   * @see #getValue()
   * @return the second element (not null). */
  @Nonnull
  public Y _2() {
    return this.second;
  }

  /** Applies the given function to both elements of this pair.
   * 
   * @see #uncurry(BiFunction)
   * @param <R>
   *          return type.
   * @param f
   *          A function on two elements.
   * @throws NullPointerException
   *           if f is null
   * @return The result of applying this pair to f. */
  public <R> R applyTo(final BiFunction<X, Y, R> f) {
    requireNonNull(f, "f");
    return f.apply(this.first, this.second);
  }

  /** Returns this pair, as it is immutable.
   * 
   * @return <code>this</code> */
  @Override
  @Nonnull
  public Pair<T, X, Y> clone() {
    return this;
  }

  /** Performs the operation of the given consumer on both elements of this pair.
   * 
   * @see #uncurry(BiFunction)
   * @throws NullPointerException
   *           if consumer is null
   * @param consumer
   *          A consumer of two elements. */
  public void consumeBy(final BiConsumer<X, Y> consumer) {
    requireNonNull(consumer, "consumer");
    consumer.accept(this.first, this.second);
  }

  /** Compares two pairs for equality. The given object must also be a pair and contain two elements
   * that are equal to this pair's elements. This is compatible to {@link Map.Entry#equals(Object)}.
   * 
   * @return <code>true</code>, iff both first and second are equal. */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj instanceof Map.Entry) {
      @SuppressWarnings("unchecked")
      final Map.Entry<X, Y> e2 = (Map.Entry<X, Y>) obj;
      return this.first.equals(e2.getKey()) && this.second.equals(e2.getValue());
    }
    return false;
  }

  @Override
  public void forEach(final Consumer<? super T> consumer) {
    requireNonNull(consumer, "consumer");
    consumer.accept(this.first);
    consumer.accept(this.second);
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    return this.first.hashCode() ^ this.second.hashCode();
  }

  /** Iterator using a type that is shared by both values. The type is checked before the iterator is
   * created. */
  @Override
  public Iterator<T> iterator() {
    return new Iterator<T>() {
      byte pos = 0;

      @Override
      public boolean hasNext() {
        return this.pos < 2;
      }

      @Override
      public T next() {
        try {
          if (this.pos == 0)
            return Pair.this.first;
          else if (this.pos == 1)
            return Pair.this.second;
          else
            throw new NoSuchElementException("A Pair only contains two elements.");
        } finally {
          this.pos++;
        }
      }
    };
  }

  @Override
  public Spliterator<T> spliterator() {
    return Spliterators.spliterator(this.iterator(), 2, SIZED | IMMUTABLE | ORDERED | NONNULL);
  }

  @Nonnull
  public Stream<T> stream() {
    return StreamSupport.stream(this.spliterator(), false);
  }

  /** Creates an inverted pair.
   * <p>
   * <code>(a, b) &rarr; (b, a)</code>
   * 
   * @return <code>new Pair&lt;&gt;(this.second, this.first)</code> */
  @Nonnull
  public Pair<T, Y, X> swap() {
    return new Pair<>(this.second, this.first);
  }

  /** This Pair as an array so that first is on index 0 and second is on index 1.
   * 
   * @return <code>new Object[] { this.first, this.second };</code> */
  @Nonnull
  public Object[] toArray() {
    return new Object[] { this.first, this.second };
  }

  /** Returns a string representation of this Pair.
   * 
   * @see #toString(String)
   * @see BitSetUtilities#deepToString(Object, int)
   * @return "Pair(<i>first</i>, <i>second</i>)" */
  @Override
  @Nonnull
  public String toString() {
    return this.toString("Pair(%s, %s)");
  }

  /** Applies <i>first</i> and <i>second</i> to the given format string. {@link Map.Entry Map
   * entries} use this format: {@code "%s=%s"}
   * 
   * @see BitSetUtilities#deepToString(Object, int)
   * 
   * @returns A string representation, based on the given format. */
  @Nonnull
  public String toString(final String format) {
    requireNonNull(format, "format");
    return String.format(format, this.first.toString(), this.second.toString());
  }

  // Methods for Map.Entry:

  /** {@inheritDoc}
   * 
   * @see #first
   * @see #_1() */
  @Override
  @Nonnull
  public X getKey() {
    return this.first;
  }

  /** {@inheritDoc}
   * 
   * @see #second
   * @see #_2() */
  @Override
  @Nonnull
  public Y getValue() {
    return this.second;
  }

  /** Not supported because Pair is immutable! */
  @Override
  public Y setValue(final Y value) {
    throw new UnsupportedOperationException("Pair is immutable.");
  }

  // Same as the static methods of Map.Entry but with more fitting names:

  /** Returns a serializable comparator that compares Pair in natural order on the first element.
   *
   * @param <F>
   *          the {@link Comparable} type of the first element
   * @param <S>
   *          the type of the second element
   * @return a comparator that compares Pair in natural order on key. */
  @Nonnull
  public static <F extends Comparable<? super F>, S> Comparator<Map.Entry<F, S>> comparingByFirst() {
    return Map.Entry.comparingByKey();
  }

  /** Returns a serializable comparator that compares Pair in natural order on the second element.
   *
   * @param <F>
   *          the type of the first element
   * @param <S>
   *          the {@link Comparable} type of the second element
   * @return a comparator that compares pair in natural order on the second element. */
  @Nonnull
  public static <F, S extends Comparable<? super S>> Comparator<Map.Entry<F, S>> comparingBySecond() {
    return Map.Entry.comparingByValue();
  }

  /** Returns a comparator that compares Pairs by the first element using the given
   * {@link Comparator}.
   *
   * <p>
   * The returned comparator is serializable if the specified comparator is also serializable.
   * 
   * @param <F>
   *          the type of the first element
   * @param <S>
   *          the type of the second element
   * @param cmp
   *          the {@link Comparator}
   * @return a comparator that compares Pair by the the first element. */
  @Nonnull
  public static <F, S> Comparator<Map.Entry<F, S>> comparingByFirst(final Comparator<? super F> cmp) {
    requireNonNull(cmp, "cmp");
    return Map.Entry.comparingByKey(cmp);
  }

  /** Returns a comparator that compares Pair by the second element using the given
   * {@link Comparator}.
   *
   * <p>
   * The returned comparator is serializable if the specified comparator is also serializable.
   *
   * @param <F>
   *          the type of the first element
   * @param <S>
   *          the type of the second element
   * @param cmp
   *          the {@link Comparator}
   * @return a comparator that compares Pairs by the second element. */
  @Nonnull
  public static <F, S> Comparator<Map.Entry<F, S>> comparingBySecond(final Comparator<? super S> cmp) {
    requireNonNull(cmp, "cmp");
    return Map.Entry.comparingByValue(cmp);
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private final static Comparator<Pair> comparator = (Comparator) getComparator();

  private static <A extends Comparable<A>, B extends Comparable<B>> Comparator<Pair<?, A, B>> getComparator() {
    return Comparator
        .<Pair<?, A, B>, A> comparing(Pair::_1, Comparator.naturalOrder())
        .thenComparing(Comparator.<Pair<?, A, B>, B> comparing(Pair::_2, Comparator.naturalOrder()));
  }

  @Override
  public int compareTo(final Pair<T, X, Y> o) {
    return Objects.compare(this, o, comparator);
  }

  /** This pair as a {@link Map}. The <i>first</i> element is mapped to <code>false</code> and the
   * <i>second</i> to <code>true</code>, which is the natural order of the boolean values. */
  @Nonnull
  public Map<Boolean, T> toMap() {
    final Pair<Map.Entry<Boolean, T>, Map.Entry<Boolean, T>, Map.Entry<Boolean, T>> entries;
    entries = of(of(false, Pair.this.first), of(true, Pair.this.second));
    return new AbstractMap<Boolean, T>() {
      @Override
      public T get(final Object key) {
        if (Boolean.FALSE.equals(key))
          return Pair.this.first;
        if (Boolean.TRUE.equals(key))
          return Pair.this.second;
        return null;
      }

      @Override
      public Set<Map.Entry<Boolean, T>> entrySet() {
        return new AbstractSet<Map.Entry<Boolean, T>>() {
          @Override
          public Iterator<java.util.Map.Entry<Boolean, T>> iterator() {
            return entries.iterator();
          }

          @Override
          public int size() {
            return 2;
          }
        };
      }

      @Override
      public int size() {
        return 2;
      }
    };
  }

  /** Converts a {@link Map} to a {@link Pair}. The <i>first</i> value must be mapped to
   * <code>false</code>, the <i>second</i> to <code>true</code>, which is the natural order for
   * boolean values.
   * 
   * @see Collectors#partitioningBy(java.util.function.Predicate) */
  public static <T> Pair<T, T, T> ofMap(final Map<Boolean, ? extends T> map) {
    requireNonNull(map, "map");
    return of(map.get(false), map.get(true));
  }

}
