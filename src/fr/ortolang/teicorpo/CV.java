package fr.ortolang.teicorpo;

import java.util.ArrayList;

public class CV{
	String id;
	String cv_desc;
	String cv_desc_lang;
	ArrayList<CV_entry> entries;

	public CV(){			
		entries = new ArrayList<CV_entry>();
	}
}
