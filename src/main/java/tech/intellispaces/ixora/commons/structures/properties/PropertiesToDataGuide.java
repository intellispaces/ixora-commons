package tech.intellispaces.ixora.commons.structures.properties;

import tech.intellispaces.framework.commons.exception.ExceptionFunctions;
import tech.intellispaces.framework.commons.exception.UnexpectedViolationException;
import tech.intellispaces.framework.commons.type.TypeFunctions;
import tech.intellispaces.framework.core.annotation.Data;
import tech.intellispaces.framework.core.annotation.Guide;
import tech.intellispaces.framework.core.annotation.Mapper;
import tech.intellispaces.framework.core.common.NameFunctions;
import tech.intellispaces.framework.core.object.ObjectFunctions;
import tech.intellispaces.ixora.structures.properties.Properties;
import tech.intellispaces.ixora.structures.properties.PropertiesToDataTransition;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;

@Guide
public class PropertiesToDataGuide implements PropertiesToDataTransition {

  @Mapper
  @Override
  public <T> T propertiesToData(Properties properties, Class<T> domainClass) {
    if (domainClass.isAnnotationPresent(Data.class)) {
      return processDataClass(properties, domainClass);
    }
    throw new UnsupportedOperationException("Not implemented");
  }

  @SuppressWarnings("unchecked")
  private <T> T processDataClass(Properties properties, Class<T> domainClass) {
    String dataClassName = NameFunctions.getDataClassCanonicalName(domainClass.getName());
    Class<?> dataClass = TypeFunctions.getClass(dataClassName).orElseThrow(() ->
        UnexpectedViolationException.withMessage("Can't find data class. Domain class {}, expected data class {}",
            domainClass.getCanonicalName(), dataClassName));
    Constructor<?>[] constructors = dataClass.getDeclaredConstructors();
    if (constructors.length != 1) {
      throw UnexpectedViolationException.withMessage("Data class {} must contain one constructor", dataClassName);
    }
    Constructor<?> constructor = constructors[0];
    if (constructor.getParameterCount() != domainClass.getDeclaredMethods().length) {
      throw UnexpectedViolationException.withMessage("Data class {} must contain constructor with {} parameters",
          dataClassName, domainClass.getDeclaredMethods().length);
    }

    Object[] arguments = new Object[constructor.getParameterCount()];
    int index = 0;
    for (Parameter param : constructor.getParameters()) {
      Object value = properties.value(param.getName());
      if (value == null && param.getType().isPrimitive()) {
        value = TypeFunctions.getDefaultValueOf(param.getType());
      }
      if (value instanceof Properties && ObjectFunctions.isObjectHandleClass(param.getType())) {
        Class<?> paramDomainClass = ObjectFunctions.getDomainClassOfObjectHandle(param.getType());
        value = processDataClass((Properties) value, paramDomainClass);
      }
      arguments[index++] = value;
    }
    return (T) ExceptionFunctions.coverException(constructor::newInstance, arguments);
  }
}
