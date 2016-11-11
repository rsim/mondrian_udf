package com.eazybi.mondrian.udf;

import mondrian.olap.*;
import mondrian.olap.type.*;
import mondrian.spi.UserDefinedFunction;

import java.util.*;

public class DateCompareUdf extends DateUserDefinedFunction {
  public Object execute(Evaluator evaluator, Argument[] arguments) {
    Date date1 = parseDateArg(evaluator, arguments, 0);
    if (date1 == null) return null;
    Date date2 = parseDateArg(evaluator, arguments, 1);
    if (date2 == null) return null;

    double difference = date1.getTime() - date2.getTime();
    if (difference < 0) return -1;
    if (difference > 0) return 1;
    return 0;
  }

  public String getDescription() {
    return "Returns -1 if first date is less than second date, 1 if first date is greater than second date, and 0 if dates are equal.";
  }

  public String getName() {
    return "DateCompare";
  }

  public Type[] getParameterTypes() {
    return new Type[] {
      new ScalarType(),
      new ScalarType()
    };
  }

  public Type getReturnType(Type[] parameterTypes) {
    return new NumericType();
  }

}
