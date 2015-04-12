import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class formula {
	private int kTitolo;
	//private int tfdif;
	//private int kStem;
	//private int Kcitazioni;
	//private static ArrayList<Integer> pesoT;
	
	// ANCORA DA FINIRE
	public formula(int _kTitolo){
		kTitolo = _kTitolo;
	}
	
	public int pesoTitolo(Documento doc){
		String titolo = doc.mioTitolo();
		HashMap<String, Integer> keywords = doc.mieKeyword();
		Set<String> keywSet = keywords.keySet();
		
		// Lo usavo per controllare se c'erano tutte le parole chiave associate al documento;
		// così ho scoperto che c'era un errore nel parser
		for(String key: keywSet){
			System.out.println(key);
			}
		System.out.println(titolo);

		return kTitolo;
		
	}
}