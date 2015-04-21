import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class Documento {
	private int id;
	private String titolo;
	private String sabstract;
	private String autori;
	private HashMap<String, Integer> keywords;
	private HashMap<String, Integer> stems;
	private ArrayList<Integer> citazioni;

	public Documento(int _id, String _titolo, String _autori,
			String _sabstract, HashMap<String, Integer> _keywords,
			HashMap<String, Integer> _stems, ArrayList<Integer> _citazioni) {
		id = _id;
		titolo = _titolo;
		sabstract = _sabstract;
		autori = _autori;
		keywords = _keywords;
		stems = _stems;
		citazioni = _citazioni;

		mergeAutori();
	}

	public String mioTitolo() {
		return titolo;
	}

	public String mioAutori() {
		return autori;
	}

	public String mioAbstract() {
		return sabstract;
	}

	public int mioId() {
		return id;
	}

	public HashMap<String, Integer> mieKeyword() {
		return keywords;
	}

	public HashMap<String, Integer> mieStems() {
		return stems;
	}

	public ArrayList<Integer> mieCitazioni() {
		return citazioni;
	}

	public String getStemKeyWords(String key) {
		Set<String> kstems = stems.keySet();
		String stem = null;

		// Ci sarà UN solo stem che è anche prefisso di una keywords
		// Se non trovo uno stem, probabilmente la radice ha subito una modifica
		// (y -> i)
		int differenza = 0;
		while (stem == null) {
			for (String k : kstems) {
				String t = k.subSequence(0, k.length() - differenza).toString();
				if (key.startsWith(t)) {
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
		for (String k : keys) {
			tot += keywords.get(k);
		}

		return tot;
	}

	// Calcolo il numero totale di parole chiave presenti nell'abstract
	public int getNumFreqKeywordsAbstractTotale() {
		Set<String> keys = keywords.keySet();
		int tot = 0;
		for (String k : keys) {
			tot += sabstract.split(k).length - 1;
		}

		return tot;
	}

	public int getFrequenzaKeywordAbstract(String key) {
		return sabstract.split(key).length - 1;
	}

	public void mergeAutori() {
		String keyword = "";
		char[] caratteri = autori.toLowerCase().toCharArray();

		for (int i = 0; i < caratteri.length; i++) {
			if (caratteri[i] != ' ' && caratteri[i] != '\t'
					&& caratteri[i] != '.') {
				keyword += caratteri[i];
			} else if (caratteri[i] == ' ') {
				// Ho una nuova parola chiave se è formato da più di due
				// caratteri
				if (keyword.length() > 1) {
					if (keywords.containsKey(keyword)) {
						// Se è presente incremento la frequenza
						keywords.put(keyword, keywords.get(keyword) + 1);
					} else {
						keywords.put(keyword, 1);
					}
				}
				keyword = "";
			} else {
				keyword = "";
			}
		}
	}
}