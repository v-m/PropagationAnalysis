package com.vmusco.smf.mutation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;

import org.apache.commons.io.FileUtils;

import spoon.compiler.SpoonCompiler;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtTypeMember;
import spoon.reflect.factory.Factory;
import spoon.reflect.factory.FactoryImpl;
import spoon.reflect.visitor.DefaultJavaPrettyPrinter;
import spoon.support.DefaultCoreFactory;
import spoon.support.JavaOutputProcessor;
import spoon.support.StandardEnvironment;
import spoon.support.compiler.jdt.JDTBasedSpoonCompiler;

import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.MutationStatistics;
import com.vmusco.smf.analysis.ProcessStatistics;
import com.vmusco.smf.compilation.ClassFileUtil;
import com.vmusco.smf.compilation.Compilation;
import com.vmusco.smf.utils.ConsoleTools;
import com.vmusco.smf.utils.NewReportedStandardEnvironment;

public final class Mutation {
	private static final String MUTANT_FILE_PREFIX = "mutant_";

	private Mutation() {}

	public static MutationStatistics createMutationElement(ProcessStatistics ps, Class<MutationOperator<?>> mutatorClass) throws InstantiationException, IllegalAccessException, ClassNotFoundException{
		return createMutationElement(ps, mutatorClass, null, null);
	}

	public static MutationStatistics createMutationElement(ProcessStatistics ps, Class<MutationOperator<?>> mutatorClass, String mutationid, String[] classToMutate) throws InstantiationException, IllegalAccessException, ClassNotFoundException{
		MutationStatistics ms = new MutationStatistics(ps, mutatorClass);

		if(classToMutate != null){
			ms.classToMutate = classToMutate;
		}

		if(mutationid != null){
			ms.mutationName = mutationid;
		}

		return ms;
	}

	public static void createMutants(ProcessStatistics ps, MutationStatistics ms, MutationCreationListener mcl, boolean reset) throws IOException, URISyntaxException{
		createMutants(ps, ms, mcl, reset, -1);
	}

