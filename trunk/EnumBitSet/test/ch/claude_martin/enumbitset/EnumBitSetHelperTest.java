package ch.claude_martin.enumbitset;

import static ch.claude_martin.enumbitset.TestUtilities.*;
import static org.junit.Assert.*;

import java.math.BigInteger;
import java.util.BitSet;
import java.util.Collections;
import java.util.EnumSet;

import org.junit.Test;

import com.sun.org.apache.xml.internal.serialize.ElementState;

import ch.claude_martin.enumbitset.EnumBitSetTest.Alphabet;
import ch.claude_martin.enumbitset.EnumBitSetTest.Element;
import ch.claude_martin.enumbitset.EnumBitSetTest.Planet;

@SuppressWarnings("static-method")
public class EnumBitSetHelperTest {

  @Test
  public final void testBitmask() {
    for (Planet p : Planet.class.getEnumConstants())
      assertEquals(1L << p.ordinal(), p.bitmask().longValue());
    for (Element e : Element.class.getEnumConstants())
      assertEquals(BigInteger.ONE, e.bitmask().shiftRight(e.ordinal()));
  }

  @Test
  public final void testBitmask64() {
    for (Planet p : Planet.class.getEnumConstants())
      assertEquals(1L << p.ordinal(), p.bitmask64());
    expectMT64EE("Elements>64", () -> Element.R.bitmask64());
  }

  @Test
  public final void testElementOf() {
    EnumBitSet<Planet> planets = EnumBitSet.allOf(Planet.class);
    Planet[] empty = new Planet[0];
    for (Planet p : planets) {
      assertTrue(p.elementOf(planets.toBigInteger()));
      assertFalse(p.elementOf(BigInteger.ZERO));
      assertTrue(p.elementOf(planets.toBitSet()));
      assertFalse(p.elementOf(new BitSet()));
      assertTrue(p.elementOf(planets.toSet()));
      assertFalse(p.elementOf(Collections.emptySet()));
      assertTrue(p.elementOf(planets));
      assertFalse(p.elementOf(EnumBitSet.noneOf(Planet.class)));
      assertTrue(p.elementOf(planets.toArray(empty)));
      assertFalse(p.elementOf(empty));
      assertTrue(p.elementOf(planets.toLong()));
      assertFalse(p.elementOf(0L));
    }
  }

  @Test
  public final void testIntersect() {
    EnumBitSet<Planet> planets = EnumBitSet.allOf(Planet.class);
    EnumBitSet<Planet> none = EnumBitSet.noneOf(Planet.class);
    for (Planet p : planets) {
      assertEquals(p.toBigInteger(), p.intersect(planets.toBigInteger()));
      assertEquals(none.toBigInteger(), p.intersect(none.toBigInteger()));
      assertEquals(p.toBitSet(), p.intersect(planets.toBitSet()));
      assertEquals(none.toBitSet(), p.intersect(none.toBitSet()));
      assertEquals(p.toEnumBitSet(), p.intersect(planets));
      assertEquals(none, p.intersect(none));
      assertEquals(p.toEnumBitSet(), p.intersect(planets.toArray(new Planet[0])));
      assertEquals(none, p.intersect(none.toArray(new Planet[0])));
      assertEquals(p.toLong(), p.intersect(planets.toLong()));
      assertEquals(none.toLong(), p.intersect(none.toLong()));
    }
  }

  @Test
  public final void testOthers() {
    for (Planet p : Planet.class.getEnumConstants())
      assertEquals(p.others(), EnumBitSet.allOf(Planet.class).minusVarArgs(p));
    for (Element e : Element.class.getEnumConstants())
      assertEquals(e.others(), EnumBitSet.allOf(Element.class).minusVarArgs(e));
  }

