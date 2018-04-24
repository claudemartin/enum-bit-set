package ch.claude_martin.enumbitset;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

import java.math.BigInteger;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import ch.claude_martin.enumbitset.annotations.CheckReturnValue;
import ch.claude_martin.enumbitset.annotations.DefaultAnnotationForParameters;
import ch.claude_martin.enumbitset.annotations.NonNull;
import ch.claude_martin.enumbitset.annotations.Nonnegative;
import ch.claude_martin.enumbitset.annotations.Nullable;

/** This data structure allows managing enum constants in a mutable set with methods similar to
 * EnumSet and BitSet. This holds a regular EnumSet, but adds more functions to use it as a BitSet.
 * Note that the implementation of EnumSet works like a {@link BitSet} but it does not share an
 * interface with the type.
 * 
 * <p>
 * All information is hidden. But this class offers many more methods compared to {@link EnumSet}.
 * Those extra methods are inspired by set theory so that set operations are much simpler to perform
 * with the given methods. These methods are defined in the interface {@link DomainBitSet}.
 * 
 * <p>
 * Methods such as {@link #union(EnumBitSet)}, {@link #toEnumSet()}, and {@link #complement()}
 * return a new and independent set. This allows a functional style of programming.
 * 
 * However, this set is mutable. It can be altered using the interface of {@link Collection} (
 * {@link #add(Enum)}, {@link #remove(Object)} etc.). This allows the classic imperative style of
 * programming.
 * 
 * <p>
 * Sadly both BitSet and EnumSet are not implementations of some interface. Therefore this class can
 * not share the interface of these other classes. Most methods are implemented here so this is in
 * fact compatible in most cases.
 * 
 * <p>
 * This set is a {@link DomainBitSet} with a domain made of all enum elements of a given enum type.
 * 
 * <p>
 * This set is not thread-safe. You can use {@link Collections#unmodifiableSet(Set)}, but will lose
 * all methods not declared in {@link java.util.Set}.
 * 
 * <p>
 * See the <a href="./package-info.html">package-info</a> for naming conventions.
 * 
 * @author <a href="http://claude-martin.ch/enumbitset/">Copyright &copy; 2014 Claude Martin</a>
 * 
 * @param <E>
 *          Enum type that implements <code>{@link EnumBitSetHelper}&lt;E&gt; </code>. */
