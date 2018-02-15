package fr.ortolang.teicorpo;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class HierarchicTrans{

	// Nom du fichier
	public String fileName;
	// Chemin du fichier
	public String filePath;
	// Informations en-tête du document
	public MetaInf metaInf;
	// Information sur les tiers présents dans la transcription 
	public HashMap<String, TierInfo> tiersInfo;

	// Représentation de la timeline : map
	public HashMap<String, String> timeline;
	// Représentation de la timeline ordonnée : tableau
	public ArrayList<String> times;

	// Représentation hierarchique des annotations de la transcription
	public HashMap<String, ArrayList<Annot>> hierarchic_representation;

	// Liste des noms des tiers principaux de la transcription
	public ArrayList<String> mainTiersNames;
	// Liste des vocabulaires contrôlés
	public ArrayList<CV> cvs;
	// Ensemble des langues utilisées
	public HashSet<String> languages;
	// Liste des types linguistiques
	public ArrayList<LgqType> lgqTypes;
	// Format initiale de la transcription : Elan, Praat... 
	String initial_format;

	public HierarchicTrans(){
		//Initialisation des structures de données
		tiersInfo = new HashMap<String, TierInfo>();
		timeline = new HashMap<String, String>();
		hierarchic_representation = new HashMap<String, ArrayList<Annot>>();
		metaInf = new MetaInf();
		cvs = new ArrayList<CV>();
		times = new ArrayList<String>();
	}

	public String getTimeValue(String timeId){
		if(Utils.isNotEmptyOrNull(timeId)){
			return timeline.get(timeId.split("#")[1]);
		}
		return "";
	}
	
	//  Passage de la représentation partition à la représentation hierarchique  
	//  Ne fonctionne que si les informations sur les tiers sont bien renseignées
	//  Représentation partition : une map clé = nom du tier, valeur = liste des annotations, ordonné ou non ???? 
	public void partionRepresentationToHierachic(Map<String, ArrayList<Annot>> partition_annotations, TierParams optionsTEI){
		//Ajout des tiers principaux d'abord : ceux qui ne dépendent d'aucune autre annotation
		//Dans les linguistic-type, la contrainte "-" signifie qu'il s'agit d'un type principal
		for(Map.Entry<String , ArrayList<Annot>> entry : partition_annotations.entrySet()){
			String tierName = entry.getKey();
			//System.out.printf(">>%s%n", tierName);
			if (tierName != null && optionsTEI != null) {
				if (optionsTEI.isDontDisplay(tierName, 1))
					continue;
				if (!optionsTEI.isDoDisplay(tierName, 1))
					continue;
			}
			//System.out.printf("OK%n");
			if(this.tiersInfo.get(tierName).type.constraint.equals(LgqType.ROOT)){
				this.hierarchic_representation.put(tierName, entry.getValue());
			}
		}
		//Complétion de l'arbre à partir des annotations principales
		for(Map.Entry<String , ArrayList<Annot>> entry : this.hierarchic_representation.entrySet()){
			String tierName = entry.getKey();
			ArrayList<Annot> annots = entry.getValue();
			buildSubTiers(tierName, annots, partition_annotations);
		}
	}


	// Construction des sous-annotations, à partir des annotations
	// En fonction du type des sous-annotations: temporel ou référentiel
	// Information renseignée dans la variable time_align de l'objet TierInfo
	// Importance de la variable time_align pour reconstruire les dépendances
	public void buildSubTiers(String mainAnnotType, ArrayList<Annot> main_annots, Map<String, ArrayList<Annot>> partition_annotations ){		
		//Pour chaque type de dépendants
		for(String tName : tiersInfo.get(mainAnnotType).dependantsNames){
			//Selon le type d'alignment: temporel ou référentiel
//			if(tiersInfo.get(tName).type.time_align){
			// ce sont tous des time alignable tiers et les éléments symboliques seront distingués à l'impression du xml
				buildSubTimeAlignableTiers(mainAnnotType, main_annots, tName, partition_annotations);
/*			}
			else{
				buildSubRefAlignableTiers(mainAnnotType, main_annots, tName, partition_annotations);
			}
*/		}
	}


	// Construction des dépendances temporelles
	public void buildSubTimeAlignableTiers(String annotType, ArrayList<Annot> main_annots, String subAnnotName, Map<String, ArrayList<Annot>> partition_annotations){
		ArrayList<Annot> subAnnots = partition_annotations.get(subAnnotName);
		for (Annot annot : main_annots){
			for(int j = 0; j<subAnnots.size(); j++){
				Annot subAnnot = subAnnots.get(j);
				if(isInclude(annot.start, annot.end, subAnnot.start, subAnnot.end)){
					annot.dependantAnnotations.add(subAnnot);
				}
			}
		}
		if(tiersInfo.get(subAnnotName).dependantsNames.size()>0){
			buildSubTiers(subAnnotName, subAnnots, partition_annotations);
		}
	}
/*
	// Construction des dépendances référentielles
	public void buildSubRefAlignableTiers(String annotType, ArrayList<Annot> main_annots, String subAnnotName, Map<String, ArrayList<Annot>> align_annotations){
		ArrayList<Annot> subAnnots = align_annotations.get(subAnnotName);
		for (Annot annot : main_annots){
			for(int j = 0; j<subAnnots.size(); j++){//Obligé de parcourir toutes les annotations ? ou ordonnés??
				Annot subAnnot = subAnnots.get(j);
				if ((Utils.isNotEmptyOrNull(subAnnot.link) && subAnnot.link.equals(annot.id)) || isInclude(annot.start, annot.end, subAnnot.start, subAnnot.end)){
					annot.dependantAnnotations.add(subAnnot);
					break;
				}
			}
			if(tiersInfo.get(subAnnotName).dependantsNames.size()>0){
				buildSubTiers(subAnnotName, subAnnots, align_annotations);
			}
		}
	}
*/
	public boolean isInclude(String mainStart, String mainEnd, String subStart, String subEnd ) {
		try {
			Double mainStartDouble = Double.parseDouble((mainStart));
			Double mainEndDouble = Double.parseDouble((mainEnd));
			Double subStartDouble = Double.parseDouble((subStart));
			Double subEndDouble = Double.parseDouble((subEnd));
			boolean val = (subStartDouble >= mainStartDouble && subEndDouble <= mainEndDouble);
			//System.out.println(mainStartDouble + "-" + mainEndDouble + "->" + subStartDouble + "-" + subEndDouble + "  resBool ->  "+  val);
			return val;
		} catch(Exception e) {
			return false;
		}
	}

	public static void main (String args[]) throws IOException{
		//Exemple
		HierarchicTrans ht = new HierarchicTrans();
		HashMap<String, ArrayList<Annot>> partition_annotations = new HashMap<String, ArrayList<Annot>>();
		ht.partionRepresentationToHierachic(partition_annotations, null);

	}
}
