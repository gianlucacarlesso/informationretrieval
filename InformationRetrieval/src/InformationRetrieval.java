import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;


public class InformationRetrieval {
	public static void main(String[] args) {
		try {
			HashMap<Integer, String> docsTitolo = Parser.parserDocumentoTitolo("./data/docid.documento-testo.txt");
			HashMap<Integer, HashMap<String, Integer>> docsKeyWords = Parser.parserDocumentoKeyWords("./data/freq.docid.word.txt");
			// Ho commentato provvisoriamente per velocizzare le prove, dando al documento come stem le sue parole chiave � [da decommentare]
			HashMap<Integer, HashMap<String, Integer>> docsStems = Parser.parserDocumentoKeyWords("./data/freq.docid.stem.txt");
			
		    // UtilsIR.reformatXML("./data/docid.documento.xml", "./data/documenti.xml");
			HashMap<Integer, ArrayList<Integer>> docsCitazioni = Parser.parserCitazioni("./data/documenti.xml");
			
			// Creo un arraylist con tutti i documenti della collezione
			HashMap<Integer, Documento> docs = new HashMap<Integer, Documento>();
			Set<Integer> docsid = docsTitolo.keySet();
			for(int k: docsid) {
				//if(k==1) { // Solo per debug
				Documento doc = new Documento(k, docsTitolo.get(k), docsKeyWords.get(k), docsStems.get(k), docsCitazioni.get(k));
				docs.put(k, doc);
				//}
			}
			Formula ff = new Formula();
			ff.calcolaFormula(docs, "./data/pesi.txt");
			
			System.out.println("Fine");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
