package com.eazybi.mondrian.udf;

import mondrian.olap.*;
import mondrian.olap.type.*;
import mondrian.spi.UserDefinedFunction;

import java.util.*;

public class GetBooleanUdf extends GetPropertyUdf {
  public Object execute(Evaluator evaluator, Argument[] arguments) {
    Object value = getPropertyValue(evaluator, arguments);
    if (value instanceof Boolean) {
      return value;
    } else if (value instanceof Number) {
      int intValue = ((Number) value).intValue();
      if (intValue == 1) {
        return Boolean.TRUE;
      } else {
        return Boolean.FALSE;
      }
    } else {
      // always return Boolean value as otherwise Mondrian will fail if null is returned as Boolean result
      return Boolean.FALSE;
    }
  }

  public String getName() {
    return "getBoolean";
  }

  public Type getReturnType(Type[] parameterTypes) {
    return new BooleanType();
  }

}
