package eu.excitementproject.eop.example;

import java.util.Iterator;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;

import static eu.excitementproject.eop.lap.implbase.LAP_ImplBase.*; // TEXTVIEW HYPOTHESISVIEW
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import eu.excitement.type.alignment.Link;
import eu.excitement.type.alignment.Link.Direction;
import eu.excitement.type.alignment.Target;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.dkpro.MaltParserEN;


//import static org.apache.uima.fit.util.JCasUtil; 
import org.uimafit.util.JCasUtil; 

public class example1 {

	/**
	 * A simple example that shows some usage for the alignment.Target and alignment.Link. 
	 *  
	 * <P> 
	 * The example assumes that you know the very basic of CAS. 
	 * In case if you are not familiar with CAS, it would be better to first 
	 * check the CAS usage example.  
	 * https://github.com/gilnoh/cas_access_example.git
	 * (example-1 in the above git will introduce you to the basic usage of CAS.) 
	 * 
	 * <P> 
	 * The code uses MaltParser LAP to get dependency annotations. The LAP uses 
	 * TreeTagger modules --- thus you need to install them to run the code actually. 
	 * 
	 * <P> Example 2 will cover more; for example, Prober for link annotations within 
	 * a CAS, and some utilities, etc. 
	 * 
	 */
	public static void main(String[] args) throws Exception {		// (For the sake of simplicity, we will ignore all Exceptions. Just don't do that in your code...)  
		
		// We have two types, for the alignment. One is alignment.Target that holds
		// various "annotation data". The other is alignment.Link, which actually connects
		// two targets; one target in TextView, the other target in HypothesisView. 
		// Let's see some usage for the types.
		
		// (You can find v3 link type definitions in the following URL.) 
		// https://docs.google.com/presentation/d/1A06cK2XsnaU9QXa1Ku_diafjcWKsvMG-6N8G0KDZWZw/edit?usp=sharing		

		// Before begin, let's have some CASes with some linguistic annotations. 
		// MaltParserEN is a LAPAccess, that tokenizes, lemmatizes, and does dependency parsing		
		LAPAccess lap = new MaltParserEN(); 
		JCas JCas1 = lap.generateSingleTHPairCAS("I have a dog.", "I own a pet."); 
		
		// Okay, now JCas1 and JCas2 have TextView:"I have a dog.", HypothesisView:"I own a pet." 
		// And each view has annotations of "Token", "Lemma", and "Dependency". 
		
		// - Q1: How can I put my annotations in Target? 
		// Let's start with some tokens. 

		// Okay, let's get some tokens from JCas1, and put them in 
		// alignment.Target type. 

		// first, we need the two views ... 
		JCas textViewOfJCas1 = JCas1.getView(TEXTVIEW); 
		JCas hypoViewOfJCas1 = JCas1.getView(HYPOTHESISVIEW); 

		// and tokens from TEXTVIEW
		Target[] text_targets = new Target[5]; // (for next examples) 
		Target[] hypo_targets = new Target[5]; 

		int i = 0; 
		for(Token token: JCasUtil.select(textViewOfJCas1, Token.class))
		{
			// Actual work code to put Annotations 
			// in a Target instance. 
			
			// 1) prepare a Target instance.  
			Target tg = new Target(textViewOfJCas1);
			// 2) prepare a FSArray instance, put the target annotations in it.   
			// (Note that FSArray is not really a Java Array -- but FSArray) 
			FSArray annots = new FSArray(textViewOfJCas1, 1); // this is a size 1 FSarray; if you put more, you need bigger array, of course... 
			annots.set(0, token);
			// 3) Okay, now the FSArray is prepared. Put it on field "targetAnnotations" 
			tg.setTargetAnnotations(annots);
			// 4) Set begin - end value of the Target annotation (just like any annotation)
			// note that, setting of begin and end of Target is a convention. 
			// - begin as the earliest "begin" (among Target-ed annotations) 
			// - end as the latest "end" (among Target-ed annotations) 
			tg.setBegin(token.getBegin());
			tg.setEnd(token.getEnd());
			// 5) add it to the index (just like any annotation) 
			tg.addToIndexes(); 
			
			// The target instance is now ready. 
			text_targets[i] = tg; // store it for next examples ... 
			i++; 
		}
					
		// Okay. no comments for preparing hypo side targets. 
		i=0; 
		for(Token token: JCasUtil.select(hypoViewOfJCas1, Token.class) )
		{
			Target tg = new Target(hypoViewOfJCas1);
			FSArray annots = new FSArray(hypoViewOfJCas1, 1); 
			annots.set(0, token);
			tg.setTargetAnnotations(annots);
			tg.setBegin(token.getBegin());
			tg.setEnd(token.getEnd());
			tg.addToIndexes(); 
			hypo_targets[i] = tg; // store it for next examples ... 
			i++; 			
		}

		// now text_targets have = [ target for "I", "have", "a", "dog", "." ] 
		// hypo_targets are = [ similar for "I", "own", "a", "pet", "." ] 		
		
		// - Q2: Oh. I see that. But token seems... very simple. Can I also 
		//   put something more complex? say, dependency-triple, or partial parse tree
		//   in a Target instance? 
		
		// Sure. Just as an example, putting a dependency triple (Token - Dependency - Token)
		// as a Target
		Target triple_on_T = null; 
		{
			triple_on_T = new Target(textViewOfJCas1); 
			FSArray fs_t = new FSArray(textViewOfJCas1, 3); 
			//Iterator<Dependency> itr = (JCasUtil.select(textViewOfJCas1, Dependency.class)).iterator(); 
			Iterator<Dependency> itr = JCasUtil.iterator(textViewOfJCas1,  Dependency.class);
			Dependency dep = itr.next(); 
			dep = itr.next(); // say, let's pick the 2nd dependency  "a --det--> dog"   
			
			Token ta = dep.getDependent(); 
			Token tb = dep.getGovernor(); 
			// okay. we have (ta - dep - tb)
			// let's put them in Target 
			fs_t.set(0, ta); 
			fs_t.set(1,  dep);
			fs_t.set(2, tb);
			triple_on_T.setTargetAnnotations(fs_t);
			// set begin-end of Target .. 
			// note that, setting of begin and end of Target is a convention. 
			// begin as the earliest "begin" (among Target-ed annotations) 
			// end as the latest "end" (among Target-ed annotations) 
			triple_on_T.setBegin(ta.getBegin()); // here we know token_a appears before token_b
			triple_on_T.setEnd(tb.getEnd());  
			triple_on_T.addToIndexes(); 
			
			// The target instance now holds that triple. 
		}
		
		
		// - Q3: Okay. now I see we can put any Annotation data added by LAP
		//       in a Target.  
		//       Can you show me how I can actually mark a alignment.Link? 
		{
			// let's the Target-s from Q1. 
			// text_targets[] = [ targets for "I", "have", "a", "dog", "." ] 
			// hypo_targets[] = [ targets for "I", "own", "a", "pet", "." ] 		
			
			// Let's put two Links on JCas1 
			// (dog - pet), and (have - own) 
			
			// Steps for making a link  
			// 1) prepare the Link 
			Link link1 = new Link(hypoViewOfJCas1); // note that, we will add the Link to HYPOTHESIS VIEW 
			// 2) put the Targets 
			// (here, we already have two Targets prepared for us in Q1) 
			Target target_T = text_targets[1]; 
			Target target_H = hypo_targets[1]; 
			link1.setTSideTarget(target_T); 
			link1.setHSideTarget(target_H); 
			
			// 3) put direction and strength 
			link1.setDirection(Direction.Bidirection); 
			// use setDirection() method to set the direction of the Link instance. 
			// The argument is an Enum type that is defined within Link class. 
			// (Note that, the direction is actually represented as a String-subtype within 
			// the CAS --- directionString --- but the Link class provides ENUM access instead 
			// of String access with setDirection() and getDirection() as (safer) wrapper. ) 
			
			link1.setStrength(1.0); 
			// Note on Strength --- it is just a double value. You can use any value. 
			// However, if your semantic (e.g. local entailment, etc ) has some convention
			// on normalization --- please follow that. 
			
			// 4) put the "link-information".
			link1.setAlignerID("WordNet"); // ID of the alinger, or the resource behind the alinger  
			link1.setAlignerVersion("3.1"); // version number of the aligner, or the resource 
			link1.setLinkInfo("synonym"); // detailed information about the relation.  
			
			// 5) mark "Semantic Group Label" 
			// TBDTBDTBDTBD TO BE DETERMINED  
			// Semantic group label marks the Link; you can choose one or more 
			// Labels that will be defined soon. 
			// such as "LocalEntailment", "LocalContradiction", "alignsToken", etc etc. 
			// This feature, is provided for the convenience of the consumers of the Links. 
			// (such as EDAs, or feature extractors, can utilize many different link types 
			// in a coherent fashion --- e.g. get me all local-entailments, local-contradiction, etc) 
			
			// 6) set begin-end 
			// Note that, by convention, we use begin-end of Target of TSideTarget 
			link1.setBegin(target_H.getBegin()); 
			link1.setEnd(target_H.getEnd());
			
			// 7) add to index 
			link1.addToIndexes(); 
			// Note that, by convention, we add Link to HSide (on HypothesisView) 
			// (note that when we prepare the Link in 1), we used HYPOTHESIS view.) 
						
			// we also put second Link, just for next example. 
			Link link2 = new Link(hypoViewOfJCas1); 
			link2.setTSideTarget(text_targets[3]); 
			link2.setHSideTarget(hypo_targets[3]); 
			link2.setDirection(Direction.TtoH); 
			link2.setStrength(0.9); 
			link2.setAlignerID("WordNet"); 
			link2.setAlignerVersion("3.1"); 
			link2.setLinkInfo("hypernym"); 
			link2.setBegin(hypo_targets[3].getBegin()); 
			link2.setEnd(hypo_targets[3].getEnd());
			link2.addToIndexes(); 
		}

		// - Q4: Oja. How about the opposite? How can I get Links back from a 
		//   CAS? "Give me all Links in a CAS!" 

		Link myLink = null; 
		{
			// Getting Link back from a JCas is just like, getting back any 
			// annotation type from a JCAS. 
			// Just keep in mind that, it is the contract (or convention) 
			// that all Links are annotated on HYPOTHESISVIEW.  
			
			// Let's start from JCas1 
			// first get HypothesisView 
			JCas hView = JCas1.getView(HYPOTHESISVIEW); 
			
			// get "Link" type annotations back.. 
			for (Link l : JCasUtil.select(hView, Link.class))
			{
				System.out.println("Found a link!");
				// you can access Link, as normal, annotation. Of course. 
				System.out.println("Its TSideTarget covers: " + l.getTSideTarget().getCoveredText()); 
				System.out.println("Its HSideTarget covers: " + l.getHSideTarget().getCoveredText()); 
				System.out.println("Link's full type string is: " + l.getID()); 
				System.out.println("Its direction is: " + l.getDirection().toString()); 
				System.out.println("Its strength is: " + l.getStrength()); 
				
				myLink = l; // saving for next Q  
			}
		}
		
		// - Q5: How can I get back the annotations (Token, Dependency, NER, etc) 
		//   that are stored in the Target? 
		//   Annotations stored in "Target" are casted back into "Annotation". 
		//   You have to fetch the annotations back to what they are (e.g. Token, Dependency) 
		//   Let's see how you do it. 
		
		{
			
			Target aTarget = myLink.getTSideTarget(); 
						
			// We already know that this aTarget has one Token in it. Let's fetch it.  
			FSArray arr = aTarget.getTargetAnnotations(); 			
			System.out.println("The last link, T side Target has Token "); 
			for (Token t : JCasUtil.select(arr, Token.class))
			{
				System.out.println("Begin: " + t.getBegin()); 
				System.out.println("End: " + t.getEnd());
				System.out.println("Lemma: " + t.getLemma().getValue()); 
				System.out.println("POS: " + t.getPos().getPosValue()); 
			}
			
			arr = myLink.getHSideTarget().getTargetAnnotations(); 

			System.out.println("The last link, H side Target has Token "); 
			for (Token t : JCasUtil.select(arr, Token.class))
			{
				System.out.println("Begin: " + t.getBegin()); 
				System.out.println("End: " + t.getEnd());
				System.out.println("Lemma: " + t.getLemma().getValue()); 
				System.out.println("POS: " + t.getPos().getPosValue()); 
			}
			
			// And from Q2, we have a Target that holds Dependency and Token 
			System.out.println("A target that hold dependency triple, it has; "); 
			for (Dependency d: JCasUtil.select(triple_on_T.getTargetAnnotations(), Dependency.class))
			{
				System.out.println("Dependency");
				System.out.println("Governer:" + d.getGovernor().getCoveredText()); 
				System.out.println("Dependent:" + d.getDependent().getCoveredText()); 
				System.out.println("Dependency:" + d.getDependencyType()); 
			}
			for (Token t: JCasUtil.select(triple_on_T.getTargetAnnotations(), Token.class))
			{
				System.out.println("Token");
				System.out.println("Begin: " + t.getBegin()); 
				System.out.println("End: " + t.getEnd());
				System.out.println("Lemma: " + t.getLemma().getValue()); 
				System.out.println("POS: " + t.getPos().getPosValue()); 				
			}
			
			// Wait. Just in case I don't know what is being hold in 
			// a Target's FSArray. Can I check its type, by running some code? --- 
			// You can actually check the type (getType())  
			// and even access all the data in it; as feature structures. 
			// (rather a CAS way then JCas way) 
			
			// But its usage is rather limited (since you can't use any JCas 
			// classes that wraps the UIMA types with such approach). 
	
		}
		
		// - Q6: How can I access who (which module) has annotated this Link? 
		{
			// access Link's alingerID.  
			String alingerId = myLink.getAlignerID(); 
			System.out.println(alingerId); 
			
			// This would declare which aligner (or its underlying resource) has 
			// caused the Link into existence. 
		}

		// - Q7: I've heard that each Link has type string. 
		// How can I access them? 
		{
			// use Link's getID() to get full, (or long) ID. 
			String fullId = myLink.getID(); 
			System.out.println(fullId); 
			// note that this ID is actually a concatenation of 
			// getAlingerID(), getAlingerVersion(), and getLinkInfo()
			
		}
	}
}
