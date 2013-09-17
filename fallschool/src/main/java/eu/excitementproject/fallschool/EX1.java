package eu.excitementproject.fallschool;

import java.io.File;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.uimafit.util.JCasUtil;

//import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;

import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.eop.lap.PlatformCASProber;
import eu.excitementproject.eop.lap.dkpro.MaltParserEN;
import eu.excitementproject.eop.lap.dkpro.TreeTaggerEN;

/**
 * This heavily commented code introduces the Linguistic Analysis Pipeline 
 * (LAP) of EXCITEMENT open platform. Check EX1 exercise sheet first, and proceed 
 * with this example code. 
 * 
 * @author Gil
 */
public class EX1 {

	public static void main(String[] args) {
		
    	// init logs
    	BasicConfigurator.resetConfiguration();
    	BasicConfigurator.configure();
    	Logger.getRootLogger().setLevel(Level.WARN);  

    	// remove comments of the following methods one by one, and 
    	// run it, and read it. 
       	// ex1_1();   
    	// ex1_2();  
    	// ex1_3();   
    	// ex1_4(); 
	}
	
	/**
	 *  This code introduces LAPAccess.generateSingleTHPairCAS 
	 *  [URL] 
	 */
	public static void ex1_1() {
		// Each and every LAP in EXCITEMENT Open Platform (EOP) 
		// implements the interface LAPAccess. 
		// Here, lets use the TreeTagger based LAP. 
		LAPAccess aLap = null; 
		try {
			aLap = new TreeTaggerEN(); 
		} catch (LAPException e)
		{
			System.out.println("Unable to initiated TreeTagger LAP: " + e.getMessage()); 			
		}
		
		// LAPs (all implement LAPAccess) basically support 3 types of common methods. 
	
		//// First interface: generateSingleTHPair 
		// LAPs can generate a specific data format that can be 
		// accepted by EOP Entailment Decision Algorithms (EDAs). This is supported 
		// by the LAPAccess.generateSingleTHPairCAS.
		
		JCas aJCas = null; 
		try { 
			aJCas = aLap.generateSingleTHPairCAS("This is the Text part.", "The Hypothesis comes here."); 
		} catch (LAPException e)
		{
			System.out.println("Unable to run TreeTagger LAP: " + e.getMessage()); 						
		}
		
		// All output of LAPs are stored in a data type that is called CAS. 
		// This data type is borrowed from Apache UIMA: for the moment, just think
		// of it as a data type that can hold any annotation data. One way to see
		// it is "smarter" version of CONLL format; just much more flexible, and 
		// unlike CONLL, this is "im-memory" format. 
		
		// Take a look at a CAS figure; to see how it stores data of a T-H pair. 
		// Figure URL: http://hltfbk.github.io/Excitement-Open-Platform/specification/spec-1.1.3.html#CAS_example

		// Here, let's briefly check what is stored in this actual aJCas. 
		// Say, how it is annotated? 
		try {
			// This command checks CAS data, and checks if it is compatible for the EDAs
			PlatformCASProber.probeCas(aJCas, System.out); 
			// the following command dumps all annotations to text file. 
			CASAccessUtilities.dumpJCasToTextFile(aJCas, "test_dump1.txt"); 
			System.out.println("test_dump1.txt file dumped."); 
		} catch (LAPException e)
		{
			System.out.println("Failed to dump CAS data: " + e.getMessage()); 						
		}
		// TODO Task1_1 check out this file, in Excitement-Open-Platform/fallschool/test_dump1.txt 	
		
		System.out.println("method ex1_1() finished"); 
	}
	
	/**
	 * This code introduces LAPAccess.processRawInputFormat
	 */
	public static void ex1_2() 
	{
		// LAPs also support file based mass pre-processing. 
		// As an example let's process RTE3 English data with TreeTagger LAP.

		// Initialize an LAP, here it's TreeTagger  
		LAPAccess ttLap = null; 
		try {
			ttLap = new TreeTaggerEN(); 
		} catch (LAPException e)
		{
			System.out.println("Unable to initiated TreeTagger LAP: " + e.getMessage()); 			
		}
		
		// Prepare input file, and output directory. 
		File f = new File("./src/main/resources/RTE3-dataset/English_dev.xml"); 
		File outputDir = new File("./target/"); 
		
		// Call LAP method for file processing. 
		// This takes some time. RTE data has 800 cases in it. 
		// Each case, will be first annotated as a CAS, and then it will be 
		// serialized into one XMI file. 
		try {
			ttLap.processRawInputFormat(f, outputDir); 
		} catch (LAPException e)
		{
			System.out.println("Failed to process EOP RTE data format: " + e.getMessage()); 						
		}
	
		// TODO Task1_2: now all RTE3 training data is annotated and stored in 
		// output dir ( Excitement-Open-Platform/fallschool/target/ ) 
		// a. Check the files are really there. 
		// b. Open up one XMI file to get impression that how the CAS content is 
		//    stored into XML-based file. 
		System.out.println("method ex1_2() finished"); 
	}
	
