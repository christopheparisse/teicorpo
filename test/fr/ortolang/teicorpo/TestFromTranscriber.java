package fr.ortolang.teicorpo;

import org.junit.Test;

public class TestFromTranscriber {

    @Test
    public void test() {
        System.out.println("TranscriberToTei");
        String wd = System.getProperty("user.dir");
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        System.out.printf("%nTranscriber to TEI%n");
        RunTeiCorpo.run(
                wd,
                "trs",
                "tei",
                "/test/data/sources/transcriber/" + "DOC_FR_2020_CHOIX_5.trs",
                "/test/data/dest/transcriber/" + "DOC_FR_2020_CHOIX_5.trs.tei_corpo.xml",
                "Test of Transcriber To Tei"
        );
        System.out.printf("%nTEI (from Transcriber) to Transcriber%n");
        RunTeiCorpo.run(
                wd,
                "tei",
                "trs",
                "/test/data/dest/transcriber/" + "DOC_FR_2020_CHOIX_5.trs.tei_corpo.xml",
                "/test/data/dest/transcriber/" + "DOC_FR_2020_CHOIX_5.trs.tei_corpo.trs",
                "Test of TEI (from Transcriber) to Transcriber"
        );
        System.out.printf("%nTEI (from Transcriber) to ELAN%n");
        RunTeiCorpo.run(
                wd,
                "tei",
                "elan",
                "/test/data/dest/transcriber/" + "DOC_FR_2020_CHOIX_5.trs.tei_corpo.xml",
                "/test/data/dest/transcriber/" + "DOC_FR_2020_CHOIX_5.trs.tei_corpo.eaf",
                "Test of TEI (from Transcriber) to Elan"
        );
    }
}
