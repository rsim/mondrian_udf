package com.eazybi.mondrian.udf;

import mondrian.olap.*;
import mondrian.olap.type.*;
import mondrian.spi.UserDefinedFunction;

import java.sql.Clob;
import java.sql.SQLException;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.IOException;

import java.util.*;

public class GetPropertyUdf implements UserDefinedFunction {
  private HashMap<String, HashMap<Object, Member>> levelPropertiesMemberCache = new HashMap<String, HashMap<Object, Member>>();

  public Object execute(Evaluator evaluator, Argument[] arguments) {
    return getPropertyValue(evaluator, arguments);
  }

  public String getDescription() {
    return "Returns member property value or null if property does not exist.";
  }

  public String getName() {
    return "getProperty";
  }

  public Type[] getParameterTypes() {
    return new Type[] {
      MemberType.Unknown,
      new StringType()
    };
  }

  public String[] getReservedWords() {
    return null;
  }

  public Type getReturnType(Type[] parameterTypes) {
    return new ScalarType();
  }

  public Syntax getSyntax() {
    return Syntax.Method;
  }

  protected Object getPropertyValue(Evaluator evaluator, Argument[] arguments) {
    Member member;
    Object memberOrDimension = arguments[0].evaluate(evaluator);
    if (memberOrDimension instanceof Member) {
      member = (Member) memberOrDimension;
    } else if (memberOrDimension instanceof Dimension) {
      member = getCurrentHierarchyMember(evaluator, (Dimension) memberOrDimension);
    } else {
      return null;
    }

    String propertyName = (String) arguments[1].evaluateScalar(evaluator);
    Object value = getMemberPropertyValue(member, propertyName);

    if (value == null) {
      Level level = member.getLevel();
      if (!Util.isValidProperty(propertyName, level)) {
        // looukp from level properties_from annotation
        String propertiesFrom = getLevelAnnotation(level, "properties_from");
        if (propertiesFrom != null) {
          HashMap<Object,Member> propertiesMemberCache = levelPropertiesMemberCache.get(propertiesFrom);
          // populate level member properties HashMap during the first time
          if (propertiesMemberCache == null) {
            propertiesMemberCache = new HashMap<Object,Member>();
            List<Id.Segment> segments = Util.parseIdentifier(propertiesFrom);
            OlapElement olapElement = evaluator.getSchemaReader().lookupCompound(
              evaluator.getCube(),
              Util.parseIdentifier(propertiesFrom),
              false,
              Category.Level
            );
            if (olapElement instanceof Level) {
              Level propertiesLevel = (Level) olapElement;
              Object mKey;
              for (Member m : evaluator.getSchemaReader().getLevelMembers(propertiesLevel, false)) {
                mKey = m.getPropertyValue("KEY");
                propertiesMemberCache.put(mKey, m);
              }
            }
            levelPropertiesMemberCache.put(propertiesFrom, propertiesMemberCache);
          }

          Member propertiesMember = propertiesMemberCache.get(member.getPropertyValue("KEY"));
          if (propertiesMember != null) {
            value = getMemberPropertyValue(propertiesMember, propertyName);
          }
        }
      }
    }

    return value;
  }

  private Member getCurrentHierarchyMember(Evaluator evaluator, Dimension dimension) {
    Member defaultHierarchyCurrentMember = null, hCurrentMember;
    for (Hierarchy h : dimension.getHierarchies()) {
      hCurrentMember = evaluator.getContext(h);
      if (! hCurrentMember.isAll()) return hCurrentMember;
      if (defaultHierarchyCurrentMember == null) defaultHierarchyCurrentMember = hCurrentMember;
    }
    return defaultHierarchyCurrentMember;
  }

  protected Object getMemberPropertyValue(Member member, String propertyName) {
    Object value = member.getPropertyValue(propertyName);
    if (value instanceof java.sql.Date || value instanceof java.sql.Timestamp) {
      return new Date(((Date) value).getTime());
    } else if (value instanceof Clob) {
      return clobToString((Clob) value);
    }
    return value;
  }

  private String clobToString(Clob data) {
    StringBuilder sb = new StringBuilder();
    try {
      Reader reader = data.getCharacterStream();
      BufferedReader br = new BufferedReader(reader);

      String line;
      boolean isFirst = true;
      while(null != (line = br.readLine())) {
        if (isFirst) {
          isFirst = false;
        } else {
          sb.append("\n");
        }
        sb.append(line);
      }
      br.close();
    } catch (SQLException e) {
      return null;
    } catch (IOException e) {
      return null;
    }
    return sb.toString();
  }

  private String getLevelAnnotation(Level level, String annotationName) {
    Annotation annotation = level.getAnnotationMap().get(annotationName);
    if (annotation != null) {
      return (String) annotation.getValue();
    }
    return null;
  }

}
