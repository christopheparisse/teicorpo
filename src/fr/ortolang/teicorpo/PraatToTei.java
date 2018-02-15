/**
 * reading praat files
 * file taken from ELAN open sources
 * adaptation by Christophe Parisse
 */

package fr.ortolang.teicorpo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * A class to extract annotations from a Praat .TextGrid file. Only
 * "IntervalTier"s and "TextTier"s are supported. The expected format is roughly
 * like below, but the format is only loosely checked.
 * 
 * 1 File type = "ooTextFile" 2 Object class = "TextGrid" 3 4 xmin = 0 5 xmax =
 * 36.59755102040816 6 tiers? &lt;exists&; 7 size = 2 8 item []: 9 item [1]: 10
 * class = "IntervalTier" 11 name = "One" 12 xmin = 0 13 xmax =
 * 36.59755102040816 14 intervals: size = 5 15 intervals [1]: 16 xmin = 0 17
 * xmax = 1 18 text = ""
 * 
 * @version Feb 2013 the short notation format (roughly the same lines without
 *          the keys and the indentation) is now also supported
 */
public class PraatToTei extends GenericMain {
	static String EXT = ".textgrid";

	private final char brack = '[';
	private final String eq = "=";
	private final String item = "item";
	private final String cl = "class";
	private final String tierSpec = "IntervalTier";
	private final String textTierSpec = "TextTier";
	private final String nm = "name";
	private final String interval = "intervals";
	private final String min = "xmin";
	private final String max = "xmax";
	private final String tx = "text";
	private final String points = "points";
	private final String time = "time";
	private final String mark = "mark";
	private final String number = "number";
	private final String escapedInnerQuote = "\"\"";
	private final String escapedOuterQuote = "\"\"\"";

	private boolean includeTextTiers = false;
	private String encoding;
	private boolean verbose = false;

	private File gridFile;
	private Map<String, String> tierNames;
	private Map<String, ArrayList<Annot>> annotationMap;
	private PraatSpecialChars lookUp;
	
	private TierParams optionsTEI = null;

	private enum SN_POSITION {// short notation line position
		OUTSIDE, // not in any type of tier
		NEXT_IS_NAME, // next line is a tier name
		NEXT_IS_MIN, // next line is the min time of interval
		NEXT_IS_MAX, // next line is the max time of interval
		NEXT_IS_TEXT, // next line is the text of an interval
		NEXT_IS_TIME, // next line is the time of a point annotation
		NEXT_IS_MARK, // next line is the text of a point annotation
		NEXT_IS_TOTAL_MIN, // next line is the overall min of a tier, ignored
		NEXT_IS_TOTAL_MAX, // next line is the overall max of a tier, ignored
		NEXT_IS_SIZE// next line is the number of annotations of a tier, ignored
	};

	/**
	 * Creates a new Praat TextGrid parser for the file at the specified path
	 *
	 * @param fileName
	 *            the path to the file
	 *
	 * @throws IOException
	 *             if the file can not be read, for whatever reason
	 */
	public void init(String fileName) throws IOException {
		init(fileName, false, 1);
	}

	/**
	 * Creates a new Praat TextGrid parser for the file at the specified path.
	 *
	 * @param fileName
	 *            the path to the file
	 * @param includeTextTiers
	 *            if true "TextTiers" will also be parsed
	 * @param pointDuration
	 *            the duration of annotations if texttiers are also parsed
	 *
	 * @throws IOException
	 *             if the file can not be read, for whatever reason
	 */
	public void init(String fileName, boolean includeTextTiers, int pointDuration) throws IOException {
		if (fileName != null) {
			gridFile = new File(fileName);
		}
		this.includeTextTiers = includeTextTiers;
		parse();
	}

