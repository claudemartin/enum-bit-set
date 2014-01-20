package ch.claude_martin.enumbitset;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.Set;

/**
 * This adds support to use bit fields and bit masks for the enym type. The set
 * of enum values is interpreted as an {@link EnumBitSet enum bit set}, which
 * can be stored to a bit field (i.e. an integer field in a database).
 * 
 * <p>
 * Since this is an interface it does not change the state of the element it is
 * used on or any parameters passed to a method. Instead it always returns a
 * newly created object. I.e. all methods that take a Set return a new Set. If
 * you wish to alter the set you can simply use the methods of that set.
 * 
 * <p>
 * Examples for usage:
 * 
 * <pre>User permissions:
 * <code>public enum Role { READ, WRITE, EXECUTE }
 * 
 * BigInteger permissions = MyDBAccess.getUserPermissions(currentUser);
 * permissions = Role.READ.removedFrom(permissions);
 * MyDBAccess.setUserPermissions(currentUser, permissions);
 * </code></pre>
 * 
 * <pre>Planetary composition:
 * <code>public enum Planet { MERCURY, VENUS, EARTH, ... }
 * public enum Element { H, He, Li, Be, B, C, N, ... }
 * 
 * final Map&lt;Planet, EnumBitSet&lt;Element&gt;&gt; composition = new HashMap<>();
 * final toBigInteger&lt;Planet&gt; mercury = Element.O.union(Element.Na, Element.H, Element.He, Element.K);
 * composition.put(Planet.MERCURY, mercury);
 * </code>
 * 
 * BigInteger permissions = MyDBAccess.getUserPermissions(currentUser);
 * permissions = Role.READ.removedFrom(permissions);
 * MyDBAccess.setUserPermissions(currentUser, permissions);
 * </code></pre>
 * 
 * <pre>Counterexample:
 * <code>public enum Status { SUBMITTED, REVIEWED, ACCEPTED, PUBLISHED }</code>
 * Each status includes all previous ones (can't be reviewed if not submitted). 
 * A regular integer field is enough to store the status 
 * (e.g. <code>REVIEWED.ordinal()</code>). 
 * </pre>
 * <p>
 * Java 8 Beta has still a bug that does not allow assert statements in
 * interfaces: <a
 * href="https://bugs.openjdk.java.net/browse/JDK-8025141">https:/
 * /bugs.openjdk.java.net/browse/JDK-8025141</a>
 * 
 * 
 * @author <a href="http://claude-martin.ch/enumbitset/">Copyright &copy; 2014
 *         Claude Martin</a>
 * 
 * @param <E>
 *          The Enum-Type.
 */
public interface EnumBitSetHelper<E extends Enum<E> & EnumBitSetHelper<E>> {

	/**
	 * Returns whether this enum can be found in the list of enums of the same
	 * type.
	 * 
	 * @returns <code>true</code>, if <code>this</code> can be found.
	 */
	// @SafeVarargs
	@SuppressWarnings("unchecked")
	// TODO add @SafeVarargs as soon as java 8 allows it here.
	public default boolean elementOf(final Enum<E>... set) {
		for (final Enum<?> e : requireNonNull(set))
			if (e == this)
				return true;
		return false;
	}

	/**
	 * Returns whether this enum can be found in the set of enums of the same
	 * type.
	 * <p>
	 * This is equivalent to: <code>
	 * set.contains(this);
	 * </code>
	 * 
	 * @returns <code>true</code>, if <code>this</code> can be found in
	 *          <code>set</code>.
	 */
	public default boolean elementOf(final Set<E> set) {
		return requireNonNull(set).contains(this);
	}

	/**
	 * Creates a new EnumSet with <code>this</code> added.
	 * 
	 * @returns A new {@link EnumSet} including all elements of the set and also
	 *          <code>this</code>.
	 */
	@SuppressWarnings("unchecked")
	public default EnumSet<E> union(final EnumSet<E> set) {
		final EnumSet<E> result = EnumSet.copyOf(requireNonNull(set));
		result.add((E) this);
		return result;
	}

