package com.eazybi.mondrian.udf;

import mondrian.olap.*;
import mondrian.olap.type.*;
import mondrian.spi.UserDefinedFunction;

import java.sql.Clob;

import java.util.*;
import java.text.SimpleDateFormat;

public class GetStringUdf extends GetPropertyUdf {
  public Object execute(Evaluator evaluator, Argument[] arguments) {
    Object value = getPropertyValue(evaluator, arguments);
    if (value instanceof String) {
      return value;
    } else if (value instanceof Number || value instanceof Boolean) {
      return value.toString();
    } else if (value instanceof Date) {
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      return dateFormat.format(value).replace(" 00:00:00", "");
    } else {
      return null;
    }
  }

  public String getName() {
    return "getString";
  }

  public Type getReturnType(Type[] parameterTypes) {
    return new StringType();
  }

}
