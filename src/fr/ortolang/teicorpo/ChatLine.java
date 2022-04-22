package fr.ortolang.teicorpo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatLine {

	String head;
	String tail;
	
	ChatLine(String line) {
		if (line == null || line.isEmpty()){
			head = "";
			tail = "";
		} else {
			Pattern pattern = Pattern.compile("([%*@][A-Za-zÀ-ÖØ-öø-ÿ_0-9_-]*)[\\s:]+(.*)");
			Matcher matcher = pattern.matcher(line);
			//System.out.println(line);
			if (matcher.matches()) { // cas des lignes avec @ ou * et un :
				head = matcher.group(1);
				if (head.length() == 1) {
					head = head + "UNK";
				}
				tail = matcher.group(2);
				//System.out.printf("x> %s <x> %s%n", head, tail);
			} else {// cas des lignes avec @ mais sans :
				head = line;
				tail = "";
				//System.out.printf("xx> %s%n", head);
			}
		}
	}

	String type(){
		return head;
	}

	public String toString() {
		return "[" + head + "][" + tail + "]";
	}

	public static void main(String[] args) {
		ChatLine cl = new ChatLine(args[0]);
		System.out.println(cl.head + " :-: " + cl.tail);
	}
}
