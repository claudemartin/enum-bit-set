package ch.claude_martin.enumbitset;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.EnumSet;

import ch.claude_martin.enumbitset.annotations.CheckReturnValue;
import ch.claude_martin.enumbitset.annotations.DefaultAnnotationForParameters;
import ch.claude_martin.enumbitset.annotations.NonNull;
import ch.claude_martin.enumbitset.annotations.Nonnegative;
import ch.claude_martin.enumbitset.annotations.SuppressFBWarnings;

/** This extends any enum type with methods for bitwise operations and use in an {@link EnumBitSet}.
 * A set of such enum values can be interpreted as an {@link EnumBitSet enum bit set}, which can be
 * stored to a bit field (i.e. an integer field in a database).
 * 
 * <p>
 * Since this is an interface it does not change the state of the element it is used on or any
 * parameters passed to a method. Instead it always returns a newly created object. I.e. all methods
 * that take an EnumBitSet return a new EnumBitSet. If you wish to alter such a set you can simply
 * use the methods of that set.
 * 
 * <p>
 * <b><i>Examples for usage:</i></b>
 * 
 * <pre>
 * <b>User permissions:</b><code>
 * public enum Role implements EnumBitSetHelper&lt;Role> { 
 *    READ, WRITE, EXECUTE 
 * }
 * 
 * BigInteger permissions = MyDBAccess.getUserPermissions(currentUser);
 * permissions = Role.READ.removedFrom(permissions);
 * MyDBAccess.setUserPermissions(currentUser, permissions);
 * </code>
 *
 * <b>Planetary composition:</b>
 * <code>public enum Planet implements EnumBitSetHelper&lt;Planet> { MERCURY, VENUS, EARTH, ... }
 * public enum Element implements EnumBitSetHelper&lt;Element> { H, He, Li, Be, B, C, N, ... }
 * 
 * final Map&lt;Planet, EnumBitSet&lt;Element&gt;&gt; composition = new HashMap&lt;&gt;();
 * final EnumBitSet&lt;Element&gt; mercury = Element.O.union(Element.Na, Element.H, Element.He, Element.K);
 * composition.put(Planet.MERCURY, mercury);
 * </code>
 * 
 * <b>Counterexample:</b>
 * <code>public enum Status { SUBMITTED, REVIEWED, ACCEPTED, PUBLISHED }</code>
 * Each status includes all previous ones (can't be reviewed if not submitted). 
 * A regular integer field is enough to store the status 
 * (e.g. <code>REVIEWED.ordinal()</code>).
 * </pre>
 * <p>
 * 
 * 
 * @author <a href="http://claude-martin.ch/enumbitset/">Copyright &copy; 2014 Claude Martin</a>
 * 
 * @param <E>
 *          The Enum-Type. */
