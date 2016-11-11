package com.eazybi.mondrian.udf;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.text.SimpleDateFormat;
import java.text.ParsePosition;
import java.text.ParseException;

import com.mdimension.jchronic.Chronic;
import com.mdimension.jchronic.Options;
import com.mdimension.jchronic.utils.Span;

import mondrian.olap.*;
import mondrian.util.Pair;
import mondrian.mdx.*;
import mondrian.calc.*;

public class DateUtils {

  static Pattern YEAR_REGEX = Pattern.compile("\\A\\d\\d\\d\\d\\z");
  static Pattern QUARTER_REGEX = Pattern.compile("\\AQ(\\d) (\\d\\d\\d\\d)\\z");
  static Pattern WEEK_REGEX = Pattern.compile("\\AW\\d\\d, (.*)\\z");
  static Pattern MONTH_DAY_YEAR_REGEX = Pattern.compile("\\A(\\w+) (\\d\\d )?(\\d\\d\\d\\d)\\z");
  static String MONTH_DAY_YEAR_FORMAT_STRING = "MMM dd yyyy";

  public static Date parseDate(String string) {
    Matcher m;
    int year, quarter, month, day;

    if (string == null) {
      return null;
    } else {
      m = YEAR_REGEX.matcher(string);
      if (m.matches()) {
        year = Integer.parseInt(string);
        return ymd(year, 1, 1);
      }

      m = QUARTER_REGEX.matcher(string);
      if (m.matches()) {
        quarter = Integer.parseInt(m.group(1));
        year = Integer.parseInt(m.group(2));
        return ymd(year, (quarter-1)*3+1, 1);
      }

      try {
        m = WEEK_REGEX.matcher(string);
        if (m.matches()) {
          string = m.group(1);
          SimpleDateFormat MONTH_DAY_YEAR_FORMAT = new SimpleDateFormat(MONTH_DAY_YEAR_FORMAT_STRING, Locale.ENGLISH);
          return MONTH_DAY_YEAR_FORMAT.parse(string);
        }

        m = MONTH_DAY_YEAR_REGEX.matcher(string);
        if (m.matches()) {
          if (m.group(2) == null) {
            string = m.group(1) + " 01 " + m.group(3);
          }
          SimpleDateFormat MONTH_DAY_YEAR_FORMAT = new SimpleDateFormat(MONTH_DAY_YEAR_FORMAT_STRING, Locale.ENGLISH);
          return MONTH_DAY_YEAR_FORMAT.parse(string);
        }
      } catch (ParseException e) {
        // do nothing and try Chronic
      }

      // set guess option to false to get beginning of exact dates
      Span span = Chronic.parse(string, new Options(false));
      if (span == null) {
        return null;
      }
      // changed to return date with time as exact dates will now return time 00:00:00 with guess option set to false
      // return beginningOfDay(span.getBeginCalendar().getTime());
      return span.getBeginCalendar().getTime();
    }
  }

  public static Date parseDateWithCache(Object arg, Map<String,Date> parseDateCache) {
    if (arg instanceof Date) {
      return (Date) arg;
    } else if (arg instanceof String) {
      String string = (String) arg;
      if (parseDateCache.containsKey(string)) {
        return parseDateCache.get(string);
      } else {
        Date date = parseDate(string);
        parseDateCache.put(string, date);
        return date;
      }
    } else {
      return null;
    }
  }

