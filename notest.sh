rm target/*.jar
mvn package -DskipTests
cp -v target/teicorpo-*.jar teicorpo.jar
