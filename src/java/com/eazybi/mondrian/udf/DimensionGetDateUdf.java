package com.eazybi.mondrian.udf;

import mondrian.olap.type.*;

public class DimensionGetDateUdf extends GetDateUdf {

  public Type[] getParameterTypes() {
    return new Type[] {
      DimensionType.Unknown,
      new StringType()
    };
  }

}
