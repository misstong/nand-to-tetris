import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class VMtranslator {
	private static List<File> getFiles(File directory){
		
		File[] vms= directory.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File arg0, String arg1) {
				// TODO Auto-generated method stub
				if(arg1.endsWith(".vm"))
					return true;
				return false;
			}
			
		});
		return Arrays.asList(vms);
	}
   
    public static void main(String[] args) {
    	if(args.length!=1) {
    		System.out.print("Usage:");
    	}else {
    		String arg=args[0];
    		File f=new File(arg);
    		List<File> inputFiles=new ArrayList<File>();
    		String outPath="";
    		CodeWriter writer;
    		if(f.isDirectory()) {
    			inputFiles=getFiles(f);
    			if(inputFiles.size()==0)
    				throw new IllegalArgumentException("this directory not contain any vm files");
    			
    			outPath=f.getAbsolutePath()+"/"+f.getName()+".asm";
    			
    			
    		}else  if(f.isFile()){
    			if(!f.getAbsolutePath().endsWith(".vm"))
    				throw new IllegalArgumentException(".vm file required");
    			inputFiles.add(f);
    			outPath=f.getAbsolutePath().substring(0,f.getAbsolutePath().lastIndexOf("."))+".asm";
    		}
    		
    		writer=new CodeWriter(new File(outPath));
    		for(File vm:inputFiles) {
    			Parser p=new Parser(vm);
    			while(p.hasMoreCommands()) {
    				p.advance();
    				int type=p.commandType();
    				if(type==Parser.ARITHMETIC) {
    					writer.writeArithmetic(p.arg1());
    				}else if(type==Parser.PUSH||type==Parser.POP)
    				{
    					writer.writePushPop(type, p.arg1(), p.arg2());
    				}
    			}
    		}
    		writer.close();
    		
    		System.out.println("Translation done");
    	}
    	
    }
}
