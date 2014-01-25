package ch.claude_martin.enumbitset;

/**
 * 
 * Unchecked exception for any method that uses a bit set of type long (64 bits)
 * even though the enum type contains more than 64 elements.
 * 
 * <p>
 * It is recommended that a static block with an assertion is included to any
 * enum type that is not indented to contain more than 64 elements:
 * 
 * <pre><code>
 * public static enum Suit implements EnumBitSetHelper&lt;Suit&gt; {
 *   CLUBS, DIAMONDS, HEARTS, SPADES;
 *   static {
 *     assert values().length &lt;= 64 : "This enum type is not indented to contain more than 64 constants.";
 *   }
 * }
 * </code></pre>
 * 
 * @author <a href="http://claude-martin.ch/enumbitset/">Copyright &copy; 2014
 *         Claude Martin</a>
 */
public class MoreThan64ElementsException extends RuntimeException {
	private static final long serialVersionUID = -3153017224720289309L;
	private final Class<? extends EnumBitSetHelper<?>> enumType;

	MoreThan64ElementsException(final Class<? extends EnumBitSetHelper<?>> type) {
		// "type" must not be null! But let's be sure this wouldn't fail for null.
		super("Enum " + (type == null ? "<unknown>" : type.getSimpleName()) + " contains more than 64 elements.");
		this.enumType = type;
	}

	public Class<? extends EnumBitSetHelper<?>> getEnumType() {
		return this.enumType;
	}
}
