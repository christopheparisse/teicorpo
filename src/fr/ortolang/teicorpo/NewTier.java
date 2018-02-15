package fr.ortolang.teicorpo;

public class NewTier {
	String newID;
	String oldID;
	String lingType;
	String parent;
	
	public NewTier() {
		newID = "";
		oldID = "";
		lingType = "";
		parent = "";
	}

	public NewTier(String tn, String otn, String lt, String p) {
		newID = tn;
		oldID = otn;
		lingType = lt;
		parent = p;
	}
	
	public String toString() {
		return "newtier: NI:" + newID + " OI:" + oldID + " LT:" + lingType + " P:" + parent;
	}
}
