/**
* @author Evan Widger
* wrapper for suggestion file, a csv type file to suggest common edits to build.prop files in 
property.name;val1|val2|val3 
* format
* @version 0.3
*/
package net.widgetron.propeditor;

import java.io.*;
import java.util.*;



public class SuggestionFile {
	private Hashtable<String,ArrayList<String>> sugList;
	/**
	 * 
	 */
	public SuggestionFile(BufferedReader fh) throws FileNotFoundException, IOException {				
		sugList = parseFile(fh);	
		fh.close();
	}
	public Hashtable<String,ArrayList<String>> parseFile(BufferedReader sFile) throws FileNotFoundException,IOException{
		Hashtable<String,ArrayList<String>> sList = new Hashtable<String,ArrayList<String>>();

		Scanner scanner = new Scanner(sFile);
		try {
			//first use a Scanner to get each line
			while ( scanner.hasNextLine() ){
				String nextline = scanner.nextLine();
	
				Scanner scanner2 = new Scanner(nextline);
				scanner2.useDelimiter(";");
					
				if ( scanner2.hasNext() ){
				  	try{
				    	String name = scanner2.next();
						String values = scanner2.next();
						Scanner scanner3 = new Scanner(values);
						scanner3.useDelimiter("\\|");
						ArrayList<String> tmpValues = new ArrayList<String>();
						while ( scanner3.hasNext()){
							try{
								tmpValues.add( scanner3.next() );
							}catch(Exception e){
								e.printStackTrace();
							}
						}
							sList.put(name,tmpValues);					
							scanner3.close();
				    	}catch(Exception e){
				    		e.printStackTrace();
				    	}		    	  
				//(no need for finally here, since String is source)
				scanner2.close();
				}
			}
		}finally {
			//ensure the underlying stream is always closed
			scanner.close();
		}
		return sList; 	
	}
	
	public String[] getValSugs(String propName){
		if( ! sugList.containsKey(propName)){
			return new String[0];
		}else{
			String[] str = new String [sugList.get(propName).size()];
			return sugList.get(propName).toArray(str);
		}
	}
	public String[] getNameSugs(){
		
		String[] tmpString = new String[sugList.size()]; 
		int i = 0;
		Enumeration<String> keys = sugList.keys();
		while (  keys.hasMoreElements()  )
		   {
			tmpString[i]   = (String)keys.nextElement();
			i++;
		   } 
		return tmpString;
	}
	
	//this is only used by MakeSuggestionFile
	public Hashtable<String,ArrayList<String>> getSugList(){
		return sugList;
	}

}
