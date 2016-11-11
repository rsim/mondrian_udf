package com.eazybi.mondrian.udf;

import mondrian.olap.*;
import mondrian.olap.type.*;
import mondrian.spi.UserDefinedFunction;

import java.util.*;

public class DateDiffWorkdaysUdf extends DateUserDefinedFunction {
  public Object execute(Evaluator evaluator, Argument[] arguments) {
    Date dateFrom = parseDateArg(evaluator, arguments, 0);
    if (dateFrom == null) return null;
    Date dateTo = parseDateArg(evaluator, arguments, 1);
    if (dateTo == null) return null;

    ArrayList<Integer> nonworkdays = parseNonWorkdaysArg(evaluator, arguments, 2);
    if (nonworkdays == null) return null;

    return new Double(workdays(dateFrom, dateTo, nonworkdays));
  }

  public String getDescription() {
    return "Returns difference in workdays from first date to second date.";
  }

  public String getName() {
    return "DateDiffWorkdays";
  }

  public Type[] getParameterTypes() {
    return new Type[] {
      new ScalarType(),
      new ScalarType(),
      new StringType()
    };
  }

  public Type getReturnType(Type[] parameterTypes) {
    return new NumericType();
  }

  double workdays(Date from, Date to, ArrayList<Integer> nonworkdays) {
    // check that from date is before to date
    if (to.compareTo(from) < 0) return -workdays(to, from, nonworkdays);

    // move from date to first workday
    Calendar c1 = Calendar.getInstance();
    c1.setTime(from);
    while (true) {
      int w1 = c1.get(Calendar.DAY_OF_WEEK);
      if (nonworkdays.contains(w1)) {
        c1.set(Calendar.HOUR_OF_DAY, 0);
        c1.set(Calendar.MINUTE, 0);
        c1.set(Calendar.SECOND, 0);
        c1.set(Calendar.MILLISECOND, 0);
        c1.add(Calendar.DAY_OF_WEEK, 1);
      } else {
        break;
      }
    }

    // move to date to first workday
    Calendar c2 = Calendar.getInstance();
    c2.setTime(to);
    while (true) {
      int w2 = c2.get(Calendar.DAY_OF_WEEK);
      if (nonworkdays.contains(w2)) {
        c2.set(Calendar.HOUR_OF_DAY, 0);
        c2.set(Calendar.MINUTE, 0);
        c2.set(Calendar.SECOND, 0);
        c2.set(Calendar.MILLISECOND, 0);
        c2.add(Calendar.DAY_OF_WEEK, 1);
      } else {
        break;
      }
    }
    long t1 = c1.getTimeInMillis() + c1.get(Calendar.DST_OFFSET);
    long t2 = c2.getTimeInMillis() + c2.get(Calendar.DST_OFFSET);

    double days = (t2 - t1) / 86400000.0;

    long fullWeeks = ((long) days) / 7;
    long nonworkCount = fullWeeks * nonworkdays.size();

    // count nonworkdays in last week
    Calendar c3 = Calendar.getInstance();
    c3.setTime(new Date(t1 + fullWeeks * 7 * 86400000));
    while (c3.compareTo(c2) < 0) {
      int w3 = c3.get(Calendar.DAY_OF_WEEK);
      if (nonworkdays.contains(w3)) {
        nonworkCount += 1;
      }
      c3.add(Calendar.DAY_OF_WEEK, 1);
    }

    return days - nonworkCount;
  }

}
