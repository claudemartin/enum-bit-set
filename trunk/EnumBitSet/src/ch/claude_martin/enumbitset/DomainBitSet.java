package ch.claude_martin.enumbitset;

import static java.util.Objects.requireNonNull;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/** A bit set with a defined domain (universe). The domain is an ordered set of all elements that are
 * allowed in this bit set.
 * 
 * <p>
 * The methods that return a DomainBitSet&lt;T&gt; are expected to create a new object and not
 * change the state of this object. The implementation could be mutable or immutable. Mutable
 * implementations should not implement {@link Set}&lt;T&gt;, because the specifications of
 * {@link #equals(Object)} are different.
 * 
 * <p>
 * Methods such as {@link #union(Iterable)}, {@link #toSet()}, and {@link #complement()} return a
 * new and independent set. This allows a functional style of programming.
 * 
 * However, this set could be mutable. This allows the classic imperative style of programming.
 * 
 * <p>
 * Note that the implementation is not necessarily a bit set. It could be any data structure that
 * allows to add and remove elements of the domain. Therefore, all elements of the domain should
 * implement {@link #hashCode()} and {@link #equals(Object)} for correct behavior and better
 * performance. The {@link #iterator() iterator} can return the elements in any order.
 * 
 * @param <T>
 *          A type that all elements in the domain share.
 * 
 * @author <a href="http://claude-martin.ch/enumbitset/">Copyright &copy; 2014 Claude Martin</a> */
public interface DomainBitSet<T> extends Iterable<T>, Cloneable {

  /** Creates a set with the given domain, that contains all elements.
   * 
   * @param <T>
   *          The type of the elements.
   * @param elements
   *          The elements of the domain and the set.
   * @return A new DomainBitSet containing all given elements. */
  public static <T> DomainBitSet<T> allOf(@Nonnull final List<T> elements) {
    if (elements.size() > 64)
      return GeneralDomainBitSet.allOf(elements);
    else
      return SmallDomainBitSet.allOf(elements);
  }

  /** Creates a set with the given domain, that contains all elements.
   * 
   * @param <T>
   *          The type of the elements.
   * @param elements
   *          The elements of the domain and the set.
   * @return A new DomainBitSet containing all given elements. */
  @SafeVarargs
  public static <T> DomainBitSet<T> allOf(@Nonnull final T... elements) {
    if (elements.length > 64)
      return GeneralDomainBitSet.allOf(elements);
    else
      return SmallDomainBitSet.allOf(elements);
  }

  /** Creates a general bit set with a domain that consists of all elements of all given enum types.
   * Note that all bit masks become invalid when any of the types are altered. The set is empty
   * after creation.
   * 
   * @param enumTypes
   *          All enum types that define the domain. The ordering is relevant.
   * @return A new DomainBitSet that can contain enums from different enum types. */
  @SafeVarargs
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static DomainBitSet<Enum<?>> createMultiEnumBitSet(
      @Nonnull final Class<? extends Enum<?>>... enumTypes) {
    final List<Enum<?>> dom = new LinkedList<>();
    for (final Class type : enumTypes)
      for (final Object e : EnumSet.allOf(type))
        dom.add((Enum) e);
    if (dom.size() > 64)
      return GeneralDomainBitSet.noneOf(dom);
    else
      return SmallDomainBitSet.noneOf(dom);
  }

  /** Creates a set with the given domain, that contains none of the elements.
   * 
   * @param <T>
   *          The type of the elements.
   * @param elements
   *          The elements of the domain.
   * @return A new DomainBitSet containing none of the given elements. */
  public static <T> DomainBitSet<T> noneOf(@Nonnull final List<T> elements) {
    if (elements.size() > 64)
      return GeneralDomainBitSet.noneOf(elements);
    else
      return SmallDomainBitSet.noneOf(elements);
  }

  /** Creates a set with the given domain, that contains none of the elements.
   * 
   * @param <T>
   *          The type of the elements.
   * @param elements
   *          The elements of the domain.
   * @return A new DomainBitSet containing none of the given elements. */
  @SafeVarargs
  public static <T> DomainBitSet<T> noneOf(@Nonnull final T... elements) {
    if (elements.length > 64)
      return GeneralDomainBitSet.noneOf(elements);
    else
      return SmallDomainBitSet.noneOf(elements);
  }

