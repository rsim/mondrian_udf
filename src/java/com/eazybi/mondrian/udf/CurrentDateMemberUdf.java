package com.eazybi.mondrian.udf;

import mondrian.olap.*;
import mondrian.olap.type.*;
import mondrian.spi.UserDefinedFunction;
import mondrian.util.Pair;

import java.util.*;

public class CurrentDateMemberUdf implements UserDefinedFunction {
  private HashMap<String, Pair<Date, Date>> startDatesCache = new HashMap<String, Pair<Date, Date>>();

  public Object execute(Evaluator evaluator, Argument[] arguments) {
    Level level = (Level) arguments[0].evaluate(evaluator);
    Hierarchy hierarchy = level.getHierarchy();
    Member previousMember = hierarchy.getNullMember();
    if (hierarchy.getDimension().getDimensionType() == mondrian.olap.DimensionType.TimeDimension) {
      long nowTime = evaluator.getQueryStartTime().getTime();
      for(Member member : evaluator.getSchemaReader().getLevelMembers(level, false)) {
        Pair<Date, Date> startDates = DateUtils.periodStartDatesWithCache(member, startDatesCache, evaluator);
        if (startDates != null && startDates.left.getTime() > nowTime) break;
        previousMember = member;
      }
    }
    return previousMember;
  }

  public String getDescription() {
    return "Returns current date member of time dimension level.";
  }

  public String getName() {
    return "CurrentDateMember";
  }

  public Type[] getParameterTypes() {
    return new Type[] {
      mondrian.olap.type.LevelType.Unknown
    };
  }

  public String[] getReservedWords() {
    return null;
  }

  public Type getReturnType(Type[] parameterTypes) {
    return MemberType.Unknown;
  }

  public Syntax getSyntax() {
    return Syntax.Property;
  }

}