  @SuppressWarnings("deprecation")
  public static Pair<Date,Date> periodStartDates(Member member, Evaluator evaluator) {
    if (member.isCalculated()) {
      return calculatedPeriodStartDates(member, evaluator);
    }

    int year, quarter, month, day;
    Member parent;
    Object propertyValue;
    Date startDate;
    LevelType levelType = member.getLevel().getLevelType();

    Object key = member.getPropertyValue("KEY");

    if (levelType == LevelType.TimeYears) {
      year = getIntFromKey(key);
      if (year < 0) return null;

      if (isFiscalYearHierarchy(member)) {
        return fiscalYearStartDates(member, year);
      } else {
        return new Pair<Date,Date>(ymd(year, 1, 1), ymd(year+1, 1, 1));
      }
    }

    if (levelType == LevelType.TimeQuarters) {
      quarter = getIntFromKey(key);
      if (quarter < 0) return null;
      parent = member.getParentMember();
      while(parent.getLevel().getLevelType() != LevelType.TimeYears) parent = parent.getParentMember();
      year = getIntFromKey(parent.getPropertyValue("KEY"));
      if (year < 0) return null;

      if (isFiscalYearHierarchy(member)) {
        return fiscalQuarterStartDates(member, year, quarter);
      } else {
        return new Pair<Date,Date>(ymd(year, (quarter-1)*3+1, 1), ymd(year, quarter*3+1, 1));
      }
    }

    if (levelType == LevelType.TimeMonths) {
      month = getIntFromKey(key);
      if (month < 0) return null;
      parent = member.getParentMember();
      while(parent.getLevel().getLevelType() != LevelType.TimeYears) parent = parent.getParentMember();
      year = getYear(parent, month);
      if (year < 0) return null;
      return new Pair<Date,Date>(ymd(year, month, 1), ymd(year, month+1, 1));
    }

    if (levelType == LevelType.TimeWeeks) {
      // check if START_DATE property is available
      propertyValue = member.getPropertyValue("START_DATE");
      if (propertyValue instanceof java.sql.Date || propertyValue instanceof java.sql.Timestamp) {
        startDate = new Date(((Date) propertyValue).getTime());
      } else {
        startDate = parseDate(member.getName());
      }
      if (startDate == null) return null;
      return new Pair<Date,Date>(startDate, addDays(startDate, 7));
    }

    if (levelType == LevelType.TimeDays) {
      parent = member.getParentMember();
      if (key instanceof java.sql.Date || key instanceof java.sql.Timestamp) {
        startDate = new Date(((Date) key).getTime());
        if (startDate == null) return null;
        return new Pair<Date,Date>(startDate, addDays(startDate, 1));
      } else if (parent.getLevel().getLevelType() == LevelType.TimeWeeks) {
        // check if START_DATE property is available
        propertyValue = member.getPropertyValue("START_DATE");
        if (propertyValue instanceof java.sql.Date || propertyValue instanceof java.sql.Timestamp) {
          startDate = new Date(((Date) propertyValue).getTime());
        } else {
          startDate = parseDate(member.getName());
        }
        if (startDate == null) return null;
        return new Pair<Date,Date>(startDate, addDays(startDate, 1));
      } else {
        day = getIntFromKey(key);
        if (day < 0) return null;
        parent = member.getParentMember();
        while(parent.getLevel().getLevelType() != LevelType.TimeMonths) parent = parent.getParentMember();
        month = getIntFromKey(parent.getPropertyValue("KEY"));
        if (month < 0) return null;
        parent = parent.getParentMember();
        while(parent.getLevel().getLevelType() != LevelType.TimeYears) parent = parent.getParentMember();
        year = getYear(parent, month);
        if (year < 0) return null;
        return new Pair<Date,Date>(ymd(year, month, day), ymd(year, month, day+1));
      }
    }

    return null;
  }

  private static Pair<Date,Date> calculatedPeriodStartDates(Member member, Evaluator evaluator) {
    if (evaluator == null) return null;

    List<Member> membersList = calculatedChildrenSet(evaluator, member, true);
    int size = membersList.size();
    Date firstStartDate = null;
    Date lastNextStartDate = null;
    for (Member childMember : membersList) {
      if (!childMember.equals(member)) {
        Pair<Date,Date> startDates = periodStartDates(childMember, evaluator);
        if (startDates != null) {
          if (firstStartDate == null || firstStartDate.getTime() > startDates.left.getTime()) {
            firstStartDate = startDates.left;
          }
          if (lastNextStartDate == null || lastNextStartDate.getTime() < startDates.right.getTime()) {
            lastNextStartDate = startDates.right;
          }
        }
      }
    }
    if (firstStartDate != null && lastNextStartDate != null) {
      return new Pair<Date,Date>(firstStartDate, lastNextStartDate);
    }

    return null;
  }

  public static List<Member> calculatedChildrenSet(Evaluator evaluator, Member member, boolean cascading) {
    List<Member> resultsList = new ArrayList<Member>();

    Exp expression = expression = member.getExpression();
    ResolvedFunCall funCall = null;
    String funName = null;
    if (expression != null && expression instanceof ResolvedFunCall) {
      funCall = (ResolvedFunCall) expression;
      funName = funCall.getFunName();
    }

    if (funName != null && funName.toLowerCase().equals("aggregate") && funCall.getArgCount() == 1) {
      Exp setExpr = funCall.getArgs()[0];
      Calc setCalc = evaluator.getQuery().compileExpression(setExpr, false, null);
      TupleList tupleList = (TupleList) setCalc.evaluate(evaluator);
      for (List<Member> tuple : tupleList) {
        Member newMember = tuple.get(0);
        if (cascading && newMember.isCalculated()) {
          resultsList.addAll(calculatedChildrenSet(evaluator, newMember, cascading));
        } else {
          resultsList.add(newMember);
        }
      }
    } else if (expression != null && expression.getType() instanceof mondrian.olap.type.MemberType) {
      Calc memberCalc = evaluator.getQuery().compileExpression(expression, false, null);
      Member newMember = (Member) memberCalc.evaluate(evaluator);
      if (cascading && newMember.isCalculated()) {
        resultsList.addAll(calculatedChildrenSet(evaluator, newMember, cascading));
      } else {
        resultsList.add(newMember);
      }
    } else {
      resultsList.add(member);
    }

    return resultsList;
  }

