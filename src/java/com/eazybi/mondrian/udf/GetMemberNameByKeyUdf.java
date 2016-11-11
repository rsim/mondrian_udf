package com.eazybi.mondrian.udf;

import mondrian.olap.*;
import mondrian.olap.type.*;
import mondrian.spi.UserDefinedFunction;
import mondrian.util.Pair;

import java.util.*;

public class GetMemberNameByKeyUdf extends GetMemberByKeyUdf {
  private HashMap<Object,Member> memberCache = new HashMap<Object,Member>();

  public Object execute(Evaluator evaluator, Argument[] arguments) {
    Member member = (Member) super.execute(evaluator, arguments);
    if (member.isNull()) return null;
    return member.getName();
  }

  public String getDescription() {
    return "Returns level member name with specified key value.";
  }

  public String getName() {
    return "getMemberNameByKey";
  }

  public Type getReturnType(Type[] parameterTypes) {
    return new StringType();
  }

}