  /** Default implementation of a {@link java.lang.Cloneable cloning-method} using
   * {@link #union(BigInteger)}.
   * 
   * @return <code>this.union(BigInteger.ZERO)</code> */
  @Nonnull
  @CheckReturnValue
  public default DomainBitSet<T> clone() {
    return this.union(BigInteger.ZERO);
  }

  /** Creates a new set with the same domain, initially containing all the elements of the domain
   * that are not contained in this set.
   * 
   * @return The complement of this set. */
  @Nonnull
  @CheckReturnValue
  public default DomainBitSet<T> complement() {
    return allOf(getDomain()).minus(this);

  }

  /** Returns <tt>true</tt> if this set contains the specified element.
   *
   * @see Collection#contains(Object)
   * @param o
   *          element whose presence in this collection is to be tested
   * @return <tt>true</tt> if this set contains the specified element
   * @throws ClassCastException
   *           if the type of the specified element is incompatible with this collection (<a
   *           href="#optional-restrictions">optional</a>)
   * @throws NullPointerException
   *           if the specified element is null. */
  public default boolean contains(@Nonnull final Object o) {
    requireNonNull(o);
    for (final T e : this)
      if (o.equals(e))
        return true;
    return false;
  }

  /** Returns <tt>true</tt> if this set contains all of the elements in the specified collection.
   *
   * @see Collection#containsAll(Collection)
   * @param c
   *          collection to be checked for containment in this collection
   * @return <tt>true</tt> if this collection contains all of the elements in the specified
   *         collection
   * @throws ClassCastException
   *           if the types of one or more elements in the specified collection are incompatible
   *           with this collection
   * @throws NullPointerException
   *           if the specified collection contains one or more null elements, or if the specified
   *           collection is null.
   * @see #contains(Object) */
  public default boolean containsAll(@Nonnull final Collection<?> c) {
    for (final Object e : requireNonNull(c))
      if (!this.contains(e))
        return false;
    return true;
  }

  /** Returns the Cartesian Product.
   * 
   * <p>
   * The returned set has a size of <code>this.size() * set.size()</code>.
   * 
   * @see #cross(DomainBitSet, BiConsumer)
   * @param <Y>
   *          The type of the elements in the given set.
   * @param set
   *          Another set.
   * @return the Cartesian Product.
   * @see #semijoin(DomainBitSet, BiPredicate) */
  @Nonnull
  @CheckReturnValue
  public default <Y extends Object> Set<Pair<Object, T, Y>> cross(@Nonnull final DomainBitSet<Y> set) {
    final HashSet<Pair<Object, T, Y>> result = new HashSet<>(this.size() * set.size());
    this.cross(set, Pair.curry(result::add)::apply);
    return result;
  }

  /** Creates the Cartesian Product and applies a given function to all coordinates.
   * <p>
   * Cartesian product of A and B, denoted <code>A × B</code>, is the set whose members are all
   * possible ordered pairs <code>(a,b)</code> where a is a member of A and b is a member of B. <br>
   * The Cartesian product of <code>{1, 2}</code> and <code>{red, white}</code> is {(1, red), (1,
   * white), (2, red), (2, white)}.
   * <p>
   * The consumer will be invoked exactly <code>(this.size() &times; set.size())</code> times.
   * <p>
   * A BiFunction can be used by passing <code>::apply</code> as consumer.<br>
   * Example: <code>Pair.curry(mySet::add)::apply</code>.
   * 
   * @see #cross(DomainBitSet)
   * @param <Y>
   *          The type of the elements in the given set.
   * @param set
   *          Another set.
   * @param consumer
   *          A function to consume two elements.
   * @see #semijoin(DomainBitSet, BiPredicate) */
  public default <Y> void cross(@Nonnull final DomainBitSet<Y> set,
      @Nonnull final BiConsumer<T, Y> consumer) {
    requireNonNull(consumer);
    requireNonNull(set);
    if (set.isEmpty())
      return; // Nothing to do...
    this.forEach(x -> set.forEach(y -> consumer.accept(x, y)));
  }

