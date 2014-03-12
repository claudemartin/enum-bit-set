package ch.claude_martin.enumbitset;

import static ch.claude_martin.enumbitset.BitSetUtilities.asLong;//
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;

/** BitSet with a domain of up to 64 elements. This is checked at creation, so that it is not thrown
 * later. However, a mask that has a larger domain causes an {@link IllegalArgumentException}
 * instead.
 * 
 * @param <T>
 *          The type of the domain. All elements in the domain must be of type T or of any subtype
 *          of T. */
public class SmallDomainBitSet<T> implements DomainBitSet<T> {
  private static final class Itr<T> implements Iterator<T> {
    private final Domain<T> dom;
    private int             pos = 0;
    private long            next;

    public Itr(final Domain<T> d, final long mask) {
      this.dom = d;
      this.next = mask;
      while (this.next != 0L && (this.next & 1L) == 0L) {
        this.pos++;
        this.next >>>= 1;
      }
    }

    @Override
    public boolean hasNext() {
      return this.next != 0;
    }

    @Override
    public T next() {
      final T result = this.dom.get(this.pos);
      do {
        this.pos++;
        this.next >>>= 1;
      } while (this.next != 0 && (this.next & 1L) == 0);
      return result;
    }
  }

  /** Creates a set with the given domain, that contains all elements.
   * 
   * @param <T>
   *          The type of the set and its domain.
   * @param domain
   *          The type of the domain.
   * @return A SmallDomainBitSet containing all elements of the given domain. */
  public static <T> SmallDomainBitSet<T> allOf(final List<T> domain) {
    if (domain.size() == 64)
      return SmallDomainBitSet.<T> of(domain, -1L);
    else
      return SmallDomainBitSet.<T> of(domain, (1L << domain.size()) - 1L);
  }

  /** Creates a set with the given domain, that contains all elements.
   * 
   * @param <T>
   *          The type of the domain.
   * @param domain
   *          The domain.
   * 
   * @return A SmallDomainBitSet containing all elements of the given domain. */
  @SafeVarargs
  public static <T> SmallDomainBitSet<T> allOf(final T... domain) {
    return allOf(asList(domain));
  }

  /** Creates a set with the given domain, that contains none of the elements.
   * 
   * @param <T>
   *          The type of the set and its domain.
   * @param domain
   *          The elements of the domain.
   * @return Empty SmallDomainBitSet based on the given domain. */
  public static <T> SmallDomainBitSet<T> noneOf(final List<T> domain) {
    return SmallDomainBitSet.of(new DefaultDomain<>(domain), 0L);
  }

  /** Creates a set with the given domain, that contains none of the elements.
   * 
   * @param <T>
   *          The type of the set and its domain.
   * @param domain
   *          The elements of the domain.
   * @return Empty SmallDomainBitSet based on the given domain. */
  @SafeVarargs
  public static <T> SmallDomainBitSet<T> noneOf(final T... domain) {
    return noneOf(asList(domain));
  }

  /** Creates a set with the given domain, containing all given elements.
   * 
   * @param <T>
   *          The type of the set and its domain.
   * @param domain
   *          The elements of the domain.
   * @param set
   *          The elements.
   * @return SmallDomainBitSet based on the given domain and set. */
  private static <T> SmallDomainBitSet<T> of(final Domain<T> domain, final long set) {
    return new SmallDomainBitSet<>(domain, set);
  }

  /** Creates a set with the given domain, containing all given elements.
   * 
   * @param <T>
   *          The type of the set and its domain.
   * @param domain
   *          The elements of the domain.
   * @param set
   *          The elements.
   * @return SmallDomainBitSet based on the given domain and set. */
  public static <T> SmallDomainBitSet<T> of(final List<T> domain, final Collection<T> set) {
    int i = 0;
    long mask = 0L;
    for (final T t : domain) {
      if (set.contains(requireNonNull(t)))
        mask |= 1L << i;
      i++;
    }
    return of(domain, mask);
  }

