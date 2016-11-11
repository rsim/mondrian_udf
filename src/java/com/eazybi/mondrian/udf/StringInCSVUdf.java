package com.eazybi.mondrian.udf;

import mondrian.olap.*;
import mondrian.olap.type.*;
import mondrian.spi.UserDefinedFunction;

public class StringInCSVUdf implements UserDefinedFunction {
  public Object execute(Evaluator evaluator, Argument[] arguments) {
    Object arg = arguments[0].evaluateScalar(evaluator);
    if (arg == null) return Boolean.FALSE;
    String string;
    if (arg instanceof String) {
      string = (String) arg;
    } else if (arg instanceof Double && ((Double) arg).longValue() == ((Double) arg).doubleValue()) {
      string = Long.toString(((Double) arg).longValue());
    } else {
      string = arg.toString();
    }

    String csv = (String) arguments[1].evaluateScalar(evaluator);
    if (csv == null) return Boolean.FALSE;

    int index = csv.indexOf(string);
    if (index < 0) return Boolean.FALSE;

    if (index > 0 && csv.charAt(index - 1) != ',') return Boolean.FALSE;

    int stringLength = string.length();
    if (index + stringLength < csv.length() && csv.charAt(index + stringLength) != ',') return Boolean.FALSE;

    return Boolean.TRUE;
  }

  public String getDescription() {
    return "Returns if string is a value in comma separated values string.";
  }

  public String getName() {
    return "StringInCSV";
  }

  public Type[] getParameterTypes() {
    return new Type[] {
      new ScalarType(),
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
