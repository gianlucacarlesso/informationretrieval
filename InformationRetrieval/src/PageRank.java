import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


public class PageRank {
	private HashMap<Integer, Documento> docs;
	private HashMap<Integer, HashMap<Integer, Double>> matriceProb;
	private HashMap<Integer, Double> pageRank;
	
	public PageRank(HashMap<Integer, Documento> _docs, double d, double precisione) {
		docs = _docs;	
		matriceProb = new HashMap<Integer, HashMap<Integer, Double>>();
		pageRank = new HashMap<Integer, Double>();
		
		Set<Integer> keys = docs.keySet();
		
		List<Integer> listkeys = new ArrayList<Integer>(keys);
		Collections.sort(listkeys);

		// Calcolo la probabilita dato un doc(i) di andare in un doc(j)
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
		
		// Creo passo p^(k) con k>0
		boolean stabilita = false;
		int counter = 0;
		// Pongo un upper bound ampio per evitare loop infinito se i valori divergono
		while(!stabilita && counter < Integer.MAX_VALUE) {
			stabilita = true;
			for(Integer key : keys) {
				
				// Probabilita di un salto casuale
				Double pr = (d * 1 / docs.size());
				
				// Probabilita di seguire un collegamento
				Double peso_pr = 0.0;
				for(Integer key_i : keys) {
					if(matriceProb.get(key).containsKey(key_i)) {
						peso_pr += pageRank.get(key_i) * matriceProb.get(key).get(key_i);
					}
				}
				
				pr += (1 - d) * peso_pr;
				
				// Stabilita: se il nuovo valore e il precedente non differiscono per meno di 0.001 non ho
				// raggiunto la stabilita
				if(Math.abs(pageRank.get(key) - pr) > precisione) {
					stabilita = false;
				}
				
				// Aggiorno il valore del pagerank
				pageRank.put(key, pr);
			}

			counter++;
			System.out.println("Iterazione:" + counter);
		}
		
	}
	
	public HashMap<Integer, Double> getPageRank() {
		return pageRank;
	}
}
