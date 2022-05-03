package fr.ortolang.teicorpo; /**
 * 
 */
import fr.ortolang.teicorpo.*;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import junitx.framework.FileAssert;

/**
 * @author christopheparisse
 *
 */
public class TestFromElan {

	@Test
	public void test() {
		System.out.println("ElanToTei");
		String wd = System.getProperty("user.dir");
		System.out.println("Working Directory = " + System.getProperty("user.dir"));
		System.out.printf("%nElan to TEI%n");
		RunTeiCorpo.run(
				wd,
				"elan",
				"tei",
				"/test/data/sources/elan/" + "TestElan1.eaf",
				"/test/data/dest/elan/" + "TestElan1.eaf.tei_corpo.xml",
				"Test of Elan To Tei"
		);
		System.out.printf("%nTEI (from ELAN) Elan%n");
		RunTeiCorpo.run(
				wd,
				"tei",
				"elan",
				"/test/data/dest/elan/" + "TestElan1.eaf.tei_corpo.xml",
				"/test/data/dest/elan/" + "TestElan1.eaf.tei_corpo.eaf",
				"Test of TEI (from Elan) to Elan"
		);
		System.out.printf("%nElan to TEI%n");
		RunTeiCorpo.run(
				wd,
				"elan",
				"tei",
				"/test/data/sources/elan/" + "TestElan2.eaf",
				"/test/data/dest/elan/" + "TestElan2.eaf.tei_corpo.xml",
				"Test of Elan To Tei"
		);
		System.out.printf("%nTEI (from ELAN) Elan%n");
		RunTeiCorpo.run(
				wd,
				"tei",
				"elan",
				"/test/data/dest/elan/" + "TestElan2.eaf.tei_corpo.xml",
				"/test/data/dest/elan/" + "TestElan2.eaf.tei_corpo.eaf",
				"Test of TEI (from Elan) to Elan"
		);
		System.out.printf("%nElan to TEI%n");
		RunTeiCorpo.run(
				wd,
				"elan",
				"tei",
				"/test/data/sources/elan/" + "DOC_FR_2020_CHOIX_5.eaf",
				"/test/data/dest/elan/" + "DOC_FR_2020_CHOIX_5.eaf.tei_corpo.xml",
				"Test of Elan To Tei"
		);
		System.out.printf("%nTEI (from ELAN) Transcriber%n");
		RunTeiCorpo.run(
				wd,
				"tei",
				"trs",
				"/test/data/dest/elan/" + "DOC_FR_2020_CHOIX_5.eaf.tei_corpo.xml",
				"/test/data/dest/elan/" + "DOC_FR_2020_CHOIX_5.eaf.tei_corpo.trs",
				"Test of TEI (from Elan) to Transcriber"
		);
		System.out.printf("%nTranscriber to TEI%n");
		RunTeiCorpo.run(
				wd,
				"trs",
				"tei",
				"/test/data/sources/elan/" + "ESLO1_ENT_012_C.trs",
				"/test/data/dest/elan/" + "ESLO1_ENT_012_C.trs.tei_corpo.xml",
				"Test of Elan To Tei"
		);
		System.out.printf("%nTEI (from Trs) TEI + TTG%n");
		RunTeiCorpo.runttg(
				wd,
				"/test/data/dest/elan/" + "ESLO1_ENT_012_C.trs.tei_corpo.xml",
				"/test/data/dest/elan/" + "ESLO1_ENT_012_C.trs.tei_corpo.ttg.xml",
				"Test of TEI (from Trs) to TTG",
				"program",
				"model"
		);
		System.out.printf("%nTEI (from ELAN + TTG) Elan%n");
		RunTeiCorpo.run(
				wd,
				"tei",
				"elan",
				"/test/data/dest/elan/" + "ESLO1_ENT_012_C.trs.tei_corpo.ttg.xml",
				"/test/data/dest/elan/" + "ESLO1_ENT_012_C.trs.tei_corpo.ttg.eaf",
				"Test of TEI (from Elan + TTG) to Elan"
		);
		System.out.printf("%nClan to TEI%n");
		RunTeiCorpo.run(
				wd,
				"clan",
				"tei",
				"/test/data/sources/elan/" + "TestDNF4.cha",
				"/test/data/dest/elan/" + "TestDNF4.cha.tei_corpo.xml",
				"Test of Elan To Tei"
		);
		System.out.printf("%nTEI (from CHAT) to Elan%n");
		RunTeiCorpo.run(
				wd,
				"tei",
				"elan",
				"/test/data/dest/elan/" + "TestDNF4.cha.tei_corpo.xml",
				"/test/data/dest/elan/" + "TestDNF4.cha.tei_corpo.eaf",
				"Test of TEI (from Elan) to Elan"
		);
		System.out.printf("%nClan to TEI (for Dinlang)%n");
		String [] argsup = {  "-raw", "-target", "dinlang", "-model", wd + "/test/data/sources/elan/" + "DL-2022-05-02-GEN-TPL.etf",  "-tv", "FAT:lng-aud-P", "-tv", "MOT:lng-aud-M", "-tv", "LUC:lng-aud-Ea", "-tv", "GAB:lng-aud-Eb", "-a", "*script*", "-a", "=1" };
		RunTeiCorpo.runcpx(
				wd,
				"clan",
				"tei",
				"/test/data/sources/elan/" + "TestDNF4.cha",
				"/test/data/dest/elan/" + "TestDNF4D.cha.tei_corpo.xml",
				"Test of Elan To Tei (for Dinlang)",
				argsup
		);
		System.out.printf("%nTEI (from CHAT + template 2022-05-02 + Dinlang) Elan%n");
		RunTeiCorpo.runcpx(
				wd,
				"tei",
				"elan",
				"/test/data/dest/elan/" + "TestDNF4D.cha.tei_corpo.xml",
				"/test/data/dest/elan/" + "TestDNF4D.cha.tei_corpo.eaf",
				"Test of TEI (from CHAT + DINLANG + template 2022-05-02) to Elan",
				argsup
		);
		// MANQUE ORFEO, SNLP, Stanza
	}
}
