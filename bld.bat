del target\*.jar
cmd /c mvn package
rem copy target\teicorpo-*.jar teicorpo.jar
copy .\target\teicorpo-1.4.36-SNAPSHOT.jar teicorpo.jar