	/**
	 * Creates a new EnumSet with <code>this</code> added.
	 * 
	 * @returns A new {@link EnumSet} including all elements of the set and also
	 *          <code>this</code>.
	 */
	@SuppressWarnings("unchecked")
	public default BitSet union(final BitSet set) {
		final BitSet result = (BitSet) set.clone();
		result.set(((E) this).ordinal(), true);
		return result;
	}

	/**
	 * Creates a new EnumSet with <code>this</code> removed.
	 * 
	 * @returns A new {@link EnumSet} with all given enums, except
	 *          <code>this</code>. A copy of the set is returned if
	 *          <code>this</code> is not present.
	 */
	@SuppressWarnings("unchecked")
	public default EnumSet<E> removedFrom(final E... set) {
		if (set.length == 0)
			return EnumSet.noneOf(((Enum<E>) this).getDeclaringClass());
		final EnumSet<E> result = EnumSet.copyOf(Arrays.asList(requireNonNull(set)));
		result.remove(this);
		return result;
	}

	/**
	 * Creates a new EnumSet with <code>this</code> removed.
	 * 
	 * @returns A new {@link EnumSet} with all elements of the set, except
	 *          <code>this</code>. A copy of the set is returned if
	 *          <code>this</code> is not present.
	 */
	public default EnumSet<E> removedFrom(final EnumSet<E> set) {
		final EnumSet<E> result = EnumSet.copyOf(requireNonNull(set));
		result.remove(this);
		return result;
	}

	/**
	 * Creates a new EnumBitSet with <code>this</code> removed.
	 * 
	 * @returns A new {@link EnumBitSet} with all elements of the set, except
	 *          <code>this</code>. A copy of the set is returned if
	 *          <code>this</code> is not present.
	 */
	public default EnumBitSet<E> removedFrom(final EnumBitSet<E> set) {
		final EnumBitSet<E> result = requireNonNull(set).clone();
		result.remove(this);
		return result;
	}

	/**
	 * Returns a set of all enums except <code>this</code>.
	 * 
	 * <p>
	 * This is equivalent to: <code>EnumBitSet.just(this).complement()</code>
	 * 
	 * @returns A new {@link EnumSet} with all elements except <code>this</code>.
	 */
	@SuppressWarnings("unchecked")
	public default EnumBitSet<E> others() {
		return EnumBitSet.just((E) this).complement();
	}

	/**
	 * Returns a set containing nothing but <code>this</code>. <br/>
	 * Note: <code>EnumBitSet.just(X)</code> is equal to
	 * <code>X.asEnumBitSet()</code>
	 * 
	 * @returns A new {@link EnumSet} containing <code>this</code>.
	 */
	@SuppressWarnings("unchecked")
	public default EnumBitSet<E> toEnumBitSet() {
		return EnumBitSet.just((E) this);
	}

	/**
	 * Returns a set containing nothing but <code>this</code>.
	 * 
	 * @returns A new {@link EnumSet} containing <code>this</code>.
	 */
	@SuppressWarnings("unchecked")
	public default EnumSet<E> toEnumSet() {
		final E e = (E) this;
		final EnumSet<E> result = EnumSet.noneOf(e.getDeclaringClass());
		result.add(e);
		return result;
	}

	/**
	 * Returns a BitSet with all bits set to 0, except the bit representing
	 * <code>this</code>.
	 * 
	 * @returns A new {@link EnumSet} containing <code>this</code>.
	 */
	@SuppressWarnings("unchecked")
	public default BitSet toBitSet() {
		final E e = (E) this;
		final BitSet result = new BitSet();
		result.set(e.ordinal());
		return result;
	}

	/**
	 * Bitmask for <code>this</code>. The value is based on the ordinal. This is
	 * actually the same as {@link #bitmask()}.
	 * 
	 * @return <code>this.bitmask()</code>
	 */
	public default BigInteger toBigInteger() {
		return this.bitmask();
	}

	/**
	 * Bitmask for <code>this</code>. The value is based on the ordinal. This is
	 * actually the same as {@link #bitmask64()}.
	 * 
	 * @return <code>this.bitmask64()</code>
	 */
	public default long toLong() {
		return this.bitmask64();
	}

