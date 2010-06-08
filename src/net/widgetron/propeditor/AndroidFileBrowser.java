package net.widgetron.propeditor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class AndroidFileBrowser extends ListActivity {
	private final String TAG = "PropEditor:FileBrowser";
	private List<String> directoryEntries = new ArrayList<String>();
	private File currentDirectory = new File("/");

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		// setContentView() gets called within the next line,
		// so we do not need it here.
		browseToSystem();
	}
	
	/**
	 * This function browses to the 
	 * root-directory of the file-system.
	 */
	private void browseToSystem() {
		browseTo(new File("/system"));
    }
	
	private void browseToSdcard() {
		browseTo(android.os.Environment.getExternalStorageDirectory());
    }
	/**
	 * This function browses up one level 
	 * according to the field: currentDirectory
	 */
	private void upOneLevel(){
		if(this.currentDirectory.getParent() != null)
			this.browseTo(this.currentDirectory.getParentFile());
	}
	
	private void browseTo(final File aDirectory){
		if (aDirectory.isDirectory()){
			this.currentDirectory = aDirectory;
			fill(aDirectory.listFiles());
		}else{
			try {
				File file = aDirectory;
				boolean isProp = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf('.')+1, file.getAbsolutePath().length()).equals("prop");
				File propFile = file;
				
				if( isProp && propFile.canRead() ){
					try{
						Intent myIntent = new Intent(AndroidFileBrowser.this, PEEditScreen.class);
						Bundle bundle = new Bundle();
						//Add the parameters to bundle as
						bundle.putString("fn",file.getAbsolutePath());
						//Add this bundle to the itent
						myIntent.putExtras(bundle);
						AndroidFileBrowser.this.startActivity(myIntent);
					}catch(Exception e){
						android.util.Log.d(TAG, "OHSHI--", e);
						propFileFail();
					}

				}else{
					propFileFail();
				}				
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	private void fill(File[] files) {
		this.directoryEntries.clear();
		
		// Add the "." and the ".." == 'Up one level'
		try {
			Thread.sleep(10);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		this.directoryEntries.add(".");
		
		if(this.currentDirectory.getParent() != null)
			this.directoryEntries.add("..");
		
		int currentPathStringLenght = this.currentDirectory.getAbsolutePath().length();
			
		for (File file : files){
			if(file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf('.')+1, file.getAbsolutePath().length()).equals("prop")){
				this.directoryEntries.add(file.getAbsolutePath().substring(currentPathStringLenght));
			}else if (file.isDirectory()){
				this.directoryEntries.add(file.getAbsolutePath().substring(currentPathStringLenght));
			}
		}
				
		
		ArrayAdapter<String> directoryList = new ArrayAdapter<String>(this,
				R.layout.file_row, this.directoryEntries);
		
		this.setListAdapter(directoryList);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		int selectionRowID = position;
		String selectedFileString = this.directoryEntries.get(selectionRowID);
		if (selectedFileString.equals(".")) {
			// Refresh
			this.browseTo(this.currentDirectory);
		} else if(selectedFileString.equals("..")){
			this.upOneLevel();
		} else {
			File clickedFile = null;
			clickedFile = new File(this.currentDirectory.getAbsolutePath() 	+ this.directoryEntries.get(selectionRowID));
			if(clickedFile != null)
				this.browseTo(clickedFile);
		}
	}
	
	void propFileFail(){
		Toast.makeText(getApplicationContext(), "Failed to open PropFile", Toast.LENGTH_SHORT).show();
	}
}