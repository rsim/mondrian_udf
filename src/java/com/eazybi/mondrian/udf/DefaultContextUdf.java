package com.eazybi.mondrian.udf;

import mondrian.olap.*;
import mondrian.olap.type.*;
import mondrian.spi.UserDefinedFunction;

import java.util.*;

public class DefaultContextUdf implements UserDefinedFunction {
  public Object execute(Evaluator evaluator, Argument[] arguments) {
    Object arg = arguments[0].evaluate(evaluator);
    Member[] tupleMembers;
    if (arg instanceof Member ) {
      tupleMembers = new Member[] {(Member) arg};
    } else if (arg instanceof Member[]) {
      tupleMembers = (Member[]) arg;
    } else {
      return null;
    }

    final int savepoint = evaluator.savepoint();
    try {
      for (Member currentMember : evaluator.getNonAllMembers()) {
        boolean inTuple = false;
        Hierarchy hierarchy = currentMember.getHierarchy();
        for (Member tupleMember : tupleMembers) {
          if (hierarchy.equals(tupleMember.getHierarchy())) {
            inTuple = true;
            break;
          }
        }
        if (!inTuple) {
          evaluator.setContext(hierarchy.getDefaultMember());
        }
      }

      evaluator.setContext(tupleMembers);
      return evaluator.evaluateCurrent();
    } finally {
      evaluator.restore(savepoint);
    }
  }

  public String getDescription() {
    return "Returns tuple evaluation result in default context.";
  }

  public String getName() {
    return "DefaultContext";
  }

  public Type[] getParameterTypes() {
    return new Type[] {
      new TupleType(new Type[]{})
    };
  }

  public String[] getReservedWords() {
    return null;
  }

  public Type getReturnType(Type[] parameterTypes) {
    return new NumericType();
  }

  public Syntax getSyntax() {
    return Syntax.Function;
  }

}
