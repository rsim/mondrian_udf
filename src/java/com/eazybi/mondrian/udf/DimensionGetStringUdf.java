package com.eazybi.mondrian.udf;

import mondrian.olap.type.*;

public class DimensionGetStringUdf extends GetStringUdf {

  public Type[] getParameterTypes() {
    return new Type[] {
      DimensionType.Unknown,
      new StringType()
    };
  }

}
