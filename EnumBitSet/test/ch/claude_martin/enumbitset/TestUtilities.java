package ch.claude_martin.enumbitset;

import static org.junit.jupiter.api.Assertions.fail;

// The tests should simply be migrated to JUnit 5, so this class is not needed.
public class TestUtilities {
  @FunctionalInterface
  public static interface Failable<E extends Throwable> {
    public void run() throws E;
  }

  /** This does the same as this pattern:
   * 
   * <pre>
   * <code>
   *   try {
   *         // do something that should fail...
   *         fail("......");
   *       } catch (final XYZException e) {
   *         // expected
   *       }</code>
   * </pre>
   * 
   * @param expected
   *          The expected {@link Exception}
   * @param msg
   *          The message explaining why it failed.
   * @param code
   *          the code to run */
  @SafeVarargs
  static <E extends Throwable> void expect(final Class<E> expected, final String msg,
      final Failable<E>... code) {
    for (final Failable<E> failable : code)
      try {
        failable.run();
        fail(msg);
      } catch (final Throwable e) {
        // expected
        if (!expected.isAssignableFrom(e.getClass()))
          throw new AssertionError("unexpected exception", e);
      }
  }

  /** NullPointerException */
  @SafeVarargs
  static void expectNPE(final String msg, final Failable<NullPointerException>... code) {
    expect(NullPointerException.class, msg, code);
  }

  /** IllegalArgumentException */
  @SafeVarargs
  static void expectIAE(final String msg, final Failable<IllegalArgumentException>... code) {
    expect(IllegalArgumentException.class, msg, code);
  }

  /** IndexOutOfBoundsException */
  @SafeVarargs
  static void expectIOOBE(final String msg, final Failable<IndexOutOfBoundsException>... code) {
    expect(IndexOutOfBoundsException.class, msg, code);
  }

  /** RuntimeException */
  @SafeVarargs
  static void expectRE(final String msg, final Failable<RuntimeException>... code) {
    expect(RuntimeException.class, msg, code);
  }

  /** MoreThan64ElementsException */
  @SafeVarargs
  static void expectMT64EE(final String msg, final Failable<MoreThan64ElementsException>... code) {
    expect(MoreThan64ElementsException.class, msg, code);
  }
}