	/**
	 *  This code introduces LAPAccess.addAnnotationOn
	 */
	public static void ex1_3() 
	{
		// Previous two methods generates "Pair data stored in CAS" (or XMI file) 
		// , including Entailment Pair annotation, and so on 
		
		// But what if, if you simply wants to annotate a sentence, or something 
		// like that. E.g. no Entailment pair, just a single text document 
		// annotation. 
		
		// All LAP has addAnnotationOn() method is there to give you this 
		// capability. 
		// The following code shows you how you can do that. 
		
		// first, prepare Malt parser based LAP 
		LAPAccess malt = null; 
		try {
			malt = new MaltParserEN(); 
		} catch (LAPException e)
		{
			System.out.println("Unable to initiated MaltParser (with TreeTagger) LAP: " + e.getMessage()); 			
		}
		
		// and let's annotate something. 
		try {
			// get one empty CAS.  
			JCas aJCas = CASAccessUtilities.createNewJCas(); 
			
			// Before asking LAP to process, you have to set at least two things. 
			// One is language, and the other is document itself. 
			aJCas.setDocumentLanguage("EN"); // ISO 639-1 language code.  
			String doc = "This is a document. You can pass an arbitary document to CAS and let LAP work on it."; 
			aJCas.setDocumentText(doc); 
			malt.addAnnotationOn(aJCas); 
		} catch (LAPException e)
		{
			System.out.println("Failed to process EOP RTE data format: " + e.getMessage()); 						
		}
		
		// Malt parser annotates the given aJCas document text. 
		// But here, there is no Pair, no TEXTVIEW, or HYPOTHESISVIEW.
		
		// TODO Task1_3 Dump this result of malt parser result to a textfile.
		//      Check how the CAS stores dependency parser result. 
		// (use CASAccessUtilities.dumpJCasToTextFile()) 
	}
	
	/**
	 * This code introduces how you can iterate over added annotations 
	 * within a JCas. 
	 */
	public static void ex1_4() 
	{
		// So far, so good. But how can we access annotation results 
		// stored in a JCas? You can iterate them, like the followings.  

		// First, prepare LAP and process a T-H pair. 
		LAPAccess malt = null; 
		JCas aJCas = null; 
		try {
			malt = new MaltParserEN(); 
			aJCas = malt.generateSingleTHPairCAS("We thought that there were many cats in this garden.", "But there was only one cat, among all the gardens in the city."); 
		} catch (LAPException e)
		{
			System.out.println("Unable to initiated MaltParser (with TreeTagger) LAP: " + e.getMessage()); 			
		}
		
		// aJCas has now T-H pair. 
		// Here, let's iterate over the Tokens on Text side. 
		try {
			JCas textView = aJCas.getView("TextView");
			System.out.println("Listing tokens of TextView."); 
			for (Token tok : JCasUtil.select(textView, Token.class))
			{
				String s = tok.getCoveredText(); // .getCoveredText() let you check the text on the document that this annotation is attached to.  
				int begin = tok.getBegin(); 
				int end = tok.getEnd(); 
				System.out.println(begin + "-" + end + " " + s); 		
			}
		} catch (CASException e)
		{
			System.out.println("Exception while accesing TextView of CAS: " + e.getMessage()); 						
		}

		// And here, let's iterate over the dependency edges on the Hypothesis side. 
		try { 
			JCas hypothesisView = aJCas.getView("HypothesisView"); 
			for (Dependency dep : JCasUtil.select(hypothesisView, Dependency.class)) {

				// One Dependency annotation holds the information for a dependency edge.
				// Basically, 3 things;
				// It holds "Governor (points to a Token)", "Dependent (also to a Token)",
				// and relationship between them (as a string)
				Token dependent = dep.getDependent();
				Token governor = dep.getGovernor();
				String dTypeStr = dep.getDependencyType();

				// lets print them with full token information (lemma, pos, loc)
				// info for the dependent ...
				int dBegin = dependent.getBegin();
				int dEnd = dependent.getEnd();
				String dTokenStr = dependent.getCoveredText();
				String dLemmaStr = dependent.getLemma().getValue();
				String dPosStr = dependent.getPos().getPosValue();

				// info for the governor ...
				int gBegin = governor.getBegin();
				int gEnd = governor.getEnd();
				String gTokenStr = governor.getCoveredText();
				String gLemmaStr = governor.getLemma().getValue();
				String gPosStr = governor.getPos().getPosValue();

				// and finally print the edge with full info
				System.out.println(dBegin + "-" + dEnd + " " + dTokenStr + "/" + dLemmaStr + "/" + dPosStr);
				System.out.println("\t ---"+ dTypeStr + " --> ");
				System.out.println("\t " + gBegin + "-" + gEnd + " " + gTokenStr + "/" + gLemmaStr + "/" + gPosStr);
				}
		} catch (CASException e)
		{
			System.out.println("Exception while accesing HypothesisView of CAS: " + e.getMessage()); 						
		}		

		// TODO [Optional Task] Task 1_4  
		// ( This is an optional task: you can skip without affecting later exercise) 
		// 
		// Try to print out the above T-H pair as two bags of lemmas. 
		// 
		// You can iterate over Lemma type (you will need to import Lemma class), 
		// or, you can iterate over Tokens, and use Token.getLemma() to fetch Lemmas. 	
		// Then, you can access Lemma value, by calling Lemma.getValue(); 
		
		System.out.println("ex1_4() method finished");  
		
	}
}
