/**
 * Created by hduser on 23/3/2016.
 */
public class ReturnType {
    public boolean parse_value ;
    public int number ;
    public int op ;

    public ReturnType(boolean parse_value ,int number){
        this.parse_value = parse_value;
        this.number = number ;
        this.op = 0;
    }
    public ReturnType(){
        this.parse_value =false;
        this.number =0;
        this.op = 0;
    }
    public ReturnType(boolean parse_value ,int number,int op){
        this.parse_value = parse_value;
        this.number = number ;
        this.op = op;
    }
}
