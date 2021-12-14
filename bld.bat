del target\*.jar
cmd /c mvn package
copy target\teicorpo-1.4.44-SNAPSHOT.jar teicorpo.jar
copy target\teicorpo-1.4.44-SNAPSHOT.jar C:\brainstorm\evalang\commands\teicorpo.jar