	/**
	 * Creates a new Praat TextGrid parser for the file at the specified path.
	 *
	 * @param fileName
	 *            the path to the file
	 * @param includeTextTiers
	 *            if true "TextTiers" will also be parsed
	 * @param pointDuration
	 *            the duration of annotations if texttiers are also parsed
	 * @param encoding
	 *            the character encoding of the file
	 *
	 * @throws IOException
	 *             if the file can not be read, for whatever reason
	 */
	public void init(String fileName, boolean includeTextTiers, int pointDuration, String encoding)
			throws IOException {
		if (fileName != null) {
			gridFile = new File(fileName);
		}
		this.includeTextTiers = includeTextTiers;
		this.encoding = encoding;

		parse();
	}

	/**
	 * Creates a new Praat TextGrid parser for the specified file.
	 *
	 * @param gridFile
	 *            the TextGrid file
	 *
	 * @throws IOException
	 *             if the file can not be read, for whatever reason
	 */
	public void init(File gridFile) throws IOException {
		init(gridFile, false, 1);
	}

	/**
	 * Creates a new Praat TextGrid parser for the specified file.
	 *
	 * @param gridFile
	 *            the TextGrid file
	 * @param includeTextTiers
	 *            if true "TextTiers" will also be parsed
	 * @param pointDuration
	 *            the duration of annotations if texttiers are also parsed
	 * 
	 * @throws IOException
	 *             if the file can not be read, for whatever reason
	 */
	public void init(File gridFile, boolean includeTextTiers, int pointDuration) throws IOException {
		init(gridFile, includeTextTiers, pointDuration, null);
	}

	/**
	 * Creates a new Praat TextGrid parser for the specified file.
	 *
	 * @param gridFile
	 *            the TextGrid file
	 * @param includeTextTiers
	 *            if true "TextTiers" will also be parsed
	 * @param pointDuration
	 *            the duration of annotations if texttiers are also parsed
	 * @param encoding
	 *            the character encoding of the file
	 * 
	 * @throws IOException
	 *             if the file can not be read, for whatever reason
	 */
	public void init(File gridFile, boolean includeTextTiers, int pointDuration, String encoding) throws IOException {
		this.gridFile = gridFile;

		this.includeTextTiers = includeTextTiers;
		this.encoding = encoding;

		parse();
	}

	/**
	 * Returns a list of detected interval tiers.
	 *
	 * @return a list of detected interval tiernames
	 */
	public Map<String, String> getTierNames() {
		return tierNames;
	}

	/**
	 * Returns a list of annotation records for the specified tier.
	 *
	 * @param tierName
	 *            the name of the tier
	 *
	 * @return the annotation records of the specified tier
	 */
	public ArrayList<Annot> getAnnotationRecords(String tierName) {
		if ((tierName == null) || (annotationMap == null)) {
			return null;
		}

		ArrayList<Annot> value = annotationMap.get(tierName);

		if (value instanceof ArrayList) {
			return value;
		}

		return null;
	}

	/**
	 * Reads a few lines and returns whether the file is in short notation.
	 * 
	 * @param reader
	 *            the reader object
	 * @return true if in short text notation, false otherwise
	 */
	private boolean isShortNotation(BufferedReader reader) throws IOException {
		if (reader == null) {
			return false;
		}

		String line;
		int lineCount = 0;

		boolean xmin = false, xmax = false, tiers = false;// are the keys xmin
															// and xmax and
															// tiers? found

		while ((line = reader.readLine()) != null && lineCount < 5) {
			if (line.length() == 0) {
				continue;// skip empty lines
			}

			if (lineCount == 2) {
				xmin = (line.indexOf(min) > -1);
			}
			if (lineCount == 3) {
				xmax = (line.indexOf(max) > -1);
			}
			if (lineCount == 4) {
				tiers = (line.indexOf("tiers?") > -1);
			}
			lineCount++;
		}

		return (!xmin && !xmax && !tiers);
	}

