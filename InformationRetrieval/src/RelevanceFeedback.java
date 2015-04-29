import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RelevanceFeedback {
	private HashMap<Integer, HashMap<String, Double>> keywordsQuery;
	private HashMap<Integer, Documento> docs;
	private HashMap<Integer, HashMap<String, Double>> pesiKeywordDocumenti;

	public RelevanceFeedback(
			HashMap<Integer, HashMap<String, Double>> _keywordsQuery,
			HashMap<Integer, Documento> _docs,
			HashMap<Integer, HashMap<String, Double>> _pesiKeywordDocumenti) {

		keywordsQuery = _keywordsQuery;
		docs = _docs;
		pesiKeywordDocumenti = _pesiKeywordDocumenti;

	}

	private void completaQueriesKeywordMancanti(
			HashMap<Integer, List<Map.Entry<Integer, Double>>> reperimento,
			int M) {

		Set<Integer> queries = keywordsQuery.keySet();

		// Per ogni query:
		for (Integer queryId : queries) {

			// Recupero i primi M documenti reperiti da cui ricavare le keywords
			// per Q'
			List<Map.Entry<Integer, Double>> docReperiti = reperimento
					.get(queryId);

			for (int i = 0; i < docReperiti.size() && i < M; i++) {
				Set<String> keys = docs.get(docReperiti.get(i).getKey())
						.mieKeyword().keySet();
				for (String key : keys) {
					if (!keywordsQuery.get(queryId).containsKey(key)) {
						keywordsQuery.get(queryId).put(key, 0.0);
					}
				}
			}
		}

	}

	public void generaNuoveQueriesRF_esplicito(
			HashMap<Integer, List<Map.Entry<Integer, Double>>> reperimento,
			int M, String pathQrels) throws IOException {
		completaQueriesKeywordMancanti(reperimento, M);

		HashMap<Integer, ArrayList<Integer>> docQrels = Parser
				.parserQrels(pathQrels);

		Set<Integer> queries = keywordsQuery.keySet();

		// Per ogni query:
		for (Integer queryId : queries) {
			// Recupero le keywords della query
			Set<String> keywords = keywordsQuery.get(queryId).keySet();

			for (String key : keywords) {
				// Recupero i primi M documenti reperiti
				List<Map.Entry<Integer, Double>> docReperiti = reperimento
						.get(queryId);

				Double coeffPositivo = 0.0;
				Double coeffNegativo = 0.0;
				for (int i = 0; i < docReperiti.size() && i < M; i++) {
					
					// il documento i-esimo fa parte dei rilevanti?
					if (docQrels.get(queryId)!=null && docQrels.get(queryId).contains(docReperiti.get(i).getKey())) {
						if (docs.get(docReperiti.get(i).getKey()).mieKeyword()
								.containsKey(key)) {
							// La keyword del doc e' presente quindi
							// coefficiente positivo
							coeffPositivo += 1;
						}
					} else {
						if (docs.get(docReperiti.get(i).getKey()).mieKeyword()
								.containsKey(key)) {
							// La keyword del doc NON e' presente quindi
							// coefficiente positivo
							coeffNegativo += 1;
						}
					}
				}
				
				if(docQrels.get(queryId) != null) {
					coeffPositivo /= docQrels.get(queryId).size();
					coeffNegativo /= (M - docQrels.get(queryId).size());
				} else {
					coeffPositivo = 0.0;
					coeffNegativo /= M;
				}

				
				Double coeff = (double) keywordsQuery.get(queryId).get(key) + coeffPositivo - coeffNegativo;
				
				keywordsQuery.get(queryId).put(key, coeff);

			}

		}
	}
	
	public void generaNuoveQueriesRF_pseudo(
			HashMap<Integer, List<Map.Entry<Integer, Double>>> reperimento,
			int M, int N, String pathQrels) throws IOException {
		completaQueriesKeywordMancanti(reperimento, M);

		Set<Integer> queries = keywordsQuery.keySet();
		
		// Prendo i primi N come rilevanti
		HashMap<Integer, ArrayList<Integer>> docQrels = new HashMap<Integer, ArrayList<Integer>>();
		
		for (Integer queryId : queries) {
			docQrels.put(queryId, new ArrayList<Integer>());
			for(int i = 0; i < N && i < reperimento.get(queryId).size(); i++) {
				docQrels.get(queryId).add(reperimento.get(queryId).get(i).getKey());
			}
		}
		

		// Per ogni query:
		for (Integer queryId : queries) {
						// Recupero le keywords della query
			Set<String> keywords = keywordsQuery.get(queryId).keySet();

			for (String key : keywords) {
				// Recupero i primi M documenti reperiti
				List<Map.Entry<Integer, Double>> docReperiti = reperimento
						.get(queryId);

				Double coeffPositivo = 0.0;
				Double coeffNegativo = 0.0;
				for (int i = 0; i < docReperiti.size() && i < M; i++) {
					
					// il documento i-esimo fa parte dei rilevanti?
					if (docQrels.get(queryId)!=null && docQrels.get(queryId).contains(docReperiti.get(i).getKey())) {
						if (docs.get(docReperiti.get(i).getKey()).mieKeyword()
								.containsKey(key)) {
							// La keyword del doc e' presente quindi
							// coefficiente positivo
							coeffPositivo += 1;
						}
					} else {
						if (docs.get(docReperiti.get(i).getKey()).mieKeyword()
								.containsKey(key)) {
							// La keyword del doc NON e' presente quindi
							// coefficiente positivo
							coeffNegativo += 1;
						}
					}
				}
				
				if(docQrels.get(queryId) != null) {
					coeffPositivo /= docQrels.get(queryId).size();
					coeffNegativo /= (M - docQrels.get(queryId).size());
				} else {
					coeffPositivo = 0.0;
					coeffNegativo /= M;
				}

				
				Double coeff = (double) keywordsQuery.get(queryId).get(key) + coeffPositivo - coeffNegativo;
				
				keywordsQuery.get(queryId).put(key, coeff);

			}

		}
	}
}
