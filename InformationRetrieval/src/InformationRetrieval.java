import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
			
			docsCitazioni = Parser.parserCitationsList("./data/citation.list.txt");
			
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
			HashMap<Integer, HashMap<String, Double>> keywordsQuery = Parser.parserQueryKeyword("./data/query-keyword.txt");
			
			// Recupero lo stem di tutte le keyword
			HashMap<Integer, ArrayList<String>> stemQuery = Parser.parserQueryStem("./data/query-stem.txt");
			int M = 1000;
			int N =5;
			double d = 0.10;
			double precisionePageRank = 1.0E-14;
			Reperimento reperimento = new Reperimento(pesiDocs, keywordsQuery, stemQuery, docs);
			HashMap<Integer, List<Map.Entry<Integer, Double>>> docsReperiti = reperimento.eseguiReperimento("./data/reperimento.txt", M);
			// FINE LABORATORIO 3 //
			
			// INIZIO LABORATORIO 4 //
			reperimento.eseguiRelevanceFeedback("./data/reperimentoRF.txt", docsReperiti, N, M, "./data/qrels-originale.txt");
			
			RelevanceFeedback rf_esplicito = new RelevanceFeedback(keywordsQuery, docs, pesiDocs);
			rf_esplicito.generaNuoveQueriesRF_esplicito(docsReperiti, M, N, "./data/qrels-originale.txt");
			
			Reperimento reperimentoRF_esplicito = new Reperimento(pesiDocs, keywordsQuery, stemQuery, docs);
			
			HashMap<Integer, List<Map.Entry<Integer, Double>>> docsReperitiRF_esplicito = reperimentoRF_esplicito.eseguiReperimento("./data/reperimentoEsplicito.txt", M);
			
//			// INIZIO LABORATORIO 5 //
//			
//			Reperimento reperimentoPageRank = new Reperimento(pesiDocs, keywordsQuery, stemQuery, docs);
//			reperimentoPageRank.eseguiReperimentoPageRank(d, M, precisionePageRank, docsReperiti, "./data/pageRank.txt");
			
			// FINE LABORATORIO 5 //
			
			// INIZIO LABORATORIO 6 //
			
//			N = 20;
//			Reperimento reperimentoLSA = new Reperimento(pesiDocs, keywordsQuery, stemQuery, docs);
//	    	reperimentoLSA.eseguiReperimentoLSA(N, M, docsReperiti, "./data/lsa.txt", docs, keywordsQuery);
			// FINE LABORATORIO 6 //
			
			// INIZIO LABORATORIO 7 //
//			N = 50;
//			Reperimento reperimentoHITS = new Reperimento(pesiDocs, keywordsQuery, stemQuery, docs);
//	    	reperimentoHITS.eseguiReperimentoHITS(N, M, docsReperiti, "./data/hits.txt", docs, keywordsQuery);			
			// FINE LABORATORIO 7 //
			
			System.out.println("Fine");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
