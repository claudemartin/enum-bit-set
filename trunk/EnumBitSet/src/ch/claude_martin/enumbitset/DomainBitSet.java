package ch.claude_martin.enumbitset;

import static java.util.Objects.requireNonNull;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

/** A bit set with a defined domain (universe). The domain is an ordered set of all elements that are
 * allowed in this bit set.
 * 
 * <p>
 * The methods that return a DomainBitSet&lt;T&gt; are expected to create a new object and not
 * change the state of this object. The implementation could be mutable or immutable. Mutable
 * implementations are expected to also implement {@link Set}&lt;T&gt;.
 * 
 * <p>
 * Note that ambiguity arises when any of the types are used that are supported. <br>
 * Example: <code><pre>
 * DomainBitSet&lt;BigInteger&gt; set = ...;
 * final BigInteger value = BigInteger.valueOf(42);
 * set.union(value); // bit mask: "101010" =&gt; 3 elements
 * set.union(value, value); // element 42
 * set.union(Arrays.asList(value)); // element 42
 * set.union(Collections.singleton(value)); // element 42
 * </pre></code>
 * 
 * <p>
 * Note that the implementation is not necessarily a bit set. It could be any data structure that
 * allows to add and remove elements of the domain. Therefore, all elements of the domain should
 * implement {@link #hashCode()} and {@link #equals(Object)} for correct behavior and better
 * performance.
 * 
 * <p>
 * Iteration over s domain bit set always returns all containing elements in the same order as they
 * appear in the domain.
 * 
 * @param <T>
 *          A type that all elements in the domain share. */
public interface DomainBitSet<T> extends Iterable<T>, Cloneable {

