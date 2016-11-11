# encoding: utf-8

require 'spec_helper'

describe "Mondrian UDF" do
  include OlapSpecHelper

  describe "user defined function" do
    before(:all) do
      @olap = olap_connection
    end

    %w(get getProperty).each do |function_name|
      describe function_name do
        it "should return value if property exists" do
          result = @olap.from('Sales').
            with_member('[Measures].[State Country]').as("[Customers].CurrentMember.#{function_name}('Country')").
            columns('[Measures].[State Country]').
            rows('[Customers].[USA].[CA]').
            execute
          result.values.should == [['USA']]
        end

        it "should return nil if property does not exist" do
          result = @olap.from('Sales').
            with_member('[Measures].[missing]').as("[Customers].CurrentMember.#{function_name}('missing')").
            columns('[Measures].[missing]').
            rows('[Customers].[USA].[CA]').
            execute
          result.values.should == [[nil]]
        end

        it "should return number property" do
          result = @olap.from('Sales').
            with_member('[Measures].[Total Children]').as("[Customers].CurrentMember.#{function_name}('Total Children')").
            columns('[Measures].[Total Children]').
            rows('[Customers].[Name].&[1]').
            execute
          result.values.should == [[4]]
        end

        it "should return date property" do
          result = @olap.from('Sales').
            with_member('[Measures].[Birth Date]').as("[Customers].CurrentMember.#{function_name}('Birth Date')").
            columns('[Measures].[Birth Date]').
            rows('[Customers].[Name].&[1]').
            execute
          result.values[0][0].class.should == java.util.Date
          result.values[0][0].getTime.should == java.util.Date.new(1961-1900,8-1,26).getTime # value from FoodMart database for customer_id = 1
        end

        it "should return boolean property" do
          result = @olap.from('Sales').
            with_member('[Measures].[Low Fat]').as("[Products].CurrentMember.#{function_name}('Low Fat')").
            columns('[Measures].[Low Fat]').
            rows('[Products].[Product Name].&[Washington Mango Drink]').
            execute
          result.values.should == [[true]]
        end
      end
    end

    describe "get using properties_from annotation" do
      it "should return value if property exists" do
        result = @olap.from('Sales').
          with_member('[Measures].[State Country]').as("[Customers].CurrentHierarchyMember.get('Country')").
          columns('[Measures].[State Country]').
          rows('[Customers.Country].[USA].[CA]').
          execute
        result.values.should == [['USA']]
      end

      it "should get non-default hierarchy property using dimension get syntax" do
        result = @olap.from('Sales').
          with_member('[Measures].[State Country]').as("[Customers].get('Country')").
          columns('[Measures].[State Country]').
          rows('[Customers.Country].[USA].[CA]').
          execute
        result.values.should == [['USA']]
      end
    end

    describe "getString" do
      it "should return string value if property exists" do
        result = @olap.from('Sales').
          with_member('[Measures].[City Country]').as("[Customers].CurrentMember.getString('Country')").
          columns('[Measures].[City Country]').
          rows('[Customers].[USA].[CA]').
          execute
        result.values.should == [['USA']]
      end

      it "should return nil if property does not exist" do
        result = @olap.from('Sales').
          with_member('[Measures].[missing]').as("[Customers].CurrentMember.getString('missing')").
          columns('[Measures].[missing]').
          rows('[Customers].[USA].[CA]').
          execute
        result.values.should == [[nil]]
      end

      it "should return number property as string" do
        result = @olap.from('Sales').
          with_member('[Measures].[Total Children]').as("[Customers].CurrentMember.getString('Total Children')").
          columns('[Measures].[Total Children]').
          rows('[Customers].[Name].&[1]').
          execute
        result.values.should == [['4']]
      end

      it "should return date property as string" do
        result = @olap.from('Sales').
          with_member('[Measures].[Birth Date]').as("[Customers].CurrentMember.getString('Birth Date')").
          columns('[Measures].[Birth Date]').
          rows('[Customers].[Name].&[1]').
          execute
        result.values.should == [['1961-08-26']]
      end

      it "should return boolean property as string" do
        result = @olap.from('Sales').
          with_member('[Measures].[Low Fat]').as("[Products].CurrentMember.getString('Low Fat')").
          columns('[Measures].[Low Fat]').
          rows('[Products].[Product Name].&[Washington Mango Drink]').
          execute
        result.values.should == [['true']]
      end

      it "should return non-default hierarchy property using dimension get syntax" do
        result = @olap.from('Sales').
          with_member('[Measures].[State Country]').as("[Customers].getString('Country')").
          columns('[Measures].[State Country]').
          rows('[Customers.Country].[USA].[CA]').
          execute
        result.values.should == [['USA']]
      end
    end

    describe "getNumber" do
      it "should return number property if property exists" do
        result = @olap.from('Sales').
          with_member('[Measures].[Total Children]').as("[Customers].CurrentMember.getNumber('Total Children')").
          columns('[Measures].[Total Children]').
          rows('[Customers].[Name].&[1]').
          execute
        result.values.should == [[4]]
      end

      it "should return nil if property does not exist" do
        result = @olap.from('Sales').
          with_member('[Measures].[missing]').as("[Customers].CurrentMember.getNumber('missing')").
          columns('[Measures].[missing]').
          rows('[Customers].[USA].[CA]').
          execute
        result.values.should == [[nil]]
      end

      it "should return string property as number if can be parsed as numeric" do
        result = @olap.from('Sales').
          with_member('[Measures].[Postal Code]').as("[Customers].CurrentMember.getNumber('Postal Code')").
          columns('[Measures].[Postal Code]').
          rows('[Customers].[Name].&[1]').
          execute
        result.values.should == [[15057]]
      end

      it "should return non-default hierarchy property using dimension get syntax" do
        result = @olap.from('Sales').
          with_member('[Measures].[Total Children]').as("[Customers].getNumber('Total Children')").
          columns('[Measures].[Total Children]').
          rows('[Customers.Country].[Name].&[1]').
          execute
        result.values.should == [[4]]
      end
    end

    describe "getDate" do
      it "should return date value if property exists" do
        result = @olap.from('Sales').
          with_member('[Measures].[Birth Date]').as("[Customers].CurrentMember.getDate('Birth Date')").
          columns('[Measures].[Birth Date]').
          rows('[Customers].[Name].&[1]').
          execute
        result.values[0][0].class.should == java.util.Date
        result.values[0][0].getTime.should == java.util.Date.new(1961-1900,8-1,26).getTime
      end

      it "should return nil if property does not exist" do
        result = @olap.from('Sales').
          with_member('[Measures].[missing]').as("[Customers].CurrentMember.getDate('missing')").
          columns('[Measures].[missing]').
          rows('[Customers].[USA].[CA]').
          execute
        result.values.should == [[nil]]
      end

      it "should return non-default hierarchy property using dimension get syntax" do
        result = @olap.from('Sales').
          with_member('[Measures].[Birth Date]').as("[Customers].getDate('Birth Date')").
          columns('[Measures].[Birth Date]').
          rows('[Customers.Country].[Name].&[1]').
          execute
        result.values[0][0].class.should == java.util.Date
        result.values[0][0].getTime.should == java.util.Date.new(1961-1900,8-1,26).getTime
      end
    end

    describe "getBoolean" do
      it "should return boolean value if property exists" do
        result = @olap.from('Sales').
          with_member('[Measures].[Low Fat]').as("[Products].CurrentMember.getBoolean('Low Fat')").
          columns('[Measures].[Low Fat]').
          rows('[Products].[Product Name].&[Washington Mango Drink]').
          execute
        result.values.should == [[true]]
      end

      it "should return false if property does not exist" do
        result = @olap.from('Sales').
          with_member('[Measures].[missing]').as("[Customers].CurrentMember.getBoolean('missing')").
          columns('[Measures].[missing]').
          rows('[Customers].[USA].[CA]').
          execute
        result.values.should == [[false]]
      end

      it "should return numer property value 1 as true" do
        result = @olap.from('Sales').
          with_member('[Measures].[Total Children]').as("[Customers].CurrentMember.getBoolean('KEY')").
          columns('[Measures].[Total Children]').
          rows('[Customers].[Name].&[1]').
          execute
        result.values.should == [[true]]
      end

      it "should return non-default hierarchy property using dimension get syntax" do
        result = @olap.from('Sales').
          with_member('[Measures].[Total Children]').as("[Customers].getBoolean('KEY')").
          columns('[Measures].[Total Children]').
          rows('[Customers.Country].[Name].&[1]').
          execute
        result.values.should == [[true]]
      end
    end

    describe "AllProperties" do
      it "should return all member properties" do
        result = @olap.from('Sales').
          with_member('[Measures].[all properties]').as("[Customers].CurrentMember.AllProperties").
          columns('[Measures].[all properties]').
          rows('[Customers].[Name].&[1]').
          execute
        result.values.should == [[
          [
            "KEY: 1",
            "Birth Date: 1961-08-26",
            "Total Children: 4",
            "Postal Code: 15057"
          ].join("\n")
        ]]
      end

    end

    describe "DateDiffDays" do
      it "should return difference in days between dates" do
        result = @olap.from('Sales').
          with_member('[Measures].[difference]').as("DateDiffDays(DateSerial(2013,5,10),DateSerial(2013,5,11))").
          columns('[Measures].[difference]').
          execute
        result.values.should == [1]
        result.values[0].class.should == Float
      end

      it "should return nil if one of arguments is nil" do
        result = @olap.from('Sales').
          with_member('[Measures].[difference]').as("DateDiffDays(DateSerial(2013,5,10),NULL)").
          columns('[Measures].[difference]').
          execute
        result.values.should == [nil]
      end

      describe "Daylight saving" do
        it "should return difference in days between dates when changes to winter time" do
          result = @olap.from('Sales').
            with_member('[Measures].[difference]').as("DateDiffDays(DateSerial(2016,10,22),DateSerial(2016,10,31))").
            columns('[Measures].[difference]').
            execute
          result.values.should == [9]
          result.values[0].class.should == Float
        end

        it "should return difference in days between dates when changes to summer time" do
          result = @olap.from('Sales').
            with_member('[Measures].[difference]').as("DateDiffDays(DateSerial(2016,3,20),DateSerial(2016,3,28))").
            columns('[Measures].[difference]').
            execute
          result.values.should == [8]
          result.values[0].class.should == Float
        end
      end
    end

    describe "DateDiffWorkdays" do
      [nil, ",'67'"].each do |nonworkdays|
        it "should return workdays between dates#{nonworkdays}" do
          result = @olap.from('Sales').
            with_member('[Measures].[difference]').as("DateDiffWorkdays(DateSerial(2013,8,20),DateSerial(2013,8,27)#{nonworkdays})").
            columns('[Measures].[difference]').
            execute
          result.values.should == [5]
          result.values[0].class.should == Float
        end

        it "should return workdays between dates with time#{nonworkdays}" do
          result = @olap.from('Sales').
            with_member('[Measures].[difference]').as("DateDiffWorkdays('2013-08-20 10:00:00','2013-08-26 16:00:00'#{nonworkdays})").
            columns('[Measures].[difference]').
            execute
          result.values.should == [4.25]
        end

        it "should return negative workdays between dates in oposite order#{nonworkdays}" do
          result = @olap.from('Sales').
            with_member('[Measures].[difference]').as("DateDiffWorkdays('2013-08-26 16:00:00','2013-08-20 10:00:00'#{nonworkdays})").
            columns('[Measures].[difference]').
            execute
          result.values.should == [-4.25]
        end

        it "should return workdays between dates in same week#{nonworkdays}" do
          result = @olap.from('Sales').
            with_member('[Measures].[difference]').as("DateDiffWorkdays('2013-08-20 10:00:00','2013-08-21 16:00:00'#{nonworkdays})").
            columns('[Measures].[difference]').
            execute
          result.values.should == [1.25]
        end


        it "should return workdays between dates in non-working days#{nonworkdays}" do
          result = @olap.from('Sales').
            with_member('[Measures].[difference]').as("DateDiffWorkdays('2013-08-17 10:00:00','2013-08-25 16:00:00'#{nonworkdays})").
            columns('[Measures].[difference]').
            execute
          result.values.should == [5]
        end

        it "should return 0 workdays between dates in same weekend#{nonworkdays}" do
          result = @olap.from('Sales').
            with_member('[Measures].[difference]').as("DateDiffWorkdays('2013-08-17 10:00:00','2013-08-18 16:00:00'#{nonworkdays})").
            columns('[Measures].[difference]').
            execute
          result.values.should == [0]
        end
      end

      it "should return workdays with non-default non-working days" do
        result = @olap.from('Sales').
          with_member('[Measures].[difference]').as("DateDiffWorkdays('2013-08-17 12:00:00','2013-08-20 18:00:00','71')").
          columns('[Measures].[difference]').
          execute
        result.values.should == [1.25]
      end

      describe "Daylight saving" do
        it "should return workdays between dates when changes to winter time" do
          result = @olap.from('Sales').
            with_member('[Measures].[difference]').as("DateDiffWorkdays(DateSerial(2016,10,22),DateSerial(2016,10,31), '67')").
            columns('[Measures].[difference]').
            execute
          result.values.should == [5]
          result.values[0].class.should == Float
        end

        it "should return workdays between dates when changes to winter time and only one workday" do
          result = @olap.from('Sales').
            with_member('[Measures].[difference]').as("DateDiffWorkdays(DateSerial(2016,10,22),DateSerial(2016,10,30), '234567')").
            columns('[Measures].[difference]').
            execute
          result.values.should == [1]
          result.values[0].class.should == Float
        end

        it "should return workdays between dates when changes to summer time" do
          result = @olap.from('Sales').
            with_member('[Measures].[difference]').as("DateDiffWorkdays(DateSerial(2016,3,20),DateSerial(2016,3,28), '67')").
            columns('[Measures].[difference]').
            execute
          result.values.should == [5]
          result.values[0].class.should == Float
        end

        it "should return workdays between dates when changes to summer time and only one workday" do
          result = @olap.from('Sales').
            with_member('[Measures].[difference]').as("DateDiffWorkdays(DateSerial(2016,3,20),DateSerial(2016,3,28), '234567')").
            columns('[Measures].[difference]').
            execute
          result.values.should == [1]
          result.values[0].class.should == Float
        end
      end
    end

    describe "DateDiffHours" do
      it "should return difference in days between dates" do
        result = @olap.from('Sales').
          with_member('[Measures].[difference]').as("DateDiffHours(DateSerial(2013,5,10),DateSerial(2013,5,11))").
          columns('[Measures].[difference]').
          execute
        result.values.should == [24]
      end

      it "should return nil if one of arguments is nil" do
        result = @olap.from('Sales').
          with_member('[Measures].[difference]').as("DateDiffHours(DateSerial(2013,5,10),NULL)").
          columns('[Measures].[difference]').
          execute
        result.values.should == [nil]
      end
    end

    describe "DateDiffMinutes" do
      it "should return difference in days between dates" do
        result = @olap.from('Sales').
          with_member('[Measures].[difference]').as("DateDiffMinutes(DateSerial(2013,5,10),DateSerial(2013,5,11))").
          columns('[Measures].[difference]').
          execute
        result.values.should == [24 * 60]
      end

      it "should return nil if one of arguments is nil" do
        result = @olap.from('Sales').
          with_member('[Measures].[difference]').as("DateDiffMinutes(DateSerial(2013,5,10),NULL)").
          columns('[Measures].[difference]').
          execute
        result.values.should == [nil]
      end
    end

    describe "DateCompare" do
      it "should return -1 if first date is less than second date" do
        result = @olap.from('Sales').
          with_member('[Measures].[difference]').as("DateCompare(DateSerial(2013,5,10),DateSerial(2013,5,11))").
          columns('[Measures].[difference]').
          execute
        result.values.should == [-1]
      end

      it "should return 1 if first date is greater than second date" do
        result = @olap.from('Sales').
          with_member('[Measures].[difference]').as("DateCompare(DateSerial(2013,5,12),DateSerial(2013,5,11))").
          columns('[Measures].[difference]').
          execute
        result.values.should == [1]
      end

      it "should return 0 if dates are equal" do
        result = @olap.from('Sales').
          with_member('[Measures].[difference]').as("DateCompare(DateSerial(2013,5,11),DateSerial(2013,5,11))").
          columns('[Measures].[difference]').
          execute
        result.values.should == [0]
      end

      it "should return nil if one of arguments is nil" do
        result = @olap.from('Sales').
          with_member('[Measures].[difference]').as("DateCompare(DateSerial(2013,5,10),NULL)").
          columns('[Measures].[difference]').
          execute
        result.values.should == [nil]
      end
    end

    describe "DateAddDays" do
      it "should add days and return new date" do
        result = @olap.from('Sales').
          with_member('[Measures].[new date]').as("DateAddDays(DateSerial(2013,5,10),1)").
          columns('[Measures].[new date]').
          execute
        result.values[0].getTime.should == java.util.Date.new(2013-1900,5-1,10+1).getTime
        result.values[0].class.should == java.util.Date
      end

      it "should return nil if date argument is nil" do
        result = @olap.from('Sales').
          with_member('[Measures].[new date]').as("DateAddDays(NULL,1)").
          columns('[Measures].[new date]').
          execute
        result.values.should == [nil]
      end
    end

    describe "DateAddWorkdays" do
      [nil, ",'67'"].each do |nonworkdays|
        it "should add workdays and return new date#{nonworkdays}" do
          result = @olap.from('Sales').
            with_member('[Measures].[difference]').as("DateDiffDays(DateAddWorkdays(DateSerial(2013,8,20),5#{nonworkdays}),DateSerial(2013,8,27))").
            columns('[Measures].[difference]').
            execute
          result.values.should == [0]
        end

        it "should add workdays to date with time#{nonworkdays}" do
          result = @olap.from('Sales').
            with_member('[Measures].[difference]').as("DateDiffDays(DateAddWorkdays('2013-08-20 10:00:00',4.25#{nonworkdays}),'2013-08-26 16:00:00')").
            columns('[Measures].[difference]').
            execute
          result.values.should == [0]
        end

        it "should add negative workdays to date with time#{nonworkdays}" do
          result = @olap.from('Sales').
            with_member('[Measures].[difference]').as("DateDiffDays(DateAddWorkdays('2013-08-26 16:00:00',-4.25#{nonworkdays}),'2013-08-20 10:00:00')").
            columns('[Measures].[difference]').
            execute
          result.values.should == [0]
        end

        it "should add workdays and return date in same week#{nonworkdays}" do
          result = @olap.from('Sales').
            with_member('[Measures].[difference]').as("DateDiffDays(DateAddWorkdays('2013-08-20 10:00:00',1.25#{nonworkdays}),'2013-08-21 16:00:00')").
            columns('[Measures].[difference]').
            execute
          result.values.should == [0]
        end

        it "should add workdays to date in non-working day#{nonworkdays}" do
          result = @olap.from('Sales').
            with_member('[Measures].[difference]').as("DateDiffDays(DateAddWorkdays('2013-08-17 10:00:00',5#{nonworkdays}),'2013-08-26 00:00:00')").
            columns('[Measures].[difference]').
            execute
          result.values.should == [0]
        end

        it "should be nil when adding too large number of workdays" do
          result = @olap.from('Sales').
            with_member('[Measures].[date]').as("DateAddWorkdays(Now(),99999999999999999999.99)").
            columns('[Measures].[date]').
            execute
          result.values.should == [nil]
        end
      end

      it "should add workdays with non-default non-working days" do
        result = @olap.from('Sales').
          with_member('[Measures].[difference]').as("DateDiffDays(DateAddWorkdays('2013-08-17 12:00:00',1.25,'71'),'2013-08-20 18:00:00')").
          columns('[Measures].[difference]').
          execute
        result.values.should == [0]
      end
    end

    describe "DateParse" do
      it "should parse date from string" do
        result = @olap.from('Sales').
          with_member('[Measures].[new date]').as("DateParse('May 11 2013')").
          columns('[Measures].[new date]').
          execute
        result.values[0].getTime.should == java.util.Date.new(2013-1900,5-1,11).getTime
        result.values[0].class.should == java.util.Date
      end

      it "should return nil if string is not recognized as date" do
        result = @olap.from('Sales').
          with_member('[Measures].[new date]').as("DateParse('abc')").
          columns('[Measures].[new date]').
          execute
        result.values.should == [nil]
      end
    end

    describe "DateWithoutTime" do
      it "should parse date without time from string" do
        result = @olap.from('Sales').
          with_member('[Measures].[new date]').as("DateWithoutTime('2013-10-07 12:35:12')").
          columns('[Measures].[new date]').
          execute
        result.values[0].as_json.should == '2013-10-07'
      end

      it "should parse date without time from datetime" do
        result = @olap.from('Sales').
          with_member('[Measures].[new date]').as("DateWithoutTime(DateParse('2013-10-07 12:35:12'))").
          columns('[Measures].[new date]').
          execute
        result.values[0].as_json.should == '2013-10-07'
      end
    end

    describe "TimestampToDate" do
      it "should get date from Unix timestamp" do
        java_date = java.util.Date.new(2015-1900,3-1,10,12,13,14)
        timestamp = java_date.getTime / 1000
        result = @olap.from('Sales').
          with_member('[Measures].[date]').as("TimestampToDate(#{timestamp})").
          columns('[Measures].[date]').
          execute
        result.values[0].getTime.should == java_date.getTime
        result.values[0].class.should == java.util.Date
      end
    end

    describe "DateToTimestamp" do
      it "should get Unix timestamp from date" do
        java_date = java.util.Date.new(2015-1900,3-1,10,12,13,14)
        timestamp = java_date.getTime / 1000
        result = @olap.from('Sales').
          with_member('[Measures].[timestamp]').as("DateToTimestamp('#{java_date.as_json}')").
          columns('[Measures].[timestamp]').
          execute
        result.values[0].should == timestamp
      end
    end

    describe "DateBetween" do
      it "should be true if date is between specified dates" do
        result = @olap.from('Sales').
          with_member('[Measures].[date between]').as("DateBetween('May 11 2013', DateSerial(2013,5,1), DateSerial(2013,5,31))").
          columns('[Measures].[date between]').
          execute
        result.values.should == [true]
      end

      it "should be false if date is not between specified dates" do
        result = @olap.from('Sales').
          with_member('[Measures].[date between]').as("DateBetween('May 11 2013', DateSerial(2013,5,12), DateSerial(2013,5,31))").
          columns('[Measures].[date between]').
          execute
        result.values.should == [false]
      end

      it "should be false if one of dates is nil" do
        result = @olap.from('Sales').
          with_member('[Measures].[date between]').as("DateBetween('May 11 2013', DateSerial(2013,5,1), NULL)").
          columns('[Measures].[date between]').
          execute
        result.values.should == [false]
      end
    end

    describe "DateInPeriod" do
      it "should be true if date is between year start and end dates" do
        result = @olap.from('Sales').
          with_member('[Measures].[date in period]').as("DateInPeriod(DateSerial(1997,1,1), [Time].[1997])").
          columns('[Measures].[date in period]').
          execute
        result.values.should == [true]
      end

      it "should be false if date is not between year start and end dates" do
        result = @olap.from('Sales').
          with_member('[Measures].[date in period]').as("DateInPeriod(DateSerial(1998,1,1), [Time].[1997])").
          columns('[Measures].[date in period]').
          execute
        result.values.should == [false]
      end

      it "should be true if date is between quarter start and end dates" do
        result = @olap.from('Sales').
          with_member('[Measures].[date in period]').as("DateInPeriod(DateSerial(1997,1,1), [Time].[1997].[Q1])").
          columns('[Measures].[date in period]').
          execute
        result.values.should == [true]
      end

      it "should be false if date is not between quarter start and end dates" do
        result = @olap.from('Sales').
          with_member('[Measures].[date in period]').as("DateInPeriod(DateSerial(1997,4,1), [Time].[1997].[Q1])").
          columns('[Measures].[date in period]').
          execute
        result.values.should == [false]
      end

      it "should be true if date is between month start and end dates" do
        result = @olap.from('Sales').
          with_member('[Measures].[date in period]').as("DateInPeriod(DateSerial(1997,1,1), [Time].[1997].[Q1].[January])").
          columns('[Measures].[date in period]').
          execute
        result.values.should == [true]
      end

      it "should be false if date is not between month start and end dates" do
        result = @olap.from('Sales').
          with_member('[Measures].[date in period]').as("DateInPeriod(DateSerial(1997,2,1), [Time].[1997].[Q1].[January])").
          columns('[Measures].[date in period]').
          execute
        result.values.should == [false]
      end

      it "should be true if date is between week start and end dates" do
        result = @olap.from('Sales').
          with_member('[Measures].[date in period]').as("DateInPeriod(DateSerial(1997,1,1), [Time.Weekly].[1997].[W01, Dec 30 1996])").
          columns('[Measures].[date in period]').
          execute
        result.values.should == [true]
      end

      it "should be false if date is not between week start and end dates" do
        result = @olap.from('Sales').
          with_member('[Measures].[date in period]').as("DateInPeriod(DateSerial(1997,1,6), [Time.Weekly].[1997].[W01, Dec 30 1996])").
          columns('[Measures].[date in period]').
          execute
        result.values.should == [false]
      end

      it "should be true if date is between day start and end dates" do
        result = @olap.from('Sales').
          with_member('[Measures].[date in period]').as("DateInPeriod(DateSerial(1997,1,1), [Time].[1997].[Q1].[January].[1])").
          columns('[Measures].[date in period]').
          execute
        result.values.should == [true]
      end

      it "should be false if date is not between day start and end dates" do
        result = @olap.from('Sales').
          with_member('[Measures].[date in period]').as("DateInPeriod(DateSerial(1997,1,2), [Time].[1997].[Q1].[January].[1])").
          columns('[Measures].[date in period]').
          execute
        result.values.should == [false]
      end

      it "should be true if weekly hierarchy date is between day start and end dates" do
        result = @olap.from('Sales').
          with_member('[Measures].[date in period]').as("DateInPeriod(DateSerial(1997,1,1), [Time.Weekly].[1997].[W01, Dec 30 1996].[Jan 01 1997])").
          columns('[Measures].[date in period]').
          execute
        result.values.should == [true]
      end

      it "should be false if weekly hierarchy date is not between day start and end dates" do
        result = @olap.from('Sales').
          with_member('[Measures].[date in period]').as("DateInPeriod(DateSerial(1997,1,2), [Time.Weekly].[1997].[W01, Dec 30 1996].[Jan 01 1997])").
          columns('[Measures].[date in period]').
          execute
        result.values.should == [false]
      end

      it "should be true for non-empty date and all time member" do
        result = @olap.from('Sales').
          with_member('[Measures].[date in period]').as("DateInPeriod(DateSerial(1997,1,1), [Time].DefaultMember)").
          columns('[Measures].[date in period]').
          execute
        result.values.should == [true]
      end

      it "should be false for empty date and all time member" do
        result = @olap.from('Sales').
          with_member('[Measures].[date in period]').as("DateInPeriod('', [Time].DefaultMember)").
          columns('[Measures].[date in period]').
          execute
        result.values.should == [false]
      end

      it "should be true for non-empty date and weekly hierarchy all time member" do
        result = @olap.from('Sales').
          with_member('[Measures].[date in period]').as("DateInPeriod(DateSerial(1997,1,1), [Time.Weekly].DefaultMember)").
          columns('[Measures].[date in period]').
          execute
        result.values.should == [true]
      end

      it "should check if date is between aggregated member start and end dates" do
        result = @olap.from('Sales').
          with_member('[Time].[aggregated dates]').
            as("Aggregate({[Time].[1997].[Q1].[January].[1],[Time].[1997].[Q1].[January].[2]})").
          with_member('[Measures].[date in period 1]').as("DateInPeriod('1997-01-01', [Time].[aggregated dates])").
          with_member('[Measures].[date in period 2]').as("DateInPeriod('1997-01-02', [Time].[aggregated dates])").
          with_member('[Measures].[date in period 3]').as("DateInPeriod('1996-12-31', [Time].[aggregated dates])").
          with_member('[Measures].[date in period 4]').as("DateInPeriod('1997-01-03', [Time].[aggregated dates])").
          columns('[Measures].[date in period 1]', '[Measures].[date in period 2]',
                  '[Measures].[date in period 3]', '[Measures].[date in period 4]').
          execute
        result.values.should == [true, true, false, false]
      end

      it "should check if date is between cascading aggregated member start and end dates" do
        result = @olap.from('Sales').
          with_member('[Time].[aggregated dates 1]').
            as("Aggregate({[Time].[1997].[Q1].[January].[1],[Time].[1997].[Q1].[January].[2]})").
          with_member('[Time].[aggregated dates 2]').
            as("Aggregate({[Time].[1997].[Q1].[January].[3],[Time].[1997].[Q1].[January].[4]})").
          with_member('[Time].[aggregated dates]').
            as("Aggregate({CascadingChildrenSet([Time].[aggregated dates 1])," <<
              "CascadingChildrenSet([Time].[aggregated dates 2])})").
          with_member('[Measures].[date in period 1]').as("DateInPeriod('1997-01-01', [Time].[aggregated dates])").
          with_member('[Measures].[date in period 2]').as("DateInPeriod('1997-01-04', [Time].[aggregated dates])").
          with_member('[Measures].[date in period 3]').as("DateInPeriod('1996-12-31', [Time].[aggregated dates])").
          with_member('[Measures].[date in period 4]').as("DateInPeriod('1997-01-05', [Time].[aggregated dates])").
          columns('[Measures].[date in period 1]', '[Measures].[date in period 2]',
                  '[Measures].[date in period 3]', '[Measures].[date in period 4]').
          execute
        result.values.should == [true, true, false, false]
      end

      it "should check if date is between other calculated member start and end dates" do
        result = @olap.from('Sales').
          with_member('[Time].[aggregated dates 1]').
            as("[Time].[1997].[Q1].[January].Children.Item(0)").
          with_member('[Time].[aggregated dates 2]').
            as("[Time].[1997].[Q1].[January].Children.Item(1)").
          with_member('[Time].[aggregated dates]').
            as("Aggregate({CascadingChildrenSet([Time].[aggregated dates 1])," <<
              "CascadingChildrenSet([Time].[aggregated dates 2])})").
          with_member('[Measures].[date in period 1]').as("DateInPeriod('1997-01-01', [Time].[aggregated dates])").
          with_member('[Measures].[date in period 2]').as("DateInPeriod('1997-01-02', [Time].[aggregated dates])").
          with_member('[Measures].[date in period 3]').as("DateInPeriod('1996-12-31', [Time].[aggregated dates])").
          with_member('[Measures].[date in period 4]').as("DateInPeriod('1997-01-03', [Time].[aggregated dates])").
          columns('[Measures].[date in period 1]', '[Measures].[date in period 2]',
                  '[Measures].[date in period 3]', '[Measures].[date in period 4]').
          execute
        result.values.should == [true, true, false, false]
      end
    end

    describe "AnyDateInPeriod" do
      it "should be true if one date is between month start and end dates" do
        result = @olap.from('Sales').
          with_member('[Measures].[date in period]').as("AnyDateInPeriod('1997-01-01', [Time].[1997].[Q1].[January])").
          columns('[Measures].[date in period]').
          execute
        result.values.should == [true]
      end

      it "should be true if at least one date is between month start and end dates" do
        result = @olap.from('Sales').
          with_member('[Measures].[date in period]').as("AnyDateInPeriod('1997-01-01,1997-02-01', [Time].[1997].[Q1].[January])").
          columns('[Measures].[date in period]').
          execute
        result.values.should == [true]
      end

      it "should be false if no date is between month start and end dates" do
        result = @olap.from('Sales').
          with_member('[Measures].[date in period]').as("AnyDateInPeriod('1996-12-31,1997-02-01', [Time].[1997].[Q1].[January])").
          columns('[Measures].[date in period]').
          execute
        result.values.should == [false]
      end

      it "should be false if string is empty" do
        result = @olap.from('Sales').
          with_member('[Measures].[date in period]').as("AnyDateInPeriod('', [Time].[1997].[Q1].[January])").
          columns('[Measures].[date in period]').
          execute
        result.values.should == [false]
      end

      it "should be true for non-empty date and all time member" do
        result = @olap.from('Sales').
          with_member('[Measures].[date in period]').as("AnyDateInPeriod('1997-01-01', [Time].DefaultMember)").
          columns('[Measures].[date in period]').
          execute
        result.values.should == [true]
      end

      it "should be false for empty date and all time member" do
        result = @olap.from('Sales').
          with_member('[Measures].[date in period]').as("AnyDateInPeriod('', [Time].DefaultMember)").
          columns('[Measures].[date in period]').
          execute
        result.values.should == [false]
      end
    end

    describe "DateBeforePeriodEnd" do
      it "should be true if date is before month end" do
        result = @olap.from('Sales').
          with_member('[Measures].[date in period]').as("DateBeforePeriodEnd(DateSerial(1996,12,31), [Time].[1997].[Q1].[January])").
          columns('[Measures].[date in period]').
          execute
        result.values.should == [true]
      end

      it "should be false if date is not before month end" do
        result = @olap.from('Sales').
          with_member('[Measures].[date in period]').as("DateBeforePeriodEnd(DateSerial(1997,2,1), [Time].[1997].[Q1].[January])").
          columns('[Measures].[date in period]').
          execute
        result.values.should == [false]
      end
    end

    describe "DateAfterPeriodEnd" do
      it "should be true if date is after month end" do
        result = @olap.from('Sales').
          with_member('[Measures].[date in period]').as("DateAfterPeriodEnd(DateSerial(1997,2,1), [Time].[1997].[Q1].[January])").
          columns('[Measures].[date in period]').
          execute
        result.values.should == [true]
      end

      it "should be false if date is not after month end" do
        result = @olap.from('Sales').
          with_member('[Measures].[date in period]').as("DateAfterPeriodEnd(DateSerial(1997,1,31), [Time].[1997].[Q1].[January])").
          columns('[Measures].[date in period]').
          execute
        result.values.should == [false]
      end
    end

    describe "StartDate" do
      it "should return year start date" do
        result = @olap.from('Sales').
          with_member('[Measures].[date compare]').as("DateCompare([Time].[1997].StartDate, DateSerial(1997,1,1))").
          columns('[Measures].[date compare]').
          execute
        result.values.should == [0]
      end

      it "should return month start date" do
        result = @olap.from('Sales').
          with_member('[Measures].[date compare]').as("DateCompare([Time].[1997].[Q1].[February].StartDate, DateSerial(1997,2,1))").
          columns('[Measures].[date compare]').
          execute
        result.values.should == [0]
      end
    end

    describe "NextStartDate" do
      it "should return year next start date" do
        result = @olap.from('Sales').
          with_member('[Measures].[date compare]').as("DateCompare([Time].[1997].NextStartDate, DateSerial(1998,1,1))").
          columns('[Measures].[date compare]').
          execute
        result.values.should == [0]
      end

      it "should return month start date" do
        result = @olap.from('Sales').
          with_member('[Measures].[date compare]').as("DateCompare([Time].[1997].[Q1].[February].NextStartDate, DateSerial(1997,3,1))").
          columns('[Measures].[date compare]').
          execute
        result.values.should == [0]
      end
    end

    describe "CurrentDateMember" do
      it "should return current month or last month" do
        result = @olap.from('Sales').
          with_member('[Measures].[period full name]').as("[Time].[Month].CurrentDateMember.UniqueName").
          columns('[Measures].[period full name]').
          execute
        result.values.should == ['[Time].[1998].[Q4].[December]']
      end
    end

    describe "DateMember" do
      it "should return selected day" do
        result = @olap.from('Sales').
          with_member('[Measures].[period full name]').as("[Time].[Day].DateMember('Dec 13 1997').UniqueName").
          columns('[Measures].[period full name]').
          execute
        result.values.should == ['[Time].[1997].[Q4].[December].[13]']
      end

      it "should return null member for invalid date" do
        result = @olap.from('Sales').
          with_member('[Measures].[period full name]').as("[Time].[Day].DateMember('qwerty').UniqueName").
          columns('[Measures].[period full name]').
          execute
        result.values.should == ['[Time].[#null]']
      end
    end

    describe "getMemberByKey" do
      it "should return member with specified key" do
        result = @olap.from('Sales').
          with_member('[Measures].[name]').as("[Customers].[Name].getMemberByKey(1).Name").
          columns('[Measures].[name]').
          execute
        result.values.should == ['Sheri Nowmer']
      end

      it "should return null member if specified key is missing" do
        result = @olap.from('Sales').
          with_member('[Measures].[name]').as("[Customers].[Name].getMemberByKey(-1).Name").
          columns('[Measures].[name]').
          execute
        result.values.should == ['#null']
      end
    end

    describe "getMemberNameByKey" do
      it "should return member name with specified key" do
        result = @olap.from('Sales').
          with_member('[Measures].[name]').as("[Customers].[Name].getMemberNameByKey(1)").
          columns('[Measures].[name]').
          execute
        result.values.should == ['Sheri Nowmer']
      end

      it "should return nil if specified key is missing" do
        result = @olap.from('Sales').
          with_member('[Measures].[name]').as("[Customers].[Name].getMemberNameByKey(-1)").
          columns('[Measures].[name]').
          execute
        result.values.should == [nil]
      end
    end

    describe "getMembersByKeys" do
      it "should return set of members with specified string keys" do
        result = @olap.from('Sales').
          with_member('[Measures].[set]').as("SetToStr([Customers].[State Province].getMembersByKeys('CA,WA'))").
          columns('[Measures].[set]').
          execute
        result.values.should == ['{[Customers].[USA].[CA], [Customers].[USA].[WA]}']
      end

      it "should return set of members with specified integer keys" do
        result = @olap.from('Sales').
          with_member('[Measures].[names]').as("Generate([Customers].[Name].getMembersByKeys('1,2'),[Customers].CurrentMember.Name,',')").
          columns('[Measures].[names]').
          execute
        result.values.should == ['Sheri Nowmer,Derrick Whelply']
      end

      it "should ignore non-integer keys when keys are integer" do
        result = @olap.from('Sales').
          with_member('[Measures].[names]').as("Generate([Customers].[Name].getMembersByKeys('1,dummy'),[Customers].CurrentMember.Name,',')").
          columns('[Measures].[names]').
          execute
        result.values.should == ['Sheri Nowmer']
      end

      it "should return empty set if specified member key is missing" do
        result = @olap.from('Sales').
          with_member('[Measures].[set]').as("SetToStr([Customers].[State Province].getMembersByKeys('XX'))").
          columns('[Measures].[set]').
          execute
        result.values.should == ['{}']
      end

      it "should return empty set if keys are nil" do
        result = @olap.from('Sales').
          with_member('[Measures].[set]').as("SetToStr([Customers].[State Province].getMembersByKeys(NonZero(0)))").
          columns('[Measures].[set]').
          execute
        result.values.should == ['{}']
      end
    end

    describe "Key" do
      it "should return member integer key" do
        result = @olap.from('Sales').
          with_member('[Measures].[key]').as("[Customers].[Name].getMemberByKey(1).Key").
          columns('[Measures].[key]').
          execute
        result.values.should == [1]
      end

      it "should return member string key" do
        result = @olap.from('Sales').
          with_member('[Measures].[key]').as("[Customers].[State Province].getMemberByKey('CA').Key").
          columns('[Measures].[key]').
          execute
        result.values.should == ['CA']
      end
    end

    describe "CurrentHierarchy" do
      it "should return time weekly hierarchy when weekly hierarchy is used" do
        result = @olap.from('Sales').
          with_member('[Measures].[hierarchy name]').as("[Time].CurrentHierarchy.Name").
          columns('[Measures].[hierarchy name]').
          rows('[Time.Weekly].[1997].[W01, Dec 30 1996]').
          execute
        result.values.should == [['Time.Weekly']]
      end

      it "should return time default hierarchy when no time hierarchy is used" do
        result = @olap.from('Sales').
          with_member('[Measures].[hierarchy name]').as("[Time].CurrentHierarchy.Name").
          columns('[Measures].[hierarchy name]').
          execute
        result.values.should == ['Time']
      end
    end

    describe "CurrentHierarchyMember" do
      it "should return time weekly hierarchy member when weekly hierarchy is used" do
        result = @olap.from('Sales').
          with_member('[Measures].[hierarchy name]').as("[Time].CurrentHierarchyMember.UniqueName").
          columns('[Measures].[hierarchy name]').
          rows('[Time.Weekly].[1997].[W01, Dec 30 1996]').
          execute
        result.values.should == [['[Time.Weekly].[1997].[W01, Dec 30 1996]']]
      end

      it "should return time default hierarchy default member when no time hierarchy is used" do
        result = @olap.from('Sales').
          with_member('[Measures].[hierarchy name]').as("[Time].CurrentHierarchyMember.UniqueName").
          columns('[Measures].[hierarchy name]').
          execute
        result.values.should == ['[Time].[All Times]']
      end
    end

    describe "ChildrenSet" do
      it "should return argument set from calculated member with Aggregate function" do
        result = @olap.from('Sales').
          with_member('[Measures].[set as string]').as("SetToStr(ChildrenSet([Customers].[USA west coast]))").
          columns('[Measures].[set as string]').
          execute
        result.values.should == ['{[Customers].[USA].[WA], [Customers].[USA].[OR], [Customers].[USA].[CA]}']
      end

      it "should return set from calculated member with complex Aggregate function" do
        result = @olap.from('Sales').
          with_member('[Measures].[set as string]').as("SetToStr(ChildrenSet([Customers].[USA without WA]))").
          columns('[Measures].[set as string]').
          execute
        result.values.should == ['{[Customers].[USA].[CA], [Customers].[USA].[OR]}']
      end

      it "should return set of the same member for calculated member without Aggregate function" do
        result = @olap.from('Sales').
          with_member('[Measures].[set as string]').as("SetToStr(ChildrenSet([Customers].[CA and OR]))").
          columns('[Measures].[set as string]').
          execute
        result.values.should == ['{[Customers].[CA and OR]}']
      end

      it "should return set of one child member for a calculated member that returns one member" do
        result = @olap.from('Sales').
          with_member('[Customers].[calculated CA]').as("[Customers].[State Province].Members.Item('CA')").
          with_member('[Measures].[set as string]').as("SetToStr(ChildrenSet([Customers].[calculated CA]))").
          columns('[Measures].[set as string]').
          execute
        result.values.should == ['{[Customers].[USA].[CA]}']
      end

      it "should return set of normal member children" do
        result = @olap.from('Sales').
          with_member('[Measures].[set as string]').as("SetToStr(ChildrenSet([Customers].[USA]))").
          columns('[Measures].[set as string]').
          execute
        result.values.should == ['{[Customers].[USA].[CA], [Customers].[USA].[OR], [Customers].[USA].[WA]}']
      end

      it "should return set of the same member for member without children" do
        result = @olap.from('Sales').
          with_member('[Measures].[set as string]').as("SetToStr(ChildrenSet([Gender].[F]))").
          columns('[Measures].[set as string]').
          execute
        result.values.should == ['{[Gender].[F]}']
      end

    end

    describe "CascadingChildrenSet" do
      it "should return argument set from calculated member with Aggregate function" do
        result = @olap.from('Sales').
          with_member('[Measures].[set as string]').as("SetToStr(CascadingChildrenSet([Customers].[USA west coast]))").
          columns('[Measures].[set as string]').
          execute
        result.values.should == ['{[Customers].[USA].[WA], [Customers].[USA].[OR], [Customers].[USA].[CA]}']
      end

      it "should return cascading argument set from aggregated member of other aggregated members" do
        result = @olap.from('Sales').
          with_member('[Customers].[aggregate CA]').as("Aggregate({[Customers].[USA].[CA]})").
          with_member('[Customers].[aggregate OR]').as("Aggregate({[Customers].[USA].[OR]})").
          with_member('[Customers].[aggregate CA and OR]').
            as("Aggregate({[Customers].[aggregate CA],[Customers].[aggregate OR]})").
          with_member('[Measures].[set as string]').as("SetToStr(CascadingChildrenSet([Customers].[aggregate CA and OR]))").
          columns('[Measures].[set as string]').
          execute
        result.values.should == ['{[Customers].[USA].[CA], [Customers].[USA].[OR]}']
      end

      it "should return cascading argument set from aggregated member of other calculated members returning a member" do
        result = @olap.from('Sales').
          with_member('[Customers].[aggregate CA]').as("[Customers].[USA].[CA]").
          with_member('[Customers].[aggregate aggregate CA]').as("[Customers].[aggregate CA]").
          with_member('[Measures].[set as string]').as("SetToStr(CascadingChildrenSet([Customers].[aggregate aggregate CA]))").
          columns('[Measures].[set as string]').
          execute
        result.values.should == ['{[Customers].[USA].[CA]}']
      end
    end

    # PreviousPeriods([Time].CurrentHierarchyMember) is a short version of MDX expression
    # Generate(StripCalculatedMembers(
    #     Ascendants([Time].CurrentHierarchyMember)),
    #   IIF([Time].CurrentHierarchyMember IS
    #       [Time].CurrentHierarchyMember.FirstSibling, {}
    #     { [Time].CurrentHierarchyMember.FirstSibling:
    #       [Time].CurrentHierarchyMember.PrevMember } ) )
    describe "PreviousPeriods" do
      it "should return previous periods for a date member" do
        result = @olap.from('Sales').
          with_member('[Measures].[set as string]').as("SetToStr(PreviousPeriods([Time].[1997].[Q1].[March].[2]))").
          columns('[Measures].[set as string]').
          execute
        result.values.should == ['{' + [
          '[Time].[1997].[Q1].[January]',
          '[Time].[1997].[Q1].[February]',
          '[Time].[1997].[Q1].[March].[1]'
        ].join(', ') + '}']
      end

      it "should return empty set for all member" do
        result = @olap.from('Sales').
          with_member('[Measures].[set as string]').as("SetToStr(PreviousPeriods([Time].DefaultMember))").
          columns('[Measures].[set as string]').
          execute
        result.values.should == ['{}']
      end

      it "should return empty set for null member" do
        result = @olap.from('Sales').
          with_member('[Measures].[set as string]').as("SetToStr(PreviousPeriods([Time].DefaultMember.PrevMember))").
          columns('[Measures].[set as string]').
          execute
        result.values.should == ['{}']
      end

      it "should return previous periods for calculated member returning one member" do
        result = @olap.from('Sales').
          with_member('[Time].[current month]').as('[Time].[1997].[Q1].[March]').
          with_member('[Measures].[set as string]').as("SetToStr(PreviousPeriods([Time].[current month]))").
          columns('[Measures].[set as string]').
          execute
        result.values.should == ['{[Time].[1997].[Q1].[January], [Time].[1997].[Q1].[February]}']
      end
    end

    describe "Titleize" do
      it "should titleize underscored word" do
        result = @olap.from('Sales').
          with_member('[Measures].[titleize]').as("Titleize('state_province')").
          columns('[Measures].[titleize]').
          execute
        result.values.should == ['State Province']
      end

      it "should titleize dashed word" do
        result = @olap.from('Sales').
          with_member('[Measures].[titleize]').as("Titleize('state-province')").
          columns('[Measures].[titleize]').
          execute
        result.values.should == ['State Province']
      end

      it "should titleize camelcased word" do
        result = @olap.from('Sales').
          with_member('[Measures].[titleize]').as("Titleize('StateProvince')").
          columns('[Measures].[titleize]').
          execute
        result.values.should == ['State Province']
      end

      it "should titleize Unicode word" do
        result = @olap.from('Sales').
          with_member('[Measures].[titleize]').as("Titleize('āčē ģīķ')").
          columns('[Measures].[titleize]').
          execute
        result.values.should == ['Āčē Ģīķ']
      end
    end

    describe "ExtractString" do
      it "should extract group from regular expression" do
        result = @olap.from('Sales').
          with_member('[Measures].[string]').
          as("ExtractString('abc: 123'||Chr(10)||'def: 456'||Chr(10)||'ghi: 789','^def:\\s*(\\d+)$',1)").
          columns('[Measures].[string]').
          execute
        result.values.should == ['456']
      end

      it "should extract all matching string from regular expression" do
        result = @olap.from('Sales').
          with_member('[Measures].[string]').
          as("ExtractString('abc: 123'||Chr(10)||'def: 456'||Chr(10)||'ghi: 789','def:\\s*\\d+')").
          columns('[Measures].[string]').
          execute
        result.values.should == ['def: 456']
      end

      it "should return nil if string does not match" do
        result = @olap.from('Sales').
          with_member('[Measures].[string]').as("ExtractString('abc','def')").
          columns('[Measures].[string]').
          execute
        result.values.should == [nil]
      end

      it "should return nil if string is empty" do
        result = @olap.from('Sales').
          with_member('[Measures].[string]').as("ExtractString('','def')").
          columns('[Measures].[string]').
          execute
        result.values.should == [nil]
      end
    end

    describe "NonEmptyString" do
      it "should return argument if non-empty string" do
        result = @olap.from('Sales').
          with_member('[Measures].[string]').as("NonEmptyString('abc')").
          columns('[Measures].[string]').
          execute
        result.values.should == ['abc']
      end

      it "should return nil if empty string" do
        result = @olap.from('Sales').
          with_member('[Measures].[string]').as("NonEmptyString('')").
          columns('[Measures].[string]').
          execute
        result.values.should == [nil]
      end

      it "should return nil if empty value" do
        result = @olap.from('Sales').
          with_member('[Measures].[string]').as("NonEmptyString(ExtractString('abc','def'))").
          columns('[Measures].[string]').
          execute
        result.values.should == [nil]
      end
    end

    describe "NonZero" do
      it "should return argument if non-zero" do
        result = @olap.from('Sales').
          with_member('[Measures].[number]').as("NonZero(123.456)").
          columns('[Measures].[number]').
          execute
        result.values.should == [123.456]
      end

      it "should return nil if argument is zero" do
        result = @olap.from('Sales').
          with_member('[Measures].[number]').as("NonZero(0)").
          columns('[Measures].[number]').
          execute
        result.values.should == [nil]
      end

      it "should return nil if argument is not numeric" do
        result = @olap.from('Sales').
          with_member('[Measures].[number]').as("NonZero('123')").
          columns('[Measures].[number]').
          execute
        result.values.should == [nil]
      end
    end

    describe "IsNumber" do
      it "should be true for number" do
        result = @olap.from('Sales').
          with_member('[Measures].[isnumber]').as("IsNumber(123.456)").
          columns('[Measures].[isnumber]').
          execute
        result.values.should == [true]
      end

      it "should be false for string" do
        result = @olap.from('Sales').
          with_member('[Measures].[isnumber]').as("IsNumber('123.456')").
          columns('[Measures].[isnumber]').
          execute
        result.values.should == [false]
      end

      it "should be false for empty value" do
        result = @olap.from('Sales').
          with_member('[Measures].[isnumber]').as("IsNumber(NonZero(0))").
          columns('[Measures].[isnumber]').
          execute
        result.values.should == [false]
      end
    end

    describe "DefaultContext" do
      before(:all) do
        @total = @olap.from('Sales').columns('[Measures].[Store Sales]').execute.values.first
        @total_f = @olap.from('Sales').columns('[Measures].[Store Sales]').where('[Gender].[F]').execute.values.first
      end

      it "should return measure value in default context" do
        result = @olap.from('Sales').
          with_member('[Measures].[Total Sales]').
            as("DefaultContext([Measures].[Store Sales])").
          columns('[Measures].[Total Sales]').
          rows('[Customers].[USA].[CA]').
          execute
        result.values.first.first.should == @total
      end

      it "should return measure value with specified other dimension member" do
        result = @olap.from('Sales').
          with_member('[Measures].[Total Sales]').
            as("DefaultContext(([Measures].[Store Sales], [Gender].[F]))").
          columns('[Measures].[Total Sales]').
          rows('[Customers].[USA].[CA]').
          execute
        result.values.first.first.should == @total_f
      end

      it "should set Time Weekly hierarchy to default context" do
        result = @olap.from('Sales').
          with_member('[Measures].[Total Sales]').
            as("DefaultContext([Measures].[Store Sales])").
          columns('[Measures].[Total Sales]').
          rows('{[Time.Weekly].[Week].Members.Item(0)}').
          execute
        result.values.first.first.should == @total
      end

      it "should restore context after the function evaluation" do
        result = @olap.from('Sales').
          with_member('[Measures].[Total Sales]').
            as("DefaultContext([Measures].[Store Sales]) + [Measures].[Store Sales]").
          columns('[Measures].[Total Sales]').
          rows('[Gender].[F]').
          execute
        result.values.first.first.should == @total + @total_f
      end

    end

    describe "StringInCSV" do
      {
        true => [
          ["abc", "abc"],
          ["abc", "abc,def"],
          ["def", "abc,def"],
          ["def", "abc,def,ghi"],
          # allow this case as CSV typically will be simple join by comma without escaping values in quotes
          ["abc,def", "abc,def,ghi"],
          # numbers should be casted to string
          [123, "123,456"],
          [123.456, "123.456,789"]
        ],
        false => [
          ["abcdef", "abc"],
          ["abc", "abcdef"],
          ["def", "abcdef"],
          # frozen string will be used as expressions
          ["NonZero(0)".freeze, "NonZero(0)"],
          ["NonZero(0)", "NonZero(0)".freeze]
        ]
      }.each do |value, samples|
        samples.each do |string, csv|
          it "should be #{value} for #{string.inspect} in #{csv.inspect}" do
            result = @olap.from('Sales').
              with_member('[Measures].[incsv]').
              as("StringInCSV(#{string.frozen? ? string : string.inspect}, #{csv.frozen? ? csv : csv.inspect})").
              columns('[Measures].[incsv]').
              execute
            result.values.should == [value]
          end
        end
      end
    end

  end


  describe "date_parse" do
    it "should parse just year as 1st of January" do
      Java::ComEazybiMondrianUdf::DateUtils.parseDate("2012").as_json.should == '2012-01-01'
    end

    it "should parse quarter as 1st date of quarter" do
      Java::ComEazybiMondrianUdf::DateUtils.parseDate("Q4 2012").as_json.should == '2012-10-01'
    end

    it "should parse month as 1st date of month" do
      Java::ComEazybiMondrianUdf::DateUtils.parseDate("Dec 2012").as_json.should == '2012-12-01'
    end

    it "should parse week as 1st date of week" do
      Java::ComEazybiMondrianUdf::DateUtils.parseDate("W10, Mar 05 2012").as_json.should == '2012-03-05'
    end

    it "should parse full date" do
      Java::ComEazybiMondrianUdf::DateUtils.parseDate("Dec 13 2012").as_json.should == '2012-12-13'
    end

    it "should parse now" do
      now = Time.now
      Java::ComEazybiMondrianUdf::DateUtils.parseDate("now").as_json.should == now.to_s(:db)
    end

    it "should parse day ago" do
      Java::ComEazybiMondrianUdf::DateUtils.parseDate("1 day ago").as_json.should == 1.day.ago.to_s(:db)
    end

    it "should parse days ago" do
      # time information might differ because of time zone changes
      Java::ComEazybiMondrianUdf::DateUtils.parseDate("30 days ago").as_json.split(' ').first.should == 30.days.ago.to_date.to_s
    end

    it "should parse day from now" do
      Java::ComEazybiMondrianUdf::DateUtils.parseDate("1 day from now").as_json.should == 1.day.from_now.to_s(:db)
    end

    it "should parse days from now" do
      # time information might differ because of time zone changes
      Java::ComEazybiMondrianUdf::DateUtils.parseDate("30 days from now").as_json.split(' ').first.should == 30.days.from_now.to_date.to_s
    end

    it "should parse garbage as nil" do
      Java::ComEazybiMondrianUdf::DateUtils.parseDate("qwerty").should be_nil
    end
  end
end
