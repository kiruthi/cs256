package com.example.powerpoint;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.samsung.samm.common.SAMMLibConstants;
import com.samsung.samm.common.SObjectImage;
import com.samsung.samm.common.SOptionPlay;
import com.samsung.samm.common.SOptionSCanvas;
import com.samsung.spensdk.SCanvasConstants;
import com.samsung.spensdk.SCanvasView;
import com.samsung.spensdk.applistener.AnimationProcessListener;
import com.samsung.spensdk.applistener.ColorPickerColorChangeListener;
import com.samsung.spensdk.applistener.SCanvasInitializeListener;
import com.samsung.spensdk.applistener.SPenHoverListener;
import com.samsung.spensdk.applistener.SPenTouchListener;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.RectF;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * This is the main activity where all the drawing and animations occur.
 *
 * @author David Tang
 *
 */

@SuppressLint("NewApi")
public class MainActivity extends Activity {
    //Both of these integers are defined in SOptionPlay
    public final int SLOWEST_SPEED = 0;
    public final int FASTEST_SPEED = 3;
    
    private RelativeLayout mCanvasContainer;
    private SCanvasView mSCanvas;
    private int animationSpeed;
    private SOptionSCanvas options;
   // private String saveFile;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        
        getActionBar().setDisplayShowHomeEnabled(false);
        getActionBar().setDisplayShowTitleEnabled(false);
        setContentView(R.layout.activity_main);
        
