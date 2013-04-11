package com.example.powerpoint;

import java.io.File;
import java.io.FileNotFoundException;

import com.samsung.samm.common.SAMMLibConstants;
import com.samsung.samm.common.SOptionPlay;
import com.samsung.samm.common.SOptionSCanvas;
import com.samsung.spensdk.SCanvasView;
import com.samsung.spensdk.applistener.AnimationProcessListener;
import com.samsung.spensdk.applistener.SCanvasInitializeListener;
import com.samsung.spensdk.applistener.SPenHoverListener;
import com.samsung.spensdk.applistener.SPenTouchListener;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This is the main activity where all the drawing and animations occur.
 * 
 * @author David Tang
 *
 */

//This line is required to run the following two lines in the onCreate() method:
//getActionBar().setDisplayShowHomeEnabled(false);
//getActionBar().setDisplayShowTitleEnabled(false);
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class MainActivity extends Activity {
    //Both of these integers are defined in SOptionPlay
    public final int SLOWEST_SPEED = 0;
    public final int FASTEST_SPEED = 3;

    private RelativeLayout mCanvasContainer;
    private SCanvasView mSCanvas;
    private int animationSpeed;
    private SOptionSCanvas options;
    
    InputMethodManager imm;
    
    Bitmap bitmap;
    
    String saveFile;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        imm =  (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        getActionBar().setDisplayShowHomeEnabled(false);
        getActionBar().setDisplayShowTitleEnabled(false);
        setContentView(R.layout.activity_main);
        
        animationSpeed = SOptionPlay.ANIMATION_SPEED_NORMAL; //Default speed
        options = new SOptionSCanvas();
        options.mPlayOption.setSoundEffectOption(false); //Disable artificial sound effects
        mCanvasContainer = (RelativeLayout) findViewById(R.id.canvas_container);
        mSCanvas = new SCanvasView(this);
        mSCanvas.setSCanvasInitializeListener(new SCanvasInitializeListener() {
            @Override
            //You must set the options in this method. Otherwise the SCanvasView
            //will not be initialized to take in the options correctly. Setting
            //the options outside will result in the default behavior because the
            //option was never properly applied.
            public void onInitialized() {
                mSCanvas.setOption(options);
            }
        }); 
        mSCanvas.setAnimationProcessListener(new AnimationProcessListener() {
            @Override
            //This method runs immediately after the animation finishes playing
            public void onPlayComplete() {
                mSCanvas.setAnimationMode(false); //Enable drawing again
            }

            @Override
            //This method runs after each stroke is completed. As of this time
            //I'm not sure how adding other drawable objects to the SCanvas effects
            //the progress.
            public void onChangeProgress(int nProgress) {
                //We can later use this section to perhaps keep track of a playback animation
                //if a user makes a mistake and needs to continue drawing beginning at a past
                //state. The parameter is supposed to be a number between 0 and 100 to signify
                //the percentage of the animation completed.
            }
        });
        
        mSCanvas.setTitle("SPen PowerPoint App");
        mSCanvas.setOnTouchListener(new View.OnTouchListener() {      
            @Override
            public boolean onTouch(final View v, final MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    //We might be able to make this useful later.
                    
                    //System.out.println("OBJECTLIST" + mSCanvas.getSObjectList(false));
                    Toast.makeText(MainActivity.this, "Stroke completed.", Toast.LENGTH_SHORT).show(); //To test stroke completion
                }
                return false;
            }
        });
        mSCanvas.setSPenTouchListener(new SPenTouchListener(){

			@Override
			public void onTouchButtonDown(View arg0, MotionEvent arg1) {
				// TODO Auto-generated method stub
				//Toast.makeText(MainActivity.this, "Menu To Be Added", Toast.LENGTH_SHORT).show();
				
			}

			@Override
			public void onTouchButtonUp(View arg0, MotionEvent arg1) {
				// TODO Auto-generated method stub
			    //Toast.makeText(MainActivity.this, "On Touch Button Up", Toast.LENGTH_SHORT).show();
			}

			@Override
			public boolean onTouchFinger(View arg0, MotionEvent arg1) {
				// TODO Auto-generated method stub
			    //Toast.makeText(MainActivity.this, "On Touch Finger", Toast.LENGTH_SHORT).show();
				return false;
			}

			@Override
			public boolean onTouchPen(View arg0, MotionEvent arg1) {
				// TODO Auto-generated method stub
			    //Toast.makeText(MainActivity.this, "On Touch Pen", Toast.LENGTH_SHORT).show();
				return false;
			}

			@Override
			public boolean onTouchPenEraser(View arg0, MotionEvent arg1) {
				// TODO Auto-generated method stub
			    //Toast.makeText(MainActivity.this, "On Touch Pen Eraser", Toast.LENGTH_SHORT).show();
				return false;
			}
        	
        });
        
        mSCanvas.setSPenHoverListener(new SPenHoverListener() {
            @Override
            public boolean onHover(View view, MotionEvent event) {
                //Toast.makeText(MainActivity.this, "Hovering", Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            public void onHoverButtonDown(View view, MotionEvent event) {
                Toast.makeText(MainActivity.this, "Hovering with Button Down", Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onHoverButtonUp(View view, MotionEvent event) {
                //Toast.makeText(MainActivity.this, "Hovering with Button Up", Toast.LENGTH_SHORT).show();
            }
        });
        
        mCanvasContainer.addView(mSCanvas);
        
        //File folder = getBaseContext().getCacheDir();
        //String folderPath = folder.getAbsolutePath();
        //System.out.println(folderPath);
        //saveFile = folderPath + "/" + "SAMM.data";
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
       
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action1:
                previewAnimation();
                return true;
            case R.id.action2:
                mSCanvas.undo();
                return true;
            case R.id.action3:
                mSCanvas.redo();
                return true;
            case R.id.action4:
                slowDownAnimation();
                return true;
            case R.id.action5:
                speedUpAnimation();
                return true;
            case R.id.action6:
                pauseResume();
                return true;
            case R.id.action7:
            	openGallery();
            	return true;
            case R.id.action8:
            	addText();
            	return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    public void addText()
    {
    	EditText text = new EditText(this);
    	mCanvasContainer.addView(text);
    	
    	text.requestFocus();
    	
    	imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }
    public void openGallery()
    {
    	if(imm.isAcceptingText())
    	{
    		imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    	}
    	
    	Intent intent = new Intent(Intent.ACTION_PICK,
    			MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    	
    	final int IMG_REQ_CODE = 1234;
    	
    	startActivityForResult(intent, IMG_REQ_CODE);
    }
    
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) 
        {
        	switch(requestCode)
        	{
        		case 1234:
        			Toast.makeText(MainActivity.this, "Image Selected", Toast.LENGTH_SHORT).show();
        			Uri pictureUri = data.getData();
    
        			 try {
        			  bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(pictureUri));
//        			  setContentView(new MyView(this));
//        			  mCanvasContainer.addView(new MyView(this));
        			  
        			  ImageView imgView = new ImageView(this);
        			  imgView.setImageBitmap(bitmap);

        			  mCanvasContainer.addView(imgView);
        			  
        			 } catch (FileNotFoundException e) {
        			  // TODO Auto-generated catch block
        			  e.printStackTrace();
        			 }
        			break;
        	}
        }
    }
    
    class MyView extends View
    {

		public MyView(Context context) 
		{
			super(context);
		}
		
		@Override
		protected void onDraw(Canvas canvas) 
		{
			canvas.drawBitmap(bitmap, 50, 50, null);
		}
    	
    }

    //Need to find a way to redo() after previewing the animation. Perhaps keep
    //a history of saveSammData()?.
    public void previewAnimation() {
        //This line ensures that the animation plays only up to the current state.
        //It will also wipe any redo() history.
        mSCanvas.loadSAMMData(mSCanvas.saveSAMMData());
       
        
        //System.out.println(saveFile);
        //mSCanvas.saveSAMMFile(saveFile);
        //mSCanvas.loadSAMMFile(saveFile, true, true);
        
        mSCanvas.setAnimationMode(true);
        mSCanvas.setAnimationSpeed(animationSpeed);
        mSCanvas.doAnimationStart();
    }

    public void slowDownAnimation() {
        if (animationSpeed > SLOWEST_SPEED)
            mSCanvas.setAnimationSpeed(--animationSpeed);
    }

    public void speedUpAnimation() {
        if (animationSpeed < FASTEST_SPEED)
            mSCanvas.setAnimationSpeed(++animationSpeed);
    }
    
    public void pauseResume() {
        int state = mSCanvas.getAnimationState();
        
        if (state == SAMMLibConstants.ANIMATION_STATE_ON_PAUSED) {
            mSCanvas.doAnimationResume();
            ((TextView) findViewById(R.id.action6)).setText("Pause");
        }
        else if (state == SAMMLibConstants.ANIMATION_STATE_ON_RUNNING) {
            mSCanvas.doAnimationPause();
            ((TextView) findViewById(R.id.action6)).setText("Resume");
        }
    }
}
