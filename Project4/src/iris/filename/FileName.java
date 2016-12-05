package filename;

import java.io.File;

/**
 * Created by hduser on 15/5/2016.
 */
public class FileName {
    public static String name ;

    public static void createSpg(String fileJava){
        File f = new File(fileJava);

        String fname = f.getName();
        int pos = fname.lastIndexOf(".");
        if (pos > 0) {
            fname = fname.substring(0, pos);
        }
        //System.out.println(fname);
        StringBuilder builder = new StringBuilder();
        builder.append("optimized-spiglet/");
        builder.append(fname);
        builder.append(".spg");
        name = new String(builder.toString());
    }
}
