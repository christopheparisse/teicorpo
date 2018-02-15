package fr.ortolang.teicorpo;

import java.util.Comparator;

import org.w3c.dom.Element;

public class CompareTimeline implements Comparator<Element>{
	
	public CompareTimeline() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public int compare(Element when1, Element when2) {
		if (!Utils.isNotEmptyOrNull(when1.getAttribute("interval")) && !Utils.isNotEmptyOrNull(when2.getAttribute("interval"))){
			return 0;
		}
		if(!Utils.isNotEmptyOrNull(when1.getAttribute("interval"))){
			return -1;
		}
		else if(!Utils.isNotEmptyOrNull(when2.getAttribute("interval"))){
			return 1;
		}
		// TODO Auto-generated method stub
		try{
			Float start1 = Float.parseFloat(when1.getAttribute("interval"));
			Float start2 = Float.parseFloat(when2.getAttribute("interval"));
			if(start1>start2){
				return 1;
			}
			else if(start1<start2){
				return -1;
			}
			else{
				return 0;
			}
		}
		catch(Exception e){
			return 0;
		}
	}

}
