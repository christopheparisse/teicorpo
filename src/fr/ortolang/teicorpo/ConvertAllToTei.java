package fr.ortolang.teicorpo;

public class ConvertAllToTei {

	public static void main(String[] args) {
		try {
			ClanToTei.main(args);
			ElanToTei.main(args);
			TranscriberToTei.main(args);
			PraatToTei.main(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
