package fr.ortolang.teicorpo;
import java.io.File;
import java.io.FilenameFilter;

/**
 * prend tous les fichiers d'un répertoire avec une extension donnée et les déplacent dans
 * un répertoire de nom du fichier sans extension.
 * aa.xxx --> aa/aa.xxx
 * @author mm
 *
 */

public class ReorgPath {

	public ReorgPath(String input, final String format){

		File inputDir = new File (input);
		File[] files = new File[0];

		if (inputDir.exists() && inputDir.isDirectory()){
			files = inputDir.listFiles(new FilenameFilter(){
				public boolean accept(File dir, String name) {
					return name.endsWith(format);
				}
			});
		}
		System.out.println(files.length);

		for (File f : files){
			String name = f.getName().split(format)[0];
			String transDirName;
			if(input.endsWith("/")){
				transDirName = input + name + "/";
			}
			else{
				transDirName = input + "/" + name + "/";
			}
			File transDir = new File(transDirName);
			transDir.mkdir();
			System.out.println("*******************************");
			System.out.println("*******************************");
			for(File file : inputDir.listFiles()){
				String fileName = file.getName();
				if(fileName.startsWith(name)){
					if(file.isFile()){
						file.renameTo(new File(transDirName + fileName));
						System.out.println(transDirName + fileName);
					}
				}
			}
		}	
	}

	public static void main (String[] args){
		String inputDir = args[0];
		final String format = args[1];
		File dir = new File(inputDir);
		new ReorgPath(dir.getAbsolutePath(), format);
		System.out.println(dir.getAbsolutePath());
	}
}
