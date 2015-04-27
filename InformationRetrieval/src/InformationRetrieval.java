import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;


public class InformationRetrieval {
	public static void main(String[] args) {
		try {
			
			// INIZIO LABORATORIO 2 //
			
			HashMap<Integer, String> docsTitolo = new HashMap<Integer, String>();
			HashMap<Integer, String> docsAutori = new HashMap<Integer, String>();
			HashMap<Integer, String> docsAbstract = new HashMap<Integer, String>();
			HashMap<Integer, ArrayList<Integer>> docsCitazioni = new HashMap<Integer, ArrayList<Integer>>();
			HashMap<Integer, HashMap<String, Integer>> docsKeyWords = Parser.parserDocumentoKeyWords("./data/freq.docid.word.txt");
			HashMap<Integer, HashMap<String, Integer>> docsStems = Parser.parserDocumentoKeyWords("./data/freq.docid.stem.txt");
			
		    // UtilsIR.reformatXML("./data/docid.documento.xml", "./data/documenti.xml");
			Parser.parserDocumenti("./data/documenti.xml", docsCitazioni, docsTitolo, docsAutori, docsAbstract);
			
			// Creo un arraylist con tutti i documenti della collezione
			HashMap<Integer, Documento> docs = new HashMap<Integer, Documento>();
			Set<Integer> docsid = docsTitolo.keySet();
			for(int k: docsid) {
				Documento doc = new Documento(k, docsTitolo.get(k), docsAutori.get(k), docsAbstract.get(k), docsKeyWords.get(k), docsStems.get(k), docsCitazioni.get(k));
				docs.put(k, doc);
			}
			Formula ff = new Formula();
			HashMap<Integer, HashMap<String, Double>> pesiDocs = ff.calcolaFormula(docs, "./data/pesi.txt");
			
			// FINE LABORATORIO 2 //
			
			// INIZIO LABORATORIO 3 //
			
			// Recupero tutte le keywords delle query
			HashMap<Integer, HashMap<String, Integer>> keywordsQuery = Parser.parserQueryKeyword("./data/query-keyword.txt");
			
			// Recupero lo stem di tutte le keyword
			HashMap<Integer, ArrayList<String>> stemQuery = Parser.parserQueryStem("./data/query-stem.txt");
			int M = 1000;
			int N = 100;
			Reperimento reperimento = new Reperimento(pesiDocs, keywordsQuery, stemQuery, docs);
			HashMap<Integer, HashMap<Integer, Double>> docsReperiti = reperimento.eseguiReperimento("./data/reperimento.txt", M);
			reperimento.eseguiRelevanceFeedback("./data/reperimentoRF.txt", docsReperiti, N, M, "./data/qrels-originale.txt");
			
			// FINE LABORATORIO 3 //
			
			System.out.println("Fine");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
