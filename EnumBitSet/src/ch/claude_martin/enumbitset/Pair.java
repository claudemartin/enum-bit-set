package ch.claude_martin.enumbitset;

import static java.util.Objects.requireNonNull;
import static java.util.Spliterator.IMMUTABLE;
import static java.util.Spliterator.NONNULL;
import static java.util.Spliterator.ORDERED;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/** An immutable, ordered pair (2-tuple) of two non-null elements. This can be used in a Cartesian
 * product.
 * <p>
 * A special characteristic is that each pair has a generic type argument that is a supertype of
 * both elements. As a Cartesian product often uses two related types this can make it easier to
 * work with such a pair.
 * 
 * @param <T>
 *          A common type of both elements. <code>Object.class</code> always works.
 * @param <X>
 *          The type of the first element. Extends &lt;T&gt;.
 * @param <Y>
 *          The type of the second element. Extends &lt;T&gt;.
 * @author <a href="http://claude-martin.ch/enumbitset/">Copyright &copy; 2014 Claude Martin</a> */
public final class Pair<T, X extends T, Y extends T> implements Iterable<T> {
  /** Converts a {@link Function function on pairs} to a {@link BiFunction function on two elements}.
   * 
   * @see #uncurry(BiFunction)
   * @param <TT>
   *          Common type
   * @param <TX>
   *          Type of first element. Extends TT.
   * @param <TY>
   *          Type of second element. Extends TT.
   * @param <R>
   *          Return type of <i>f</i>.
   * @param f
   *          A function that takes a Pair.
   * @return A BiFunction that takes two elements and applies a created Pair on the given Function. */
  public static <TT, TX extends TT, TY extends TT, R> BiFunction<TX, TY, R> curry(
      final Function<Pair<TT, TX, TY>, R> f) {
    return (x, y) -> f.apply(Pair.of(x, y));
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
  // TODO : Write test for this method.
  public static <TT, TX extends TT, TY extends TT> Pair<TT, TX, TY> of(final Class<TT> commonType,
      final TX first, final TY second) {
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
  public static <TT, TX extends TT, TY extends TT> Pair<TT, TX, TY> of(final TX first,
      final TY second) {
    return new Pair<>(first, second);
  }

  /** Converts a {@link BiFunction function on two elements} to a {@link Function function on pairs}.
   * 
   * @see #curry(Function)
   * @param <TT>
   *          Common type
   * @param <TX>
   *          Type of first element. Extends TT.
   * @param <TY>
   *          Type of second element. Extends TT.
   * @param <R>
   *          Return type of <i>f</i>.
   * @param f
   *          A BiFunction that takes two elements.
   * @return A Function that takes a pair and applies both elements on the given Function. */
  public static <TT, TX extends TT, TY extends TT, R> Function<Pair<TT, TX, TY>, R> uncurry(
      final BiFunction<TX, TY, R> f) {
    return (p) -> f.apply(p.first, p.second);
  }

  /** The first value of this pair. Not null.
   * 
   * <p>
   * This is also known as the <i>first coordinate</i> or the <i>left projection</i> of the pair. */
  public final X first;

  /** The first value of this pair. Not null.
   * 
   * <p>
   * This is also known as the <i>second coordinate</i> or the <i>right projection</i> of the pair. */
  public final Y second;

  private Pair(final X first, final Y second) {
    this.first = requireNonNull(first);
    this.second = requireNonNull(second);
  }

  /** {@inheritDoc} @return <code>this</code> */
  @Override
  public Pair<T, X, Y> clone() {
    return this;
  }

  /** {@inheritDoc} @return <code>true</code>, iff both first and second are equal. */
  @Override
  public boolean equals(final Object obj) {
    return obj instanceof Pair //
        && Objects.equals(this.first, ((Pair<?, ?, ?>) obj).first) //
        && Objects.equals(this.second, ((Pair<?, ?, ?>) obj).second);
  }

  /** {@inheritDoc} @return <code>Objects.hash(this.first, this.second)</code> */
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
    return Spliterators.spliterator(this.iterator(), 2, IMMUTABLE | ORDERED | NONNULL);
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
   * 
   * @return "Pair(<i>first</i>, <i>second</i>)" */
  @Override
  public String toString() {
    return "Pair(" + this.first + ", " + this.second + ")";
  }

}
