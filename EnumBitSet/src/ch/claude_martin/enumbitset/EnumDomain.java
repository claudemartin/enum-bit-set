package ch.claude_martin.enumbitset;

import static java.util.Arrays.asList;

import java.lang.ref.SoftReference;
import java.util.*;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

@Immutable
final class EnumDomain<E extends Enum<E> & EnumBitSetHelper<E>> extends AbstractList<E> implements
    Domain<E> {
  private static final long serialVersionUID = 3225868883383217275L;
  
  // Array of all elements in the domain:
  private final E[]           elements;                     // index == ordinal
  private final int           hash;
  private final Class<E>      enumType;

  private static final//      domainCache:
    Map<
      Class<? extends Enum<?>>, // Maps Enum-Type to Domain
      SoftReference<// Allows GC to collect unused Domains
        Domain<? extends Enum<?>>>>
                              domainCache = new IdentityHashMap<>();

  @SuppressWarnings("unchecked")
  static <X extends Enum<X> & EnumBitSetHelper<X>> Domain<X> of(final Class<X> enumType) {
    synchronized (domainCache) {
      Domain<X> domain = null;
      final SoftReference<Domain<? extends Enum<?>>> ref = domainCache.get(enumType);
      if (ref != null)
        domain = (Domain<X>) ref.get();
      if (null == domain) {
        domain = new EnumDomain<>(enumType);
        domainCache.put(enumType, new SoftReference<Domain<? extends Enum<?>>>(domain));
      }
      return domain;
    }
  }

  private EnumDomain(final Class<E> enumType) {
    this.enumType = enumType;
    this.elements = enumType.getEnumConstants();
    this.hash = Arrays.hashCode(this.elements);
  }

  @Override
  public boolean add(final E e) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void add(final int index, final E element) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean contains(final Object o) {
    if (o == null)
      return false;
    return o.getClass().getDeclaringClass() == this.getEnumType();
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null || !(obj instanceof Domain) || this.hashCode() != obj.hashCode())
      return false;
    if (obj instanceof EnumDomain) {
      final EnumDomain<?> ed = (EnumDomain<?>) obj;
      return this.getEnumType() == ed.getEnumType();
    }
    return Arrays.equals(this.elements, ((Domain<?>) obj).toArray());
  }
  
  /**
   * {@inheritDoc}
   * <p>Note that this always creates {@link EnumBitSet}s.
   */
  @Override
  public Function<Collection<E>, DomainBitSet<E>> factory() {
    return (s) -> EnumBitSet.asEnumBitSet(s, this.getEnumType());
  }

  @Override
  public E get(final int index) {
    return this.elements[index];
  }

  Class<E> getEnumType() {
    return this.enumType;
  }

  @Override
  public int hashCode() {
    return this.hash;
  }

  @Override
  public int indexOf(final Object o) {
    if (o instanceof Enum)
      return ((Enum<?>) o).ordinal();
    return -1;
  }

  @Override
  public boolean isEmpty() {
    return this.elements.length == 0;
  }

  @Override
  public Iterator<E> iterator() {
    return asList(this.elements).iterator();
  }

  @Override
  public int lastIndexOf(final Object o) {
    return this.indexOf(o);
  }

  @Override
  public int size() {
    return this.elements.length;
  }

  @Override
  public Object[] toArray() {
    return this.elements.clone();
  }

  @Override
  public <X> X[] toArray(final X[] a) {
    return super.toArray(a);
  }

  /** This proxy class is used to serialize EnumDomain instances. */
  private static class SerializationProxy<E extends Enum<E> & EnumBitSetHelper<E>> implements
      java.io.Serializable {
    private static final long serialVersionUID = 6062818650132433646L;
    
    private final Class<E>    enumType;

    public SerializationProxy(@Nonnull final Class<E> enumType) {
      this.enumType = enumType;
    }

    private Object readResolve() {
      return EnumDomain.of(this.enumType);
    }
  }

  private Object writeReplace() {
    return new SerializationProxy<>(this.enumType);
  }

  @SuppressWarnings({ "static-method", "unused" })
  private void readObject(java.io.ObjectInputStream stream) throws java.io.InvalidObjectException {
    throw new java.io.InvalidObjectException("Proxy required");
  }
}
