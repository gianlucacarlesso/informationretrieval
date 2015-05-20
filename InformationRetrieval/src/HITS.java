import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jblas.DoubleMatrix;

public class HITS {
	private HashMap<Integer, Documento> docs;
	private HashMap<Integer, List<Map.Entry<Integer, Double>>> docsReperiti;

	public HITS(HashMap<Integer, Documento> _docs,
			HashMap<Integer, List<Map.Entry<Integer, Double>>> _docsReperiti) {
		docs = _docs;
		docsReperiti = _docsReperiti;
	}

	private DoubleMatrix costruisciMatriceD(Integer queryId,
			HashMap<Integer, List<Map.Entry<Integer, Double>>> reperimento,
			int N, HashMap<Integer, Integer> righeDocs) {
		righeDocs.clear();

		// Recupero i primi N documenti reperiti
		List<Map.Entry<Integer, Double>> docReperiti = reperimento.get(queryId);

		int counter = 0;
		for (int i = 0; i < docReperiti.size() && i < N; i++) {

			int docId = docReperiti.get(i).getKey();
			if (!righeDocs.containsKey(docId)) {
				righeDocs.put(docId, counter);
				counter++;
			}
		}

		// Repero i Q documenti associati (citati) ai primi N
		Set<Integer> keys = righeDocs.keySet();
		HashMap<Integer, Integer> righeDocsConQ = new HashMap<Integer, Integer>();
		for (int key : keys) {
			ArrayList<Integer> citati = docs.get(key).mieCitazioni();

			for (int i = 0; i < citati.size(); i++) {
				int docCitato = citati.get(i);
				// Se il doc non e' presente, lo aggiungo
				if (!righeDocs.containsKey(docCitato)
						&& !righeDocsConQ.containsKey(docCitato)) {
					righeDocsConQ.put(docCitato, counter);
					counter++; // proseguo col counter, memorizzo, per ogni doc,
								// la riga dove lo posiziono
				}
			}
		}

		righeDocs.putAll(righeDocsConQ);

		// Costruisco la matrice D
		DoubleMatrix D = DoubleMatrix.zeros(righeDocs.size(), righeDocs.size());

		keys = righeDocs.keySet();
		for (int docInEsame : keys) {
			// Posizione docInEsame nella matrice
			int posDocInEsame = righeDocs.get(docInEsame);

			// Recupero le citazioni
			ArrayList<Integer> citati = docs.get(docInEsame).mieCitazioni();

			// Imposto il valore 1/Ncit(i) per i doc citati da docInEsame
			for (int i = 0; i < citati.size(); i++) {
				int idDocCitato = citati.get(i);

				// Verifico che il doc sia derivato dagli N che ho in esame
				if (righeDocs.containsKey(idDocCitato)) {
					int posDocCitato = righeDocs.get(idDocCitato);
					double valore = 1 / citati.size();

					D.put(posDocInEsame, posDocCitato, valore);
				}
			}
		}

		return D;
	}

	private DoubleMatrix costruisciMatriceB(DoubleMatrix D) {
		return D.mmul(D.transpose());
	}

	private DoubleMatrix costruisciMatriceC(DoubleMatrix D) {
		return (D.transpose()).mmul(D);
	}

	public HashMap<Integer, Double> calcoloPesiCentralitaAutorevolezza(
			int queryId, int N) {
		HashMap<Integer, Integer> righeDocs = new HashMap<Integer, Integer>();
		;
		DoubleMatrix D = costruisciMatriceD(queryId, docsReperiti, N, righeDocs);
		DoubleMatrix Dt = D.transpose();

		DoubleMatrix h = DoubleMatrix.zeros(D.rows, 1);
		DoubleMatrix a = DoubleMatrix.zeros(D.rows, 1);

		// Inizializzo la matrice h(0) e a(0)
		for (int i = 0; i < h.rows; i++) {
			h.put(i, 0, 1.0 / h.rows);
			a.put(i, 0, 1.0 / h.rows);
		}

		// Inizio l'iterazione
		boolean convergo = false;
		int counter = 0;

		double precMacchina = 1e-14;
		while (!convergo && counter < Integer.MAX_VALUE) {
			DoubleMatrix new_a = Dt.mmul(h);
			DoubleMatrix new_h = D.mmul(new_a);

			// Normalizzo i vettori a ed h
			if (new_a.max() > precMacchina) {
				new_a = new_a.div(new_a.max());
			}

			if (new_h.max() > precMacchina) {
				new_h = new_h.div(new_h.max());
			}

			// Verifico se ho raggiunto la convergenza
			if (precisioneMacchina(a, new_a) && precisioneMacchina(h, new_h)) {
				convergo = true;
			}

			a = new_a;
			h = new_h;

			counter++;

		}

		// Preparo i coefficienti per la funzione di reperimennto a/h
		DoubleMatrix coefficienti = DoubleMatrix.zeros(a.rows, a.columns);
		for (int i = 0; i < a.rows; i++) {
			for (int j = 0 ; j < a.columns; j++) {
				if(Math.abs(h.get(i,j)) > precMacchina) {
					coefficienti.put(i, j, a.get(i,j) / h.get(i,j));
				}
			}
		}
		
		HashMap<Integer, Double> coeff = new HashMap<Integer, Double>();

		Set<Integer> docsId = righeDocs.keySet();
		for (Integer docId : docsId) {
			int rigaDoc = righeDocs.get(docId);
			double valoreHits = coefficienti.get(rigaDoc, 0);
			coeff.put(docId, valoreHits);
		}

		return coeff;
	}

	private boolean precisioneMacchina(DoubleMatrix a, DoubleMatrix b) {
		DoubleMatrix t = a.sub(b);
		boolean result = true;
		Double precMacchina = 1e-14;

		for (int i = 0; i < t.rows && result; i++) {
			for (int j = 0; j < t.columns && result; j++) {
				if (Math.abs(t.get(i, j)) > precMacchina) {
					result = false;
				}
			}
		}

		return result;
	}
}
