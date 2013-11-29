package eu.excitementproject.eop.lap.hei.phraseextract;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Logger;

import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.eop.lap.PlatformCASProber;
import eu.excitementproject.eop.lap.dkpro.TreeTaggerEN;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.tcas.Annotation;

public class NounNGramExtract {

	/**
	 * A small test class that uses LAP to extract n-grams that ends with nouns. 
	 * 
	 */
	static Logger logger;
	
	public static void main(String[] args) {
		
		logger = Logger.getLogger(NounNGramExtract.class.getName());  
	
		// first, generate all XMI files via an LAP. 
		//testLAP_EN(); // need to be called just once. The files gonna stay in 
		//"./target/EN/dev/" here and 
		//"./target/EN/test/" here. 

		try
		{
			
//			File test = new File ("./target/EN/test/99.xmi"); 
//			JCas testCAS = PlatformCASProber.probeXmi(test, null);
//			JCas tv = testCAS.getView("TextView"); 
//			JCas hv = testCAS.getView("HypothesisView"); 
//			System.out.println("dev-" + test.getName() + "-Text part"); 
//			walkOverNounGrammarFST(tv);  
//			System.out.println("dev-" + test.getName() + "-Hypothesis part"); 
//			walkOverNounGrammarFST(hv);  
//			System.exit(1); 
			
			// Iterate over all XMI file, 
			// Extract uni, bi, tri and quadra-grams that ends with noun. 

			File dir = new File ("./target/EN/dev/"); 
			for (File f : dir.listFiles())
			{
				JCas aJCas = PlatformCASProber.probeXmi(f, null);
				JCas textview = aJCas.getView("TextView"); 
				JCas hypoview = aJCas.getView("HypothesisView"); 
				System.out.println("dev-" + f.getName() + "-Text part"); 
				//walkOverNounEndingNGrams(textview);  
				walkOverNounGrammarFST(textview); 
				System.out.println("dev-" + f.getName() + "-Hypothesis part"); 
				walkOverNounGrammarFST(hypoview);  
			}
			dir = new File ("./target/EN/test/"); 
			for (File f : dir.listFiles())
			{
				//File f = new File ("./target/EN/dev/799.xmi"); 
				JCas aJCas = PlatformCASProber.probeXmi(f, null);
				JCas textview = aJCas.getView("TextView"); 
				JCas hypoview = aJCas.getView("HypothesisView"); 
				System.out.println("test-" + f.getName() + "-Text part"); 
				walkOverNounGrammarFST(textview);  
				System.out.println("test-" + f.getName() + "-Hypothesis part"); 
				walkOverNounGrammarFST(hypoview);  
			}


		}
		catch (Exception e)
		{
			System.err.println(e.getMessage()); 
			System.exit(1); 
		}
				
	}
	