        View content = findViewById(R.id.canvas_container);
        content.setDrawingCacheEnabled(true);
        
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
                mSCanvas.setTitle("SPen PowerPoint App");
                mSCanvas.setOption(options);
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
                mSCanvas.setSPenTouchListener(new SPenTouchListener() {
                    @Override
                    public void onTouchButtonDown(View arg0, MotionEvent arg1) {
                        
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
                        optionsDialog();
                    }
                    
                    @Override
                    public void onHoverButtonUp(View view, MotionEvent event) {

                    }
                });
            }
        });
        
        
        mCanvasContainer.addView(mSCanvas);
        
      //change background to white
        mSCanvas.setBackgroundColor(Color.WHITE);
        mSCanvas.setBGColor(Color.WHITE);
        
        
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
    
    public void screenCapture(String fileName)
    {
    	View content = findViewById(R.id.canvas_container);
      	Bitmap bitmap = content.getDrawingCache(true);
    	File file = new File(Environment.getExternalStorageDirectory().getPath()+"/"+fileName+".png");
      	try{
    		file.createNewFile();
    		FileOutputStream outStream = new FileOutputStream(file);
    		bitmap.compress(CompressFormat.PNG, 100, outStream);
    		outStream.close();
    	}catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
           /*
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
                mSCanvas.setCanvasMode(SCanvasConstants.SCANVAS_MODE_INPUT_TEXT);
            	return true;
            case R.id.action9:
                mSCanvas.setCanvasMode(SCanvasConstants.SCANVAS_MODE_INPUT_PEN);
                return true;
            case R.id.action10:
                mSCanvas.setCanvasMode(SCanvasConstants.SCANVAS_MODE_INPUT_ERASER);
                return true;
                */
            case R.id.options:
            	optionsDialog();
            	return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    public void optionsDialog()
    {
    	final Dialog myDialog = new Dialog(MainActivity.this);
        myDialog.setCancelable(true);
        myDialog.setTitle("Options");
        myDialog.setContentView(R.layout.dialog_layout);
        
        Button textButton = (Button) myDialog.findViewById (R.id.textButton);
        textButton.setText("Text Mode");
        textButton.setOnClickListener( new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mSCanvas.setCanvasMode(SCanvasConstants.SCANVAS_MODE_INPUT_TEXT);
                myDialog.dismiss();
            }
        });
        
        Button drawButton = (Button) myDialog.findViewById (R.id.drawButton);
        drawButton.setText("Pen Mode");
        drawButton.setOnClickListener( new OnClickListener() {
            @Override
            public void onClick(View arg0) {
            	
            	
                mSCanvas.setCanvasMode(SCanvasConstants.SCANVAS_MODE_INPUT_PEN);
                myDialog.dismiss();
            }
        });
        
        Button eraseButton = (Button) myDialog.findViewById (R.id.eraseButton);
        eraseButton.setText("Eraser Mode");
        eraseButton.setOnClickListener( new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mSCanvas.setCanvasMode(SCanvasConstants.SCANVAS_MODE_INPUT_ERASER);
                myDialog.dismiss();
            }
        });
        
        Button cancelButton = (Button) myDialog.findViewById (R.id.cancelButton);
        cancelButton.setText("Cancel");
        cancelButton.setOnClickListener( new OnClickListener() {

            @Override
            public void onClick(View arg0) {
            	myDialog.dismiss();              
            }
        });
        
        Button saveButton = (Button) myDialog.findViewById (R.id.saveButton);
        saveButton.setText("Save");
        saveButton.setOnClickListener( new OnClickListener() {

            @Override
            public void onClick(View arg0) {
            	 saveDialog();
            	myDialog.dismiss();              
            }
        });
        
        Button clearButton = (Button) myDialog.findViewById (R.id.clearButton);
        clearButton.setText("Clear");
        clearButton.setOnClickListener( new OnClickListener() {

            @Override
            public void onClick(View arg0) {
            	cancelDialog();
            	myDialog.dismiss();              
            }
        });
        
        Button pictureButton = (Button) myDialog.findViewById (R.id.pictureButton);
        pictureButton.setText("Insert Picture");
        pictureButton.setOnClickListener( new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                openGallery();
                myDialog.dismiss();
            }
        });
        
        myDialog.show();     
    }
    
    public void openGallery() {
    	Intent intent = new Intent(Intent.ACTION_PICK,
                                   MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    	
    	final int IMG_REQ_CODE = 1234;
    	
    	startActivityForResult(intent, IMG_REQ_CODE);
    }
    
    public void saveDialog()
    {
        AlertDialog.Builder alert= new AlertDialog.Builder(this);
    	alert.setTitle("Save As");
    	alert.setMessage("Enter Slide Name");
    	final EditText input = new EditText(this);
    	alert.setView(input);
    	
    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				screenCapture(input.getText().toString());
			}
		});
    	
    	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
				
			}
		});
    	alert.show();
     }
    
    public void cancelDialog()
    {
        AlertDialog.Builder alert= new AlertDialog.Builder(this);
    	alert.setTitle("Cancel");
    	alert.setMessage("Are you Sure?");
    	
    	alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
            	mSCanvas.clearScreen();
			}
		});
    	
    	alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
				
			}
		});
    	alert.show();
     }
    
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
        	switch(requestCode) {
        	    case 1234:
        	        Uri pictureUri = data.getData();
                    
        			try {
        			    Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(pictureUri));
        			    RectF rectF = new RectF(0, 0, 200, 200); //Position and size of image
        			    SObjectImage sImageObject = new SObjectImage();
        			    sImageObject.setRect(rectF);
        			    sImageObject.setImageBitmap(bitmap);
        			    if(mSCanvas.insertSAMMImage(sImageObject, true) ) {}
        			} catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
        			}
        			break;
        	}
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
    	/*
    	int state = mSCanvas.getAnimationState();
        
        if (state == SAMMLibConstants.ANIMATION_STATE_ON_PAUSED) {
            mSCanvas.doAnimationResume();
            ((TextView) findViewById(R.id.action6)).setText("Pause");
        }
        else if (state == SAMMLibConstants.ANIMATION_STATE_ON_RUNNING) {
            mSCanvas.doAnimationPause();
            ((TextView) findViewById(R.id.action6)).setText("Resume");
        }
        */
    }
}
