package fr.ortolang.teicorpo;

public class ConllWord {
    String tiers[];
    ConllWord(String u) {
        // split line
        tiers = u.split("\\t");
    }
    public String toString() {
        String s = "Length: " + tiers.length + " ";
        for (int i=0; i < tiers.length - 1; i++) {
            s += tiers[i] + "!-!";
        }
        s += tiers[tiers.length-1];
        return s;
    }
}
