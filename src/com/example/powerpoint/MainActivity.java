
package com.example.powerpoint;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.samsung.samm.common.SObjectImage;
import com.samsung.samm.common.SOptionPlay;
import com.samsung.samm.common.SOptionSCanvas;
import com.samsung.spensdk.SCanvasConstants;
import com.samsung.spensdk.SCanvasView;
import com.samsung.spensdk.applistener.AnimationProcessListener;
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
import android.database.Cursor;
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
import android.widget.Toast;

/**
 *@author David Tang, Kiruthika Sivaraman, Parnit Sainion
 * MainActivity is where the presentations are created.
 */
@SuppressLint("NewApi")
public class MainActivity extends Activity {
    //Both of these integers are defined in SOptionPlay
    public final int SLOWEST_SPEED = 0;
    public final int FASTEST_SPEED = 3;
    
    //Set up the number of slides for a presentation
    private int slideNo = 0;
    private int maxSlideNo = 0;
    
    //Presentation isSvaed and name variables
    private boolean isSaved = false;
    private String savedName;
    
    //SCanvas variables
    private RelativeLayout mCanvasContainer;
    private SCanvasView mSCanvas;
    private int animationSpeed;
    private SOptionSCanvas options;

    private HashMap<String,Integer> settingResourceMapInt = new HashMap<String, Integer>();
   // private String saveFile;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
       
        getActionBar().setDisplayShowHomeEnabled(false);
        getActionBar().setDisplayShowTitleEnabled(false);
        
        //set up layout
        setContentView(R.layout.activity_main);
        
        //Set up s canvas and enable cache
        View content = findViewById(R.id.canvas_container);
        content.setDrawingCacheEnabled(true);
        
        animationSpeed = SOptionPlay.ANIMATION_SPEED_NORMAL; //Default speed
        options = new SOptionSCanvas();
        options.mPlayOption.setSoundEffectOption(false); //Disable artificial sound effects
        mCanvasContainer = (RelativeLayout) findViewById(R.id.canvas_container);
        mSCanvas = new SCanvasView(this);
        
        //Initialize resources to enable color picker
        settingResourceMapInt = new HashMap<String, Integer>();
        settingResourceMapInt.put(SCanvasConstants.LAYOUT_PEN_SPINNER, R.layout.activity_main);
        mSCanvas.createSettingView(mCanvasContainer, settingResourceMapInt);
        
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
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        
        
