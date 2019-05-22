package fr.ortolang.teicorpo;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class Tests {
  public static void main(String[] args) {
    Result result2 = JUnitCore.runClasses(ClanToTeiTest.class, ElanToTeiTest.class, TeiToClanTest.class, TeiToElanTest.class);
    for (Failure failure : result2.getFailures()) {
      System.out.println(failure.getDescription());
    }
	if (result2.wasSuccessful() == true){
		//127 : nombre magique afin de signaler
		// que tout les tests sont passés
		// System.exit(127);
		System.out.println("All results2 ok.");
	}
  }
}