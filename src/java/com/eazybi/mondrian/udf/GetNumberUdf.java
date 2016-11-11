package com.eazybi.mondrian.udf;

import mondrian.olap.*;
import mondrian.olap.type.*;
import mondrian.spi.UserDefinedFunction;

import java.util.*;

public class GetNumberUdf extends GetPropertyUdf {
  public Object execute(Evaluator evaluator, Argument[] arguments) {
    Object value = getPropertyValue(evaluator, arguments);
    if (value instanceof Number) {
      return value;
    } else if (value instanceof String) {
      try {
        return Long.valueOf((String) value);
      } catch (NumberFormatException e1) {
        try {
          return Double.valueOf((String) value);
        } catch (NumberFormatException e2) {
          return null;
        }
      }
    } else {
      return null;
    }
  }

  public String getName() {
    return "getNumber";
  }

  public Type getReturnType(Type[] parameterTypes) {
    return new NumericType();
  }

}
