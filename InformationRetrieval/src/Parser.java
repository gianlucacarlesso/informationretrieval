import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Parser {

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

	public static HashMap<Integer, HashMap<String, Integer>> parserDocumentoKeyWords(
			String pathDocumento) throws IOException {
		FileReader reader = new FileReader(pathDocumento);
		BufferedReader bufferReader = new BufferedReader(reader);
		String linea = "";
		HashMap<Integer, HashMap<String, Integer>> docsKeyWords = new HashMap<Integer, HashMap<String, Integer>>();

		while ((linea = bufferReader.readLine()) != null) {
			String[] token = linea.split(" ");

			if (!docsKeyWords.containsKey(new Integer(token[1]))) {
				docsKeyWords.put(new Integer(token[1]),
						new HashMap<String, Integer>());
			}

			// todo controllo
			docsKeyWords.get(new Integer(token[1])).put(token[2],
					new Integer(token[0]));
		}

		bufferReader.close();

		return docsKeyWords;
	}

	public static HashMap<Integer, ArrayList<Integer>> parserDocumenti(
			String pathDocumento, HashMap<Integer, ArrayList<Integer>> docsCitazioni, HashMap<Integer, String> docsTitolo, HashMap<Integer, String> docsAbstract) {

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {

			// Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			// parse using builder to get DOM representation of the XML file
			Document dom = db.parse(pathDocumento);

			Element docEle = dom.getDocumentElement();

			// get a nodelist of elements
			NodeList docs = docEle.getElementsByTagName("DOC");

			if (docs != null && docs.getLength() > 0) {
				for (int i = 0; i < docs.getLength(); i++) {
					Node nNode = docs.item(i);
					Integer docid = null;
					String sCitazioni = null;
					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
						Element eElement = (Element) nNode;
						docid = new Integer(eElement
								.getElementsByTagName("DOCID").item(0)
								.getTextContent().trim());

						// System.out.println(docid);

						// Recupero titolo
						String title = eElement.getElementsByTagName("TITLE")
								.item(0).getTextContent().trim();
						docsTitolo.put(docid, title);
						
						// Recupero abstract
						
						Node abstrac = eElement.getElementsByTagName("ABSTRACT").item(0);
						String sabstract = "";
						if(abstrac != null) {
								sabstract = abstrac.getTextContent().trim();
						}
						docsAbstract.put(docid, sabstract);
						
						// Recupero le citazioni
						sCitazioni = eElement.getElementsByTagName("CITATIONS")
								.item(0).getTextContent();
						ArrayList<Integer> citazioni = new ArrayList<Integer>();
						sCitazioni = sCitazioni.trim();
						String[] token = sCitazioni.split(" +");

						for (int j = 0; j < token.length; j++) {
							// System.out.println(token[j]);
							Integer idCitazione = new Integer(token[j].trim());

							if (docid.compareTo(idCitazione) != 0 && !citazioni.contains(idCitazione)) {
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
	
	public static HashMap<Integer, HashMap<String, Integer>> parserQueryKeyword(
			String pathDocumento) throws IOException {
			FileReader reader = new FileReader(pathDocumento);
			BufferedReader bufferReader = new BufferedReader(reader);
			String linea = "";
			HashMap<Integer, HashMap<String, Integer>> queryKeyWords = new HashMap<Integer, HashMap<String, Integer>>();

			while ((linea = bufferReader.readLine()) != null) {
				String[] token = linea.split(" ");

				if (!queryKeyWords.containsKey(new Integer(token[0]))) {
					queryKeyWords.put(new Integer(token[0]),
							new HashMap<String, Integer>());
				}
				
				// Controllo che la keyword della query non sia gia' presente
				if(!queryKeyWords.get(new Integer(token[0])).containsKey(token[1])) {
					queryKeyWords.get(new Integer(token[0])).put(token[1], 1);
				} else {
					queryKeyWords.get(new Integer(token[0])).put(token[1], queryKeyWords.get(new Integer(token[0])).get(token[1]) + 1);
				}
			}

			bufferReader.close();

			return queryKeyWords;
	}
	
	public static HashMap<Integer, ArrayList<String>> parserQueryStem(
			String pathDocumento) throws IOException {
			FileReader reader = new FileReader(pathDocumento);
			BufferedReader bufferReader = new BufferedReader(reader);
			String linea = "";
			HashMap<Integer, ArrayList<String>> queryKeyWords = new HashMap<Integer, ArrayList<String>>();

			while ((linea = bufferReader.readLine()) != null) {
				String[] token = linea.split(" ");

				if (!queryKeyWords.containsKey(new Integer(token[0]))) {
					queryKeyWords.put(new Integer(token[0]),
							new ArrayList<String>());
				}
				
				// Controllo che la keyword della query non sia gia' presente
				if(!queryKeyWords.get(new Integer(token[0])).contains(token[1])) {
					queryKeyWords.get(new Integer(token[0])).add(token[1]);
				}
			}

			bufferReader.close();

			return queryKeyWords;
	}
	
}
