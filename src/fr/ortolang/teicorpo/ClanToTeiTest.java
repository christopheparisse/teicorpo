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
public class ClanToTeiTest {

	@Test
	public void test() {
		System.out.println("ClanToTei");
		String wd = System.getProperty("user.dir") + "/test/";
		System.out.println("Working Directory = " +
	              System.getProperty("user.dir"));
		ClanToTei program = new ClanToTei();
		String filehead = wd + "TestClan1";
		String [] args = { filehead + ClanToTei.EXT, "-v", "-test" };
		try {
			program.mainCommand(args, ClanToTei.EXT, Utils.EXT, "Test of ClanToTei", 0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		File in = new File(filehead + Utils.EXT + ".correct");
		File out = new File(filehead + Utils.EXT);
		try {
			FileAssert.assertEquals(in, out);
		} catch(AssertionError e) {
			System.out.println("The files differ!");
		}
		System.out.println("The files are identical!");
	}

}
