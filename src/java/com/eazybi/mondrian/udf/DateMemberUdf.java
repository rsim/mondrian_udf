package com.eazybi.mondrian.udf;

import mondrian.olap.*;
import mondrian.olap.type.*;
import mondrian.spi.UserDefinedFunction;
import mondrian.util.Pair;

import java.util.*;

public class DateMemberUdf extends DateUserDefinedFunction {
  private HashMap<String, Pair<Date, Date>> startDatesCache = new HashMap<String, Pair<Date, Date>>();

  public Object execute(Evaluator evaluator, Argument[] arguments) {
    Level level = (Level) arguments[0].evaluate(evaluator);
    Hierarchy hierarchy = level.getHierarchy();
    Member previousMember = hierarchy.getNullMember();
    Date date = parseDateArg(evaluator, arguments, 1);
    if (date == null) return previousMember;

    if (hierarchy.getDimension().getDimensionType() == mondrian.olap.DimensionType.TimeDimension) {
      long dateTime = date.getTime();
      for(Member member : evaluator.getSchemaReader().getLevelMembers(level, false)) {
        Pair<Date, Date> startDates = DateUtils.periodStartDatesWithCache(member, startDatesCache, evaluator);
        if (startDates != null && startDates.left.getTime() > dateTime) break;
        previousMember = member;
      }
    }

    return previousMember;
  }

  public String getDescription() {
    return "Returns date member of time dimension level that includes specified date.";
  }

  public String getName() {
    return "DateMember";
  }

  public Type[] getParameterTypes() {
    return new Type[] {
      mondrian.olap.type.LevelType.Unknown,
      new ScalarType()
    };
  }

  public Type getReturnType(Type[] parameterTypes) {
    return MemberType.Unknown;
  }

  public Syntax getSyntax() {
    return Syntax.Method;
  }

}
