import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.lang.Math;

public class Formula {
	private double kTitolo;

	// private int tfdif;
	// private int kStem;
	// private int Kcitazioni;
	// private static ArrayList<Integer> pesoT;

	// ANCORA DA FINIRE
	public Formula() {

	}

	private double pesoTitolo(Documento doc, String keyword) {
		String titolo = doc.mioTitolo();

		// Evito di confrontare "Parola" con "parola" e dedurre che sono
		// diverse
		titolo = titolo.toLowerCase();
		keyword = keyword.toLowerCase();

		if (titolo.contains(keyword)) {
			kTitolo = 1;
		} else if (titolo.contains(doc.getStemKeyWords(keyword).toLowerCase())) {
			// Cerco lo stem nel titolo
			kTitolo = 0.5;
		} else {
			kTitolo = 0;
		}

		return kTitolo;
	}

	// Formula per calcolare il peso dello stem per una keyword
	private double pesoStemKeyword(Documento doc, String key) {
		int numStemKeyword = doc.getNumStemKeyword(key);
		int frequenzaKeyword = doc.getFrequenzaKeyword(key);
		int numFreqKeywordsTotali = doc.getNumFreqKeywordsTotali();

		double kstem = (numStemKeyword - frequenzaKeyword)
				/ numFreqKeywordsTotali;
		return kstem;
	}

	// Conto i documenti citati che contengono la keyword
	private int numeroDocCitatiConKeywords(HashMap<Integer, Documento> docs,
			Documento doc, String key) {
		ArrayList<Integer> citazioni = doc.mieCitazioni();

		Documento docTmp = null;
		int numeroDocsCitati = 0;
		for (int i = 0; i < citazioni.size(); i++) {
			docTmp = docs.get(citazioni.get(i));

			// Se la keyword non è presente nel documento citato, se cerco di recuperarla ottengo null
			Object valore = docTmp.mieKeyword().get(key);
			if (valore != null) {
				numeroDocsCitati++;
			}
		}

		return numeroDocsCitati;
	}

	// Calcolo il peso delle citazioni
	private double pesoCitazioni(HashMap<Integer, Documento> docs,
			Documento doc, String key) {
		int numeroDocumentiCitati = doc.mieCitazioni().size();
		int numDocCitatiConKeywords = numeroDocCitatiConKeywords(docs, doc, key);
		double valore = doc.getFrequenzaKeyword(key)
				* Math.log(numeroDocumentiCitati / numDocCitatiConKeywords);

		return valore;
	}

	public HashMap<Integer, HashMap<String, Double>> calcolaFormula(
			HashMap<Integer, Documento> docs, String path) throws IOException {
		
		double titolo, tfidf, kstem, kcitazioni = 0;
		HashMap<Integer, HashMap<String, Double>> pesi = new HashMap<Integer, HashMap<String, Double>>();

		Set<Integer> docsid = docs.keySet();
		Documento documento = null;
		// Calcola la formula per ogni documento
		for (Integer docid : docsid) {
			documento = docs.get(docid);

			pesi.put(docid, new HashMap<String, Double>());

			// Procedo per ogni keywords di un documento
			Set<String> keys = documento.mieKeyword().keySet();
			for (String key : keys) {
				// Calcolo i diversi coefficienti
				titolo = pesoTitolo(documento, key);
				tfidf = 0; // TODO: aggiungere metodo
				kstem = pesoStemKeyword(documento, key);
				kcitazioni = pesoCitazioni(docs, documento, key);

				double peso = titolo + tfidf + kstem + kcitazioni;

				pesi.get(docid).put(key, peso);
			}

		}

		// salvo i pesi in un file
		scriviPesi(path, pesi);
		return pesi;
	}

	public void scriviPesi(String path,
			HashMap<Integer, HashMap<String, Double>> pesi) throws IOException {
		FileWriter writer = new FileWriter(path);

		Set<Integer> pesiId = pesi.keySet();
		// Scorro tutti i documenti
		for (Integer pesoId : pesiId) {
			// Procedo per ogni keywords
			Set<String> keywords = pesi.get(pesoId).keySet();

			for (String key : keywords) {
				// Calcolo i diversi coefficienti
				Double valorePeso = pesi.get(pesoId).get(key);

				writer.write(key + " " + pesoId + " " + valorePeso + "\n");
			}
		}

		writer.close();
	}
}