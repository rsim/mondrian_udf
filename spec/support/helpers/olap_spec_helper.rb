module OlapSpecHelper
  CONNECTION_PARAMS = {
    database: 'foodmart',
    host: 'localhost',
    username: 'foodmart',
    password: 'foodmart'
  }

  def olap_connection(options = {})
    Mondrian::OLAP::Connection.create(
      CONNECTION_PARAMS.merge(
        driver: 'mysql',
        schema: options[:schema] || olap_schema(options)
      )
    )
  end

  def olap_schema(options = {})
    Mondrian::OLAP::Schema.define "default" do
      cube 'Sales' do
        table 'sales_fact_1997'
        dimension 'Gender', foreign_key: 'customer_id' do
          hierarchy has_all: true, primary_key: 'customer_id' do
            table 'customer'
            level 'Gender', column: 'gender', unique_members: true
          end
        end
        dimension 'Customers', foreign_key: 'customer_id' do
          hierarchy has_all: true, all_member_name: 'All Customers', primary_key: 'customer_id' do
            table 'customer'
            level 'Country', column: 'country', unique_members: true
            level 'State Province', column: 'state_province', unique_members: true do
              property 'Country', column: 'country'
            end
            level 'City', column: 'city', unique_members: false
            level 'Name', column: 'customer_id', name_column: 'fullname', unique_members: false do
              property 'Birth Date', column: 'birthdate'
              property 'Total Children', column: 'total_children'
              property 'Postal Code', column: 'postal_code'
            end
          end
          hierarchy 'Country', has_all: true, all_member_name: 'All Countries', primary_key: 'customer_id' do
            table 'customer'
            level 'Country', column: 'country', unique_members: true do
              annotations properties_from: '[Customers].[Country]'
            end
            level 'State', column: 'state_province', unique_members: true do
              annotations properties_from: '[Customers].[State Province]'
            end
            level 'Name', column: 'customer_id', unique_members: true do
              annotations properties_from: '[Customers].[Name]',
                caption_from: '[Customers].[Name]'
            end
            level 'Middle Name', column: 'mi', unique_members: false, hide_member_if: 'IfBlankName'
          end
        end
        dimension 'Products', foreign_key: 'product_id' do
          hierarchy has_all: true, all_member_name: 'All Products',
                    primary_key: 'product_id', primary_key_table: 'product' do
            join left_key: 'product_class_id', right_key: 'product_class_id' do
              table 'product'
              table 'product_class'
            end
            level 'Product Family', table: 'product_class', column: 'product_family', unique_members: true
            level 'Product Department', table: 'product_class', column: 'product_department', unique_members: false
            level 'Product Category', table: 'product_class', column: 'product_category', unique_members: false
            level 'Product Subcategory', table: 'product_class', column: 'product_subcategory', unique_members: false
            level 'Brand Name', table: 'product', column: 'brand_name', unique_members: false
            level 'Product Name', table: 'product', column: 'product_name', unique_members: true do
              property 'Low Fat', column: 'low_fat', type: 'Boolean'
            end
          end
        end
        dimension 'Time', foreign_key: 'time_id', type: 'TimeDimension' do
          hierarchy has_all: true, all_member_name: 'All Times', primary_key: 'time_id' do
            table 'time_by_day'
            level 'Year', column: 'the_year', type: 'Numeric', unique_members: true, level_type: 'TimeYears'
            level 'Quarter', column: 'quarter', unique_members: false, level_type: 'TimeQuarters'
            level 'Month', column: 'month_of_year', name_column: 'the_month', type: 'Numeric', unique_members: false, level_type: 'TimeMonths'
            level 'Day', column: 'day_of_month', type: 'Numeric', unique_members: false, level_type: 'TimeDays'
          end
          hierarchy 'Weekly', has_all: true, all_member_name: 'All Times', primary_key: 'time_id' do
            table 'time_by_day'
            level 'Year', column: 'the_year', type: 'Numeric', unique_members: true, level_type: 'TimeYears' do
              annotations member_format: "Weekly %Y"
            end
            level 'Week', type: 'Numeric', unique_members: false, level_type: 'TimeWeeks' do
              key_expression do
                sql 'week(the_date,3)'
              end
              name_expression do
                sql "cast(concat('W',lpad(week(the_date,3),2,'0'),', ',date_format(str_to_date(concat(the_year, week(the_date, 1),' Monday'), '%x%v %W'),'%b %d %Y')) as char(16))"
              end
            end
            level 'Day', column: 'day_of_month', type: 'Numeric', unique_members: false, level_type: 'TimeDays' do
              name_expression do
                sql "date_format(the_date,'%b %d %Y')"
              end
            end
          end

          hierarchy 'Fiscal', has_all: true, all_member_name: 'All Times (Fiscal)', primary_key: 'time_id' do
            annotations first_month: 2
            table 'time_by_day'
            level 'Year', column: 'fiscal_year', type: 'Numeric', unique_members: true, level_type: 'TimeYears' do
              key_expression do
                sql "case when month_of_year < 2 then the_year else the_year+1 end"
              end
              name_expression do
                sql %{concat("FY", (case when month_of_year < 2 then the_year else the_year+1 end))}
              end
            end
            level 'Quarter', column: 'fiscal_quarter', type: 'Numeric', level_type: 'TimeQuarters' do
              key_expression do
                sql "format(floor((case when month_of_year < 2 then month_of_year-2+12 else month_of_year-2 end) / 3) + 1, 0)"
              end
              name_expression do
                sql %{concat("FQ", format(floor((case when month_of_year < 2 then month_of_year-2+12 else month_of_year-2 end) / 3) + 1, 0),
                             " ", (case when month_of_year < 2 then the_year else the_year+1 end))}
              end
            end
            level 'Month', column: 'month_of_year', type: 'Numeric', level_type: 'TimeMonths' do
              name_expression do
                sql "date_format(the_date, '%b %Y')"
              end
            end
            level 'Day', column: 'day_of_month', type: 'Numeric', level_type: 'TimeDays' do
              name_expression do
                sql "date_format(the_date, '%b %d %Y')"
              end
            end
          end
        end
        measure 'Unit Sales', column: 'unit_sales', aggregator: 'sum', format_string: '#,###'
        measure 'Store Sales', column: 'store_sales', aggregator: 'sum', format_string: '#,##0.00'

        calculated_member 'Infinity' do
          dimension 'Measures'
          formula '1/0'
        end
        calculated_member 'Minus Infinity' do
          dimension 'Measures'
          formula '-1/0'
        end
        calculated_member 'NaN' do
          dimension 'Measures'
          formula '(1/0) / (1/0)'
          visible false
        end
        calculated_member 'Now' do
          dimension 'Measures'
          formula 'Now()'
          format_string 'mmm dd yyyy'
        end
        calculated_member 'USA west coast' do
          dimension 'Customers'
          formula 'Aggregate({[Customers].[USA].[WA],[Customers].[USA].[OR],[Customers].[USA].[CA]})'
        end
        calculated_member 'USA without WA' do
          dimension 'Customers'
          formula 'Aggregate(Except([Customers].[USA].Children, [Customers].[USA].[WA]))'
        end
        calculated_member 'CA and OR' do
          dimension 'Customers'
          formula '[Customers].[USA].[CA] + [Customers].[USA].[OR]'
        end
      end

      %w( Get GetProperty GetString GetNumber GetDate GetBoolean Key AllProperties
          DimensionGet DimensionGetString DimensionGetNumber DimensionGetDate DimensionGetBoolean
          DateDiffDays DateDiffHours DateDiffMinutes DateAddDays DateParse DateBetween DateCompare DateWithoutTime
          DateDiffWorkdays DateDiffWorkdaysDefault DateAddWorkdays DateAddWorkdaysDefault
          DateInPeriod AnyDateInPeriod DateBeforePeriodEnd DateAfterPeriodEnd StartDate NextStartDate
          TimestampToDate DateToTimestamp
          CurrentDateMember DateMember
          GetMemberByKey GetMemberNameByKey GetMembersByKeys
          CurrentHierarchy CurrentHierarchyMember
          ChildrenSet CascadingChildrenSet PreviousPeriods
          Titleize ExtractString ExtractStringDefault NonEmptyString NonZero IsNumber StringInCSV
          DefaultContext
        ).each do |udf_name|
        user_defined_function udf_name, class_name: "com.eazybi.mondrian.udf.#{udf_name}Udf"
      end

    end
  end

end
