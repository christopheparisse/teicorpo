java -cp C:\devlopt\teicorpo\teicorpo.jar fr.ortolang.teicorpo.TeiCorpo -v -to tei .\test1.trs
java -cp C:\devlopt\teicorpo\teicorpo.jar fr.ortolang.teicorpo.TeiCorpo -v -to eaf .\test1.tei_corpo.xml -o test1.to.eaf
java -cp C:\devlopt\teicorpo\teicorpo.jar fr.ortolang.teicorpo.TeiCorpo -v -to tei test1.to.eaf -o test1.back.tei.xml
java -cp C:\devlopt\teicorpo\teicorpo.jar fr.ortolang.teicorpo.TeiCorpo -v -to trs test1.back.tei.xml -o test1.back.to.trs
