package com.example.powerpoint;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

public class SplashActivity extends Activity 
{
	protected int waitTime  = 2000;
	
	public void onCreate( Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.app_splash);
		
		ImageView splashImageView = (ImageView) findViewById(R.id.appSpashImageView);
		
		int splashId;
		
		splashId= R.drawable.splash_image;
		
		Bitmap splashBitmap = BitmapFactory.decodeResource(getResources(), splashId);
		
		splashImageView.setImageBitmap(splashBitmap);
		
		final SplashActivity thisSplash = this;
		
		Thread splashThread = new Thread(){
			
			public void run ()
			{
				try{
					synchronized(this){
						wait(waitTime);
					}
				}catch(Exception e)
				{
					
				}
				
				Intent intent= new Intent();
				intent.setClass(thisSplash, MainActivity.class);
				startActivity(intent);
				finish();
			}
		};
		splashThread.start();
	}
}
