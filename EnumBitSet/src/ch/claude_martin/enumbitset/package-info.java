/**
 * <p>{@link ch.claude_martin.enumbitset.EnumBitSetHelper} allows to extend any
 * enum type with methods to work with sets and bit fields, while the class 
 * {@link ch.claude_martin.enumbitset.EnumBitSet} is an alternative for 
 * {@link java.util.EnumSet}, also containing more methods for set operations
 * and to work with bit fields.
 * 
 * <p>
 * Bit sets are by default of type {@link ch.claude_martin.enumbitset.EnumBitSet EnumBitSet}, 
 * but {@link java.math.BigInteger BigInteger},
 * {@link java.util.BitSet BitSet}, and {@link java.lang.Long long} 
 * can be used as well. Bit masks should not be stored into a database unless 
 * you can guarantee that no problematic changes are made in the future. 
 * The only changes that would not break your code is to add
 * new enums at the end of the list of existing enums and renaming without
 * changing the meaning of the value. Note that {@link java.util.EnumSet EnumSet} and
 * {@link java.math.BigInteger BigInteger} already has many methods to help using 
 * sets of enums and large integers.
 * 
 * <p>
 * A bit field is always used to store a set of bits. Each bit can represent one
 * enum value. Each enum has a distinct {@link ch.claude_martin.enumbitset.EnumBitSetHelper#bitmask() bit mask}.
 * 
 * <p>
 * Nomenclature:
 * <dl>
 * <dt><b>enum type</b></dt>
 * <dd>Any type that is an enum. I.e. it uses "enum" instead of "class" in its
 * declaration. <br>
 * Example: <code>Planet.class</code></dd>
 * <dt><b>enum</b></dt>
 * <dd>A field of an enum type. This is one single value of any enum type. 
 * To make a distinction between enum and enum type other terms such as 
 * enum element or enum constant are used.<br>
 * Example: Planet.VENUS</dd>
 * <dt><b>bit field</b></dt>
 * <dd>A field whose value represents a set of values. A database may contain a
 * single integer type field, which is used as a bit field.</dd>
 * <dt><b>bit set</b></dt>
 * <dd>An integer value that represents a set of values. For each containing
 * value a bit is set to 1.</dd>
 * <dt><b>bit mask</b></dt>
 * <dd>A value like a bit set. The mask is used for bitwise operations on a bit
 * set.</dd> </dl>
 * <p>
 * The goal of this interface is to allow the use of existing types. Since all
 * primitive types are limited this uses {@link java.math.BigInteger BigInteger} 
 * instead. The type {@link ch.claude_martin.enumbitset.EnumBitSet EnumBitSet} 
 * combines the features of such types and can be used instead.
 * 
 * <p>
 * Naming conventions: <br>
 * Commonly used terms from set theory are preferred. So <code>union</code> is
 * used instead of <code>AND</code>, <code>&amp;</code>, <code>&#x2227;</code>
 * etc. <br>
 * 
 * <style>.tbl {text-align:center;}</style>
 * <table border="1" class="tbl"><caption>Table of set operations</caption>
 * <tr>
 * <th>name</th>
 * <th>set</th>
 * <th>logic</th>
 * <th>binary*</th>
 * <th>example</th>
 * <th>explanation</th>
 * </tr>
 * <tr>
 * <td>union</td>
 * <td>A &#x222a; B</td>
 * <td>A &#x2228; B</td>
 * <td>A | B</td>
 * <td>{1,2,3} &#x222a; {3,4,5} = {1,2,3,4,5}</td>
 * <td>All elements that are in either A or B.</td>
 * </tr>
 * <tr>
 * <td>intersect</td>
 * <td>A &#x2229; B</td>
 * <td>A &#x2227; B</td>
 * <td>A &amp; B</td>
 * <td>{1,2,3} &#x2229; {3,4,5} = {3}</td>
 * <td>All elements that are in both A and B.</td>
 * </tr>
 * <tr>
 * <td>minus</td>
 * <td>A &#x2216; B</td>
 * <td>A &#x2227; &#x00AC;B</td>
 * <td>A &amp; ~B</td>
 * <td>{1,2,3} &#x2216; {3,4,5} = {1,2}</td>
 * <td>All elements that are in A, but not in B.</td>
 * </tr>
 * <tr>
 * <td>complement</td>
 * <td>A<sup>C</sup></td>
 * <td>&#x00AC;A</td>
 * <td><span title="U = Mask for set with all elements.">U</span> &amp; ~A</td>
 * <td>A<sup>C</sup> = <span title="Universe = All elements of the enum type.">Universe</span> &#x2216; A</td>
 * <td>All elements that are not in A.</td>
 * </tr>
 * </table>
 * * bitwise operators, 
 * <a 
 * 		href="http://docs.oracle.com/javase/specs/jls/se7/html/jls-15.html#jls-15.22"
 * 		>as used in Java</a>.
 * 
 * <p>Method names:
 * Methods named "to<i>XY</i>" create a new object of type XY that represents 
 * the same set on which it is invoked. toString returns the default string 
 * representation of and equal {@link java.util.EnumSet}.
 * Methods named "as<i>XY</i>" are static and take a set to convert it into a 
 * new set. 
 * Many methods of EnumBitSetHelper treat an enum constant as if it were a set 
 * with just its own value set. Conversion to a set will return a new set 
 * containing just one single value.
 * In stead of <i>toBigInteger</i> there is 
 * {@link ch.claude_martin.enumbitset.EnumBitSetHelper#bitmask() bitmask()}.
 * <br>
 * All these methods create a new data structure. Only the methods of 
 * {@link java.util.Set} allow modification of the EnumBitSet. Enum elements can 
 * not be modified by the methods of {@link ch.claude_martin.enumbitset.EnumBitSetHelper}.   
 * 
 * <p>
 * You can use database fields with less than 64 bits. Just make sure you use an
 * unsigned type (if your database supports this, note that standard-SQL does
 * not). Your DB Access Library (e.g. JDBC) should handle the type conversions
 * correctly for you. I do recommend using 64 bits in the first place as you
 * don't have much benefit from saving some bits. To use fewer bits you can it 
 * convert like this:<br>
 * <code>byte b = (byte) (myEnumBitSet.toLong() &amp; 0xFF);</code> 
 * <br>
 * Invoking a method that uses 64 bit on an enum type with more than 64 elements 
 * will throw a {@link ch.claude_martin.enumbitset.MoreThan64ElementsException}.
 * 
 * @author <a href="http://claude-martin.ch/enumbitset/">Copyright &copy; 2014 Claude Martin</a>
 */
package ch.claude_martin.enumbitset;