@DefaultAnnotationForParameters({ NonNull.class })
public final class EnumBitSet<E extends Enum<E> & EnumBitSetHelper<E>> implements DomainBitSet<E>,
    Collection<E> {
  private static final long serialVersionUID = -7833695756979160691L;

  /** Returns the actual enum type. If the class is the anonymous type of an enum constant that
   * extends its own type then the supertype is returned.
   * 
   * @param type
   *          Type of some enum
   * @return The actual type
   * @see Enum#getDeclaringClass() */
  @SuppressWarnings("unchecked")
  private static <X extends Enum<X> & EnumBitSetHelper<X>> Class<X> getActualEnumType(
      final Class<X> type) {
    final var zuper = type.getSuperclass();
    if (zuper == Enum.class)
      return type;
    return (Class<X>) zuper;
  }

  /** Creates an EnumBitSet containing all of the elements in the specified element type.
   * 
   * @see #of(Enum, Enum...)
   * @see #noneOf(Class)
   * @param <X>
   *          The enum type.
   * @param type
   *          Enum type.
   * @return EnumBitSet containing all elements. */
  public static <X extends Enum<X> & EnumBitSetHelper<X>> EnumBitSet<X> allOf(final Class<X> type) {
    return new EnumBitSet<>(type, EnumSet.allOf(getActualEnumType(type)));
  }

  /** Convert EnumSet to BitInteger.
   * 
   * @param <X>
   *          Enum type of the elements.
   * @param set
   *          A set of enum constants.
   * @return A BigInteger that represents the given set as a bit mask. */
  @Nonnegative
  public static <X extends Enum<X> & EnumBitSetHelper<X>> BigInteger asBigInteger(
      final EnumSet<X> set) {
    return BitSetUtilities.asBigInteger(asBitSet(set));
  }

  /** Convert VarArg/Array of enums to BitInteger.
   * 
   * @param <X>
   *          Enum type of the elements.
   * @param set
   *          A set of enum constants.
   * @return A BigInteger that represents the given set as a bit mask. */
  @SafeVarargs
  @Nonnegative
  public static <X extends Enum<X> & EnumBitSetHelper<X>> BigInteger asBigInteger(final X... set) {
    return BitSetUtilities.asBigInteger(asBitSet(set));
  }

  /** Creates a BitSet of a given set of enums.
   * 
   * @param <X>
   *          Enum type of the elements.
   * @param set
   *          A set of enum constants.
   * @return New BitSet, equal to the given EnumSet. */
  public static <X extends Enum<X> & EnumBitSetHelper<X>> BitSet asBitSet(final EnumSet<X> set) {
    final var result = new BitSet(64);
    for (final X e : requireNonNull(set, "set"))
      result.set(e.ordinal());
    return result;
  }

  /** Creates a BitSet of a given set of enums.
   * 
   * @param <X>
   *          Enum type of the elements.
   * @param set
   *          A set of enum constants.
   * @return A BitSet that represents the given set. */
  @SafeVarargs
  public static <X extends Enum<X> & EnumBitSetHelper<X>> BitSet asBitSet(final X... set) {
    final var result = new BitSet();
    for (final X e : requireNonNull(set, "set"))
      result.set(e.ordinal());
    return result;
  }

  /** Creates set of enums from a bit set.
   * 
   * @param <X>
   *          The class of the parameter elements and of the set
   * @param mask
   *          A bit mask, must be positive.
   * @param type
   *          The class of the parameter elements and of the set.
   * @return New EnumBitSet, equal to the given bit mask. */
  @NonNull
  public static <X extends Enum<X> & EnumBitSetHelper<X>> EnumBitSet<X> asEnumBitSet(
      @Nonnegative final BigInteger mask, final Class<X> type) {
    if (requireNonNull(mask, "mask").signum() == -1)
      throw new IllegalArgumentException("The mask must not be negative!");
    return new EnumBitSet<>(type, asEnumSet(mask, type));
  }

  /** Creates set of enums from a bit set.
   * 
   * @param <X>
   *          The class of the parameter elements and of the set
   * @param set
   *          A BitSet.
   * @param type
   *          The class of the parameter elements and of the set.
   * @return New EnumBitSet, equal to the BitSet. */
  public static <X extends Enum<X> & EnumBitSetHelper<X>> EnumBitSet<X> asEnumBitSet(
      final BitSet set, final Class<X> type) {
    return new EnumBitSet<>(type, asEnumSet(set, type));
  }

  /** Creates a new EnumBitSet from a given Collection. The Collection must not contain
   * <code>null</code>.
   * 
   * @param <X>
   *          The class of the parameter elements and of the set
   * @param collection
   *          A Collection of enum elements, not null and not containing null.
   * @param type
   *          The class of the parameter elements and of the set.
   * @return New EnumBitSet, equal to the Collection. */
  public static <X extends Enum<X> & EnumBitSetHelper<X>> EnumBitSet<X> asEnumBitSet(
      final Collection<X> collection, final Class<X> type) {
    requireNonNull(collection, "collection");
    final var result = noneOf(type);
    result.addAll(collection);
    return result;
  }

  /** Creates a new EnumBitSet from a given BitSet.
   * 
   * @param <X>
   *          The class of the parameter elements and of the set
   * @param set
   *          An EnumSet.
   * @param type
   *          The class of the parameter elements and of the set.
   * @return New EnumBitSet, equal to the EnumSet. */
  public static <X extends Enum<X> & EnumBitSetHelper<X>> EnumBitSet<X> asEnumBitSet(
      final EnumSet<X> set, final Class<X> type) {
    return new EnumBitSet<>(type, requireNonNull(set, "set").clone());
  }

  /** Creates set of enums from a long.
   * 
   * @param <X>
   *          The class of the parameter elements and of the set
   * @param mask
   *          A bit mask.
   * @param type
   *          The class of the parameter elements and of the set.
   * @return New EnumBitSet, equal to the given bit mask. */
  public static <X extends Enum<X> & EnumBitSetHelper<X>> EnumBitSet<X> asEnumBitSet(
      final long mask, final Class<X> type) throws MoreThan64ElementsException {
    return new EnumBitSet<>(type, asEnumSet(mask, type));
  }

  /** Creates set of enums from a bit set.
   * 
   * @param <X>
   *          The class of the parameter elements and of the set
   * @param mask
   *          A bit mask, must be positive.
   * @param type
   *          The class of the parameter elements and of the set
   * @return New EnumSet, equal to the given bit mask. */
  @NonNull
  public static <X extends Enum<X> & EnumBitSetHelper<X>> EnumSet<X> asEnumSet(
      @Nonnegative final BigInteger mask, final Class<X> type) {
    if (requireNonNull(mask, "mask").signum() == -1)
      throw new IllegalArgumentException("The mask must not be negative!");
    final var result = EnumSet.allOf(requireNonNull(type, "type"));
    result.removeIf(e -> e.intersect(mask).equals(BigInteger.ZERO));
    return result;
  }

  /** Creates set of enums from a BitSet.
   * 
   * @param <X>
   *          The class of the parameter elements and of the set
   * @param bitset
   *          A BitSet.
   * @param type
   *          The class of the parameter elements and of the set.
   * @return New EnumSet, equal to the given BitSet. */
  @NonNull
  public static <X extends Enum<X> & EnumBitSetHelper<X>> EnumSet<X> asEnumSet(final BitSet bitset,
      final Class<X> type) {
    requireNonNull(bitset);
    final var result = EnumSet.allOf(requireNonNull(type, "type"));
    result.removeIf(e -> !bitset.get(e.ordinal()));
    return result;
  }

  /** Creates set of enums from a 64 bit bit set.
   * 
   * @param <X>
   *          The enum type of the set.
   * @param mask
   *          The bit mask.
   * @param type
   *          The enum type of the set.
   * @throws MoreThan64ElementsException
   *           This fails if the enum type contains more than 64 elements.
   * @return New EnumSet with all elements of the given bit mask. */
  public static <X extends Enum<X> & EnumBitSetHelper<X>> EnumSet<X> asEnumSet(final long mask,
      final Class<X> type) throws MoreThan64ElementsException {
    final var result = EnumSet.allOf(requireNonNull(type, "type"));
    result.removeIf(e -> (e.bitmask64() & mask) == 0);
    return result;
  }

  /** Creates set of enums from at least one element.
   * 
   * It is recommended to use {@link EnumSet#of(Enum, Enum...)} directly!
   * 
   * @param <X>
   *          The class of the parameter elements and of the set
   * @param first
   *          an element that the set is to contain initially
   * @param rest
   *          the remaining elements the set is to contain initially
   * @throws NullPointerException
   *           if any of the specified elements are null, or if <tt>rest</tt> is null
   * @return an EnumSet initially containing the specified elements. This is equal to:
   *         <code>EnumSet.of(first, rest)</code>. */
  @SafeVarargs
  @NonNull
  public static <X extends Enum<X> & EnumBitSetHelper<X>> EnumSet<X> asEnumSet(final X first,
      final X... rest) {
    return EnumSet.of(first, rest);
  }

  /** Creates a 64 bit bitmask of a given set of enums.
   * 
   * @param <X>
   *          Enum type of the elements.
   * @param set
   *          An EnumSet af an enum type with up to 64 constants.
   * @throws MoreThan64ElementsException
   *           This fails if any element in the set has a higher index than 63.
   * @return A long value that represents the given set as a bit mask. */
  public static <X extends Enum<X> & EnumBitSetHelper<X>> long asLong(final EnumSet<X> set)
      throws MoreThan64ElementsException {
    long result = 0L;
    for (final X x : requireNonNull(set, "set"))
      result |= x.bitmask64();
    return result;
  }

  /** Creates a 64 bit bit set of a given set of enums.
   * 
   * @param <X>
   *          Enum type of the elements.
   * @param set
   *          An EnumSet af an enum type with up to 64 constants.
   * @throws MoreThan64ElementsException
   *           This fails if any element in the set has a higher index than 63.
   * @return A long value that represents the given set as a bit mask. */
  @SafeVarargs
  public static <X extends Enum<X> & EnumBitSetHelper<X>> long asLong(final X... set)
      throws MoreThan64ElementsException {
    long result = 0;
    for (final X e : requireNonNull(set, "set"))
      result |= e.bitmask64();// bitmask64() checks index!
    return result;
  }

  /** Returns a new EnumBitSet containing just one enum value.<br>
   * Note: <code>EnumBitSet.just(X)</code> is equal to <code>X.asEnumBitSet()</code>
   * 
   * @param <X>
   *          The enum type of the value.
   * @param value
   *          The single value that will be contained in the result.
   * @return New EnumBitSet containing nothing but <code>value</code>. */
  @NonNull
  public static <X extends Enum<X> & EnumBitSetHelper<X>> EnumBitSet<X> just(final X value) {
    final var result = noneOf(requireNonNull(value, "value").getDeclaringClass());
    result.add(value);
    return result;
  }

  /** Creates an EnumBitSet containing none of the elements in the specified element type.
   * 
   * @see #of(Enum, Enum...)
   * @see #allOf(Class)
   * @param <X>
   *          The enum type.
   * @param type
   *          Enum type.
   * @return EnumBitSet containing no elements. */
  @NonNull
  public static <X extends Enum<X> & EnumBitSetHelper<X>> EnumBitSet<X> noneOf(final Class<X> type) {
    return new EnumBitSet<>(type);
  }

  /** Creates a new EnumBitSet containing at least one value.
   * 
   * @param <X>
   *          The enum type of all elements.
   * @param first
   *          The first element (must not be null).
   * @param more
   *          More elements to add.
   * @return New EnumBitSet containing all given elements.
   * @see #noneOf(Class) */
  @SafeVarargs
  @NonNull
  public static <X extends Enum<X> & EnumBitSetHelper<X>> EnumBitSet<X> of(final X first,
      final X... more) {
    requireNonNull(first, "first");
    requireNonNull(more, "more");

    final var result = noneOf(first.getDeclaringClass());
    result.add(first);
    for (final X x : more)
      result.add(x);
    return result;
  }

  /** Creates an enum set initially containing all of the elements in the range defined by the two
   * specified endpoints. The returned set will contain the endpoints themselves, which may be
   * identical but must not be out of order.
   * 
   * @param <X>
   *          The class of the parameter elements and of the set
   * @param from
   *          the first element in the range
   * @param to
   *          the last element in the range
   * @throws NullPointerException
   *           if {@code from} or {@code to} are null
   * @throws IllegalArgumentException
   *           if {@code from.compareTo(to) > 0}
   * @return an enum set initially containing all of the elements in the range defined by the two
   *         specified endpoints */
  public static <X extends Enum<X> & EnumBitSetHelper<X>> EnumBitSet<X> range(//
      final X from, final X to) {
    return new EnumBitSet<>(//
        requireNonNull(from, "from").getDeclaringClass(), //
        EnumSet.range(from, requireNonNull(to, "to")));
  }

  @NonNull
  private final EnumSet<E>   bitset;

  @NonNull
  private final Class<E>     enumType;

  private int                enumTypeSize = -1;

  @Nullable
  private volatile Domain<E> domain       = null;

  private EnumBitSet(final Class<E> type) {
    this(type, EnumSet.noneOf(getActualEnumType(type)));
  }

  EnumBitSet(final Class<E> type, final EnumSet<E> set) {
    this.enumType = getActualEnumType(requireNonNull(type, "type"));
    this.bitset = requireNonNull(set, "set");
  }

  /** {@inheritDoc} */
  @Override
  public boolean add(final E e) {
    return this.bitset.add(e);
  }

  /** {@inheritDoc} */
  @Override
  public boolean addAll(final Collection<? extends E> c) {
    return this.bitset.addAll(c);
  }

  /** {@inheritDoc} */
  @Override
  public void clear() {
    this.bitset.clear();
  }

  /** Returns a copy of this set. */
  @Override
  @NonNull
  public EnumBitSet<E> clone() {
    return new EnumBitSet<>(this.enumType, this.bitset.clone());
  }

  /** Creates a new EnumBitSet with the same element type as this, initially containing all the
   * elements of this type that are not contained in this set.
   * 
   * @return The complement of this EnumBitSet. */
  @Override
  public EnumBitSet<E> complement() {
    return new EnumBitSet<>(this.enumType, EnumSet.complementOf(this.bitset));
  }

  /** {@inheritDoc} */
  @Override
  public boolean contains(final Object o) {
    return this.bitset.contains(requireNonNull(o, "o"));
  }

  /** {@inheritDoc} */
  @Override
  public boolean containsAll(final Collection<?> c) {
    return this.bitset.containsAll(c);
  }

  /** The Cartesian product with another EnumBitSet. This overload only differs in that its generic
   * return type uses {@link Enum} and {@link EnumBitSetHelper} instead of Object.
   * 
   * @param <Y>
   *          The type of the elements in the given set.
   * @param set
   *          Another set.
   * @see DomainBitSet#cross(DomainBitSet)
   * @see DomainBitSet#cross(DomainBitSet, BiConsumer)
   * @see BitSetUtilities#cross(DomainBitSet, DomainBitSet, Class)
   * @return a {@link Set} containing all {@link Pair pairs}. */
  @SuppressWarnings("unchecked")
  @NonNull
  public <Y extends Enum<Y> & EnumBitSetHelper<Y>> Set<Pair<EnumBitSetHelper<?>, E, Y>> cross(
      final EnumBitSet<Y> set) {
    requireNonNull(set, "set");

    // This cast is ok because we know that all EnumBitSets always use types that implement
    // EnumBitSetHelper. The default implementation can be used, because the type is only generic.
    // This can be asserted by this:
    assert this.isEmpty()
        || EnumBitSetHelper.class.isAssignableFrom(this.iterator().next().getClass());
    assert set.isEmpty()
        || EnumBitSetHelper.class.isAssignableFrom(set.iterator().next().getClass());
    return (Set<Pair<EnumBitSetHelper<?>, E, Y>>) (Object) DomainBitSet.super.cross(set);
  }

  /** Is the given enum constant an element of this set's domain?
   * 
   * @return <code>object.getDeclaringClass() == this.enumType</code> */
  @Override
  public boolean domainContains(final E object) {
    requireNonNull(object, "object");
    return object.getDeclaringClass() == this.enumType;
  }

  /** Compares the specified object with this domain bit set for equality. Returns <tt>true</tt>, iff
   * the given object is also a {@link DomainBitSet}, the two sets have the same domain, and every
   * member of the given set is contained in this set.
   * 
   * @see DomainBitSet#equals(Object)
   * @see #ofEqualElements(DomainBitSet)
   * @return True, if this also a {@link DomainBitSet}, with the same domain and elements. */
  @SuppressWarnings("unchecked")
  @Override
  public boolean equals(final Object other) {
    if (this == other)
      return true;
    if (other instanceof EnumBitSet)
      return this.enumType == ((EnumBitSet<E>) other).enumType
          && this.bitset.equals(((EnumBitSet<E>) other).bitset);
    if (other instanceof DomainBitSet)
      return this.ofEqualDomain((DomainBitSet<E>) other)
          && this.ofEqualElements((DomainBitSet<E>) other);
    return false;
  }

  @Override
  public void forEach(final Consumer<? super E> action) {
    requireNonNull(action, "action");
    this.bitset.forEach(action);
  }

  /** {@inheritDoc}
   * 
   * @see #getElement(int) */
  @Override
  public boolean getBit(final int bitIndex) throws IndexOutOfBoundsException {
    final int size = this.getEnumTypeSize();
    if (bitIndex < 0 || bitIndex >= size)
      throw new IndexOutOfBoundsException();
    if (size <= 64)
      return (this.toLong() >>> bitIndex) % 2L == 1L;
    // Using getDomain should be faster as the domain is cached.
    final var result = this.bitset.contains(this.getDomain().get(bitIndex));
    assert result == this.toBitSet().get(bitIndex);
    return result;
  }

  /** The Domain containing all elements of the enum type.
   * 
   * @see #getEnumTypeSize()
   * @return <code>Domain</code> with all enum elements. */
  @Override
  public Domain<E> getDomain() {
    if (null == this.domain)
      return this.domain = EnumDomain.of(this.enumType);
    return this.domain;
  }

  /** The declaring enum type that defines the available enum elements.
   * <p>
   * Note that an enum constant could use an anonymous inner type, which extends the declaring type.
   * 
   * @return The enum type of all elements in this set. */
  @NonNull
  public Class<E> getEnumType() {
    return this.enumType;
  }

  /** Amount of enum elements. This is relevant to know how large the bit field must be to hold a bit
   * set of this type.
   * <p>
   * This is equal to <code>{@link #getDomain()}.size()</code>, but possibly faster.
   * 
   * @return Number of constants of the enum type. */
  @Nonnegative
  public int getEnumTypeSize() {
    if (this.enumTypeSize == -1)
      this.enumTypeSize = this.getDomain().size();
    return this.enumTypeSize;
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    return this.getDomain().hashCode() ^ this.bitset.hashCode();
  }

  /** Returns a new EnumBitSet containing all elements that are in <code>this</code> and the given
   * <code>mask</code>.
   * 
   * @see #complement()
   * @param mask
   *          Another set, represented by a bit mask, must be positive.
   * @return <code> this &#x2229; set</code> */
  @Override
  public EnumBitSet<E> intersect(final BigInteger mask) {
    if (requireNonNull(mask, "mask").signum() == -1)
      throw new IllegalArgumentException("The mask must not be negative!");
    final var clone = this.bitset.clone();
    clone.removeIf(e -> !mask.testBit(e.ordinal()));
    return new EnumBitSet<>(this.enumType, clone);
  }

  /** Returns a new EnumBitSet containing all elements that are in <code>this</code> and the given
   * <code>set</code>.
   * 
   * @see #complement()
   * @param set
   *          Another set.
   * @return <code> this &#x2229; set</code> */
  @Override
  public EnumBitSet<E> intersect(final BitSet set) {
    requireNonNull(set, "set");
    final var clone = this.bitset.clone();
    clone.removeIf(e -> !set.get(e.ordinal()));
    return new EnumBitSet<>(this.enumType, clone);
  }

  /** Returns a new EnumBitSet containing all elements that are in <code>this</code> and the given
   * <code>set</code>.
   * 
   * @param set
   *          Another set.
   * @see #complement()
   * @return <code> this &#x2229; set</code> */
  public EnumBitSet<E> intersect(final EnumBitSet<E> set) {
    requireNonNull(set, "set");
    return this.intersect(set.toEnumSet());
  }

  /** Returns a new EnumBitSet containing all elements that are in <code>this</code> and the given
   * <code>set</code>.
   * 
   * @see #complement()
   * @param set
   *          Another set.
   * @return <code> this &#x2229; set</code> */
  public EnumBitSet<E> intersect(final EnumSet<E> set) {
    requireNonNull(set, "set");

    final var clone = this.bitset.clone();
    clone.retainAll(set);
    return new EnumBitSet<>(this.enumType, clone);
  }

  /** Returns a new EnumBitSet containing all elements that are in <code>this</code> and the given
   * <code>set</code>.
   * 
   * @see #complement()
   * @param set
   *          Another set.
   * @return <code> this &#x2229; set</code> */
  @Override
  public EnumBitSet<E> intersect(final Iterable<E> set) {
    if (requireNonNull(set, "set") instanceof EnumBitSet)
      return this.intersect(((EnumBitSet<E>) set).bitset);
    final var result = EnumSet.noneOf(this.enumType);
    for (final E e : set)
      if (this.contains(e))
        result.add(e);
    return new EnumBitSet<>(this.enumType, result);
  }

  /** Returns a new EnumBitSet containing all elements that are in <code>this</code> and the given
   * <code>mask</code>.
   * 
   * @see #complement()
   * @param mask
   *          Another set, represented by a bit mask.
   * @throws MoreThan64ElementsException
   *           This fails if any element in the set has a higher index than 63.
   * @return <code> this &#x2229; set</code> */
  @Override
  public EnumBitSet<E> intersect(final long mask) throws MoreThan64ElementsException {
    final var clone = this.bitset.clone();
    clone.removeIf(e -> (e.bitmask64() & mask) == 0);
    return new EnumBitSet<>(this.enumType, clone);
  }

  /** Returns a new EnumBitSet containing all elements that are in <code>this</code> and the given
   * <code>set</code>.
   * 
   * @see #complement()
   * @param set
   *          Another set.
   * @return <code> this &#x2229; set</code> */
  @Override
  public EnumBitSet<E> intersectVarArgs(@SuppressWarnings("unchecked") final E... set) {
    requireNonNull(set, "set");

    final var clone = this.bitset.clone();
    clone.retainAll(asList(set));
    return new EnumBitSet<>(this.enumType, clone);
  }

  /** {@inheritDoc} */
  @Override
  public boolean isEmpty() {
    return this.bitset.isEmpty();
  }

  /** {@inheritDoc} */
  @Override
  public Iterator<E> iterator() {
    return this.bitset.iterator();
  }

  /** Returns a new set with elements of a given enum type, containing all elements of the other enum
   * type.
   * <p>
   * Mapping is done by the ordinal of each enum constant. Therefore the given enum type must not
   * contain less constants than the enum type of this set.
   * 
   * @see #map(Domain, Function)
   * @see #map(Class, Function)
   * @see Stream#map(Function)
   * @param newEnumType
   *          The new enum type.
   * @param <S>
   *          Type of given enum domain.
   * @throws IllegalArgumentException
   *           if the given enum type contains less constants.
   * @return new set, using the given enum type. */
  @SuppressWarnings("unchecked")
  @NonNull
  @CheckReturnValue
  public <S extends Enum<S> & EnumBitSetHelper<S>> EnumBitSet<S> map(final Class<S> newEnumType) {
    requireNonNull(newEnumType, "newEnumType");
    if (this.enumType == newEnumType)
      return (EnumBitSet<S>) this.clone();
    final var d = EnumDomain.of(newEnumType);
    if (d.size() < this.getDomain().size())
      throw new IllegalArgumentException("The given enum type contains less elements.");
    return this.map(newEnumType, e -> d.get(e.ordinal()));
  }

  /** Returns a new set with elements of a given enum type, containing all mapped elements.
   * <p>
   * This is a convenience method. The same can be done with: <code>this.stream().map(mapper)</code>
   * 
   * @see #map(Domain, Function)
   * @param newEnumType
   *          The new enum type.
   * @param mapper
   *          function to map from E to S.
   * @param <S>
   *          Type of given enum domain.
   * @see #map(Domain, Function)
   * @see Stream#map(Function)
   * @return new set, using the given enum type. */
  @NonNull
  @CheckReturnValue
  public <S extends Enum<S> & EnumBitSetHelper<S>> EnumBitSet<S> map(final Class<S> newEnumType,
      final Function<E, S> mapper) {
    requireNonNull(newEnumType, "newEnumType");
    requireNonNull(mapper, "mapper");
    final EnumBitSet<S> result = noneOf(newEnumType);
    this.forEach(e -> result.add(mapper.apply(e)));
    return result;
  }

  /** {@inheritDoc}
   * <p>
   * This returns an EnumBitSet if the given Domain originates from an EnumBitSet.
   * 
   * @see #map(Class, Function) */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public <S> DomainBitSet<S> map(final Domain<S> newDomain, final Function<E, S> mapper) {
    requireNonNull(newDomain, "newDomain");
    requireNonNull(mapper, "mapper");
    if (newDomain instanceof EnumDomain) {
      // fact: S extends Enum & EnumBitSetHelper
      final var result = new EnumBitSet(((EnumDomain) newDomain).getEnumType());
      final var bs = result.bitset;
      this.forEach(e -> bs.add(mapper.apply(e)));
      return result;
    }
    return DomainBitSet.super.map(newDomain, mapper);
  }

  /** Returns a new EnumBitSet containing all elements that are in <code>this</code>, but not in the
   * given <code>mask</code>.
   * 
   * @see #removeAll(Collection)
   * @param mask
   *          Another set, represented by a bit mask, must be positive.
   * @return <code>this &#x2216; mask</code> */
  @Override
  public EnumBitSet<E> minus(final BigInteger mask) {
    requireNonNull(mask, "mask");

    // A\B = A & ~B
    // So one might think that this works:
    // this.toBigInteger().and(mask.not());
    // It doesn't, because the BigInteger doesn't know the size of the enum
    // type (the "domain").

    // Solution using BigInteger:
    final var self = this.toBigInteger();
    final var one = BigInteger.ONE;
    final var all = one.shiftLeft(this.getEnumTypeSize()).subtract(one);
    final var notb = mask.xor(all);
    return asEnumBitSet(self.and(notb), this.enumType);

    // Solution using EnumSet:
    // final EnumBitSet<E> result = this.clone();
    // if (BigInteger.ZERO.equals(mask))
    // return result;
    // result.removeAll(asEnumSet(mask, this.enumType));
    // return result;
  }

  /** Returns a new EnumBitSet containing all elements that are in <code>this</code>, but not in the
   * given <code>bit set</code>.
   * 
   * @see #removeAll(Collection)
   * @param set
   *          Another set.
   * @return <code>this &#x2216; set</code> */
  @Override
  public EnumBitSet<E> minus(final BitSet set) {
    requireNonNull(set, "set");
    final var result = this.clone();
    if (set.isEmpty())
      return result;
    result.removeAll(asEnumSet(set, this.enumType));
    return result;
  }

  /** Returns a new EnumBitSet containing all elements that are in <code>this</code>, but not in the
   * given <code>set</code>.
   * 
   * @see #removeAll(Collection)
   * @param set
   *          Another set.
   * @return <code>this &#x2216; set</code> */
  @NonNull
  public EnumBitSet<E> minus(final EnumBitSet<E> set) {
    requireNonNull(set, "set");
    final var result = this.clone();
    if (set.isEmpty())
      return result;
    result.removeAll(set);
    return result;
  }

  /** Returns a new EnumBitSet containing all elements that are in <code>this</code>, but not in the
   * given <code>set</code>.
   * 
   * @see #removeAll(Collection)
   * @param set
   *          Another set.
   * @return <code>this &#x2216; set</code> */
  @NonNull
  public EnumBitSet<E> minus(final EnumSet<E> set) {
    requireNonNull(set, "set");
    final var result = this.clone();
    if (set.isEmpty())
      return result;
    result.removeAll(set);
    return result;
  }

  /** Returns a new EnumBitSet containing all elements that are in <code>this</code>, but not in the
   * given <code>set</code>.
   * 
   * @see #removeAll(Collection)
   * @param set
   *          Another set.
   * @return <code>this &#x2216; mask</code> */
  @Override
  public EnumBitSet<E> minus(final Iterable<E> set) {
    if (requireNonNull(set, "set") instanceof EnumBitSet)
      return this.intersect(((EnumBitSet<E>) set).bitset);
    final var result = this.clone();
    set.forEach(result.bitset::remove);
    return result;
  }

  /** Returns a new EnumBitSet containing all elements that are in <code>this</code>, but not in the
   * given <code>mask</code>.
   * 
   * @see #removeAll(Collection)
   * @param mask
   *          Another set, represented by a bit mask.
   * @throws MoreThan64ElementsException
   *           This fails if any element in this set has a higher index than 63.
   * @return <code>this &#x2216; mask</code> */
  @Override
  public EnumBitSet<E> minus(final long mask) throws MoreThan64ElementsException {
    if (mask == 0)
      return this.clone();
    return asEnumBitSet(this.toLong() & ~mask, this.enumType);
  }

  /** Returns a new EnumBitSet containing all elements that are in <code>this</code>, but not in the
   * given <code>set</code>.
   * 
   * @see #removeAll(Collection)
   * @param set
   *          Another set.
   * @return <code>this &#x2216; set</code> */
  @Override
  @SuppressWarnings("unchecked")
  public EnumBitSet<E> minusVarArgs(final E... set) {
    requireNonNull(set, "set");

    final var result = this.clone();
    if (set.length == 0)
      return result;
    result.removeAll(asList(set));
    return result;
  }

  @Override
  public boolean ofEqualDomain(final DomainBitSet<E> set) {
    if (requireNonNull(set, "set") instanceof EnumBitSet)
      return this.enumType == ((EnumBitSet<?>) set).enumType;
    // unlikely but possible:
    return this.getDomain().equals(set.getDomain());
  }

  @Override
  public Stream<E> parallelStream() {
    return this.bitset.parallelStream();
  }

  @Override
  @SuppressWarnings("unchecked")
  public Iterable<EnumBitSet<E>> powerset() throws MoreThan64ElementsException {
    return (Iterable<EnumBitSet<E>>) DomainBitSet.super.powerset();
  }

  /** {@inheritDoc} */
  @Override
  public boolean remove(final Object o) {
    return this.bitset.remove(o);
  }

  /** {@inheritDoc}
   * 
   * @see #minusVarArgs(Enum...)
   * @see #minus(EnumBitSet)
   * @see #minus(EnumSet)
   * @see #minus(BigInteger) */
  @Override
  public boolean removeAll(final Collection<?> c) {
    return this.bitset.removeAll(c);
  }

  /** {@inheritDoc}
   * 
   * @see #intersect(BigInteger)
   * @see #intersect(BitSet)
   * @see #intersectVarArgs(Enum...)
   * @see #intersect(EnumBitSet)
   * @see #intersect(EnumSet)
   * @see #intersect(long) */
  @Override
  public boolean retainAll(final Collection<?> c) {
    return this.bitset.retainAll(c);
  }

  /** {@inheritDoc}
   * <p>
   * Not to be confused with {@link #getEnumTypeSize()}. */
  @Override
  public int size() {
    return this.bitset.size();
  }

  /** {@inheritDoc} */
  @Override
  public Spliterator<E> spliterator() {
    return this.bitset.spliterator();
  }

  @Override
  public Stream<E> stream() {
    return this.bitset.stream();
  }

  /** {@inheritDoc} */
  @Override
  public Object[] toArray() {
    return this.bitset.toArray();
  }

  /** {@inheritDoc} */
  @Override
  public <T> T[] toArray(final T[] a) {
    return this.bitset.toArray(a);
  }

  /** Returns a BigInteger that represents this set.
   * 
   * @see #toBinaryString()
   * @return A representation of this {@link EnumBitSet} as a {@link BigInteger} . */
  @Override
  public BigInteger toBigInteger() {
    if (this.getEnumTypeSize() <= 64)
      return BitSetUtilities.asBigInteger(this.toLong());
    else
      return BitSetUtilities.asBigInteger(this.toBitSet());
  }

  /** Returns a new BitSet that represents this set.
   * 
   * @return A representation of this {@link EnumBitSet} as a {@link BitSet}; */
  @Override
  public BitSet toBitSet() {
    final var result = new BitSet(this.getEnumTypeSize());
    for (final E e : this.bitset)
      result.set(e.ordinal());
    return result;
  }

  /** Copy of the underlying EnumSet.
   * 
   * @return <code>bitset.clone()</code> */
  @NonNull
  public EnumSet<E> toEnumSet() {
    return this.bitset.clone();
  }

  /** Returns a long value that represents this set.
   * 
   * @throws MoreThan64ElementsException
   *           This fails if any element in this set has a higher index than 63.
   * @return A representation of this {@link EnumBitSet} as a {@link Long long}. */
  @Override
  public long toLong() throws MoreThan64ElementsException {
    long result = 0L;
    for (final E e : this.bitset)
      result |= e.bitmask64();// bitmask64() checks index!
    return result;
  }

  /** Copy of the underlying EnumSet.
   * 
   * @return <code>bitset.clone()</code> */
  @Override
  public Set<E> toSet() {
    return this.bitset.clone();
  }

  /** Returns a string representation of this set.
   * 
   * @see EnumSet#toString() */
  @Override
  public String toString() {
    return this.bitset.toString();
  }

  /** Returns a new EnumBitSet containing all elements that are in <code>this</code> or the given
   * <code>mask</code>.
   * 
   * @param mask
   *          Bit mask of another set, must be positive.
   * @return <code> this &#x222a; set</code>
   * @throws IllegalArgumentException
   *           The bit mask must not have any bits set that are not mapped to an enum constant. */
  @Override
  public EnumBitSet<E> union(@Nonnegative final BigInteger mask) {
    if (requireNonNull(mask, "mask").signum() == -1)
      throw new IllegalArgumentException("The mask must not be negative!");
    final var result = this.clone();
    if (mask.signum() == 0)
      return result;
    final var dom = this.getDomain();
    // Test that mask does not contain more than domain allows:
    if (0 != mask.shiftRight(dom.size()).signum())
      throw new IllegalArgumentException("The given mask is not applicable to this set.");
    for (int i = 0; i < dom.size(); i++)
      if (mask.testBit(i))
        result.add(dom.get(i));
    assert this.union(asEnumSet(mask, this.enumType)).equals(result) : "Error in union(BigInteger)";
    return result;
  }

  /** Returns a new EnumBitSet containing all elements that are in <code>this</code> or the given
   * <code>set</code>.
   * 
   * @param set
   *          Another set.
   * @return <code> this &#x222a; set</code>
   * @throws IllegalArgumentException
   *           The bit set must not have any bits set that are not mapped to an enum constant. */
  @Override
  public EnumBitSet<E> union(final BitSet set) {
    requireNonNull(set, "set");

    // return union(asEnumSet(set, this.enumType));
    final var dom = this.getDomain();
    if (-1 != set.nextSetBit(dom.size()))
      throw new IllegalArgumentException("The given mask is not applicable to this set.");
    final var clone = this.bitset.clone();
    for (int i = set.nextSetBit(0); i >= 0; i = set.nextSetBit(i + 1))
      clone.add(dom.get(i));// The above check should ensure that no IOOBE is thrown here.
    return new EnumBitSet<>(this.enumType, clone);
  }

  /** Returns a new EnumBitSet containing all elements that are in <code>this</code> or the given
   * <code>set</code>.
   * 
   * @param set
   *          Another set.
   * @return <code> this &#x222a; set</code> */
  public EnumBitSet<E> union(final EnumBitSet<E> set) {
    requireNonNull(set, "set");
    return this.union(set.bitset);
  }

  /** Returns a new EnumBitSet containing all elements that are in <code>this</code> or the given
   * <code>set</code>.
   * 
   * @param set
   *          Another set.
   * @return <code> this &#x222a; set</code> */
  public EnumBitSet<E> union(final EnumSet<E> set) {
    requireNonNull(set, "set");

    final var clone = this.bitset.clone();
    clone.addAll(set);
    return new EnumBitSet<>(this.enumType, clone);
  }

  @Override
  public EnumBitSet<E> union(final Iterable<E> set) {
    if (requireNonNull(set, "set") instanceof EnumBitSet)
      return this.union(((EnumBitSet<E>) set).bitset);
    final var clone = this.bitset.clone();
    set.forEach(clone::add);
    return new EnumBitSet<>(this.enumType, clone);
  }

  /** Returns a new EnumBitSet containing all elements that are in <code>this</code> or the given
   * <code>mask</code>.
   * 
   * @param mask
   *          Bit mask of another set.
   * @throws MoreThan64ElementsException
   *           This fails if any element in this set has a higher index than 63.
   * @return <code> this &#x222a; set</code> */
  @Override
  public EnumBitSet<E> union(final long mask) throws MoreThan64ElementsException {
    return this.union(asEnumSet(mask, this.enumType));
  }

  /** Returns a new EnumBitSet containing all elements that are in <code>this</code> or the given
   * <code>set</code>.
   * 
   * @param set
   *          Another set.
   * @return <code> this &#x222a; set</code> */
  @Override
  public EnumBitSet<E> unionVarArgs(@SuppressWarnings("unchecked") final E... set) {
    requireNonNull(set, "set");

    final var clone = this.bitset.clone();
    clone.addAll(asList(set));
    return new EnumBitSet<>(this.enumType, clone);
  }

  /** {@inheritDoc}
   * <p>
   * As this uses enum types the position is always the {@link Enum#ordinal() ordinal} of the
   * constant. */
  @Override
  public Stream<Pair<Object, Integer, E>> zipWithPosition() {
    return this.stream().map(e -> Pair.of(e.ordinal(), e));
  }

  /** This proxy class is used to serialize EnumBitSet instances. */
  private static class SerializationProxy<E extends Enum<E> & EnumBitSetHelper<E>> implements
      java.io.Serializable {
    private static final long serialVersionUID = 7134313027153728022L;

    private final Class<E>    enumType;
    private final EnumSet<E>  bitset;

    public SerializationProxy(final Class<E> enumType, final EnumSet<E> bitset) {
      this.enumType = enumType;
      this.bitset = bitset;
    }

    private Object readResolve() {
      return new EnumBitSet<>(this.enumType, this.bitset);
    }
  }

  private Object writeReplace() {
    return new SerializationProxy<>(this.enumType, this.bitset);
  }

  @SuppressWarnings({ "static-method", "unused" })
  private void readObject(final java.io.ObjectInputStream stream)
      throws java.io.InvalidObjectException {
    throw new java.io.InvalidObjectException("Proxy required");
  }
}