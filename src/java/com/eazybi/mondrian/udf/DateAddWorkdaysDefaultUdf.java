package com.eazybi.mondrian.udf;

import mondrian.olap.*;
import mondrian.olap.type.*;
import mondrian.spi.UserDefinedFunction;

public class DateAddWorkdaysDefaultUdf extends DateAddWorkdaysUdf {
  public Type[] getParameterTypes() {
    return new Type[] {
      new ScalarType(),
      new NumericType()
    };
  }
}
