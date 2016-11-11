package com.eazybi.mondrian.udf;

import mondrian.olap.type.*;

public class DimensionGetBooleanUdf extends GetBooleanUdf {

  public Type[] getParameterTypes() {
    return new Type[] {
      DimensionType.Unknown,
      new StringType()
    };
  }

}
