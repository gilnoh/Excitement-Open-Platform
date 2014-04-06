package eu.excitementproject.eop.lap.biu.sentencesplit;

import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.junit.Test;


/**
 * Tests for sentence splitters. This abstract class should e extended
 * with classes of specific splitters.<BR>
 * The motivation for these tests rose when we found mistakes that Nagel
 * is doing on sentences with special unicode characters.
 * 
 * Currently all tested sentences here are taken from RTE3, but generally
 * any sentences could be used.
 * 
 * @author Ofer Bronstein
 * @since April 2014
 */
public abstract class SentenceSplitterTests {

	private void testSplitter(String inputDocument, String[] expectedOutput) {
		try {
			List<String> expectedOutputList = Arrays.asList(expectedOutput);
			System.out.printf("\n- Sentence Splitter Test\n  ======================\n*INPUT:\n%s\n*EXPECTED OUTPUT (%d sentence(s)):\n%s\n",
					inputDocument, expectedOutputList.size(), expectedOutputList);
			
			SentenceSplitter splitter = getSplitter();
			splitter.setDocument(inputDocument);
			splitter.split();
			List<String> receivedOutput = splitter.getSentences();
			System.out.printf("*RECEIVED OUTPUT FROM SPLITTER (%d sentence(s)):\n%s\n", receivedOutput.size(), receivedOutput);
			
			if (expectedOutputList.equals(receivedOutput)) {
				System.out.printf("RESULT: ***** Sentence-split is equal to expected! *****\n");
			}
			else {
				System.err.printf("RESULT:\n\t\tXXXXXXXX Sentence-split is different than expected. XXXXXXXX\n");
				fail("Sentence-split is different than expected.");
			}
		} catch (SentenceSplitterException e) {
			fail("Test failed due to exception: \n" + ExceptionUtils.getFullStackTrace(e));
		}
	}
	
	@Test
	public void oneSentence() throws Exception {
		// One sentence
		testSplitter(
			"Wilkins was chosen for the England's squad which qualified for the 1986 World Cup in Mexico, and played in the opening defeat against Portugal.",
			new String[] {
				"Wilkins was chosen for the England's squad which qualified for the 1986 World Cup in Mexico, and played in the opening defeat against Portugal."
			});
	}
	
	@Test
	public void oneSentenceWithPeriod() throws Exception {
		// One sentence that has a non-sentence-ending period in it
		testSplitter(
			"The dispute has come up between the manufacturer of the iPhone (which was presented on Wednesday for the first time) - Apple Inc. - and a leader in network and communication systems, based in San Jose - Cisco.",
			new String[] {
				"The dispute has come up between the manufacturer of the iPhone (which was presented on Wednesday for the first time) - Apple Inc. - and a leader in network and communication systems, based in San Jose - Cisco."
			});
	}
	
	@Test
	public void twoSentences() throws Exception {
		// Two sentences
		testSplitter(
				"Governor Tim Pawlenty of Minnesota signed a bill into law on Thursday that requires the state to generate a significant amount of its energy needs from renewable sources. The amount of power generated by wind turbines in the state stands now at 895 megawatts.",
				new String[] {
					"Governor Tim Pawlenty of Minnesota signed a bill into law on Thursday that requires the state to generate a significant amount of its energy needs from renewable sources.",
					"The amount of power generated by wind turbines in the state stands now at 895 megawatts."
				});
	}
	
	@Test
	public void nonAsciiChars() throws Exception {
		// Sentence with non-ASCII Unicode characters
		testSplitter(
				"This region became the main focal point of the rebellion lead by 27-year-old René Capistran Garza, leader of the Mexican Association of Catholic Youth.",
				new String[] {
					"This region became the main focal point of the rebellion lead by 27-year-old René Capistran Garza, leader of the Mexican Association of Catholic Youth."
				});
	}
	
	@Test
	public void twoSentencesNonAsciiChars() throws Exception {
		// Two sentences with non-ASCII Unicode characters
		testSplitter(
				"André Henri Constant van Hasselt was born at Maastricht, in Limburg. He was educated in his native town, and at the university of Liège.",
				new String[] {
						"André Henri Constant van Hasselt was born at Maastricht, in Limburg.",
						"He was educated in his native town, and at the university of Liège.",
				});
	}
	
	public abstract SentenceSplitter getSplitter();
}
