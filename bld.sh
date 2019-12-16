rm target/*.jar
mvn package
cp -v target/teicorpo-*.jar teicorpo.jar
