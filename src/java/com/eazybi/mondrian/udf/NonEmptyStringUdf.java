package com.eazybi.mondrian.udf;

import mondrian.olap.*;
import mondrian.olap.type.*;
import mondrian.spi.UserDefinedFunction;

public class NonEmptyStringUdf implements UserDefinedFunction {
  public Object execute(Evaluator evaluator, Argument[] arguments) {
    Object arg = arguments[0].evaluateScalar(evaluator);
    if (arg instanceof String) {
      String s = (String) arg;
      if (s.length() > 0) {
        return s;
      }
    }
    return null;
  }

  public String getDescription() {
    return "Returns string value if non-empty.";
  }

  public String getName() {
    return "NonEmptyString";
  }

  public Type[] getParameterTypes() {
    return new Type[] {
      new ScalarType()
    };
  }

  public String[] getReservedWords() {
    return null;
  }

  public Type getReturnType(Type[] parameterTypes) {
    return new StringType();
  }

  public Syntax getSyntax() {
    return Syntax.Function;
  }

}
