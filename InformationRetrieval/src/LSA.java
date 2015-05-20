import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jblas.DoubleMatrix;
import org.jblas.Singular;

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

	private DoubleMatrix costruisciMatriceX(Integer queryId,
			HashMap<Integer, List<Map.Entry<Integer, Double>>> reperimento,
			int N, HashMap<String, Integer> righeKeywords) {

		DoubleMatrix matriceX = DoubleMatrix.zeros(righeKeywords.size(), N);

		List<Map.Entry<Integer, Double>> docReperiti = reperimento.get(queryId);

		for (int i = 0; i < docReperiti.size() && i < N; i++) {
			Set<String> keys = docs.get(docReperiti.get(i).getKey())
					.mieKeyword().keySet();
			for (String key : keys) {
				// recupero la posizione della keyword corrente rispetto alla
				// riga della matrice
				int posKey = righeKeywords.get(key);

				matriceX.put(posKey, i, 1.0);
			}
		}

		return matriceX;
	}

	private DoubleMatrix costruisciVettoreY(Integer queryId,
			HashMap<Integer, List<Map.Entry<Integer, Double>>> reperimento,
			int N, HashMap<String, Integer> righeKeywords) {

		DoubleMatrix vettoreY = DoubleMatrix.zeros(righeKeywords.size(), 1);

		Set<String> keys = righeKeywords.keySet();
		for (String k : keys) {
			if (keywordsQueries.get(queryId).containsKey(k)) {
				int posKey = righeKeywords.get(k);
				vettoreY.put(posKey, 0, 1.0);
			}
		}

		return vettoreY;
	}

	private DoubleMatrix costruisciMatriceS(DoubleMatrix x) {
		DoubleMatrix xx_t = x.mmul(x.transpose());
		DoubleMatrix v_t = (Singular.fullSVD(xx_t)[2]);
		System.out.println(v_t.rows);
		System.out.println(v_t.columns);
		return v_t;
	}

	public DoubleMatrix eseguiLSA(int N, int queryId) {
		HashMap<String, Integer> righeKeywords = creaRigheKeywordsMatrice(
				queryId, docsReperiti, N);

		DoubleMatrix x = costruisciMatriceX(queryId, docsReperiti, N, righeKeywords);
		DoubleMatrix y = costruisciVettoreY(queryId, docsReperiti, N, righeKeywords);

		DoubleMatrix v_t = costruisciMatriceS(x);

		DoubleMatrix v1 = v_t.getColumn(0); // v_t.getMatrix(0, righeKeywords.size() - 1, 0, 0);
		DoubleMatrix v2 = v_t.getColumn(1); // v_t.getMatrix(0, righeKeywords.size() - 1, 1, 1);

		DoubleMatrix newy_y1 = (v1.transpose()).mmul(y);
		DoubleMatrix newy_y2 = (v2.transpose()).mmul(y);

		DoubleMatrix newY = new DoubleMatrix(2, 1);
		newY.put(0, 0, newy_y1.get(0, 0));
		newY.put(1, 0, newy_y2.get(0, 0));

		DoubleMatrix newy_x1 = (v1.transpose()).mmul(x);
		DoubleMatrix newy_x2 = (v2.transpose()).mmul(x);

		DoubleMatrix newX = new DoubleMatrix(2, N);
		newX.putRow(0, newy_x1); // setMatrix(0, 0, 0, N - 1, newy_x1);
		newX.putRow(0, newy_x2); // setMatrix(1, 1, 0, N - 1, newy_x2);

		return (newX.transpose()).mmul(newY);
	}

}
