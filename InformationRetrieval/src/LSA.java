import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Jama.Matrix;

public class LSA {
	private HashMap<Integer, Documento> docs;
	private HashMap<Integer, HashMap<String, Double>> keywordsQueries;
	private HashMap<Integer, List<Map.Entry<Integer, Double>>> docsReperiti;

	public LSA(HashMap<Integer, Documento> _docs,
			HashMap<Integer, HashMap<String, Double>> _keywordsQuery,
			HashMap<Integer, List<Map.Entry<Integer, Double>>> _docsReperiti) {
		docs = _docs;
		keywordsQueries = _keywordsQuery;
		docsReperiti = _docsReperiti;
	}

	private HashMap<String, Integer> creaRigheKeywordsMatrice(Integer queryId,
			HashMap<Integer, List<Map.Entry<Integer, Double>>> reperimento,
			int N) {
		HashMap<String, Integer> righeKeywords = new HashMap<String, Integer>();

		// Recupero i primi M documenti reperiti da cui ricavare le keywords
		// per Q'
		List<Map.Entry<Integer, Double>> docReperiti = reperimento.get(queryId);

		int counter = 0;
		for (int i = 0; i < docReperiti.size() && i < N; i++) {
			Set<String> keys = docs.get(docReperiti.get(i).getKey())
					.mieKeyword().keySet();
			for (String key : keys) {
				if (!righeKeywords.containsKey(key)) {
					righeKeywords.put(key, counter);
					counter++;
				}

			}
		}

		return righeKeywords;
	}

	private Matrix costruisciMatriceX(Integer queryId,
			HashMap<Integer, List<Map.Entry<Integer, Double>>> reperimento,
			int N, HashMap<String, Integer> righeKeywords) {

		Matrix matriceX = new Matrix(righeKeywords.size(), N, 0.0);

		List<Map.Entry<Integer, Double>> docReperiti = reperimento.get(queryId);

		for (int i = 0; i < docReperiti.size() && i < N; i++) {
			Set<String> keys = docs.get(docReperiti.get(i).getKey())
					.mieKeyword().keySet();
			for (String key : keys) {
				// recupero la posizione della keyword corrente rispetto alla
				// riga della matrice
				int posKey = righeKeywords.get(key);

				matriceX.set(posKey, i, 1.0);
			}
		}

		return matriceX;
	}

	private Matrix costruisciVettoreY(Integer queryId,
			HashMap<Integer, List<Map.Entry<Integer, Double>>> reperimento,
			int N, HashMap<String, Integer> righeKeywords) {

		Matrix vettoreY = new Matrix(righeKeywords.size(), 1, 0.0);

		Set<String> keys = righeKeywords.keySet();
		for (String k : keys) {
			if (keywordsQueries.get(queryId).containsKey(k)) {
				int posKey = righeKeywords.get(k);
				vettoreY.set(posKey, 0, 1.0);
			}
		}

		return vettoreY;
	}

	private Matrix costruisciMatriceS(Matrix x) {
		Matrix xx_t = x.times(x.transpose());
		Matrix v_t = xx_t.svd().getV();
		System.out.println(v_t.getRowDimension());
		System.out.println(v_t.getColumnDimension());
		return v_t;
	}

	public Matrix eseguiLSA(int N, int queryId) {
		HashMap<String, Integer> righeKeywords = creaRigheKeywordsMatrice(
				queryId, docsReperiti, N);

		System.out.println(righeKeywords.size());

		Matrix x = costruisciMatriceX(queryId, docsReperiti, N, righeKeywords);
		Matrix y = costruisciVettoreY(queryId, docsReperiti, N, righeKeywords);

		Matrix v_t = costruisciMatriceS(x);

		Matrix v1 = v_t.getMatrix(0, righeKeywords.size() - 1, 0, 0);
		Matrix v2 = v_t.getMatrix(0, righeKeywords.size() - 1, 1, 1);

		Matrix newy_y1 = (v1.transpose()).times(y);
		Matrix newy_y2 = (v2.transpose()).times(y);

		Matrix newY = new Matrix(2, 1);
		newY.set(0, 0, newy_y1.get(0, 0));
		newY.set(1, 0, newy_y2.get(0, 0));

		Matrix newy_x1 = (v1.transpose()).times(x);
		Matrix newy_x2 = (v2.transpose()).times(x);

		Matrix newX = new Matrix(2, N);
		newX.setMatrix(0, 0, 0, N - 1, newy_x1);
		newX.setMatrix(1, 1, 0, N - 1, newy_x2);

		return (newX.transpose()).times(newY);
	}

}
