del target\*.jar
cmd /c mvn package
copy target\teicorpo-1.41.08-SNAPSHOT.jar teicorpo.jar
copy target\teicorpo-1.41.08-SNAPSHOT.jar C:\brainstorm\evalang\commands\teicorpo.jar
