import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;


public class Parser {
    public static final int ARITHMETIC=0;
    public static final int PUSH=1;
    public static final int POP=2;
    public static final int RETURN=3;
    public static final int LABEL=4;
    public static final int GOTO=5;
    public static final int IF=6;
    public static final int FUNCTION=7;
    public static final int CALL=8;
    Scanner sc;
    private int cmdtype=-1;
    private String arg1="";
    private int arg2=-1;
    public Parser(File fileIn) {
    	try {
			sc=new Scanner(fileIn);
			String preprocessed="";
			while(sc.hasNextLine()) {
				String line=sc.nextLine().trim();
				if(line.equals("")||line.startsWith("//"))
					continue;
				preprocessed+=line+"\n";
				
			}
			sc=new Scanner(preprocessed);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       
    }


	/**
     * Are there more command to read
     * @return
     */
    public boolean hasMoreCommands(){
    	return sc.hasNextLine();   
    }

    /**
     * Reads next command from the input and makes it current command
     * Be called only when hasMoreCommands() returns true
     */
    public void advance(){
    	String[] line=sc.nextLine().split("");
       cmdtype=-1;
       arg1="";
       arg2=-1;
       
       if(isArithmetic(line[0])) {
    	   cmdtype=ARITHMETIC;
    	   arg1=line[1];
       }else if(line[0].equals("return"))
       {
    	   cmdtype=RETURN;
    	  // arg1=line[0];
       }else {
    	   arg1=line[1];
    	   if(line[0].equals("push")) {
    		   cmdtype=PUSH;
    	   }else if(line[0].equals("pop")) {
    		   cmdtype=POP;
    	   }else if(line[0].equals("label")) {
    		   cmdtype=LABEL;
    	   }else if(line[0].equals("if-goto")) {
    		   cmdtype=IF;
    	   }else if(line[0].equals("goto")) {
    		   cmdtype=GOTO;
    	   }else if(line[0].equals("function")) {
    		   cmdtype=FUNCTION;
    	   }else if(line[0].equals("call")) {
    		   cmdtype=CALL;
    	   }else {
    		   throw new IllegalArgumentException("wrong cmd");
    	   }
    	   if(cmdtype==PUSH||cmdtype==POP||cmdtype==FUNCTION||cmdtype==CALL)
    		   arg2=Integer.parseInt(line[2]);
       }

    }

    private boolean isArithmetic(String string) {
		// TODO Auto-generated method stub
    	if(string.equals("add")||string.equals("sub")||string.equals("neg")||
    			string.equals("eq")||string.equals("gt")||string.equals("lt")||
    			string.equals("and")||string.equals("or")||string.equals("not"))
    		return true;
		return false;
	}


	/**
     * Return the type of current command
     * ARITHMETIC is returned for all ARITHMETIC type command
     * @return
     */
    public int commandType(){

       if(cmdtype!=-1)
    	   return cmdtype;
       throw new IllegalStateException("No command");
    }

    /**
     * Return the first argument of current command
     * When it is ARITHMETIC, return it self
     * When it is RETURN, should not to be called
     * @return
     */
    public String arg1(){
    	if(cmdtype==RETURN)
    		throw new IllegalStateException("return cmd has no args");
       return arg1;
    }

    /**
     * Return the second argument of current command
     * Be called when it is PUSH, POP, FUNCTION or CALL
     * @return
     */
    public int arg2(){
    	if(cmdtype==PUSH||cmdtype==POP||cmdtype==FUNCTION||cmdtype==CALL)
    		return arg2;
    	throw new IllegalStateException("no arg2");
      
    }

    
   
}