  /** Searches an object in the domain of this set.
   * 
   * @param object
   *          The object to be searched.
   * @throws NullPointerException
   *           if the object is <tt>null</tt>.
   * @return <tt>true</tt>, iff the domain contains the given object. */
  public default boolean domainContains(@Nonnull final T object) {
    return this.getDomain().contains(requireNonNull(object));
  }

  /** Compares the specified object with this domain bit set for equality. Returns <tt>true</tt>, iff
   * the given object is also a {@link DomainBitSet}, the two sets have the same domain, and every
   * member of the given set is contained in this set.
   * 
   * <p>
   * Comparison of elements only: <br>
   * <code>this.ofEqualElements(other)</code><br>
   * Which is equivalent to:<br>
   * <code>this.toSet().equals(other.toSet())</code>
   * <p>
   * Comparison of domain only: <br>
   * <code>this.ofEqualDomain(other)</code>
   * 
   * @see #ofEqualElements(DomainBitSet)
   * @return True, if this also a {@link DomainBitSet}, with the same domain and elements. */
  @Override
  public boolean equals(final Object other);

  /** Returns the value of the bit with the specified index. The value is {@code true} if the bit
   * with the index {@code bitIndex} is currently set in this {@code BitSet}; otherwise, the result
   * is {@code false}.
   * <p>
   * Note that not all DomainBitSets are implemented as a bit set. In that case this method emulates
   * the behavior of an actual bit set.
   * 
   * @param bitIndex
   *          the bit index
   * @return the value of the bit with the specified index
   * @see #contains(Object)
   * @see BitSet#get(int)
   * @see BigInteger#testBit(int)
   * @throws IndexOutOfBoundsException
   *           if the specified index is negative or out of bounds. */
  public boolean getBit(final int bitIndex) throws IndexOutOfBoundsException;

  /** Returns a distinct list, containing all elements of the domain. There is no guarantee that the
   * set is the same for multiple invocations.
   * <p>
   * All elements are ordered as they are defined in the domain.
   * <p>
   * Note that the returned set is immutable.
   * 
   * @return The {@link Domain} of this set. */
  public Domain<T> getDomain();

  /** Returns an Optional that might contain the element at the specified position.
   * <p>
   * The inverse has to be done in the domain: <br/>
   * <code>mySet.{@linkplain #getDomain()}.{@linkplain Domain#indexOf(Object) indexOf(element)};</code>
   * 
   * @param index
   *          index of an element in the domain.
   * @see #zipWithPosition()
   * @see #getBit(int)
   * @see Domain#indexOf(Object)
   * @return Optional that might contain the element at the specified position.
   * @throws IndexOutOfBoundsException
   *           if the index is out of range */
  @Nonnull
  public default Optional<T> getElement(final int index) {
    final T o = this.getDomain().get(index);
    if (this.contains(o))
      return Optional.of(o);
    else
      return Optional.empty();
  }

  /** Hash code of domain and elements.
   * <p>
   * {@inheritDoc} */
  @Override
  public int hashCode();

  /** The intersection of this and a given mask.
   * 
   * @param mask
   *          The mask of the other set.
   * 
   * @return Intersection of this and the given mask. */
  @Nonnull
  @CheckReturnValue
  public DomainBitSet<T> intersect(@Nonnull final BigInteger mask);

  /** The intersection of this and a given bit set.
   * 
   * @param set
   *          The bit set representation of the other set.
   * 
   * @return Intersection of this and the given bit set. */
  @Nonnull
  @CheckReturnValue
  public DomainBitSet<T> intersect(@Nonnull final BitSet set);

  /** Intersection of this and the given set.
   * 
   * @param set
   *          An {@link Iterable} collection of elements from the domain.
   * @throws IllegalArgumentException
   *           If any of the elements are not in the domain.
   * @return Intersection of this and the given collection. */
  @Nonnull
  @CheckReturnValue
  public DomainBitSet<T> intersect(@Nonnull final Iterable<T> set) throws IllegalArgumentException;

