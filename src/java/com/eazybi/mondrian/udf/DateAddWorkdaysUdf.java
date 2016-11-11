package com.eazybi.mondrian.udf;

import mondrian.olap.*;
import mondrian.olap.type.*;
import mondrian.spi.UserDefinedFunction;

import java.util.*;

public class DateAddWorkdaysUdf extends DateUserDefinedFunction {
  public Object execute(Evaluator evaluator, Argument[] arguments) {
    Date date = parseDateArg(evaluator, arguments, 0);
    if (date == null) return null;
    double days = ((Number) arguments[1].evaluateScalar(evaluator)).doubleValue();

    ArrayList<Integer> nonworkdays = parseNonWorkdaysArg(evaluator, arguments, 2);
    if (nonworkdays == null) return null;

    // return new Date((long)(date.getTime() + 86400000.0 * days));
    return addworkdays(date, days, nonworkdays);
  }

  public String getDescription() {
    return "Returns date plus specified number of workdays.";
  }

  public String getName() {
    return "DateAddWorkdays";
  }

  public Type[] getParameterTypes() {
    return new Type[] {
      new ScalarType(),
      new NumericType(),
      new StringType()
    };
  }

  public Type getReturnType(Type[] parameterTypes) {
    return new DateTimeType();
  }

  Date addworkdays(Date from, double days, ArrayList<Integer> nonworkdays) {
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

    int workweekSize = 7 - nonworkdays.size();
    long fullWeeks = days >= 0 ? ((long) days) / workweekSize : - ((long) -days) / workweekSize;

    double remainingDays = days - fullWeeks * workweekSize;
    if (remainingDays < 0) {
      // go back one more week and then add remaining Days
      fullWeeks -= 1;
      remainingDays += workweekSize;
    }

    // in case if fullWeeks reached long overflow then remainingDays will be larger than 1 week.
    if (remainingDays > workweekSize) {
      return null;
    }

    Calendar c2 = Calendar.getInstance();
    c2.setTime(new Date(c1.getTimeInMillis() + fullWeeks * 7 * 86400000));
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

    while (remainingDays >= 1) {
      c2.add(Calendar.DAY_OF_WEEK, 1);
      remainingDays -= 1;
      while (true) {
        int w2 = c2.get(Calendar.DAY_OF_WEEK);
        if (nonworkdays.contains(w2)) {
          c2.add(Calendar.DAY_OF_WEEK, 1);
        } else {
          break;
        }
      }
    }

    Calendar c3 = Calendar.getInstance();
    c3.setTime(new Date(c2.getTimeInMillis() + (long) (remainingDays * 86400000.0) ));
    while (true) {
      int w3 = c3.get(Calendar.DAY_OF_WEEK);
      if (nonworkdays.contains(w3)) {
        c3.set(Calendar.HOUR_OF_DAY, 0);
        c3.set(Calendar.MINUTE, 0);
        c3.set(Calendar.SECOND, 0);
        c3.set(Calendar.MILLISECOND, 0);
        c3.add(Calendar.DAY_OF_WEEK, 1);
      } else {
        break;
      }
    }

    return c3.getTime();
  }

}
