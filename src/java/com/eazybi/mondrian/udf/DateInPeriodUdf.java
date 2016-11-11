package com.eazybi.mondrian.udf;

import mondrian.olap.*;
import mondrian.olap.type.*;
import mondrian.spi.UserDefinedFunction;
import mondrian.util.Pair;

import java.util.*;

public class DateInPeriodUdf extends DateUserDefinedFunction {
  private HashMap<String, Pair<Date, Date>> startDatesCache = new HashMap<String, Pair<Date, Date>>();

  public Object execute(Evaluator evaluator, Argument[] arguments) {
    Date date = parseDateArg(evaluator, arguments, 0);
    if (date == null) return Boolean.FALSE;

    Member member = (Member) arguments[1].evaluate(evaluator);
    if (member.isAll()) return Boolean.TRUE;

    Pair<Date, Date> startDates = DateUtils.periodStartDatesWithCache(member, startDatesCache, evaluator);
    if (startDates == null) return Boolean.FALSE;

    return Boolean.valueOf(startDates.left.getTime() <= date.getTime() && date.getTime() < startDates.right.getTime());
  }

  public String getDescription() {
    return "Returns if date is between time period start and end dates.";
  }

  public String getName() {
    return "DateInPeriod";
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
