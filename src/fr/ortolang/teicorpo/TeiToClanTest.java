/**
 * 
 */
package fr.ortolang.teicorpo;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import junitx.framework.FileAssert;

/**
 * @author christopheparisse
 *
 */
public class TeiToClanTest {

	@Test
	public void test() {
		System.out.println("TeiToClan");
		String wd = System.getProperty("user.dir") + "/test/";
		System.out.println("Working Directory = " +
	              System.getProperty("user.dir"));
		TeiToClan program = new TeiToClan();
		String filehead = wd + "TestClan1";
		String [] args = { filehead + Utils.EXT, "-v", "-test" };
		try {
			program.mainCommand(args, Utils.EXT, TeiToClan.EXT, "Test of TeiToClan", 0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		File in = new File(filehead + Utils.EXT_PUBLISH + TeiToClan.EXT + ".correct");
		File out = new File(filehead + Utils.EXT_PUBLISH + TeiToClan.EXT);
		try {
			FileAssert.assertEquals(in, out);
		} catch(AssertionError e) {
			System.out.println("The files differ!");
		}
		System.out.println("The files are identical!");
	}
}
