package net.widgetron.propeditor;
 
import java.io.*;

import android.app.*; 
import android.os.Bundle; 
import android.util.Log;
import android.view.*;
import android.widget.*;
import android.content.*;
import android.widget.AdapterView.OnItemClickListener;

public class PEEditScreen extends Activity {
	private final int MANUAL_DIALOG = 1;
	private final String TAG = "PropEditor:EditScreen";
	
	
	private Runtime rt;
	static PropEditor pE;
	
	private String workingfilename;
	private String originalFileName;
	private ListView propList;
	
	private String currProp;
	private String jlSugtitle;
	private String thingString;
	private String editMode;
	protected String selectedEdit;
	private String[] sugFullList;

	private String sdDir = "/propeditor/";
	
/** Called when the activity is first created. */ 
	@Override 
	public void onCreate(Bundle icicle) { 
		super.onCreate(icicle); 
		/* Make this application use  
		* the editor.xml-layout-file. */ 
		this.setContentView(R.layout.editor); 
		
		propList = (ListView)findViewById(R.id.PropList);
	    
		Bundle bundle = getIntent().getExtras();

		//Next extract the values using the key as
		this.originalFileName  = bundle.getString("fn");
		//open suggestionfile
		InputStream is = this.getResources().openRawResource(R.raw.suggestionfile);
		BufferedReader sFile= new BufferedReader(new InputStreamReader(is));
		this.rt = Runtime.getRuntime();
		this.sdDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + sdDir;
		workingfilename = sdDir + "tmp.prop";
		if (originalFileName.startsWith("/system/")){
		
			suCommand("cp " + originalFileName + " " + workingfilename);
		}else{
			workingfilename = originalFileName;
		}
		
		
		
		BufferedReader pFile;
		try {
			pFile = new BufferedReader(new InputStreamReader(new FileInputStream(this.workingfilename)));
			PEEditScreen.pE = new PropEditor(pFile, sFile); 
		} catch (FileNotFoundException e) {
			Toast.makeText(getApplicationContext(), "Failed to open PropFile", Toast.LENGTH_SHORT).show();
			Log.e(TAG, "Failed to open PropFile", e);
			this.finish();	
		}		
	} 
	
  	public void onResume(){
  		super.onResume();  		
  		String propListArr[] = pE.getPropFile().getAllProps();
		propList.setAdapter(new ArrayAdapter<String>(this, R.layout.mylist , propListArr));
		propList.setTextFilterEnabled(true);
		propList.setOnItemClickListener(new ModHandler());
  	}
  	
	
/// MENUS
  
  
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.options_menu, menu);
		return true;
	}
 //Handles item selections 
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.save:
			saveFile();
			return true;
		case R.id.add:
			addProp();
			return true;
		case R.id.save_as:
			saveAs();
			return true;
		case R.id.merge:
			mergeFiles();
			return true;
		}
		return false;
	}
	void saveFileFail(){
		Toast.makeText(getApplicationContext(), "Failed to save PropFile", Toast.LENGTH_SHORT).show();
	}
	
	protected Dialog onCreateDialog(int id){
		  Dialog dialog;
		    switch(id) {
		    case MANUAL_DIALOG:
		    	final EditText input = new EditText(PEEditScreen.this);
		    	dialog = new 	AlertDialog.Builder(PEEditScreen.this).setTitle("Custom entry").setView(
						input).setPositiveButton(android.R.string.ok , new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								selectedEdit = input.getText().toString();
								modifyProps();
								input.setText("", TextView.BufferType.EDITABLE);
								
							}
						}
						).setNegativeButton(android.R.string.cancel , new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {}
						}).create();
		        break;			
		    default:
		        dialog = null;
		    }
		    return dialog;

	}
	protected void onPrepareDialog(int id, Dialog dialog){
	    switch(id) {
	    case MANUAL_DIALOG:
	    	dialog.setTitle("Manual entry for " + editMode + thingString);
	        break;
	    default:
	        dialog = null;
	    }
	}
