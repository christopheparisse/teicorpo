del target\*.jar
cmd /c mvn package
copy target\teicorpo-*.jar teicorpo.jar
