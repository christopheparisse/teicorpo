package fr.ortolang.teicorpo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TierInfo {
	LgqType type; // Type linguistique LINGUISTIC_TYPE_REF (with other information)
	String participant; // PARTICIPANT
	String parent; // PARENT_REF
	String annotator; // ANNOTATOR
	String lang; // DEFAULT_LOCALE
	String lang_ref; // LANG_REF
	String tier_id; // TIER_ID (often used as a pointer to this class)
	ArrayList<String> dependantsNames;

	TierInfo() {
		tier_id = "";
		type = new LgqType();
		dependantsNames = new ArrayList<String>();
	}
	
	public String toString() {
		String s = " Dependants:{";
		for(int i=0; i<dependantsNames.size() ; i++) {
			s += dependantsNames.get(i) + ";";
		}
		s += "}";
		return "tier: " + this.tier_id 
				+ " part:" + this.participant 
				+ " parent:" + this.parent 
				+ " annot:" + this.annotator 
				+ " type:" + this.type.toString() 
				+ " lang:" + this.lang
				+ " lang_ref:" + this.lang_ref 
				+ s;
	}
	
	public static void buildDependantsNames(HashMap<String, TierInfo> tiersInfo){
		for(Map.Entry<String , TierInfo> entry : tiersInfo.entrySet()){
			String tierName = entry.getKey();
			for(Map.Entry<String , TierInfo> tierINF : tiersInfo.entrySet()){
				String parent = tierINF.getValue().parent;
				if(Utils.isNotEmptyOrNull(parent) && parent.equals(tierName) && !entry.getValue().dependantsNames.contains(tierINF.getKey())){
					entry.getValue().dependantsNames.add(tierINF.getKey());
				}
			}
		}
	}
}

