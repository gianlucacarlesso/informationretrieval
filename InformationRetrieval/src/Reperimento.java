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
	private HashMap<Integer, HashMap<String, Integer>> keywordsQuery;
	private HashMap<Integer, ArrayList<String>> stemQuery;
	private HashMap<Integer, Documento> docs;

	public Reperimento(
			HashMap<Integer, HashMap<String, Double>> _pesiKeywordDocumenti,
			HashMap<Integer, HashMap<String, Integer>> _keywordsQuery,
			HashMap<Integer, ArrayList<String>> _stemQuery,
			HashMap<Integer, Documento> _docs) {
		pesiKeywordDocumenti = _pesiKeywordDocumenti;
		keywordsQuery = _keywordsQuery;
		stemQuery = _stemQuery;
		docs = _docs;
	}

	public double getPesoStem(int queryId, int docId) throws IOException {
		double peso = 0;

		if (!keywordsQuery.containsKey(queryId)) {
			throw new IOException("L'id della query specificata non esiste");
		} else if (!pesiKeywordDocumenti.containsKey(docId)) {
			throw new IOException("L'id del documento specificato non esiste");

		} else {
			Documento doc = docs.get(new Integer(docId));
			HashMap<String, Double> pesiKeywords = pesiKeywordDocumenti
					.get(new Integer(docId));
			Set<String> keywordsDoc = pesiKeywords.keySet();
			for (String kDoc : keywordsDoc) {
				// Verifico che la keyword del documento NON sia presente nelle
				// keyword della query e che lo stem di
				// tale keyword sia presente nello stem delle keyword della
				// query
				if (!keywordsQuery.containsKey(kDoc)
						&& stemQuery.get(queryId).contains(
								doc.getStemKeyWords(kDoc))) {
					peso += pesiKeywords.get(kDoc);
				}
			}
		}

		return peso;
	}

	public HashMap<Integer, HashMap<Integer, Double>> eseguiReperimento(
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
		scriviPesi(path, reperimento, maxDocsReperiti);
		return reperimento;
	}

	public void scriviPesi(String path,
			HashMap<Integer, HashMap<Integer, Double>> reperimento, int maxDocReperiti)
			throws IOException {
		FileWriter writer = new FileWriter(path);

		Set<Integer> queriesId = reperimento.keySet();
		// Scorro tutte le query
		for (Integer queryId : queriesId) {
			// Procedo per ogni

			Comparator<Map.Entry<Integer, Double>> comp = new Comparator<Map.Entry<Integer, Double>>() {

				@Override
				public int compare(Entry<Integer, Double> o1,
						Entry<Integer, Double> o2) {
					return Double.compare(o1.getValue(), o2.getValue());
				}
			};
			
			Set<Map.Entry<Integer, Double>> entries = reperimento.get(queryId)
					.entrySet();
			List<Map.Entry<Integer, Double>> entrylist = new ArrayList<Map.Entry<Integer, Double>>(
					entries);
			Collections.sort(entrylist, comp);

			for (int i = 0; i < entrylist.size() && (i < maxDocReperiti || maxDocReperiti == 0); i++) {
				// Calcolo i diversi coefficienti

				writer.write(queryId + " Q0 " + entrylist.get(i).getKey() + " "
						+ (i + 1) + " " + entrylist.get(i).getValue()
						+ " GR11R1\n");
			}
		}
		
		writer.close();
	}

	
	public double getPeso(int queryId, int queryDoc) throws IOException{
		double peso = 0;
		
		if(!keywordsQuery.containsKey(queryId)) {
			throw new IOException("L'id della query specificata non esiste");
		} else if(!pesiKeywordDocumenti.containsKey(queryDoc)) {
			throw new IOException("L'id del documento specificato non esiste");
			
		} else {
			
			HashMap<String, Double> pesiKeywords = pesiKeywordDocumenti.get(new Integer(queryDoc));
			Set<String> keywordsDoc = pesiKeywords.keySet();
			
			HashMap<String, Integer> keywQuery = keywordsQuery.get(queryId);
			Set<String> keywordsThisQuery = keywQuery.keySet();
			
			
			for(String kDoc: keywordsDoc) {
				if(keywordsThisQuery.contains(kDoc)) {
					peso = peso + pesiKeywords.get(kDoc);
				}
			}
		}
		
		return peso;
	}
	
}
