import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class Documento {
	private int id;
	private String titolo;
	private String sabstract;
	private HashMap<String, Integer> keywords;
	private HashMap<String, Integer> stems;
	private ArrayList<Integer> citazioni;

	public Documento(int _id, String _titolo, String _sabstract,
			HashMap<String, Integer> _keywords,
			HashMap<String, Integer> _stems, ArrayList<Integer> _citazioni) {
		id = _id;
		titolo = _titolo;
		sabstract = _sabstract;
		keywords = _keywords;
		stems = _stems;
		citazioni = _citazioni;
	}
	
	public String mioTitolo(){
		return titolo;
	}
	
	public String mioAbstract() {
		return sabstract;
	}
	
	public int mioId(){
		return id;
	}
	
	public HashMap<String, Integer> mieKeyword(){
		return keywords;
	}
	
	public HashMap<String, Integer> mieStems(){
		return stems;
	} 
	
	public ArrayList<Integer> mieCitazioni(){
		return citazioni;
	}

	public String getStemKeyWords(String key) {
		Set<String> kstems = stems.keySet();
		String stem = null;
		
		// Ci sarà UN solo stem che è anche prefisso di una keywords
		// Se non trovo uno stem, probabilmente la radice ha subito una modifica (y -> i)
		int differenza = 0;
		while(stem == null) {
			for(String k: kstems) {
				String t = k.subSequence(0, k.length() - differenza).toString();
				if(key.startsWith(t)) {
					stem = k;
				}
			}	
			differenza++;
		}		
		return stem;
	}
	
	// Ritorna il numero di stem presenti di una keyword
	public int getNumStemKeyword(String key) {
		return stems.get(getStemKeyWords(key));
	}
	
	// Ritorna la frequenza di una keyword4
	public int getFrequenzaKeyword(String key) {
		return keywords.get(key);
	}
	
	// Ritorna il totale delle frequenze delle keywords
	public int getNumFreqKeywordsTotali() {
		Set<String> keys = keywords.keySet();
		int tot = 0;
		for(String k: keys) {
			tot += keywords.get(k);
		}
		
		return tot;
	}
	
	// Calcolo il numero totale di parole chiave presenti nell'abstract
	public int getNumFreqKeywordsAbstractTotale() {
		Set<String> keys = keywords.keySet();
		int tot = 0;
		for(String k: keys) {
			tot += sabstract.split(k).length - 1;
		}
		
		return tot;
	}
	
	public int getFrequenzaKeywordAbstract(String key) {
		return sabstract.split(key).length - 1;
	}
}