package com.eazybi.mondrian.udf;

import mondrian.olap.*;
import mondrian.olap.type.*;
import mondrian.spi.UserDefinedFunction;
import mondrian.mdx.*;
import mondrian.calc.*;

import java.util.*;

public class CascadingChildrenSetUdf extends ChildrenSetUdf {
  public Object execute(Evaluator evaluator, Argument[] arguments) {
    Member member = (Member) arguments[0].evaluate(evaluator);
    return childrenSet(evaluator, member, true);
  }

  public String getDescription() {
    return "Returns cascading children set of aggregate calculated member (set argument of Aggregate function).";
  }

  public String getName() {
    return "CascadingChildrenSet";
  }

}
