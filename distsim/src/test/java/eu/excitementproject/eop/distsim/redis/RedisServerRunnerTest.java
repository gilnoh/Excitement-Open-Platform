package eu.excitementproject.eop.distsim.redis;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;

import com.google.common.io.Files;
import com.google.common.io.Resources;

import eu.excitementproject.eop.common.component.lexicalknowledge.LexicalResource;
import eu.excitementproject.eop.common.component.lexicalknowledge.LexicalRule;
import eu.excitementproject.eop.common.component.lexicalknowledge.RuleInfo;
import eu.excitementproject.eop.distsim.resource.SimilarityStorageBasedLexicalResource;
import eu.excitementproject.eop.distsim.storage.DefaultSimilarityStorage;
import eu.excitementproject.eop.distsim.storage.RedisBasedStringListBasicMap;
import eu.excitementproject.eop.distsim.storage.SimilarityStorage;

public class RedisServerRunnerTest {

	@Test
	public void test() {
		
        Logger.getRootLogger().setLevel(Level.INFO); // (hiding < INFO)

		// Simple running itself. Without specifying rdb file. (won't create/load any) 
		RedisServerRunner rs = null; 		
		try {
			rs = new RedisServerRunner(6371); 
			rs.start();
			// no need // Thread.sleep(5000); 
		} catch (Exception e)
		{
			System.out.println("Redis Server runner raised an exception: " + e.getMessage()); 
			e.printStackTrace(); 
			fail("Redis server runner test failed."); 
		}
		assertNotNull(rs); 
		rs.stop(); 
		
		// Running with a specific RDB file. 
		// TODO (update: those files need to be provided by Jar resources!) 
		RedisServerRunner rs_l = null; 
		RedisServerRunner rs_r = null; 
		try {
//			rs_l = new RedisServerRunner(6379, "/home/tailblues/temp/", "similarity-l2r.rdb"); 
//			rs_r = new RedisServerRunner(6380, "/home/tailblues/temp/", "similarity-r2l.rdb"); 
			System.out.println("extracting l2r file");
			File l2rRdb = extractDataFileFromJar("redis-german-lin/similarity-l2r.rdb");
			System.out.println("extracted in: " + l2rRdb.getParent() + "//" + l2rRdb.getName()); 
			System.out.println("extracting r2l file");
			File r2lRdb = extractDataFileFromJar("redis-german-lin/similarity-r2l.rdb"); 
			System.out.println("extracted in: " + r2lRdb.getParent() + "//" + r2lRdb.getName()); 
//			rs_l = new RedisServerRunner(6379, "/Users/tailblues/temp", "similarity-l2r.rdb"); 
//			rs_r = new RedisServerRunner(6380, "/Users/tailblues/temp", "similarity-r2l.rdb"); 
			rs_l = new RedisServerRunner(6379, l2rRdb.getParent(), l2rRdb.getName()); 
			rs_r = new RedisServerRunner(6380, r2lRdb.getParent(), r2lRdb.getName()); 
			
			// run 
			rs_l.start();
			//Thread.sleep(1000); // do we need this? no. 
			rs_r.start(); 
			//Thread.sleep(5000); 

		} catch (Exception e)
		{
			System.out.println("Redis Server runner raised an exception: " + e.getMessage()); 
			e.printStackTrace(); 
			fail("Redis server runner test failed."); 
		}
		assertNotNull(rs_l); 
		assertNotNull(rs_r); 


		// Reading testing sequence 
		RedisBasedStringListBasicMap l2r = new RedisBasedStringListBasicMap("127.0.0.1", 6379);
		RedisBasedStringListBasicMap r2l = new RedisBasedStringListBasicMap("127.0.0.1", 6380); 
		
		SimilarityStorage ss = new DefaultSimilarityStorage(l2r, r2l, "GermanLin", "inst1", "eu.excitementproject.eop.distsim.items.LemmaPosBasedElement"); 
		LexicalResource<? extends RuleInfo> resource = new SimilarityStorageBasedLexicalResource(ss, 10);

		try {
			List<? extends LexicalRule<? extends RuleInfo>> similarities_l = resource.getRulesForLeft("ewig", null); 
			System.out.println("left-2-right rules: ");
			for (LexicalRule<? extends RuleInfo> similarity : similarities_l)
				System.out.println("<" + similarity.getLLemma() + "," + similarity.getLPos() + ">" + " --> " + "<" + similarity.getRLemma() + "," + similarity.getRPos() + ">" + ": " + similarity.getConfidence());

			List<? extends LexicalRule<? extends RuleInfo>> similarities_r = resource.getRulesForRight("ewig", null); 
			System.out.println("right-2-left rules: ");
			for (LexicalRule<? extends RuleInfo> similarity : similarities_r)
				System.out.println("<" + similarity.getLLemma() + "," + similarity.getLPos() + ">" + " --> " + "<" + similarity.getRLemma() + "," + similarity.getRPos() + ">" + ": " + similarity.getConfidence());
		
		
		} catch (Exception e)
		{
			System.out.println("Lexical resource access via Redis Server runner raised an exception: " + e.getMessage()); 
			e.printStackTrace(); 
			fail("Redis server runner test failed."); 			
		}
		
		rs_l.stop();
		rs_r.stop(); 
		
	}
	
	private static File extractDataFileFromJar(String resourceName) throws IOException {
		File tmpDir = Files.createTempDir();
		tmpDir.deleteOnExit();

		File rdb = new File(tmpDir, resourceName);
		FileUtils.copyURLToFile(Resources.getResource(resourceName), rdb);
		rdb.deleteOnExit();
		//rdb.setExecutable(true);
		
		return rdb;
	}

}