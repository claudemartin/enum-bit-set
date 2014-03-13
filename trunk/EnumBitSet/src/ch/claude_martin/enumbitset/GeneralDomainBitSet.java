package ch.claude_martin.enumbitset;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

import java.math.BigInteger;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/** Provides a mutable implementation of {@link DomainBitSet}, that can be used with any type. */
public final class GeneralDomainBitSet<T> implements DomainBitSet<T>, Collection<T> {

  /** Creates a set with the given domain, that contains all elements.
   * 
   * @param <X>
   *          The type of the set and its domain.
   * @param domain
   *          The domain.
   * @return New GeneralDomainBitSet of given domain, containing all elements. */
  public static <X> GeneralDomainBitSet<X> allOf(final LinkedHashSet<X> domain) {
    final GeneralDomainBitSet<X> result = new GeneralDomainBitSet<>(new DefaultDomain<>(domain));
    result.addAll(domain);
    return result;
  }

  /** Creates a set with the given domain, that contains all elements.
   * 
   * @param <X>
   *          The type of the set and its domain.
   * @param domain
   *          The domain.
   * @return New GeneralDomainBitSet of given domain, containing all elements. */
  public static <X> GeneralDomainBitSet<X> allOf(final List<X> domain) {
    final GeneralDomainBitSet<X> result = new GeneralDomainBitSet<>(new DefaultDomain<>(domain));
    result.addAll(domain);
    return result;
  }

  /** Creates a set with the given domain, that contains all elements.
   * 
   * @param <X>
   *          The type of the set and its domain.
   * @param domain
   *          The domain.
   * @return New GeneralDomainBitSet of given domain, containing all elements. */
  @SafeVarargs
  public static <X> GeneralDomainBitSet<X> allOf(final X... domain) {
    return allOf(asList(domain));
  }

  /** Creates an empty set with the given domain.
   * 
   * @param <X>
   *          The type of the set and its domain.
   * @param domain
   *          The domain.
   * @return Empty GeneralDomainBitSet of given domain. */
  public static <X> GeneralDomainBitSet<X> noneOf(final LinkedHashSet<X> domain) {
    return new GeneralDomainBitSet<>(new DefaultDomain<>(domain));
  }

  /** Creates an empty set with the given domain.
   * 
   * @param <X>
   *          The type of the set and its domain.
   * @param domain
   *          The domain.
   * @return Empty GeneralDomainBitSet of given domain. */
  public static <X> GeneralDomainBitSet<X> noneOf(final List<X> domain) {
    return new GeneralDomainBitSet<>(new DefaultDomain<>(domain));
  }

  /** Creates an empty set with the given domain.
   * 
   * @param <X>
   *          The type of the set and its domain.
   * @param domain
   *          The domain.
   * @return Empty GeneralDomainBitSet of given domain. */
  @SafeVarargs
  public static <X> GeneralDomainBitSet<X> noneOf(final X... domain) {
    return noneOf(asList(domain));
  }

  /** Creates an empty set with the given domain, containing the given elements.
   * 
   * @param <T>
   *          The type of the set and its domain.
   * @param domain
   *          The domain.
   * @param initialSet
   *          The elements to be contained.
   * @return New GeneralDomainBitSet of given domain and elements. */
  public static <T> GeneralDomainBitSet<T> of(final LinkedHashSet<T> domain,
      final Collection<T> initialSet) {
    final GeneralDomainBitSet<T> result = new GeneralDomainBitSet<>(new DefaultDomain<>(domain));
    result.addAll(initialSet);
    return result;
  }

  /** Creates an empty set with the given domain, containing the given elements.
   * 
   * @param <T>
   *          The type of the set and its domain.
   * @param domain
   *          The domain.
   * @param initialSet
   *          The elements to be contained.
   * @return New GeneralDomainBitSet of given domain and elements. */
  public static <T> GeneralDomainBitSet<T> of(final List<T> domain, final Collection<T> initialSet) {
    final GeneralDomainBitSet<T> result = new GeneralDomainBitSet<>(new DefaultDomain<>(domain));
    result.addAll(initialSet);
    return result;
  }

  private final Set<T>    set;

  private final Domain<T> domain;

  private GeneralDomainBitSet(final Domain<T> domain) {
    this.domain = domain;
    this.set = new HashSet<>();
  }

  /** Copy-Constructor that returns an exact clone. */
  private GeneralDomainBitSet(final GeneralDomainBitSet<T> bitset) {
    this.domain = bitset.domain;
    this.set = new HashSet<>(bitset.set);
  }

  /** Copy-Constructor that returns an empty/full clone.
   * <ul>
   * <li>empty=true &rarr; Set is empty.</li>
   * <li>empty=false &rarr; Set if full.</li>
   * </ul> */
  @SuppressWarnings("unused")
  private GeneralDomainBitSet(final GeneralDomainBitSet<T> bitset, final boolean empty) {
    this.domain = bitset.domain;
    this.set = empty ? new HashSet<>(this.domain.size()) : new HashSet<T>(bitset.domain);
  }

