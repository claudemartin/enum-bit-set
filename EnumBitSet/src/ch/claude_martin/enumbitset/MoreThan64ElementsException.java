package ch.claude_martin.enumbitset;

/**
 * Unchecked exception for any method that uses a bit set of type long (64 bits)
 * even though the enum type contains more than 64 elements.
 * 
 * @author <a href="http://claude-martin.ch/enumbitset/">Claude Martin</a>
 */
public class MoreThan64ElementsException extends RuntimeException {
	private static final long serialVersionUID = -3153017224720289309L;
	private final Class<? extends EnumBitSetHelper<?>> enumType;

	public MoreThan64ElementsException(final Class<? extends EnumBitSetHelper<?>> type) {
		super("MoreThan64ElementsException for Enum " + type.getSimpleName());
		this.enumType = type;
	}

	public Class<? extends EnumBitSetHelper<?>> getEnumType() {
		return this.enumType;
	}
}
