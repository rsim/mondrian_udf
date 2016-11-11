package com.eazybi.mondrian.udf;

import mondrian.olap.*;
import mondrian.olap.type.*;
import mondrian.spi.UserDefinedFunction;
import mondrian.util.Pair;

import java.util.*;

public class GetMembersByKeysUdf implements UserDefinedFunction {
  private HashMap<Object,Member> memberCache = new HashMap<Object,Member>();

  public Object execute(Evaluator evaluator, Argument[] arguments) {
    Level level = (Level) arguments[0].evaluate(evaluator);
    String memberKeysString = (String) arguments[1].evaluateScalar(evaluator);
    List<Member> resultsList = new ArrayList<Member>();
    if (memberKeysString == null) return resultsList;
    String memberKeys[] = memberKeysString.split(",");


    // if member cache is empty then on first execution get all level members in cache
    if (memberCache.isEmpty()) {
      Object mKey;
      for (Member m : evaluator.getSchemaReader().getLevelMembers(level, false)) {
        mKey = m.getPropertyValue("KEY");
        memberCache.put(mKey, m);
      }
    }

    for (String memberKey : memberKeys) {
      Member member = memberCache.get(memberKey);
      // if argument was number then also try to lookup by Integer
      if (member == null) {
        int intKey = Integer.MIN_VALUE;
        try {
          intKey = Integer.parseInt(memberKey, 10);
        } catch (NumberFormatException e) {
        }
        // if converting to integer was successful
        if (Integer.MIN_VALUE < intKey && intKey < Integer.MAX_VALUE) {
          member = memberCache.get(new Integer(intKey));
        }
      }

      if (member != null && !member.isNull()) {
        resultsList.add(member);
      }
    }
    return resultsList;
  }

  public String getDescription() {
    return "Returns set of level members with specified comma separated keys.";
  }

  public String getName() {
    return "getMembersByKeys";
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
    return new SetType(MemberType.Unknown);
  }

  public Syntax getSyntax() {
    return Syntax.Method;
  }

}
