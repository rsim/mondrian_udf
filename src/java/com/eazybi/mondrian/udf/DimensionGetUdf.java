package com.eazybi.mondrian.udf;

import mondrian.olap.type.*;

public class DimensionGetUdf extends GetUdf {

  public Type[] getParameterTypes() {
    return new Type[] {
      DimensionType.Unknown,
      new StringType()
    };
  }

}
