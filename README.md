Mondrian MDX user defined functions
===================================

Source code of Mondrian MDX user defined functions (UDF) that are extracted from [eazyBI](https://eazybi.com).

[mondrian-olap](https://github.com/rsim/mondrian-olap) is used for testing.

This repository also includes [jchronic](https://github.com/samtingleff/jchronic) jar file.

RUNNING TESTS
-------------

Install the standard FoodMart Mondrian schema in the MySQL database on the localhost.

Install necessary gems with

    bundle install

Run tests with

    rake spec

Recompile Mondrian UDFs with

    rake jar

(will always be recompiled if necessary when running `rake spec`).

USAGE
-----

Include jar files from the `lib` subdirectory in the class path. Define user defined functions in the Mondrian schema as defined in `olap_spec_helper.rb`.

LICENSE
-------

See LICENSE.txt.

