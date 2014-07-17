package ch.claude_martin.enumbitset;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.stream.Collectors;

import org.junit.Test;

@SuppressWarnings("static-method")
public class DefaultDomainTest {

  static final DefaultDomain<Integer>   domain123 = DefaultDomain.of(asList(1, 2, 3));       ;
  static final DefaultDomain<Character> domainABC = DefaultDomain.of(asList('A', 'B', 'C')); ;

  @Test(expected = UnsupportedOperationException.class)
  public final void testAdd() {
    domain123.add(666);
  }

  @Test
  public final void testContainsObject() {
    assertTrue(domain123.contains(1));
    assertTrue(domain123.contains(2));
    assertTrue(domain123.contains(3));
    for (final Character c : domainABC)
      assertTrue(domainABC.contains(c));
    assertFalse(domain123.contains(4));
    assertFalse(domainABC.contains('a'));
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Test
  public final void testFactory() {
    for (final DefaultDomain d : asList(domain123, domainABC)) {
      final DomainBitSet clone = (DomainBitSet) d.factory().apply(
          d.stream().collect(Collectors.toList()));
      assertSame(d, clone.getDomain());
      assertEquals(SmallDomainBitSet.allOf(d), clone);

      final DomainBitSet empty = (DomainBitSet) d.factory().apply(Collections.emptyList());
      assertSame(d, empty.getDomain());
      assertTrue(empty.isEmpty());

      try {
        d.factory().apply(null);
        fail("d.factory().apply(null) should fail");
      } catch (final Exception e) {
        // expected
      }

      try {
        d.factory().apply("foo");
        fail("d.factory().apply(\"foo\") should fail");
      } catch (final Exception e) {
        // expected
      }
    }

  }

  @SuppressWarnings({ "rawtypes" })
  @Test
  public final void testGetInt() {
    for (final DefaultDomain d : asList(domain123, domainABC)) {
      final Object[] array = d.toArray();
      for (int i = 0; i < d.size(); i++) {
        assertEquals(array[i], d.get(i));
      }
    }
    try {
      domain123.get(-1);
      fail("domain123.get(-1) should fail");
    } catch (final IndexOutOfBoundsException e) {
      // expected
    }
    try {
      domain123.get(domain123.size());
      fail("domain123.get(domain123.size()) should fail");
    } catch (final IndexOutOfBoundsException e) {
      // expected
    }
  }

  @Test
  public final void testHashCode() {
    final DefaultDomain<Integer> d123 = DefaultDomain.of(asList(1, 2, 3));
    assertEquals(domain123.hashCode(), d123.hashCode());
    assertNotEquals(domain123.hashCode(), domainABC.hashCode());
  }

  @SuppressWarnings({ "rawtypes" })
  @Test
  public final void testIndexOfObject() {
    for (final DefaultDomain d : asList(domain123, domainABC)) {
      final Object[] array = d.toArray();
      for (int i = 0; i < array.length; i++) {
        assertEquals(i, d.indexOf(array[i]));
      }
    }

    assertEquals(-1, domain123.indexOf("foo"));
    assertEquals(-1, domain123.indexOf(null));
  }

  @Test
  public final void testOf() {
    final DomainBitSet<Character> ac = domainABC.factory().apply(asList('A', 'C'));
    assertSame(domainABC, DefaultDomain.of(GeneralDomainBitSet.of(domainABC, ac.toSet())));
    assertSame(ac.getDomain(), DefaultDomain.of(ac.getDomain()));

    try {
      DefaultDomain.of(null);
      fail("DefaultDomain.of(null) should fail");
    } catch (final NullPointerException e) {
      // expected
    }

    try {
      DefaultDomain.of(asList(1, 1, 1));
      fail("DefaultDomain.of([1,1,1]) should fail");
    } catch (final IllegalArgumentException e) {
      // expected
    }
  }

  @Test
  public final void testSize() {
    assertEquals(3, domain123.size());
    assertEquals(3, domainABC.size());
  }

}
