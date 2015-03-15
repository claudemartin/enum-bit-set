# What this project provides:
* The EnumBitSet is a data structure similar to EnumSet with more methods.
* The EnumBitSetHelper interface allows to work directly on the enum constants.
* The DomainBitSet is the interface implemented by EnumBitSet.

# Set operations:
* `intersect`
* `union`
* `minus` (relative complement)
* `complement`
* `cross` (Cartesian product)
* `semijoin` (`cross` with filter)
* `powerset`
* `map` elements to different domain
* `zipWithPosition` 

# Documentation:
All is documented and the ant build file contains a target "javadoc". Just run it, read the documentation directly in the source code, or read it [online](https://web.archive.org/web/20140624175002/http://claude-martin.ch/enumbitset/doc/).

# Java 8 and newer!!
Note that you can only use this with Java 8 and newer releases of Java.

# Multi Paradigm
You can use the EnumBitSets like the EnumSets in Java. EnumBitSet is a mutable data structure with an interface you will recognize.

But it also supports a more functional approach, where a new set is returned for each operation. Use only those methods and the data structure can be used in a concurrent system. Those methods are thread-safe because they do not change any state.

# Project website:
http://claude-martin.ch/enumbitset/

# Example code:
```java

// Examle enum type:
private static enum Permission implements EnumBitSetHelper<Permission> {
        READ, WRITE, EXECUTE, DELETE;
}

// Example code:

final EnumBitSet<Permission> permissions = bob.getPermissions();
// Add element to EnumBitSet:
permissions.add(Permission.READ);

// Check EnumBitSet:
if (Permission.WRITE.elementOf(permissions))
        Files.write(path, bytes);

// Functional Programming:

// Assume we have some users with permissions:
final List<User> users = getAllUsers();

// Get users with permission to DELETE:
final Stream<User> canDelete = users.stream().filter(u -> u.getPermissions().contains(Permission.DELETE));
// See what the group can do:
final Optional<EnumBitSet<Permission>> union = 
                canDelete.<EnumBitSet<Permission>> map(u -> u.getPermissions()).reduce(EnumBitSet<Permission>::union);
union.ifPresent(System.out::println); // [READ, WRITE, DELETE]
```
Note: If you think this code looks weird and not like Java then it's because Java 8 has new ways of working with Collections. This project provides a classic Java API and you can use conventional Java practices.

# Outline of EnumBitSetHelper:
See the javadoc of [EnumBitSetHelper](https://web.archive.org/web/20140624175002/http://claude-martin.ch/enumbitset/doc/index.html?ch/claude_martin/enumbitset/EnumBitSetHelper.html) to see what the interface offers.
