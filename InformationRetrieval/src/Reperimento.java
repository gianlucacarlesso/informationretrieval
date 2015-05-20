import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
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

import org.jblas.DoubleMatrix;

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
				if ((!keywordsQuery.get(queryId).containsKey(kDoc) || (keywordsQuery
						.get(queryId).containsKey(kDoc) && keywordsQuery.get(
						queryId).get(kDoc) == 0.0))
						&& stemQuery.get(queryId).contains(
								doc.getStemKeyWords(kDoc))) {

					peso += pesiKeywords.get(kDoc);
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
		return scriviPesi(path, reperimento, maxDocsReperiti, 1, "pippo");
	}

	public HashMap<Integer, List<Map.Entry<Integer, Double>>> scriviPesi(
			String path,
			HashMap<Integer, HashMap<Integer, Double>> reperimento,
			int maxDocReperiti, int npos, String QualeParte) throws IOException {
		FileWriter writer = new FileWriter(path);

		HashMap<Integer, List<Map.Entry<Integer, Double>>> ranking = new HashMap<Integer, List<Entry<Integer, Double>>>();

		// Prendo i documenti rilevanti per implementare l'N variabile dato dal
		// numero di doc rilevanti per ogni query
		HashMap<Integer, ArrayList<Integer>> docQrels = Parser
				.parserQrels("./data/qrels-originale.txt");
		int documentiRilevanti = 0;

		System.out.println(docQrels.keySet());
		Set<Integer> queriesId = reperimento.keySet();
		System.out.println(queriesId);
		// Scorro tutte le query
		for (Integer queryId : queriesId) {
			// Procedo per ogni

			// in qrels non ci sono tutte le query
			if (docQrels.keySet().contains(queryId)) {
				documentiRilevanti = docQrels.get(queryId).size();
			} else {
				documentiRilevanti = 20;
			}
			// solo per metodo 2 (N variabile)
			if (QualeParte == "prima") {
				maxDocReperiti = documentiRilevanti;
				npos = 1;
			} else if (QualeParte == "seconda") {
				maxDocReperiti = 1000 - documentiRilevanti + 1;
				npos = documentiRilevanti;
			}

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

			String backspace = "\n";
			for (int i = 0; i < entrylist.size()
					&& (i < maxDocReperiti || maxDocReperiti == 0); i++) {
				// Calcolo i diversi coefficienti

				String queryID = "";
				if (queryId < 10) {
					queryID = "0";
				}
				queryID += queryId;

				// if ((i + 1) < entrylist.size()) {
				// backspace = "\n";
				// } else {
				// backspace = "";
				// }

				writer.write(queryID + " Q0 " + entrylist.get(i).getKey() + " "
						+ (i + npos) + " " + entrylist.get(i).getValue() + " "
						+ " GR11R0" + backspace);
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
				if (keywordsThisQuery.contains(kDoc)
						&& keywordsQuery.get(queryId).get(kDoc) != 0.0) {
					peso = peso
							+ (pesiKeywords.get(kDoc)
									* keywordsQuery.get(queryId).get(kDoc) + Math
										.exp(keywordsQuery.get(queryId).get(
												kDoc)));
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
			HashMap<Integer, List<Map.Entry<Integer, Double>>> reperimento,
			int N, int M, String pathQrels) throws IOException {
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
			List<Map.Entry<Integer, Double>> docReperiti = reperimento
					.get(queryId);
			ArrayList<Integer> docSelezionati = new ArrayList<Integer>();

			for (int i = 0; i < docReperiti.size() && i < N; i++) {
				docSelezionati.add(docReperiti.get(i).getKey());
			}

			// Conto quanti degli N documenti sono rilevanti
			ArrayList<Integer> docRilevanti = new ArrayList<Integer>();
			for (int i = 0; i < docSelezionati.size(); i++) {
				if (docQrels.containsKey(queryId)
						&& docQrels.get(queryId)
								.contains(docSelezionati.get(i))) {
					docRilevanti.add(docSelezionati.get(i));
				}
			}

			reperimentoRF.put(queryId, new HashMap<Integer, Double>());

			// Per ogni documento:
			for (Integer docId : documenti) {
				pesokeyword = getPeso(queryId, docId);
				pesoStem = getPesoStem(queryId, docId);

				double peso = pesokeyword + pesoStem;

				if (docRilevanti.contains(docId) && !docRilevanti.isEmpty()) {
					peso += peso * (1.0 / docRilevanti.size());
				} else {
					peso -= peso * (1.0 / (M - docRilevanti.size()));
				}
				reperimentoRF.get(queryId).put(docId, peso);
			}

		}

		// salvo i pesi in un file
		scriviPesi(path, reperimentoRF, M, 1, "pippo");
		return reperimentoRF;
	}

	public void eseguiReperimentoPageRank(double d, int M, double precisione,
			HashMap<Integer, List<Map.Entry<Integer, Double>>> reperimento,
			String path) throws IOException {
		PageRank pr = new PageRank(docs, d, precisione);
		HashMap<Integer, Double> pr_valori = pr.getPageRank();

		HashMap<Integer, HashMap<Integer, Double>> reperimentoPR = new HashMap<Integer, HashMap<Integer, Double>>();

		Set<Integer> keys = reperimento.keySet();
		for (Integer key : keys) {
			reperimentoPR.put(key, new HashMap<Integer, Double>());
			List<Map.Entry<Integer, Double>> listReperiti = reperimento
					.get(key);
			for (int i = 0; i < listReperiti.size(); i++) {
				double pr_valore = 0.0;
				if (pr_valori.get(listReperiti.get(i).getKey()) != null
						&& listReperiti.get(i) != null
						&& pr_valori.containsKey(listReperiti.get(i).getKey())) {
					pr_valore = pr_valori.get(listReperiti.get(i).getKey());
				}

				reperimentoPR.get(key).put(listReperiti.get(i).getKey(),
						listReperiti.get(i).getValue() * pr_valore);
			}
		}

		scriviPesi(path, reperimentoPR, M, 1, "pippo");

	}

	public void eseguiReperimentoLSA(int N, int M,
			HashMap<Integer, List<Map.Entry<Integer, Double>>> reperimento,
			String path, HashMap<Integer, Documento> docs,
			HashMap<Integer, HashMap<String, Double>> keywordsQuery)
			throws IOException, InterruptedException {
		LSA lsa = new LSA(docs, keywordsQuery, reperimento);

		HashMap<Integer, HashMap<Integer, Double>> reperimentoLSA = new HashMap<Integer, HashMap<Integer, Double>>();

		HashMap<Integer, HashMap<Integer, Double>> reperimentoLSA_tmp1 = new HashMap<Integer, HashMap<Integer, Double>>();
		HashMap<Integer, HashMap<Integer, Double>> reperimentoLSA_tmp2 = new HashMap<Integer, HashMap<Integer, Double>>();

		// Prendo i documenti rilevanti per implementare l'N variabile dato dal
		// numero di doc rilevanti per ogni query
		HashMap<Integer, ArrayList<Integer>> docQrels = Parser
				.parserQrels("./data/qrels-originale.txt");

		// Per ogni query
		Set<Integer> keys = reperimento.keySet();
		for (Integer key : keys) {
			if (docQrels.keySet().contains(key)) {
				N = docQrels.get(key).size();
			} else {
				N = 20;
			}

			DoubleMatrix pesiLSA = lsa.eseguiLSA(N, key);
			reperimentoLSA.put(key, new HashMap<Integer, Double>());
			List<Map.Entry<Integer, Double>> listReperiti = reperimento
					.get(key);
			for (int i = 0; i < listReperiti.size(); i++) {
				if (i < N) {
					System.out.println(pesiLSA.get(i,0));
					double lsa_valore = 1+pesiLSA.get(i, 0);//Math.exp(pesiLSA.get(i, 0));
					reperimentoLSA.get(key).put(listReperiti.get(i).getKey(),
							listReperiti.get(i).getValue() * lsa_valore);

					if (!reperimentoLSA_tmp1.containsKey(key)) {
						reperimentoLSA_tmp1.put(key,
								new HashMap<Integer, Double>());
					}
					reperimentoLSA_tmp1.get(key).put(
							listReperiti.get(i).getKey(),
							listReperiti.get(i).getValue() * lsa_valore);
				} else {
					reperimentoLSA.get(key).put(listReperiti.get(i).getKey(),
							listReperiti.get(i).getValue());

					if (!reperimentoLSA_tmp2.containsKey(key)) {
						reperimentoLSA_tmp2.put(key,
								new HashMap<Integer, Double>());
					}
					reperimentoLSA_tmp2.get(key).put(
							listReperiti.get(i).getKey(),
							listReperiti.get(i).getValue());
				}
			}
		}

		scriviPesi("./data/tmp1.txt", reperimentoLSA_tmp1, N, 1, "prima");
		scriviPesi("./data/tmp2.txt", reperimentoLSA_tmp2, M - N + 1, N,
				"seconda");
		concatFile("./data/tmp1.txt", "./data/tmp2.txt", path, N, M - N + 1);

		// new File("./data/tmp1.txt").delete();
		// new File("./data/tmp2.txt").delete();

	}

	public void concatFile(String path1, String path2, String outPath, int N,
			int M) throws IOException {
		BufferedReader inFile1 = new BufferedReader(new FileReader(path1));
		BufferedReader inFile2 = new BufferedReader(new FileReader(path2));

		// Prendo i documenti rilevanti per implementare l'N variabile dato dal
		// numero di doc rilevanti per ogni query
		HashMap<Integer, ArrayList<Integer>> docQrels = Parser
				.parserQrels("./data/qrels-originale.txt");

		// File to write
		BufferedWriter outFile = new BufferedWriter(new FileWriter(outPath));
		String lineFile1 = "";
		String lineFile2 = "";
		int countFile1 = 20;
		int countFile2 = 0;
		int queryEsaminata = 1;
		int numeroDocRilevanti = 20;
		while ((lineFile1 = inFile1.readLine()) != null) {

			if (countFile1 + 1 > numeroDocRilevanti) {
				countFile1 = 0;
				
				if (docQrels.keySet().contains(queryEsaminata)) {
					numeroDocRilevanti = docQrels.get(queryEsaminata).size();
				} else {
					numeroDocRilevanti = 20;
				}
			}

			countFile1++;

			outFile.write(lineFile1 + "\n");

			if (countFile1 == numeroDocRilevanti) {
				countFile2 = 0;
				while ((lineFile2 = inFile2.readLine()) != null
						&& countFile2 < (1000 - numeroDocRilevanti + 1)) {
					countFile2++;

					outFile.write(lineFile2 + "\n");
				}

				queryEsaminata = queryEsaminata + 1;
			}
		}

		inFile1.close();
		inFile2.close();
		outFile.close();
	}
	
	public void eseguiReperimentoHITS(int N, int M, 
			HashMap<Integer, List<Map.Entry<Integer, Double>>> reperimento,
			String path, HashMap<Integer, Documento> docs,
			HashMap<Integer, HashMap<String, Double>> keywordsQuery)
			throws IOException, InterruptedException {
		HITS hits = new HITS(docs, reperimento);

		HashMap<Integer, HashMap<Integer, Double>> reperimentoHITS = new HashMap<Integer, HashMap<Integer, Double>>();

		HashMap<Integer, HashMap<Integer, Double>> reperimentoHITS_tmp1 = new HashMap<Integer, HashMap<Integer, Double>>();
		HashMap<Integer, HashMap<Integer, Double>> reperimentoHITS_tmp2 = new HashMap<Integer, HashMap<Integer, Double>>();

		// Per ogni query
		Set<Integer> keys = reperimento.keySet();
		for (Integer queryId : keys) {
			// Calcolo il coefficiente dato da HITS per la query in esame
			HashMap<Integer, Double> pesiHITS = hits.calcoloPesiCentralitaAutorevolezza(queryId, N);
			
			reperimentoHITS.put(queryId, new HashMap<Integer, Double>());
			List<Map.Entry<Integer, Double>> listReperiti = reperimento
					.get(queryId);
			
			for (int i = 0; i < listReperiti.size(); i++) {
				if (i < N) {
					// Recupero il coefficiente dato da hits per i primi N doc
					double hits_valore = 1 + pesiHITS.get(listReperiti.get(i).getKey());
					
					reperimentoHITS.get(queryId).put(listReperiti.get(i).getKey(),
							listReperiti.get(i).getValue() * hits_valore);

					if (!reperimentoHITS_tmp1.containsKey(queryId)) {
						reperimentoHITS_tmp1.put(queryId,
								new HashMap<Integer, Double>());
					}
					reperimentoHITS_tmp1.get(queryId).put(
							listReperiti.get(i).getKey(),
							listReperiti.get(i).getValue() * hits_valore);
				} else {
					reperimentoHITS.get(queryId).put(listReperiti.get(i).getKey(),
							listReperiti.get(i).getValue());

					if (!reperimentoHITS_tmp2.containsKey(queryId)) {
						reperimentoHITS_tmp2.put(queryId,
								new HashMap<Integer, Double>());
					}
					reperimentoHITS_tmp2.get(queryId).put(
							listReperiti.get(i).getKey(),
							listReperiti.get(i).getValue());
				}
			}
		}

		scriviPesi("./data/tmp1.txt", reperimentoHITS_tmp1, N, 1, "prima");
		scriviPesi("./data/tmp2.txt", reperimentoHITS_tmp2, M - N + 1, N,
				"seconda");
		concatFile("./data/tmp1.txt", "./data/tmp2.txt", path, N, M - N + 1);

		new File("./data/tmp1.txt").delete();
		new File("./data/tmp2.txt").delete();

	}
}
