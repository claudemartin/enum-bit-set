package ch.claude_martin.enumbitset;

import static java.util.Objects.requireNonNull;
import static java.util.Spliterator.IMMUTABLE;
import static java.util.Spliterator.NONNULL;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterator.SIZED;

import java.io.Serializable;
import java.lang.ref.Reference;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

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
public final class Pair<T, X extends T, Y extends T> implements Iterable<T>, Cloneable,
    Serializable {
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
  @SuppressWarnings("unchecked")
  public static//
  <TT, TX extends TT, TY extends TT, P extends Pair<TT, TX, TY>, R> //
  BiFunction<TX, TY, R> curry(@Nonnull final Function<P, R> f) {
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
  public static <TT, TX extends TT, TY extends TT> Pair<TT, TX, TY> of(
      @Nonnull final Class<TT> commonType, @Nonnull final TX first, @Nonnull final TY second) {
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
  public static <TT, TX extends TT, TY extends TT> Pair<TT, TX, TY> of(@Nonnull final TX first,
      @Nonnull final TY second) {
    return new Pair<>(first, second);
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
  public static <TX, TY, R> Function<Pair<?, TX, TY>, R> uncurry(
      @Nonnull final BiFunction<TX, TY, R> f) {
    requireNonNull(f, "uncurry: function must not be null");
    return (p) -> f.apply(p.first, p.second);
  }

  /** The first value of this pair. Not null.
   * 
   * <p>
   * This is also known as the <i>first coordinate</i> or the <i>left projection</i> of the pair. */
  @Nonnull
  public final X           first;

  /** The first value of this pair. Not null.
   * 
   * <p>
   * This is also known as the <i>second coordinate</i> or the <i>right projection</i> of the pair. */
  @Nonnull
  public final Y           second;

  @SuppressFBWarnings(value = "JCIP_FIELD_ISNT_FINAL_IN_IMMUTABLE_CLASS", justification = "It's lazy.")
  private transient String string = null;

  private Pair(@Nonnull final X first, @Nonnull final Y second) {
    this.first = requireNonNull(first, "first");
    this.second = requireNonNull(second, "second");
  }

  /** Scala-style getter for {@link #first}.
   * 
   * @see #first
   * @return the first element (not null). */
  @Nonnull
  public X _1() {
    return this.first;
  }

  /** Scala-style getter for {@link #second}.
   * 
   * @see #second
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
  public <R> R applyTo(@Nonnull final BiFunction<X, Y, R> f) {
    requireNonNull(f, "f");
    return f.apply(this.first, this.second);
  }

  /** Returns this pair, as it is immutable.
   * 
   * @return <code>this</code> */
  @Override
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
  public void consumeBy(@Nonnull final BiConsumer<X, Y> consumer) {
    requireNonNull(consumer, "consumer");
    consumer.accept(this.first, this.second);
  }

  /** Compares two pairs for equality. The given object must also be a pair and contain two elements
   * that are equal to this pair's elements.
   * 
   * @return <code>true</code>, iff both first and second are equal. */
  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Pair //
        && this.first.equals(((Pair<?, ?, ?>) obj).first) //
        && this.second.equals(((Pair<?, ?, ?>) obj).second);
  }

  /** {@inheritDoc}
   * 
   * @return <code>Objects.hash(this.first, this.second)</code> */
  @Override
  public int hashCode() {
    return Objects.hash(this.first, this.second);
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

  public Stream<T> stream() {
    return StreamSupport.stream(this.spliterator(), false);
  }

  /** Creates an inverted pair.
   * <p>
   * <code>(a, b) &rarr; (b, a)</code>
   * 
   * @return <code>new Pair&lt;&gt;(this.second, this.first)</code> */
  public Pair<T, Y, X> swap() {
    return new Pair<>(this.second, this.first);
  }

  /** This Pair as an array so that first is on index 0 and second is on index 1.
   * 
   * @return <code>new Object[] { this.first, this.second };</code> */
  public Object[] toArray() {
    return new Object[] { this.first, this.second };
  }

  /** Returns a string representation of this Pair.
   * <p>
   * This could lead to recursion in rare cases. Pairs, Collections, Arrays, References etc. are
   * always represented by their type. But this can still lead to a {@link StackOverflowError}.
   * 
   * @return "Pair(<i>first</i>, <i>second</i>)" */
  @Override
  public String toString() {
    if (null == this.string) {
      final Function<Object, String> f = (o) -> {
        return o.getClass().isArray() || o instanceof Iterable || o instanceof Reference
            || o instanceof Optional ? o.getClass().getSimpleName() //
                : o.toString();
      };
      this.string = "Pair(" + f.apply(this.first) + ", " + f.apply(this.second) + ")";
    }
    return this.string;
  }

}
