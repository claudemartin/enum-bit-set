package ch.claude_martin.enumbitset;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

@SuppressWarnings("static-method")
public class DefaultDomainTest {

  static final DefaultDomain<Integer>   domain123 = DefaultDomain.of(asList(1, 2, 3));;
  static final DefaultDomain<Character> domainABC = DefaultDomain.of(asList('A', 'B', 'C'));;

  @Test
  public final void testAdd() {
    assertThrows(UnsupportedOperationException.class, () -> domain123.add(666));
  }

  @Test
  public final void testContainsObject() {
    assertTrue(domain123.contains(1));
    assertTrue(domain123.contains(2));
    assertTrue(domain123.contains(3));
    for (final var c : domainABC)
      assertTrue(domainABC.contains(c));
    assertFalse(domain123.contains(4));
    assertFalse(domainABC.contains('a'));
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Test
  public final void testFactory() {
    for (final DefaultDomain d : asList(domain123, domainABC)) {
      final DomainBitSet clone = (DomainBitSet) d.factory()
          .apply(d.stream().collect(Collectors.toList()));
      assertSame(d, clone.getDomain());
      assertEquals(SmallDomainBitSet.allOf(d), clone);

      final DomainBitSet empty = (DomainBitSet) d.factory().apply(Collections.emptyList());
      assertSame(d, empty.getDomain());
      assertTrue(empty.isEmpty());

      assertThrows(Exception.class, () -> d.factory().apply(null),
          "d.factory().apply(null) should fail");
      assertThrows(Exception.class, () -> d.factory().apply("foo"),
          "d.factory().apply(\"foo\") should fail");

    }

  }

  @SuppressWarnings({ "rawtypes" })
  @Test
  public final void testGetInt() {
    for (final DefaultDomain d : asList(domain123, domainABC)) {
      final Object[] array = d.toArray();
      for (int i = 0; i < d.size(); i++)
        assertEquals(array[i], d.get(i));
    }
    assertThrows(IndexOutOfBoundsException.class, () -> domain123.get(-1),
        "domain123.get(-1) should fail");
    assertThrows(IndexOutOfBoundsException.class, () -> domain123.get(domain123.size()),
        "domain123.get(domain123.size())  should fail");
  }

  @Test
  public final void testHashCode() {
    final var d123 = DefaultDomain.of(asList(1, 2, 3));
    assertEquals(domain123.hashCode(), d123.hashCode());
    assertNotEquals(domain123.hashCode(), domainABC.hashCode());
  }

  @SuppressWarnings({ "rawtypes", "unlikely-arg-type" })
  @Test
  public final void testIndexOfObject() {
    for (final DefaultDomain d : asList(domain123, domainABC)) {
      final Object[] array = d.toArray();
      for (int i = 0; i < array.length; i++)
        assertEquals(i, d.indexOf(array[i]));
    }

    assertEquals(-1, domain123.indexOf("foo"));
    assertEquals(-1, domain123.indexOf(null));
  }

  @Test
  public final void testOf() {
    assertSame(domainABC, DefaultDomain.of(GeneralDomainBitSet.of(domainABC, domainABC)));
    final var ac = domainABC.factory().apply(asList('A', 'C'));
    assertSame(domainABC, ac.getDomain());
    assertSame(ac.getDomain(), DefaultDomain.of(ac.getDomain()));
    assertThrows(NullPointerException.class, () -> DefaultDomain.of((Collection<?>) null),
        "DefaultDomain.of(null) should fail");
    assertThrows(IllegalArgumentException.class, () -> DefaultDomain.of(asList(1, 1, 1)),
        "DefaultDomain.of([1,1,1]) should fail");
  }

  @Test
  public final void testSize() {
    assertEquals(3, domain123.size());
    assertEquals(3, domainABC.size());
  }

}
