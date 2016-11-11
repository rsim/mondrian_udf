package com.eazybi.mondrian.udf;

import mondrian.olap.*;
import mondrian.olap.type.*;
import mondrian.spi.UserDefinedFunction;

import java.util.*;

public class DateDiffHoursUdf extends DateUserDefinedFunction {
  public Object execute(Evaluator evaluator, Argument[] arguments) {
    Date dateFrom = parseDateArg(evaluator, arguments, 0);
    if (dateFrom == null) return null;
    Date dateTo = parseDateArg(evaluator, arguments, 1);
    if (dateTo == null) return null;

    return new Double((dateTo.getTime() - dateFrom.getTime()) / 3600000.0);
  }

  public String getDescription() {
    return "Returns difference in hours from first date to second date.";
  }

  public String getName() {
    return "DateDiffHours";
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
