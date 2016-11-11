package com.eazybi.mondrian.udf;

import mondrian.olap.*;
import mondrian.olap.type.*;
import mondrian.spi.UserDefinedFunction;

import java.util.*;

public class DateToTimestampUdf extends DateUserDefinedFunction {
  public Object execute(Evaluator evaluator, Argument[] arguments) {
    Date date = parseDateArg(evaluator, arguments, 0);
    if (date == null) return null;
    return ((long) date.getTime()) / 1000;
  }

  public String getDescription() {
    return "Convert a date to a Unix timestamp.";
  }

  public String getName() {
    return "DateToTimestamp";
  }

  public Type[] getParameterTypes() {
    return new Type[] {
      new ScalarType()
    };
  }

  public Type getReturnType(Type[] parameterTypes) {
    return new NumericType();
  }

}
