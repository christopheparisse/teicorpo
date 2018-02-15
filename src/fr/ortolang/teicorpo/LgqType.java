package fr.ortolang.teicorpo;

public class LgqType{
	
	///// Variables statiques de contraintes pour les types linguistiques, objet LgqType /////
	///// -, Symbolic_Association, Symbolic_Subdivision, Time_Subdivision, Included_In /////
	public static String ROOT = "-";
	public static String SYMB_ASSOC = "Symbolic_Association";
	public static String INCLUDED = "Included_In";
	public static String SYMB_DIV = "Symbolic_Subdivision";
	public static String TIME_DIV = "Time_Subdivision";
	public static String POINT = "Point";
	public static String TIME_INT = "Time_Interval";
	
//	String lgq_type_name; // LINGUISTIC_TYPE_REF  (dans le TIER pointe sur LINGUISTIC_TYPE_ID)
	String lgq_type_id; // LINGUISTIC_TYPE_ID : id du type (nom utilisé dans ELAN)
	String constraint;// CONSTRAINTS Liste fermée : Symbolic_Association, Symbolic_Subdivision, Time_Subdivision, Included_In, Point?
	String cv_ref; // CONTROLLED_VOCABULARY_REF: Vocabulaire controlé
	String graphic_ref;// GRAPHIC_REFERENCES : Par défaut false
	boolean time_align; //  TIME_ALIGNABLE : dépend de contrainte
	
	public LgqType() {
		lgq_type_id = "";
		constraint = "";
		cv_ref = null;
		graphic_ref = "false";
	}
	
	public String toString() {
		return // "Name:" + this.lgq_type_name +
				  " ID:" + this.lgq_type_id 
				+ " Constraint:" + this.constraint
				+ " CV:" + this.cv_ref
				+ " GraphRef:" + this.graphic_ref
				+ " TimeAlign:" + (this.time_align ? "true" : "false");
	}

	public static boolean isTimeType(String typeSG) {
		if (typeSG.equals(LgqType.ROOT) || typeSG.equals(LgqType.TIME_DIV) || typeSG.equals(LgqType.INCLUDED) || typeSG.equals(LgqType.POINT) || typeSG.equals(LgqType.TIME_INT))
			return true;
		return false;
	}
}