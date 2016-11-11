package com.eazybi.mondrian.udf;

import mondrian.olap.*;
import mondrian.olap.type.*;
import mondrian.spi.UserDefinedFunction;

public class DateDiffWorkdaysDefaultUdf extends DateDiffWorkdaysUdf {
  public Type[] getParameterTypes() {
    return new Type[] {
      new ScalarType(),
      new ScalarType(),
    };
  }
}