  @Test
  public final void testRemovedFrom() {
    final EnumBitSet<Element> elements = EnumBitSet.allOf(Element.class);
    for (Element e : elements) {
      final EnumBitSet<Element> expected = elements.minusVarArgs(e);
      assertEquals(expected.toBigInteger(), e.removedFrom(elements.toBigInteger()));
      assertEquals(expected.toBitSet(), e.removedFrom(elements.toBitSet()));
      assertEquals(expected, e.removedFrom(elements));
      assertEquals(expected, e.removedFrom(elements.toArray(new Element[0])));
      assertEquals(expected.toEnumSet(), e.removedFrom(elements.toEnumSet()));
    }

    final EnumBitSet<Planet> planets = EnumBitSet.allOf(Planet.class);
    for (Planet p : planets) {
      final EnumBitSet<Planet> expected = planets.minusVarArgs(p);
      assertEquals(expected.toLong(), p.removedFrom(planets.toLong()));
    }

    expectIAE("negative BigInteger", () -> Element.H.removedFrom(BigInteger.valueOf(-1)));

    expectMT64EE("R>64", () -> Element.R.removedFrom(123));

    expectNPE("null", () -> Element.H.removedFrom((BigInteger) null),//
        () -> Element.H.removedFrom((BitSet) null),//
        () -> Element.H.removedFrom((EnumBitSet<Element>) null),//
        () -> Element.H.removedFrom((Element[]) null), //
        () -> Element.H.removedFrom((EnumSet<Element>) null));
  }

  @Test
  public final void testToBigInteger() {
    for (Planet p : Planet.class.getEnumConstants())
      assertEquals(EnumBitSet.just(p).toBigInteger(), p.toBigInteger());
    for (Element e : Element.class.getEnumConstants())
      assertEquals(EnumBitSet.just(e).toBigInteger(), e.toBigInteger());
  }

  @Test
  public final void testToBitSet() {
    for (Planet p : Planet.class.getEnumConstants())
      assertEquals(EnumBitSet.just(p).toBitSet(), p.toBitSet());
    for (Element e : Element.class.getEnumConstants())
      assertEquals(EnumBitSet.just(e).toBitSet(), e.toBitSet());
  }

  @Test
  public final void testToEnumBitSet() {
    for (Planet p : Planet.class.getEnumConstants())
      assertEquals(EnumBitSet.just(p), p.toEnumBitSet());
    for (Element e : Element.class.getEnumConstants())
      assertEquals(EnumBitSet.just(e), e.toEnumBitSet());
  }

  @Test
  public final void testToEnumSet() {
    for (Planet p : Planet.class.getEnumConstants())
      assertEquals(EnumBitSet.just(p).toEnumSet(), p.toEnumSet());
    for (Element e : Element.class.getEnumConstants())
      assertEquals(EnumBitSet.just(e).toEnumSet(), e.toEnumSet());
  }

  @Test
  public final void testToLong() {
    for (Planet p : Planet.class.getEnumConstants())
      assertEquals(EnumBitSet.just(p).toLong(), p.toLong());
    expectMT64EE("R>64", () -> Element.R.toLong());
  }

  @Test
  public final void testUnion() {

    EnumBitSet<Planet> planets = EnumBitSet.allOf(Planet.class);
    EnumBitSet<Planet> none = EnumBitSet.noneOf(Planet.class);
    for (Planet p : planets) {
      EnumBitSet<Planet> just = EnumBitSet.just(p);
      assertEquals(planets.toBigInteger(), p.union(planets.toBigInteger()));
      assertEquals(just.toBigInteger(), p.union(none.toBigInteger()));
      assertEquals(planets.toBitSet(), p.union(planets.toBitSet()));
      assertEquals(just.toBitSet(), p.union(none.toBitSet()));
      assertEquals(planets, p.union(planets));
      assertEquals(just, p.union(none));
      assertEquals(planets, p.union(planets.toArray(new Planet[0])));
      assertEquals(just, p.union(none.toArray(new Planet[0])));
      assertEquals(planets.toLong(), p.union(planets.toLong()));
      assertEquals(just.toLong(), p.union(none.toLong()));
    }

  }

  @Test
  public final void testXor() {

    EnumBitSet<Planet> planets = EnumBitSet.allOf(Planet.class);
    BigInteger bi = planets.toBigInteger();
    EnumBitSet<Planet> none = EnumBitSet.noneOf(Planet.class);
    for (Planet p : planets) {
      EnumBitSet<Planet> just = EnumBitSet.just(p);
      assertEquals(p.removedFrom(bi), p.xor(bi));
      assertEquals(just.toBigInteger(), p.xor(none.toBigInteger()));
      assertEquals(p.removedFrom(planets), p.xor(planets));
      assertEquals(just, p.xor(none));
    }

    expectIAE("negative BigInteger", () -> Element.H.xor(BigInteger.valueOf(-1)));
  }

}
