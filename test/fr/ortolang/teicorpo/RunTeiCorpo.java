package fr.ortolang.teicorpo;

import junitx.framework.FileAssert;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.fail;

public class RunTeiCorpo {
    public static void run(String wd, String from, String to, String filefrom, String fileto, String info) {
        runcpx(wd, from, to, filefrom, fileto, info, null);
    }

    static <T> T[] concatWithArrayCopy(T[] array1, T[] array2) {
        T[] result = Arrays.copyOf(array1, array1.length + array2.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        return result;
    }
    public static void runcpx(String wd, String from, String to, String filefrom, String fileto, String info, String [] argscpx) {
        String [] args = { "-from", from, wd + filefrom, "-to", to, "-o", wd + fileto, "-v", "-test" };
        TeiCorpo program = new TeiCorpo();
        try {
            program.mainCommand( argscpx == null ? args : concatWithArrayCopy(args, argscpx), "", "", info, 4);
        } catch (IOException e) {
            System.err.println("cannot start TeiCorpo command");
            e.printStackTrace();
            System.exit(1);
        }
        // compare result
        System.out.printf("Comparing files:%n\t%s%nand\t%s%n", wd + fileto, wd + fileto + ".correct");
        File left = new File(wd + fileto);
        File right = new File(wd + fileto + ".correct");
        try {
            FileAssert.assertEquals(left, right);
            System.out.println("The files are identical!");
        } catch(AssertionError e) {
            fail("The files differ!");
        }
    }
    public static void runttg(String wd, String filefrom, String fileto, String ttgprogram, String model, String info) {
        String [] args = { wd + filefrom, "-o", wd + fileto, "-v", "-test" };
        TeiTreeTagger program = new TeiTreeTagger();
        try {
            program.mainCommand(args, "", "", info, 4);
        } catch (IOException e) {
            System.err.println("cannot start TeiTreeTagger command");
            e.printStackTrace();
            System.exit(1);
        }
        // compare result
        System.out.printf("Comparing files:%n\t%s%nand\t%s%n", wd + fileto, wd + fileto + ".correct");
        File left = new File(wd + fileto);
        File right = new File(wd + fileto + ".correct");
        try {
            FileAssert.assertEquals(left, right);
            System.out.println("The files are identical!");
        } catch(AssertionError e) {
            fail("The files differ!");
        }
    }
}