  @Override
  public boolean add(final T e) {
    this.check(e);
    return this.set.add(e);
  }

  /** {@inheritDoc}
   * 
   * @throws IllegalArgumentException
   *           If any of the elements in <tt>c</tt> is not element of the given domain. In that case
   *           the set is not altered at all. */
  @Override
  public boolean addAll(final Collection<? extends T> c) {
    c.forEach(this::check);
    return this.set.addAll(c);
  }

  private void check(final T e) {
    if (!this.domainContains(e))
      throw new IllegalArgumentException(String.format(
          "The object '%s' is not element of the domain.", e));
  }

  private void checkMask(final long mask) throws MoreThan64ElementsException,
      IllegalArgumentException {
    final int size = this.domain.size();
    if (size > 64)
      throw new MoreThan64ElementsException();
    if (mask >>> size > 0)
      throw new IllegalArgumentException("Mask cotains more elements than the domain.");
  }

  @Override
  public void clear() {
    this.set.clear();
  }

  @Override
  public GeneralDomainBitSet<T> clone() {
    return new GeneralDomainBitSet<>(this);
  }

  @Override
  public GeneralDomainBitSet<T> complement() {
    if (this.set.size() == this.domain.size())
      return new GeneralDomainBitSet<>(this, true);
    final GeneralDomainBitSet<T> result = new GeneralDomainBitSet<>(this, false);
    if (this.isEmpty())
      return result;
    result.removeAll(this);
    return result;
  }

  @Override
  public boolean contains(final Object o) {
    return this.set.contains(o);
  }

  @Override
  public boolean containsAll(final Collection<?> c) {
    return this.set.containsAll(c);
  }

