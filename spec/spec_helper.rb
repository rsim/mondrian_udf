require "rubygems"
require "bundler"
Bundler.setup(:default, :development)

require 'rspec'
require 'active_support/all'

# Requires supporting ruby files with custom matchers and macros, etc,
# in spec/support/ and its subdirectories.
Dir[File.dirname(__FILE__) + "/support/**/*.rb"].each {|f| require f}

RSpec.configure do |config|
end

Java::JavaLang::System.setProperty("jdbc.driver.autoload", "true")
require 'jdbc/mysql'
require 'mondrian/olap'

%w(lib).each do |directory|
  $:.unshift(File.dirname(__FILE__) + "/../#{directory}")
end

require "jchronic-0.2.6.jar"
require "eazybi_mondrian_udf.jar"
