package com.eazybi.mondrian.udf;

import mondrian.olap.*;
import mondrian.olap.type.*;
import mondrian.spi.UserDefinedFunction;

import java.util.*;

public class KeyUdf implements UserDefinedFunction {
  public Object execute(Evaluator evaluator, Argument[] arguments) {
    Member member = (Member) arguments[0].evaluate(evaluator);
    Object value = member.getPropertyValue("KEY");
    if (value instanceof java.sql.Date || value instanceof java.sql.Timestamp) {
      return new Date(((Date) value).getTime());
    }
    return value;
  }

  public String getDescription() {
    return "Returns member key.";
  }

  public String getName() {
    return "Key";
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
    return new ScalarType();
  }

  public Syntax getSyntax() {
    return Syntax.Property;
  }

}
