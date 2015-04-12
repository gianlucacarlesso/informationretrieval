import java.util.ArrayList;
import java.util.HashMap;

public class Documento {
	private int id;
	private String titolo;
	private HashMap<String, Integer> keywords;
	private HashMap<String, Integer> stems;
	private ArrayList<Integer> citazioni;

	public Documento(int _id, String _titolo,
			HashMap<String, Integer> _keywords,
			HashMap<String, Integer> _stems, ArrayList<Integer> _citazioni) {
		id = _id;
		titolo = _titolo;
		keywords = _keywords;
		stems = _stems;
		citazioni = _citazioni;
	}
	
	public String mioTitolo(){
		return titolo;
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


}