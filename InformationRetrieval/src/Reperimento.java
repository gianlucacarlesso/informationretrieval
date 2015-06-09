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
	public static int LSA_N_FISSO = 0;
	public static int LSA_N = 20;
	public static int LSA_N_DOCS_RILEVANTI = 1;
	public static int LSA_N_DOCS_PESO_MAX = 2;
	public static double LSA_N_DOCS_PESO_MAX_PERCENT = 10;

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
		return scriviPesi(path, reperimento, maxDocsReperiti, 1);
	}

	public HashMap<Integer, List<Map.Entry<Integer, Double>>> scriviPesi(
			String path,
			HashMap<Integer, HashMap<Integer, Double>> reperimento,
			int maxDocReperiti, int npos) throws IOException {
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

			String backspace = "\n";
			for (int i = 0; i < entrylist.size()
					&& (i < maxDocReperiti || maxDocReperiti == 0); i++) {
				// Calcolo i diversi coefficienti

				String queryID = "";
				if (queryId < 10) {
					queryID = "0";
				}
				queryID += queryId;

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
		scriviPesi(path, reperimentoRF, M, 1);
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

		scriviPesi(path, reperimentoPR, M, 1);

	}

	public void eseguiReperimentoLSA(int M,
			HashMap<Integer, List<Map.Entry<Integer, Double>>> reperimento,
			String path, HashMap<Integer, Documento> docs,
			HashMap<Integer, HashMap<String, Double>> keywordsQuery,
			int versione) throws IOException, InterruptedException {

		LSA lsa = new LSA(docs, keywordsQuery, reperimento);

		HashMap<Integer, HashMap<Integer, Double>> reperimentoLSA = new HashMap<Integer, HashMap<Integer, Double>>();

		HashMap<Integer, HashMap<Integer, Double>> reperimentoLSA_tmp1 = new HashMap<Integer, HashMap<Integer, Double>>();
		HashMap<Integer, HashMap<Integer, Double>> reperimentoLSA_tmp2 = new HashMap<Integer, HashMap<Integer, Double>>();

		// Per ogni query
		Set<Integer> keys = reperimento.keySet();
		int N = 0;
		for (Integer key : keys) {

			switch (versione) {
			case 1:
				HashMap<Integer, ArrayList<Integer>> docQrels = Parser
						.parserQrels("./data/qrels-originale.txt");
				if(docQrels.containsKey(key)) {
					N = docQrels.get(key).size();
				} else {
					N = Reperimento.LSA_N;
				}
				break;
			case 2:
				N = contaDocRilevantiConMaxValore(reperimento,
						LSA_N_DOCS_PESO_MAX_PERCENT, key);
				break;
			default:
				N = LSA_N;
			}

			DoubleMatrix pesiLSA = lsa.eseguiLSA(N, key);
			reperimentoLSA.put(key, new HashMap<Integer, Double>());
			List<Map.Entry<Integer, Double>> listReperiti = reperimento
					.get(key);
			for (int i = 0; i < listReperiti.size(); i++) {
				if (i < N) {
					System.out.println(pesiLSA.get(i, 0));
					double lsa_valore = 1 + pesiLSA.get(i, 0);// Math.exp(pesiLSA.get(i,
																// 0));
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

			// Scrivo i file dei pesi dei documenti per ogni query
			scriviPesi("./data/" + key + "_1.txt", reperimentoLSA_tmp1, N, 1);
			scriviPesi("./data/" + key + "_2.txt", reperimentoLSA_tmp2, M - N, N + 1);
			concatFile("./data/" + key + "_1.txt", "./data/" + key + "_2.txt",
					"./data/" + key + ".txt");
			new File("./data/" + key + "_1.txt").delete();
			new File("./data/" + key + "_2.txt").delete();

			reperimentoLSA_tmp1 = new HashMap<Integer, HashMap<Integer, Double>>();
			reperimentoLSA_tmp2 = new HashMap<Integer, HashMap<Integer, Double>>();
		}

		FileWriter writer = new FileWriter(path);
		Integer oldKey = 0;
		String firstFile = "";
		for (Integer key : keys) {
			if(oldKey == 0) {
				firstFile = "./data/" + key + ".txt";
			} else {
				concatFile(firstFile, "./data/" + key + ".txt", "./data/" + key + "_c.txt");
				
				new File(firstFile).delete();
				new File("./data/" + key + ".txt").delete();
				
				firstFile = "./data/" + key + "_c.txt";
			}
			oldKey = key;
		}
		new File(firstFile).renameTo(new File(path));
		
		writer.close();
	}

	private int contaDocRilevantiConMaxValore(
			HashMap<Integer, List<Map.Entry<Integer, Double>>> reperimento,
			double percentuale, int queryId) {
		double valore = reperimento.get(queryId).get(0).getValue();
		valore -= valore * percentuale / 100.0;
		int numDocs = 0;
		for (int i = 0; i < reperimento.get(queryId).size(); i++) {
			if (reperimento.get(queryId).get(i).getValue() > valore) {
				numDocs++;
			} else {
				i = reperimento.get(queryId).size();
			}
		}
		return numDocs;
	}

	public void concatFile(String path1, String path2, String outPath)
			throws IOException {
		BufferedReader inFile1 = new BufferedReader(new FileReader(path1));
		BufferedReader inFile2 = new BufferedReader(new FileReader(path2));

		// File to write
		BufferedWriter outFile = new BufferedWriter(new FileWriter(
				"./data/concatTmp.txt"));
		String lineFile1 = "";
		String lineFile2 = "";

		while ((lineFile1 = inFile1.readLine()) != null) {
			outFile.write(lineFile1 + "\n");
		}

		while ((lineFile2 = inFile2.readLine()) != null) {
			outFile.write(lineFile2 + "\n");
		}

		inFile1.close();
		inFile2.close();
		outFile.close();

		// Copio i dati del file temporaneo concatTmp nel path del file outPath
		inFile1 = new BufferedReader(new FileReader("./data/concatTmp.txt"));
		outFile = new BufferedWriter(new FileWriter(outPath));
		while ((lineFile1 = inFile1.readLine()) != null) {
			outFile.write(lineFile1 + "\n");
		}
		inFile1.close();
		outFile.close();
		
		new File("./data/concatTmp.txt").delete();
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
			HashMap<Integer, Double> pesiHITS = hits
					.calcoloPesiCentralitaAutorevolezza(queryId, N);

			reperimentoHITS.put(queryId, new HashMap<Integer, Double>());
			List<Map.Entry<Integer, Double>> listReperiti = reperimento
					.get(queryId);

			for (int i = 0; i < listReperiti.size(); i++) {
				if (i < N) {
					// Recupero il coefficiente dato da hits per i primi N doc
					double hits_valore = 1 + pesiHITS.get(listReperiti.get(i)
							.getKey());

					reperimentoHITS.get(queryId).put(
							listReperiti.get(i).getKey(),
							listReperiti.get(i).getValue() * hits_valore);

					if (!reperimentoHITS_tmp1.containsKey(queryId)) {
						reperimentoHITS_tmp1.put(queryId,
								new HashMap<Integer, Double>());
					}
					reperimentoHITS_tmp1.get(queryId).put(
							listReperiti.get(i).getKey(),
							listReperiti.get(i).getValue() * hits_valore);
				} else {
					reperimentoHITS.get(queryId).put(
							listReperiti.get(i).getKey(),
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

			// Scrivo i file dei pesi dei documenti per ogni query
			scriviPesi("./data/" + queryId + "_1.txt", reperimentoHITS_tmp1, N, 1);
			scriviPesi("./data/" + queryId + "_2.txt", reperimentoHITS_tmp2, M - N, N + 1);
			concatFile("./data/" + queryId + "_1.txt", "./data/" + queryId + "_2.txt",
					"./data/" + queryId + ".txt");
			new File("./data/" + queryId + "_1.txt").delete();
			new File("./data/" + queryId + "_2.txt").delete();

			reperimentoHITS_tmp1 = new HashMap<Integer, HashMap<Integer, Double>>();
			reperimentoHITS_tmp2 = new HashMap<Integer, HashMap<Integer, Double>>();
		}

		FileWriter writer = new FileWriter(path);
		Integer oldKey = 0;
		String firstFile = "";
		for (Integer key : keys) {
			if(oldKey == 0) {
				firstFile = "./data/" + key + ".txt";
			} else {
				concatFile(firstFile, "./data/" + key + ".txt", "./data/" + key + "_c.txt");
				
				new File(firstFile).delete();
				new File("./data/" + key + ".txt").delete();
				
				firstFile = "./data/" + key + "_c.txt";
			}
			oldKey = key;
		}
		new File(firstFile).renameTo(new File(path));
		
		writer.close();
	}
}
