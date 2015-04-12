import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class InformationRetrieval {
	public static void main(String[] args) {
		try {
			HashMap<Integer, String> docsTitolo = parser.parserDocumentoTitolo("./data/docid.documento-testo.txt");
			HashMap<Integer, HashMap<String, Integer>> docsKeyWords = parser.parserDocumentoKeyWords("./data/freq.docid.word.txt");
			// Ho commentato provvisoriamente per velocizzare le prove, dando al documento come stem le sue parole chiave è [da decommentare]
			// HashMap<Integer, HashMap<String, Integer>> docsStems = parser.parserDocumentoKeyWords("./data/freq.docid.stem.txt");
			
		    // UtilsIR.reformatXML("./data/docid.documento.xml", "./data/documenti.xml");
			HashMap<Integer, ArrayList<Integer>> docsCitazioni = parser.parserCitazioni("./data/documenti.xml");
			
			// una prova di costruzione di un documento; alla fine lo faremo per ogni id. Notare che ho usato per gli stem lo stesso metodo 
			// delle parole chiave, solo per velocità in queste prove.
			Documento prova = new Documento(2, docsTitolo.get(2), docsKeyWords.get(2), docsKeyWords.get(2), docsCitazioni.get(2));
			formula ff = new formula(0);
			ff.pesoTitolo(prova);
			
			System.out.println("Fine");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
