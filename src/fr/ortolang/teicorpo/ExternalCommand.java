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

class AfficheurFlux implements Runnable {

    private final InputStream inputStream;
    private PrintWriter out;
    private boolean verbose;

    AfficheurFlux(InputStream inputStream, String outputName, boolean verbose) {
    	this.verbose = verbose;
        this.inputStream = inputStream;
        if (outputName != null) {
    		try {
    			FileOutputStream of = new FileOutputStream(outputName);
    			OutputStreamWriter outWriter = new OutputStreamWriter(of, "UTF-8");
    			out = new PrintWriter(outWriter, true);
    		} catch (Exception e) {
    			out = new PrintWriter(System.out, true);
    		}
        } else
        	out = null;
    }

    private BufferedReader getBufferedReader(InputStream is) {
        return new BufferedReader(new InputStreamReader(is));
    }

    @Override
    public void run() {
        BufferedReader br = getBufferedReader(inputStream);
        String ligne = "";
        try {
            while ((ligne = br.readLine()) != null) {
            	if (out != null)
                    out.println(ligne);
            	else {
            		if (verbose) System.out.println(ligne);
            	}
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

public class ExternalCommand {
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
		File test = new File(progName);
		if (test.exists())
			return progName;
		// environment variables
		String value = System.getenv(envVar);
        if (value != null) {
			String pLoc = value + "/" + progName;
			//System.err.println("EnvL:" + pLoc);
			test = new File(pLoc);
			if (test.exists())
				return pLoc;
        }
        // location of jar
		File jarLocation = getJarParent();
		if (jarLocation != null) {
			String pLoc = jarLocation.getParent() + "/" + progName;
			//System.err.println("PL:" + pLoc);
			test = new File(pLoc);
			if (test.exists())
				return pLoc;
		}
		return null;
	}

    public static void command(String[] args, String output, boolean verbose) {
        if (verbose) System.err.println("Début du programme : " + Utils.join(args));
        try {
            Process p = Runtime.getRuntime().exec(args);
            AfficheurFlux fluxSortie = new AfficheurFlux(p.getInputStream(), output, verbose);
            AfficheurFlux fluxErreur = new AfficheurFlux(p.getErrorStream(), null, verbose);

            new Thread(fluxSortie).start();
            new Thread(fluxErreur).start();

            p.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (verbose) System.err.println("Fin du programme");
    }
    
    public static void main(String[] args) {
    	String os = System.getProperty("os.name");
    	System.out.println(os);
    	os = System.getProperty("os.version");
    	System.out.println(os);
    	os = System.getProperty("os.arch");
    	System.out.println(os);
//        System.getProperties().list(System.out);
    }
}
