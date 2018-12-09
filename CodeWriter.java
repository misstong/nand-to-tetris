import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeWriter {

   private PrintWriter writer;
   private int arthJumpFlag=0;
   private int labelCnt=0;
   private String fileName="";
   private Pattern id=Pattern.compile("^[^0-9][0-9A-Za-z\\.\\:\\_\\$]+");
   
    /**
     * Open an output file and be ready to write content
     * @param fileOut can be a directory!
     */
    public CodeWriter(File fileOut) {
    	try {
			writer=new PrintWriter(fileOut);
			fileName=fileOut.getName();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       
    }

    /**
     *¡°If the program¡¯s argument is a directory name rather than a file name,
     * the main program should process all the .vm files in this directory.
     * In doing so, it should use a separate Parser for handling each input file and a single CodeWriter for handling the output.¡±
     *
     * Inform the CodeWrither that the translation of a new VM file is started
     */
    public void setFileName(File fileOut){
    	fileName=fileOut.getName();
    }

    /**
     * write assembly code for label cmd
     * @param label
     */
    public void writeLabel(String label) {
    	Matcher m=id.matcher(label);
    	if(m.matches()) {
    		writer.print("("+label+")\n");
    	}else {
    		throw new IllegalArgumentException("wrong label format");
    	}
    }
    
    /**
     * write assembly code for goto command
     * @param label
     */
    public void writeGoto(String label) {
    	Matcher m=id.matcher(label);
    	if(m.matches()) {
    		writer.print("@"+label+"\n0;JMP\n");
    	}else {
    		throw new IllegalArgumentException("wrong label format");
    	}
    }
    /**
     * write assembly code for if-goto command
     * @param
     */
    public void writeIf(String label) {
    	Matcher m=id.matcher(label);
    	if(m.matches()) {
    		writer.print(arithmeticTemplate1()+"@"+label+"\nD;JNE\n");
    	}else {
    		throw new IllegalArgumentException("wrong label format");
    	}
    }
    
    /**
     * Write assembly code that effects the VM initialization
     * also called BOOTSTRAP CODE.
     * This code must be placed at the beginning of the output file
     */
    public void writeInit(){

        writer.print("@256\n" +
                         "D=A\n" +
                         "@SP\n" +
                         "M=D\n");
        writeCall("Sys.init",0);

    }

    /**
     * Write assembly code that effects the call command
     * @param functionName
     * @param numArgs
     */
    public void writeCall(String functionName, int numArgs){
    	String newLabel = "RETURN_LABEL" + (labelCnt++);

    	writer.print("@" + newLabel + "\n" + "D=A\n@SP\nA=M\nM=D\n@SP\nM=M+1\n");//push return address
    	writer.print(pushTemplate1("LCL",0,true));//push LCL
    	writer.print(pushTemplate1("ARG",0,true));//push ARG
        writer.print(pushTemplate1("THIS",0,true));//push THIS
        writer.print(pushTemplate1("THAT",0,true));//push THAT

        writer.print("@SP\n" +
                        "D=M\n" +
                        "@5\n" +
                        "D=D-A\n" +
                        "@" + numArgs + "\n" +
                        "D=D-A\n" +
                        "@ARG\n" +
                        "M=D\n" +
                        "@SP\n" +
                        "D=M\n" +
                        "@LCL\n" +
                        "M=D\n" +
                        "@" + functionName + "\n" +
                        "0;JMP\n" +
                        "(" + newLabel + ")\n"
                        );
    }
    
    /**
     * Write assembly code that effects the return command
     */
    public void writeReturn(){

    	writer.print(returnTemplate());

    }

    /**
     * Write assembly code that effects the function command
     * @param functionName
     * @param numLocals
     */
    public void writeFunction(String functionName, int numLocals){

        writer.print("(" + functionName +")\n");

        for (int i = 0; i < numLocals; i++){

            writePushPop(Parser.PUSH,"constant",0);

        }

    }

    /**
     * save value of pre frame to given position
     * @param position
     * @return
     */
    public String preFrameTemplate(String position){

        return "@R11\n" +
                "D=M-1\n" +
                "AM=D\n" +
                "D=M\n" +
                "@" + position + "\n" +
                "M=D\n";

    }

    /**
     * assembly code template for return command
     * use R13 for FRAME R14 for RET
     * @return
     */
    public String returnTemplate(){

        return "@LCL\n" +
                "D=M\n" +
                "@R11\n" +
                "M=D\n" +
                "@5\n" +
                "A=D-A\n" +
                "D=M\n" +
                "@R12\n" +
                "M=D\n" +
                popTemplate1("ARG",0,false) +
                "@ARG\n" +
                "D=M\n" +
                "@SP\n" +
                "M=D+1\n" +
                preFrameTemplate("THAT") +
                preFrameTemplate("THIS") +
                preFrameTemplate("ARG") +
                preFrameTemplate("LCL") +
                "@R12\n" +
                "A=M\n" +
                "0;JMP\n";
    }
    /**
     * Write the assembly code that is the translation of the given arithmetic command
     * @param command
     */
    public void writeArithmetic(String command){

      if(command.equals("add")) {
    	  writer.print(arithmeticTemplate1()+"M=M+D\n");
      }else if(command.equals("sub")) {
    	  writer.print(arithmeticTemplate1()+"M=M-D\n");
      }else if(command.equals("and")) {
    	  writer.print(arithmeticTemplate1()+"M=M&D\n");
      }else if(command.equals("or")) {
    	  writer.print(arithmeticTemplate1()+"M=M|D\n");
      }else if(command.equals("gt")) {
    	  writer.print(arithmeticTemplate2("JLE"));
      }else if(command.equals("lt")) {
    	  writer.print(arithmeticTemplate2("JGE"));
      }else if(command.equals("eq")) {
    	  writer.print(arithmeticTemplate2("JNE"));
      }else if(command.equals("not")) {
    	  writer.print("@SP\nA=M-1\nM=!M\n");
      }else if(command.equals("neg")) {
    	  writer.print("D=0\n@SP\nA=M-1\nM=D-M\n");
      }else {
    	  throw new IllegalArgumentException("call writeArithmetic");
      }

    }

    /**
     * Write the assembly code that is the translation of the given command
     * where the command is either PUSH or POP
     * @param command PUSH or POP
     * @param segment
     * @param index
     */
    public void writePushPop(int command, String segment, int index){

       if(command==Parser.PUSH) {
    	   if (segment.equals("constant")){

    		   writer.print("@" + index + "\n" + "D=A\n@SP\nA=M\nM=D\n@SP\nM=M+1\n");

           }else if (segment.equals("local")){

        	   writer.print(pushTemplate1("LCL",index,false));

           }else if (segment.equals("argument")){

        	   writer.print(pushTemplate1("ARG",index,false));

           }else if (segment.equals("this")){

        	   writer.print(pushTemplate1("THIS",index,false));

           }else if (segment.equals("that")){

        	   writer.print(pushTemplate1("THAT",index,false));

           }else if (segment.equals("temp")){

        	   writer.print(pushTemplate1("R5", index + 5,false));

           }else if (segment.equals("pointer") && index == 0){

        	   writer.print(pushTemplate1("THIS",index,true));

           }else if (segment.equals("pointer") && index == 1){

        	   writer.print(pushTemplate1("THAT",index,true));

           }else if (segment.equals("static")){
               //every file has its static space
        	   writer.print("@" + fileName + index + "\n" + "D=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n");

           }
       }else if(command==Parser.POP) {
    	   if (segment.equals("local")){

               writer.print(popTemplate1("LCL",index,false));

           }else if (segment.equals("argument")){

        	   writer.print(popTemplate1("ARG",index,false));

           }else if (segment.equals("this")){

        	   writer.print(popTemplate1("THIS",index,false));

           }else if (segment.equals("that")){

        	   writer.print(popTemplate1("THAT",index,false));

           }else if (segment.equals("temp")){

        	   writer.print(popTemplate1("R5", index + 5,false));

           }else if (segment.equals("pointer") && index == 0){

        	   writer.print(popTemplate1("THIS",index,true));

           }else if (segment.equals("pointer") && index == 1){

        	   writer.print(popTemplate1("THAT",index,true));

           }else if (segment.equals("static")){
               //every file has its static space
        	   writer.print("@" + fileName + index + "\nD=A\n@R13\nM=D\n@SP\nAM=M-1\nD=M\n@R13\nA=M\nM=D\n");

           }
       }
    }

    /**
     * Close the output file
     */
    public void close(){
    	writer.close();
      

    }

    /* Template for add sub and or
    * @return
    */
   private String arithmeticTemplate1(){

       return "@SP\n" +
               "AM=M-1\n" +
               "D=M\n" +
               "A=A-1\n";

   }

   /**
    * Template for gt lt eq
    * @param type JLE JGT JEQ
    * @return
    */
   private String arithmeticTemplate2(String type){
	   arthJumpFlag++;
       return "@SP\n" +
               "AM=M-1\n" +
               "D=M\n" +
               "A=A-1\n" +
               "D=M-D\n" +
               "@FALSE" + arthJumpFlag + "\n" +
               "D;" + type + "\n" +
               "@SP\n" +
               "A=M-1\n" +
               "M=-1\n" +
               "@CONTINUE" + arthJumpFlag + "\n" +
               "0;JMP\n" +
               "(FALSE" + arthJumpFlag + ")\n" +
               "@SP\n" +
               "A=M-1\n" +
               "M=0\n" +
               "(CONTINUE" + arthJumpFlag + ")\n";

   }
   /**
    * Template for push local,this,that,argument,temp,pointer,static
    * @param segment
    * @param index
    * @param isDirect Is this command a direct addressing?
    * @return
    */
   private String pushTemplate1(String segment, int index, boolean isDirect){

       //When it is a pointer, just read the data stored in THIS or THAT
       String noPointerCode = (isDirect)? "" : "@" + index + "\n" + "A=D+A\nD=M\n";

       return "@" + segment + "\n" +
               "D=M\n"+
               noPointerCode +
               "@SP\n" +
               "A=M\n" +
               "M=D\n" +
               "@SP\n" +
               "M=M+1\n";

   }

   /**
    * Template for pop local,this,that,argument,temp,pointer,static
    * @param segment
    * @param index
    * @param isDirect Is this command a direct addressing?
    * @return
    */
   private String popTemplate1(String segment, int index, boolean isDirect){

       //When it is a pointer R13 will store the address of THIS or THAT
       String noPointerCode = (isDirect)? "D=A\n" : "D=M\n@" + index + "\nD=D+A\n";

       return "@" + segment + "\n" +
               noPointerCode +
               "@R13\n" +
               "M=D\n" +
               "@SP\n" +
               "AM=M-1\n" +
               "D=M\n" +
               "@R13\n" +
               "A=M\n" +
               "M=D\n";

   }

}