	/**
	 * 64 bit bitmask for <code>this</code>. The value is based on the ordinal.
	 * 
	 * @see #bitmask()
	 * @see #toEnumBitSet()
	 * @see #toBitSet()
	 * @see #toEnumSet()
	 * @return <code>1&lt;&lt;this.ordinal()</code>
	 */
	@SuppressWarnings("unchecked")
	public default long bitmask64() throws MoreThan64ElementsException {
		final E e = (E) this;
		if (e.ordinal() >= 64)
			throw new MoreThan64ElementsException(e.getDeclaringClass());
		return 1 << e.ordinal();
	}

	/**
	 * Bitmask for <code>this</code>. The value is based on the ordinal.
	 * 
	 * @see #toEnumBitSet()
	 * @see #toBitSet()
	 * @see #toEnumSet()
	 * @return <code>1&lt;&lt;this.ordinal()</code>
	 */
	@SuppressWarnings("unchecked")
	public default BigInteger bitmask() {
		return BigInteger.ONE.shiftLeft(((E) this).ordinal());
	}

	/**
	 * Returns whether this value is set in the given bitmask.
	 * 
	 * @param bitmask64
	 *          A bitmask.
	 * @return (this.bitmask64() & bitmask8) != 0;
	 */
	public default boolean elementOf(final long bitmask64) throws MoreThan64ElementsException {
		return (this.bitmask64() & bitmask64) != 0;
	}

	/**
	 * Returns whether this value is set in the given bitmask.
	 * 
	 * @param bitmask
	 *          A bitmask.
	 * @return (this.bitmask() & bitmask) != 0
	 */
	public default boolean elementOf(final BigInteger bitmask) {
		if (bitmask.signum() == -1)
			throw new IllegalArgumentException("The mask must not be negative!");
		return !this.bitmask().and(requireNonNull(bitmask)).equals(BigInteger.ZERO);
	}

	/**
	 * Returns whether this value is set in the given bitset.
	 * 
	 * @param bitset
	 * @return bitset.get(this.ordinal());
	 */
	@SuppressWarnings("unchecked")
	public default boolean elementOf(final BitSet bitset) {
		return requireNonNull(bitset).get(((E) this).ordinal());
	}

	/**
	 * @param mask
	 *          A bit mask.
	 * @return this.bitmask64() | mask;
	 */
	public default long union(final long mask) {
		return this.bitmask64() | mask;
	}

	/**
	 * 
	 * Note that there is a much simpler way using {@link EnumSet#of(Enum)
	 * of(...)}. The following expressions define the same set: <br>
	 * <code>Planet.<b>EARTH</b>.or(Planet.<b>MARS</b>, Planet.<b>JUPITER</b>)<br/>
	 * Planet.of(Planet.<b>EARTH</b>, Planet.<b>MARS</b>, Planet.<b>JUPITER</b>)</code>
	 * <br/>
	 * However, this method returns a bitmask of type BigInteger.
	 * 
	 * @see EnumSet#of(Enum)
	 * @see #union(BigInteger)
	 * @return <code>this.bitmask() | arg<sub>0</sub>.bitmask() | ... | arg<sub>N</sub>.bitmask()</code>
	 */
	@SuppressWarnings("unchecked")
	public default EnumBitSet<E> union(final E... set) {
		final EnumBitSet<E> result = EnumBitSet.just((E) this);
		result.addAll(asList(set));
		return result;
	}

	/**
	 * Takes the bitmasks of <code>this</code> and <code>mask</code>, then applies
	 * logical OR. This results in a new bit mask that also includes
	 * <code>this</code>.
	 * 
	 * @see #union(BigInteger)
	 * @see #union(EnumSet)
	 * @see #union(Enum...)
	 * @return <code>mask.or(this.bitmask())</code>
	 */
	public default BigInteger union(final BigInteger mask) {
		if (mask.signum() == -1)
			throw new IllegalArgumentException("The mask must not be negative!");
		return requireNonNull(mask).or(this.bitmask());
	}

	/**
	 * Takes the bitmasks of <code>this</code> and a clone of <code>mask</code>,
	 * then applies logical OR. This results in a new bit set that also includes
	 * <code>this</code>.
	 * 
	 * @see #union(BigInteger)
	 * @see #union(EnumSet)
	 * @see #union(Enum...)
	 * @return <code>mask.clone() | this</code>
	 */
	@SuppressWarnings("unchecked")
	public default EnumBitSet<E> union(final EnumBitSet<E> mask) {
		final EnumBitSet<E> clone = mask.clone();
		clone.add((E) this);
		return clone;
	}

