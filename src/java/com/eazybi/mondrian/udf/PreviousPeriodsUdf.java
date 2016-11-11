package com.eazybi.mondrian.udf;

import mondrian.olap.*;
import mondrian.olap.type.*;
import mondrian.spi.UserDefinedFunction;
import mondrian.mdx.*;
import mondrian.calc.*;

import java.util.*;

public class PreviousPeriodsUdf implements UserDefinedFunction {
  private HashMap<Member, List<Member>> previousPeriodsCache = new HashMap<Member, List<Member>>();

  public Object execute(Evaluator evaluator, Argument[] arguments) {
    Member member = (Member) arguments[0].evaluate(evaluator);
    return previousPeriods(evaluator, member);
  }

  protected List<Member> previousPeriods(Evaluator evaluator, Member member) {
    List<Member> resultsList = previousPeriodsCache.get(member);
    if (resultsList != null) return resultsList;

    if (member.isCalculated()) {
      List<Member> membersList = DateUtils.calculatedChildrenSet(evaluator, member, true);
      if (membersList.size() == 1) {
        member = membersList.get(0);
      }
    }

    resultsList = new ArrayList<Member>();
    SchemaReader schemaReader = evaluator.getSchemaReader();

    Member currentMember = member;
    while (currentMember != null) {
      Level level = currentMember.getLevel();
      Member parentMember = currentMember.getParentMember();
      if (parentMember == null) {
        // if there is no All Time member then get all root elements until member
        if (!currentMember.isAll() && !currentMember.isCalculated() && !currentMember.isNull()) {
          List<Member> rootMembers = schemaReader.getLevelMembers(level, false);
          List<Member> previousSiblings = new ArrayList<Member>();
          for (Member rootMember : rootMembers) {
            if (rootMember.equals(currentMember)) break;
            previousSiblings.add(rootMember);
          }
          resultsList.addAll(0, previousSiblings);
        }
      } else {
        Member firstSibling = schemaReader.getMemberChildren(parentMember).get(0);
        if (!firstSibling.equals(currentMember)) {
          List<Member> previousSiblings = new ArrayList<Member>();
          schemaReader.getMemberRange(level, firstSibling, currentMember, previousSiblings);
          previousSiblings.remove(previousSiblings.size() - 1);
          resultsList.addAll(0, previousSiblings);
        }
      }
      currentMember = parentMember;
    }

    previousPeriodsCache.put(member, resultsList);
    return resultsList;
  }

  public String getDescription() {
    return "Returns set of previous time dimension members for calculation of cumulative sums.";
  }

  public String getName() {
    return "PreviousPeriods";
  }

  public Type[] getParameterTypes() {
    return new Type[] {
      MemberType.Unknown
    };
  }

  public String[] getReservedWords() {
    return null;
  }

  public Type getReturnType(Type[] parameterTypes) {
    return new SetType(MemberType.Unknown);
  }

  public Syntax getSyntax() {
    return Syntax.Function;
  }

}
