package no.roek.nlpged.application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import no.roek.nlpged.algorithm.GraphEditDistance;
import no.roek.nlpged.graph.Graph;
import no.roek.nlpged.graph.Node;
import no.roek.nlpged.misc.EditWeightService;
import no.roek.nlpged.preprocessing.DependencyParser;

import org.maltparser.core.exception.MaltChainedException;

import com.konstantinosnedas.HungarianAlgorithm;


public class App {

	public static void main(String[] args) throws MaltChainedException, ClassNotFoundException, IOException {
		Config cs = new Config("app.properties");
		DependencyParser depParser = new DependencyParser(cs.getProperty("MALT_PARAMS"), cs.getProperty("POSTAGGER_PARAMS"));
		Map<String, Double> posEditWeights = EditWeightService.getEditWeights(cs.getProperty("POS_SUB_WEIGHTS"), cs.getProperty("POS_INSDEL_WEIGHTS"));
		Map<String, Double> deprelEditWeights = EditWeightService.getInsDelCosts(cs.getProperty("DEPREL_INSDEL_WEIGHTS"));


		boolean running = true;
		while(running) {
			System.out.println("\n------");
			String[] texts = getInputTexts(args);
			getDistance(texts, depParser, posEditWeights, deprelEditWeights);
			running = shouldContinue();
		}
		System.out.println("Exiting..");
	}
	
	public static void getDistance(String[] texts, DependencyParser depParser, Map<String,Double> posEditWeights, Map<String,Double> deprelEditWeights) throws MaltChainedException {
		Graph g1 = depParser.dependencyParse("1", texts[0]);
		Graph g2 = depParser.dependencyParse("2", texts[1]);

		GraphEditDistance ged = new GraphEditDistance(g1, g2, posEditWeights, deprelEditWeights);

		System.out.println("Calculating graph edit distance for the two sentences:");
		System.out.println(texts[0]);
		System.out.println(texts[1]);
		System.out.println("Distance between the two sentences: "+ged.getDistance()+". Normalised: "+ged.getNormalizedDistance());
		System.out.println("Edit path:");
		for(String editPath : getEditPath(g1, g2, ged.getCostMatrix(), true)) {
			System.out.println(editPath);
		}
	}

	public static String[] getInputTexts(String[] args)  {
		String text1="", text2="";
		if(args.length!=2) {
			InputStreamReader converter = new InputStreamReader(System.in);
			BufferedReader in = new BufferedReader(converter);
			try {
				System.out.println("Please enter the first sentence: ");
				text1 = in.readLine();

				System.out.println("Please enter the second sentence: ");
				text2 = in.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else {
			return args;
		}

		return new String[] {text1, text2};
	}

	public static List<String> getEditPath(Graph g1, Graph g2, double[][] costMatrix, boolean printCost) {
		return getAssignment(g1, g2, costMatrix, true, printCost);
	}

	public static List<String> getFreeEdits(Graph g1, Graph g2, double[][] costMatrix) {
		return getAssignment(g1, g2, costMatrix, false, false);
	}

	public static List<String> getAssignment(Graph g1, Graph g2, double[][] costMatrix, boolean editPath, boolean printCost) {
		List<String> editPaths = new ArrayList<>();
		int[][] assignment = HungarianAlgorithm.hgAlgorithm(costMatrix, "min");

		for (int i = 0; i < assignment.length; i++) {
			String from = getEditPathAttribute(assignment[i][0], g1);
			String to = getEditPathAttribute(assignment[i][1], g2);

			double cost = costMatrix[assignment[i][0]][assignment[i][1]];
			if(cost != 0 && editPath) {
				if(printCost) {
					editPaths.add("("+from+" -> "+to+") = "+cost);
				}
			}else if(cost == 0 && !editPath) {
				editPaths.add("("+from+" -> "+to+")");
			}
		}

		return editPaths;

	}

	private static String getEditPathAttribute(int nodeNumber, Graph g) {
		if(nodeNumber < g.getNodes().size()) {
			Node n= g.getNode(nodeNumber);
			return n.getLabel();
		}else {
			return "ε";
		}
	}
	
	public static boolean shouldContinue() {
		InputStreamReader converter = new InputStreamReader(System.in);
		BufferedReader in = new BufferedReader(converter);
		System.out.println("continue? [y/n]");
		try {
			String answer = in.readLine();
			return (answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
}
