package com.vmusco.pminer;

import java.io.FileInputStream;
import java.util.HashMap;

import com.vmusco.pminer.analyze.ExploreMutants;
import com.vmusco.pminer.analyze.GraphDisplayAnalyzer;
import com.vmusco.pminer.analyze.StatisticsMutantAnalyzer;
import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.MutationStatistics;
import com.vmusco.smf.analysis.ProcessStatistics;
import com.vmusco.softminer.graphs.EdgeIdentity;
import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.graphs.GraphNodeVisitor;
import com.vmusco.softminer.graphs.Graph.GraphApi;
import com.vmusco.softminer.graphs.persistance.GraphML;
import com.vmusco.softminer.graphs.persistance.GraphPersistence;

public class MutantsVizualization {
	public static void main(String[] args) throws Exception {
		
		//ProcessStatistics ps = ProcessStatistics.loadState("/home/vince/Experiments/ChangePropagation-dataset/commons-codec/smf.run.xml");
		
		String pt = "/home/vince/Experiments/ChangePropagation-dataset/commons-codec/";
		MutationStatistics<?> ms = MutationStatistics.loadState(pt+"mutations/main/LCR/mutations.xml");
		ProcessStatistics ps = ms.getRelatedProcessStatisticsObject();
		
		Graph g = Graph.getNewGraph(GraphApi.GRAPH_STREAM);
		
		GraphPersistence gp = new GraphML(g);
		gp.load(new FileInputStream(pt+"usegraph_C.xml"));
		
		//for(String s : ms.loadResultsForExecutedTestOnMutants(0)){
		for(String s : new String[]{"mutant_3"}){
			UseGraph ug = new UseGraph(g);
			//"mutant_4421"
			
			if(ms.loadMutationStats(s) == null){
				System.out.println("Mutant not found -- probably not executed !");
				System.exit(1);
			}
			MutantIfos mi = ms.getMutationStats(s);
			g.visitTo(ug, mi.getMutationIn());
			
			String[] retrievedArray = ExploreMutants.getRetrievedTests(ug, ps.getTestCases());
			GraphDisplayAnalyzer gda = new GraphDisplayAnalyzer(ug.getBasinGraph(), false, false);
			StatisticsMutantAnalyzer sma = new StatisticsMutantAnalyzer(-1, null);
			

			sma.fireIntersectionFound(ps, mi.getMutationIn(), mi, retrievedArray, ug, -1);
			gda.fireIntersectionFound(ps, mi.getMutationIn(), mi, retrievedArray, ug, -1);
			
			g.visitTo(new GraphNodeVisitor() {
				
				@Override
				public void visitNode(String node) {
				}
				
				@Override
				public void visitEdge(String from, String to) {
					System.out.println("(\""+from+"\",\""+to+"\")");
				}
				
				@Override
				public String[] nextNodesToVisitFrom(String node) {
					return null;
				}
			}, mi.getMutationIn());
			
			
			
			
			System.out.print("MUTANT: "+s+"\t");
			System.out.print("  prec = "+sma.getAvgPrecision());
			System.out.print("  rec = "+sma.getAvgRecall());
			System.out.println("  fscore = "+sma.getAvgFScore());
			
			
			
			HashMap<EdgeIdentity, Float> edgesw = new HashMap<>();
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.bm.PhoneticEngine.encode(java.lang.String)", "org.apache.commons.codec.language.bm.PhoneticEngine.encode(java.lang.String,org.apache.commons.codec.language.bm.Languages$LanguageSet)"), 0.36850484606864875f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.bm.PhoneticEngineRegressionTest.encode(java.util.Map,boolean,java.lang.String)", "org.apache.commons.codec.language.bm.PhoneticEngine.encode(java.lang.String,org.apache.commons.codec.language.bm.Languages$LanguageSet)"), 0.9173252582550049f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.bm.BeiderMorseEncoder.encode(java.lang.String)", "org.apache.commons.codec.language.bm.PhoneticEngine.encode(java.lang.String)"), 0.3333335405226989f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.bm.PhoneticEngine.encode(java.lang.String,org.apache.commons.codec.language.bm.Languages$LanguageSet)", "org.apache.commons.codec.language.bm.PhoneticEngine.encode(java.lang.String)"), 0.0f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.bm.PhoneticEnginePerformanceTest.test()", "org.apache.commons.codec.language.bm.PhoneticEngine.encode(java.lang.String)"), 0.353125f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.bm.PhoneticEngineRegressionTest.encode(java.util.Map,boolean,java.lang.String)", "org.apache.commons.codec.language.bm.PhoneticEngine.encode(java.lang.String)"), 0.9173252582550049f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.bm.PhoneticEngineTest.testEncode()", "org.apache.commons.codec.language.bm.PhoneticEngine.encode(java.lang.String)"), 0.0f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.bm.PhoneticEngineRegressionTest.testCompatibilityWithOriginalVersion()", "org.apache.commons.codec.language.bm.PhoneticEngineRegressionTest.encode(java.util.Map,boolean,java.lang.String)"), 0.998583984375f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.bm.PhoneticEngineRegressionTest.testSolrASHKENAZI()", "org.apache.commons.codec.language.bm.PhoneticEngineRegressionTest.encode(java.util.Map,boolean,java.lang.String)"), 0.674072265625f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.bm.PhoneticEngineRegressionTest.testSolrGENERIC()", "org.apache.commons.codec.language.bm.PhoneticEngineRegressionTest.encode(java.util.Map,boolean,java.lang.String)"), 0.6747680664062499f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.bm.PhoneticEngineRegressionTest.testSolrSEPHARDIC()", "org.apache.commons.codec.language.bm.PhoneticEngineRegressionTest.encode(java.util.Map,boolean,java.lang.String)"), 0.625f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.bm.BeiderMorseEncoder.encode(java.lang.Object)", "org.apache.commons.codec.language.bm.BeiderMorseEncoder.encode(java.lang.String)"), 0.3386474609375f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.StringEncoder.encode(java.lang.String)", "org.apache.commons.codec.language.bm.BeiderMorseEncoder.encode(java.lang.String)"), 0.0f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.bm.BeiderMorseEncoderTest.assertNotEmpty(org.apache.commons.codec.language.bm.BeiderMorseEncoder,java.lang.String)", "org.apache.commons.codec.language.bm.BeiderMorseEncoder.encode(java.lang.String)"), 0.3368705749511719f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.bm.BeiderMorseEncoderTest.testAllChars()", "org.apache.commons.codec.language.bm.BeiderMorseEncoder.encode(java.lang.String)"), 0.353125f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.bm.BeiderMorseEncoderTest.testEncodeGna()", "org.apache.commons.codec.language.bm.BeiderMorseEncoder.encode(java.lang.String)"), 0.3375f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.bm.BeiderMorseEncoderTest.testLongestEnglishSurname()", "org.apache.commons.codec.language.bm.BeiderMorseEncoder.encode(java.lang.String)"), 0.3375f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.bm.BeiderMorseEncoderTest.testOOM()", "org.apache.commons.codec.language.bm.BeiderMorseEncoder.encode(java.lang.String)"), 0.32499999999999996f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.bm.BeiderMorseEncoderTest.testSpeedCheck()", "org.apache.commons.codec.language.bm.BeiderMorseEncoder.encode(java.lang.String)"), 0.359375f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.Encoder.encode(java.lang.Object)", "org.apache.commons.codec.language.bm.BeiderMorseEncoder.encode(java.lang.Object)"), 0.0f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.bm.BeiderMorseEncoderTest.testSpeedCheck2()", "org.apache.commons.codec.language.bm.BeiderMorseEncoder.encode(java.lang.Object)"), 0.359375f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.bm.BeiderMorseEncoderTest.testSpeedCheck3()", "org.apache.commons.codec.language.bm.BeiderMorseEncoder.encode(java.lang.Object)"), 0.359375f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.AbstractCaverphone.isEncodeEqual(java.lang.String,java.lang.String)", "org.apache.commons.codec.StringEncoder.encode(java.lang.String)"), 0.17500000000000002f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.AbstractCaverphone.encode(java.lang.Object)", "org.apache.commons.codec.StringEncoder.encode(java.lang.String)"), 0.2692533493041992f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.SoundexUtils.difference(org.apache.commons.codec.StringEncoder,java.lang.String,java.lang.String)", "org.apache.commons.codec.StringEncoder.encode(java.lang.String)"), 0.20011160714285714f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.StringEncoderAbstractTest.checkEncoding(java.lang.String,java.lang.String)", "org.apache.commons.codec.StringEncoder.encode(java.lang.String)"), 0.2174107142858857f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.StringEncoderAbstractTest.testEncodeNull()", "org.apache.commons.codec.StringEncoder.encode(java.lang.String)"), 0.0f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.StringEncoderAbstractTest.testLocaleIndependence()", "org.apache.commons.codec.StringEncoder.encode(java.lang.String)"), 0.0f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.bm.BeiderMorseEncoderTest.testAsciiEncodeNotEmpty1Letter()", "org.apache.commons.codec.language.bm.BeiderMorseEncoderTest.assertNotEmpty(org.apache.commons.codec.language.bm.BeiderMorseEncoder,java.lang.String)"), 0.390625f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.bm.BeiderMorseEncoderTest.testAsciiEncodeNotEmpty2Letters()", "org.apache.commons.codec.language.bm.BeiderMorseEncoderTest.assertNotEmpty(org.apache.commons.codec.language.bm.BeiderMorseEncoder,java.lang.String)"), 0.384375f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.bm.BeiderMorseEncoderTest.testEncodeAtzNotEmpty()", "org.apache.commons.codec.language.bm.BeiderMorseEncoderTest.assertNotEmpty(org.apache.commons.codec.language.bm.BeiderMorseEncoder,java.lang.String)"), 0.3375f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.binary.Base64Codec13Test.testEncoder()", "org.apache.commons.codec.Encoder.encode(java.lang.Object)"), 0.252649305012892f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.StringEncoderAbstractTest.testEncodeEmpty()", "org.apache.commons.codec.Encoder.encode(java.lang.Object)"), 0.0f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.StringEncoderAbstractTest.testEncodeWithInvalidObject()", "org.apache.commons.codec.Encoder.encode(java.lang.Object)"), 0.0f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.StringEncoderComparator.compare(java.lang.Object,java.lang.Object)", "org.apache.commons.codec.Encoder.encode(java.lang.Object)"), 0.19921875f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.Caverphone1Test.testIsCaverphoneEquals()", "org.apache.commons.codec.language.AbstractCaverphone.isEncodeEqual(java.lang.String,java.lang.String)"), 0.1f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.Caverphone2Test.testIsCaverphoneEquals()", "org.apache.commons.codec.language.AbstractCaverphone.isEncodeEqual(java.lang.String,java.lang.String)"), 0.15000000000000002f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.Encoder.encode(java.lang.Object)", "org.apache.commons.codec.language.AbstractCaverphone.encode(java.lang.Object)"), 0.2692533493041992f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.RefinedSoundex.difference(java.lang.String,java.lang.String)", "org.apache.commons.codec.language.SoundexUtils.difference(org.apache.commons.codec.StringEncoder,java.lang.String,java.lang.String)"), 0.19062500000000002f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.Soundex.difference(java.lang.String,java.lang.String)", "org.apache.commons.codec.language.SoundexUtils.difference(org.apache.commons.codec.StringEncoder,java.lang.String,java.lang.String)"), 0.20479910714285715f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.Caverphone2Test.testCaverphoneRevisitedRandomWords()", "org.apache.commons.codec.StringEncoderAbstractTest.checkEncoding(java.lang.String,java.lang.String)"), 0.15000000000000002f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.ColognePhoneticTest.testAabjoe()", "org.apache.commons.codec.StringEncoderAbstractTest.checkEncoding(java.lang.String,java.lang.String)"), 0.19491525423728814f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.ColognePhoneticTest.testAaclan()", "org.apache.commons.codec.StringEncoderAbstractTest.checkEncoding(java.lang.String,java.lang.String)"), 0.288135593220339f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.ColognePhoneticTest.testAychlmajrForCodec122()", "org.apache.commons.codec.StringEncoderAbstractTest.checkEncoding(java.lang.String,java.lang.String)"), 0.3389830508474576f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.StringEncoderAbstractTest.checkEncodingVariations(java.lang.String,java.lang.String[])", "org.apache.commons.codec.StringEncoderAbstractTest.checkEncoding(java.lang.String,java.lang.String)"), 0.33928571464369445f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.StringEncoderAbstractTest.checkEncodings(java.lang.String[][])", "org.apache.commons.codec.StringEncoderAbstractTest.checkEncoding(java.lang.String,java.lang.String)"), 0.20111973649364406f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.StringEncoderComparatorTest.testComparatorWithDoubleMetaphoneAndInvalidInput()", "org.apache.commons.codec.StringEncoderComparator.compare(java.lang.Object,java.lang.Object)"), 0.0f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.StringEncoderComparatorTest.testComparatorWithSoundex()", "org.apache.commons.codec.StringEncoderComparator.compare(java.lang.Object,java.lang.Object)"), 0.19921875f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.RefinedSoundexTest.testDifference()", "org.apache.commons.codec.language.RefinedSoundex.difference(java.lang.String,java.lang.String)"), 0.19062500000000002f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.SoundexTest.testDifference()", "org.apache.commons.codec.language.Soundex.difference(java.lang.String,java.lang.String)"), 0.20479910714285715f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.Caverphone1Test.testCaverphoneRevisitedCommonCodeAT1111()", "org.apache.commons.codec.StringEncoderAbstractTest.checkEncodingVariations(java.lang.String,java.lang.String[])"), 0.1f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.Caverphone2Test.testCaverphoneRevisitedCommonCodeAT11111111()", "org.apache.commons.codec.StringEncoderAbstractTest.checkEncodingVariations(java.lang.String,java.lang.String[])"), 0.15000000000000002f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.Caverphone2Test.testCaverphoneRevisitedRandomNameKLN1111111()", "org.apache.commons.codec.StringEncoderAbstractTest.checkEncodingVariations(java.lang.String,java.lang.String[])"), 0.15000000000000002f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.Caverphone2Test.testCaverphoneRevisitedRandomNameTN11111111()", "org.apache.commons.codec.StringEncoderAbstractTest.checkEncodingVariations(java.lang.String,java.lang.String[])"), 0.15000000000000002f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.Caverphone2Test.testCaverphoneRevisitedRandomNameTTA1111111()", "org.apache.commons.codec.StringEncoderAbstractTest.checkEncodingVariations(java.lang.String,java.lang.String[])"), 0.15000000000000002f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.Caverphone2Test.testCaverphoneRevisitedRandomWords()", "org.apache.commons.codec.StringEncoderAbstractTest.checkEncodingVariations(java.lang.String,java.lang.String[])"), 0.15000000000000002f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.ColognePhoneticTest.testVariationsMella()", "org.apache.commons.codec.StringEncoderAbstractTest.checkEncodingVariations(java.lang.String,java.lang.String[])"), 0.3305084745762712f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.ColognePhoneticTest.testVariationsMeyer()", "org.apache.commons.codec.StringEncoderAbstractTest.checkEncodingVariations(java.lang.String,java.lang.String[])"), 0.2711864406779661f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.DaitchMokotoffSoundexTest.testEncodeIgnoreApostrophes()", "org.apache.commons.codec.StringEncoderAbstractTest.checkEncodingVariations(java.lang.String,java.lang.String[])"), 0.3933531746031746f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.DaitchMokotoffSoundexTest.testEncodeIgnoreHyphens()", "org.apache.commons.codec.StringEncoderAbstractTest.checkEncodingVariations(java.lang.String,java.lang.String[])"), 0.3933531746031746f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.SoundexTest.testB650()", "org.apache.commons.codec.StringEncoderAbstractTest.checkEncodingVariations(java.lang.String,java.lang.String[])"), 0.3142857142857143f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.SoundexTest.testEncodeIgnoreApostrophes()", "org.apache.commons.codec.StringEncoderAbstractTest.checkEncodingVariations(java.lang.String,java.lang.String[])"), 0.1875f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.SoundexTest.testEncodeIgnoreHyphens()", "org.apache.commons.codec.StringEncoderAbstractTest.checkEncodingVariations(java.lang.String,java.lang.String[])"), 0.20982142857142858f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.SoundexTest.testHWRuleEx3()", "org.apache.commons.codec.StringEncoderAbstractTest.checkEncodingVariations(java.lang.String,java.lang.String[])"), 0.2142857142857143f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.SoundexTest.testMsSqlServer2()", "org.apache.commons.codec.StringEncoderAbstractTest.checkEncodingVariations(java.lang.String,java.lang.String[])"), 0.20089285714285715f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.Caverphone1Test.testEndMb()", "org.apache.commons.codec.StringEncoderAbstractTest.checkEncodings(java.lang.String[][])"), 0.1f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.Caverphone1Test.testSpecificationV1Examples()", "org.apache.commons.codec.StringEncoderAbstractTest.checkEncodings(java.lang.String[][])"), 0.1f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.Caverphone1Test.testWikipediaExamples()", "org.apache.commons.codec.StringEncoderAbstractTest.checkEncodings(java.lang.String[][])"), 0.1f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.Caverphone2Test.testCaverphoneRevisitedExamples()", "org.apache.commons.codec.StringEncoderAbstractTest.checkEncodings(java.lang.String[][])"), 0.15000000000000002f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.Caverphone2Test.testEndMb()", "org.apache.commons.codec.StringEncoderAbstractTest.checkEncodings(java.lang.String[][])"), 0.15000000000000002f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.Caverphone2Test.testSpecificationExamples()", "org.apache.commons.codec.StringEncoderAbstractTest.checkEncodings(java.lang.String[][])"), 0.15000000000000002f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.ColognePhoneticTest.testEdgeCases()", "org.apache.commons.codec.StringEncoderAbstractTest.checkEncodings(java.lang.String[][])"), 0.4067796610169492f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.ColognePhoneticTest.testExamples()", "org.apache.commons.codec.StringEncoderAbstractTest.checkEncodings(java.lang.String[][])"), 0.4661016949152542f);
			edgesw.put(new EdgeIdentity("org.apache.commons.codec.language.ColognePhoneticTest.testHyphen()", "org.apache.commons.codec.StringEncoderAbstractTest.checkEncodings(java.lang.String[][])"), 0.4152542372881356f);

			gda.changeEdgesWeights(edgesw);
			ug.getBasinGraph().bestDisplay();
			
			System.in.read();
		}
	}
}