  /** Creates a set with the given domain, containing elements according to a given bit mask.
   * 
   * @param <T>
   *          The type of the set and its domain.
   * @param domain
   *          The elements of the domain.
   * @param mask
   *          The elements as a bit mask.
   * @return SmallDomainBitSet based on the given domain and bit mask. */
  public static <T> SmallDomainBitSet<T> of(final List<T> domain, final long mask) {
    return SmallDomainBitSet.<T> of(new DefaultDomain<>(domain), mask);
  }

  /** Creates a set with the given domain, containing all given elements.
   * 
   * @param <T>
   *          The type of the set and its domain.
   * @param domain
   *          The elements of the domain.
   * @param set
   *          The elements.
   * @return SmallDomainBitSet based on the given domain and set. */
  @SafeVarargs
  public static <T> SmallDomainBitSet<T> of(final List<T> domain, final T... set) {
    return of(domain, asList(set));
  }

  private final Domain<T> domain;

  private final long      set;

  private final long      all;

  private int             hash = 0;

  private SmallDomainBitSet(final Domain<T> domain, final long set) {
    this.domain = domain;
    this.set = set;
    final int size = this.domain.size();
    // all = Mask for a complete set
    if (size == 64)
      this.all = -1;
    else
      this.all = (1L << size) - 1L;
    this.checkMask(set);
  }

  private long arrayToLong(final T[] arr) {
    return this.itrToLong(Arrays.asList(arr));
  }

  private long checkMask(final long mask) throws IllegalArgumentException {
    if ((mask & this.all) != mask)
      throw new IllegalArgumentException(
          "The parameter contains more elements than the domain allows.");
    return mask;
  }

  @Override
  public SmallDomainBitSet<T> clone() {
    return this;
  }

  @Override
  public DomainBitSet<T> complement() {
    return new SmallDomainBitSet<>(this.domain, this.not(this.set));
  }

  @Override
  public boolean equals(final Object other) {
    if (other == this)
      return true;
    if (other instanceof DomainBitSet) {
      @SuppressWarnings("unchecked")
      final DomainBitSet<T> domBitSet = (DomainBitSet<T>) other;
      if (this.size() != domBitSet.size() || !this.ofEqualDomain(domBitSet))
        return false;
      if (other instanceof SmallDomainBitSet)
        return this.set == domBitSet.toLong();
      else
        return this.containsAll(domBitSet.toSet());
    }
    return false;
  }

  @Override
  public boolean getBit(final int bitIndex) throws IndexOutOfBoundsException {
    return (this.set >>> bitIndex) % 2 == 1;
  }

  @Override
  public Domain<T> getDomain() {
    return this.domain;
  }

  @Override
  public int hashCode() {
    if (this.hash == 0)
      this.hash = this.domain.hashCode() ^ (int) (this.set ^ this.set >>> 32);
    return this.hash;
  }

  @Override
  public DomainBitSet<T> intersect(final BigInteger mask) {
    return new SmallDomainBitSet<>(this.domain, this.set & this.checkMask(asLong(mask)));
  }

  @Override
  public DomainBitSet<T> intersect(final BitSet s) {
    return new SmallDomainBitSet<>(this.domain, this.set & this.checkMask(asLong(s)));
  }

  @Override
  public DomainBitSet<T> intersect(final Iterable<T> s) {
    return new SmallDomainBitSet<>(this.domain, this.set & this.itrToLong(s));
  }

  @Override
  public DomainBitSet<T> intersect(final long mask) throws IllegalArgumentException {
    return new SmallDomainBitSet<>(this.domain, this.set & this.checkMask(mask));
  }

  @Override
  public DomainBitSet<T> intersectVarArgs(@SuppressWarnings("unchecked") final T... elements) {
    return new SmallDomainBitSet<>(this.domain, this.set & this.arrayToLong(elements));
  }

  @Override
  public boolean isEmpty() {
    return this.set == 0L;
  }

  @Override
  public Iterator<T> iterator() {
    return new Itr<>(this.domain, this.set);
  }