@DefaultAnnotationForParameters({ NonNull.class })
public interface EnumBitSetHelper<E extends Enum<E> & EnumBitSetHelper<E>> extends Comparable<E>,
    Serializable {

  /** Bitmask for <code>this</code>. The value is based on the ordinal.
   * 
   * @see #toEnumBitSet()
   * @see #toBitSet()
   * @see #toEnumSet()
   * @return <code>1&lt;&lt;this.ordinal()</code> */
  @SuppressFBWarnings("unchecked")
  @NonNull
  @Nonnegative
  public default BigInteger bitmask() {
    return BigInteger.ONE.shiftLeft(((E) this).ordinal());
  }

  /** 64 bit bitmask for <code>this</code>. The value is based on the ordinal.
   * 
   * @see #bitmask()
   * @see #toEnumBitSet()
   * @see #toBitSet()
   * @see #toEnumSet()
   * @return <code>1&lt;&lt;this.ordinal()</code>
   * @throws MoreThan64ElementsException
   *           If more than 64 constants are in the enum type then a <code>long</code> is not
   *           enough. The exception is only thrown if this element is not one of the first 64
   *           elements. */
  @SuppressFBWarnings("unchecked")
  public default long bitmask64() throws MoreThan64ElementsException {
    final E e = (E) this;
    if (e.ordinal() >= 64)
      throw new MoreThan64ElementsException(e.getDeclaringClass());
    return 1L << e.ordinal();
  }

  /** Returns whether this value is set in the given bitmask.
   * 
   * @param bitmask
   *          A bitmask.
   * @return (this.bitmask() &amp; bitmask) != 0 */
  public default boolean elementOf(@Nonnegative final BigInteger bitmask) {
    if (requireNonNull(bitmask).signum() == -1)
      throw new IllegalArgumentException("The mask must not be negative!");
    return !this.bitmask().and(bitmask).equals(BigInteger.ZERO);
  }

  /** Returns whether this value is set in the given bitset.
   * 
   * @param bitset
   *          A bit set.
   * @return bitset.get(this.ordinal()); */
  @SuppressFBWarnings("unchecked")
  public default boolean elementOf(final BitSet bitset) {
    return requireNonNull(bitset).get(((E) this).ordinal());
  }

  /** Returns whether this enum can be found in the given collection.
   * <p>
   * This is equivalent to: <code>coll.contains(this);</code>
   * 
   * @param collection
   *          A collection, not null.
   * 
   * @return <code>true</code>, iff <code>this</code> can be found in <code>collection</code>. */
  public default boolean elementOf(final Collection<E> collection) {
    return requireNonNull(collection).contains(this);
  }

  /** Returns whether this enum constant can be found in the array of constants of the same type.
   * 
   * @param set
   *          A set of enum elements, all non-null and of the same enum type.
   * @return <code>true</code>, if <code>this</code> can be found. */
  // @SafeVarargs not possible because default method can't be final.
  @SuppressFBWarnings("unchecked")
  public default boolean elementOf(final Enum<E>... set) {
    for (final Enum<?> e : requireNonNull(set))
      if (e == this)
        return true;
    return false;
  }

  /** Returns whether this value is set in the given bitmask.
   * <p>
   * Note: This does not check if the given mask is valid for the enum type of this element. It may
   * have more bits set than this enum type has constants.
   * 
   * @param bitmask64
   *          A bitmask.
   * @throws MoreThan64ElementsException
   *           if this element is not one of the first 64 elements.
   * @return (this.bitmask64() &amp; bitmask64) != 0; */
  public default boolean elementOf(final long bitmask64) throws MoreThan64ElementsException {
    return (this.bitmask64() & bitmask64) != 0;
  }

  /** Bit mask with all other bits removed. The resulting bit mask will have just one or zero bits
   * set to 1.
   * 
   * @see #elementOf(BigInteger)
   * @param mask
   *          A bit mask, must be positive.
   * @return <code>mask.and(this.bitmask())</code> */
  @CheckReturnValue
  @NonNull
  @Nonnegative
  public default BigInteger intersect(@Nonnegative final BigInteger mask) {
    if (requireNonNull(mask).signum() == -1)
      throw new IllegalArgumentException("The mask must not be negative!");
    return mask.and(this.bitmask());
  }

  /** Bit mask with all other bits removed. The resulting BitSet will have just one or zero bits set
   * to true.
   * 
   * @see #elementOf(BitSet)
   * @param set
   *          A bit set.
   * @return <code>mask &amp; this</code> */
  @CheckReturnValue
  @NonNull
  public default BitSet intersect(final BitSet set) {
    final BitSet result = new BitSet();
    @SuppressFBWarnings("unchecked")
    final int ord = ((E) this).ordinal();
    result.set(ord, requireNonNull(set).get(ord));
    return result;
  }

  /** Creates a set with all other elements removed. The resulting set will contain just this element
   * or nothing at all.
   * 
   * @see #elementOf(BitSet)
   * @param set
   *          A set.
   * @return <code>set &amp; this.ordinal()</code> */
  @SuppressFBWarnings({ "unchecked" })
  @CheckReturnValue
  @NonNull
  public default EnumBitSet<E> intersect(final E... set) {
    if (Arrays.asList(set).contains(this))
      return EnumBitSet.just((E) this);
    else
      return EnumBitSet.noneOf(((Enum<E>) this).getDeclaringClass());
  }

  /** Creates a set with all other elements removed. The resulting set will contain just this element
   * or nothing at all.
   * 
   * @see #elementOf(BigInteger)
   * @param set
   *          A set.
   * @return <code>set.clone() &amp; this</code> */
  @CheckReturnValue
  @NonNull
  public default EnumBitSet<E> intersect(final EnumBitSet<E> set) {
    requireNonNull(set);
    @SuppressFBWarnings("unchecked")
    final E e = (E) this;
    final EnumBitSet<E> result = EnumBitSet.noneOf(e.getDeclaringClass());
    if (set.contains(this))
      result.add(e);
    return result;
  }

  /** Bit mask with all other bits removed. The resulting bit mask will have just one or zero bits
   * set to 1.
   * 
   * @see #elementOf(BigInteger)
   * @param set
   *          A set.
   * @return <code>mask.clone() &amp; this</code> */
  @CheckReturnValue
  @NonNull
  public default EnumSet<E> intersect(final EnumSet<E> set) {
    @SuppressFBWarnings("unchecked")
    final E e = (E) this;
    final EnumSet<E> result = EnumSet.noneOf(e.getDeclaringClass());
    if (set.contains(this))
      result.add(e);
    return result;
  }

  /** Bit mask with all other bits removed. The resulting bit mask will have just one or zero bits
   * set to 1.
   * <p>
   * Note: This does not check if the given mask is valid for the enum type of this element. It may
   * have more bits set than this enum type has constants.
   * 
   * @see #elementOf(long)
   * @param mask
   *          A bit mask.
   * @throws MoreThan64ElementsException
   *           The enum type must not contain more than 64 elements.
   * @return <code>mask &amp; this.bitmask64()</code> */
  @CheckReturnValue
  @NonNull
  public default long intersect(final long mask) throws MoreThan64ElementsException {
    return mask & this.bitmask64();
  }

  /** Returns the name of this enum constant, exactly as declared in its enum declaration.
   * 
   * @see Enum#name()
   * @return the name of this enumeration constant */
  public String name();

  /** @see Enum#ordinal()
   * @return the ordinal of this enumeration constant */
  public int ordinal();

  /** Returns a set of all elements except <code>this</code>.
   * 
   * <p>
   * This is equivalent to: <code>EnumBitSet.just(this).complement()</code>
   * 
   * @return A new {@link EnumSet} with all elements except <code>this</code>. */
  @SuppressFBWarnings("unchecked")
  @NonNull
  public default EnumBitSet<E> others() {
    return EnumBitSet.just((E) this).complement();
  }

  /** Removes this from the given mask.
   * 
   * @param mask
   *          A bit mask, must be positive.
   * @return <code>mask.andNot(this.bitmask())</code> */
  @CheckReturnValue
  @NonNull
  @Nonnegative
  public default BigInteger removedFrom(@Nonnegative final BigInteger mask) {
    if (requireNonNull(mask).signum() == -1)
      throw new IllegalArgumentException("The mask must not be negative!");
    return mask.andNot(this.bitmask());
  }

  /** Removes this from the BitSet and returns the new BitSet.
   * 
   * @param set
   *          A set that may contain <code>this</code>.
   * @return <code>set \ this</code> */
  @SuppressFBWarnings("unchecked")
  @CheckReturnValue
  @NonNull
  public default BitSet removedFrom(final BitSet set) {
    if (requireNonNull(set).isEmpty())
      return new BitSet();
    final BitSet result = (BitSet) set.clone();
    result.set(((E) this).ordinal(), false);
    return result;
  }

  /** Creates a new EnumSet with <code>this</code> removed.
   * 
   * @param set
   *          A set that may contain <code>this</code>.
   * @return A new {@link EnumSet} with all given enums, except <code>this</code>. A copy of the set
   *         is returned even if <code>this</code> is not present. */
  @SuppressFBWarnings("unchecked")
  @CheckReturnValue
  @NonNull
  public default EnumBitSet<E> removedFrom(final E... set) {
    if (set.length == 0)
      return EnumBitSet.noneOf(((Enum<E>) this).getDeclaringClass());
    final EnumBitSet<E> result = EnumBitSet.of((E) this, requireNonNull(set));
    result.remove(this);
    return result;
  }

  /** Creates a new EnumBitSet with <code>this</code> removed.
   * 
   * @param set
   *          A set that may contain <code>this</code>.
   * @return A new {@link EnumBitSet} with all elements of the set, except <code>this</code>. A copy
   *         of the set is returned even if <code>this</code> is not present. */
  @CheckReturnValue
  @NonNull
  public default EnumBitSet<E> removedFrom(final EnumBitSet<E> set) {
    final EnumBitSet<E> result = requireNonNull(set).clone();
    result.remove(this);
    return result;
  }

  /** Creates a new EnumSet with <code>this</code> removed.
   * 
   * @param set
   *          A set that may contain <code>this</code>.
   * @return A new {@link EnumSet} with all elements of the set, except <code>this</code>. A copy of
   *         the set is returned even if <code>this</code> is not present. */
  @CheckReturnValue
  @NonNull
  public default EnumSet<E> removedFrom(final EnumSet<E> set) {
    final EnumSet<E> result = EnumSet.copyOf(requireNonNull(set));
    result.remove(this);
    return result;
  }

  /** Removes this from the given mask.
   * <p>
   * Note: This does not check if the given mask is valid for the enum type of this element. It may
   * have more bits set than this enum type has constants.
   * 
   * @param mask
   *          A bit mask.
   * @throws MoreThan64ElementsException
   *           if this element is not one of the first 64 elements.
   * @return <code>mask &amp; ~this.bitmask64()</code> */
  @CheckReturnValue
  @NonNull
  public default long removedFrom(final long mask) throws MoreThan64ElementsException {
    return mask & ~this.bitmask64();
  }

  /** Bit mask for <code>this</code>. The value is based on the ordinal. This is actually the same as
   * {@link #bitmask()}.
   * 
   * @return <code>this.bitmask()</code> */
  @NonNull
  @Nonnegative
  public default BigInteger toBigInteger() {
    return this.bitmask();
  }

  /** Returns a BitSet with all bits set to 0, except the bit representing <code>this</code>.
   * 
   * @return A new {@link EnumSet} containing <code>this</code>. */
  @SuppressFBWarnings("unchecked")
  @NonNull
  public default BitSet toBitSet() {
    final E e = (E) this;
    final BitSet result = new BitSet();
    result.set(e.ordinal());
    return result;
  }

  /** Returns a set containing nothing but <code>this</code>. <br>
   * Note: <code>EnumBitSet.just(X)</code> is equal to <code>X.asEnumBitSet()</code>
   * 
   * @return A new {@link EnumSet} containing <code>this</code>. */
  @SuppressFBWarnings("unchecked")
  @NonNull
  public default EnumBitSet<E> toEnumBitSet() {
    return EnumBitSet.just((E) this);
  }

  /** Returns a set containing nothing but <code>this</code>.
   * 
   * @return A new {@link EnumSet} containing <code>this</code>. */
  @SuppressFBWarnings("unchecked")
  @NonNull
  public default EnumSet<E> toEnumSet() {
    final E e = (E) this;
    final EnumSet<E> result = EnumSet.noneOf(e.getDeclaringClass());
    result.add(e);
    return result;
  }

  /** Bitmask for <code>this</code>. The value is based on the ordinal. This is actually the same as
   * {@link #bitmask64()} .
   * 
   * @see #bitmask64()
   * @return <code>this.bitmask64()</code>
   * @throws MoreThan64ElementsException
   *           If more than 64 constants are in the enum type then a <code>long</code> is not
   *           enough. The exception is only thrown if this element is not one of the first 64
   *           elements. */
  public default long toLong() throws MoreThan64ElementsException {
    return this.bitmask64();
  }

  /** Takes the bitmasks of <code>this</code> and <code>mask</code>, then applies logical OR. This
   * results in a new bit mask that also includes <code>this</code> .
   * 
   * @see #union(BigInteger)
   * @see #union(EnumSet)
   * @see #union(Enum...)
   * @param mask
   *          A bit mask, must be positive.
   * @return <code>mask.or(this.bitmask())</code> */
  @CheckReturnValue
  @NonNull
  @Nonnegative
  public default BigInteger union(@Nonnegative final BigInteger mask) {
    if (requireNonNull(mask).signum() == -1)
      throw new IllegalArgumentException("The mask must not be negative!");
    return mask.or(this.bitmask());
  }

  /** Creates a new EnumSet with <code>this</code> added.
   * 
   * @param set
   *          A set of enum elements.
   * @return A new {@link EnumSet} including all elements of the set and also <code>this</code>. */
  @SuppressFBWarnings("unchecked")
  @CheckReturnValue
  @NonNull
  public default BitSet union(final BitSet set) {
    final BitSet result = (BitSet) set.clone();
    result.set(((E) this).ordinal(), true);
    return result;
  }

  /** Creates an EnumBitSet containing this and all other elements.
   * <p>
   * Note that there are other ways to do this. The following expressions define the same set: <br>
   * <code>Planet.<b>EARTH</b>.union(Planet.<b>MARS</b>, Planet.<b>JUPITER</b>)<br>
   * Planet.of(Planet.<b>EARTH</b>, Planet.<b>MARS</b>, Planet.<b>JUPITER</b>)</code>
   * 
   * @see EnumSet#of(Enum)
   * @param set
   *          A list of elements to add.
   * @return Returns a new set containing <code>this</code> and all given elements. */
  @SuppressFBWarnings("unchecked")
  @CheckReturnValue
  @NonNull
  public default EnumBitSet<E> union(final E... set) {
    final EnumBitSet<E> result = EnumBitSet.just((E) this);
    result.addAll(asList(set));
    return result;
  }

  /** Takes the bitmasks of <code>this</code> and a clone of <code>mask</code>, then applies logical
   * OR. This results in a new bit set that also includes <code>this</code>.
   * 
   * @see #union(BigInteger)
   * @see #union(EnumSet)
   * @see #union(Enum...)
   * @param set
   *          A set of enum elements.
   * @return <code>mask.clone() | this</code> */
  @SuppressFBWarnings("unchecked")
  @CheckReturnValue
  @NonNull
  public default EnumBitSet<E> union(final EnumBitSet<E> set) {
    final EnumBitSet<E> clone = requireNonNull(set).clone();
    clone.add((E) this);
    return clone;
  }

  /** Creates a new EnumSet with <code>this</code> added.
   * 
   * @param set
   *          A set of enum elements.
   * @return A new {@link EnumSet} including all elements of the set and also <code>this</code>. */
  @SuppressFBWarnings("unchecked")
  @CheckReturnValue
  @NonNull
  public default EnumSet<E> union(final EnumSet<E> set) {
    final EnumSet<E> result = EnumSet.copyOf(requireNonNull(set));
    result.add((E) this);
    return result;
  }

  /** Creates a bit mask with <code>this</code> included.
   * 
   * @param mask
   *          A bit mask.
   * @throws MoreThan64ElementsException
   *           Thrown if the enum type contains more than 64 elements.
   * @return this.bitmask64() | mask; */
  @CheckReturnValue
  @NonNull
  public default long union(final long mask) throws MoreThan64ElementsException {
    return this.bitmask64() | mask;
  }

  /** This can be used to switch one bit in a bit mask.
   * 
   * @param mask
   *          A bit mask, must be positive.
   * @return <code>mask.xor(this.bitmask())</code> */
  @CheckReturnValue
  @NonNull
  @Nonnegative
  public default BigInteger xor(@Nonnegative final BigInteger mask) {
    if (requireNonNull(mask).signum() == -1)
      throw new IllegalArgumentException("The mask must not be negative!");
    return mask.xor(this.bitmask());
  }

  /** Removes or adds <tt>this</tt> to the given set. The operation is equivalent to switching one
   * bit in a bit mask.
   * 
   * @param set
   *          A set of enum elements.
   * @return <code>set.clone() XOR this</code> */
  @SuppressFBWarnings("unchecked")
  @CheckReturnValue
  @NonNull
  public default EnumBitSet<E> xor(final EnumBitSet<E> set) {
    final EnumBitSet<E> result = set.clone();
    if (set.contains(this))
      result.remove(this);
    else
      result.add((E) this);
    return result;
  }

}