/// suggestion screen alert dialog list
	
  	public void suggestionScreen(String suggestFor, String edMode){
		editMode = edMode;
		//begin suggestion screen
		
		String[] sugList = pE.getSugs(suggestFor, edMode);		

		sugFullList = new String[sugList.length + 1];
		System.arraycopy(sugList, 0, sugFullList, 0, sugList.length);
		sugFullList[sugList.length] = "Manual Entry";
		
		if(editMode.equals("Prop")){
			jlSugtitle = "Property name suggestions:";
		}else{
			jlSugtitle = "Value suggestions for: ";
		}
				
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(jlSugtitle + suggestFor);
		builder.setItems(sugFullList, new SuggestionHandler());
		AlertDialog alert = builder.create();
		alert.show();
	}
  	public void saveFile(){
  		if (workingfilename == originalFileName){
	  		try {
				if (pE.getPropFile().saveFile(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(workingfilename)))) ){
					saveFileSuccess();
				}else{
					saveFileFail();
				}
			} catch (FileNotFoundException e) {
				android.util.Log.e(TAG, "failed to save file", e);
				saveFileFail();
			}
		}else{
			try {
				if (pE.getPropFile().saveFile(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(workingfilename)))) ){
					if (! suCommand("busybox mount -o rw,remount /system")){
						Toast.makeText(getApplicationContext(), "failed to remount system rw", Toast.LENGTH_SHORT).show();
					}else{
						if (! suCommand("cp " + workingfilename + " " + originalFileName)){
							suCommand("busybox mount -o ro,remount /system");
							Toast.makeText(getApplicationContext(), "failed to copy working copy to system", Toast.LENGTH_SHORT).show();
						}else{
							suCommand("busybox mount -o ro,remount /system");
							Toast.makeText(getApplicationContext(), "Saved " + originalFileName + " to /system; reboot for changes to take effect", Toast.LENGTH_LONG).show();
						}
					}
				}
			} catch (Exception e) {
				android.util.Log.e(TAG, "failed to save file", e);
				saveFileFail();
			}
		}
  	}
  	
  	public void saveAs() {
		CharSequence message = "Enter filename to save as";
		final EditText input = new EditText(PEEditScreen.this);
		input.setText(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + '/', TextView.BufferType.EDITABLE);
		new AlertDialog.Builder(PEEditScreen.this).setTitle("Enter Filename").setMessage(message ).setView(
				input).setPositiveButton(android.R.string.ok , new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String pFileName = input.getText().toString();
						try{
							if (! pE.getPropFile().saveFile(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pFileName))))  ){
								android.util.Log.d(TAG, "savefile returned false ");
								saveFileFail();
							}else{
								saveFileSuccess();
							}
						}catch(Exception e){
							android.util.Log.e(TAG, "OHSHI--", e);
							saveFileFail();
						}
						
					}
				}).setNegativeButton(android.R.string.cancel , new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				}).show();
	}
	private void saveFileSuccess() {
		Toast.makeText(getApplicationContext(), "PropFile saved", Toast.LENGTH_SHORT).show();
	}
  	void modifyProps (){
		if (!(selectedEdit == null)){ 
			if(editMode.equals("Prop")){
				currProp = selectedEdit;
				suggestionScreen(currProp, "Value");
			}else{
				if (pE.getPropFile().modProp(currProp, selectedEdit)){
					onResume();
			
				}else{
					Toast.makeText(getApplicationContext(), "Illegal character in property name/value.", Toast.LENGTH_SHORT).show();
				}	
				currProp = null;
			  	jlSugtitle = null;
			  	thingString = null;
			  	editMode = null;
			  	selectedEdit = null;
			}
		}
	}

	void addProp () {
			suggestionScreen("","Prop");
	}
	
	void mergeFiles() {
		CharSequence message = "Enter path of file to merge";
		final EditText input = new EditText(PEEditScreen.this);
		input.setText(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + '/', TextView.BufferType.EDITABLE);
		new AlertDialog.Builder(PEEditScreen.this).setTitle("Enter Filename").setMessage(message ).setView(
				input).setPositiveButton(android.R.string.ok , new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String pFileName = input.getText().toString();
						boolean isProp = (pFileName.substring(pFileName.lastIndexOf('.')+1, pFileName.length()).equals("prop"));
						File propFile = new File (pFileName);
						ProgressDialog mergeWait = ProgressDialog.show(PEEditScreen.this, "", "Merging. Please wait...", true);
						
						if( isProp && propFile.canRead() ){
							try{
								PropFile pFtoMerge = new PropFile(new BufferedReader(new InputStreamReader(new FileInputStream(pFileName))));
								String[] mergeKeys = pFtoMerge.getAllPropNames();
								for ( String key : mergeKeys){
									pE.getPropFile().modProp(key, pFtoMerge.getValue(key));
								}
								mergeWait.dismiss();
								onResume();
								Toast.makeText(getApplicationContext(), "Changes merged, look over them to make sure they're correct and don't forget to save", Toast.LENGTH_LONG).show();
							}catch(Exception e){
								mergeWait.dismiss();
								android.util.Log.d(TAG, "OHSHI--", e);
								Toast.makeText(getApplicationContext(), "Failed to open PropFile for merging", Toast.LENGTH_SHORT).show();
							}

						}else{
							mergeWait.dismiss();
							Toast.makeText(getApplicationContext(), "Failed to open PropFile for merging", Toast.LENGTH_SHORT).show();
						}
					}
				}).setNegativeButton(android.R.string.cancel , new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Do nothing.
					}
				}).show();
		
		
	}
	 public boolean hasRootPermission() {
	        boolean rooted = true;
	                try {
	                        File su = new File("/system/bin/su");
	                        if (su.exists() == false) {
	                                su = new File("/system/xbin/su");
	                                if (su.exists() == false) {
	                                        rooted = false;
	                                }
	                        }
	                } catch (Exception e) {
	                        Log.d(TAG, "Can't obtain root - Here is what I know: "+e.getMessage());
	                        rooted = false;
	                }
	                return rooted;
	}
	    

	public boolean suCommand(String command) {
		int returncode = -1;
		try{
			Log.d(TAG, "Root-Command ==> su -c \""+command+"\"");
			
			Process p = rt.exec("su -c sh");

			//kanged from http://christophe.vandeplas.com/2010/01/09/change-files-readonly-filesystem-your-android-phone
			DataOutputStream os = new DataOutputStream(p.getOutputStream());
			os.writeBytes(command + "\n"); os.flush();
			// and finally close the shell
			os.writeBytes("exit\n"); os.flush();
			//end kangage
			
			returncode = p.waitFor();
			if (returncode == 0) {
				return true;
			}else{
				Log.d(TAG, "root command: " + command + " failed with returncode "+ returncode);
				return false;
			}
		}catch(Exception e){
			Log.d(TAG, "Root-Command error, return code: " + returncode, e);
			return false;
		}
		
	}
	class ModHandler implements OnItemClickListener{
		@Override
		public void onItemClick(AdapterView<?> parent, View view,int position, long id)  {
			String sugFor = (String) parent.getItemAtPosition(position);
			currProp = sugFor.substring(0,sugFor.indexOf("="));
			suggestionScreen(currProp, "Value");
		}
	}
	
	class SuggestionHandler implements DialogInterface.OnClickListener{
		@Override
		public void onClick(DialogInterface view, int selected) {
			String selVal = sugFullList[selected];
			if (currProp == null){
				thingString = "";
			}else{
				thingString = " for " + currProp;
			}
			if( selVal.equals("Manual Entry")){
				showDialog(MANUAL_DIALOG);
			}else{
				selectedEdit = selVal;
				modifyProps();
			}			
		}
	}
} 