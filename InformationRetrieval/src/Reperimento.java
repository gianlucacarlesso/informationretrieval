import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Reperimento {
	private HashMap<Integer, HashMap<String, Double>> pesiKeywordDocumenti;
	private HashMap<Integer, HashMap<String, Double>> keywordsQuery;
	private HashMap<Integer, ArrayList<String>> stemQuery;
	private HashMap<Integer, Documento> docs;

	public Reperimento(
			HashMap<Integer, HashMap<String, Double>> _pesiKeywordDocumenti,
			HashMap<Integer, HashMap<String, Double>> _keywordsQuery,
			HashMap<Integer, ArrayList<String>> _stemQuery,
			HashMap<Integer, Documento> _docs) {
		pesiKeywordDocumenti = _pesiKeywordDocumenti;
		keywordsQuery = _keywordsQuery;
		stemQuery = _stemQuery;
		docs = _docs;
	}

	public double getPesoStem(int queryId, int docId) throws IOException {
		double peso = 0;
		double numeroKeyWordsQuery = 0;
		if (!keywordsQuery.containsKey(queryId)) {
			throw new IOException("L'id della query specificata non esiste");
		} else if (!pesiKeywordDocumenti.containsKey(docId)) {
			throw new IOException("L'id del documento specificato non esiste");

		} else {
			Documento doc = docs.get(new Integer(docId));
			HashMap<String, Double> pesiKeywords = pesiKeywordDocumenti
					.get(new Integer(docId));
			Set<String> keywordsDoc = pesiKeywords.keySet();

			HashMap<String, Double> keywQuery = keywordsQuery.get(queryId);
			Set<String> keywordsThisQuery = keywQuery.keySet();
			numeroKeyWordsQuery = keywordsThisQuery.size();

			for (String kDoc : keywordsDoc) {
				// Verifico che la keyword del documento NON sia presente nelle
				// keyword della query e che lo stem di
				// tale keyword sia presente nello stem delle keyword della
				// query
				if ((!keywordsQuery.get(queryId).containsKey(kDoc) || (keywordsQuery.get(queryId).containsKey(kDoc) && keywordsQuery.get(queryId).get(kDoc) == 0.0))
						&& stemQuery.get(queryId).contains(
								doc.getStemKeyWords(kDoc))) {
					
					peso += pesiKeywords.get(kDoc) ;
				}
			}
		}

		if (numeroKeyWordsQuery > 0) {
			return peso * numeroKeyWordsQuery;
		} else {
			return peso;
		}

	}

	public HashMap<Integer, List<Map.Entry<Integer, Double>>> eseguiReperimento(
			String path, int maxDocsReperiti) throws IOException {
		// queryId -> (docId; peso)
		HashMap<Integer, HashMap<Integer, Double>> reperimento = new HashMap<Integer, HashMap<Integer, Double>>();

		Set<Integer> queries = keywordsQuery.keySet();
		Set<Integer> documenti = pesiKeywordDocumenti.keySet();
		for (Integer queryId : queries) {
			double pesokeyword = 0;
			double pesoStem = 0;

			reperimento.put(queryId, new HashMap<Integer, Double>());

			// Per ogni query:
			for (Integer docId : documenti) {

				// Per ogni documento:
				pesokeyword = getPeso(queryId, docId);
				pesoStem = getPesoStem(queryId, docId);

				reperimento.get(queryId).put(docId, pesokeyword + pesoStem);
			}
		}

		// salvo i pesi in un file
		return scriviPesi(path, reperimento, maxDocsReperiti);
	}

	public HashMap<Integer, List<Map.Entry<Integer, Double>>> scriviPesi(
			String path,
			HashMap<Integer, HashMap<Integer, Double>> reperimento,
			int maxDocReperiti) throws IOException {
		FileWriter writer = new FileWriter(path);

		HashMap<Integer, List<Map.Entry<Integer, Double>>> ranking = new HashMap<Integer, List<Entry<Integer, Double>>>();

		Set<Integer> queriesId = reperimento.keySet();
		// Scorro tutte le query
		for (Integer queryId : queriesId) {
			// Procedo per ogni

			Comparator<Map.Entry<Integer, Double>> comp = new Comparator<Map.Entry<Integer, Double>>() {

				@Override
				public int compare(Entry<Integer, Double> o1,
						Entry<Integer, Double> o2) {
					return Double.compare(o2.getValue(), o1.getValue());
				}
			};

			Set<Map.Entry<Integer, Double>> entries = reperimento.get(queryId)
					.entrySet();
			List<Map.Entry<Integer, Double>> entrylist = new ArrayList<Map.Entry<Integer, Double>>(
					entries);
			Collections.sort(entrylist, comp);

			ranking.put(queryId, entrylist);

			String backspace = "";
			for (int i = 0; i < entrylist.size()
					&& (i < maxDocReperiti || maxDocReperiti == 0); i++) {
				// Calcolo i diversi coefficienti

				String queryID = "";
				if (queryId < 10) {
					queryID = "0";
				}
				queryID += queryId;

				if ((i + 1) < entrylist.size()) {
					backspace = "\n";
				} else {
					backspace = "";
				}

				writer.write(queryID + " Q0 " + entrylist.get(i).getKey() + " "
						+ (i + 1) + " " + entrylist.get(i).getValue() + " "
						+ " GR11R4" + backspace);
			}
		}

		writer.close();

		return ranking;
	}

	public double getPeso(int queryId, int queryDoc) throws IOException {
		double peso = 0;
		double numeroKeyWordsQuery = 0;
		if (!keywordsQuery.containsKey(queryId)) {
			throw new IOException("L'id della query specificata non esiste");
		} else if (!pesiKeywordDocumenti.containsKey(queryDoc)) {
			throw new IOException("L'id del documento specificato non esiste");

		} else {

			HashMap<String, Double> pesiKeywords = pesiKeywordDocumenti
					.get(new Integer(queryDoc));
			Set<String> keywordsDoc = pesiKeywords.keySet();

			HashMap<String, Double> keywQuery = keywordsQuery.get(queryId);
			Set<String> keywordsThisQuery = keywQuery.keySet();
			numeroKeyWordsQuery = keywordsThisQuery.size();

			for (String kDoc : keywordsDoc) {
				if (keywordsThisQuery.contains(kDoc) && keywordsQuery.get(queryId).get(kDoc) != 0.0) {
					peso = peso
							+ (pesiKeywords.get(kDoc) * keywordsQuery.get(queryId).get(kDoc) + Math.exp(keywordsQuery
									.get(queryId).get(kDoc)));
				}
			}
		}

		if (numeroKeyWordsQuery > 0) {
			return peso * numeroKeyWordsQuery;
		} else {
			return peso;
		}
	}

	public HashMap<Integer, HashMap<Integer, Double>> eseguiRelevanceFeedback(
			String path,
			HashMap<Integer, List<Map.Entry<Integer, Double>>> reperimento, int N,
			int M, String pathQrels) throws IOException {
		// queryId -> (docId; peso)
		HashMap<Integer, HashMap<Integer, Double>> reperimentoRF = new HashMap<Integer, HashMap<Integer, Double>>();

		HashMap<Integer, ArrayList<Integer>> docQrels = Parser
				.parserQrels(pathQrels);

		Set<Integer> queries = keywordsQuery.keySet();
		Set<Integer> documenti = pesiKeywordDocumenti.keySet();
		
		// Per ogni query:
		for (Integer queryId : queries) {
			double pesokeyword = 0;
			double pesoStem = 0;

			// Recupero i primi N documenti reperiti
			List<Map.Entry<Integer, Double>> docReperiti = reperimento.get(queryId);
			ArrayList<Integer> docSelezionati = new ArrayList<Integer>();

			for(int i = 0; i < docReperiti.size() && i < N; i++) {
				docSelezionati.add(docReperiti.get(i).getKey());
			}

			// Conto quanti degli N documenti sono rilevanti
			ArrayList<Integer> docRilevanti = new ArrayList<Integer>();
			for (int i = 0; i < docSelezionati.size(); i++) {
				if (docQrels.containsKey(queryId) && docQrels.get(queryId).contains(docSelezionati.get(i))) {
					docRilevanti.add(docSelezionati.get(i));
				}
			}

			reperimentoRF.put(queryId, new HashMap<Integer, Double>());

			// Per ogni documento:
			for (Integer docId : documenti) {
				pesokeyword = getPeso(queryId, docId);
				pesoStem = getPesoStem(queryId, docId);

				double peso = pesokeyword + pesoStem;
				
				if(docRilevanti.contains(docId) && !docRilevanti.isEmpty()) {
					peso += peso * (1.0 / docRilevanti.size());	
				} else {
					peso -= peso * (1.0 / (M - docRilevanti.size()));
				}
				reperimentoRF.get(queryId).put(docId, peso);
			}
			
		}

		// salvo i pesi in un file
		scriviPesi(path, reperimentoRF, M);
		return reperimentoRF;
	}
}
