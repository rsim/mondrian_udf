package com.eazybi.mondrian.udf;

import mondrian.olap.*;
import mondrian.olap.type.*;
import mondrian.spi.UserDefinedFunction;

import java.util.*;
import java.text.SimpleDateFormat;

public class AllPropertiesUdf extends GetPropertyUdf {
  public Object execute(Evaluator evaluator, Argument[] arguments) {
    Member member = (Member) arguments[0].evaluate(evaluator);
    if (member == null || member.isAll()) return null;
    Level level = member.getLevel();

    StringBuilder sb = new StringBuilder();

    sb.append("KEY: ");
    sb.append(getMemberPropertyAsString(member, "KEY"));

    for (Property property : level.getProperties()) {
      if (property.isInternal()) continue;

      String propertyName = property.getCaption();
      sb.append("\n");
      sb.append(propertyName);
      sb.append(": ");
      sb.append(getMemberPropertyAsString(member, propertyName));
    }

    return sb.toString();
  }

  public String getDescription() {
    return "Returns all property names and values for a member.";
  }

  public String getName() {
    return "AllProperties";
  }

  public Type[] getParameterTypes() {
    return new Type[] {
      MemberType.Unknown
    };
  }

  public Type getReturnType(Type[] parameterTypes) {
    return new StringType();
  }

  public Syntax getSyntax() {
    return Syntax.Property;
  }

  private String getMemberPropertyAsString(Member member, String propertyName) {
    Object value = getMemberPropertyValue(member, propertyName);
    if (value == null) {
      return "";
    } else if (value instanceof String) {
      if (((String) value).contains("\n")) {
        return "\n\u00A0\u00A0" + ((String) value).replaceAll("\\r\\n|\\r|\\n", "\n\u00A0\u00A0");
      } else {
        return (String) value;
      }
    } else if (value instanceof Date) {
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      return dateFormat.format(value).replace(" 00:00:00", "");
    } else {
      return value.toString();
    }
  }
}
