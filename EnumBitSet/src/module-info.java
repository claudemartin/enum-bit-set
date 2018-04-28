module ch.claude_martin.enumbitset {
  requires java.base;
// To run tests in Eclipse you must require the junit-module.
// This should be fixed, but I don't know how.
// static means it requires it for compilation, but not for running it.
  requires static org.junit.jupiter.api; 
  exports ch.claude_martin.enumbitset;
}