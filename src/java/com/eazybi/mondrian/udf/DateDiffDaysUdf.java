package com.eazybi.mondrian.udf;

import mondrian.olap.*;
import mondrian.olap.type.*;
import mondrian.spi.UserDefinedFunction;

import java.util.*;

public class DateDiffDaysUdf extends DateUserDefinedFunction {
  public Object execute(Evaluator evaluator, Argument[] arguments) {
    Date dateFrom = parseDateArg(evaluator, arguments, 0);
    if (dateFrom == null) return null;
    Date dateTo = parseDateArg(evaluator, arguments, 1);
    if (dateTo == null) return null;

    TimeZone tz = TimeZone.getDefault();
    int offset = tz.getDSTSavings();

    return new Double((dateTo.getTime() + (tz.inDaylightTime(dateTo) ? offset : 0) - (dateFrom.getTime() + (tz.inDaylightTime(dateFrom) ? offset : 0))) / 86400000.0);
  }

  public String getDescription() {
    return "Returns difference in days from first date to second date.";
  }

  public String getName() {
    return "DateDiffDays";
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
