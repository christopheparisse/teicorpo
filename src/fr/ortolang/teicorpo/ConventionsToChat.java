package fr.ortolang.teicorpo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConventionsToChat {
	
	public static String noise(String s){
		Pattern p1 = Pattern.compile("(\\s|^\\s*)(\\*) ([^/\\*]*) (B/\\*)");
		Matcher m = p1.matcher(s);
		while (m.find()){
			if(s.startsWith(m.group())){
				s = s.replace(m.group(), " 0 [=! " + m.group(3) + "]");
			}
			else{
				s = s.replace(m.group(), " [=! " + m.group(3) + "]");
			}			
		}
		return s;
	}
	
	public static String pauses(String s){
		//Pause non chrono
		Pattern p1 = Pattern.compile("(\\s|^)(#+)(\\s|$)");
		Matcher m1 = p1.matcher(s);
		while(m1.find()){
			String rep = m1.group(2).replaceAll("#", ".");
			s = s.replace(m1.group(), " (" + rep + ") ");
		}		
		//Pause chrono
		Pattern p2 = Pattern.compile("(\\s|^)(#)(\\d+\\.\\d*)(\\s|$)");
		Matcher m2 = p2.matcher(s);
		while(m2.find()){
			s = s.replace(m2.group(), " (" + m2.group(3) + ") ");
		}
		return s;
	}
	
	public static String term(String s){
		String patternStr = "#(\\+\\.\\.\\.|\\+/\\.|\\+!\\?|\\+//\\.|\\+/\\?|\\+\"/\\.|\\+\"\\.|\\+//\\?|\\+\\.\\.\\?|\\+\\.|\\.|\\?|!)\\s*$";
		Pattern pattern = Pattern.compile(patternStr);
		Matcher matcher = pattern.matcher(s);
		if (matcher.find()) {
			s  = s.replace(matcher.group(), matcher.group(1));
		}
		return s;
	}
	
	
	public static String setConv(String s){
		s = pauses(s);		
		s = noise(s);
		s = term(s);
		return Utils.cleanString(s);
	}
	
	public static String chatToText(String s) {
		// FClitic = "-t-elles?|-t-ils?|-t-on|-ce|-elles?|-ils?|-je|-la|-les?|-leur|-lui|-mes?|-m\'|-moi|-nous|-on|-toi|-tu|-t\'|-vous|-en|-y|-ci|-elle|-il";
		String patternStr = "(\\+\\.\\.\\.|\\+/\\.|\\+!\\?|\\+//\\.|\\+/\\?|\\+\"/\\.|\\+\"\\.|\\+//\\?|\\+\\.\\.\\?|\\+\\.)\\s*$";
		Pattern pattern = Pattern.compile(patternStr);
		Matcher matcher = pattern.matcher(s);
		if (matcher.find()) {
			s  = s.replace(matcher.group(), ".");
		}
		s = s.replaceAll("\\+", " ");
		s = s.replaceAll("_", " ");
		s = s.replaceAll("' ", "'");
		s = s.replaceAll(",", "");
		return s;
	}

	public static String clean(String l) {
		l = l.replaceAll( "\\&.*?\\s", " " );
		l = l.replaceAll( "\\([.\\d]\\)", "" );
		l = l.replaceAll( "\\(", "" );
		l = l.replaceAll( "\\)", "" );
		l = l.replaceAll( "\\[.*\\]", "" );
		l = l.replaceAll( "\\x01", "" );
		l = l.replaceAll( "\\x02", "" );
		l = l.replaceAll( "\\x03", "" );
		l = l.replaceAll( "\\x04", "" );
		l = l.replaceAll( "\\x07", "" );
		l = l.replaceAll( "\\x08", "" );
		l = l.replaceAll( "\\x08", "" );
		l = l.replaceAll("\\p{C}", "");
		l = l.replaceAll( "\\+\\<", "" );
		l = l.replaceAll( "<", "" );
		l = l.replaceAll( ">", "" );
		l = l.replaceAll( "⟪", "" );
		l = l.replaceAll( "⟫", "" );
		l = l.replaceAll( "‹", "" );
		l = l.replaceAll( "›", "" );
		l = l.replaceAll( "⌊", "" );
		l = l.replaceAll( "⌋", "" );
		l = l.replaceAll( "⌈", "" );
		l = l.replaceAll( "⌉", "" );
		l = l.replaceAll( ":", "" );
		// ⌊ &=SNIFF ⌋
		// when we'd ⌈ go out of ⌉ town and
		l = l.replaceAll( "0", "" );
		l = l.replaceAll( "1", "" );
		l = l.replaceAll( "2", "" );
		l = l.replaceAll( "3", "" );
		l = l.replaceAll( "4", "" );
		l = l.replaceAll( "5", "" );
		l = l.replaceAll( "6", "" );
		l = l.replaceAll( "7", "" );
		l = l.replaceAll( "8", "" );
		l = l.replaceAll( "9", "" );
		//l = l.replaceAll( " +", " " ); // garder les marqueurs de fin d'énoncé
		//l = l.replaceAll( "' ", "'" );
		return l;
	}
	
	public static void main(String [] args){
		String a = "OBS:	Anaé 0 [=! rit] . ";
		System.out.println(a);
		System.out.println(a.replaceAll("\\p{C}", "XXXX"));
	}
}