	/**
	 * Parses the file and extracts interval tiers with their annotations.
	 *
	 * @throws IOException
	 *             if the file can not be read for any reason
	 */
	private void parse() throws IOException {
		if ((gridFile == null) || !gridFile.exists()) {
			System.err.println("No existing file specified.");
			System.exit(1);
		}

		BufferedReader reader = null;

		try {
			if (encoding == null) {
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(gridFile)));
			} else {
				try {
					reader = new BufferedReader(new InputStreamReader(new FileInputStream(gridFile), encoding));
				} catch (UnsupportedEncodingException uee) {
					System.err.println("Unsupported encoding: " + uee.getMessage());
					reader = new BufferedReader(new InputStreamReader(new FileInputStream(gridFile)));
				}
			}
			// Praat files on Windows and Linux are created with encoding
			// "Cp1252"
			// on Mac with encoding "MacRoman". The ui could/should be extended
			// with an option to specify the encoding
			// InputStreamReader isr = new InputStreamReader(
			// new FileInputStream(gridFile));
			// System.out.println("Encoding: " + isr.getEncoding());
			// System.out.println("Read encoding: " + encoding);
			if (verbose) System.out.println("Read encoding: " + encoding);

			boolean isShortNotation = isShortNotation(reader);
			if (verbose) System.out.println("Praat TextGrid is in short notation: " + isShortNotation);

			if (isShortNotation) {
				parseShortNotation(reader);
				return;
			}

			tierNames = new LinkedHashMap<String, String>();
			annotationMap = new HashMap<String, ArrayList<Annot>>();

			ArrayList<Annot> records = new ArrayList<Annot>();
			Annot record = null;

			String line;
			// int lineNum = 0;
			String tierName = null;
			String annValue = "";
			String begin = "-1";
			String end = "-1";
			boolean inTier = false;
			boolean inInterval = false;
			boolean inTextTier = false;
			boolean inPoints = false;
			int eqPos = -1;

