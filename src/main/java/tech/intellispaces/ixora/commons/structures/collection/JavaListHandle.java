package tech.intellispaces.ixora.commons.structures.collection;

import tech.intellispaces.framework.core.annotation.Mapper;
import tech.intellispaces.framework.core.annotation.ObjectHandle;
import tech.intellispaces.ixora.basic.DomainHandle;
import tech.intellispaces.ixora.structures.collection.ListUnmovableHandle;

import java.util.Collections;

@ObjectHandle
public abstract class JavaListHandle<E> implements ListUnmovableHandle<E> {
  private final java.util.List<E> javaList;
  private final Class<E> elementClass;

  public JavaListHandle(java.util.List<E> javaList, Class<E> elementClass) {
    this.javaList = Collections.unmodifiableList(javaList);
    this.elementClass = elementClass;
  }

  @Mapper
  @Override
  public DomainHandle<E> elementDomain() {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Mapper
  @Override
  public E element(int index) {
    return javaList.get(index);
  }

  @Mapper
  @Override
  public int size() {
    return javaList.size();
  }

  @Override
  public java.util.List<E> javaList() {
    return javaList;
  }
}
