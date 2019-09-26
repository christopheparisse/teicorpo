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
public class ElanToTeiTest {

	@Test
	public void test() {
		System.out.println("ElanToTei");
		String wd = System.getProperty("user.dir") + "/test/";
		System.out.println("Working Directory = " +
	              System.getProperty("user.dir"));
		ElanToTei program = new ElanToTei();
		String filehead = wd + "TestElan1";
		String [] args = { filehead + ElanToTei.EXT, "-v", "-test" };
		try {
			program.mainCommand(args, ElanToTei.EXT, Utils.EXT, "Test of ElanToTei", 0);
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