  @Override
  public boolean domainContains(final T object) {
    return this.domain.contains(requireNonNull(object));
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean equals(final Object o) {
    return this == o || o instanceof DomainBitSet && this.ofEqualDomain((DomainBitSet<T>) o)
        && this.ofEqualElements((DomainBitSet<T>) o);
  }

  @Override
  public void forEach(final Consumer<? super T> action) {
    this.set.forEach(action);
  }

  @Override
  public boolean getBit(final int bitIndex) throws IndexOutOfBoundsException {
    return this.contains(this.domain.get(bitIndex));
  }

  @Override
  public Domain<T> getDomain() {
    return this.domain;
  }

  @Override
  public int hashCode() {
    return this.set.hashCode();
  }

  @Override
  public GeneralDomainBitSet<T> intersect(final BigInteger mask) {
    return this.intersect(BitSetUtilities.asBitSet(mask));
  }

  @Override
  public GeneralDomainBitSet<T> intersect(final BitSet other) {
    final GeneralDomainBitSet<T> result = new GeneralDomainBitSet<>(this, true);
    if (other.isEmpty())
      return result;
    for (int i = other.nextSetBit(0); i >= 0; i = other.nextSetBit(i + 1)) {
      final T value = this.domain.get(i);
      if (this.contains(value))
        result.add(value);
    }
    return result;
  }

  @Override
  public GeneralDomainBitSet<T> intersect(final Iterable<T> other) {
    final GeneralDomainBitSet<T> result = new GeneralDomainBitSet<>(this, true);
    other.forEach(t -> {
      this.check(t);
      if (this.contains(t))
        result.add(t);
    });
    return result;
  }

  @Override
  public GeneralDomainBitSet<T> intersect(final long mask) throws MoreThan64ElementsException {
    this.checkMask(mask);
    if (mask == 0)
      return new GeneralDomainBitSet<>(this, true);
    final GeneralDomainBitSet<T> result = new GeneralDomainBitSet<>(this);
    final int domSize = this.domain.size();
    for (int i = 0; i < domSize; i++)
      if ((mask & 1L << i) == 0)
        result.remove(this.domain.get(i));
    return result;
  }

  @Override
  public GeneralDomainBitSet<T> intersectVarArgs(@SuppressWarnings("unchecked") final T... other) {
    final GeneralDomainBitSet<T> result = new GeneralDomainBitSet<>(this);
    final HashSet<T> hashset = new HashSet<>(asList(other));
    result.set.removeIf(t -> !hashset.contains(t));
    return result;
  }

  @Override
  public boolean isEmpty() {
    return this.set.isEmpty();
  }

  @Override
  public Iterator<T> iterator() {
    return this.set.iterator();
  }

  @Override
  public GeneralDomainBitSet<T> minus(final BigInteger mask) {
    return this.minus(BitSetUtilities.asBitSet(mask));
  }

  @Override
  public GeneralDomainBitSet<T> minus(final BitSet other) {
    final GeneralDomainBitSet<T> result = new GeneralDomainBitSet<>(this);
    for (int i = other.nextSetBit(0); i >= 0; i = other.nextSetBit(i + 1))
      result.remove(this.domain.get(i));
    return result;
  }

  @Override
  public GeneralDomainBitSet<T> minus(final Iterable<T> other) {
    final GeneralDomainBitSet<T> result = new GeneralDomainBitSet<>(this);
    other.forEach(result::remove);
    return result;
  }

  @Override
  public GeneralDomainBitSet<T> minus(final long mask) throws MoreThan64ElementsException {
    this.checkMask(mask);
    final GeneralDomainBitSet<T> result = new GeneralDomainBitSet<>(this);
    if (mask == 0)
      return result;
    final int domSize = this.domain.size();
    for (int n = 0; n < domSize; n++)
      if ((mask & 1L << n) != 0)
        result.remove(this.domain.get(n));
    return result;
  }

  @Override
  public GeneralDomainBitSet<T> minusVarArgs(@SuppressWarnings("unchecked") final T... other) {
    final GeneralDomainBitSet<T> result = new GeneralDomainBitSet<>(this);
    result.removeAll(asList(other));
    return result;
  }

  @Override
  public boolean ofEqualElements(final DomainBitSet<T> other) {
    if (other instanceof GeneralDomainBitSet)
      return this.set.equals(((GeneralDomainBitSet<T>) other).set);
    return this.set.equals(other.toSet());
  }

  @Override
  public Stream<T> parallelStream() {
    return this.set.parallelStream();
  }

  @Override
  public boolean remove(final Object o) {
    return this.set.remove(o);
  }

  @Override
  public boolean removeAll(final Collection<?> c) {
    return this.set.removeAll(c);
  }

  @Override
  public boolean removeIf(final Predicate<? super T> filter) {
    return this.set.removeIf(filter);
  }

  @Override
  public boolean retainAll(final Collection<?> c) {
    return this.set.retainAll(c);
  }

  @Override
  public int size() {
    return this.set.size();
  }

  @Override
  public Spliterator<T> spliterator() {
    return this.set.spliterator();
  }

  @Override
  public Stream<T> stream() {
    return this.set.stream();
  }

  @Override
  public Object[] toArray() {
    return this.set.toArray();
  }

  @Override
  public <X> X[] toArray(final X[] a) {
    return this.set.toArray(a);
  }

  @Override
  public BigInteger toBigInteger() {
    if (this.domain.size() <= 64)
      return BitSetUtilities.asBigInteger(this.toLong());
    else
      return BitSetUtilities.asBigInteger(this.toBitSet());
  }

  @Override
  public BitSet toBitSet() {
    final BitSet result = new BitSet(this.domain.size());
    for (final T t : this.set)
      result.set(this.domain.indexOf(t), true);
    return result;
  }

  /** Returns a new LinkedHashSet with the same elements, ordered as they appear in the domain.
   * 
   * @return A {@link LinkedHashSet} containing all elements of this set. */
  public LinkedHashSet<T> toLinkedHashSet() {
    final LinkedHashSet<T> result = new LinkedHashSet<>();
    for (final T t : this.domain)
      if (this.set.contains(t))
        result.add(t);
    return result;
  }

  @Override
  public long toLong() throws MoreThan64ElementsException {
    long result = 0L;
    if (this.domain.size() > 64)
      throw new MoreThan64ElementsException();
    for (final T t : this.set)
      result |= 1L << this.domain.indexOf(t);
    return result;
  }

  @Override
  public Set<T> toSet() {
    return new HashSet<>(this.set);
  }

  @Override
  public String toString() {
    // Note: This could theoretically lead to recursion, but then the domain contains references to
    // mutable objects, which is invalid.
    return this.set.toString();
  }

  @Override
  public GeneralDomainBitSet<T> union(final BigInteger mask) {
    return this.union(BitSetUtilities.asBitSet(mask));
  }

  @Override
  public GeneralDomainBitSet<T> union(final BitSet other) {
    final GeneralDomainBitSet<T> result = new GeneralDomainBitSet<>(this);
    for (int i = other.nextSetBit(0); i >= 0; i = other.nextSetBit(i + 1))
      result.add(this.domain.get(i));
    return result;
  }

  @Override
  public GeneralDomainBitSet<T> union(final Iterable<T> other) {
    final GeneralDomainBitSet<T> result = new GeneralDomainBitSet<>(this);
    other.forEach(result::add);
    return result;
  }

  @Override
  public GeneralDomainBitSet<T> union(final long mask) throws MoreThan64ElementsException {
    this.checkMask(mask);
    final GeneralDomainBitSet<T> result = new GeneralDomainBitSet<>(this);
    if (mask == 0)
      return result;
    final int domSize = this.domain.size();
    for (int i = 0; i < domSize; i++)
      if ((mask & 1L << i) != 0)
        result.add(this.domain.get(i));
    return result;
  }

  @Override
  public GeneralDomainBitSet<T> unionVarArgs(@SuppressWarnings("unchecked") final T... other) {
    final GeneralDomainBitSet<T> result = new GeneralDomainBitSet<>(this);
    result.addAll(asList(other));
    return result;
  }

}