			while ((line = reader.readLine()) != null) {
				// lineNum++;
				// System.out.println(lineNum + " " + line);

				if ((line.indexOf(cl) >= 0) && ((line.indexOf(tierSpec) > 5) || (line.indexOf(textTierSpec) > 5))) {
					// check if we have to include text (point) tiers
					if (line.indexOf(textTierSpec) > 5) {
						if (includeTextTiers) {
							inTextTier = true;
						} else {
							inTextTier = false;
							inTier = false;
							continue;
						}
					}
					// begin of a new tier
					records = new ArrayList<Annot>();
					inTier = true;

					continue;
				}

				if (!inTier) {
					continue;
				}

				eqPos = line.indexOf(eq);

				if (inTextTier) {
					// text or point tier
					if (eqPos > 0) {
						// split and parse
						if (!inPoints && (line.indexOf(nm) >= 0) && (line.indexOf(nm) < eqPos)) {
							tierName = extractTierName(line, eqPos);

							if (!annotationMap.containsKey(tierName)) {
								annotationMap.put(tierName, records);
								tierNames.put(tierName, "TextTier");
								if (verbose) System.out.println("Point Tier detected: " + tierName);
							} else {
								// the same (sometimes empty) tiername can occur
								// more than once, rename
								int count = 2;
								String nextName = "";
								for (; count < 50; count++) {
									nextName = tierName + "-" + count;
									if (!annotationMap.containsKey(nextName)) {
										annotationMap.put(nextName, records);
										tierNames.put(nextName, "TextTier");
										if (verbose) System.out.println(
												"Point Tier detected: " + tierName + " and renamed to: " + nextName);
										break;
									}
								}
							}

							continue;
						} else if (!inPoints) {
							continue;
						} else if (line.indexOf(time) > -1 || line.indexOf(number) > -1) {
							begin = extractTime(line, eqPos);
							// System.out.println("B: " + begin);
						} else if (line.indexOf(mark) > -1) {
							// extract value
							annValue = extractTextValue(line, eqPos);
							// finish and add the annotation record
							inPoints = false;
							// System.out.println("T: " + annValue);
							record = new Annot(tierName, annValue, begin,
									"-1" /* begin + pointDuration */);
							records.add(record);
							// reset
							annValue = "";
							begin = "-1";
						}
					} else {
						// points??
						if ((line.indexOf(points) >= 0) && (line.indexOf(brack) > points.length())) {
							inPoints = true;

							continue;
						} else {
							if ((line.indexOf(item) >= 0) && (line.indexOf(brack) > item.length())) {
								// reset
								inTextTier = false;
								inPoints = false;
							}
						}
					} // end point tier
				} else {
					// interval tier
					if (eqPos > 0) {
						// split and parse
						if (!inInterval && (line.indexOf(nm) >= 0) && (line.indexOf(nm) < eqPos)) {
							tierName = extractTierName(line, eqPos);

							if (!annotationMap.containsKey(tierName)) {
								annotationMap.put(tierName, records);
								tierNames.put(tierName, "IntervalTier");
								if (verbose) System.out.println("Tier detected: " + tierName);
							} else {
								// the same (sometimes empty) tiername can occur
								// more than once, rename
								int count = 2;
								String nextName = "";
								for (; count < 50; count++) {
									nextName = tierName + "-" + count;
									if (!annotationMap.containsKey(nextName)) {
										annotationMap.put(nextName, records);
										tierNames.put(nextName, "IntervalTier");
										if (verbose) System.out
												.println("Tier detected: " + tierName + " and renamed to: " + nextName);
										break;
									}
								}
							}

							continue;
						} else if (!inInterval) {
							continue;
						} else if (line.indexOf(min) > -1) {
							begin = extractTime(line, eqPos);
							// System.out.println("B: " + begin);
						} else if (line.indexOf(max) > -1) {
							end = extractTime(line, eqPos);
							// System.out.println("E: " + end);
						} else if (line.indexOf(tx) > -1) {
							// extract value
							annValue = extractTextValue(line, eqPos);
							// finish and add the annotation record
							inInterval = false;
							// System.out.println("T: " + annValue);
							record = new Annot(tierName, annValue, begin, end);
							if (Utils.isNotEmptyOrNull(annValue)) {
								records.add(record);
							}
							// reset
							annValue = "";
							begin = "-1";
							end = "-1";
						}
					} else {
						// interval?
						if ((line.indexOf(interval) >= 0) && (line.indexOf(brack) > interval.length())) {
							inInterval = true;

							continue;
						} else {
							if ((line.indexOf(item) >= 0) && (line.indexOf(brack) > item.length())) {
								// reset
								inTier = false;
								inInterval = false;
							}
						}
					}
				}
			}

