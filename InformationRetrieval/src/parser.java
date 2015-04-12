import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class parser {
	private HashMap<String, Integer> keywords;
	private HashMap<String, Integer> stems;
	private ArrayList<Integer> citazioni;
	
	public static HashMap<Integer, String> parserDocumentoTitolo(
			String pathDocumento) throws IOException {
		FileReader reader = new FileReader(pathDocumento);
		BufferedReader bufferReader = new BufferedReader(reader);
		String linea = "";
		HashMap<Integer, String> docsTitolo = new HashMap<Integer, String>();
		while ((linea = bufferReader.readLine()) != null) {
			String[] token = linea.split("	 ");
			docsTitolo.put(new Integer(token[0]), token[1]);

		}
		bufferReader.close();
		return docsTitolo;
	}

	public static HashMap<Integer, HashMap<String, Integer>> parserDocumentoKeyWords(String pathDocumento) throws IOException {
		FileReader reader = new FileReader(pathDocumento);
		BufferedReader bufferReader = new BufferedReader(reader);		
		String linea = "";
		HashMap<Integer, HashMap<String, Integer>> docsKeyWords = new HashMap<Integer, HashMap<String, Integer>>();
		
		while ((linea = bufferReader.readLine()) != null) {
			
			String[] token = linea.split(" ");
			// L'hashmap interiore, da completare con un secondo ciclo while
			HashMap<String, Integer> intermedia = new HashMap<String, Integer>();
			
			if (!docsKeyWords.containsKey(token[1])) {
				// Il nuovo reader per completare l'hashmap "interna" (cioè "intermedia").
				FileReader reader2 = new FileReader(pathDocumento);
				BufferedReader bufferReader2 = new BufferedReader(reader2);
				String linea2 = "";
				while ((linea2 = bufferReader2.readLine()) != null) {
					String[] token2 = linea2.split(" ");
					
					if (token[1].equals(token2[1])){
						intermedia.put(token2[2], new Integer(token2[0]));
					}
					// Per velocizzare parzialmente il ciclo while: se "intermedia" non è vuoto e e se l'id in esame in questo passo
					// (cioè token2[1]) è diverso da token[1], allora vuol dire che contiene già tutti i valori associati all'id.
					// Ovviamente questo velocizza l'operazione solo fino a metà file, perché dopo circa la metà bisogna comunque prima
					// leggere molte righe.
					if (intermedia.keySet().isEmpty()==false && !token[1].equals(token2[1])){
						break;
					}
				}
				docsKeyWords.put(new Integer(token[1]), intermedia);
				bufferReader2.close();
				// Un controllo per vedere quanto ci mette a completare il ciclo while.
				System.out.println(token[1]);
			}	
		}
		bufferReader.close();
		return docsKeyWords;
		}

	public static HashMap<Integer, ArrayList<Integer>> parserCitazioni(String pathDocumento) {
		
		HashMap<Integer, ArrayList<Integer>> docsCitazioni = new HashMap<Integer, ArrayList<Integer>>();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {

			// Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			// parse using builder to get DOM representation of the XML file
			Document dom = db.parse(pathDocumento);
			
			Element docEle = dom.getDocumentElement();

			//get a nodelist of elements
			NodeList docs = docEle.getElementsByTagName("DOC");

			if(docs != null && docs.getLength() > 0) {
				for(int i = 0 ; i < docs.getLength(); i++) {
					Node nNode = docs.item(i);
					Integer docid = null;
					String sCitazioni = null;
					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
						Element eElement = (Element) nNode;
						docid = new Integer(eElement.getElementsByTagName("DOCID").item(0).getTextContent().trim());
						
						// System.out.println(docid);
						
						sCitazioni = eElement.getElementsByTagName("CITATIONS").item(0).getTextContent();
						ArrayList<Integer> citazioni = new ArrayList<Integer>();
						sCitazioni = sCitazioni.trim();
						String[] token = sCitazioni.split(" +");

						for(int j = 0; j < token.length; j++) {
							//System.out.println(token[j]);
							Integer idCitazione = new Integer(token[j].trim());
							
							if(docid != idCitazione && !citazioni.contains(idCitazione)) {
								citazioni.add(idCitazione);
							}
						}
						
						docsCitazioni.put(docid, citazioni);
					} 
					
				}
			}

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (SAXException se) {
			se.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		
		return docsCitazioni;
	}
}

