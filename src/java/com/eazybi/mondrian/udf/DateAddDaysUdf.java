package com.eazybi.mondrian.udf;

import mondrian.olap.*;
import mondrian.olap.type.*;
import mondrian.spi.UserDefinedFunction;

import java.util.*;

public class DateAddDaysUdf extends DateUserDefinedFunction {
  public Object execute(Evaluator evaluator, Argument[] arguments) {
    Date date = parseDateArg(evaluator, arguments, 0);
    if (date == null) return null;
    double days = ((Number) arguments[1].evaluateScalar(evaluator)).doubleValue();
    return new Date((long)(date.getTime() + 86400000.0 * days));
  }

  public String getDescription() {
    return "Returns date plus specified number of days.";
  }

  public String getName() {
    return "DateAddDays";
  }

  public Type[] getParameterTypes() {
    return new Type[] {
      new ScalarType(),
      new NumericType()
    };
  }

  public Type getReturnType(Type[] parameterTypes) {
    return new DateTimeType();
  }

}
