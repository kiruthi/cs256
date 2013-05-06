
package com.portablepresentations;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

/**
 * 
 * @author David Tang, Kiruthika Sivaraman, Parnit Sainion 
 * Splash Activity displays a splash screen when app opens.
 */
public class SplashActivity extends Activity 
{
	//time set for splash image to stay open
	protected int waitTime  = 2000;
	
	public void onCreate( Bundle savedInstanceState)
	{
		int splashId;
		
		super.onCreate(savedInstanceState);	
		
		//set up splash layout and splash image view
		setContentView(R.layout.app_splash);		
		ImageView splashImageView = (ImageView) findViewById(R.id.appSpashImageView);
			
		//sets up id for splash image
		splashId= R.drawable.splash_image;
		
		//sets splash image to image view
		Bitmap splashBitmap = BitmapFactory.decodeResource(getResources(), splashId);		
		splashImageView.setImageBitmap(splashBitmap);
		
		//stores this id for thread
		final SplashActivity thisSplash = this;
		
		Thread splashThread = new Thread(){			
			public void run ()
			{
				try{
					//synchronizes this thread and waits for wait duration
					synchronized(this){
						wait(waitTime);
					}
				}catch(Exception e)
				{
					
				}
				
				//Intent is create to start MainActivity
				Intent intent= new Intent();
				intent.setClass(thisSplash, MainActivity.class);
				startActivity(intent);
				finish();
			}
		};
		//Splash thread begins
		splashThread.start();
	}
}
