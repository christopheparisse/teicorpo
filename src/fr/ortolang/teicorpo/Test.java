package fr.ortolang.teicorpo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
	public static void main(String args[]) {
//		Pattern pattern = Pattern.compile("(.*)[/](.*?)[/]?\\[@(.*?)=\\'(.*)\\'\\]");
		Pattern pattern = Pattern.compile(args[0]);
		Matcher matcher = pattern.matcher(args[1]);
		//System.out.println(line);
		if (matcher.matches()) {
			System.err.println("OK");
			System.err.printf("1: %s%n", matcher.group(1));
			System.err.printf("2: %s%n", matcher.group(2));
			System.err.printf("3: %s%n", matcher.group(3));
			System.err.printf("4: %s%n", matcher.group(4));
			return;
		}
		System.err.println("BAD");
	}
}
