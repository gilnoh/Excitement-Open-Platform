package eu.excitementproject.fallschool;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;

import eu.excitement.type.alignment.AlignedText;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.eop.lap.dkpro.MaltParserDE;
import eu.excitementproject.eop.lap.dkpro.MaltParserEN;

/**
 * This class shows two skeleton codes for "CAS-in and CAS-out" example for 
 * those who works on Linguistic Analysis Pipeline "post-processing" type of works. 
 * (German lemma disambiguation, and English question to statement reformulation) 
 * 
 * @author Gil
 *
 */
public class CASinCASoutSkeleton {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

    	// init logs
    	BasicConfigurator.resetConfiguration();
    	BasicConfigurator.configure();
    	Logger.getRootLogger().setLevel(Level.WARN);  

    	// Prepare LAP
		LAPAccess aLap = null; 
		try {
			aLap = new MaltParserEN(); // change to MaltParserDE() for German. 
		} catch (LAPException e)
		{
			System.out.println("Unable to initiated MaltParser LAP: " + e.getMessage()); 			
		}

		JCas aJCas = null; 
		try { 
			aJCas = aLap.generateSingleTHPairCAS("Microsoft bought Nokia for 7 billion dollars.", "Which company bought Nokia?"); 
		} catch (LAPException e)
		{
			System.out.println("Unable to run MaltParser LAP: " + e.getMessage()); 						
			System.exit(1); 
		}
		
		// And there comes the skeleton codes. 

		//JCas result1 = skel1(aJCas); // This method shows how you can update "Lemma".  
		//JCas result2 = skel2(aJCas, aLap); // This method shows how you can update "the document text" of CAS.   
		
	}
	
	/**
	 * A simple example that shows how you can "post-process" an LAP output and update 
	 * its "Lemma" values. 
	 */
	public static JCas skel1(JCas casInput)
	{
		JCas aJCas = casInput; 		
		
		JCas textView = null; 
		JCas hypoView = null; 
		
		try {
			textView = aJCas.getView("TextView"); 
			hypoView = aJCas.getView("HypothesisView"); 
			
			// you can iterate over the lemmas 
			// listing lemmas of textView 
			for (Lemma lem : JCasUtil.select(textView, Lemma.class)) // First, on the Text side. 
			{
				String s = lem.getValue(); // .getValue() will let you check the value of Lemma as string.  
				int begin = lem.getBegin(); 
				int end = lem.getEnd(); 
				System.out.println(begin + "-" + end + " " + s); 		

				// let's say, if you find a specific lemma, (for example, "|" separated ones in TreeTagger) 
				// you can update it. 
				if (s.contains("buy")) // here, I used "buy", instead of "|". just for this example.  
				{
					// TODO Write and call the method that disambiguates this lemma. 
					// Say, 
					// String resolved = disambiguate(lem, set_of_context_lemmas); 					
					String resolved = "buy_disambiguated"; // here, let's assume somehow we get the result. 
					
					// and now update the Lemma object with "fixed lemma string value"
					lem.setValue(resolved); 
					// Now the "lemma" object within CAS, has updated value. 
					// changed from "buy" to "buy_disambiguated". 
				}
			}
			
			for (Lemma lem: JCasUtil.select(hypoView, Lemma.class)) 
			{
				// you should run the same thing over Hypothesis too. 
				// codes ... 
			}
			
		} catch (CASException e)
		{
			System.out.println("Failed to access CAS: " + e.getMessage()); 
			System.exit(1); 
		}
		
		return aJCas; 
		// This returned aJCas would now have updated Lemma. 
		// For example, originally, the JCAS had 
		//		0-9 Microsoft
		//		10-16 buy
		//		17-22 Nokia
		//		23-26 for
		//		27-28 7
		//		29-36 billion
		//		37-44 dollar
		//		44-45 .
		
		// But now it will have updated lemma on: 
		//      10-16 buy_disambiguated
		
		// So this shows how you can update a lemma in a JCAS. 
	}
	
	
	/**
	 * A simple example that shows how you can update "Document Text" of CAS.  
	 * Note that it gets one CAS and generates (resets) a new CAS and returns this new CAS. 
	 */
	public static JCas skel2(JCas casInput, LAPAccess lap)
	{
		JCas aJCas = casInput; 
		
		
		try {
			JCas textView = aJCas.getView("TextView"); 
			JCas hypoView = aJCas.getView("HypothesisView"); 

			// you can iterate over the lemmas and dependencies ... in Text part and Hypothesis part; 
			// as described in ex1_4();   
			
			// TODO Write "new code" 
			// With the lemmas and dependencies, let's assume that we have somehow found out 
			// how to convert Hypothesis into a statement format. 
			 
			// For example, somethingLike: 
			// String statement = convertToStatement(hypoView.getDocumentText()); 
			
			//  Say, we have found the proper "statement" form of Hypothesis is the following
			// from the example: 
			//                  012345678901234567890123
			String statement = "A company bought Nokia.";
			// also, the "important part" of the statement is (which) somehow determined 
			// to begin from 0 to 9 ("A company"). 
			int begin = 0; 
			int end = 9; 
			
			// Let's encode this knowledge in a *new* JCas. 
			
			String text = textView.getDocumentText(); 
			String new_hypothesis = statement; 
			
			// ask LAP to generate a *new* JCas with the updated statement-style hypothesis.  
			JCas resultJCas = lap.generateSingleTHPairCAS(text, new_hypothesis); 
			// before return it, let's "mark" the "important part"
			// we don't have a specific type for this, so we will simply reuse alignment annotation. 
			
			AlignedText a = new AlignedText(resultJCas.getView("HypothesisView")); 
			a.setBegin(begin); // note that we assumed that we get begin & end from the new code.  
			a.setEnd(end); 
			// and add this to the JCas
			a.addToIndexes(); 
			
			// Okay. Now we have a new JCas with "converted hypothesis", and it is 
			// annotated with the same LAP (passed through argument), 
			// and it also has one "AlignedText" annotation that points which part was 
			// important part in "converted statement". 
			
			// return newly generated JCas
			
			// You can dump them and check all is there. 
			try {
				CASAccessUtilities.dumpJCasToTextFile(resultJCas, "test_dump_new_JCas.txt"); 
				System.out.println("resulting JCas Dumpted to /fallschool/test_dump_new_JCas.txt"); 
			} catch (LAPException e)
			{
				System.out.println("Failed to dump CAS data: " + e.getMessage()); 						
			}

			return resultJCas; 
		
		} catch (LAPException|CASException e)
		{
			System.out.println("Failed to access CAS: " + e.getMessage()); 
			System.exit(1); 
			return null; 
		}

	}
}