  /** Intersection of this and the given set.
   * 
   * @param mask
   *          The bit mask of another set.
   * @return A new DomainBitSet that represents the intersection.
   * @throws MoreThan64ElementsException
   *           If the domain contains more than 64 elements, then long can't be used.
   * @throws IllegalArgumentException
   *           If the domain contains less than 64 elements then some long values are illegal. */
  @Nonnull
  @CheckReturnValue
  public DomainBitSet<T> intersect(@Nonnull final long mask) throws MoreThan64ElementsException;

  /** The intersection of this set and a set represented by an array (varargs). The name is different
   * so that it is unambiguous.
   * 
   * @see #intersect(Iterable)
   * @param set
   *          A set as an array. Duplicates are ignored. Must not be nor contain <code>null</code>.
   * @return The intersection of this and the given set. */
  @Nonnull
  @CheckReturnValue
  public default DomainBitSet<T> intersectVarArgs(
      @Nonnull @SuppressWarnings("unchecked") final T... set) {
    return this.intersect(Arrays.asList(requireNonNull(set)));
  }

  /** Returns <tt>true</tt> if this set contains no elements.
   * 
   * @return <tt>true</tt> if this set contains no elements
   * @see Collection#isEmpty() */
  public default boolean isEmpty() {
    return this.size() == 0;
  }

  /** Returns an iterator over elements of type T.
   * 
   * <p>
   * The order is not defined as this could be backed by a set. Iteration in the same order as the
   * domain can be done like this: <br>
   * <code>domainBitSet.getDomain().stream().filter(domainBitSet::contains).forEach(...)</code> */
  @Override
  @Nonnull
  public Iterator<T> iterator();

  /** Returns a new set with elements of a given domain, containing all mapped elements. Mapping is
   * done by index in the domain. Therefore the new domains must not be smaller than the domain of
   * this set.
   * 
   * <p>
   * If the given domain is the same as the domain of this set then the returned value is equal to
   * <code>this.clone()</code>.
   * 
   * @param domain
   *          The new domain
   * @param mapper
   *          function to map from T to S.
   * @param <S>
   *          Type of given domain. It has to be the same size or larger than the domain of this
   *          set.
   * @throws IllegalArgumentException
   *           if the given domain contains less elements.
   * @return new set, using the given domain. */
  @SuppressWarnings("unchecked")
  @Nonnull
  @CheckReturnValue
  public default <S> DomainBitSet<S> map(final Domain<S> domain) {
    requireNonNull(domain, "domain");
    if (domain == this.getDomain())
      return (DomainBitSet<S>) this.clone();
    if (domain.size() < this.getDomain().size())
      throw new IllegalArgumentException("The given domain is too small.");
    return this.map(domain, (t) -> domain.get(this.getDomain().indexOf(t)));
  }

  /** Returns a new set with elements of a given domain, containing all mapped elements.
   * <p>
   * This is a convenience method. The same can be done with: <code>this.stream().map(mapper)</code>
   * 
   * @param domain
   *          The new domain
   * @param mapper
   *          function to map from T to S.
   * @param <S>
   *          Type of given domain.
   * 
   * @see Stream#map(Function)
   * @throws IllegalArgumentException
   *           if the mapper returns illegal elements.
   * @return new set, using the given domain. */
  @Nonnull
  @CheckReturnValue
  public default <S> DomainBitSet<S> map(@Nonnull final Domain<S> domain,
      @Nonnull final Function<T, S> mapper) {
    requireNonNull(domain, "domain");
    requireNonNull(mapper, "mapper");
    return this.stream().map(mapper).collect(BitSetUtilities.toDomainBitSet(domain.factory()));
  }

  /** The relative complement of this set and a set represented by a {@link BigInteger}.
   * 
   * @param mask
   *          The other set as a bit mask.
   * @throws IllegalArgumentException
   *           If the parameter is negative.
   * @return The relative complement of this and the given set. */
  @Nonnull
  @CheckReturnValue
  public DomainBitSet<T> minus(@Nonnull final BigInteger mask);

