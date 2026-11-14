package ch.claude_martin.enumbitset;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.SequencedSet;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.function.Predicate;

import ch.claude_martin.enumbitset.annotations.DefaultAnnotationForParameters;
import ch.claude_martin.enumbitset.annotations.Immutable;
import ch.claude_martin.enumbitset.annotations.NonNull;
import ch.claude_martin.enumbitset.annotations.Nullable;

/** A domain defines the elements that a {@link DomainBitSet} can contain. This is also known as the
 * <i>universe of discourse</i>. It is a set with the following characteristics:
 * <ul>
 * <li><b>ordered</b> (as it is a {@link List}),</li>
 * <li><b>distinct</b> (as it is a {@link Set}),</li>
 * <li><b>immutable</b> (in size and content),</li>
 * <li><b>finite</b> (up to 2<sup>31</sup>-1 elements),</li>
 * <li><b>enumerable</b> (computably),</li>
 * <li><b>non-null</b> (no element is <code>null</code>).</li>
 * </ul>
 * The {@link Spliterator} returned by {@link #spliterator() } also indicates most of these
 * characteristics.
 * 
 * <p>
 * A domain should have a fast implementation of {@link #indexOf(Object)}, so that conversions of
 * the DomainBitSet are fast.
 * 
 * @param <T>
 *          A type that all elements in the domain share.
 * 
 * @author <a href="http://claude-martin.ch/enumbitset/">Copyright &copy; 2026 Claude Martin</a> */
@Immutable
@DefaultAnnotationForParameters({ NonNull.class })
public interface Domain<T> extends Collection<T>, Serializable {

  /** Creates a Domain of the given elements.
   * 
   * @param elements
   *          Elements of the domain.
   * @return New domain containing all given elements. */
  @SafeVarargs
  @NonNull
  public static <T> Domain<T> of(final T... elements) {
    requireNonNull(elements, "elements");
    return DefaultDomain.of(asList(elements));
  }

  /** Creates a Domain of the given elements. if the given collection is a Domain then the same
   * object is returned.
   * 
   * @param elements
   *          Elements of the domain. The collection must be ordered.
   * @return Domain containing all given elements. */
  @SuppressWarnings("unchecked")
  public static <T> Domain<T> of(@NonNull final Collection<? extends T> elements) {
    requireNonNull(elements, "elements");
    if (elements instanceof Domain domain)
      return domain;
    return DefaultDomain.of(elements);
  }

  /** Returns the element at that index.
   * 
   * @see List#get(int) */
  public T get(final int i);

  /** Returns the index of the given element.
   * 
   * @see List#indexOf(Object) */
  public int indexOf(@Nullable final Object o);

  /** Two domains are defined to be equal if they contain the same elements in the same order.
   * 
   * @param other
   *          the object to be compared for equality with this domain */
  @Override
  public boolean equals(final Object other);

  /** Creates a factory to create new sets with this domain.
   * <p>
   * The returned DomainBitSet is optimized for this Domain.
   * 
   * @see BitSetUtilities#toDomainBitSet(Domain) */
  public Function<Collection<T>, DomainBitSet<T>> factory();

  /** Returns the hash code value for this domain. The value must be equal to:<br>
   * <code>java.util.Arrays.hashCode(this.toArray())</code>
   * 
   * @return a hash code for this domain */
  @Override
  public int hashCode();

  @Override
  public default Spliterator<T> spliterator() {
    return Spliterators.spliterator(this.iterator(), Spliterator.ORDERED | Spliterator.DISTINCT
        | Spliterator.SIZED | Spliterator.NONNULL | Spliterator.IMMUTABLE, this.size());
  }

  /** The size of this domain.
   * 
   * @return Size of domain. */
  public int size();

  
  /** This domain as an array. */
  public T[] toArray(); 
}
