import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;


public class InformationRetrieval {
	public static void main(String[] args) {
		try {
			
			// INIZIO LABORATORIO 2 //
			
			HashMap<Integer, String> docsTitolo = new HashMap<Integer, String>();
			HashMap<Integer, String> docsAbstract = new HashMap<Integer, String>();
			HashMap<Integer, ArrayList<Integer>> docsCitazioni = new HashMap<Integer, ArrayList<Integer>>();
			HashMap<Integer, HashMap<String, Integer>> docsKeyWords = Parser.parserDocumentoKeyWords("./data/freq.docid.word.txt");
			HashMap<Integer, HashMap<String, Integer>> docsStems = Parser.parserDocumentoKeyWords("./data/freq.docid.stem.txt");
			
		    // UtilsIR.reformatXML("./data/docid.documento.xml", "./data/documenti.xml");
			Parser.parserDocumenti("./data/documenti.xml", docsCitazioni, docsTitolo, docsAbstract);
			
			// Creo un arraylist con tutti i documenti della collezione
			HashMap<Integer, Documento> docs = new HashMap<Integer, Documento>();
			Set<Integer> docsid = docsTitolo.keySet();
			for(int k: docsid) {
				Documento doc = new Documento(k, docsTitolo.get(k), docsAbstract.get(k), docsKeyWords.get(k), docsStems.get(k), docsCitazioni.get(k));
				docs.put(k, doc);
			}
			Formula ff = new Formula();
			ff.calcolaFormula(docs, "./data/pesi.txt");
			
			// FINE LABORATORIO 2 //
			
			// INIZIO LABORATORIO 3 //
			
			HashMap<Integer, HashMap<String, Integer>> keywordsQuery = Parser.parserQueryKeyword("./data/query-keyword.txt");
			
			// FINE LABORATORIO 3 //
			
			System.out.println("Fine");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
