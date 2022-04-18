package fr.ortolang.teicorpo;

// import fr.ortolang.teicorpo.*;

import junitx.framework.FileAssert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class TestFromPraat {

    @Test
    public void test() {
        System.out.println("PraatToTei");
        String wd = System.getProperty("user.dir");
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        System.out.printf("%nPraat to TEI%n");
        RunTeiCorpo.run(
                wd,
                "praat",
                "tei",
                "/test/data/sources/praat/" + "FOR_DIA.textgrid",
                "/test/data/dest/praat/" + "FOR_DIA.textgrid.tei_corpo.xml",
                "Test of Praat To Tei"
        );
        System.out.printf("%nTEI (from Praat) to Praat%n");
        RunTeiCorpo.run(
                wd,
                "tei",
                "praat",
                "/test/data/dest/praat/" + "FOR_DIA.textgrid.tei_corpo.xml",
                "/test/data/dest/praat/" + "FOR_DIA.textgrid.tei_corpo.textgrid",
                "Test of TEI (from Praat) to Praat"
        );
    }
}
