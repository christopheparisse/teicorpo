package fr.ortolang.teicorpo;
/**
 * 
 */

import static org.junit.Assert.*;
import java.io.File;
import java.io.IOException;
import org.junit.Test;
import junitx.framework.FileAssert;

/**
 * @author christopheparisse
 *
 */
public class TestFromClan {

	@Test
	public void test() {
		System.out.println("Conversions from Clan");
		String wd = System.getProperty("user.dir");
		System.out.println("Working Directory = " + System.getProperty("user.dir"));
		System.out.printf("%nClan to TEI%n");
		RunTeiCorpo.run(
				wd,
				"clan",
				"tei",
				"/test/data/sources/clan/" + "TestClan1.cha",
				"/test/data/dest/clan/" + "TestClan1.cha.tei_corpo.xml",
				"Test of Clan To Tei"
		);
		System.out.printf("%nTEI (from Clan) to Clan%n");
		RunTeiCorpo.run(
				wd,
				"tei",
				"clan",
				"/test/data/dest/clan/" + "TestClan1.cha.tei_corpo.xml",
				"/test/data/dest/clan/" + "TestClan1.cha.tei_corpo.cha" ,
				"Test of Tei (from Clan) to Clan"
		);
		System.out.printf("%nClan to TEI%n");
		RunTeiCorpo.run(
				wd,
				"clan",
				"tei",
				"/test/data/sources/clan/" + "testclan2.cha",
				"/test/data/dest/clan/" + "testclan2.cha.tei_corpo.xml",
				"Test of Clan To Tei"
		);
		System.out.printf("%nTEI (from Clan) to Clan%n");
		RunTeiCorpo.run(
				wd,
				"tei",
				"clan",
				"/test/data/dest/clan/" + "testclan2.cha.tei_corpo.xml",
				"/test/data/dest/clan/" + "testclan2.cha.tei_corpo.cha" ,
				"Test of Tei (from Clan) to Clan"
		);
		System.out.printf("%nClan to TEI%n");
		RunTeiCorpo.run(
				wd,
				"clan",
				"tei",
				"/test/data/sources/clan/" + "064.cha",
				"/test/data/dest/clan/" + "064.cha.tei_corpo.xml",
				"Test of Clan To Tei"
		);
		System.out.printf("%nTEI (from Clan) to Clan%n");
		RunTeiCorpo.run(
				wd,
				"tei",
				"clan",
				"/test/data/dest/clan/" + "064.cha.tei_corpo.xml",
				"/test/data/dest/clan/" + "064.cha.tei_corpo.cha" ,
				"Test of Tei (from Clan) to Clan"
		);
		System.out.printf("%nClan to TEI%n");
		RunTeiCorpo.run(
				wd,
				"clan",
				"tei",
				"/test/data/sources/clan/" + "030121.cha",
				"/test/data/dest/clan/" + "030121.cha.tei_corpo.xml",
				"Test of Clan To Tei"
		);
		System.out.printf("%nTEI (from Clan) to Clan%n");
		RunTeiCorpo.run(
				wd,
				"tei",
				"clan",
				"/test/data/dest/clan/" + "030121.cha.tei_corpo.xml",
				"/test/data/dest/clan/" + "030121.cha.tei_corpo.cha" ,
				"Test of Tei (from Clan) to Clan"
		);
	}
}
