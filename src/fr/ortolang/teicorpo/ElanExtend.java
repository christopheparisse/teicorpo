package fr.ortolang.teicorpo;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;

public class ElanExtend extends GenericMain {

    /** Encodage des fichiers de sortie et d'entrée. */
    static final String outputEncoding = "UTF-8";
    /** Extension Elan **/
    public static String EXT = ".eaf";

    ElanToHT elanToHT;
    private Node timeOrder;
    private int lastUsedAnnotation = 0;

    public void transform(String inputFile, String outputName, TierParams options) throws IOException {
        elanToHT = new ElanToHT(new File(inputFile), true);
        // TeiDocument.setTranscriptionDesc(ht.docTEI, "elan", "0.1", "no information on format");
        // TeiDocument.setDocumentName(ht.docTEI, options.outputTEIName != null ? options.outputTEIName : Utils.lastname(outputName), options);
        // now change the docEAF
        if (options.raw)
            System.out.println("Process all tiers including dependent tiers");
        else
            System.out.println("Process only main tiers");
        initLastAnnotAndTemplate();
        extendEAFAlignedAnnotation(elanToHT, options);
        if (options.raw == true)
            extendEAFDependentTiers(elanToHT, options);
        Utils.createFile(elanToHT.docEAF, outputName);
    }

