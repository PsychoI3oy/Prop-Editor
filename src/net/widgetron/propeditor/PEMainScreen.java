package net.widgetron.propeditor;

import java.io.DataOutputStream;
import java.io.File;

import android.app.Activity; 
import android.app.AlertDialog;
import android.content.*;
import android.os.Bundle; 
import android.util.Log;
import android.view.*;
import android.view.View.*;
import android.widget.*;

public class PEMainScreen extends Activity{ 
	/** Called when the activity is first created. */ 
	private Button openButton;
	private final String TAG = "PropEditor:MainScreen";
	private Runtime rt;
	private Button copyButton;
	private Button restoreButton;
	private String sdDir = "/propeditor/";
	
	
	@Override 
	public void onCreate(Bundle savedState) { 
		super.onCreate(savedState); 
		this.setContentView(R.layout.main); 
		this.openButton = (Button)findViewById(R.id.open);
		this.openButton.setOnClickListener(new openListener());
		this.rt = Runtime.getRuntime();
		this.sdDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + sdDir;
		this.copyButton = (Button)findViewById(R.id.CopyToCard);
		this.restoreButton = (Button)findViewById(R.id.CopyFromCard);
		
		if(hasRootPermission()){
			suCommand("mkdir " + sdDir);
			this.copyButton.setText("Copy /system/*.prop to " + sdDir);
			this.restoreButton.setText("Copy " + sdDir +"*.prop  to /system/");
			this.copyButton.setOnClickListener(new copyListener());
			this.restoreButton.setOnClickListener(new restoreListener());
		}else{
			this.copyButton.setText("no su access");
			this.restoreButton.setText("no su access");
			
		}
		
		
		
	} 

	void propFileFail(){
		Toast.makeText(getApplicationContext(), "Failed to open PropFile", Toast.LENGTH_SHORT).show();
	}
	
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
	                        Log.d(TAG, "Can't obtain root - Here is what I know: "+e.getMessage());
	                        rooted = false;
	                }
	                return rooted;
	}
	    

	public boolean suCommand(String command) {
		int returncode = 0;
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

	// end kangage


	
	class openListener implements OnClickListener{
	@Override
	public void onClick(View v) {
		CharSequence message = "Enter path of .prop file to edit";
		final EditText input = new EditText(PEMainScreen.this);
		input.setText(sdDir , TextView.BufferType.EDITABLE);
		
		new AlertDialog.Builder(PEMainScreen.this).setTitle("Enter Filename").setMessage(message ).setView(
				input).setPositiveButton(android.R.string.ok , new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						
						String pFileName = input.getText().toString();
						
						boolean isProp = (pFileName.substring(pFileName.lastIndexOf('.')+1, pFileName.length()).equals("prop"));
						File propFile = new File (pFileName);
						
						if( isProp && propFile.canRead() ){
							try{
								Intent myIntent = new Intent(PEMainScreen.this, PEEditScreen.class);
								Bundle bundle = new Bundle();
								//Add the parameters to bundle as
								bundle.putString("fn",pFileName);
								//Add this bundle to the itent
								myIntent.putExtras(bundle);
								PEMainScreen.this.startActivity(myIntent);
							}catch(Exception e){
								android.util.Log.d(TAG, "OHSHI--", e);
								propFileFail();
							}

						}else{
							propFileFail();
						}
					}
				}).setNegativeButton(android.R.string.cancel , new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Do nothing.
					}
				}).show();
		}
	}
	class copyListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			File pEdir = new File (sdDir);
			
			if (!  pEdir.isDirectory() ){
				if( pEdir.exists() ){
					pEdir.delete();
				}
				
				if (! pEdir.mkdir() ){
					Toast.makeText(getApplicationContext(), "Cannot create or write to " + sdDir, Toast.LENGTH_SHORT).show();
				}
			}
			
			
			if (pEdir.canWrite() && pEdir.isDirectory() ) {
				if(suCommand("cp /system/*.prop "+ sdDir)){
					Toast.makeText(getApplicationContext(), "Coped propfiles to " + sdDir, Toast.LENGTH_SHORT).show();
				}else{
					Toast.makeText(getApplicationContext(), "Failed to copy PropFiles to "+ sdDir, Toast.LENGTH_SHORT).show();
				}
			}else{
				Toast.makeText(getApplicationContext(), "Failed to copy PropFiles to "+sdDir, Toast.LENGTH_SHORT).show();
			}
		}
	}
	class restoreListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			if (! suCommand("busybox mount -o rw,remount /system")){
				Toast.makeText(getApplicationContext(), "failed to remount system rw", Toast.LENGTH_SHORT).show();
			}else{
				if (! suCommand("cp " + sdDir+"*.prop /system/")){
					Toast.makeText(getApplicationContext(), "failed to copy "+sdDir, Toast.LENGTH_SHORT).show();
				}else{
					suCommand("busybox mount -o ro,remount /system");
					Toast.makeText(getApplicationContext(), "Copied  " + sdDir + "*.prop to /system; reboot for changes to take effect", Toast.LENGTH_LONG).show();
				}
			}
		}
	}

	
	
	
	
	
}