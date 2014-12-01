import java.io.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class Disassembler {
	
	//Disassembler Variables
	static ArrayList<String> toConvert = new ArrayList<String>();
	static ArrayList<String> dataArray = new ArrayList<String>();
	static ArrayList<String> codeArray = new ArrayList<String>();
	static int msgcounter=1;
	static int ifcount=0;
	static int ifelsecount=0;
	static int forcount=0;
	static int docount=0;
	static int whilecount=0;
	static boolean includeDisplay=false;
	
	public static void main(String[] args){
		Disassembler d = new Disassembler();
		d.toAssembly();
	}

	public void toAssembly() {
		
		String filename = "";

		try {
			
			Scanner input = new Scanner (System.in);
			System.out.println("Enter .java filename: ");
			filename = input.nextLine();
			BufferedReader inputStream = new BufferedReader(new FileReader(filename));
			
			// 1. Place Source Code in a Single String Variable
			
			String line = "";
			String sourceCode="";
			while ((line = inputStream.readLine()) != null) {
				line=line.trim();
				sourceCode+=line;
			}
			
			// 2. Clean Source Code String
				sourceCode.trim();
				sourceCode=sourceCode.replace(sourceCode.substring(0,1+sourceCode.indexOf("{")),"");
				sourceCode=sourceCode.substring(0,sourceCode.length()-2);
				sourceCode=sourceCode.replace("{","{;");
				sourceCode=sourceCode.replace("}",";};");
			// 3. Tokenize sourceCode by Blocks {,} or by ;
				
				StringTokenizer st = new StringTokenizer(sourceCode, ";");
				while (st.hasMoreTokens()) {
					String token = st.nextToken();
			        System.out.println(token);
					toConvert.add(token);
			     }
			convertToAssembly(filename);
			
		}
		
		catch (FileNotFoundException e) {
			System.err.println("File not found.");
		} 
		catch (IOException e) {
			System.err.println("Error in filestream.");
		}
		
	} //end function
	
	public void convertToAssembly (String filename) throws IOException {
		
		String display =
				"\tdisplay proc\n";                           
				display+= "\t\tmov bx, 10\n";                
				display+= "\t\tmov dx, 0000h \n";                
				display+= "\t\tmov cx, 0000h \n";                   

				display+= "\t\tDloop1: \n";
				display+= "\t\t\tmov dx, 0000h \n";                   
				display+= "\t\t\tdiv bx \n";                          
				display+= "\t\t\tpush dx \n";                         
				display+= "\t\t\tinc cx \n";                         
				display+= "\t\t\tcmp ax, 0 \n";                 
				display+= "\t\t\tjne Dloop1 \n";         

				display+= "\t\tDloop2: \n";
				display+= "\t\t\tpop dx \n";                     
				display+= "\t\t\tadd dx, 30h \n";            
				display+= "\t\t\tmov ah, 02h \n";                            
				display+= "\t\t\tint 21h \n";                    
				display+= "\t\t\tloop Dloop2 \n";             
				display+= "\t\tret \n";     
				display+= "\tdisplay endp \n";
		String newFilename = "";
		String title = "title";
		String model = ".model small";
		String stack = ".stack 100h";
		BufferedReader output = new BufferedReader(new InputStreamReader(System.in));
		PrintWriter outputStream = null;
		dataArray.add(".data");
		
		//add data variables here
		for(int i=0; i<toConvert.size();i++) {
			String currLine = toConvert.get(i);
			
			//Block Statements
			if(currLine.contains("{")){
				//Insert Code for Handling (If,Else,While,etc.)
			}
			//Line Statement Handler
			else{
				//Insert Code for Translating a java Non-Block Statement into Assembly
				//Set includeDisplay to true if Printing of Variables is required
			}
		}
		
		
		codeArray.add("\n\tmov ax, 4c00h \n \tint 21h");
		codeArray.add("\tmain endp \n \tend main");
		
		newFilename = getNewFilename(filename);
		title = title + " " + newFilename;
		
		try {
			outputStream =  new PrintWriter(new FileOutputStream(newFilename + ".asm"));
			outputStream.println(title);
			outputStream.println(model);
			outputStream.println(stack);
			for (int e = 0; e < dataArray.size(); e++) {
				outputStream.println(dataArray.get(e));
			}
			
			outputStream.println(".code");
			if(includeDisplay){outputStream.println(display);}
			outputStream.println("\tmain proc");
			outputStream.println("\tmov ax, @data \n \tmov ds, ax\n");
			
			
			for (int f = 0; f < codeArray.size(); f++) {
				outputStream.println(codeArray.get(f));
			}
			outputStream.close();
			
			//Print Contents of .asm file
			for(String item: dataArray){
				System.out.println(item);
			}
			
			for(String item: codeArray){
				System.out.println(item);
			}
			
			System.out.println("successful");
		}
		
		catch (FileNotFoundException e) {
			System.err.println("No file found.");
			System.exit(0);
		}
	}
	
	//Block Functions
	
	// 1. Function for Converting Non-Block Statements (must know its encapsulating block: { })
	// defined by { - int start
	// defined by } - int end
	// * tabs may be useful for asm code generation - int tabcount
	public void convertNBToAssembly(int start, int end, int tabcount){
		
		//Place Tabs for .asm Code Readability
		String tab="";
		for(int j=0;j<tabcount;j++){tab+="\t";}
		String  newLine = tab+"mov dl, 0ah \n";
		newLine +=tab+"mov ah, 02h \n";
		newLine += tab+"int 21h";
		
		for(int i=start;i<end;i++){
			String currLine = toConvert.get(i);
			
			// A. Multi-Level Block Handling
			if(currLine.contains("{")){
				//Insert Code to Handle Inner If-Else-While, etc. Blocks
			}
			
			// B. Line for Line Converter
			else{
				// 1. Print Message Strings
				// Cases Handled: System.out.println("Hello world!");
				if (currLine.contains("System.out")&&currLine.contains("\"")) {
					String msgString = getMsgString (currLine);
					dataArray.add("\tmsg" + msgcounter + " db " +  '\''  + msgString + "\'," + "'$'");	
								
					codeArray.add(tab+"lea dx, " + "msg" + msgcounter);
					codeArray.add(tab+"mov ah, 09h");
					codeArray.add(tab+"int 21h");
					codeArray.add(newLine);
					msgcounter++;
				}
				// 1.2 Print Numbers
				// Cases Handled: int a = 255; System.out.println(a);
				else if (currLine.contains("System.out")&&!currLine.contains("\"")) {
					includeDisplay=true;
					String numString = getNumString(currLine);
					codeArray.add(tab+"mov ax, "+numString);
					codeArray.add(tab+"call display");
					codeArray.add(newLine);
				}
				// 2. Integer Variables (Declaration) int a=12; int b;
				else if (currLine.contains("int")) {
					String intString = getIntString(currLine);
								
					dataArray.add(intString);
				}
				// 3. Integer Variables (Assignment) 
				//Cases Handled: x = 3 + 2; x = 3 - 2; x++; x--; x = 0;
				else if (currLine.contains("=")||
					currLine.contains("++")||currLine.contains("--")) {
					getAssignIntString(currLine,tabcount);
				}
			}
		}
		
	}
	
	// 2. Function for Converting Block Statements (If,If-Else,For,Do-While,While)
	public int convertBToAssembly(int start,String bstate, int tabcount){
		int i=start;
		if(bstate.contains("if")){
			//Differentiate Between If and If-Else Code
		}
		else if(bstate.contains("do")){
			//Add Code for dowhile
		}
		else if(bstate.contains("while")){
			//Add Code for while
		}
		else if(bstate.contains("for")){
			//Add Code for for
		}
		return i;
	}
	
	//Block Statement Parser Functions
	
	// 1. If Block Converter
		public void convertIf(int start, int end, int tabcount){
			// Add Code Here...
		}
	// 2. If-Else Block Converter
	// Contains more arguments because if-else deals with 2 statement blocks
		public void convertIfElse(int ifstart, int ifend, int elsestart, int elseend, int tabcount){
			// Add Code Here...
		}
	
	//Helper Functions for Block Statements
	
	// 1. Function to find the corresponding '}' of a block statement
		public int findEnd(int start){
			int opencount=0;
			int closecount=-1;
			for(int j=start;j<toConvert.size();j++){
				String line = toConvert.get(j);
				if(line.contains("{"))opencount++;
				else if(line.contains("}")){
					closecount++;
					if(opencount==closecount){
						return j;
					}
				}
			}
			return -1;
		}
	
	// 2. If-Else vs If Block Identifier
		public int checkElse(int start){
			int opencount=0;
			int closecount=-1;
			for(int j=start;j<toConvert.size();j++){
				String line = toConvert.get(j);
				if(opencount==closecount&&!line.contains("else")){break;}
				if(opencount==closecount&&line.contains("else")){
					return j;
				}
				if(line.contains("{"))opencount++;
				else if(line.contains("}")){
					closecount++;
					
				}
				
			}
			return -1;
		}
	
	//Miscellaneous Helper Functions
	
	// 1. Function for Message Strings
		public String getMsgString (String currLine) {
			
			int g = 0;
			int h = 0;
			String newLine = "";
			
			for (int i = 0; i < currLine.length(); i++) {
				char curr = currLine.charAt(i);
				if (curr == '"') {
					g = i;
					break;
				}
				
			}
			
			for (int j = 0; j < currLine.length(); j++) {
				char curr = currLine.charAt(j);
				if (curr == '"' && j != g) {
					h = j;
					break;
				}
			}
			
			g++;
			newLine = currLine.substring(g, h);
			return newLine;
			
		} //end function
	
	// 1.2 Function for Printing Variables
		public String getNumString(String currLine){
			String newLine="";
			currLine=currLine.trim();
			int a = currLine.indexOf("(");
			int b = currLine.indexOf(")");
			newLine = currLine.substring(a+1,b);
			return newLine;
		}
		
	// 2. Function for Int Variables
		public String getIntString(String currLine){
			String newLine="";
			currLine=currLine.trim();
			currLine=currLine.replaceFirst("int", "");
			currLine=currLine.replaceFirst("static", "");
			currLine=currLine.replace(" ", "");
			// First Case: a=0; or a=255;
			if(currLine.contains("=")){
				int a = currLine.indexOf("=");
				int end = currLine.length();
				int value = Integer.parseInt(currLine.substring(a+1,end));
				String varname = currLine.substring(0,a);
				newLine ="\t"+ varname + " dw "+ value;
				
			}
			// Second Case: a; longvarname;
			else{
				int end = currLine.length();
				String varname = currLine.substring(0,end);
				newLine ="\t"+ varname + " dw ?";
			}
			return newLine;
		}// end function
	
	// 3. Function for Int Variables (Mid-code Assignment)
		public void getAssignIntString(String currLine, int tabcount){
			String tab="";
			for(int j=0;j<tabcount;j++){tab+="\t";}
			currLine=currLine.trim();
			currLine=currLine.replace(" ","");
			// x++;
			if(currLine.contains("++")){
				int a = currLine.indexOf("+");
				String varname = currLine.substring(0,a);
				codeArray.add(tab+"inc "+ varname);
			}
			// x--;
			else if(currLine.contains("--")){
				int a = currLine.indexOf("-");
				String varname = currLine.substring(0,a);
				codeArray.add(tab+"dec "+ varname);
			}
			else{
				String fOp="";
				String sOp="";
				int a = currLine.indexOf("=");
				String varname = currLine.substring(0,a);
				// x=3+2;
				if(currLine.contains("+")||currLine.contains("-")){
					String sign = "";
					String op = "";
					if(currLine.contains("+")){sign="+";op="add ";}
					if(currLine.contains("-")){sign="-";op="sub ";}
					
					int findex = currLine.indexOf(sign);
					int end = currLine.length();
					fOp = currLine.substring(a+1,findex);
					sOp = currLine.substring(findex+1,end);
					// x = x + y; x = x - y;
					if(!isInteger(fOp)){
						codeArray.add(tab+"mov cx,"+fOp);
						codeArray.add(tab+"mov "+varname+",cx");
					}
					else{
						codeArray.add(tab+"mov "+varname+","+fOp);
					}
					if(!isInteger(sOp)){
						codeArray.add(tab+"mov cx,"+sOp);
						codeArray.add(tab+op+varname+",cx");
					}
					else{
						codeArray.add(tab+op+varname+","+sOp);
					}
				}
				// x = 0;
				else{
					int eq = currLine.indexOf("=");
					int end = currLine.length();
					sOp = currLine.substring(eq+1,end);
					if(!isInteger(sOp)){
						codeArray.add(tab+"mov cx,"+sOp);
						codeArray.add(tab+"mov "+varname+",cx");
					}
					else{codeArray.add(tab+"mov "+varname+","+sOp);}
					
				}
			}
		}//end function
	
		// 4. Function for Equality Comparators (jxx setter)
		public String setCond(String bstate){
			String cond="";
			if(bstate.contains("==")){cond = "jne";}
			else if(bstate.contains(">=")){cond = "jl";}
			else if(bstate.contains("<=")){cond = "jg";}
			else if(bstate.contains(">")){cond = "jle";}
			else if(bstate.contains("<")){cond = "jge";}
			return cond;
		}
		// 5. Tokenize Left Side of Equality Comparator
		public String setLeftExpr(String bstate){
			String left="";
			if(bstate.contains("==")){left = bstate.substring(bstate.indexOf("(")+1,bstate.indexOf("="));}
			else if(bstate.contains(">=")){left = bstate.substring(bstate.indexOf("(")+1,bstate.indexOf(">"));}
			else if(bstate.contains("<=")){left = bstate.substring(bstate.indexOf("(")+1,bstate.indexOf("<"));}
			else if(bstate.contains(">")){left = bstate.substring(bstate.indexOf("(")+1,bstate.indexOf(">"));}
			else if(bstate.contains("<")){left = bstate.substring(bstate.indexOf("(")+1,bstate.indexOf("<"));}
			return left;
		}
		// 6. Tokenize Right Side of Equality Comparator
		public String setRightExpr(String bstate){
			String right="";
			if(bstate.contains("==")){right = bstate.substring(bstate.indexOf("=",bstate.indexOf("=")+1)+1,bstate.indexOf(")"));}
			else if(bstate.contains(">=")){right = bstate.substring(bstate.indexOf("=")+1,bstate.indexOf(")"));}
			else if(bstate.contains("<=")){right = bstate.substring(bstate.indexOf("=")+1,bstate.indexOf(")"));}
			else if(bstate.contains(">")){right = bstate.substring(bstate.indexOf(">")+1,bstate.indexOf(")"));}
			else if(bstate.contains("<")){right = bstate.substring(bstate.indexOf("<")+1,bstate.indexOf(")"));}
			return right;
		}
	
	public String getNewFilename(String filename) {
		
		int d = 0;
		
		for (int c = 0; c < filename.length(); c++) {
			char curr = filename.charAt(c);
			if (curr == '.') {
				d = c;
				break;
			}
		}
		filename = filename.substring(0, d);
		return filename;
		
	} //end function
	public static boolean isInteger(String s) {
	    try { 
	        Integer.parseInt(s); 
	    } catch(NumberFormatException e) { 
	        return false; 
	    }
	    // only got here if we didn't return false
	    return true;
	}
	
	
} //end class
