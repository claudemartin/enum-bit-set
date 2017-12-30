package ch.claude_martin.enumbitset;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Function;

import com.sun.istack.internal.Nullable;

import ch.claude_martin.enumbitset.annotations.DefaultAnnotationForParameters;
import ch.claude_martin.enumbitset.annotations.Immutable;
import ch.claude_martin.enumbitset.annotations.NonNull;
import ch.claude_martin.enumbitset.annotations.SuppressFBWarnings;

/** Default implementation of {@link Domain}.
 * <p>
 * This is used by {@link SmallDomainBitSet} and {@link GeneralDomainBitSet}, while
 * {@link EnumBitSet} uses a specialized implementation.
 * 
 * @param <T>
 *          A type that all elements in the domain share.
 * 
 * @author <a href="http://claude-martin.ch/enumbitset/">Copyright &copy; 2014 Claude Martin</a> */
@Immutable
@DefaultAnnotationForParameters({ NonNull.class })
final class DefaultDomain<T> extends AbstractList<T> implements Domain<T> {
  private static final long          serialVersionUID = -1159105301120332006L;

  @SuppressFBWarnings({ "unchecked", "rawtypes" })
  private static final DefaultDomain EMPTY_DOMAIN     = new DefaultDomain(Collections.EMPTY_LIST);

  /** Returns a Domain of the given elements.
   * <p>
   * The given collection can be a DefaultDomain, in which case it is returned directly. If it is a
   * DomainBitSet with a DefaultDomain, then its existing domain is returned. In any other case a
   * new instance is created.
   * <p>
   * The caller must make sure that the domain is a distinct collection with a well defined
   * iteration order (e.g. List, LinkedHashSet etc.).
   * 
   * @throws IllegalArgumentException
   *           if the given collections contains duplicates.
   * @return Domain of the given elements. */
  @SuppressFBWarnings("unchecked")
  @NonNull
  public static <T> DefaultDomain<T> of(final Collection<? extends T> domain) {
    requireNonNull(domain, "domain");

    if (domain instanceof DefaultDomain)
      return (DefaultDomain<T>) domain;

    if (domain.isEmpty())
      return EMPTY_DOMAIN;

    if (domain instanceof DomainBitSet) {
      final Domain<T> domain2 = ((DomainBitSet<T>) domain).getDomain();
      if (domain2 instanceof DefaultDomain && domain.size() == domain2.size())
        return (DefaultDomain<T>) domain2;
    }

    return new DefaultDomain<>(domain);
  }

  /** Internal use only! */
  @SafeVarargs
  @NonNull
  static <T> DefaultDomain<T> of(final T... domain) {
    requireNonNull(domain, "domain");

    if (domain.length == 0)
      return EMPTY_DOMAIN;

    return new DefaultDomain<>(domain);
  }

  // Array of all elements in the domain:
  @NonNull
  private final T[]                 elements;
  // View of the array as a List:
  @NonNull
  private final List<T>             list;

  // Lookup table: element->index
  @NonNull
  private final HashMap<T, Integer> map;

  @SuppressFBWarnings(value = "JCIP_FIELD_ISNT_FINAL_IN_IMMUTABLE_CLASS", justification = "It's lazy.")
  private int                       hash;    // defaults to 0, later it's set to a hash code.

  /** The caller must make sure that the domain is a distinct collection with a well defined
   * iteration order (e.g. List, LinkedHashSet etc.).
   * 
   * @throws IllegalArgumentException
   *           if the given collections contains duplicates. */
  @SuppressFBWarnings("unchecked")
  private DefaultDomain(@NonNull final Collection<? extends T> domain)
      throws IllegalArgumentException {
    this(domain.toArray((T[]) new Object[requireNonNull(domain, "domain").size()]));
  }

  /** Internal use only!
   * 
   * @param domain
   *          The elements of the domain. */
  @SafeVarargs
  private DefaultDomain(final T... domain) {
    this.elements = requireNonNull(domain, "domain");
    this.map = new HashMap<>((int) (1.5 * domain.length));
    this.list = asList(domain);
    int i = 0;
    for (final T t : domain) {
      this.elements[i] = requireNonNull(t);
      this.map.put(t, i);
      i++;
    }
    if (this.map.size() != domain.length)
      throw new IllegalArgumentException("The domain must not contain duplicates.");
  }

  @Override
  public boolean contains(final Object obj) {
    return this.map.containsKey(obj);
  }

  @Override
  public boolean containsAll(final Collection<?> c) {
    return this.map.keySet().containsAll(c);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null || !(obj instanceof Domain) || this.hashCode() != obj.hashCode())
      return false;
    if (obj instanceof DefaultDomain)
      return Arrays.equals(this.elements, ((DefaultDomain<?>) obj).elements);
    return Arrays.equals(this.elements, ((Domain<?>) obj).toArray());
  }

  @Override
  public Function<Collection<T>, DomainBitSet<T>> factory() {
    if (this.size() <= 64)
      return (s) -> SmallDomainBitSet.of(this, s);
    return (s) -> GeneralDomainBitSet.of(this, s);
  }

  @Override
  public T get(final int i) {
    return this.elements[i];
  }

  @Override
  public int hashCode() {
    if (this.hash == 0)
      this.hash = Arrays.hashCode(this.elements);
    return this.hash;
  }

  @Override
  public int indexOf(@Nullable final Object o) {
    final Integer index = this.map.get(o);
    if (index == null)
      return -1;
    assert this.elements[index].equals(o);
    return index;
  }

  @Override
  public boolean isEmpty() {
    return this.elements.length == 0;
  }

  @Override
  public Iterator<T> iterator() {
    return this.list.iterator();
  }

  @Override
  public int size() {
    return this.elements.length;
  }

  @Override
  public Spliterator<T> spliterator() {
    return Domain.super.spliterator();
  }

  @Override
  public Object[] toArray() {
    return this.elements.clone();
  }

  @Override
  public <X> X[] toArray(final X[] a) {
    return super.toArray(requireNonNull(a, "a"));
  }

  /** This proxy class is used to serialize DefaultDomain instances. */
  private static class SerializationProxy<T> implements java.io.Serializable {
    private static final long serialVersionUID = -7898910202865145301L;
    @NonNull
    private final T[]         elements;

    public SerializationProxy(@NonNull final T[] elements) {
      this.elements = elements;
    }

    private Object readResolve() {
      return DefaultDomain.of(this.elements);
    }

  }

  private Object writeReplace() {
    return new SerializationProxy<>(this.elements);
  }

  @SuppressFBWarnings({ "static-method", "unused" })
  private void readObject(final java.io.ObjectInputStream stream)
      throws java.io.InvalidObjectException {
    throw new java.io.InvalidObjectException("Proxy required");
  }

}