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
			Pattern pattern = Pattern.compile("([%*@]\\w+)[\\s:]+(.*)");
			Matcher matcher = pattern.matcher(line);
			if (matcher.matches()) { // cas des lignes avec @ ou * et un :
				head = matcher.group(1);
				tail = matcher.group(2);
			} else {// cas des lignes avec @ mais sans :
				head = line;
				tail = "";
			}
		}
	}

	String type(){
		return head;
	}

	public static void main(String[] args) {
		ChatLine cl = new ChatLine(args[0]);
		System.out.println(cl.head + " :-: " + cl.tail);
	}
}
