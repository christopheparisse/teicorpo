package fr.ortolang.teicorpo;

public class Test {
	public static void main(String args[]) {
		String s = "(..) uh.";
		String d = s.replaceAll( "\\([.]*\\)", "" );
		System.out.println(s + " | " + d);
		s = "(..) uh.";
		d = ConventionsToChat.clean(s);
		System.out.println(s + " | " + d);
	}
}
