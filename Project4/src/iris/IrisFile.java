import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kyriakos on 30/5/2016.
 */
public class IrisFile{
    private String name ;
    private List<String> buffer ;

    public IrisFile(){
        buffer = new ArrayList<>();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void emit(String tuple){
        this.buffer.add(tuple);
    }

    public void create() throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter writer = new PrintWriter(name, "UTF-8");
        for(String tuple : buffer){
            writer.println(tuple);
        }
        writer.close();
    }

}