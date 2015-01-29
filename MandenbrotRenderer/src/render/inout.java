package render;


import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Mini
 */


public class inout {
    
    void writeFile(String output, String loc){
        try {
            FileWriter fil = new FileWriter(loc+".txt");
            PrintWriter ud = new PrintWriter (fil);
            ud.println(output);
            ud.close();
        }catch(IOException ex){
        }
    }

    String readFile(String loc){
        String s = ("");
        try{
        FileReader fil = new FileReader(loc+".txt");
        BufferedReader ind = new BufferedReader (fil);
        String line = ind.readLine();
        while (line != null){
        s = s + line + "\n";
        line = ind.readLine();
        }
    } catch (FileNotFoundException ex) {
            s = "false";
            return s;
    }
        catch (IOException ex) {
            Logger.getLogger(inout.class.getName()).log(Level.SEVERE, null, ex);
        }
        return s;
    }
}