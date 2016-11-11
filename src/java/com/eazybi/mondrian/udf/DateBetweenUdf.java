package com.eazybi.mondrian.udf;

import mondrian.olap.*;
import mondrian.olap.type.*;
import mondrian.spi.UserDefinedFunction;

import java.util.*;

public class DateBetweenUdf extends DateUserDefinedFunction {
  public Object execute(Evaluator evaluator, Argument[] arguments) {
    Date date = parseDateArg(evaluator, arguments, 0);
    if (date == null) return Boolean.FALSE;
    Date dateFrom = parseDateArg(evaluator, arguments, 1);
    if (dateFrom == null) return Boolean.FALSE;
    if (dateFrom.getTime() > date.getTime()) return Boolean.FALSE;
    Date dateTo = parseDateArg(evaluator, arguments, 2);
    if (dateTo == null) return Boolean.FALSE;
    return Boolean.valueOf(date.getTime() <= dateTo.getTime());
  }

  public String getDescription() {
    return "Returns if date is between other two dates.";
  }

  public String getName() {
    return "DateBetween";
  }

  public Type[] getParameterTypes() {
    return new Type[] {
      new ScalarType(),
      new ScalarType(),
      new ScalarType()
    };
  }

  public Type getReturnType(Type[] parameterTypes) {
    return new BooleanType();
  }

}
