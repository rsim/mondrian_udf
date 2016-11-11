package com.eazybi.mondrian.udf;

import mondrian.olap.type.*;

public class DimensionGetNumberUdf extends GetNumberUdf {

  public Type[] getParameterTypes() {
    return new Type[] {
      DimensionType.Unknown,
      new StringType()
    };
  }

}
