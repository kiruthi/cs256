package com.portablepresentations;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 *@author David Tang, Kiruthika Sivaraman, Parnit Sainion 
 * DisplayFileAcitivity allows users to select a presentation or presentation slide.
 */
public class DisplayFileActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_file);

    	Intent recieved = getIntent();
    	final String option = recieved.getStringExtra("option");
		
		File dir = getBaseContext().getDir("spen", 0);
    	File[] subFiles = dir.listFiles();
    	
    	List<String> list = new ArrayList<String>();
    	
    	if (subFiles != null)
    	{
    	    for (File file : subFiles)
    	    {
    	        list.add(file.getName());
    	    }
    	}
    	
    	if(list.size() == 0)
    	{
    		Intent intent = new Intent();
			intent.putExtra("dir", -1);
            setResult(Activity.RESULT_OK, intent);
            finish();
    	}
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>
        (DisplayFileActivity.this, R.layout.activity_display_file, 
                R.id.dummyText, list);
    	
    	
    	ListView listView = (ListView)findViewById(R.id.listView1);
    	listView.setAdapter(adapter);
    	
    	listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				if(option.equalsIgnoreCase("load"))
				{
					listSlides(arg2);
				}
				else if(option.equalsIgnoreCase("delete"))
				{
					deletePres(arg2);
				}
				else
				{
					exportFiles(arg2);
				}
				
			}
    		
		});
	}
	
	/**
	 * Method to delete the selected presentation
	 * @param presNo
	 */
	
	public void deletePres(final int presNo)
	{
		
		//set up dialog
        AlertDialog.Builder alert= new AlertDialog.Builder(this);
    	alert.setTitle("Delete Presentation");
    	alert.setMessage("Are you Sure?");
    	
    	alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				//Presentations directory is retrieved and all directories in that folder are obtained
				File dir = getBaseContext().getDir("spen", 0);
				File[] subFiles = dir.listFiles();
				
				if(presNo < subFiles.length)
				{
					dir = new File(dir.getAbsoluteFile() + "/" + subFiles[presNo].getName());
					
					if(dir.exists())
					{
						File[] files = dir.listFiles();
						
						for(File file : files)
						{
							file.delete();
						}
						
						dir.delete();
					}
				}
				
				Intent intent = new Intent();
				intent.putExtra("status", "ok");
		        setResult(Activity.RESULT_OK, intent);
		        finish();
			}
		});
    	
    	alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
				Intent intent = new Intent();
				intent.putExtra("status", "no");
		        setResult(Activity.RESULT_OK, intent);
		        finish();
				
			}
		});
    	alert.show();
		
	}
	
	
	/**
	 *  Users select the presentation they want export out. The path to the presentation directory is passed back to 
	 *  the main activity
	 * @param position position of selected presentation in the presentations directory
	 */
	public void exportFiles(int position)
	{
		//Presentations directory is retrieved and all directories in that folder are obtained
		File dir = getBaseContext().getDir("spen", 0);
    	File[] subFiles = dir.listFiles();
    	
    	List<String> list = new ArrayList<String>();    	
    	if (subFiles != null)
    	{
    	    for (File file : subFiles)
    	    {
    	        list.add(file.getName());
    	    }
    	}
    	
    	//Desired presentation directory is obtained 
    	dir = new File(dir.getAbsolutePath() + "/" + list.get(position));
    	
    	//System.out.println("Dir Path "+dir.getAbsolutePath());
    	//System.out.println("Dir Name "+dir.getName());
    	
    	//All slides in desired presentation are listed
    	File[] files = dir.listFiles();
    
    	//Intent with desired presentations path is set to return to main activity
    	Intent intent = new Intent();
		intent.putExtra("dir", 0);
		intent.putExtra("dirPath", dir.getAbsolutePath());
        setResult(Activity.RESULT_OK, intent);
        
        //Toast message is set to notify users that files are being exported (or not)
    	if (files != null)
    	{
    		Toast t = Toast.makeText(getApplicationContext(), "Exporting Now. Please Wait.", Toast.LENGTH_SHORT);
    		t.show();
    	}	
    	else
    	{
    		Toast t = Toast.makeText(getApplicationContext(), "No Files in Project. Please check project.", Toast.LENGTH_SHORT);
    		t.show();
    	}
    	
        finish();
	}
	
	public void listSlides(final int dirNo)
	{
		Intent intent = new Intent();
		intent.putExtra("dir", dirNo);
        setResult(Activity.RESULT_OK, intent);
        finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_display_file, menu);
		return true;
	}

}