  private long itrToLong(final Iterable<T> itr) {
    long result = 0;
    for (final T t : itr) {
      final int index = this.domain.indexOf(t);
      if (index == -1)
        throw new IllegalArgumentException("Domain does not contain " + t);
      result |= 1L << index;
    }
    return result;
  }

  @Override
  public DomainBitSet<T> minus(final BigInteger mask) {
    return new SmallDomainBitSet<>(this.domain, this.set & this.not(asLong(mask)));
  }

  @Override
  public DomainBitSet<T> minus(final BitSet s) {
    return new SmallDomainBitSet<>(this.domain, this.set & this.not(asLong(s)));
  }

  @Override
  public DomainBitSet<T> minus(final Iterable<T> s) {
    return new SmallDomainBitSet<>(this.domain, this.set & this.not(this.itrToLong(s)));
  }

  @Override
  public DomainBitSet<T> minus(final long mask) throws IllegalArgumentException {
    this.checkMask(mask);
    return new SmallDomainBitSet<>(this.domain, this.set & this.not(mask));
  }

  @Override
  public DomainBitSet<T> minusVarArgs(@SuppressWarnings("unchecked") final T... elements) {
    return new SmallDomainBitSet<>(this.domain, this.set & this.not(this.arrayToLong(elements)));
  }

  private long not(final long mask) throws IllegalArgumentException {
    this.checkMask(mask);
    return ~mask & this.all;
  }

  @Override
  public boolean ofEqualElements(final DomainBitSet<T> other) {
    if (this == other)
      return true;
    if (other == null)
      return false;
    if (this.isEmpty())
      return other.isEmpty();
    // note: the other SmallDBS could have a different domain, but still contain the same elements!
    return this.size() == other.size() && this.stream().allMatch(other::contains);
  }

  @Override
  public int size() {
    return Long.bitCount(this.set);
  }

  @Override
  public Spliterator<T> spliterator() {
    return Spliterators.spliterator(this.iterator(), this.size(), Spliterator.SIZED
        | Spliterator.DISTINCT | Spliterator.NONNULL | Spliterator.IMMUTABLE);
  }

  @Override
  public BigInteger toBigInteger() {
    return BitSetUtilities.asBigInteger(this.set);
  }

  @Override
  public BitSet toBitSet() {
    return BitSetUtilities.asBitSet(this.set);
  }

  @Override
  public long toLong() {
    return this.set;
  }

  @Override
  public Set<T> toSet() {
    final Set<T> result = new LinkedHashSet<>(this.size());
    this.forEach(result::add);
    return result;
  }

  @Override
  public String toString() {
    final Iterator<T> it = this.iterator();
    if (!it.hasNext())
      return "[]";

    final StringBuilder sb = new StringBuilder();
    sb.append('[');
    for (;;) {
      final T e = it.next();
      sb.append(e == this ? "(this Collection)" : e);
      if (!it.hasNext())
        return sb.append(']').toString();
      sb.append(',').append(' ');
    }
  }

  @Override
  public DomainBitSet<T> union(final BigInteger mask) {
    return new SmallDomainBitSet<>(this.domain, this.set | this.checkMask(asLong(mask)));
  }

  @Override
  public DomainBitSet<T> union(final BitSet s) {
    return new SmallDomainBitSet<>(this.domain, this.set | this.checkMask(asLong(s)));
  }

  @Override
  public DomainBitSet<T> union(final Iterable<T> s) {
    return new SmallDomainBitSet<>(this.domain, this.set | this.itrToLong(s));
  }

  @Override
  public DomainBitSet<T> union(final long mask) throws IllegalArgumentException {
    return new SmallDomainBitSet<>(this.domain, this.set | this.checkMask(mask));
  }

  @Override
  public DomainBitSet<T> unionVarArgs(@SuppressWarnings("unchecked") final T... elements) {
    return new SmallDomainBitSet<>(this.domain, this.set | this.arrayToLong(elements));
  }

  @Override
  public Stream<Pair<Object, Integer, T>> zipWithPosition() {
    return this.stream().map(e -> Pair.of(this.domain.indexOf(e), e));
  }
}
