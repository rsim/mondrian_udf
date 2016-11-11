package com.eazybi.mondrian.udf;

import mondrian.olap.*;
import mondrian.olap.type.*;
import mondrian.spi.UserDefinedFunction;

import java.util.*;

public class DateParseUdf extends DateUserDefinedFunction {
  public Object execute(Evaluator evaluator, Argument[] arguments) {
    return parseDateArg(evaluator, arguments, 0);
  }

  public String getDescription() {
    return "Returns date that is parsed from string argument.";
  }

  public String getName() {
    return "DateParse";
  }

  public Type[] getParameterTypes() {
    return new Type[] {
      new StringType()
    };
  }

  public Type getReturnType(Type[] parameterTypes) {
    return new DateTimeType();
  }

}
