package net.widgetron.propeditor;

import java.io.File;

import android.app.Activity; 
import android.app.AlertDialog;
import android.content.*;
import android.os.Bundle; 
//import android.util.Log;
import android.view.*;
import android.view.View.*;
import android.widget.*;

public class PEMainScreen extends Activity implements OnClickListener { 
	/** Called when the activity is first created. */ 
	private Button aButton;
	private final String TAG = "PropEditor:MainScreen";
	
	@Override 
	public void onCreate(Bundle savedState) { 
		super.onCreate(savedState); 
		this.setContentView(R.layout.main); 
		this.aButton = (Button)findViewById(R.id.open);
		aButton.setOnClickListener(this);
	} 

	void propFileFail(){
		Toast.makeText(getApplicationContext(), "Failed to open PropFile", Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public void onClick(View v) {
		CharSequence message = "Enter path of .prop file to edit";
		final EditText input = new EditText(PEMainScreen.this);
		input.setText(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + '/', TextView.BufferType.EDITABLE);
		
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
								// Log.d(TAG, "filename passed in" + pFileName);
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