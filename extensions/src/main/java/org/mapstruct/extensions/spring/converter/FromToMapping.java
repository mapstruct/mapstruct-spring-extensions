package org.mapstruct.extensions.spring.converter;

import com.squareup.javapoet.TypeName;

import java.util.Objects;
import java.util.Optional;

public class FromToMapping {
  private TypeName source;
  private TypeName target;
  private String adapterMethodName;

  public TypeName getSource() {
    return source;
  }

  public FromToMapping source(final TypeName source) {
    this.source = source;
    return this;
  }

  public TypeName getTarget() {
    return target;
  }

  public FromToMapping target(final TypeName target) {
    this.target = target;
    return this;
  }

  public Optional<String> getAdapterMethodName() {
    return Optional.ofNullable(adapterMethodName);
  }

  public FromToMapping adapterMethodName(final String adapterMethodName) {
    this.adapterMethodName = adapterMethodName;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    FromToMapping that = (FromToMapping) o;
    return Objects.equals(source, that.source) && Objects.equals(target, that.target) && Objects.equals(adapterMethodName, that.adapterMethodName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(source, target, adapterMethodName);
  }
}
