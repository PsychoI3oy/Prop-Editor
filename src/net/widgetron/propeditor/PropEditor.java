
/**
 * @author evan
 *the "Controller" for the MVC architecture. 
 * This class coordinates a PropFile and a list of suggestions
 * @version 0.3
 */
package net.widgetron.propeditor;

import java.io.*;

public class PropEditor {
	private PropFile pF; 
	private SuggestionFile sF;
	
	
	public PropEditor(BufferedReader pFileHandle, BufferedReader sFile){		
	/*	if (fName.substring(fName.lastIndexOf('.')+1,fName.length()).equals("prop")){
			pFileName = fName;*/
			PropFile pFile = openPropFile(pFileHandle);
			if( pFile != null){
				//	System.out.println("Opened prop file");
				this.pF = pFile;
				openSugFile(sFile);			
			}
			
		/*}else{
			if(loadSession(fName)){
				openSugFile(sFile);
			}
		}*/
			
	}
	
	public PropFile openPropFile(BufferedReader pFileHandle){
		PropFile pFil;
		try {
			pFil = new PropFile(pFileHandle);
			return pFil;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void closePropFile(){
		pF = null;
	}
	
	public void openSugFile(BufferedReader sugFile){
		SuggestionFile sF ;
		try {
			sF = new SuggestionFile(sugFile);
			this.sF = sF;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public boolean saveSession(String filename){
		try {
			FileOutputStream fS = new FileOutputStream(filename);
			ObjectOutputStream oS = new ObjectOutputStream(fS);
			oS.writeObject(pF);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	public boolean loadSession(String filename){
		try {
			FileInputStream fS = new FileInputStream(filename);
			ObjectInputStream oS = new ObjectInputStream(fS);
			pF = (PropFile) oS.readObject();
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	public SuggestionFile getSugFile(){
		return sF;
	}
	public PropFile getPropFile(){
		return pF;
	}

	public String[] getSugs(String suggestFor, String edMode){
		if(edMode.equals("Prop")){
			return  sF.getNameSugs();
		}else{
			return  sF.getValSugs(suggestFor);
		}
	}
	
}
