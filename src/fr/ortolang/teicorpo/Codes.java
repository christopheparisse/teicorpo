package fr.ortolang.teicorpo;

public class Codes {
	String leftBracket; // for xml and others (chat groups)
	String rightBracket; // idem
	String leftEvent; // for events 
	String rightEvent; // idem
	String leftParent; // 
	String rightParent; // 
	String leftCode; // codes inside events
	String rightCode; // idem
	
	void standardCodes () {
		leftBracket = "<"; // 27EA - "❮"; // "⟨" 27E8 - "❬" 
		rightBracket = ">"; // 27EB - "❯"; // "⟩" 27E9 - "❭" - 276C à 2771 ❬ ❭ ❮ ❯ ❰ ❱ 
		leftEvent = "[=!"; // 27E6 - "『"; // 300E - "⌈"; // u2308 
		rightEvent = "]"; // 27E7 - "』"; // 300F - "⌋"; // u230b
		leftParent = "[%"; // 2045 // "⁘"; // 2058 // "⁑" // 2051
		rightParent = "]"; // 2046 // "⁘"; // 2058
		leftCode = "{"; // 231C - "⁌"; // 204C
		rightCode = "}"; // 231F - "⁍"; // 204D
	}

	void advancedCodes () {
		leftBracket = "⟪"; // 27EA - "❮"; // "⟨" 27E8 - "❬" 
		rightBracket = "⟫"; // 27EB - "❯"; // "⟩" 27E9 - "❭" - 276C à 2771 ❬ ❭ ❮ ❯ ❰ ❱ 
		leftEvent = "⟦"; // 27E6 - "『"; // 300E - "⌈"; // u2308 
		rightEvent = "⟧"; // 27E7 - "』"; // 300F - "⌋"; // u230b
		leftParent = "⁅"; // 2045 // "⁘"; // 2058 // "⁑" // 2051
		rightParent = "⁆"; // 2046 // "⁘"; // 2058
		leftCode = "⌜"; // 231C - "⁌"; // 204C
		rightCode = "⌟"; // 231F - "⁍"; // 204D
	}
}
