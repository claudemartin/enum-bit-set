package ch.claude_martin.enumbitset;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;

/**
 * EnumSet and BitSet in one mutable object. This holds a regular EnumSet, but
 * adds more functions to use it as a BitSet. Note that the implementation of
 * EnumSet works like a bit set but the interface doesn't allow to use it as
 * such.
 * 
 * <p>
 * All information is hidden. But this class offers many more methods compared
 * to {@link EnumSet}. Those extra methods are inspired by set theory so that
 * set operations are much simpler to perform with the given methods.
 * 
 * <p>
 * Methods such as {@link #toEnumSet()} and {@link #complement()} return a new
 * and independent set. This allows a functional style of programming.
 * 
 * However, this set is mutable. It can be altered using the interface of
 * {@link Set} ({@link #add(Enum)}, {@link #remove(Object)} etc.). This allows
 * the classic imperative style of programming.
 * 
 * <p>
 * Sadly both BitSet and EnumSet are not implementations of some interface.
 * Therefore this class can not share the interface of these other classes. Most
 * methods are implemented here so this is in fact compatible in most cases.
 * 
 * <p>
 * This set is not thread-safe. You can use
 * {@link Collections#unmodifiableSet(Set)}, but will lose all methods not
 * declared in {@link java.util.Set}.
 * 
 * <p>
 * See the <a href="./package-info.html">package-info</a> for naming
 * conventions.
 * 
 * @author <a href="http://claude-martin.ch/enumbitset/">Copyright &copy; 2014
 *         Claude Martin</a>
 * 
 * @param <E>
 *          Enum type that implements <code>{@link EnumBitSetHelper}&lt;E&gt;
 *          </code>.
 */
public final class EnumBitSet<E extends Enum<E> & EnumBitSetHelper<E>> implements Set<E>, Cloneable {
	/**
	 * An immutable ordered pair. This can be used in a Cartesian product.
	 * 
	 * @param <X>
	 *          The enum type of the first element.
	 * @param <Y>
	 *          The enum type of the second element.
	 */
	public static class Pair<X extends Enum<X> & EnumBitSetHelper<X>, Y extends Enum<Y> & EnumBitSetHelper<Y>> {
		/** The first value of this pair. Not null. */
		public final X first;
		/** The first value of this pair. Not null. */
		public final Y second;

		@SuppressWarnings("hiding")
		public Pair(final X first, final Y second) {
			this.first = requireNonNull(first);
			this.second = requireNonNull(second);
		}

		/** @return <code>this</code> */
		@Override
		public Pair<X, Y> clone() {
			return this;
		}

		/** {@inheritDoc} */
		@Override
		public boolean equals(final Object obj) {
			return obj instanceof Pair //
			    && Objects.equals(this.first, ((Pair<?, ?>) obj).first) //
			    && Objects.equals(this.second, ((Pair<?, ?>) obj).second);
		}

		/** {@inheritDoc} */
		@Override
		public int hashCode() {
			return Objects.hash(this.first, this.second);
		}

		/**
		 * Creates an inverted pair.
		 * <p>
		 * <code>(a, b) &rarr; (b, a)</code>
		 * 
		 * @return <code>new Pair&lt;&gt;(this.second, this.first)</code>
		 */
		public Pair<Y, X> swap() {
			return new Pair<>(this.second, this.first);
		}

		/**
		 * This Pair as an array so that first is on index 0 and second is on index
		 * 1.
		 * 
		 * @param <E>
		 *          Both are enum elements but the type can be different.
		 * @return <code>new Object[] { this.first, this.second };</code>
		 */
		@SuppressWarnings("unchecked")
		public <E extends Enum<?>> E[] toArray() {
			return (E[]) new Enum[] { this.first, this.second };
		}

		/** {@inheritDoc} */
		@Override
		public String toString() {
			return "Pair(" + this.first + ", " + this.second + ")";
		}
	}

	/**
	 * Creates an EnumBitSet containing all of the elements in the specified
	 * element type.
	 * 
	 * @see #of(Enum, Enum...)
	 * @param <X>
	 *          The enum type.
	 * @param type
	 *          Enum type.
	 * @return EnumBitSet containing all elements.
	 */
	public static <X extends Enum<X> & EnumBitSetHelper<X>> EnumBitSet<X> allOf(final Class<X> type) {
		return new EnumBitSet<>(type, EnumSet.allOf(type));
	}

