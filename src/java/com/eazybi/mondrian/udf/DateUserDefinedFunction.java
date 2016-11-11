package com.eazybi.mondrian.udf;

import mondrian.olap.*;
import mondrian.olap.type.*;
import mondrian.spi.UserDefinedFunction;

import java.util.*;

public abstract class DateUserDefinedFunction implements UserDefinedFunction {
  private HashMap<String, Date> parseDateCache = new HashMap<String, Date>();

  protected Date parseDateArg(Evaluator evaluator, Argument[] arguments, int index) {
    return DateUtils.parseDateWithCache(arguments[index].evaluateScalar(evaluator), parseDateCache);
  }

  protected Date parseDate(String dateString) {
    return DateUtils.parseDateWithCache(dateString, parseDateCache);
  }

  protected ArrayList<Integer> parseNonWorkdaysArg(Evaluator evaluator, Argument[] arguments, int index) {
    ArrayList<Integer> nonworkdays = new ArrayList<Integer>();

    if (arguments.length == index + 1) {
      String nonworkdaysString = arguments[index].evaluateScalar(evaluator).toString();
      for (char dayChar : nonworkdaysString.toCharArray()) {
        int calendarDay = (dayChar - '0') % 7 + Calendar.SUNDAY;
        nonworkdays.add(calendarDay);
      }
      if (nonworkdays.size() >= 7) return null;
    } else {
      nonworkdays.add(Calendar.SATURDAY);
      nonworkdays.add(Calendar.SUNDAY);
    }
    return nonworkdays;
  }

  public String[] getReservedWords() {
    return null;
  }

  public Syntax getSyntax() {
    return Syntax.Function;
  }

}
