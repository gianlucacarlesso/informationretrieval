import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class UtilsIR {
	public static void reformatXML(String pathFileIn, String pathFileOut) throws IOException {
		FileReader reader = new FileReader(pathFileIn);
		BufferedReader bufferReader = new BufferedReader(reader);
		FileWriter writer = new FileWriter(pathFileOut);
		
		String linea = "";
		
		while ((linea = bufferReader.readLine()) != null) {
			String[] token = linea.split("	", 2);
			System.out.println(token[0]);
			writer.write(token[1] + "\n");
		}
		bufferReader.close();
		writer.close();
	}
}