	public static void createMutants(ProcessStatistics ps, MutationStatistics ms, MutationCreationListener mcl, boolean reset, int nb) throws IOException, URISyntaxException{		
		long t1 = System.currentTimeMillis();
		int mutantcounter = 0;

		StandardEnvironment standardEnvironment = new StandardEnvironment();
		standardEnvironment.setAutoImports(true);

		Factory factory = new FactoryImpl(new DefaultCoreFactory(), standardEnvironment);
		SpoonCompiler compiler = new JDTBasedSpoonCompiler(factory);

		String[] mutateFrom = ms.classToMutate;
		if(ms.classToMutate == null || ms.classToMutate.length <= 0){
			mutateFrom = ps.srcToCompile;
		}

		for(String srcitem : mutateFrom){
			compiler.addInputSource(new File(ps.getProjectIn(true) + File.separator + srcitem));
		}

		//Updating classpath
		String[] cp;
		int i = 0;

		if(ps.getClasspath() != null){
			cp = new String[ps.getClasspath().length + 1];

			for(String cpe : ps.getClasspath()){
				cp[i++] = cpe;
			}
		}else{
			cp = new String[1];
		}

		cp[i] = ps.getWorkingDir() + File.separator + ps.projectOut;

		compiler.setSourceClasspath(cp);

		// Build (in memory)
		compiler.build();

		// Obtain list of element to mutate
		List<String> arg0 = new ArrayList<String>();

		arg0.add(ms.getMutationClassName());
		compiler.process(arg0);

		// Clean previous mutants
		File f = new File(ms.getSourceMutationResolved());
		if(reset && f.exists()){
			System.out.println("Mutant sources folder exists... Erasing...");
			FileUtils.deleteDirectory(f);
			System.out.println("Succeded: "+(f.exists()?"False":"True"));
		}
		f.mkdirs();

		f = new File(ms.getBytecodeMutationResolved());
		if(reset && f.exists()){
			System.out.println("Mutant bytecode folder exists... Erasing...");
			FileUtils.deleteDirectory(f);
			System.out.println("Succeded: "+(f.exists()?"False":"True"));
		}
		f.mkdirs();

		if(reset){
			ms.mutations.clear();
		}else{
			for(Object o : ms.mutations.keySet()){
				String s = (String) o;
				int numb = Integer.valueOf(s.substring(MUTANT_FILE_PREFIX.length()));
				if(numb >= mutantcounter){
					mutantcounter = numb+1;
				}
			}

			System.out.println("Continuing to mutant "+mutantcounter);
		}

		int cpt = 1;
		int cpt2 = 1;

		int nbmax = MutationGateway.getMutationCandidates().length;

		if(mcl != null) mcl.preparationDone(nbmax);

		List<Object[]> mutations = new ArrayList<Object[]>();

		for(CtElement e : MutationGateway.getMutationCandidates()){
			CtClass theClass = findAssociatedClass(e);

			cpt++;

			if(theClass == null){
				ConsoleTools.write("WARNING:\n", ConsoleTools.BG_YELLOW);
				ConsoleTools.write("Unable to find a parent class for the element "+e.getSignature()+".");
				ConsoleTools.write("This item is skipped cleanly and silently but be aware of this :)");
				ConsoleTools.endLine(2);
				continue;
			}

			HashMap<CtElement, TargetObtainer> mutatedEntriesWithTargets = null;

			try{
				mutatedEntriesWithTargets = ms.getMutationObject().getMutatedEntriesWithTarget(e, factory);
			}catch(ClassCastException ex){
				ex.printStackTrace();
			}

			Iterator iterator = mutatedEntriesWithTargets.keySet().iterator();

			while(iterator.hasNext()){
				CtElement m = (CtElement) iterator.next();
				TargetObtainer to = mutatedEntriesWithTargets.get(m);

				Object[] o = new Object[]{ e, m, to };
				mutations.add(o);
			}
		}

		Collections.shuffle(mutations);
		Set<String> mutHashs = new HashSet<String>();

		for(Object m : ms.mutations.keySet()){
			MutantIfos mi = (MutantIfos) ms.mutations.get(m);

			if(mi.hash == null){
				String outp = ms.getSourceMutationResolved() + File.separator + m;
				System.out.println("Fixing hash for "+outp);
				try{
					mi.hash = convertByteHashToString(getHashForMutationSource(outp));
				}catch(Exception ex){
					System.out.println("Unable to extract hash for "+m);
				}
			}

			mutHashs.add(mi.hash);
		}

		int validmutants = 0;
		int droppedmutants = 0;
		int hashclashcpt = 0;
		int fnb = (nb<0)?mutations.size():nb;
		
		while(mutations.size()>0 && validmutants<fnb){
			Object[] o = mutations.remove(0);
			CtElement e = (CtElement) o[0];
			CtElement m = (CtElement) o[1];
			TargetObtainer to = (TargetObtainer) o[2];
			CtClass theClass = findAssociatedClass(e);
			CtElement toReplace = to.DetermineTarget(e);

			MutantIfos ifos = new MutantIfos();
			ifos.mutationIn = Mutation.getMethodFullSignatureForParent(toReplace);
			if(ifos.mutationIn == null)
				continue;

			ifos.mutationFrom = toReplace.toString();
			ifos.mutationTo = m.toString();

			if(mcl != null) mcl.newMutationProposal(cpt, cpt2, e, m);
			cpt2++;

			m.setParent(toReplace.getParent());
			toReplace.replace(m);

			DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
			Map<String, byte[]> built = Compilation.compilesUsingJavax(theClass, generateAssociatedClassContent(theClass), ps.getTestingClasspath(), diagnostics);

			String mutationid = MUTANT_FILE_PREFIX+mutantcounter++;
			String outp = ms.getSourceMutationResolved() + File.separator + mutationid;
			String boutp = ms.getBytecodeMutationResolved() + File.separator + mutationid;

			persistMutantClass(theClass, outp, factory);

			boolean hashclash = false;
			try{
				ifos.hash = convertByteHashToString(getHashForMutationSource(outp));

				if(mutHashs.contains(ifos.hash)){
					System.out.println("\t =======>>>>>> This mutant already exist... Skipping and dropping "+mutationid+"...");
					hashclash = true;
					FileUtils.deleteDirectory(new File(outp));
					mutantcounter--;
					hashclashcpt++;
				}else{
					mutHashs.add(ifos.hash);
				}
			}catch(Exception ex){
				System.out.println("Unable to extract hash for "+mutationid);
			}

			if(!hashclash){
				if(built != null){
					ifos.viable = true;

					//TODO: theClass.isTopLevel() ==> Should be taken into consideration !!!
					persistBytecodes(built, boutp);

					validmutants++;

					if(mcl != null) mcl.viableMutant(cpt, cpt2, e, m);
				}else{
					ifos.viable = false;
					if(ifos.mutationIn == null)
						ifos.mutationIn = "?";

					FileOutputStream fos = new FileOutputStream(boutp+".debug.txt");

					for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {
						if(diagnostic.getCode() != null){
							fos.write(diagnostic.getCode().getBytes());
							fos.write("\n".getBytes());
						}
						if(diagnostic.getKind() != null){
							fos.write(diagnostic.getKind().toString().getBytes());
							fos.write("\n".getBytes());
						}
						fos.write(Long.toString(diagnostic.getPosition()).getBytes());
						fos.write("\n".getBytes());
						fos.write(Long.toString(diagnostic.getStartPosition()).getBytes());
						fos.write("\n".getBytes());
						fos.write(Long.toString(diagnostic.getEndPosition()).getBytes());
						fos.write("\n".getBytes());
						if(diagnostic.getSource() != null){
							fos.write(diagnostic.getSource().toString().getBytes());
							fos.write("\n".getBytes());
						}
						if(diagnostic.getMessage(null) != null){
							fos.write(diagnostic.getMessage(null).getBytes());
							fos.write("\n".getBytes());
						}
						fos.write("=====\n".getBytes());
					}

					fos.close();
					droppedmutants++;
					if(mcl != null) mcl.unviableMutant(cpt, cpt2, e, m);
				}

				ms.mutations.put(mutationid, ifos);
			}

			m.replace(toReplace);


			if(mcl != null) mcl.endingMutationCheck(cpt, validmutants, droppedmutants, e);
		}

		System.out.println(hashclashcpt);

		long t2 = System.currentTimeMillis();
		ms.mutantsGenerationTime = t2-t1;

		if(mcl != null) mcl.mutationSummary(validmutants, droppedmutants+hashclashcpt, ms.mutantsGenerationTime);
	}

