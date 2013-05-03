package com.example.powerpoint;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class DisplayFileActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_file);
		
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
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>
        (DisplayFileActivity.this, R.layout.activity_display_file, 
                R.id.dummyText, list);
    	
    	
    	ListView listView = (ListView)findViewById(R.id.listView1);
    	listView.setAdapter(adapter);
    	
    	listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				Intent intent = new Intent();
				intent.putExtra("file", arg2);
                setResult(Activity.RESULT_OK, intent);
                finish();
			}
    		
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_display_file, menu);
		return true;
	}

}