	/**
	 * Creates a BigInteger of a given mask.
	 * 
	 * @param bitset
	 *          A bit set.
	 * @return The bit mask.
	 */
	public static BigInteger asBigInteger(final BitSet bitset) {
		if (requireNonNull(bitset).isEmpty())
			return BigInteger.ZERO;
		return new BigInteger(bitset.toByteArray());
	}

	/**
	 * Convert EnumSet to BitInteger.
	 * 
	 * @param <X>
	 *          Enum type of the elements.
	 * @param set
	 *          A set of enum constants.
	 * @return A BigInteger that represents the given set as a bit mask.
	 */
	public static <X extends Enum<X> & EnumBitSetHelper<X>> BigInteger asBigInteger(final EnumSet<X> set) {
		BigInteger result = BigInteger.ZERO;
		for (final X e : requireNonNull(set))
			result = result.or(e.bitmask());
		return result;
	}

	/**
	 * Convert VarArg/Array of enums to BitInteger.
	 * 
	 * @param <X>
	 *          Enum type of the elements.
	 * @param set
	 *          A set of enum constants.
	 * @return A BigInteger that represents the given set as a bit mask.
	 * */
	@SafeVarargs
	public static <X extends Enum<X> & EnumBitSetHelper<X>> BigInteger asBigInteger(final X... set) {
		BigInteger result = BigInteger.ZERO;
		for (final X e : requireNonNull(set))
			result = result.or(e.bitmask());
		return result;
	}

	/**
	 * Creates a BitSet of a given mask.
	 * 
	 * @param mask
	 *          A bit mask, must be positive.
	 * @return New BitSet, equal to the given bit mask.
	 */
	public static BitSet asBitSet(final BigInteger mask) {
		if (requireNonNull(mask).signum() == -1)
			throw new IllegalArgumentException("The mask must not be negative!");
		return BitSet.valueOf(mask.toByteArray());
	}

	/**
	 * Creates a BitSet of a given set of enums.
	 * 
	 * @param <X>
	 *          Enum type of the elements.
	 * @param set
	 *          A set of enum constants.
	 * @return New BitSet, equal to the given EnumSet.
	 */
	public static <X extends Enum<X> & EnumBitSetHelper<X>> BitSet asBitSet(final EnumSet<X> set) {
		final BitSet result = new BitSet(64);
		for (final X e : requireNonNull(set))
			result.set(e.ordinal());
		return result;
	}

	/**
	 * Creates a BitSet of a given bit mask.
	 * 
	 * <p>
	 * This method can not check if there are more than 64 elements on the enum
	 * type, as the enum type is not known. It simply converts a long to a bitset.
	 * 
	 * @param <X>
	 *          Enum type of the elements.
	 * @param mask
	 *          A bit mask.
	 * @return A BitSet that represents the given set.
	 */
	public static <X extends Enum<X> & EnumBitSetHelper<X>> BitSet asBitSet(final long mask) {
		return BitSet.valueOf(new long[] { mask });
	}

	/**
	 * Creates a BitSet of a given set of enums.
	 * 
	 * @param <X>
	 *          Enum type of the elements.
	 * @param set
	 *          A set of enum constants.
	 * @return A BitSet that represents the given set.
	 * */
	@SafeVarargs
	public static <X extends Enum<X> & EnumBitSetHelper<X>> BitSet asBitSet(final X... set) {
		final BitSet result = new BitSet();
		for (final X e : requireNonNull(set))
			result.set(e.ordinal());
		return result;
	}

	/**
	 * Creates set of enums from a bit set.
	 * 
	 * @param <X>
	 *          The class of the parameter elements and of the set
	 * @param mask
	 *          A bit mask, must be positive.
	 * @param type
	 *          The class of the parameter elements and of the set.
	 * @return New EnumBitSet, equal to the given bit mask.
	 * */
	public static <X extends Enum<X> & EnumBitSetHelper<X>> EnumBitSet<X> asEnumBitSet(final BigInteger mask, final Class<X> type) {
		if (requireNonNull(mask).signum() == -1)
			throw new IllegalArgumentException("The mask must not be negative!");
		return new EnumBitSet<>(type, asEnumSet(mask, type));
	}

	/**
	 * Creates set of enums from a bit set.
	 * 
	 * @param <X>
	 *          The class of the parameter elements and of the set
	 * @param set
	 *          A BitSet.
	 * @param type
	 *          The class of the parameter elements and of the set.
	 * @return New EnumBitSet, equal to the BitSet.
	 * */
	public static <X extends Enum<X> & EnumBitSetHelper<X>> EnumBitSet<X> asEnumBitSet(final BitSet set, final Class<X> type) {
		return new EnumBitSet<>(type, asEnumSet(set, type));
	}

