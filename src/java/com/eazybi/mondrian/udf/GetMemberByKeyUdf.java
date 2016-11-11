package com.eazybi.mondrian.udf;

import mondrian.olap.*;
import mondrian.olap.type.*;
import mondrian.spi.UserDefinedFunction;
import mondrian.util.Pair;

import java.util.*;

public class GetMemberByKeyUdf implements UserDefinedFunction {
  private HashMap<Object,Member> memberCache = new HashMap<Object,Member>();

  public Object execute(Evaluator evaluator, Argument[] arguments) {
    Object memberKey = arguments[1].evaluateScalar(evaluator);
    Level level = (Level) arguments[0].evaluate(evaluator);

    if (memberKey == null) {
      return level.getHierarchy().getNullMember();
    }

    // if member cache is empty then on first execution get all level members in cache
    if (memberCache.isEmpty()) {
      Object mKey;
      for (Member m : evaluator.getSchemaReader().getLevelMembers(level, false)) {
        mKey = m.getPropertyValue("KEY");
        memberCache.put(mKey, m);
      }
    }

    Member member = memberCache.get(memberKey);
    if (member != null) return member;

    // if argument was number then also try to lookup by Integer
    if (memberKey instanceof Number) {
      int intKey = ((Number) memberKey).intValue();
      // if converting to integer was successful
      if (intKey == ((Number) memberKey).doubleValue()) {
        member = memberCache.get(new Integer(intKey));
        if (member != null) return member;
      }
    }

    return level.getHierarchy().getNullMember();
  }

  public String getDescription() {
    return "Returns level member with specified key value.";
  }

  public String getName() {
    return "getMemberByKey";
  }

  public Type[] getParameterTypes() {
    return new Type[] {
      mondrian.olap.type.LevelType.Unknown,
      new ScalarType()
    };
  }

  public String[] getReservedWords() {
    return null;
  }

  public Type getReturnType(Type[] parameterTypes) {
    return MemberType.Unknown;
  }

  public Syntax getSyntax() {
    return Syntax.Method;
  }

}
