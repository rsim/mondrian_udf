source 'http://rubygems.org'

gem 'rake'

platforms :jruby do
  gem 'mondrian-olap', :git => 'git://github.com/rsim/mondrian-olap.git', :require => false
end

group :development, :test do
  gem 'jdbc-mysql', '~> 5.1.24'
  gem 'rspec', '~> 2.14.0'
  gem 'activesupport', '~> 3.2.18'
  gem 'pry', :require => false
end