	/**
	 * This removes all other bits. The resulting bit mask will have just one or
	 * zero bits set to 1.
	 * 
	 * @see #elementOf(BigInteger)
	 * @return <code>mask.and(this.bitmask())</code>
	 */
	public default BigInteger intersect(final BigInteger mask) {
		if (mask.signum() == -1)
			throw new IllegalArgumentException("The mask must not be negative!");
		return requireNonNull(mask).and(this.bitmask());
	}

	/**
	 * This removes all other bits. The resulting bit mask will have just one or
	 * zero bits set to 1.
	 * 
	 * @see #elementOf(BitSet)
	 * @return <code>mask & this</code>
	 */
	public default BitSet intersect(final BitSet mask) {
		final BitSet result = new BitSet();
		@SuppressWarnings("unchecked")
		final int ord = ((E) this).ordinal();
		result.set(ord, mask.get(ord));
		return result;
	}

	/**
	 * This removes all other bits. The resulting bit mask will have just one or
	 * zero bits set to 1.
	 * 
	 * @see #elementOf(BitSet)
	 * @return <code>mask & this.ordinal()</code>
	 */
	@SuppressWarnings({ "unchecked" })
	public default EnumBitSet<E> intersect(final E... set) {
		if (Arrays.asList(set).contains(this))
			return EnumBitSet.just((E) this);
		else
			return EnumBitSet.noneOf(((Enum<E>) this).getDeclaringClass());
	}

	/**
	 * This removes all other bits. The resulting bit mask will have just one or
	 * zero bits set to 1.
	 * 
	 * @see #elementOf(long)
	 * @return <code>mask & this.ordinal()</code>
	 */
	public default long intersect(final long mask) {
		return mask & this.bitmask64();
	}

	/**
	 * This removes all other bits. The resulting bit mask will have just one or
	 * zero bits set to 1.
	 * 
	 * @see #elementOf(BigInteger)
	 * @return <code>mask.clone() & this</code>
	 */
	public default EnumSet<E> intersect(final EnumSet<E> set) {
		@SuppressWarnings("unchecked")
		final E e = (E) this;
		final EnumSet<E> result = EnumSet.noneOf(e.getDeclaringClass());
		if (set.contains(this))
			result.add(e);
		return result;
	}

	/**
	 * This removes all other bits. The resulting bit mask will have just one or
	 * zero bits set to 1.
	 * 
	 * @see #elementOf(BigInteger)
	 * @return <code>mask.clone() & this</code>
	 */
	public default EnumBitSet<E> intersect(final EnumBitSet<E> set) {
		@SuppressWarnings("unchecked")
		final E e = (E) this;
		final EnumBitSet<E> result = EnumBitSet.noneOf(e.getDeclaringClass());
		if (set.contains(this))
			result.add(e);
		return result;
	}

	/**
	 * This can be used to switch one bit in a bit mask.
	 * 
	 * @return <code>mask.xor(this.bitmask())</code>
	 */
	public default BigInteger xor(final BigInteger mask) {
		if (mask.signum() == -1)
			throw new IllegalArgumentException("The mask must not be negative!");
		return requireNonNull(mask).xor(this.bitmask());
	}

	/**
	 * This can be used to switch one bit in a bit mask.
	 * 
	 * @return <code>mask.clone() XOR this</code>
	 */
	public default EnumBitSet<E> xor(final EnumBitSet<E> set) {
		@SuppressWarnings("unchecked")
		final E e = (E) this;
		final EnumBitSet<E> result = set.clone();
		if (set.contains(this))
			result.remove(e);
		else
			result.add(e);
		return result;
	}

	/**
	 * Removes this from the given mask, but only if its bit is set in the given
	 * mask.
	 * 
	 * @return <code>mask.andNot(this.bitmask())</code>
	 */
	public default BigInteger removedFrom(final BigInteger mask) {
		if (mask.signum() == -1)
			throw new IllegalArgumentException("The mask must not be negative!");
		return requireNonNull(mask).andNot(this.bitmask());
	}

}