	/**
	 * Creates a new EnumBitSet from a given Collection. The Collection must not
	 * contain <code>null</code>.
	 * 
	 * @param <X>
	 *          The class of the parameter elements and of the set
	 * @param collection
	 *          A Collection of enum elements, not null and not containing null.
	 * @param type
	 *          The class of the parameter elements and of the set.
	 * @return New EnumBitSet, equal to the Collection.
	 */
	public static <X extends Enum<X> & EnumBitSetHelper<X>> EnumBitSet<X> asEnumBitSet(final Collection<X> collection, final Class<X> type) {
		final EnumBitSet<X> result = noneOf(type);
		result.addAll(collection);
		return result;
	}

	/**
	 * Creates a new EnumBitSet from a given BitSet.
	 * 
	 * @param <X>
	 *          The class of the parameter elements and of the set
	 * @param set
	 *          An EnumSet.
	 * @param type
	 *          The class of the parameter elements and of the set.
	 * @return New EnumBitSet, equal to the EnumSet.
	 */
	public static <X extends Enum<X> & EnumBitSetHelper<X>> EnumBitSet<X> asEnumBitSet(final EnumSet<X> set, final Class<X> type) {
		return new EnumBitSet<>(type, set.clone());
	}

	/**
	 * Creates set of enums from a long.
	 * 
	 * @param <X>
	 *          The class of the parameter elements and of the set
	 * @param mask
	 *          A bit mask.
	 * @param type
	 *          The class of the parameter elements and of the set.
	 * @return New EnumBitSet, equal to the given bit mask.
	 * */
	public static <X extends Enum<X> & EnumBitSetHelper<X>> EnumBitSet<X> asEnumBitSet(final long mask, final Class<X> type) throws MoreThan64ElementsException {
		return new EnumBitSet<>(type, asEnumSet(mask, type));
	}

	/**
	 * Creates set of enums from a bit set.
	 * 
	 * @param <X>
	 *          The class of the parameter elements and of the set
	 * @param mask
	 *          A bit mask, must be positive.
	 * @param type
	 *          The class of the parameter elements and of the set
	 * @return New EnumSet, equal to the given bit mask.
	 * */
	public static <X extends Enum<X> & EnumBitSetHelper<X>> EnumSet<X> asEnumSet(final BigInteger mask, final Class<X> type) {
		if (requireNonNull(mask).signum() == -1)
			throw new IllegalArgumentException("The mask must not be negative!");
		final EnumSet<X> result = EnumSet.allOf(requireNonNull(type));
		result.removeIf(e -> e.intersect(mask).equals(BigInteger.ZERO));
		return result;
	}

	/**
	 * Creates set of enums from a BitSet.
	 * 
	 * @param <X>
	 *          The class of the parameter elements and of the set
	 * @param bitset
	 *          A BitSet.
	 * @param type
	 *          The class of the parameter elements and of the set.
	 * @return New EnumSet, equal to the given BitSet.
	 * */
	public static <X extends Enum<X> & EnumBitSetHelper<X>> EnumSet<X> asEnumSet(final BitSet bitset, final Class<X> type) {
		requireNonNull(bitset);
		final EnumSet<X> result = EnumSet.allOf(requireNonNull(type));
		result.removeIf(e -> !bitset.get(e.ordinal()));
		return result;
	}

	/**
	 * Creates set of enums from a 64 bit bit set.
	 * 
	 * @param <X>
	 *          The enum type of the set.
	 * @param mask
	 *          The bit mask.
	 * @param type
	 *          The enum type of the set.
	 * @throws MoreThan64ElementsException
	 *           This fails if the enum type contains more than 64 elements.
	 * @return New EnumSet with all elements of the given bit mask.
	 * */
	public static <X extends Enum<X> & EnumBitSetHelper<X>> EnumSet<X> asEnumSet(final long mask, final Class<X> type) throws MoreThan64ElementsException {
		final EnumSet<X> result = EnumSet.allOf(requireNonNull(type));
		result.removeIf(e -> (e.bitmask64() & mask) == 0);
		return result;
	}

