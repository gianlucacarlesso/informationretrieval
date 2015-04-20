import java.io.IOException;
import java.util.HashMap;
import java.util.Set;


public class Reperimento {
	private HashMap<Integer, HashMap<String, Double>> pesiKeywordDocumenti;
	private HashMap<Integer, HashMap<String, Integer>> keywordsQuery;
	
	public Reperimento(HashMap<Integer, HashMap<String, Double>> _pesiKeywordDocumenti, HashMap<Integer, HashMap<String, Integer>> _keywordsQuery) {
		pesiKeywordDocumenti = _pesiKeywordDocumenti;
		keywordsQuery = _keywordsQuery;
	}
	
	public double getPesoStem(int queryId, int queryDoc) throws IOException{
		if(!keywordsQuery.containsKey(queryId)) {
			throw new IOException("L'id della query specificata non esiste");
		} else if(!pesiKeywordDocumenti.containsKey(queryDoc)) {
			throw new IOException("L'id del documento specificato non esiste");
			
		} else {
			HashMap<String, Double> pesiKeywords = pesiKeywordDocumenti.get(new Integer(queryDoc));
			Set<String> keywordsDoc = pesiKeywords.keySet();
			double peso = 0;
			for(String kDoc: keywordsDoc) {
				//if(pesiKeywords.containsKey(key)) {
					
				//}
			}
		}
		
		return 0;
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
