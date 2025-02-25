import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class CSVReader {
	
	private static ArrayList<String[]> resultArray;
	
	public static ArrayList<String[]> readCSV(String filePath){
		resultArray = new ArrayList();
		String file = filePath;
		BufferedReader br = null;
		String line= "";
		String splitBy = ",";
		
		try{
			br = new BufferedReader(new FileReader(file));
			br.readLine(); //consume first line
			while ((line = br.readLine()) != null) {
				// use comma as separator
				String[] lineResult = line.split(splitBy);
				resultArray.add(lineResult);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return resultArray;
	}
}
