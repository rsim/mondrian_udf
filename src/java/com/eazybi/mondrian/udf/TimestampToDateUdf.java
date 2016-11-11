package com.eazybi.mondrian.udf;

import mondrian.olap.*;
import mondrian.olap.type.*;
import mondrian.spi.UserDefinedFunction;

import java.util.*;

public class TimestampToDateUdf extends DateUserDefinedFunction {
  public Object execute(Evaluator evaluator, Argument[] arguments) {
    Object arg = arguments[0].evaluateScalar(evaluator);
    if (arg instanceof Number) {
      return new Date(((Number)arg).longValue() * 1000);
    } else {
      return null;
    }
  }

  public String getDescription() {
    return "Convert a Unix timestamp to a date.";
  }

  public String getName() {
    return "TimestampToDate";
  }

  public Type[] getParameterTypes() {
    return new Type[] {
      new ScalarType()
    };
  }

  public Type getReturnType(Type[] parameterTypes) {
    return new DateTimeType();
  }

}
