/**
 * 
 */
package fr.ortolang.teicorpo;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import junitx.framework.FileAssert;

/**
 * @author christopheparisse
 *
 */
public class TeiToElanTest {

	@Test
	public void test() {
		System.out.println("TeiToElan");
		String wd = System.getProperty("user.dir") + "/test/";
		System.out.println("Working Directory = " +
	              System.getProperty("user.dir"));
		TeiToElan program = new TeiToElan();
		String filehead = wd + "TestElan1";
		String [] args = { filehead + Utils.EXT, "-v", "-test" };
		try {
			program.mainCommand(args, Utils.EXT, TeiToElan.EXT, "Test of TeiToElan", 0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		File in = new File(filehead + Utils.EXT_PUBLISH + TeiToElan.EXT + ".correct");
		File out = new File(filehead + Utils.EXT_PUBLISH + TeiToElan.EXT);
		try {
			FileAssert.assertEquals(in, out);
		} catch(AssertionError e) {
			System.out.println("The files differ!");
		}
		System.out.println("The files are identical!");
	}

	public static void main(String args[]) throws IOException {
		TeiToElanTest t = new TeiToElanTest();
		t.test();
	}
}
