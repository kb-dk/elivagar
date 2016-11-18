package dk.kb.elivagar.utils;

public class StringUtils {

    
    
    public static String getSuffix(String path) {
        if(path.contains(".")){
            int end = path.lastIndexOf(".");
            return path.substring(end + 1);
        }
        return "";
    }
}
