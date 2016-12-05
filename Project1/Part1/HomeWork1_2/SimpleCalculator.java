import java.io.InputStream;
import java.io.IOException;

class SimpleCalculator {

    private int lookaheadToken;

    private InputStream in;


    public SimpleCalculator(InputStream in) throws IOException {
		this.in = in;
		lookaheadToken = in.read();
	}

    private void consume(int symbol) throws IOException, ParseError {
		if (lookaheadToken != symbol) {
			//System.out.println("consume::");
			throw new ParseError();
		}
		//System.out.println(evalDigit(lookaheadToken) + "consumed" );
		lookaheadToken = in.read();
	}

    private int evalDigit(int digit){
	return digit - '0';
    }

	/*my functions*/

	private int doCalculation(int number1,int op,int number2){
		int result = 0;
		if(op == '+'){
			result =  number1 + number2 ;
			return result;
		}
		else if(op == '-'){
			result =  number1 - number2 ;
			return result;
		}
		else if(op == '*'){
			result =  number1 * number2 ;
			return result;
		}
		else if(op == '/'){
			result =  number1 / number2 ;
			return result;
		}
		return number1;			//no op
	}


	private ReturnType exp() throws IOException, ParseError {
		ReturnType cur_term = term();
		ReturnType rt_exp2 = exp2(cur_term);
		if (cur_term.parse_value && rt_exp2.parse_value)
			return new ReturnType(true,rt_exp2.number) ;
		return new ReturnType(false,0);
	}

	private ReturnType exp2(ReturnType prev_term) throws IOException, ParseError {
		ReturnType rt_ref ;
		ReturnType cur_term = new ReturnType();
		if( lookaheadToken =='+' || lookaheadToken =='-' ) {
			cur_term.op = lookaheadToken ;
			consume(lookaheadToken);
			rt_ref = term();
			cur_term.parse_value = rt_ref.parse_value ;
			cur_term.number = rt_ref.number ;
			if (cur_term.parse_value){
				//calc_terms();
				cur_term.number = doCalculation(prev_term.number,cur_term.op,cur_term.number);
				ReturnType rt_exp2 = exp2(cur_term) ;
				if(rt_exp2.parse_value) {
					return new ReturnType(true,rt_exp2.number);
				}
				else
					return new ReturnType(false,0);
			}
		}
		return new ReturnType(true,prev_term.number);
	}

	private ReturnType term() throws IOException, ParseError {
		ReturnType cur_factor  = factor();
		ReturnType cur_term2 = term2(cur_factor);
		if(cur_factor.parse_value&&cur_term2.parse_value){
			return new ReturnType(true,cur_term2.number) ;
		}
		//System.out.println("term::");
		return new ReturnType(false,0);
	}

	private ReturnType term2(ReturnType prev_factor) throws IOException, ParseError {
		ReturnType rt_ref ;
		ReturnType cur_factor = new ReturnType();
		if( lookaheadToken =='*' || lookaheadToken =='/' ) {        //consume [*/]
			cur_factor.op = lookaheadToken;
			consume(lookaheadToken);
			rt_ref = factor();
			cur_factor.parse_value = rt_ref.parse_value ;
			cur_factor.number = rt_ref.number ;
			if (cur_factor.parse_value) {
				//calc_factors();
				cur_factor.number = doCalculation(prev_factor.number,cur_factor.op,cur_factor.number);
				ReturnType cur_term2 = term2(cur_factor) ;
				if (cur_term2.parse_value) {
					return new ReturnType(true,cur_term2.number);
				}
				else
					return new ReturnType(false,0);
			}
		}
		return new ReturnType(true,prev_factor.number); // term2 == e
	}

	/*OK*/
	private ReturnType factor() throws IOException, ParseError {
		ReturnType rt = num();
		if(rt.parse_value){
			return new ReturnType(true,rt.number) ;
		}
		else if(lookaheadToken =='('){
			consume(lookaheadToken);
			ReturnType rt1  = exp();
			if(rt1.parse_value){				//na mpei exp edw
				if(lookaheadToken ==')') {
					//Upologise parenthesi
					consume(lookaheadToken);
					return new ReturnType(true,rt1.number);
				}else{
					//System.out.println("factor1::");
					throw new ParseError();		//expected ')'
				}
			}
		}
		//System.out.println("factor2::");
		throw new ParseError();
	}

	/*OK*/
	private ReturnType num() throws IOException, ParseError {
		if(lookaheadToken < '0' || lookaheadToken > '9') {
			return new ReturnType(false,0);
		}
		int number = evalDigit(lookaheadToken);
		consume(lookaheadToken);
		return new ReturnType(true,number);
	}
/*--------------------------------------------------------------------------------------------------------------------*/


    public ReturnType eval() throws IOException, ParseError {
		ReturnType rv = exp();
		if (lookaheadToken != '\n' && lookaheadToken != -1) {
			//System.out.println("eval::");
			throw new ParseError();
		}
		return rv;
    }

    public static void main(String[] args) {
		try {
			SimpleCalculator simpleCalculator = new SimpleCalculator(System.in);
			System.out.println(simpleCalculator.eval().number);
		}
		catch (IOException e) {
			System.err.println(e.getMessage());
		}
		catch(ParseError err){
			System.err.println(err.getMessage());
		}
		}
}
