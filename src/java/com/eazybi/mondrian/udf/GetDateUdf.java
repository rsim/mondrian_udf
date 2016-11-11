package com.eazybi.mondrian.udf;

import mondrian.olap.*;
import mondrian.olap.type.*;
import mondrian.spi.UserDefinedFunction;

import java.util.*;

public class GetDateUdf extends GetPropertyUdf {
  public Object execute(Evaluator evaluator, Argument[] arguments) {
    Object value = getPropertyValue(evaluator, arguments);
    if (value instanceof Date) {
      return value;
    } else {
      return null;
    }
  }

  public String getName() {
    return "getDate";
  }

  public Type getReturnType(Type[] parameterTypes) {
    return new DateTimeType();
  }

}