        return true;
    }
    
    /**
     * To be Added
     */
    public void email()
    {
    	
    }
    
    /**
     * Saves a new presentation if not saved, else adds the current slide ot the presentation
     * @param fileName name of presentation
     */
    public void screenCapture(String fileName)
    {
//    	View content = findViewById(R.id.canvas_container);
//      	Bitmap bitmap = content.getDrawingCache(true);
//    	File file = new File(Environment.getExternalStorageDirectory().getPath()+"/"+fileName+".png");
    	/*file.createNewFile();
		FileOutputStream outStream = new FileOutputStream(file);
		bitmap.compress(CompressFormat.PNG, 100, outStream);
		outStream.close();*/
      	try{
      		File presFile = getBaseContext().getDir("spen", 0);
      		File folder = new File(presFile.getAbsolutePath() + "/" + fileName);
      		
      		if(!folder.exists())
      		{
      			folder.mkdir();
      		}
      		
      		String savefName = presFile.getAbsolutePath() + "/" + fileName + "/" + slideNo;
      		
      		if(maxSlideNo == 0)
      		{
      			slideNo++;
      		}
      		else
      		{
      			slideNo = maxSlideNo;
      			maxSlideNo = 0;
      		}
      		
      		if(mSCanvas.saveSAMMFile(savefName))
      		{
      			isSaved = true;
      			Toast.makeText(this, "File Saved!", Toast.LENGTH_SHORT).show();
      		}
      		else
      		{
      			Toast.makeText(this, "Error saving file!", Toast.LENGTH_SHORT).show();
      		}
    	}catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	
    }
    
    /**
     * Sets up our menu on top of screen and pop up menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
           
        	/**case R.id.action1:
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
                return true;*/
            case R.id.action1:
            	openGallery();
            	return true;
            case R.id.action2:
                mSCanvas.setCanvasMode(SCanvasConstants.SCANVAS_MODE_INPUT_TEXT);
            	return true;
            case R.id.action3:
                mSCanvas.setCanvasMode(SCanvasConstants.SCANVAS_MODE_INPUT_PEN);
                return true;
            case R.id.action4:
                mSCanvas.setCanvasMode(SCanvasConstants.SCANVAS_MODE_INPUT_ERASER);
                return true;
            case R.id.action5:
                loadFile();
                return true;
            case R.id.action6:
                saveDialog();
                return true; 
            case R.id.action7:
                clearDialog();
                return true; 
            case R.id.action8:
            	createNewSlide();
            	return true;
            case R.id.action9:
            	createNewPresentation();
            	return true;          
            case R.id.options:
            	optionsDialog();
            	return true;
            case R.id.penSettings:
                mSCanvas.toggleShowSettingView(SCanvasConstants.SCANVAS_SETTINGVIEW_PEN);
            	return true;
            case R.id.selectorMode:
                mSCanvas.setMultiSelectionMode(true);
                return true;
            case R.id.group:
                mSCanvas.groupSAMMObjectList(mSCanvas.getSelectedSObjectList(), true);
                return true;
            case R.id.ungroup:
                mSCanvas.ungroupSelectedObjects();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    /**
     * Creates a new presentation
     */
    public void createNewPresentation()
    {
    	//alert box to confirm user wants new presentation
    	AlertDialog.Builder alert= new AlertDialog.Builder(this);
    	alert.setTitle("Create new Presentation?");
    	alert.setMessage("Are you Sure?");
    	
    	alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				if(isSaved)
		    	{
		    		createNewSlide();
		    	}
		    	savedName = "";
		    	isSaved = false;
		    	slideNo = 0;
		    	maxSlideNo = 0;
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
    
    /**
     * Adds a new slide to the presentation. If presentation is not saved, saved dialog will pop up.
     */
    public void createNewSlide()
    {
    	if(!isSaved)
    	{
    		saveDialog();
    	}
    	else
    	{
    		screenCapture(savedName);
    	}
    	mSCanvas.clearScreen();
    }
    
    /**
     * Options dialog will pop up with all the possible user options
     */
    public void optionsDialog()
    {
    	//set up dialog box
    	final Dialog myDialog = new Dialog(MainActivity.this);
        myDialog.setCancelable(true);
        myDialog.setTitle("Options");
        myDialog.setContentView(R.layout.dialog_layout);
        
        //Button for text input
        Button textButton = (Button) myDialog.findViewById (R.id.textButton);
        textButton.setText("Text Mode");
        textButton.setOnClickListener( new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mSCanvas.setCanvasMode(SCanvasConstants.SCANVAS_MODE_INPUT_TEXT);
                myDialog.dismiss();
            }
        });
        
        //Button for pen mode
        Button drawButton = (Button) myDialog.findViewById (R.id.drawButton);
        drawButton.setText("Pen Mode");
        drawButton.setOnClickListener( new OnClickListener() {
            @Override
            public void onClick(View arg0) {
            	
            	
                mSCanvas.setCanvasMode(SCanvasConstants.SCANVAS_MODE_INPUT_PEN);
                myDialog.dismiss();
            }
        });
        
        //button for eraser mode
        Button eraseButton = (Button) myDialog.findViewById (R.id.eraseButton);
        eraseButton.setText("Eraser Mode");
        eraseButton.setOnClickListener( new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mSCanvas.setCanvasMode(SCanvasConstants.SCANVAS_MODE_INPUT_ERASER);
                myDialog.dismiss();
            }
        });
        
        //button for cancel
        Button cancelButton = (Button) myDialog.findViewById (R.id.cancelButton);
        cancelButton.setText("Cancel");
        cancelButton.setOnClickListener( new OnClickListener() {

            @Override
            public void onClick(View arg0) {
            	myDialog.dismiss();              
            }
        });
        
        //button to export presentation
        Button exportButton = (Button) myDialog.findViewById (R.id.exportButton);
        exportButton.setText("Export");
        exportButton.setOnClickListener( new OnClickListener() {

            @Override
            public void onClick(View arg0) {
            	exportProject();
            	myDialog.dismiss();              
            }
        });
        
        //Button to save presentation/slide
        Button saveButton = (Button) myDialog.findViewById (R.id.saveButton);
        saveButton.setText("Save");
        saveButton.setOnClickListener( new OnClickListener() {

            @Override
            public void onClick(View arg0) {
            	 saveDialog();
            	myDialog.dismiss();              
            }
        });
        
        //button to undo
        Button undoButton = (Button) myDialog.findViewById (R.id.undoButton);
        undoButton.setText("Undo");
        undoButton.setOnClickListener( new OnClickListener() {

            @Override
            public void onClick(View arg0) {
            	mSCanvas.undo();
            	myDialog.dismiss();              
            }
        });
        
        //button to clear screen
        Button clearButton = (Button) myDialog.findViewById (R.id.clearButton);
        clearButton.setText("Clear");
        clearButton.setOnClickListener( new OnClickListener() {

            @Override
            public void onClick(View arg0) {
            	clearDialog();
            	myDialog.dismiss();              
            }
        });
        
        //button to insert image
        Button pictureButton = (Button) myDialog.findViewById (R.id.pictureButton);
        pictureButton.setText("Insert Picture");
        pictureButton.setOnClickListener( new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                openGallery();
                myDialog.dismiss();
            }
        });
        
        //button to load presentation
        Button loadButton = (Button) myDialog.findViewById (R.id.loadButton);
        loadButton.setText("Load");
        loadButton.setOnClickListener( new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                loadFile();
                myDialog.dismiss();
            }
        });
        
        //displays dialog box
        myDialog.show();     
    }
    
    /**
     * Starts DisplayFileActivity to select which presentation to export out. 
     */
    public void exportProject()
    {
    	Intent intent = new Intent(MainActivity.this, DisplayFileActivity.class);
    	intent.putExtra("option", "export");
    	startActivityForResult(intent, 2);
    }
    
    /**
     * Starts DisplayFileActivity to slect with presentation and which slide ot load
     */
    public void loadFile()
    {
    	Intent intent = new Intent(MainActivity.this, DisplayFileActivity.class);
    	intent.putExtra("option", "load");
        startActivityForResult(intent, 1);
    }
    
    /**
     * Opens Gallery to decide which image to import into the presentation
     */
    public void openGallery() {
    	Intent intent = new Intent(Intent.ACTION_PICK,
                                   MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    	
    	final int IMG_REQ_CODE = 1234;
    	
    	startActivityForResult(intent, IMG_REQ_CODE);
    }
    
    /**
     * ALert a dialog opens to confirm save and enter name of presentation.
     */
    public void saveDialog()
    {
	    	//If presentation is not currently saves
	    	if(!isSaved)
	    	{
	    		//set up dialog
	        AlertDialog.Builder alert= new AlertDialog.Builder(this);
	    	alert.setTitle("Save As");
	    	alert.setMessage("Enter Name");
	    	final EditText input = new EditText(this);
	    	alert.setView(input);
	    	
	    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					//save presentation with inputed name
					savedName = input.getText().toString();				
					screenCapture(input.getText().toString());
				}
			});
	    	
	    	//option is canceled
	    	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					
					
				}
			});
	    	//show alert
	    	alert.show();
    	}
    	else
    	{
    		//else ave current slide to presentation
    		screenCapture(savedName);
    	}
     }
    
    /**
     * Pop up alert to confirm that user wants to clear screen
     */
    public void clearDialog()
    {
    	//set up dialog
        AlertDialog.Builder alert= new AlertDialog.Builder(this);
    	alert.setTitle("Clear");
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
    
    /**
     * Saved slide as a PNG file in sdcard folder on device.
     * @param fileName Name of slide to be exported
     */
    public void exportFile(String fileName)
    {
    	//gets canvas container and sets cache
		View content = findViewById(R.id.canvas_container);
     	Bitmap bitmap = content.getDrawingCache(true);
     	
     	//creates a new file 
    	File file = new File(Environment.getExternalStorageDirectory().getPath()+"/"+fileName);
    	try {
    		//outputs files as bitmap in PNGformat
			file.createNewFile();
			FileOutputStream outStream = new FileOutputStream(file);
			bitmap.compress(CompressFormat.PNG, 100, outStream);
			outStream.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    @Override
    /**
     * Does a certain action after receiving a  code from another activity
     */
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
        	switch(requestCode) {
        		
        		case 1: //Loading Files/project
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
        	    	
        	    	int dirNo = data.getIntExtra("dir", 0);
        	    	
        	    	if(dirNo == -1)
        	    	{
        	    		Toast.makeText(MainActivity.this, "No File to Load!", Toast.LENGTH_LONG).show();
        	    	}
        	    	else if(dirNo < list.size())
        	    	{
        	    		dir = new File(dir.getAbsolutePath() + "/" + list.get(dirNo));
        	        	subFiles = dir.listFiles();
        	        	
        	        	list = new ArrayList<String>();
        	        	
        	        	if (subFiles != null)
        	        	{
        	        	    for (File file : subFiles)
        	        	    {
        	        	        list.add(file.getName());
        	        	    }
        	        	}
        	        	
        	        	int fileIndex = data.getIntExtra("file", 0);
        	        	
        	        	if(fileIndex < list.size())
        	        	{
	        	    		mSCanvas.clearScreen();
	        	    		if(mSCanvas.loadSAMMFile(dir.getAbsolutePath() + "/" 
	        	    				+ list.get(fileIndex), true))
	        	    		{
	        	    			savedName = dir.getName();
	        	    			isSaved = true;
	        	    			
	        	    			String fn = list.get(fileIndex);
	        	    			String[] fnSplit = fn.split("\\.");
	        	    			
	        	    			slideNo = Integer.parseInt(fnSplit[0]);
	        	    			maxSlideNo = list.size();
	        	    			
	        	    			Toast.makeText(MainActivity.this, "File Loaded!", Toast.LENGTH_LONG).show();
	        	    		}
        	        	}
        	    	}
        	    	
        			break;
        			
        		case 2://Exporting Project files to png
        			String dirPath = data.getStringExtra("dirPath");     
        			File directory	= new File(dirPath);
        			String dirName = directory.getName();
        	    	File[] files = directory.listFiles();
        	    
    	        	if (files != null)
    	        	{
    	        		Toast t = Toast.makeText(getApplicationContext(), "Exporting Now. Please Wait.", Toast.LENGTH_SHORT);
    	        		t.show();
    	        	    for (File file : files)
    	        	    {
    	        	    	mSCanvas.loadSAMMFile(directory.getAbsolutePath() + "/" + file.getName(),true);
    	        	    	exportFile(dirName+file.getName());
    	        	    }
    	        	}	
    	        	else
    	        	{
    	        		Toast t = Toast.makeText(getApplicationContext(), "No Files in Project. Please check project.", Toast.LENGTH_SHORT);
    	        		t.show();
    	        	}
        			break;
        			
        	    case 1234: //Loading a picture to the canvas
        	        Uri pictureUri = data.getData();
        			try {
        				
        				//get the file path to retrieve the file to find out its size in bytes
        				//stackoverflow.com/questions/6016000/how-to-open-phones-gallery-through-code
        				String[] filePC ={MediaStore.Images.Media.DATA};
        				Cursor cursor = getContentResolver().query(pictureUri, filePC, null, null, null);
        				cursor.moveToFirst();
        				String filePath =cursor.getString(cursor.getColumnIndex(filePC[0]));
        				File f = new File(filePath);
        				//System.out.println(filePath);
        				//System.out.println(f.length());       				
        			       			    
        			 
        				//file limit set to 1.5mb
        			    if(f.length()< 1572864)
        			    {	
        			    	Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(pictureUri));
	        			    RectF rectF = new RectF(0, 0, 200, 200); //Position and size of image
	        			    SObjectImage sImageObject = new SObjectImage();
	        			    sImageObject.setRect(rectF);
	        			    sImageObject.setImageBitmap(bitmap);
	        			    if(mSCanvas.insertSAMMImage(sImageObject, true) ) {}
        			    }
        			    else
        			    {
        			    	Toast.makeText(this, "Image is too big", Toast.LENGTH_SHORT).show();
        			    }
        			    
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
    
    /**
     * decrease animation speed. Not used currently
     */
    public void slowDownAnimation() {
        if (animationSpeed > SLOWEST_SPEED)
            mSCanvas.setAnimationSpeed(--animationSpeed);
    }
    
    /**
     * increase animation speed. Not used currently
     */
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