	public static byte[] getHashForMutationSource(String mutant_roots) throws IOException, NoSuchAlgorithmException{
		File mutant_root = new File(mutant_roots);

		if(mutant_root.isDirectory()){
			Iterator<File> iterateFiles = FileUtils.iterateFiles(mutant_root, new String[]{"java"}, true);
			List<String> as = new ArrayList<String>();

			while(iterateFiles.hasNext()){
				File s = iterateFiles.next();
				as.add(s.getAbsolutePath());
			}

			Collections.sort(as);
			MessageDigest md = MessageDigest.getInstance("MD5");

			for(String ss : as){
				File s = new File(ss);
				String pt = s.getAbsolutePath().substring(mutant_root.getAbsolutePath().length());
				while(pt.charAt(0) == File.separatorChar)
					pt = pt.substring(1);

				md.update(pt.getBytes());
				md.update(FileUtils.readFileToByteArray(s));
			}

			byte[] re = md.digest();
			md.reset();
			return re;
		}

		return null;
	}

	public static String convertByteHashToString(byte[] digest){
		BigInteger bigInt = new BigInteger(1,digest);
		String hashtext = bigInt.toString(16);
		while(hashtext.length() < 32 ){
			hashtext = "0"+hashtext;
		}
		return hashtext;
	}

	private static SpoonCompiler prepareCompilerForFixing(ProcessStatistics ps) {
		StandardEnvironment standardEnvironment = new StandardEnvironment();
		standardEnvironment.setAutoImports(true);

		Factory factory = new FactoryImpl(new DefaultCoreFactory(), standardEnvironment);
		SpoonCompiler compiler = new JDTBasedSpoonCompiler(factory);

		//Updating classpath
		String[] cp;
		int i = 0;

		if(ps.getClasspath() != null){
			cp = new String[ps.getClasspath().length + 1];

			for(String cpe : ps.getClasspath()){
				cp[i++] = cpe;
			}
		}else{
			cp = new String[1];
		}

		cp[i] = ps.getWorkingDir() + File.separator + ps.projectOut;

		compiler.setSourceClasspath(cp);

		return compiler;
	}