    // now change the docEAF
    private void extendEAFAlignedAnnotation(ElanToHT elanToHT, TierParams options) {
        // find all the tier that are time aligned
        NodeList tiers = this.elanToHT.docEAF.getElementsByTagName("TIER");
        for (int itiers = 0; itiers < tiers.getLength(); itiers++) {
            Element processedTier = (Element) tiers.item(itiers);
            String name = processedTier.getAttribute("TIER_ID");
            // is the tier time aligned
            TierInfo tierInfo = this.elanToHT.ht.tiersInfo.get(name);
            if (!tierInfo.parent.isEmpty()) {
                if (options.verbose) System.out.printf("Tier with a parent: %s => %s%n", name, tierInfo.parent);
            } else {
                if (options.verbose) System.out.printf("MAIN Tier: %s%n", name);
            }
            // System.out.printf("nb tiers %d%n", tiers.getLength());
            if (!options.raw && !tierInfo.parent.isEmpty()) continue;
            if (tierInfo.linguistType.time_align) {
                // Yes
                NodeList timeAnnots = processedTier.getElementsByTagName("ALIGNABLE_ANNOTATION");
                if (timeAnnots.getLength() < 1) continue;
                Element firstAnnot = (Element) timeAnnots.item(0);
                String firstAnnotRef1 = firstAnnot.getAttribute("TIME_SLOT_REF1");
                int iTimeAnnots = 0; // counter outside because we have to increase it because of the side effect of inserting nodes
                if (!firstAnnotRef1.equals("0")) {
                    // creates a first element
                    String firstAnnotRef1Time = elanToHT.ht.timeline.get(firstAnnotRef1);
                    // System.out.printf("first %s %s %s %s%n", "", "0", firstAnnotRef1, firstAnnotRef1Time);
                    lastUsedAnnotation = createNewAnnotation(elanToHT, lastUsedAnnotation, null, firstAnnot, "0", firstAnnotRef1Time);
                    iTimeAnnots++;
                    // System.out.printf("after initial insertion tiers %d at %d%n", timeAnnots.getLength(), iTimeAnnots);
                }
                for (; iTimeAnnots < timeAnnots.getLength() - 1; iTimeAnnots++) {
                    Element annotEl1 = (Element) timeAnnots.item(iTimeAnnots);
                    Element annotEl2 = (Element) timeAnnots.item(iTimeAnnots+1);
                    String el1Ref2 = annotEl1.getAttribute("TIME_SLOT_REF2");
                    String el2Ref1 = annotEl2.getAttribute("TIME_SLOT_REF1");
                    String el1Ref2Time = elanToHT.ht.timeline.get(el1Ref2);
                    String el2Ref1Time = elanToHT.ht.timeline.get(el2Ref1);
                    if (Integer.parseInt(el1Ref2Time) > Integer.parseInt(el2Ref1Time)) {
//                    if (el1Ref2Time.compareTo(el2Ref1Time) > 0) {
                        System.err.printf("Inversion time error on %s (%s) %s[%s] %s (%s) %s[%s]%n", el1Ref2, el1Ref2Time, annotEl1.getAttribute("ANNOTATION_ID"), annotEl1.getTextContent().trim(),
                                el2Ref1, el2Ref1Time, annotEl2.getAttribute("ANNOTATION_ID"), annotEl2.getTextContent().trim());
                        continue;
                    }
                    // System.out.printf("el %s %s %s %s%n", el1Ref2, el1Ref2Time, el2Ref1, el2Ref1Time);
                    if (!el1Ref2Time.equals(el2Ref1Time)) {
                        lastUsedAnnotation = createNewAnnotation(elanToHT, lastUsedAnnotation, annotEl1, annotEl2, el1Ref2Time, el2Ref1Time);
                        iTimeAnnots++;
                        // System.out.printf("after insertion nb tiers %d at %d%n", timeAnnots.getLength(), iTimeAnnots);
                    }
                }
            }
        }
    }
    // now change the docEAF
    private void extendEAFDependentTiers(ElanToHT elanToHT, TierParams options) {
        // find all the tier that are time aligned
        NodeList tiers = this.elanToHT.docEAF.getElementsByTagName("TIER");
        for (int itiers = 0; itiers < tiers.getLength(); itiers++) {
            Element processedTier = (Element) tiers.item(itiers);
            String name = processedTier.getAttribute("TIER_ID");
            // is the tier time aligned
            TierInfo tierInfo = this.elanToHT.ht.tiersInfo.get(name);
            if (tierInfo.linguistType.time_align && !tierInfo.parent.isEmpty()) {
                // if a tier is a descendant you have to divide empty fields that overlap some limits in the parent
                // get the same list for the parent
                Element ptier = null;
                for (int p = 0; p < tiers.getLength(); p++) {
                    Element ttier = (Element) tiers.item(p);
                    String pname = ttier.getAttribute("TIER_ID");
                    if (tierInfo.parent.equals(pname)) {
                        ptier = ttier;
                        break;
                    }
                }
                if (ptier == null) {
                    // not expected
                    System.err.printf("Cannot find the parent of %s (should be %s)%n", name, tierInfo.parent);
                    continue;
                }
                // do the work of adding the limits in the parent
                // find the empty in the new annotations
                // get the new set of annotations updated from the procedure above
                NodeList timeAnnots = processedTier.getElementsByTagName("ALIGNABLE_ANNOTATION");
                NodeList parentAnnots = ptier.getElementsByTagName("ALIGNABLE_ANNOTATION");
                for (int a = 0; a < timeAnnots.getLength(); a++) {
                    Element annotEl = (Element) timeAnnots.item(a);
                    if (annotEl.getTextContent().equals("<empty>")) {
                        // now see if this annotation has to be split
                        String anRef1 = annotEl.getAttribute("TIME_SLOT_REF1");
                        String anRef2 = annotEl.getAttribute("TIME_SLOT_REF2");
                        String anRef1Time = elanToHT.ht.timeline.get(anRef1);
                        String anRef2Time = elanToHT.ht.timeline.get(anRef2);
                        int iAnRef1Time = Integer.parseInt(anRef1Time);
                        int iAnRef2Time = Integer.parseInt(anRef2Time);
                        for (int p = 0; p < parentAnnots.getLength(); p++) {
                            String pRef1 = ((Element)(parentAnnots.item(p))).getAttribute("TIME_SLOT_REF1");
                            String pRef1Time = elanToHT.ht.timeline.get(pRef1);
                            int ipRef1Time = Integer.parseInt(pRef1Time);
                            String intermRefTime = anRef1Time;
                            Element intermAnnot = annotEl;
                            if (ipRef1Time > iAnRef1Time && ipRef1Time < iAnRef2Time) {
                                // cut at ipRef1Time
                                lastUsedAnnotation = createNewAnnotation(elanToHT, lastUsedAnnotation, annotEl, null, pRef1Time, anRef2Time);
                                intermRefTime = pRef1Time;
                                a++;
                                intermAnnot = (Element)timeAnnots.item(a); // new element at 'a'
                            }
                            String pRef2 = ((Element)(parentAnnots.item(p))).getAttribute("TIME_SLOT_REF2");
                            String pRef2Time = elanToHT.ht.timeline.get(pRef2);
                            int ipRef2Time = Integer.parseInt(pRef2Time);
                            if (ipRef2Time > Integer.parseInt(intermRefTime) && ipRef2Time < iAnRef2Time) {
                                // cut at ipRef2Time
                                lastUsedAnnotation = createNewAnnotation(elanToHT, lastUsedAnnotation, intermAnnot, null, pRef2Time, anRef2Time);
                                a++;
                            }
                        }
                    }
                }
                // add all the parent annotation that are after the last annotation
                int m = timeAnnots.getLength()-1; // where to add
                if (m<1) continue;
                Element lastElement = ((Element) timeAnnots.item(m));
                String anRefLast = lastElement.getAttribute("TIME_SLOT_REF2");
                String anRefLastTime = elanToHT.ht.timeline.get(anRefLast);
                int iAnRefLastTime = Integer.parseInt(anRefLastTime);
                // these two will increase with the new annotation (as well as 'm').
                String intermRefTime = anRefLastTime;
                Element intermAnnot = lastElement;
                for (int p = 0; p < parentAnnots.getLength(); p++) {
                    String pRef1 = ((Element)(parentAnnots.item(p))).getAttribute("TIME_SLOT_REF1");
                    String pRef1Time = elanToHT.ht.timeline.get(pRef1);
                    int ipRef1Time = Integer.parseInt(pRef1Time);
                    if (ipRef1Time > iAnRefLastTime) {
                        // cut at ipRef1Time
                        lastUsedAnnotation = createNewAnnotation(elanToHT, lastUsedAnnotation, intermAnnot, null, intermRefTime, pRef1Time);
                        intermRefTime = pRef1Time;
                        m++;
                        intermAnnot = (Element)timeAnnots.item(m); // new element at 'm'
                    }
                    String pRef2 = ((Element)(parentAnnots.item(p))).getAttribute("TIME_SLOT_REF2");
                    String pRef2Time = elanToHT.ht.timeline.get(pRef2);
                    int ipRef2Time = Integer.parseInt(pRef2Time);
                    if (ipRef2Time > iAnRefLastTime) {
                        // cut at ipRef2Time
                        lastUsedAnnotation = createNewAnnotation(elanToHT, lastUsedAnnotation, intermAnnot, null, intermRefTime, pRef2Time);
                        intermRefTime = pRef1Time;
                        m++;
                        intermAnnot = (Element) timeAnnots.item(m); // new element at 'm'
                    }
                }
            }
        }
    }