  /** The relative complement of this set and a set represented by a {@link BitSet}.
   * 
   * @param set
   *          The other set.
   * @return The relative complement of this and the given set. */
  @Nonnull
  @CheckReturnValue
  public DomainBitSet<T> minus(@Nonnull final BitSet set);

  /** The relative complement of this set and another set.
   * 
   * @param set
   *          The other set.
   * @return The relative complement of this and the given set. */
  @Nonnull
  @CheckReturnValue
  public DomainBitSet<T> minus(@Nonnull final Iterable<T> set);

  /** The relative complement of this set and a set represented by a bit mask.
   * 
   * @param mask
   *          The mask representing the other set.
   * @throws MoreThan64ElementsException
   *           If the domain contains more than 64 elements, then long can't be used.
   * @throws IllegalArgumentException
   *           If the domain contains less than 64 elements then some long values are illegal.
   * @return The relative complement of this and the given set. */
  @Nonnull
  @CheckReturnValue
  public DomainBitSet<T> minus(final long mask) throws MoreThan64ElementsException;

  /** The relative complement of this set and a set represented by an array (varargs). The name is
   * different so that it is unambiguous.
   * 
   * @see #minus(Iterable)
   * @param set
   *          A set as an array. Duplicates are ignored. Must not be nor contain <code>null</code>.
   * @return The relative complement of this and the given set. */
  @Nonnull
  @CheckReturnValue
  public default DomainBitSet<T> minusVarArgs(
      @Nonnull @SuppressWarnings("unchecked") final T... set) {
    return this.minus(Arrays.asList(requireNonNull(set)));
  }

  /** Compares the domains.
   * <p>
   * This is equal to, but could be a bit faster than
   * <code>this.getDomain().equals(set.getDomain())</code>.
   * 
   * @param set
   *          The other set.
   * @return <code>true</code> if both are of equal domains. */
  public default boolean ofEqualDomain(@Nonnull final DomainBitSet<T> set) {
    return this.getDomain().equals(set.getDomain());
  }

  /** Compares the elements, ignoring the domains.
   * <p>
   * This is equal to, but could be a bit faster than <code>this.toSet().equals(set.toSet())</code>.
   * 
   * @param set
   *          The other set.
   * @see Set#equals(Object)
   * @return <code>true</code> if both contain the same elements. */
  public default boolean ofEqualElements(@Nonnull final DomainBitSet<T> set) {
    return this.toSet().equals(set.toSet());
  }

  /** Returns a possibly parallel {@code Stream} with this set as its source. It is allowable for
   * this method to return a sequential stream.
   *
   * @see Collection#parallelStream()
   * @return a possibly parallel {@code Stream} over the elements in this set */
  @Nonnull
  public default Stream<T> parallelStream() {
    return StreamSupport.stream(spliterator(), true);
  }

  /** Returns a new set with all elements in this set, that have a matching element in the other set.
   * <p>
   * This is basically the same as {@link #cross(DomainBitSet)}, but filtered by a predicate. All
   * <code>this.size() &times; set.size()</code> combinations are tested! The term "semijoin" is
   * used in relational algebra, where the predicate compares the primary attributes of two tuples
   * (natural join).
   * 
   * @param set
   *          The other set
   * @param predicate
   *          Predicate to match the tuples
   * @return <code>this ⋉ set</code>.
   * @see #cross(DomainBitSet)
   * @see #cross(DomainBitSet, BiConsumer)
   * @see #map(Domain)
   * @see #map(Domain, Function) */
  @Nonnull
  @CheckReturnValue
  public default <S> DomainBitSet<T> semijoin(final DomainBitSet<S> set,
      final BiPredicate<T, S> predicate) {
    final List<T> result = new ArrayList<>();
    this.cross(set, (a, b) -> {
      if (predicate.test(a, b))
        result.add(a);
    });
    return this.intersect(result);
  }

  /** The number of elements in this set.
   * 
   * @see Collection#size()
   * @return The number of elements in this set. */
  public int size();

