jar_file = File.join(*%w(lib eazybi_mondrian_udf.jar))
begin
  require 'ant'
  directory classes = "target/classes"
  CLEAN << "target"

  file jar_file => FileList['src/java/**/*.java', 'target/classes'] do
    require 'mondrian-olap'
    mondrian_jars = $LOADED_FEATURES.grep(%r{mondrian/jars/})
    other_jars = Dir["lib/jchronic*.jar"]

    rm_rf FileList["#{classes}/**/*"]
    ant.javac srcdir: "src/java", destdir: "target/classes",
              source: "1.7", target: "1.7",
              debug: true, deprecation: true,
              classpath: "${java.class.path}:${sun.boot.class.path}:#{(mondrian_jars+other_jars).join(':')}",
              includeantRuntime: false do
      compilerarg value: "-Xlint:unchecked"
    end

    ant.jar :basedir => "target/classes", :destfile => jar_file, :includes => "**/*.class"
  end

  desc "Compile the native Java code."
  task :jar => jar_file

  namespace :jar do
    task :force do
      rm jar_file
      Rake::Task['jar'].invoke
    end
  end

rescue LoadError
  task :jar do
    puts "Run 'jar' with JRuby to re-compile the agent extension class"
  end
end
