# /bin/sh
#jar cvfe teicorpo.jar fr.ortolang.teicorpo.TeiCorpo .
cp -v target/teicorpo-*.jar teicorpo.jar 
rsync -uv teicorpo.jar /mnt/c/devlopt/prod-trjs/tools/
rsync -uv teicorpo.jar /mnt/c/devlopt/devl-trjs/tools/
rsync -uv teicorpo.jar /mnt/c/devlopt/teiconvert/system/
#rsync -uv teicorpo.jar /devlopt/aeec/dist/bin/
#rsync -uv teicorpo.jar /Users/christopheparisse/OneDrive/
#rsync -uv teicorpo.jar /projets/ct3/teiconvert/system/
