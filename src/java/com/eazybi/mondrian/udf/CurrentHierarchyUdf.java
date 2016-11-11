package com.eazybi.mondrian.udf;

import mondrian.olap.*;
import mondrian.olap.type.*;
import mondrian.spi.UserDefinedFunction;

import java.util.*;

public class CurrentHierarchyUdf implements UserDefinedFunction {
  public Object execute(Evaluator evaluator, Argument[] arguments) {
    Hierarchy hierarchy = (Hierarchy) arguments[0].evaluate(evaluator);
    Member hierarchyCurrentMember = evaluator.getContext(hierarchy);
    if (! hierarchyCurrentMember.isAll()) return hierarchy;

    Member hCurrentMember;
    for (Hierarchy h : hierarchy.getDimension().getHierarchies()) {
      if (h.equals(hierarchy)) continue;
      hCurrentMember = evaluator.getContext(h);
      if (! hCurrentMember.isAll()) return h;
    }

    return hierarchy;
  }

  public String getDescription() {
    return "Returns current hierarchy of dimension based on current context.";
  }

  public String getName() {
    return "CurrentHierarchy";
  }

  public Type[] getParameterTypes() {
    return new Type[] {
      HierarchyType.Unknown
    };
  }

  public String[] getReservedWords() {
    return null;
  }

  public Type getReturnType(Type[] parameterTypes) {
    return HierarchyType.Unknown;
  }

  public Syntax getSyntax() {
    return Syntax.Property;
  }

}