	/**
	 * Creates set of enums from at least one element.
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
	 *           if any of the specified elements are null, or if <tt>rest</tt> is
	 *           null
	 * @return an EnumSet initially containing the specified elements. This is
	 *         equal to: <code>EnumSet.of(first, rest)</code>.
	 * */
	@SafeVarargs
	public static <X extends Enum<X> & EnumBitSetHelper<X>> EnumSet<X> asEnumSet(final X first, final X... rest) {
		return EnumSet.of(first, rest);
	}

	/**
	 * Converts a BigInteger to long.
	 * 
	 * @param mask
	 *          A bit mask, must be positive and lower than 2<sup>64</sup>.
	 * @throws IllegalArgumentException
	 *           Only positive values with up to 64 bits are allowed.
	 * @return A long value representing the given BigInteger, if it is valid (bit
	 *         length = 64).
	 */
	public static long asLong(final BigInteger mask) {
		final long result = mask.longValue();// lower 64 bits
		if (result < 0)
			throw new IllegalArgumentException("Negative value not permitted.");
		if (mask.bitLength() > 64)
			throw new IllegalArgumentException("Value is too large (more than 64 bits).");
		return result;
	}

	/**
	 * Converts a BitSet to long.
	 * 
	 * @param bitset
	 *          A bit set.
	 * @throws IllegalArgumentException
	 *           Only bit sets with using up to 64 bits are allowed.
	 * @return A long value representing the given bit set.
	 */
	public static long asLong(final BitSet bitset) {
		if (bitset.length() >= 64)
			throw new IllegalArgumentException("The bitset contains more than 64 elements.");
		return bitset.toLongArray()[0];
	}

	/**
	 * Creates a 64 bit bitmask of a given set of enums.
	 * 
	 * @param <X>
	 *          Enum type of the elements.
	 * @param set
	 *          An EnumSet af an enum type with up to 64 constants.
	 * @throws MoreThan64ElementsException
	 *           This fails if any element in the set has a higher index than 63.
	 * @return A long value that represents the given set as a bit mask.
	 * */
	public static <X extends Enum<X> & EnumBitSetHelper<X>> long asLong(final EnumSet<X> set) throws MoreThan64ElementsException {
		long result = 0L;
		for (final X x : requireNonNull(set))
			result |= x.bitmask64();
		return result;
	}

	/**
	 * Creates a 64 bit bit set of a given set of enums.
	 * 
	 * @param <X>
	 *          Enum type of the elements.
	 * @param set
	 *          An EnumSet af an enum type with up to 64 constants.
	 * @throws MoreThan64ElementsException
	 *           This fails if any element in the set has a higher index than 63.
	 * @return A long value that represents the given set as a bit mask.
	 */
	@SafeVarargs
	public static <X extends Enum<X> & EnumBitSetHelper<X>> long asLong(final X... set) throws MoreThan64ElementsException {
		long result = 0;
		for (final X e : requireNonNull(set))
			result |= e.bitmask64();// bitmask64() checks index!
		return result;
	}

	/**
	 * Returns a new EnumBitSet containing just one enum value.<br>
	 * Note: <code>EnumBitSet.just(X)</code> is equal to
	 * <code>X.asEnumBitSet()</code>
	 * 
	 * @param <X>
	 *          The enum type of the value.
	 * @param value
	 *          The single value that will be contained in the result.
	 * @return New EnumBitSet containing nothing but <code>value</code>.
	 * */
	public static <X extends Enum<X> & EnumBitSetHelper<X>> EnumBitSet<X> just(final X value) {
		final EnumBitSet<X> result = noneOf(requireNonNull(value).getDeclaringClass());
		result.add(value);
		return result;
	}

	public static <X extends Enum<X> & EnumBitSetHelper<X>> EnumBitSet<X> noneOf(final Class<X> type) {
		return new EnumBitSet<>(type);
	}

	/**
	 * Creates a new EnumBitSet containing at least one value.
	 * 
	 * @param <X>
	 *          The enum type of all elements.
	 * @param first
	 *          The first element (must not be null).
	 * @param more
	 *          More elements to add.
	 * @return New EnumBitSet containing all given elements.
	 * @see #noneOf(Class)
	 */
	@SafeVarargs
	public static <X extends Enum<X> & EnumBitSetHelper<X>> EnumBitSet<X> of(final X first, final X... more) {
		final EnumBitSet<X> result = noneOf(requireNonNull(first).getDeclaringClass());
		result.add(first);
		if (more != null)
			for (final X x : more)
				result.add(x);
		return result;
	}

