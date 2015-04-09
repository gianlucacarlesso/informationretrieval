import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class InformationRetrieval {
	public static void main(String[] args) {
		try {
			HashMap<Integer, String> docsTitolo = Documento.parserDocumentoTitolo("./data/docid.documento-testo.txt");
			HashMap<Integer, HashMap<String, Integer>> docsKeyWords = Documento.parserDocumentoKeyWords("./data/freq.docid.word.txt");
			HashMap<Integer, HashMap<String, Integer>> docsStems = Documento.parserDocumentoKeyWords("./data/freq.docid.stem.txt");
			
		//	UtilsIR.reformatXML("./data/docid.documento.xml", "./data/documenti.xml");
			HashMap<Integer, ArrayList<Integer>> docsCitazioni = Documento.parserCitazioni("./data/documenti.xml");
			System.out.println("Fine");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
