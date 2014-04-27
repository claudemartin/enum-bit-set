package ch.claude_martin.enumbitset;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@Immutable
final class DefaultDomain<T> extends AbstractList<T> implements Domain<T> {
  // Array of all elements in the domain:
  @Nonnull
  private final T[]                 elements;
  // View of the array as a List:
  @Nonnull
  private final List<T>             list;
  // Lookup table: element->index
  @Nonnull
  private final HashMap<T, Integer> map;

  @SuppressFBWarnings(value = "JCIP_FIELD_ISNT_FINAL_IN_IMMUTABLE_CLASS", justification = "It's lazy.")
  private int                       hash;    // defaults to 0, later it's set to a hash code.

  /** The caller must make sure that the domain is a distinct collection with a well defined
   * iteration order (e.g. List, LinkedHashSet etc.).
   * 
   * @throws IllegalArgumentException
   *           if the given collections contains duplicates. */
  @SuppressWarnings("unchecked")
  public DefaultDomain(@Nonnull final Collection<? extends T> domain) {
    this.elements = (T[]) new Object[domain.size()];
    this.map = new HashMap<>((int) 1.5 * domain.size());
    this.list = asList(this.elements);
    int i = 0;
    for (final T t : domain) {
      this.elements[i] = requireNonNull(t);
      this.map.put(t, i);
      i++;
    }
    if (this.map.size() != domain.size())
      throw new IllegalArgumentException("The domain must not contain duplicates.");
  }

  @Override
  public boolean add(final T e) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean addAll(final Collection<? extends T> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException();
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
  public int indexOf(final Object o) {
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
  public boolean remove(final Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean removeAll(final Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean retainAll(final Collection<?> c) {
    throw new UnsupportedOperationException();
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
  public <X> X[] toArray(@Nonnull final X[] a) {
    return super.toArray(a);
  }
}