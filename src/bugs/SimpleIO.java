package bugs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFileChooser;

public class SimpleIO {
static String fileName;
    
    /**
     * Reads in lines from a user-chosen file and stores it into a string.
     * 
     * @return Single string of the entire program.
     * @throws IOException In the event of an error.
     */
    public static String load(){
        String program = "";
        BufferedReader reader;

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Load which file?");
        int result = chooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (file != null) {
            	try{
            		fileName = file.getCanonicalPath();
            	}
            	catch(IOException e){
            		e.printStackTrace();
            	}
            	try{
	                reader =
	                    new BufferedReader(new FileReader(fileName));  
	            	
	            	
	                String line;
	                while ((line = reader.readLine()) != null) {
	                    program = program + line + " \n ";
	                }
	                reader.close();
            	}
            	catch(IOException e){
            		e.printStackTrace();
            	}
                return program;
            }
        }
        return program;
    }
}
