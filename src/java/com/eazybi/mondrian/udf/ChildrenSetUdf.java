package com.eazybi.mondrian.udf;

import mondrian.olap.*;
import mondrian.olap.type.*;
import mondrian.spi.UserDefinedFunction;

import java.util.*;

public class ChildrenSetUdf implements UserDefinedFunction {
  public Object execute(Evaluator evaluator, Argument[] arguments) {
    Member member = (Member) arguments[0].evaluate(evaluator);
    return childrenSet(evaluator, member, false);
  }

  protected List<Member> childrenSet(Evaluator evaluator, Member member, boolean cascading) {
    if (member.isCalculated()) {
      return DateUtils.calculatedChildrenSet(evaluator, member, cascading);
    } else {
      List<Member> children = evaluator.getSchemaReader().getMemberChildren(member);
      if (children.size() > 0) {
        return children;
      } else {
        List<Member> resultsList = new ArrayList<Member>();
        resultsList.add(member);
        return resultsList;
      }
    }
  }

  public String getDescription() {
    return "Returns children set of aggregate calculated member (set argument of Aggregate function).";
  }

  public String getName() {
    return "ChildrenSet";
  }

  public Type[] getParameterTypes() {
    return new Type[] {
      MemberType.Unknown
    };
  }

  public String[] getReservedWords() {
    return null;
  }

  public Type getReturnType(Type[] parameterTypes) {
    return new SetType(MemberType.Unknown);
  }

  public Syntax getSyntax() {
    return Syntax.Function;
  }

}
