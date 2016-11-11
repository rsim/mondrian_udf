package com.eazybi.mondrian.udf;

import mondrian.olap.*;
import mondrian.olap.type.*;
import mondrian.spi.UserDefinedFunction;

import java.util.*;

public class TitleizeUdf implements UserDefinedFunction {
  public Object execute(Evaluator evaluator, Argument[] arguments) {
    String s = (String) arguments[0].evaluateScalar(evaluator);
    if (s == null || s.length() == 0) return s;
    return titleize(s);
  }

  String titleize(String str) {
    int strLen = str.length();
    StringBuffer buffer = new StringBuffer(strLen);
    boolean capitalizeNext = true;
    for (int i = 0; i < strLen; i++) {
      char ch = str.charAt(i);

      if (ch == ' ' || ch == '_' || ch == '-') {
        buffer.append(' ');
        capitalizeNext = true;
      } else if (capitalizeNext) {
        buffer.append(Character.toTitleCase(ch));
        capitalizeNext = false;
      } else if (Character.isUpperCase(ch) || Character.isTitleCase(ch)) {
        buffer.append(' ');
        buffer.append(ch);
      } else {
        buffer.append(ch);
      }
    }
    return buffer.toString();
  }

  public String getDescription() {
    return "Returns titleized string.";
  }

  public String getName() {
    return "Titleize";
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
