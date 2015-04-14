import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.lang.Math;

public class Formula {
	//private double kTitolo;
	//private HashMap<Integer, Documento> docs;

	// private int tfdif;
	// private int kStem;
	// private int Kcitazioni;
	// private static ArrayList<Integer> pesoT;

	// ANCORA DA FINIRE
	public Formula() {
		//docs = _docs;
	}

	private double pesoTitolo(Documento doc, String keyword) {
		String titolo = doc.mioTitolo();
		double kTitolo;
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
	
	private double pesoAbstract(Documento doc, String key){
		String sabstract = doc.mioAbstract();
		sabstract = sabstract.toLowerCase();
		double tfidfAbstract;
		
		if (sabstract.contains(key)){
			double frequenzaKeywordInAbstract = doc.getFrequenzaKeywordAbstract(key);
			double numTotaleKeywordsInAbstract=doc.getNumFreqKeywordsAbstractTotale();
			tfidfAbstract = frequenzaKeywordInAbstract/numTotaleKeywordsInAbstract;
		} else if (sabstract.contains(doc.getStemKeyWords(key).toLowerCase())){
			String keyAbstractStem = doc.getStemKeyWords(key).toLowerCase();
			double frequenzaKeywordInAbstract = doc.getFrequenzaKeywordAbstract(keyAbstractStem);
			double numTotaleKeywordsInAbstract=doc.getNumFreqKeywordsAbstractTotale();
			tfidfAbstract = frequenzaKeywordInAbstract/numTotaleKeywordsInAbstract * 0.5;
		} else {
			tfidfAbstract = 0;
		}
	
		return tfidfAbstract;
	}

	private double pesoTfidf(Documento doc, String key, HashMap<Integer, Documento> Docs){
		double frequenzaKeyword = doc.getFrequenzaKeyword(key);
		double numeroTotaleDocumenti = Docs.size();
		double numDocConKeyword=0;
		
		Set<Integer> docsid = Docs.keySet();
		for (Integer docid : docsid) {
			if (Docs.get(docid).mieKeyword().containsKey(key)){
				numDocConKeyword++;
			}
		}
		double tfidf;
		tfidf = frequenzaKeyword * Math.log(numeroTotaleDocumenti / numDocConKeyword);
		return tfidf;
	}
	
	
	// Formula per calcolare il peso dello stem per una keyword
	private double pesoStemKeyword(Documento doc, String key) {
		int numStemKeyword = doc.getNumStemKeyword(key);
		int frequenzaKeyword = doc.getFrequenzaKeyword(key);
		int numFreqKeywordsTotali = doc.getNumFreqKeywordsTotali();

		double kstem = (numStemKeyword - frequenzaKeyword) * 1.0
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
		
		// Se non ci sono citazioni con keywords
		if(numDocCitatiConKeywords == 0) {
			numeroDocumentiCitati = 1;
			numDocCitatiConKeywords = 1;
		}
		
		double valore = doc.getFrequenzaKeyword(key)
				* Math.log(1.0 * numeroDocumentiCitati / numDocCitatiConKeywords);

		return valore;
	}

	public HashMap<Integer, HashMap<String, Double>> calcolaFormula(
			HashMap<Integer, Documento> docs, String path) throws IOException {
		
		double titolo, tfidfAbstract, tfidf, kstem, kcitazioni = 0;
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
				tfidf = pesoTfidf(documento, key, docs); 
				tfidfAbstract = pesoAbstract(documento, key);
				kstem = pesoStemKeyword(documento, key);
				kcitazioni = pesoCitazioni(docs, documento, key);

				double peso = titolo + tfidf + tfidfAbstract + kstem + kcitazioni;

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