	private static String getMethodFullSignatureForParent(CtElement e){
		CtElement searchSignature = e;
		while(!(searchSignature instanceof CtMethod) && !(searchSignature instanceof CtConstructor) && searchSignature != null){
			searchSignature = searchSignature.getParent();
		}

		if(searchSignature == null){
			return null;
		}

		return resolveName((CtTypeMember)searchSignature);
	}

	public static String resolveName(CtTypeMember castedElement){

		int pos = castedElement.getSignature().indexOf("(");
		String st = castedElement.getSignature().substring(0, pos);
		pos = st.lastIndexOf(' ');

		if(castedElement instanceof CtConstructor)
			return castedElement.getSignature();
		else if(castedElement instanceof CtMethod)
			return castedElement.getDeclaringType().getQualifiedName()+"."+castedElement.getSignature().substring(pos+1);
		else
			return null;
	}

	/**
	 * 
	 * @param parentItems the items (files) to mutate
	 * @param classpath the classpath to use
	 * @param parentBuild the folder in which the original has been built
	 * @param processor a processor extending MutationOperation
	 * @param mutantOut a folder where to persist valid mutants
	 * @throws Exception 
	 */
	@Deprecated
	public static void createMutant(String parentItems, String[] classpath, String parentBuild, MutationOperator processor, String mutantOut) throws Exception{
		int mutantcounter = 0;

		Factory factory = new FactoryImpl(new DefaultCoreFactory(), new StandardEnvironment());
		SpoonCompiler compiler = new JDTBasedSpoonCompiler(factory);
		compiler.addInputSource(new File(parentItems));

		//Updating classpath
		String[] cp = new String[classpath.length + 1];

		int i = 0;
		for(String cpe : classpath){
			cp[i++] = cpe;
		}

		cp[i] = parentBuild;
		compiler.setSourceClasspath(cp);

		// Build (in memory)
		compiler.build();

		// Obtain list of element to mutate
		List<String> arg0 = new ArrayList<String>();
		arg0.add(processor.getClass().getName());
		compiler.process(arg0);

		for(CtElement e : MutationGateway.getMutationCandidates()){
			for(CtElement m : processor.getMutatedEntries(e, factory)){
				CtElement parent = e;

				parent.replace(m);

				CtClass theClass = findAssociatedClass(e);

				if(Compilation.compilesUsingJavax(theClass, generateAssociatedClassContent(theClass), classpath) != null){
					String outp = mutantOut+File.separator+MUTANT_FILE_PREFIX+mutantcounter++;
					//TODO: theClass.isTopLevel() ==> Should be taken into consideration !!!
					persistMutantClass(theClass, outp, factory);
				}

				m.replace(parent);
			}

		}
	}

	public static void persistMutantClass(CtClass aClass, String outputPath, Factory f){
		StandardEnvironment env = new NewReportedStandardEnvironment();
		JavaOutputProcessor fileOutput = new JavaOutputProcessor(new File(outputPath), new DefaultJavaPrettyPrinter(env));
		fileOutput.setFactory(f);

		//SourcePosition sp = aClass.getPosition();
		aClass.setPosition(null);
		fileOutput.getCreatedFiles().clear();
		fileOutput.createJavaFile(aClass);
	}

	public static void persistBytecodes(Map<String, byte[]> bytecodes, String outfolder) throws IOException{
		for (String compiledClassName : bytecodes.keySet()){
			String fileName = new String(compiledClassName).replace('.', File.separatorChar) + ".class";
			byte[] compiledClass = bytecodes.get(compiledClassName);
			ClassFileUtil.writeToDisk(true, outfolder, fileName, compiledClass);
		}
	}

	static private CtClass findAssociatedClass(CtElement e){
		CtElement c = e;

		while(c != null && (!(c instanceof CtClass) || (c instanceof CtClass && !((CtClass)c).isTopLevel()))){
			c = c.getParent();
		}

		return (CtClass)c;
	}

	/**
	 * 
	 * @param anElement
	 * @return as a string the source ready for compilation
	 */
	static private String generateAssociatedClassContent(CtClass anElement){
		DefaultJavaPrettyPrinter prettyPrinter = new DefaultJavaPrettyPrinter(new StandardEnvironment());
		prettyPrinter.scan(anElement);
		String sourceCode = ("package "+anElement.getPackage().getQualifiedName()+"; "+prettyPrinter.toString());
		return sourceCode;
		//prettyPrinter.reset();
	}
}
