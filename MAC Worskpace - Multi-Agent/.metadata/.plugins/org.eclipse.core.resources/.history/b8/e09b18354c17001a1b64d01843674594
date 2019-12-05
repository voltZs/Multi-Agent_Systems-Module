package mas.coursework;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.opencsv.CSVWriter;

public class SimulationLogger {
	
	static String filename = "log_data.csv";
	
	public static void appendHeader(String evaluatedAttribute){
		File file = new File(filename); 
	    try { 
	        FileWriter outputfile = new FileWriter(file, true); //true = append not overwrite
	        CSVWriter writer = new CSVWriter(outputfile); 
	        String[] data = { 
	        		evaluatedAttribute,
	        		"run",
	        		"profit"}; 
	        writer.writeNext(data); 
	        
	        // closing writer connection 
	        writer.close(); 
	    } 
	    catch (IOException e) { 
	        e.printStackTrace(); 
	    } 
		
	}
	
	public static void appendLine(int evalAttr, int run, int profit){ 
	    File file = new File(filename); 
	    try { 
	        FileWriter outputfile = new FileWriter(file, true); //true = append not overwrite
	        CSVWriter writer = new CSVWriter(outputfile); 
	        String[] data = { 
	        		Integer.toString(evalAttr),
	        		Integer.toString(run),
	        		Integer.toString(profit)}; 
	        writer.writeNext(data); 
	        
	        // closing writer connection 
	        writer.close(); 
	    } 
	    catch (IOException e) { 
	        e.printStackTrace(); 
	    } 
	} 
}