			reader.close();
		} catch (IOException ioe) {
			if (reader != null) {
				reader.close();
			}

			throw ioe;
		} catch (Exception fe) {
			if (reader != null) {
				reader.close();
			}

			fe.printStackTrace();
			throw new IOException("Error occurred while reading the file: " + fe.getMessage());
		}
	}

	/**
	 * Parses the short notation of a Praat TextGrid file. Handling completely
	 * separated from the long notation.
	 * 
	 * @param reader
	 *            the (configured) reader
	 */
	private void parseShortNotation(BufferedReader reader) throws IOException {
		if (reader == null) {
			throw new IOException("The reader object is null, cannot read from the file.");
		}

		tierNames = new LinkedHashMap<String, String>();
		annotationMap = new HashMap<String, ArrayList<Annot>>();

		ArrayList<Annot> records = new ArrayList<Annot>();
		Annot record = null;

		String line;
		String tierName = null;
		String annValue = "";
		String begin = "-1";
		String end = "-1";

		boolean inTextTier = false;// in text tier
		boolean inTier = false;// in interval tier
		int eqPos = -1;

		SN_POSITION linePos = SN_POSITION.OUTSIDE;

		while ((line = reader.readLine()) != null) {
			if (line.length() == 0) {
				continue;
			}

			if (line.indexOf(tierSpec) > -1) {
				linePos = SN_POSITION.NEXT_IS_NAME;
				inTier = true;
				inTextTier = false;
				continue;
			}
			if (line.indexOf(textTierSpec) > -1) {
				linePos = SN_POSITION.NEXT_IS_NAME;
				inTier = false;
				inTextTier = true;
				continue;
			}

			if (linePos == SN_POSITION.NEXT_IS_NAME) {// tier name on this line
				if (!inTier && !inTextTier) {
					linePos = SN_POSITION.NEXT_IS_TOTAL_MIN;
					continue;
				}

				if (inTier || (inTextTier && includeTextTiers)) {
					tierName = removeQuotes(line);
					if (tierName.length() == 0) {
						tierName = "Noname";
					}

					records = new ArrayList<Annot>();

					if (!annotationMap.containsKey(tierName)) {
						annotationMap.put(tierName, records);
						if (inTextTier) {
							tierNames.put(tierName, "TextTier");
							if (verbose) System.out.println("Point Tier detected: " + tierName);
						} else {
							tierNames.put(tierName, "IntervalTier");
							if (verbose) System.out.println("Interval Tier detected: " + tierName);
						}
					} else {
						// the same (sometimes empty) tiername can occur more
						// than once, rename
						int count = 2;
						String nextName = "";
						for (; count < 50; count++) {
							nextName = tierName + "-" + count;
							if (!annotationMap.containsKey(nextName)) {
								annotationMap.put(nextName, records);
								if (inTextTier) {
									tierNames.put(nextName, "TextTier");
									if (verbose) System.out.println(
											"Point Tier detected: " + tierName + " and renamed to: " + nextName);
								} else {
									tierNames.put(nextName, "IntervalTier");
									if (verbose) System.out.println(
											"Interval Tier detected: " + tierName + " and renamed to: " + nextName);
								}
								break;
							}
						}
					}
				}

				linePos = SN_POSITION.NEXT_IS_TOTAL_MIN;
				continue;
			}

			if (linePos == SN_POSITION.NEXT_IS_TOTAL_MIN) {
				linePos = SN_POSITION.NEXT_IS_TOTAL_MAX;
				continue;
			}

			if (linePos == SN_POSITION.NEXT_IS_TOTAL_MAX) {
				linePos = SN_POSITION.NEXT_IS_SIZE;
				continue;
			}

			if (linePos == SN_POSITION.NEXT_IS_SIZE) {
				if (inTextTier) {
					linePos = SN_POSITION.NEXT_IS_TIME;
				} else {// interval tier
					linePos = SN_POSITION.NEXT_IS_MIN;
				}
				continue;
			}
			// point text tiers
			if (linePos == SN_POSITION.NEXT_IS_TIME) {
				if (includeTextTiers) {
					// hier extract time
					begin = extractTime(line, eqPos);// eqPos = -1
				}
				linePos = SN_POSITION.NEXT_IS_MARK;
				continue;
			}

			if (linePos == SN_POSITION.NEXT_IS_MARK) {
				if (includeTextTiers) {
					annValue = extractTextValue(line, eqPos);// eqPos = -1
					// finish and add the annotation record
					record = new Annot(tierName, annValue, begin,
							"-1" /* begin + pointDuration */);
					if (Utils.isNotEmptyOrNull(annValue)) {
						records.add(record);
					}
					// reset
					annValue = "";
					begin = "-1";
				}
				linePos = SN_POSITION.NEXT_IS_TIME;
				continue;
			}
			// interval tiers
			if (linePos == SN_POSITION.NEXT_IS_MIN) {
				begin = extractTime(line, eqPos);// eqPos = -1
				linePos = SN_POSITION.NEXT_IS_MAX;
				continue;
			}

			if (linePos == SN_POSITION.NEXT_IS_MAX) {
				end = extractTime(line, eqPos);// eqPos = -1
				linePos = SN_POSITION.NEXT_IS_TEXT;
				continue;
			}

			if (linePos == SN_POSITION.NEXT_IS_TEXT) {
				// extract value
				annValue = extractTextValue(line, eqPos);// eqPos = -1
				// finish and add the annotation record
				record = new Annot(tierName, annValue, begin, end);
				if (Utils.isNotEmptyOrNull(annValue)) {
					records.add(record);
				}
				// reset
				annValue = "";
				begin = "-1";
				end = "-1";

				linePos = SN_POSITION.NEXT_IS_MIN;
				continue;
			}
		}
	}

	/**
	 * Extracts the tiername from a line.
	 *
	 * @param line
	 *            the line
	 * @param eqPos
	 *            the indexof the '=' sign
	 *
	 * @return the tier name
	 */
	private String extractTierName(String line, int eqPos) {
		if (line.length() > (eqPos + 1)) {
			String name = line.substring(eqPos + 1).trim();

			if (name.length() < 3) {
				if ("\"\"".equals(name)) {
					return "Noname";
				}

				return name;
			}

			return removeQuotes(name);
		}

		return line; // or null??
	}

	/**
	 * Extracts the text value and, if needed, converts Praat's special
	 * character sequences into unicode chars.
	 *
	 * @param value
	 *            the text value
	 * @param eqPos
	 *            the index of the equals sign
	 *
	 * @return the annotation value. If necessary Praat's special symbols have
	 *         been converted to Unicode.
	 */
	private String extractTextValue(String value, int eqPos) {
		if (value.length() > (eqPos + 1)) {
			String rawV = removeQuotes(value.substring(eqPos + 1).trim()); // should
																			// be
																			// save

			if (lookUp == null) {
				lookUp = new PraatSpecialChars();
			}
			rawV = lookUp.replaceIllegalXMLChars(rawV);

			if (rawV.indexOf('\\') > -1) {
				// convert
				// if (lookUp == null) {
				// lookUp = new PraatSpecialChars();
				// }

				return lookUp.convertSpecialChars(rawV);
			}

			return rawV;
		}

		return "";
	}

	/**
	 * Extracts a double time value, multiplies by 1000 (sec to ms) and converts
	 * to Time (string).
	 *
	 * @param value
	 *            the raw value
	 * @param eqPos
	 *            the index of the equals sign
	 *
	 * @return the time value rounded to milliseconds
	 */
	private String extractTime(String value, int eqPos) {
		if (value.length() > (eqPos + 1)) {
			String v = value.substring(eqPos + 1).trim();
			return v;
		}

		return "-1";
	}

	/**
	 * Removes a beginning and end quote mark from the specified string. Does no
	 * null check nor are spaces trimmed.
	 * 
	 * @version Feb 2013 added handling for outer escaped quotes (""
	 *          ") and inner escaped quotes ("")
	 *
	 * @param value
	 *            the value of which leading and trailing quote chars should be
	 *            removed
	 *
	 * @return the value without the quotes
	 */
	private String removeQuotes(String value) {
		boolean removeOuterQuotes = true;
		if (value.startsWith(escapedOuterQuote) && value.endsWith(escapedOuterQuote)) {
			removeOuterQuotes = false;
		}
		// replace all """ sequences by a single "
		value = value.replaceAll(escapedOuterQuote, "\"");
		value = value.replaceAll(escapedInnerQuote, "\"");

		if (removeOuterQuotes) {
			if (value.charAt(0) == '"') {
				if (value.charAt(value.length() - 1) == '"' && value.length() > 1) {
					return value.substring(1, value.length() - 1);
				} else {
					return value.substring(1);
				}
			} else {
				if (value.charAt(value.length() - 1) == '"') {
					return value.substring(0, value.length() - 1);
				} else {
					return value;
				}
			}
		} else {
			return value;
		}
	}

	public void displayAnnnotations(PraatToTei ptg) {
		/*
		 * display annotations (for debugging purposes)
		 */

		System.out.println("ANNOTATIONS");
		for (Map.Entry<String, ArrayList<Annot>> element : ptg.annotationMap.entrySet()) {
			System.out.println(element.getKey() + " :- ");
			ArrayList<Annot> l = element.getValue();
			for (int j = 0; j < l.size(); j++) {
				System.out.println("    " + j + " :- " + l.get(j));
			}
		}
	}

	public boolean convertFromPraatToTei(String inputfile, String outputfile, TierParams options) { 
		// String encoding, ArrayList<DescTier> ldt, String mediaName) {
		optionsTEI = options;
		try {
			/*
			 * try to find a param file
			 */
			String bn = Utils.fullbasename(inputfile);
			File fnb = new File(bn + ".paramtei");
			if (fnb.exists()) {
				if (optionsTEI.ldt.size() > 0) {
					System.err.println("Attention paramètres commande peut-être ignorés pour " + inputfile);
				}
				if (verbose) System.out.println("Utilisation du fichier paramètres " + fnb.toString());
				if (TierParams.getTierParams(fnb.toString(), optionsTEI.ldt, optionsTEI) == false)
					System.err.println("Erreur de traitement du fichier paramètres: " + fnb.toString());
			}
			try {
				if (options.detectEncoding == true)
					options.encoding = EncodingDetector.detect(inputfile);
			} catch (Exception e) {
				;
			}
			init(inputfile, true, 100, options.encoding);
			if (verbose) System.out.println("Fichier " + inputfile + " Encoding: " + (encoding != null ? encoding : "par défaut"));
			/*
			 * construire un hierarchic trans
			 */
			HierarchicTrans ht = new HierarchicTrans();
			ht.initial_format = "Praat";
			ht.metaInf.version = Utils.versionSoft;
			ht.metaInf.time_units = "s";
			/*
			 * construire tiers info
			 */
			File inputFileObject = new File(inputfile);
			ht.fileName = inputFileObject.getName();
			ht.filePath = inputFileObject.getAbsolutePath();

			if (Utils.isNotEmptyOrNull(optionsTEI.mediaName)) {
				Media m = new Media("", (new File(optionsTEI.mediaName)).getAbsolutePath());
				ht.metaInf.medias.add(m);
			} else {
				String url = Utils.findClosestMedia("", inputfile, "audio");
				Media m = new Media("", (new File(url)).getAbsolutePath());
				ht.metaInf.medias.add(m);
			}
			if (optionsTEI.ignorePraatNumbering == false) {
				for (int i=0; i<optionsTEI.ldt.size(); i++) {
					DescTier a = optionsTEI.ldt.get(i);
					try {
						a.workingTier = a.tier; // reinit if not found
						a.workingParent = a.parent;
						int t = Integer.valueOf(a.tier);
						int p = Integer.valueOf(a.parent);
						// replace numbers by true values
						int nth = 1; // tier number
						for (Map.Entry<String, String> element : tierNames.entrySet()) {
							if (nth == t) {
								TierInfo value = new TierInfo();
								value.participant = element.getKey();
								a.workingTier = value.participant;
								break;
							}
							nth++;
						}
						nth = 1; // tier number
						for (Map.Entry<String, String> element : tierNames.entrySet()) {
							if (nth == p) {
								TierInfo value = new TierInfo();
								value.participant = element.getKey();
								a.workingParent = value.participant;
								break;
							}
							nth++;
						}
					} catch (Exception e) {
						continue;
					}
				}
			} else {
				// we need a copy in the working fields
				for (int i=0; i<optionsTEI.ldt.size(); i++) {
					DescTier a = optionsTEI.ldt.get(i);
					a.workingTier = a.tier;
					a.workingParent = a.parent;
				}				
			}
			if (verbose) System.out.println("TIERS Information");
			for (Map.Entry<String, String> element : tierNames.entrySet()) {
				TierInfo value = new TierInfo();
				value.participant = element.getKey();
				// find if the tier is in the list of constraints
				boolean found = false;
				for (int j = 0; j < optionsTEI.ldt.size(); j++) {
					// System.out.println(" " + j + " :- " +
					// ldt.get(j).print());
					DescTier a = optionsTEI.ldt.get(j);
					if (a.workingTier.equalsIgnoreCase(value.participant)) {
						value.type.lgq_type_id = value.participant;
						value.type.constraint = DescTier.whichType(a.type);
						if (value.type.constraint == LgqType.SYMB_ASSOC || value.type.constraint == LgqType.SYMB_DIV) {
							value.type.time_align = false;
						} else {
							value.type.time_align = true;
						}
						value.parent = a.workingParent;
						found = true;
						if (verbose) System.out.println("TIER: " + a.workingToString());
						break;
					}
				}
				if (!found) {
					value.type.constraint = LgqType.ROOT;
					value.type.time_align = true;
					// si element.getValue() == TextTier alors des points sinon
					// des intervalles
					if (verbose) System.out.println("TIER: " + element.getKey() + " : ROOT");
				}
				ht.tiersInfo.put(element.getKey(), value);
			}
			/*
			 * construire les informations sur les enfants (== dependantsNames)
			 * dans ht.tiersInfo
			 */
			TierInfo.buildDependantsNames(ht.tiersInfo);
			/*
			 * for(Map.Entry<String , TierInfo> entry :
			 * ht.tiersInfo.entrySet()){ System.out.println(entry.getKey() + " "
			 * + entry.getValue().toString()); }
			 */

			/*
			 * construire timeline not necessary will be done later when
			 * printing the file
			 * 
			 * int praatid = 0; //unité timeline ht.metaInf.time_units = "s";
			 * for (Map.Entry<String, ArrayList<Annot>> element :
			 * ptg.annotationMap.entrySet()) { //
			 * System.out.println(element.getKey() + " :- "); ArrayList<Annot> l
			 * = element.getValue(); for (int j=0; j<l.size(); j++) {
			 * //System.out.println("    " + j + " :- " + l.get(j)); Annot a =
			 * l.get(j); praatid++; String id = "b" + praatid;
			 * ht.timeline.put(id, a.start); ht.times.add(a.start); a.start =
			 * "#" + id; praatid ++; id = "e" + praatid; ht.timeline.put(id,
			 * a.end); ht.times.add(a.end); a.end = "#" + id; } }
			 */

			// displayAnnotations(ptg);

			for (Entry<String, TierInfo> tierInfo : ht.tiersInfo.entrySet()) {
				Participant p = new Participant();
				p.id = tierInfo.getKey();
				ht.metaInf.participants.add(p);
			}
			ht.partionRepresentationToHierachic(annotationMap, optionsTEI);
			HT_ToTei hiertransToTei = new HT_ToTei(ht, optionsTEI);
			// System.out.println(outputfile);
			Utils.setTranscriptionDesc(hiertransToTei.docTEI, "praat", "1.0", optionsTEI.praatParamsToString());
			Utils.setDocumentName(hiertransToTei.docTEI, Utils.lastname(outputfile));
			Utils.createFile(outputfile, hiertransToTei.docTEI);

		} catch (IOException ioe) {
			ioe.printStackTrace();
			System.err.println("Interrompu!"); // ioe.toString());
			return false;
		}
		return true;
	}

	public static void main(String[] args) throws Exception {
		TierParams.printVersionMessage();
		String usageString = "Description: PraatToTei convertit un fichier Praat vers un fichier TEI%n";
		PraatToTei ptt = new PraatToTei();
		ptt.mainCommand(args, EXT, Utils.EXT, usageString, 5);
	}

	@Override
	public void mainProcess(String input, String output, TierParams options) {
		verbose = options.verbose;
		convertFromPraatToTei(input, output, options);
//		System.out.println("New file TEI created: " + output);
	}
}