package fr.ortolang.teicorpo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URISyntaxException;

public class ExternalCommand {
	public static void executeCommand(String command, boolean verbose)
	{
		if (verbose) System.out.println("Command: {" + command + "}");

	    Runtime runtime = Runtime.getRuntime();
	    try
	    {
	        Process process = runtime.exec(command);

	        BufferedReader stdInput = new BufferedReader(new
	                InputStreamReader(process.getInputStream()));

	        BufferedReader stdError = new BufferedReader(new
	                InputStreamReader(process.getErrorStream()));

	        // read the output from the command
	        if (verbose) System.out.println("Standard output of the command:\n");
	        String s = null;
	        while ((s = stdInput.readLine()) != null) {
	        	if (verbose) System.out.println(s);
	        }

	        // read any errors from the attempted command
	        if (verbose) System.out.println("Standard error of the command (if any):\n");
	        while ((s = stdError.readLine()) != null) {
	        	if (verbose) System.out.println(s);
	        }
	        process.waitFor();
	    } catch (Exception e)
	    {
	        e.printStackTrace();
	    }
	}
	
	public static File getJarParent() {
		try {
			return new File(ExternalCommand.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			System.err.println("cannot access jar file: " + e.getMessage());
			return null;
		}
	}
	
	public static String getLocation(String progName, String envVar) {
		// current dir
		// System.out.printf("progname: %s (%s)%n", progName, progName.substring(0, 1));
		File test = new File(progName);
		if (test.exists()) {
			if (progName.substring(0, 1).equals("/"))
				return progName;
			// if (progName.substring(0, 2).compareToIgnoreCase("c:") == 0)
			if (progName.substring(0, 2).matches("[a-zA-Z]:"))
				return progName;
			return "./" + progName.replaceAll("\\\\", "/");
		}
		// environment variables
		String value = System.getenv(envVar);
        if (value != null) {
    		value = value.replaceAll("\\\\", "/");
			String pLoc = (value.endsWith("/")) ? value + progName : value + "/" + progName;
			//System.err.println("EnvL:" + pLoc);
			test = new File(pLoc);
			if (test.exists())
				return pLoc;
        }
        // location of jar
		File jarLocation = getJarParent();
		if (jarLocation != null) {
			String parent = jarLocation.getParent().replaceAll("\\\\", "/");
			String pLoc =  (parent.endsWith("/")) ? parent + progName : parent + "/" + progName;
			//System.err.println("PL:" + pLoc);
			test = new File(pLoc);
			if (test.exists())
				return pLoc;
		}
		return null;
	}

    public static void command(String[] args, boolean verbose) {
        if (verbose) System.err.println("Starting : " + Utils.join(args));
        executeCommand(Utils.join(args), verbose);
    }
    
    public static void main(String[] args) {
    	String os = System.getProperty("os.name");
    	System.out.println(os);
    	os = System.getProperty("os.version");
    	System.out.println(os);
    	os = System.getProperty("os.arch");
    	System.out.println(os);
//        System.getProperties().list(System.out);
    	executeCommand(args[0], true);
    }

	public static String noblank(String location) {
		if (location == null) return null;
    	String os = System.getProperty("os.name").toLowerCase();
    	// System.out.printf("OS=%s%n", os);
		if (os.indexOf("mac") < 0 && os.indexOf("nix") < 0  && os.indexOf("linux") < 0)
			return "\"" + location + "\"";
		else {
			if (location.indexOf(" ") >= 0) {
				System.out.printf("Warning: there is a white space in [%s]. The tool might not work properly.%n", location);
				return location.replaceAll(" ", "\\\\ ");
			} else
				return location;
		}
	}
}