  private static int getIntFromKey(Object key) {
    if (key instanceof Number) {
      return ((Number) key).intValue();
    } else if (key instanceof String) {
      return Integer.parseInt(((String) key).replaceAll("[^\\d]",""));
    } else {
      return -1;
    }
  }

  public static Pair<Date,Date> periodStartDatesWithCache(Member member, Map<String, Pair<Date,Date>> startDatesCache, Evaluator evaluator) {
    String fullName = member.getUniqueName();
    if (startDatesCache.containsKey(fullName)) {
      return startDatesCache.get(fullName);
    } else {
      Pair<Date,Date> startDates = periodStartDates(member, evaluator);
      startDatesCache.put(fullName, startDates);
      return startDates;
    }
  }

  public static HashMap<String, Pair<Date, Date>> newStartDatesCache() {
    return new HashMap<String, Pair<Date, Date>>();
  }

  public static Date ymd(int year, int month, int day) {
    Calendar calendar = Calendar.getInstance();
    calendar.clear();
    calendar.set(year, month - 1, day);
    return calendar.getTime();
  }

  public static Date beginningOfDay(Date date) {
    final Calendar calendar = Calendar.getInstance();
    calendar.clear();
    calendar.setTime(date);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    return calendar.getTime();
  }

  @SuppressWarnings("deprecation")
  public static Date addDays(Date date, int days) {
    return new Date(date.getYear(), date.getMonth(), date.getDate() + days);
  }

  public static boolean isFiscalYearHierarchy(Member member) {
    Level level = member.getLevel();
    Hierarchy hierarchy = level.getHierarchy();

    return hierarchy.getName().equals(member.getDimension().getName() + ".Fiscal");
  }

  public static int getHierarchyFirstMonth(Member member) {
    Level level = member.getLevel();
    Hierarchy hierarchy = level.getHierarchy();
    int month = 1;

    Annotation firstMonthAnnotation = hierarchy.getAnnotationMap().get("first_month");
    if (firstMonthAnnotation != null) {
      month = Integer.parseInt((String) firstMonthAnnotation.getValue());
    }

    return month;
  }

  public static int getYear(Member yearMember, int month) {
    int year = getIntFromKey(yearMember.getPropertyValue("KEY"));

    if (isFiscalYearHierarchy(yearMember)) {
      int firstMonth = getHierarchyFirstMonth(yearMember);
      return month < firstMonth || firstMonth == 1 ? year : year-1;
    } else {
      return year;
    }
  }

  public static Pair<Date,Date> fiscalYearStartDates(Member member, int fiscalYear) {
    int firstMonth = getHierarchyFirstMonth(member);
    int yearStartsFrom = fiscalYear;
    int nextYearStartsFrom = yearStartsFrom;
    if (firstMonth > 1) { yearStartsFrom--; } else {nextYearStartsFrom++; }
    return new Pair<Date,Date>(ymd(yearStartsFrom, firstMonth, 1), ymd(nextYearStartsFrom, firstMonth, 1));
  }

  public static Pair<Date,Date> fiscalQuarterStartDates(Member member, int fiscalYear, int fiscalQuarter) {
    Calendar calendar = Calendar.getInstance();
    int firstMonth = getHierarchyFirstMonth(member);
    int year = firstMonth > 1 ? fiscalYear-1 : fiscalYear;
    Date quarterStartDate;
    Date nextQuarterStartDate;

    calendar.clear();
    calendar.set(year, 0, 1);
    calendar.add(Calendar.MONTH, (fiscalQuarter-1)*3+firstMonth-1);
    quarterStartDate = calendar.getTime();
    calendar.add(Calendar.MONTH, 3);
    nextQuarterStartDate = calendar.getTime();

    return new Pair<Date,Date>(quarterStartDate, nextQuarterStartDate);
  }

  static String DATE_AS_JSON_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss";

  public static String dateAsJSON(Date value) {
    SimpleDateFormat format = new SimpleDateFormat(DATE_AS_JSON_FORMAT_STRING, Locale.ENGLISH);
    return format.format(value).replace(" 00:00:00", "");
  }

  static String DATE_ISO8601_FORMAT_STRING = "yyyy-MM-dd'T'HH:mm:ssZ";

  public static String dateAsISO8601(Date value) {
    SimpleDateFormat format = new SimpleDateFormat(DATE_ISO8601_FORMAT_STRING, Locale.ENGLISH);
    return format.format(value);
  }
}
