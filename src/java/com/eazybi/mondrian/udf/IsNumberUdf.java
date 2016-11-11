package com.eazybi.mondrian.udf;

import mondrian.olap.*;
import mondrian.olap.type.*;
import mondrian.spi.UserDefinedFunction;

public class IsNumberUdf implements UserDefinedFunction {
  public Object execute(Evaluator evaluator, Argument[] arguments) {
    Object arg = arguments[0].evaluateScalar(evaluator);
    if (arg instanceof Number) {
      return Boolean.TRUE;
    } else {
      return Boolean.FALSE;
    }
  }

  public String getDescription() {
    return "Returns if argument is number.";
  }

  public String getName() {
    return "IsNumber";
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
    return new BooleanType();
  }

  public Syntax getSyntax() {
    return Syntax.Function;
  }

}
