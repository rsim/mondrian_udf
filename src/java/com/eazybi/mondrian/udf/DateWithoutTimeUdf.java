package com.eazybi.mondrian.udf;

import mondrian.olap.*;
import mondrian.olap.type.*;
import mondrian.spi.UserDefinedFunction;

import java.util.*;

public class DateWithoutTimeUdf extends DateUserDefinedFunction {
  public Object execute(Evaluator evaluator, Argument[] arguments) {
    Date date = parseDateArg(evaluator, arguments, 0);
    if (date == null) return null;
    return DateUtils.beginningOfDay(date);
  }

  public String getDescription() {
    return "Returns date argument without time information.";
  }

  public String getName() {
    return "DateWithoutTime";
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