	public static void walkOverNounGrammarFST(JCas view) throws LAPException
	{
		
		// iterate over the cas! record 
		AnnotationIndex<Annotation> tokenIndex = view.getAnnotationIndex(Token.type); 
		Iterator<Annotation> tokenIter = tokenIndex.iterator(); 
		System.out.println("Among " + tokenIndex.size() + " words:"); 

		Vector<Token> tokenHistory = new Vector<Token>(); 
		Vector<String> posHistory = new Vector<String>(); 

		
		while(tokenIter.hasNext())
		{

			Token t = (Token) tokenIter.next(); 
			//l = t.getLemma(); 
			POS p = t.getPos(); 
			String posStr = p.getType().getName().substring(52); 

			tokenHistory.add(t);
			posHistory.add(posStr); 
			
			// start state machine; if meet a noun. 
			// traverse "prev" token, by its POS. 
			if (posStr.equalsIgnoreCase("NN") || posStr.equalsIgnoreCase("N") || posStr.equalsIgnoreCase("NP"))
			{
				String state = "noun_state"; 
				int current = posHistory.size() - 1; 
				int left = posHistory.size() -1; 
				
				while (!state.isEmpty())
				{
					if (state.equalsIgnoreCase("noun_state"))
					{
						// print 
						for (int i=left; i <= current; i++)
						{
							System.out.print(tokenHistory.get(i).getCoveredText()); 
							System.out.print("/"); 
							System.out.print(posHistory.get(i)); 
							System.out.print("\t"); 
						}
						System.out.println(); 
						
						// set next state
						if (left == 0) {	
							state = ""; 
							break; 
						}
						
						left--; 
						switch(posHistory.get(left)) {
							case "NP": 
							case "NN": 
							case "N": 
								state="noun_state";  
								break; 
							case "ADJ": 
								state="adj_state"; 
								break; 
							case "ART": 
								state="det_state"; 
								break; 
							case "PP":
								state="prep_state"; 
								break; 
							default: 
								state=""; 
								break; 
						}
					}
					else if (state.equals("adj_state"))
					{
						// print 
						for (int i=left; i <= current; i++)
						{
							System.out.print(tokenHistory.get(i).getCoveredText()); 
							System.out.print("/"); 
							System.out.print(posHistory.get(i)); 
							System.out.print("\t"); 
						}
						System.out.println(); 

						// set next state
						if (left == 0) {	
							state = ""; 
							break; 
						}
						
						left--; 
						switch(posHistory.get(left)) {
							// this part was missing ---> 
							case "ADJ": 
								state="adj_state"; 
								break; 
							// <---- 
							case "ART": 
								state="det_state"; 
								break; 
							case "PP":
								state="prep_state"; 
								break; 
							default: 
								state=""; 
								break; 
						}
					}
					else if(state.equalsIgnoreCase("det_state"))
					{
						// set next state
						if (left == 0) {	
							state = ""; 
							break; 
						}
						
						left--; 
						switch(posHistory.get(left)) {
							case "PP":
								state="prep_state"; 
								break; 
							default: 
								state=""; 
								break; 
						}
					}
					else if (state.equalsIgnoreCase("prep_state"))
					{
						// set next state
						if (left == 0) {	
							state = ""; 
							break; 
						}
						
						left--; 
						switch(posHistory.get(left)) {
							case "NP": 
							case "NN": 
							case "N": 
								state="noun_state";  
							break; 
							
							default: 
								state=""; 
								break; 
						}
					}
					else
					{
						if (!state.isEmpty())
						{
							throw new LAPException("Internal integrity failure: No such state in FST: " + state); 
						}
					}
				} // while for outter if-else  
			} // if 
		} // CAS Token reading while. 
		
		tokenHistory.clear(); 
		posHistory.clear(); 
	}
	
	
	
	
	public static void walkOverNounEndingNGrams(JCas view) throws LAPException 
	{
		// iterate over the cas! record 
		AnnotationIndex<Annotation> tokenIndex = view.getAnnotationIndex(Token.type); 
		Iterator<Annotation> tokenIter = tokenIndex.iterator(); 
		System.out.println("Among " + tokenIndex.size() + " words:"); 
		
		Lemma prev_1st = null; String prev_1st_pos = null; 
		Lemma prev_2nd = null; String prev_2nd_pos = null; 
		Lemma prev_3rd = null; String prev_3rd_pos = null; 

		Lemma l = null; 
		POS p = null; 
		String lpos = null; 
		while(tokenIter.hasNext())
		{
			// update prev list 
			prev_3rd = prev_2nd; prev_3rd_pos = prev_2nd_pos;  
			prev_2nd = prev_1st; prev_2nd_pos = prev_1st_pos; 
			prev_1st = l; prev_1st_pos = lpos; 

			Token t = (Token) tokenIter.next(); 
			l = t.getLemma(); 
			p = t.getPos(); 
			lpos = p.getType().getName().substring(52); 
			
			if (lpos.equalsIgnoreCase("NN") || lpos.equalsIgnoreCase("N") || lpos.equalsIgnoreCase("NP"))
			{
//				// 4 grams 
//				if (prev_3rd != null)
//				{
					if (prev_3rd != null)
					{
						System.out.print(prev_3rd.getCoveredText());
						System.out.print("/" + prev_3rd_pos); 
						System.out.print("\t"); 
					}
					if (prev_2nd != null)
					{
						System.out.print(prev_2nd.getCoveredText());
						System.out.print("/" + prev_2nd_pos); 
						System.out.print("\t"); 
					}
					if (prev_1st != null)
					{
						System.out.print(prev_1st.getCoveredText());
						System.out.print("/" + prev_1st_pos); 
						System.out.print("\t"); 
					}
				
					System.out.print(l.getCoveredText());
					System.out.print("/" + lpos); 
					System.out.print("\n"); 
//				}
//				if (prev_2nd != null)
//				{
//					// 3 grams 
//					if (prev_2nd != null)
//					{
//						System.out.print(prev_2nd.getCoveredText());
//						System.out.print("/" + prev_2nd_pos); 
//						System.out.print("\t"); 
//					}
//					if (prev_1st != null)
//					{
//						System.out.print(prev_1st.getCoveredText());
//						System.out.print("/" + prev_1st_pos); 
//						System.out.print("\t"); 
//					}
//				
//					System.out.print(l.getCoveredText());
//					System.out.print("/" + lpos); 
//					System.out.print("\n"); 
//				}
//				if (prev_1st != null)
//				{
//					// 2 grams 
//					if (prev_1st != null)
//					{
//						System.out.print(prev_1st.getCoveredText());
//						System.out.print("/" + prev_1st_pos); 
//						System.out.print("\t"); 
//					}
//				
//					System.out.print(l.getCoveredText());
//					System.out.print("/" + lpos); 
//					System.out.print("\n"); 
//				}
//				
//				// unigram 				
//				System.out.print(l.getCoveredText());
//				System.out.print("/" + lpos); 
//				System.out.print("\n"); 

			}
			
		}
	}
	
	
	public static void testLAP_EN() {
		File inputFile = null;
		File outputDir = null;
		
		// generate XMI files for the training data
		inputFile = new File("../core/src/main/resources/data-set/English_dev.xml");
		assertTrue(inputFile.exists());
		outputDir = new File("./target/EN/dev/");
		if (!outputDir.exists()) {
			outputDir.mkdirs();
		}
		assertTrue(outputDir.exists());

		LAPAccess lap = null;

		try {
			lap = new TreeTaggerEN();
			lap.processRawInputFormat(inputFile, outputDir);
		} catch (LAPException e) {
			logger.info(e.getMessage());
		}
		
		// generate XMI files for the testing data
		inputFile = new File("../core/src/main/resources/data-set/English_test.xml");
		assertTrue(inputFile.exists());
		outputDir = new File("./target/EN/test/");
		if (!outputDir.exists()) {
			outputDir.mkdirs();
		}
		assertTrue(outputDir.exists());
		
		try {
			lap = new TreeTaggerEN();
			lap.processRawInputFormat(inputFile, outputDir);
		} catch (LAPException e) {
			logger.info(e.getMessage());
		}
	}
}
