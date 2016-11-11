package com.eazybi.mondrian.udf;

import mondrian.olap.*;
import mondrian.olap.type.*;
import mondrian.spi.UserDefinedFunction;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class ExtractStringUdf implements UserDefinedFunction {
  public Object execute(Evaluator evaluator, Argument[] arguments) {
    String s = (String) arguments[0].evaluateScalar(evaluator);
    if (s == null || s.length() == 0) return null;

    String regex = (String) arguments[1].evaluateScalar(evaluator);
    if (regex == null || regex.length() == 0) return null;

    int group;
    if (arguments.length == 3) {
      group = ((Number) arguments[2].evaluateScalar(evaluator)).intValue();
    } else {
      group = 0;
    }
    if (group < 0) return null;

    return extract(s, regex, group);
  }

  String extract(String s, String regex, int group) {
    Pattern pattern = Pattern.compile("(?m)[\\s\\S]*(" + regex + ")[\\s\\S]*");
    Matcher m = pattern.matcher(s);
    if (m.matches()) {
      return m.group(group + 1);
    } else {
      return null;
    }
  }

  public String getDescription() {
    return "Extracts string fragment using regular expression.";
  }

  public String getName() {
    return "ExtractString";
  }

  public Type[] getParameterTypes() {
    return new Type[] {
      new ScalarType(),
      new StringType(),
      new NumericType()
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
