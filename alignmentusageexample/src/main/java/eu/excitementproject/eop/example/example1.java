package eu.excitementproject.eop.example;

import org.apache.uima.jcas.JCas;

import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.dkpro.MaltParserEN;

public class example1 {

	/**
	 * A simple example that shows some usage for the alignment.Target and alignment.Link. 
	 * 
	 * TODO: Some more comments? what's here, what will come later... ? 
	 *  
	 * The example assumes that you know the very basic of CAS. 
	 * In case if you are not familiar with CAS, it would be better to first 
	 * check the CAS usage example.  
	 *
	 * https://github.com/gilnoh/cas_access_example.git
	 * (example-1 in the above git will introduce you to the basic usage of CAS.) 
	 * 
	 */
	public static void main(String[] args) {
		
		// [ We have two types, for the alignment. One is alignment.Target that holds
		// various "annotation data". The other is alignment.Link, which actually connects
		// two targets; one target in TextView, the other target in HypothesisView. 
		// TODO put v3 slides link here. 
		
		// Let's see some usage for the types.

		// Before begin, let's have one CAS with some linguistic annotations. 
		
		LAPAccess lap = null; 
		JCas JCas1 = null; 
		try {
			lap = new MaltParserEN(); 
			JCas1 = lap.generateSingleTHPairCAS("I have a dog.", "I own a pet."); 
		}
		catch (Exception e)
		{
			System.out.println("Exception -- " + e.getMessage()); 
			System.exit(1);
		}
		
		// Okay, now JCas1 has TextView:"I have a dog.", HypothesisView:"I own a pet." 
		// And each view has annotations of "Token", "Lemma", and "Dependency". 

		
		// - Q1: How can I put my annotations in Target? 

		
		// - Q2: Oh. I see that. But token seems... very simple. Can I also 
		//   put something more complex? say, depenency-triple, or partial parse tree
		//   in a Target instance? 
		
		
		// - Q3: Okay. now I see we can put any Annotation data added by LAP
		//       in a Target.  
		//       Can you show me how I can actually mark a alignment.Link? 
		
		// - Q4: Oja. How about the opposite? How can I get Links back from a 
		//   CAS? "Give me all Links in a CAS!" 
		
		// - Q5: Say, I have a Link, with its two targets --- how can I get back 
		//   the annotations that are linked by the Link? (in other words, how can I 
		//   get back my annotations within a (or two) Target(s) 
		
		// - Q6: How can I access who have annotated this? 

		// - Q7: I've heard that each Link has "unique" full-ID. How can I access them? 

	}

}
