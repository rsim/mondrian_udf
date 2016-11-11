package com.eazybi.mondrian.udf;

import mondrian.olap.*;
import mondrian.olap.type.*;
import mondrian.spi.UserDefinedFunction;
import mondrian.util.Pair;

import java.util.*;

public class AnyDateInPeriodUdf extends DateUserDefinedFunction {
  private HashMap<String, Pair<Date, Date>> startDatesCache = new HashMap<String, Pair<Date, Date>>();

  public Object execute(Evaluator evaluator, Argument[] arguments) {
    String commaSeparatedDatesString = (String) arguments[0].evaluateScalar(evaluator);
    if (commaSeparatedDatesString == null || commaSeparatedDatesString.length() == 0) return Boolean.FALSE;

    Member member = (Member) arguments[1].evaluate(evaluator);
    if (member.isAll()) return Boolean.TRUE;

    Pair<Date, Date> startDates = DateUtils.periodStartDatesWithCache(member, startDatesCache, evaluator);
    if (startDates == null) return Boolean.FALSE;

    String dateStringsArray[] = commaSeparatedDatesString.split(",");

    for (String dateString : dateStringsArray) {
      Date date = parseDate(dateString);
      if (date != null && startDates.left.getTime() <= date.getTime() && date.getTime() < startDates.right.getTime()) {
        return Boolean.TRUE;
      }
    }
    return Boolean.FALSE;
  }

  public String getDescription() {
    return "Returns if any date from comma separated string is between time period start and end dates.";
  }

  public String getName() {
    return "AnyDateInPeriod";
  }

  public Type[] getParameterTypes() {
    return new Type[] {
      new ScalarType(),
      MemberType.Unknown
    };
  }

  public Type getReturnType(Type[] parameterTypes) {
    return new BooleanType();
  }

}
