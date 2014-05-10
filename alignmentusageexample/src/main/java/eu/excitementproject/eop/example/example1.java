package eu.excitementproject.eop.example;

import java.util.Collection;
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;

import static eu.excitementproject.eop.lap.implbase.LAP_ImplBase.*; 
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import eu.excitement.type.alignment.Target;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.dkpro.MaltParserEN;







//import static org.apache.uima.fit.util.JCasUtil; 
import org.uimafit.util.JCasUtil; 

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
	public static void main(String[] args) throws Exception {		// (For the sake of simplicity, we will ignore all Exceptions. Just don't do that in your code...)  
		
		// We have two types, for the alignment. One is alignment.Target that holds
		// various "annotation data". The other is alignment.Link, which actually connects
		// two targets; one target in TextView, the other target in HypothesisView. 
		// TODO put v3 slides link here. 
		
		// Let's see some usage for the types.
		
		// Before begin, let's have some CASes with some linguistic annotations. 
		// MaltParserEN is a LAPAccess, that tokenizes, lemmatizes, and does dependency parsing		
		LAPAccess lap = new MaltParserEN(); 
		JCas JCas1 = lap.generateSingleTHPairCAS("I have a dog.", "I own a pet."); 
//		JCas JCas2 = lap.generateSingleTHPairCAS("I have a dog.", "I own a pet."); 
		
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
			// 3) Okay, now the Target items are prepared. Put it. 
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
		{
			Target triple_on_T = new Target(textViewOfJCas1); 
			FSArray fs_t = new FSArray(textViewOfJCas1, 3); 
			Dependency dep = (JCasUtil.select(textViewOfJCas1, Dependency.class)).iterator().next(); 
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
		
		// - Q4: Oja. How about the opposite? How can I get Links back from a 
		//   CAS? "Give me all Links in a CAS!" 
		
		// - Q5: Say, I have a Link, with its two targets --- how can I get back 
		//   the annotations that are linked by the Link? (in other words, how can I 
		//   get back my annotations within a (or two) Target(s) 
		
		// - Q6: How can I access who have annotated this? 

		// - Q7: I've heard that each Link has "unique" full-ID. How can I access them? 

	}

}