  /** Creates a set with the given domain, that contains all elements.
   * 
   * @param <T>
   *          The type of the elements.
   * @param elements
   *          The elements of the domain and the set.
   * @return A new DomainBitSet containing all given elements. */
  public static <T> DomainBitSet<T> allOf(final List<T> elements) {
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
  public static <T> DomainBitSet<T> allOf(final T... elements) {
    if (elements.length > 64)
      return GeneralDomainBitSet.allOf(elements);
    else
      return SmallDomainBitSet.allOf(elements);
  }

  /** Creates a general bit set with a domain that consists of all elements of all given enum types.
   * Note that all bit masks become invalid when any of the types are altered. The set is empty
   * after creation. */
  @SafeVarargs
  @SuppressWarnings("unchecked")
  public static <E extends Enum<E>> DomainBitSet<E> createMultiEnumBitSet(
      final Class<? extends Enum<?>>... enumTypes) {
    final List<E> dom = new LinkedList<>();
    for (final Class<? extends Enum<?>> type : enumTypes)
      for (final E e : EnumSet.allOf((Class<E>) type))
        dom.add(e);
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
  public static <T> DomainBitSet<T> noneOf(final List<T> elements) {
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
  public static <T> DomainBitSet<T> noneOf(final T... elements) {
    if (elements.length > 64)
      return GeneralDomainBitSet.noneOf(elements);
    else
      return SmallDomainBitSet.noneOf(elements);
  }

  /** Default implementation of a {@link java.lang.Cloneable cloning-method} using
   * {@link #union(BigInteger)}.
   * 
   * @return <code>this.union(BigInteger.ZERO)</code> */
  public default DomainBitSet<T> clone() {
    return this.union(BigInteger.ZERO);
  }

  /** Creates a new set with the same domain, initially containing all the elements of the domain
   * that are not contained in this set.
   * 
   * @return The complement of this set. */
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
  public default boolean contains(final Object o) {
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
  public default boolean containsAll(final Collection<?> c) {
    for (final Object e : requireNonNull(c))
      if (!this.contains(e))
        return false;
    return true;
  }

  /** Returns the Cartesian Product.
   * 
   * @param set
   *          Another set.
   * @return the Cartesian Product */

  public default <Y extends Object> List<Pair<Object, T, Y>> cross(final DomainBitSet<Y> set) {
    final ArrayList<Pair<Object, T, Y>> result = new ArrayList<>(this.size() * set.size());
    this.cross(set, Pair.curry(result::add));
    return result;
  }

  /** Returns the Cartesian Product.
   * <p>
   * Cartesian product of A and B, denoted <code>A × B</code>, is the set whose members are all
   * possible ordered pairs <code>(a,b)</code> where a is a member of A and b is a member of B. The
   * Cartesian product of <code>{1, 2}</code> and <code>{red, white}</code> is {(1, red), (1,
   * white), (2, red), (2, white)}.
   * 
   * @param set
   *          Another set.
   * @param consumer
   *          A function to consume two elements. The return value should always be
   *          <code>true</code>, but it is ignored. Example: <code>Pair.curry(result::add)</code>.
   * 
   * @return The Cartesian Product of <code>this</code> and <code>set</code>. */
  public default <Y> void cross(final DomainBitSet<Y> set, final BiFunction<T, Y, ?> consumer) {
    // for (final T e1 : this) for (final Y e2 : set) consumer.apply(e1, e2);
    this.forEach(x -> set.forEach(y -> consumer.apply(x, y)));
  }

  public default boolean domainContains(final T object) {
    return this.getDomain().contains(object);
  }

  /** Compares the specified object with this domain bit set for equality. Returns <tt>true</tt> if
   * the given object is also a {@link DomainBitSet}, the two sets have the same domain, and every
   * member of the given set is contained in this set.
   * 
   * <p>
   * Comparison of elements only: <br/>
   * <code>this.toSet().equals(other.toSet())</code>
   * <p>
   * Comparison of domain only: <br/>
   * <code>this.ofEqualDomain(other)</code>
   * 
   * @see #ofEqualElements(DomainBitSet) */
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
  public boolean getBit(int bitIndex) throws IndexOutOfBoundsException;

  /** Returns a distinct list, containing all elements of the domain. There is no guarantee that the
   * set is the same for multiple invocations.
   * <p>
   * All elements are ordered as they are defined in the domain.
   * <p>
   * Note that the returned set should be immutable. */
  public Domain<T> getDomain();

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
  public DomainBitSet<T> intersect(BigInteger mask);

  /** The intersection of this and a given bit set.
   * 
   * @param mask
   *          The bit set representation of the other set.
   * 
   * @return Intersection of this and the given bit set. */
  public DomainBitSet<T> intersect(BitSet set);

  /** Intersection of this and the given set.
   * 
   * @param set
   *          An {@link Iterable} collection of elements from the domain.
   * @throws IllegalArgumentException
   *           If any of the elements are not in the domain.
   * @return */
  public DomainBitSet<T> intersect(Iterable<T> set) throws IllegalArgumentException;

  /** Intersection of this and the given set.
   * 
   * @param mask
   *          The bit mask of another set.
   * @return A new DomainBitSet that represents the intersection.
   * @throws MoreThan64ElementsException
   *           If the domain contains more than 64 elements, then long can't be used.
   * @throws IllegalArgumentException
   *           If the domain contains less than 64 elements then some long values are illegal. */
  public DomainBitSet<T> intersect(long mask) throws MoreThan64ElementsException;

  /** The intersection of this set and a set represented by an array (varargs). The name is different
   * so that it is unambiguous.
   * 
   * @see #intersect(Iterable)
   * @param set
   *          A set as an array. Duplicates are ignored. Must not be or contain <code>null</code>.
   * @return The intersection of this and the given set. */
  public default DomainBitSet<T> intersectVarArgs(@SuppressWarnings("unchecked") final T... set) {
    return this.intersect(Arrays.asList(requireNonNull(set)));
  }

  /** Returns <tt>true</tt> if this set contains no elements.
   * 
   * @return <tt>true</tt> if this set contains no elements
   * @see Collection#isEmpty() */
  public default boolean isEmpty() {
    return this.size() == 0;
  }

  /** The relative complement of this set and a set represented by a {@link BigInteger}.
   * 
   * @param set
   *          The other set.
   * @return The relative complement of this and the given set. */
  public DomainBitSet<T> minus(BigInteger mask);

  /** The relative complement of this set and a set represented by a {@link BitSet}.
   * 
   * @param set
   *          The other set.
   * @return The relative complement of this and the given set. */
  public DomainBitSet<T> minus(BitSet set);

  /** The relative complement of this set and another set.
   * 
   * @param set
   *          The other set.
   * @return The relative complement of this and the given set. */
  public DomainBitSet<T> minus(Iterable<T> set);

  /** The relative complement of this set and a set represented by a bit mask.
   * 
   * @param mask
   *          The mask representing the other set.
   * @throws MoreThan64ElementsException
   *           If the domain contains more than 64 elements, then long can't be used.
   * @throws IllegalArgumentException
   *           If the domain contains less than 64 elements then some long values are illegal.
   * @return The relative complement of this and the given set. */
  public DomainBitSet<T> minus(long mask) throws MoreThan64ElementsException;

  /** The relative complement of this set and a set represented by an array (varargs). The name is
   * different so that it is unambiguous.
   * 
   * @see #minus(Iterable)
   * @param set
   *          A set as an array. Duplicates are ignored. Must not be or contain <code>null</code>.
   * @return The relative complement of this and the given set. */
  public default DomainBitSet<T> minusVarArgs(@SuppressWarnings("unchecked") final T... set) {
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
  public default boolean ofEqualDomain(final DomainBitSet<T> set) {
    return this.getDomain().equals(set.getDomain());
  }

  /** Compares the elements, ignoring the domains.
   * <p>
   * This is equal to, but could be a bit faster than <code>this.toSet().equals(set.toSet())</code>.
   * 
   * @param set
   *          The other set.
   * @see Set#equals(Object)
   * @return <code>true</code> if both are contain the same elements. */
  public default boolean ofEqualElements(final DomainBitSet<T> set) {
    return this.toSet().equals(set.toSet());
  }

  /** The number of elements in this set.
   * 
   * @see Collection#size()
   * @return The number of elements in this set. */
  public int size();

  /** A representation of the elements in this set as a {@link BigInteger}.
   * 
   * @return The bit mask as a {@link BigInteger}. */
  public BigInteger toBigInteger();

  /** A representation of the elements in this set as a {@link BitSet}.
   * 
   * @return The set as a {@link BitSet}. */
  public BitSet toBitSet();

  /** A representation of the elements in this set as a {@link Long long}.
   * 
   * @throws MoreThan64ElementsException
   *           If the domain contains more than 64 elements.
   * @return The set as a {@link Long long}. */
  public long toLong() throws MoreThan64ElementsException;

  /** A regular set, with no defined domain. Note that the returned set can be compared to other
   * regular sets, which this always returns <code>false</code> for other sets without a domain.
   * 
   * @return A set, without the domain. */
  public Set<T> toSet();

  /** The union of this set and a set represented by a {@link BitSet}.
   * <p>
   * Note: A fast version for BigInteger.ZERO should exist for each implementation!
   * 
   * @param mask
   *          A BitSet representing another set.
   * @return The union of this set and the given set. */
  public DomainBitSet<T> union(final BigInteger mask);

  /** The union of this set and a set represented by a {@link BitSet}.
   * 
   * @param mask
   *          A BitSet representing another set.
   *
   * @return The union of this set and the given set. */
  public DomainBitSet<T> union(BitSet set);

  /** The union of this set and a set represented by an {@link Iterable iterable} collection.
   * 
   * @param mask
   *          An Iterable representing another set.
   *
   * @return The union of this set and the given set. */
  public DomainBitSet<T> union(Iterable<T> set);

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
  public DomainBitSet<T> union(long mask) throws MoreThan64ElementsException;

  /** The union of this set and a set represented by an array (varargs). The name is different so
   * that it is unambiguous.
   * 
   * @see #union(Iterable)
   * @param set
   *          A set as an array. Duplicates are ignored. Must not be or contain <code>null</code>.
   * @return The union of this and the given set. */
  public default DomainBitSet<T> unionVarArgs(@SuppressWarnings("unchecked") final T... set) {
    return this.union(Arrays.asList(requireNonNull(set)));
  }

}
