package com.eazybi.mondrian.udf;

import mondrian.olap.*;
import mondrian.olap.type.*;
import mondrian.spi.UserDefinedFunction;

public class NonZeroUdf implements UserDefinedFunction {
  public Object execute(Evaluator evaluator, Argument[] arguments) {
    Object arg = arguments[0].evaluateScalar(evaluator);
    if (arg instanceof Number) {
      double value = ((Number) arg).doubleValue();
      if (value != 0) {
        return new Double(value);
      }
    }
    return null;
  }

  public String getDescription() {
    return "Returns numeric value if non-zero.";
  }

  public String getName() {
    return "NonZero";
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
    return new NumericType();
  }

  public Syntax getSyntax() {
    return Syntax.Function;
  }

}
