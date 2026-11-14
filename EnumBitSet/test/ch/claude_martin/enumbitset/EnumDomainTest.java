package ch.claude_martin.enumbitset;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

@SuppressWarnings("static-method")
public class EnumDomainTest {

  static enum Shape implements EnumBitSetHelper<Shape> {
    CIRCLE, TRIANGLE, SQUARE, STAR;
  }

  static enum City implements EnumBitSetHelper<City> {
    NEW_YORK, PARIS, LONDON, MADRID, TOKYO, BUENOS_AIRES, CAIRO, SYDNEY;
  }

  static final Domain<Shape> shapes = EnumDomain.of(Shape.class);
  static final Domain<City>  cities = EnumDomain.of(City.class);

  @Test
  public final void testAdd() {
    assertThrows(UnsupportedOperationException.class, () -> shapes.add(Shape.STAR));
    assertThrows(UnsupportedOperationException.class, () -> cities.add(City.PARIS));
  }

  @Test
  public final void testContainsObject() {
    assertFalse(shapes.contains(1));
    assertFalse(shapes.contains(City.PARIS));
    assertTrue(shapes.contains(Shape.TRIANGLE));
    for (final var c : cities)
      assertTrue(cities.contains(c));
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Test
  public final void testFactory() {
    for (final Domain d : asList(cities, shapes)) {
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
    for (final Domain d : asList(cities, shapes)) {
      final Object[] array = d.toArray();
      for (int i = 0; i < d.size(); i++)
        assertEquals(array[i], d.get(i));
    }
    assertThrows(IndexOutOfBoundsException.class, () -> cities.get(-1),
        "cities.get(-1) should fail");
    assertThrows(IndexOutOfBoundsException.class, () -> cities.get(cities.size()),
        "cities.get(domain123.size())  should fail");
  }

  @Test
  public final void testHashCode() {
    final var domain = DefaultDomain.of(City.class.getEnumConstants());
    assertEquals(domain.hashCode(), cities.hashCode());
    assertNotEquals(shapes.hashCode(), cities.hashCode());
  }
  
  @Test
  public final void testEquals() {
    final var domain = DefaultDomain.of(City.class.getEnumConstants());
    assertEquals(domain, cities);
    assertEquals(cities, domain);
    assertNotEquals(shapes, domain);
    assertNotEquals(domain, shapes);
    assertEquals(cities, cities);
    assertEquals(shapes, shapes);
  }
  
  @SuppressWarnings({ "rawtypes", "unlikely-arg-type" })
  @Test
  public final void testIndexOfObject() {
    for (final Domain d : asList(cities, shapes)) {
      final Object[] array = d.toArray();
      for (int i = 0; i < array.length; i++)
        assertEquals(i, d.indexOf(array[i]));
    }

    assertEquals(-1, cities.indexOf("foo"));
    assertEquals(-1, cities.indexOf(null));
  }

  @Test
  public final void testOf() {
    assertSame(cities, EnumBitSet.of(City.CAIRO, City.MADRID).getDomain());
    final var ac = cities.factory().apply(asList(City.PARIS, City.SYDNEY));
    assertSame(cities, ac.getDomain());
    assertThrows(NullPointerException.class, () -> EnumDomain.of((Class) null),
        "EnumDomain.of(null) should fail");
  }

  @Test
  public final void testSize() {
    assertEquals(City.class.getEnumConstants().length, cities.size());
    assertEquals(Shape.class.getEnumConstants().length, shapes.size());
  }

}