	/**
	 * Creates an enum set initially containing all of the elements in the range
	 * defined by the two specified endpoints. The returned set will contain the
	 * endpoints themselves, which may be identical but must not be out of order.
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
	 * @return an enum set initially containing all of the elements in the range
	 *         defined by the two specified endpoints
	 */
	public static <X extends Enum<X> & EnumBitSetHelper<X>> EnumBitSet<X> range(final X from, final X to) {
		return new EnumBitSet<>(requireNonNull(from).getDeclaringClass(), EnumSet.range(from, requireNonNull(to)));
	}

	private final EnumSet<E> bitset;

	private final Class<E> enumType;

	private int enumTypeSize = -1;

	private EnumBitSet(final Class<E> type) {
		this(type, EnumSet.noneOf(type));
	}

	private EnumBitSet(final Class<E> type, final EnumSet<E> set) {
		this.enumType = requireNonNull(type);
		this.bitset = requireNonNull(set);
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
	public EnumBitSet<E> clone() {
		return new EnumBitSet<>(this.enumType, this.bitset.clone());
	}

	/**
	 * Creates an enum set with the same element type as the specified enum set,
	 * initially containing all the elements of this type that are not contained
	 * in the specified set.
	 * 
	 * @return The complement of this EnumBitSet.
	 */
	public EnumBitSet<E> complement() {
		return new EnumBitSet<>(this.enumType, EnumSet.complementOf(this.bitset));
	}

	/** {@inheritDoc} */
	@Override
	public boolean contains(final Object o) {
		return this.bitset.contains(o);
	}

	/** {@inheritDoc} */
	@Override
	public boolean containsAll(final Collection<?> c) {
		return this.bitset.containsAll(c);
	}

	/**
	 * Returns the Cartesian Product.
	 * 
	 * <p>
	 * Cartesian product of A and B, denoted <code>A × B</code>, is the set whose
	 * members are all possible ordered pairs <code>(a,b)</code> where a is a
	 * member of A and b is a member of B. The Cartesian product of
	 * <code>{1, 2}</code> and <code>{red, white}</code> is {(1, red), (1, white),
	 * (2, red), (2, white)}.
	 * 
	 * @param set
	 *          Another set.
	 * @param <Y>
	 *          Enum type of the elements.
	 * @return The Cartesian Product of <code>this</code> and <code>set</code>.
	 * 
	 * */
	public <Y extends Enum<Y> & EnumBitSetHelper<Y>> List<Pair<E, Y>> cross(final EnumBitSet<Y> set) {
		final List<Pair<E, Y>> result = new ArrayList<>(this.size() * set.size());
		for (final E e1 : this)
			for (final Y e2 : set)
				result.add(new Pair<>(e1, e2));
		return result;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see EnumSet#equals(Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		return this.bitset.equals(obj);
	}

	/**
	 * The enum type class that defines the available enum elements. This is the
	 * class returned by {@link Enum#getDeclaringClass()}.
	 * 
	 * @return The declaring class of all elements in this set.
	 */
	public Class<E> getEnumType() {
		return this.enumType;
	}

	/**
	 * Amount of enum elements. This is relevant to know how large the bit field
	 * must be to holt a bit set of this type.
	 * 
	 * @return Number of constants of the enum type.
	 */
	public synchronized int getEnumTypeSize() {
		if (this.enumTypeSize == -1)
			this.enumTypeSize = this.enumType.getEnumConstants().length;
		return this.enumTypeSize;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return this.bitset.hashCode();
	}

	/**
	 * Returns a new EnumBitSet containing all elements that are in
	 * <code>this</code> and the given <code>mask</code>.
	 * 
	 * @see #complement()
	 * @param mask
	 *          Another set, represented by a bit mask, must be positive.
	 * @return <code> this &#x2229; set</code>
	 */
	public EnumBitSet<E> intersect(final BigInteger mask) {
		if (requireNonNull(mask).signum() == -1)
			throw new IllegalArgumentException("The mask must not be negative!");
		final EnumSet<E> clone = this.bitset.clone();
		clone.removeIf(e -> !mask.testBit(e.ordinal()));
		return new EnumBitSet<>(this.enumType, clone);
	}

	/**
	 * Returns a new EnumBitSet containing all elements that are in
	 * <code>this</code> and the given <code>set</code>.
	 * 
	 * @see #complement()
	 * @param set
	 *          Another set.
	 * @return <code> this &#x2229; set</code>
	 */
	public EnumBitSet<E> intersect(final BitSet set) {
		final EnumSet<E> clone = this.bitset.clone();
		clone.removeIf(e -> !set.get(e.ordinal()));
		return new EnumBitSet<>(this.enumType, clone);
	}

	/**
	 * Returns a new EnumBitSet containing all elements that are in
	 * <code>this</code> and the given <code>set</code>.
	 * 
	 * @see #complement()
	 * @param set
	 *          Another set.
	 * @return <code> this &#x2229; set</code>
	 */
	public EnumBitSet<E> intersect(@SuppressWarnings("unchecked") final E... set) {
		final EnumSet<E> clone = this.bitset.clone();
		clone.retainAll(asList(set));
		return new EnumBitSet<>(this.enumType, clone);
	}

	/**
	 * Returns a new EnumBitSet containing all elements that are in
	 * <code>this</code> and the given <code>set</code>.
	 * 
	 * @param set
	 *          Another set.
	 * @see #complement()
	 * @return <code> this &#x2229; set</code>
	 */
	public EnumBitSet<E> intersect(final EnumBitSet<E> set) {
		return intersect(set.toEnumSet());
	}

	/**
	 * Returns a new EnumBitSet containing all elements that are in
	 * <code>this</code> and the given <code>set</code>.
	 * 
	 * @see #complement()
	 * @param set
	 *          Another set.
	 * @return <code> this &#x2229; set</code>
	 */
	public EnumBitSet<E> intersect(final EnumSet<E> set) {
		final EnumSet<E> clone = this.bitset.clone();
		clone.retainAll(set);
		return new EnumBitSet<>(this.enumType, clone);
	}

	/**
	 * Returns a new EnumBitSet containing all elements that are in
	 * <code>this</code> and the given <code>mask</code>.
	 * 
	 * @see #complement()
	 * @param mask
	 *          Another set, represented by a bit mask.
	 * @return <code> this &#x2229; set</code>
	 */
	public EnumBitSet<E> intersect(final long mask) {
		final EnumSet<E> clone = this.bitset.clone();
		clone.removeIf(e -> (e.bitmask64() & mask) == 0);
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

	/**
	 * Returns a new EnumBitSet containing all elements that are in
	 * <code>this</code>, but not in the given <code>mask</code>.
	 * 
	 * @see #removeAll(Collection)
	 * @param mask
	 *          Another set, represented by a bit mask, must be positive.
	 * @return <code>this &#x2216; mask</code>
	 */
	public EnumBitSet<E> minus(final BigInteger mask) {
		// A\B = A & ~B
		// So one might think that this works:
		// this.toBigInteger().and(mask.not());
		// It doesn't, because the BigInteger doesn't know the size of the enum
		// type (the "universe").

		// Solution using BigInteger:
		final BigInteger a = this.toBigInteger();
		final BigInteger all = BigInteger.ONE.shiftLeft(this.getEnumTypeSize()).subtract(BigInteger.ONE);
		final BigInteger notb = mask.xor(all);
		return asEnumBitSet(a.and(notb), this.enumType);

		// Solution using EnumSet:
		// final EnumBitSet<E> result = this.clone();
		// if (BigInteger.ZERO.equals(mask))
		// return result;
		// result.removeAll(asEnumSet(mask, this.enumType));
		// return result;
	}

	/**
	 * Returns a new EnumBitSet containing all elements that are in
	 * <code>this</code>, but not in the given <code>bit set</code>.
	 * 
	 * @see #removeAll(Collection)
	 * @param set
	 *          Another set.
	 * @return <code>this &#x2216; set</code>
	 */
	public EnumBitSet<E> minus(final BitSet set) {
		final EnumBitSet<E> result = this.clone();
		if (set == null || set.isEmpty())
			return result;
		result.removeAll(asEnumSet(set, this.enumType));
		return result;
	}

	/**
	 * Returns a new EnumBitSet containing all elements that are in
	 * <code>this</code>, but not in the given <code>set</code>.
	 * 
	 * @see #removeAll(Collection)
	 * @param set
	 *          Another set.
	 * @return <code>this &#x2216; set</code>
	 */
	@SuppressWarnings("unchecked")
	public EnumBitSet<E> minus(final E... set) {
		final EnumBitSet<E> result = this.clone();
		if (set == null || set.length == 0)
			return result;
		result.removeAll(asList(set));
		return result;
	}

	/**
	 * Returns a new EnumBitSet containing all elements that are in
	 * <code>this</code>, but not in the given <code>set</code>.
	 * 
	 * @see #removeAll(Collection)
	 * @param set
	 *          Another set.
	 * @return <code>this &#x2216; set</code>
	 */
	public EnumBitSet<E> minus(final EnumBitSet<E> set) {
		final EnumBitSet<E> result = this.clone();
		if (set == null || set.isEmpty())
			return result;
		result.removeAll(set);
		return result;
	}

	/**
	 * Returns a new EnumBitSet containing all elements that are in
	 * <code>this</code>, but not in the given <code>set</code>.
	 * 
	 * @see #removeAll(Collection)
	 * @param set
	 *          Another set.
	 * @return <code>this &#x2216; set</code>
	 */
	public EnumBitSet<E> minus(final EnumSet<E> set) {
		final EnumBitSet<E> result = this.clone();
		if (set == null || set.isEmpty())
			return result;
		result.removeAll(set);
		return result;
	}

	/**
	 * Returns a new EnumBitSet containing all elements that are in
	 * <code>this</code>, but not in the given <code>mask</code>.
	 * 
	 * @see #removeAll(Collection)
	 * @param mask
	 *          Another set, represented by a bit mask.
	 * @return <code>this &#x2216; mask</code>
	 */
	public EnumBitSet<E> minus(final long mask) {
		if (mask == 0)
			return this.clone();
		return asEnumBitSet(this.toLong() & ~mask, this.enumType);
	}

	/** {@inheritDoc} */
	@Override
	public boolean remove(final Object o) {
		return this.bitset.remove(o);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see #minus(Enum...)
	 * @see #minus(EnumBitSet)
	 * @see #minus(EnumSet)
	 * @see #minus(BigInteger)
	 * */
	@Override
	public boolean removeAll(final Collection<?> c) {
		return this.bitset.removeAll(c);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see #intersect(BigInteger)
	 * @see #intersect(BitSet)
	 * @see #intersect(Enum...)
	 * @see #intersect(EnumBitSet)
	 * @see #intersect(EnumSet)
	 * @see #intersect(long)
	 * */
	@Override
	public boolean retainAll(final Collection<?> c) {
		return this.bitset.retainAll(c);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Not to be confused with {@link #getEnumTypeSize()}.
	 */
	@Override
	public int size() {
		return this.bitset.size();
	}

	/** {@inheritDoc} */
	@Override
	public Spliterator<E> spliterator() {
		return this.bitset.spliterator();
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

	/**
	 * Returns a BigInteger that represents this set.
	 * 
	 * @see #toBinaryString()
	 * @return A representation of this {@link EnumBitSet} as a {@link BigInteger}
	 *         .
	 */
	public BigInteger toBigInteger() {
		if (getEnumTypeSize() <= 64)
			return BigInteger.valueOf(toLong());
		else {
			BigInteger result = BigInteger.ZERO;
			for (final E e : this.bitset)
				result = result.or(e.bitmask());
			return result;
		}
	}

	/**
	 * Binary string representation of this set.
	 * <p>
	 * The length of the returned string is the same as the amount of enum
	 * elements in the enum type.
	 * 
	 * @return A representation of this {@link EnumBitSet} as a String of 0s and
	 *         1s.
	 */
	public String toBinaryString() {
		return toBinaryString(getEnumTypeSize());
	}

	/**
	 * Binary string representation of this set.
	 * <p>
	 * The length of the returned string is as least as long as <i>width</i>.
	 * <p>
	 * Example: Use this if you have an enum type with less than 64 elements but
	 * you want to use a bit field with 64 bits.
	 * <code>this.toBinaryString(64)</code> will already have the appropriate
	 * length.
	 * 
	 * @param width
	 *          The minimal width of the returned String.
	 * @return A representation of this {@link EnumBitSet} as a String of 0s and
	 *         1s. The length is at least <i>width</i>.
	 */
	public String toBinaryString(final int width) {
		final String binary = this.toBigInteger().toString(2);
		final StringBuilder sb = new StringBuilder(width < 8 ? 8 : width);
		while (sb.length() < width - binary.length())
			sb.append('0');
		sb.append(binary);
		return sb.toString();
	}

	/**
	 * Returns a new BitSet that represents this set.
	 * 
	 * @return A representation of this {@link EnumBitSet} as a {@link BitSet};
	 */
	public BitSet toBitSet() {
		final BitSet result = new BitSet(this.getEnumTypeSize());
		for (final E e : this.bitset)
			result.set(e.ordinal());
		return result;
	}

	/**
	 * Copy of the underlying EnumSet.
	 * 
	 * @return <code>bitset.clone()</code>
	 */
	public EnumSet<E> toEnumSet() {
		return this.bitset.clone();
	}

	/**
	 * Returns a long value that represents this set.
	 * 
	 * @throws MoreThan64ElementsException
	 *           This fails if any element in this set has a higher index than 63.
	 * @return A representation of this {@link EnumBitSet} as a {@link Long long}.
	 */
	public long toLong() throws MoreThan64ElementsException {
		long result = 0L;
		for (final E e : this.bitset)
			result |= e.bitmask64();// bitmask64() checks index!
		return result;
	}

	/**
	 * Returns a string representation of this set.
	 * 
	 * @see EnumSet#toString()
	 */
	@Override
	public String toString() {
		return this.bitset.toString();
	}

	/**
	 * Throws an exception if e is not of the correct type for this enum set.
	 * 
	 * @param e
	 *          the element to be checked.
	 * @throws ClassCastException
	 *           Throws ClassCastException if e is not of the correct type.
	 */
	public void typeCheck(final E e) throws ClassCastException {
		// This is equivalent to the nonvisible method: this.bitset.typeCheck(e);
		final Class<?> eClass = e.getClass();
		// note that this would be faster:
		// if (eClass != elementType && eClass.getSuperclass() != elementType)
		// But the API only guarantees that this works:
		if (eClass.getDeclaringClass() != this.enumType)
			throw new ClassCastException(eClass + " != " + this.enumType);
	}

	/**
	 * Returns a new EnumBitSet containing all elements that are in
	 * <code>this</code> or the given <code>mask</code>.
	 * 
	 * @param mask
	 *          Bit mask of another set, must be positive.
	 * @return <code> this &#x222a; set</code>
	 */
	public EnumBitSet<E> union(final BigInteger mask) {
		if (requireNonNull(mask).signum() == -1)
			throw new IllegalArgumentException("The mask must not be negative!");
		return union(asEnumSet(mask, this.enumType));
	}

	/**
	 * Returns a new EnumBitSet containing all elements that are in
	 * <code>this</code> or the given <code>set</code>.
	 * 
	 * @param set
	 *          Another set.
	 * @return <code> this &#x222a; set</code>
	 */
	public EnumBitSet<E> union(final BitSet set) {
		// return union(asEnumSet(set, this.enumType));
		final EnumSet<E> clone = this.bitset.clone();
		final E[] values = this.enumType.getEnumConstants();
		for (int i = set.nextSetBit(0); i >= 0; i = set.nextSetBit(i + 1))
			clone.add(values[i]);
		return new EnumBitSet<>(this.enumType, clone);
	}

	/**
	 * Returns a new EnumBitSet containing all elements that are in
	 * <code>this</code> or the given <code>set</code>.
	 * 
	 * @param set
	 *          Another set.
	 * @return <code> this &#x222a; set</code>
	 */
	public EnumBitSet<E> union(@SuppressWarnings("unchecked") final E... set) {
		final EnumSet<E> clone = this.bitset.clone();
		clone.addAll(asList(set));
		return new EnumBitSet<>(this.enumType, clone);
	}

	/**
	 * Returns a new EnumBitSet containing all elements that are in
	 * <code>this</code> or the given <code>set</code>.
	 * 
	 * @param set
	 *          Another set.
	 * @return <code> this &#x222a; set</code>
	 */
	public EnumBitSet<E> union(final EnumBitSet<E> set) {
		return union(set.bitset);
	}

	/**
	 * Returns a new EnumBitSet containing all elements that are in
	 * <code>this</code> or the given <code>set</code>.
	 * 
	 * @param set
	 *          Another set.
	 * @return <code> this &#x222a; set</code>
	 */
	public EnumBitSet<E> union(final EnumSet<E> set) {
		final EnumSet<E> clone = this.bitset.clone();
		clone.addAll(set);
		return new EnumBitSet<>(this.enumType, clone);
	}

	/**
	 * Returns a new EnumBitSet containing all elements that are in
	 * <code>this</code> or the given <code>mask</code>.
	 * 
	 * @param mask
	 *          Bit mask of another set.
	 * @return <code> this &#x222a; set</code>
	 */
	public EnumBitSet<E> union(final long mask) {
		return union(asEnumSet(mask, this.enumType));
	}

}