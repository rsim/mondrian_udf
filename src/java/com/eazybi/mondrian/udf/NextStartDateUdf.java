package com.eazybi.mondrian.udf;

import mondrian.olap.*;
import mondrian.olap.type.*;
import mondrian.spi.UserDefinedFunction;
import mondrian.util.Pair;

import java.util.*;

public class NextStartDateUdf extends DateUserDefinedFunction {
  private HashMap<String, Pair<Date, Date>> startDatesCache = new HashMap<String, Pair<Date, Date>>();

  public Object execute(Evaluator evaluator, Argument[] arguments) {
    Member member = (Member) arguments[0].evaluate(evaluator);
    Pair<Date, Date> startDates = DateUtils.periodStartDatesWithCache(member, startDatesCache, evaluator);
    if (startDates == null) return null;
    return startDates.right;
  }

  public String getDescription() {
    return "Returns next time period start date.";
  }

  public String getName() {
    return "NextStartDate";
  }

  public Type[] getParameterTypes() {
    return new Type[] {
      MemberType.Unknown
    };
  }

  public Type getReturnType(Type[] parameterTypes) {
    return new DateTimeType();
  }

  public Syntax getSyntax() {
    return Syntax.Property;
  }

}