  /** Creates a {@link Spliterator} over the elements in this collection.
   * 
   * @see Collection#spliterator()
   * @return a {@code Spliterator} over the elements in this collection */
  @Override
  @Nonnull
  default Spliterator<T> spliterator() {
    return Spliterators.spliterator(iterator(), size(), Spliterator.SIZED | Spliterator.DISTINCT
        | Spliterator.NONNULL);
  }

  /** Returns a sequential {@code Stream} with this collection as its source.
   *
   * @see Collection#stream()
   * @return a sequential {@code Stream} over the elements in this collection */
  @Nonnull
  default Stream<T> stream() {
    return StreamSupport.stream(spliterator(), false);
  }

  /** A representation of the elements in this set as a {@link BigInteger}.
   * 
   * @return The bit mask as a {@link BigInteger}. */
  @Nonnull
  @CheckReturnValue
  public BigInteger toBigInteger();

  /** A representation of the elements in this set as a {@link BitSet}.
   * 
   * @return The set as a {@link BitSet}. */
  @Nonnull
  @CheckReturnValue
  public BitSet toBitSet();

  /** A representation of the elements in this set as a {@link Long long}.
   * 
   * @throws MoreThan64ElementsException
   *           If the domain contains more than 64 elements.
   * @return The set as a {@link Long long}. */
  @CheckReturnValue
  public long toLong() throws MoreThan64ElementsException;

  /** A regular set, with no defined domain. Note that the returned set can be compared to other
   * regular sets, which this always returns <code>false</code> for other sets without a domain.
   * 
   * @return A set, without the domain. */
  @Nonnull
  @CheckReturnValue
  public Set<T> toSet();

  /** The union of this set and a set represented by a {@link BitSet}.
   * <p>
   * Note: A fast version for BigInteger.ZERO should exist for each implementation!
   * 
   * @param mask
   *          A BitSet representing another set.
   * @return The union of this set and the given set. */
  @Nonnull
  @CheckReturnValue
  public DomainBitSet<T> union(@Nonnull final BigInteger mask);

  /** The union of this set and a set represented by a {@link BitSet}.
   * 
   * @param set
   *          A BitSet representing another set.
   *
   * @return The union of this set and the given set. */
  @Nonnull
  @CheckReturnValue
  public DomainBitSet<T> union(@Nonnull final BitSet set);

  /** The union of this set and a set represented by an {@link Iterable iterable} collection.
   * 
   * @param set
   *          An Iterable representing another set.
   *
   * @return The union of this set and the given set. */
  @Nonnull
  @CheckReturnValue
  public DomainBitSet<T> union(@Nonnull final Iterable<T> set);

  /** The union of this set and a set represented by a bit mask.
   * 
   * @param mask
   *          A bit mask representing another set.
   *
   * @throws MoreThan64ElementsException
   *           If the domain contains more than 64 elements, then long can't be used.
   * @throws IllegalArgumentException
   *           If the domain contains less than 64 elements then some long values are illegal.
   * @return The union of this set and the given set. */
  @Nonnull
  @CheckReturnValue
  public DomainBitSet<T> union(final long mask) throws MoreThan64ElementsException;

  /** The union of this set and a set represented by an array (varargs). The name is different so
   * that it is unambiguous.
   * 
   * @see #union(Iterable)
   * @param set
   *          A set as an array. Duplicates are ignored. Must not be nor contain <code>null</code>.
   * @return The union of this and the given set. */
  @Nonnull
  @CheckReturnValue
  public default DomainBitSet<T> unionVarArgs(
      @Nonnull @SuppressWarnings("unchecked") final T... set) {
    return this.union(Arrays.asList(requireNonNull(set)));
  }

  /** Returns a sequential stream with pairs of all elements of this set and their position in the
   * domain.
   * <p>
   * This can be collected to a {@link Map} using {@link BitSetUtilities#toTreeMap() toTreeMap}.
   * 
   * @see BitSetUtilities#toTreeMap()
   * @see #getElement(int)
   * @return A stream of elements and their position. */
  @Nonnull
  @CheckReturnValue
  public default Stream<Pair<Object, Integer, T>> zipWithPosition() {
    final Domain<T> domain = this.getDomain();
    return domain.stream().filter(this::contains).map(e -> Pair.of(domain.indexOf(e), e));
  }
}
