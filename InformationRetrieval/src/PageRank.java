import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


public class PageRank {
	private HashMap<Integer, Documento> docs;
	private HashMap<Integer, HashMap<Integer, Double>> matriceProb;
	private HashMap<Integer, Double> pageRank;
	
	public PageRank(HashMap<Integer, Documento> _docs, double d) {
		docs = _docs;	
		matriceProb = new HashMap<Integer, HashMap<Integer, Double>>();
		pageRank = new HashMap<Integer, Double>();
		
		Set<Integer> keys = docs.keySet();
		
		List<Integer> listkeys = new ArrayList<Integer>(keys);
		Collections.sort(listkeys);

		for(Integer key_i:listkeys){
			matriceProb.put(key_i, new HashMap<Integer, Double>());
	
			for(Integer key_j : listkeys) {
				if(key_i <= key_j) {
					ArrayList<Integer> citazioni = docs.get(key_i).mieCitazioni();
					
					Double probabilita;
					if(citazioni.contains(key_j)) {
						probabilita = 1.0 / citazioni.size();

					} else {
						if(citazioni.size() != 0) {
							probabilita = 0.0;
						} else {
							probabilita = 1.0 / docs.size();
						}
					}
					
					matriceProb.get(key_i).put(key_j, probabilita);
				}
			}
		}
		
	
		keys = matriceProb.keySet();
		// Creo passo p^(0)
		for(Integer key : keys) {
			pageRank.put(key, 1.0 / docs.size());
		}
		
		for(Integer key : keys) {
			Double pr = (d * 1 / docs.size());
			
			Double peso_pr = 0.0;
			for(Integer key_i : keys) {
				if(matriceProb.get(key).containsKey(key_i)) {
					peso_pr += pageRank.get(key_i) * matriceProb.get(key).get(key_i);
				}
			}
			
			pr += (1 - d) * peso_pr;
			
			pageRank.put(key, pr);
		}
		
	}
	
	public HashMap<Integer, Double> getPageRank() {
		return pageRank;
	}
	
	
}
