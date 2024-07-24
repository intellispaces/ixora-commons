package tech.mindstructs.structures.properties;

import intellispaces.ixora.mindstructs.structures.properties.Properties;
import intellispaces.ixora.mindstructs.structures.properties.PropertiesHandle;
import intellispaces.ixora.mindstructs.structures.properties.PropertiesToDataMapper;
import tech.intellispaces.framework.commons.exception.UnexpectedViolationException;
import tech.intellispaces.framework.commons.function.FunctionFunctions;
import tech.intellispaces.framework.commons.type.TypeFunctions;
import tech.intellispaces.framework.core.annotation.Data;
import tech.intellispaces.framework.core.annotation.Guide;
import tech.intellispaces.framework.core.annotation.Mapper;
import tech.intellispaces.framework.core.common.NameConventionFunctions;
import tech.intellispaces.framework.core.object.ObjectFunctions;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;

@Guide
public class IxoraPropertiesToDataMapper implements PropertiesToDataMapper {

  @Mapper
  @Override
  public <T> T propertiesToData(PropertiesHandle properties, Class<T> domainClass) {
    if (domainClass.isAnnotationPresent(Data.class)) {
      return processDataClass(properties, domainClass);
    }
    throw new UnsupportedOperationException("Not implemented");
  }

  @SuppressWarnings("unchecked")
  private <T> T processDataClass(PropertiesHandle properties, Class<T> domainClass) {
    String dataClassName = NameConventionFunctions.getDataClassName(domainClass.getName());
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
        value = processDataClass((PropertiesHandle) value, paramDomainClass);
      }
      arguments[index++] = value;
    }
    return (T) FunctionFunctions.applyAndCoverIfChecked(constructor::newInstance, arguments);
  }
}