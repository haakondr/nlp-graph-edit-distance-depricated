package no.roek.nlpged.preprocessing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import no.roek.nlpged.graph.Graph;
import no.roek.nlpged.graph.Edge;
import no.roek.nlpged.graph.Node;

import org.maltparser.MaltParserService;
import org.maltparser.core.exception.MaltChainedException;

import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.tagger.maxent.TaggerConfig;


public class DependencyParser {

	private MaltParserService maltService;
	private MaxentTagger tagger;
	private Morphology lemmatiser;

	public DependencyParser(String maltParams, String postagParams) throws MaltChainedException, ClassNotFoundException, IOException {
		this.maltService = new MaltParserService();
		maltService.initializeParserModel(maltParams);
		lemmatiser = new Morphology();
		this.tagger = new MaxentTagger(postagParams);
	}

	public Graph dependencyParse(String id, String sentence) throws MaltChainedException {
		Graph graph = new Graph(id, sentence);

		HashMap<String, List<String[]>> adj = new HashMap<>();
		String[] taggedTokens = tagSentence(sentence);
		for(String wordToken : maltService.parseTokens(taggedTokens)) {
			graph.addNode(getNode(wordToken.split("\t"), adj));
		}
		addEdges(graph, adj);

		return graph;
	}

	private Node getNode(String[] wordToken, HashMap<String, List<String[]>> adj) {
		String id = wordToken[0];
		String lemma = wordToken[2];
		String pos = wordToken[4];
		if(pos.equals(",")) {
			pos = "punct";
		}
		String rel = wordToken[6];
		String deprel = wordToken[7];

		if(!adj.containsKey(id)) {
			adj.put(id, new ArrayList<String[]>());
		}

		if(!rel.equals("0")) {
			adj.get(id).add(new String[] {rel, deprel});
		}

		return new Node(id, lemma, new String[] {pos});
	}

	private void addEdges(Graph graph, HashMap<String, List<String[]>> adj) {
		for (Node from: graph.getNodes()) {
			for (String[] edge : adj.get(from.getId())){
				Node to = graph.getNode(edge[0]);
				graph.addEdge(new Edge(from.getId()+"_"+to.getId(), from, to, edge[1]));
			}
		}
	}
	
	private String[] tagSentence(String sentence) {
		String taggedSentence = tagger.tagString(sentence);
		List<String> tokens = new ArrayList<>();
		int i = 1;
		for(String token : taggedSentence.split(" ")) {
			String[] temp = token.split("_");
			String lemma = lemmatiser.lemma(temp[0], temp[1]);
			tokens.add(i+"\t"+temp[0]+"\t"+lemma+"\t"+temp[1]+"\t"+temp[1]+"\t_");
			i++;
		}

		return tokens.toArray(new String[0]);
	}
}
