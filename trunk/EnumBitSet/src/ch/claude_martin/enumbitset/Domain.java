package ch.claude_martin.enumbitset;

import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;

/** A domain defines the elements that a {@link DomainBitSet} can contain. This is also know as the
 * <i>universe of discourse</i>. It is a set with the following characteristics:
 * <ul>
 * <li><b>ordered</b> (as it is a {@link List}),</li>
 * <li><b>distinct</b> (as it is a {@link Set}),</li>
 * <li><b>immutable</b> (in size and content),</li>
 * <li><b>non-null</b> (no element is <code>null</code>).</li>
 * </ul>
 * The {@link Spliterator} returned by {@link #spliterator() } also indicates all these
 * characteristics.
 * 
 * <p>
 * A domain should have a fast implementation of {@link #indexOf(Object)}, so that conversions of
 * the DomainBitSet are fast.
 * 
 * @param <T>
 *          A type that all elements in the domain share.
 * 
 * @author <a href="http://claude-martin.ch/enumbitset/">Copyright &copy; 2014 Claude Martin</a> */
public interface Domain<T> extends List<T>, Set<T> {
  /** Two domains are defined to be equal if they contain the same elements in the same order.
   * 
   * @param other
   *          the object to be compared for equality with this domain */
  @Override
  public boolean equals(Object other);

  @Override
  public default Spliterator<T> spliterator() {
    return Spliterators.spliterator(this, Spliterator.ORDERED | Spliterator.DISTINCT
        | Spliterator.SIZED | Spliterator.NONNULL | Spliterator.IMMUTABLE);
  }
}