    private void initLastAnnotAndTemplate() {
        // find the last annotation
        NodeList property = this.elanToHT.docEAF.getElementsByTagName("PROPERTY");
        for (int i = 0; i < property.getLength(); i++) {
            Element nodeValue = (Element)property.item(i);
            String nameValue = nodeValue.getAttribute("NAME");
            if (nameValue.equals("lastUsedAnnotationId")) {
                String lastUsedAnnotationId = nodeValue.getTextContent();
                if (lastUsedAnnotationId != null) lastUsedAnnotation = Integer.parseInt(lastUsedAnnotationId);
            }
        }
        // initialize head of timeline
        NodeList tm = this.elanToHT.docEAF.getElementsByTagName("TIME_ORDER");
        timeOrder = tm.item(0);
    }

    private int createNewAnnotation(ElanToHT elanToHT, int lastUsedAnnotation, Element annotEl1, Element annotEl2, String el1Ref2Time, String el2Ref1Time) {
        // add an element in the free space between el1Ref2 and el2Ref1
        // create two new elements to the timeline
        String newId1 = createNewTimeSlot(el1Ref2Time);
        String newId2 = createNewTimeSlot(el2Ref1Time);
        // create a new "empty" element
        Element annot = elanToHT.docEAF.createElement("ANNOTATION");
        Element alAnnot = elanToHT.docEAF.createElement("ALIGNABLE_ANNOTATION");
        Element alValue = elanToHT.docEAF.createElement("ANNOTATION_VALUE");
        // insert it
        lastUsedAnnotation++;
        alAnnot.setAttribute("ANNOTATION_ID", "a" + lastUsedAnnotation);
        alAnnot.setAttribute("TIME_SLOT_REF1", newId1);
        alAnnot.setAttribute("TIME_SLOT_REF2", newId2);
        // System.out.printf("NEW %s %s%n", newId1, newId2);
        // String cveref = (annotEl2 != null) ? annotEl2.getAttribute("CVE_REF") : annotEl1.getAttribute("CVE_REF");
        // if (!cveref.isEmpty()) alAnnot.setAttribute("CVE_REF", cveref);
//        nthEmpty++;
//        alValue.setTextContent("--" + nthEmpty + "");
//        if (nthEmpty == 83) {
//            System.out.printf("XX83XX %s %s %s %s %d%n", newId1, newId2, el1Ref2Time, el2Ref1Time, el1Ref2Time.compareTo(el2Ref1Time));
//        }
        alValue.setTextContent("--");
        annot.appendChild(alAnnot);
        alAnnot.appendChild(alValue);
        if (annotEl2 != null) {
            // System.out.println("ajout AnnotEL2");
            annotEl2.getParentNode().getParentNode().insertBefore(annot, annotEl2.getParentNode());
        } else {
            // System.out.println("ajout AnnotEL1");
            annotEl1.getParentNode().appendChild(annot);
        }
        return lastUsedAnnotation;
    }

    private String createNewTimeSlot(String refTime) {
        elanToHT.initialTimelineLength++;
        String newId = "ts" + elanToHT.initialTimelineLength;
        Element newTimeSlot = elanToHT.docEAF.createElement("TIME_SLOT");
        newTimeSlot.setAttribute("TIME_SLOT_ID", newId);
        newTimeSlot.setAttribute("TIME_VALUE",refTime);
        timeOrder.appendChild(newTimeSlot);
        // add to the stored timeline
        elanToHT.ht.timeline.put(newId, refTime);
        elanToHT.ht.times.add(refTime);
        return newId;
    }

    /**
     * Main program: add empty slots to an ELAN file.
     *
     * @param args
     *            program parameter list.
     * @throws IOException
     */
    public static void main(String[] args) throws Exception {
        TierParams.printVersionMessage(false);
        ElanExtend tr = new ElanExtend();
        tr.mainCommand(args, EXT, Utils.EXT, "Description: ElanExtend fill the empty parts of an ELAN file with -- elements.%nUse option -raw if you also to process the dependent tiers.", 0);
    }

    @Override
    public void mainProcess(String input, String output, TierParams options) {
        try {
            transform(input, output, options);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.exit(1);
        }
    }
}