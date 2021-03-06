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
import java.util.function.Consumer;
import java.util.stream.Stream;

import ch.claude_martin.enumbitset.annotations.DefaultAnnotationForParameters;
import ch.claude_martin.enumbitset.annotations.Immutable;
import ch.claude_martin.enumbitset.annotations.NonNull;
import ch.claude_martin.enumbitset.annotations.SuppressFBWarnings;

/** BitSet with a domain of up to 64 elements. This is checked at creation, so that it is not thrown
 * later. However, a mask that has a larger domain causes an {@link IllegalArgumentException}
 * instead.
 * 
 * @param <T>
 *          The type of the domain. All elements in the domain must be of type T or of any subtype
 *          of T. */
@Immutable
@DefaultAnnotationForParameters({ NonNull.class })
public class SmallDomainBitSet<T> implements DomainBitSet<T>, Cloneable {
  private static final long serialVersionUID = 671939884938912745L;

  private static final class Itr<T> implements Iterator<T> {
    @NonNull
    private final Domain<T> dom;
    private int             pos = 0;
    private long            next;

    public Itr(@NonNull final Domain<T> d, final long mask) {
      this.dom = d;
      this.next = mask;
      while (this.next != 0L && (this.next & 1L) == 0L) {
        this.pos++;
        this.next >>>= 1;
      }
    }

    @Override
    public boolean hasNext() {
      return this.next != 0L;
    }

