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
	
	
	
	static PropEditor pE;
	
	private String filename;
	private ListView propList;
	
	private String currProp;
	private String jlSugtitle;
	private String thingString;
	private String editMode;
	protected String selectedEdit;
	private String[] sugFullList;

	
	
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
		this.filename  = bundle.getString("fn");
		//open suggestionfile
		Log.d(TAG, "filename inside " + this.filename);
		Log.d(TAG, "before sugfile");
  		InputStream is = this.getResources().openRawResource(R.raw.suggestionfile);
		BufferedReader sFile= new BufferedReader(new InputStreamReader(is));
		Log.d(TAG, "after sugfile");  
	/*	is = this.getResources().openRawResource(R.raw.propfile);
		BufferedReader pFile= new BufferedReader(new InputStreamReader(is)); */
	
		
		Log.d(TAG, "before propfile");
		BufferedReader pFile;
		try {
			Log.d(TAG, "inside propfile try");
			FileInputStream fis = new FileInputStream(this.filename);
			Log.d(TAG, "after propfile fis");
			InputStreamReader isr = new InputStreamReader(fis);
			Log.d(TAG, "after propfile isr");
			pFile = new BufferedReader(isr);
			Log.d(TAG, "after propfile buffered reader");
			Log.d(TAG, "after pfile br/isr/ofi");
			PEEditScreen.pE = new PropEditor(pFile, sFile); 
			Log.d(TAG, "after new prop editor");
		} catch (FileNotFoundException e) {
			Toast.makeText(getApplicationContext(), "Failed to open PropFile", Toast.LENGTH_SHORT).show();
			Log.e(TAG, "Failed to open PropFile", e);
			this.finish();	
		}		
	} 
	
  	public void onResume(){
  		super.onResume();
  		Log.d(TAG, "resumed editor");
  		
  		String propListArr[] = pE.getPropFile().getAllProps();
		propList.setAdapter(new ArrayAdapter<String>(this, R.layout.mylist , propListArr));
		propList.setTextFilterEnabled(true);
		propList.setOnItemClickListener(new ModHandler());

		Log.d(TAG, "ended editor onResume");
  	}
  	
	/*
	//// kanged from android-wifi-tether
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
                        Log.d(MSG_TAG, "Can't obtain root - Here is what I know: "+e.getMessage());
                        rooted = false;
                }
                return rooted;
    }
    
    public boolean runRootCommand(String command) {
                Log.d(MSG_TAG, "Root-Command ==> su -c \""+command+"\"");
                int returncode = NativeTask.runCommand("su -c \""+command+"\"");
        if (returncode == 0) {
                        return true;
                }
        Log.d(MSG_TAG, "Root-Command error, return code: " + returncode);
                return false;
    }

    // end kangage
    
*/
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
			try {
				if (pE.getPropFile().saveFile(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename)))) ){
					saveFileSuccess();
				}else{
					saveFileFail();
				}
			} catch (FileNotFoundException e) {
				android.util.Log.e(TAG, "failed to save file", e);
				saveFileFail();
			}
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
		    	input.setText("", TextView.BufferType.EDITABLE);
		    	dialog = new 	AlertDialog.Builder(PEEditScreen.this).setTitle("Custom entry").setView(
						input).setPositiveButton(android.R.string.ok , new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								selectedEdit = input.getText().toString();
								input.setText("", TextView.BufferType.EDITABLE);
								modifyProps();
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
  		//android.util.Log.d(TAG, "begin suggestionScreen");
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
		android.util.Log.d(TAG, "end suggestionScreen");
	}
  	
  	public void saveAs() {
		CharSequence message = "Enter filename to save as";
		final EditText input = new EditText(PEEditScreen.this);
		input.setText(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + '/', TextView.BufferType.EDITABLE);
		new AlertDialog.Builder(PEEditScreen.this).setTitle("Enter Filename").setMessage(message ).setView(
				input).setPositiveButton(android.R.string.ok , new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String pFileName = input.getText().toString();

						android.util.Log.d(TAG, "saveas with " + pFileName);
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
		android.util.Log.d(TAG, "modifying props with "  + selectedEdit);
		if (!(selectedEdit == null)){ 
			if(editMode.equals("Prop")){
				currProp = selectedEdit;
				suggestionScreen(currProp, "Value");
			}else{
				if (pE.getPropFile().modProp(currProp, selectedEdit)){
					android.util.Log.d(TAG, "write succeeded with " + currProp + "="+ selectedEdit);
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
		android.util.Log.d(TAG, "after manualEntry with " + editMode + selectedEdit);
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
								Toast.makeText(getApplicationContext(), "Changes merged, look over them to make sure they're correct and don't forget to save", Toast.LENGTH_SHORT).show();
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
			android.util.Log.d(TAG, "begin suggestionHandler");

	
			String selVal = sugFullList[selected];
			if (currProp == null){
				thingString = "";
			}else{
				thingString = " for " + currProp;
			}
			if( selVal.equals("Manual Entry")){
				android.util.Log.d(TAG, "calling manualEntry with " + editMode + thingString);
	//			CharSequence message = "Manual entry for " + editMode + thingString;
				showDialog(MANUAL_DIALOG);
			}else{
				selectedEdit = selVal;
			}
			android.util.Log.d(TAG, "middle of suggestionScreen; selectedEdit =" + selectedEdit);
				modifyProps();
		}
	}
} 