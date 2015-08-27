package render;

import java.util.ArrayList;
/**
 * Created by Mini on 27-08-2015.
 */
public class config {
    ArrayList<String> ColorIndex = new ArrayList<>();
    int limit;
    inout file = new inout();

    boolean get() { //load variables from a configfile, a config is not presant, create one and return false
        String settings = file.readFile("settings");
        if (settings.equals("false")) {
            Set();
            return get();
        }
        int cNum = Integer.valueOf(settings.substring(settings.indexOf("C?") + 3, settings.indexOf("#", settings.indexOf("C?"))));
        limit = 255 * cNum;
        ColorIndex.clear();
        for (int i = 0; i <= cNum - 1; i++) {
            ColorIndex.add(settings.substring(settings.indexOf("C" + String.valueOf(i + 1)) + 2 + String.valueOf(i).length(), settings.indexOf("#", settings.indexOf("C" + String.valueOf(i + 1)))));
        }
        return true;
    }

    int getLimit(){
        return limit;
    }

    ArrayList<String> getColors(){
        return ColorIndex;
    }

    void Set() { //create a stndard config-file
        String output;
        output = "C? 2#" + "\n" + "C1 000000180#" + "\n" + "C2 255255050#";
        file.writeFile(output, "settings");
    }
}