    @Override
    public T next() {
      final T result = this.dom.get(this.pos);
      do {
        this.pos++;
        this.next >>>= 1;
      } while (this.next != 0L && (this.next & 1L) == 0L);
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
  public static <T> SmallDomainBitSet<T> allOf(final List<T> domain)
      throws MoreThan64ElementsException {
    requireNonNull(domain, "domain");
    final int size = domain.size();
    if (size > 64)
      throw new MoreThan64ElementsException();
    else if (size == 64)
      return SmallDomainBitSet.<T> of(domain, -1L);
    else
      return SmallDomainBitSet.<T> of(domain, (1L << size) - 1L);
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
  public static <T> SmallDomainBitSet<T> allOf(final T... domain)
      throws MoreThan64ElementsException {
    return allOf(asList(domain));
  }

  /** Creates a set with the given domain, that contains none of the elements.
   * 
   * @param <T>
   *          The type of the set and its domain.
   * @param domain
   *          The elements of the domain.
   * @return Empty SmallDomainBitSet based on the given domain. */
  public static <T> SmallDomainBitSet<T> noneOf(final List<T> domain)
      throws MoreThan64ElementsException {
    return SmallDomainBitSet.of(DefaultDomain.of(domain), 0L);
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
  @NonNull
  private static <T> SmallDomainBitSet<T> of(final Domain<T> domain, final long set)
      throws MoreThan64ElementsException {
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
  @NonNull
  public static <T> SmallDomainBitSet<T> of(final List<T> domain, final Collection<T> set) {
    requireNonNull(domain, "domain");
    requireNonNull(set, "set");
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
  @NonNull
  public static <T> SmallDomainBitSet<T> of(final List<T> domain, final long mask) {
    return SmallDomainBitSet.<T> of(DefaultDomain.of(domain), mask);
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
  @NonNull
  public static <T> SmallDomainBitSet<T> of(@NonNull final List<T> domain, @NonNull final T... set) {
    return of(domain, asList(set));
  }

  private final Domain<T>      domain;

  private final long           set;

  private transient final long all;

  @SuppressFBWarnings(value = "JCIP_FIELD_ISNT_FINAL_IN_IMMUTABLE_CLASS", justification = "It's lazy.")
  private transient int        hash = 0; // defaults to 0, later it's set to a hash code.

  SmallDomainBitSet(final Domain<T> domain, final long set) throws MoreThan64ElementsException {
    this.domain = domain;
    this.set = set;
    final int size = this.domain.size();
    // all = Mask for a complete set
    if (size > 64)
      throw new MoreThan64ElementsException();
    else if (size == 64)
      this.all = -1;
    else
      this.all = (1L << size) - 1L;
    this.checkMask(set);
  }

  private long arrayToLong(@NonNull final T[] arr) {
    return this.itrToLong(Arrays.asList(arr));
  }

  private long checkMask(final long mask) throws IllegalArgumentException {
    if ((mask & this.all) != mask)
      throw new IllegalArgumentException(
          "The parameter contains more elements than the domain allows.");
    return mask;
  }

  @Override
  @SuppressFBWarnings("CN_IDIOM_NO_SUPER_CALL")
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
      @SuppressFBWarnings("unchecked")
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
  @NonNull
  public Domain<T> getDomain() {
    return this.domain;
  }

  @Override
  public int hashCode() {
    if (this.hash == 0)
      this.hash = this.domain.hashCode() ^ this.stream().mapToInt(Object::hashCode).sum();
    return this.hash;
  }

  @Override
  public DomainBitSet<T> intersect(final BigInteger mask) {
    return new SmallDomainBitSet<>(this.domain, this.set & this.checkMask(asLong(mask)));
  }

  @Override
  public DomainBitSet<T> intersect(final BitSet s) {
    requireNonNull(s, "s");
    return new SmallDomainBitSet<>(this.domain, this.set & this.checkMask(asLong(s)));
  }

  @Override
  public DomainBitSet<T> intersect(final Iterable<T> s) {
    requireNonNull(s, "s");
    return new SmallDomainBitSet<>(this.domain, this.set & this.itrToLong(s));
  }

  @Override
  public DomainBitSet<T> intersect(final long mask) throws IllegalArgumentException {
    return new SmallDomainBitSet<>(this.domain, this.set & this.checkMask(mask));
  }

  @Override
  public DomainBitSet<T> intersectVarArgs(@SuppressFBWarnings("unchecked") final T... elements) {
    requireNonNull(elements, "elements");
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
    requireNonNull(itr, "itr");
    long result = 0;
    for (final T t : itr) {
      final int index = this.domain.indexOf(t);
      assert index != -1 : "Domain does not contain " + t;
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
  public DomainBitSet<T> minusVarArgs(@SuppressFBWarnings("unchecked") final T... elements) {
    return new SmallDomainBitSet<>(this.domain, this.set & this.not(this.arrayToLong(elements)));
  }

  private long not(final long mask) throws IllegalArgumentException {
    this.checkMask(mask);
    return ~mask & this.all;
  }

  @Override
  public boolean ofEqualElements(final DomainBitSet<T> other) {
    requireNonNull(other, "other");
    if (this == other)
      return true;
    if (this.isEmpty())
      return other.isEmpty();
    // note: the other SmallDBS could have a different domain, but still contain the same elements!
    return this.size() == other.size() && this.stream().allMatch(other::contains);
  }

  /** The powerset, which is the set of all subsets.
   * <p>
   * Note: Complexity is <code>O(2<sup>n</sup>)</code>. This takes very long for large sets.
   * <p>
   * This is not thread safe and has to be processed sequentially.
   * 
   * @see #powerset(Consumer, boolean)
   * @return The powerset of this set. */
  @Override
  @SuppressFBWarnings("unchecked")
  public Iterable<SmallDomainBitSet<T>> powerset() {
    return (Iterable<SmallDomainBitSet<T>>) DomainBitSet.super.powerset();
  }

  /** {@inheritDoc}
   * 
   * @see #powerset() */
  @Override
  public void powerset(final Consumer<DomainBitSet<T>> consumer, final boolean blocking) {
    DomainBitSet.super.powerset(consumer, blocking);
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
  @NonNull
  public DomainBitSet<T> union(@NonNull final BigInteger mask) {
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
  public DomainBitSet<T> unionVarArgs(@SuppressFBWarnings("unchecked") final T... elements) {
    return new SmallDomainBitSet<>(this.domain, this.set | this.arrayToLong(elements));
  }

  @Override
  public Stream<Pair<Object, Integer, T>> zipWithPosition() {
    return this.stream().map(e -> Pair.of(this.domain.indexOf(e), e));
  }

  /** This proxy class is used to serialize SmallDomainBitSet instances. */
  private static class SerializationProxy<T> implements java.io.Serializable {
    private static final long serialVersionUID = -4553759789742727112L;

    private final Domain<T>   domain;
    private final long        set;

    public SerializationProxy(@NonNull final Domain<T> domain, @NonNull final long set) {
      this.domain = domain;
      this.set = set;
    }

    private Object readResolve() {
      return new SmallDomainBitSet<>(this.domain, this.set);
    }
  }

  private Object writeReplace() {
    return new SerializationProxy<>(this.domain, this.set);
  }

  @SuppressFBWarnings({ "static-method", "unused" })
  private void readObject(final java.io.ObjectInputStream stream)
      throws java.io.InvalidObjectException {
    throw new java.io.InvalidObjectException("Proxy required");
  }
}
