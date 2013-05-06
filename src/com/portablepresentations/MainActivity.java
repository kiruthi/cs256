
package com.portablepresentations;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.samsung.samm.common.SAMMLibConstants;
import com.samsung.samm.common.SObjectImage;
import com.samsung.samm.common.SOptionPlay;
import com.samsung.samm.common.SOptionSCanvas;
import com.samsung.spensdk.SCanvasConstants;
import com.samsung.spensdk.SCanvasView;
import com.samsung.spensdk.applistener.AnimationProcessListener;
import com.samsung.spensdk.applistener.SCanvasInitializeListener;
import com.samsung.spensdk.applistener.SPenHoverListener;
import com.samsung.spensdk.applistener.SPenTouchListener;

import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.RectF;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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
    
    //Presentation isSaved and name variables
    private boolean isSaved = false;
    private boolean isNewSlide = false;
    private boolean isSaveCalled = false;
    private boolean isNewPres = false;
    private String savedName;
    
    //SCanvas variables
    private RelativeLayout mCanvasContainer;
    private SCanvasView mSCanvas;
    private int animationSpeed;
    private int lastMode;
    private SOptionSCanvas options;
    private AudioManager audioManager;
    private HashMap<String,Integer> settingResourceMapInt;
    
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
        
        animationSpeed = SOptionPlay.ANIMATION_SPEED_SLOW;
        options = new SOptionSCanvas();
        options.mPlayOption.setSoundEffectOption(false); //Disable artificial sound effects
        mCanvasContainer = (RelativeLayout) findViewById(R.id.canvas_container);
        mSCanvas = new SCanvasView(this);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        
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
                        if (mSCanvas.isVoiceRecording()) {
                            mSCanvas.recordVoiceComplete();
                            mSCanvas.setBGAudioAsRecordedVoice();
                        }
                        
                        mSCanvas.setAnimationMode(false); //Enable drawing again
                        mSCanvas.setCanvasMode(lastMode);
                        //audioManager.setSpeakerphoneOn(false);
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
                        if (mSCanvas.isAnimationMode())
                            Toast.makeText(MainActivity.this, "Cannot edit in animation mode!", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    
                    @Override
                    public boolean onTouchPen(View arg0, MotionEvent arg1) {
                        if (mSCanvas.isAnimationMode())
                            Toast.makeText(MainActivity.this, "Cannot edit in animation mode!", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    
                    @Override
                    public boolean onTouchPenEraser(View arg0, MotionEvent arg1) {
                        if (mSCanvas.isAnimationMode())
                            Toast.makeText(MainActivity.this, "Cannot edit in animation mode!", Toast.LENGTH_SHORT).show();
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
        
        List<String> slideLst = new ArrayList<String>();
        slideLst.add("Slide 0");
        
        addItemsToList(slideLst, -1);
    }
    
    public void addItemsToList(List<String> slideLst, int selectedSlideNo)
    {
    	
    	ArrayAdapter<String> adapter = new ArrayAdapter<String>
        (MainActivity.this, R.layout.activity_main, 
                R.id.textView1, slideLst);
    	
    	
    	final ListView listView = (ListView)findViewById(R.id.slideView);
    	listView.setAdapter(null);
    	listView.setAdapter(adapter);
    	
    	if(selectedSlideNo != -1)
    	{
//    		View selected = listView.getChildAt(selectedSlideNo);
    		View selected = adapter.getView(selectedSlideNo, null, null);
    		
    		if(null != selected)
    		{
//    			selected.setBackgroundColor(Color.LTGRAY);
    		}
    	}
    	
    	listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				
				String fn;
		    	String[] fnSplit;
		    	
		    	View selected = arg0.getChildAt(arg2);
	    		
	    		if(null != selected)
	    		{
//	    			selected.setBackgroundColor(Color.LTGRAY);
	    		}
				
				if(isSaved)
				{
					screenCapture(savedName);
				
					File dir = getBaseContext().getDir("spen", 0);
					dir = new File(dir.getAbsolutePath() + "/" + savedName);
					
					File[] fName = dir.listFiles();
					
					List<String> newSlideLst = new ArrayList<String>();
					
					if(fName != null)
					{
						for(File file : fName)
						{
							fn = file.getName();
							fnSplit = fn.split("\\.");
							newSlideLst.add("Slide " + fnSplit[0]);
						}
					}
					
					Arrays.sort(fName);
					fn = fName[arg2].getName();
					fnSplit = fn.split("\\.");
					
					slideNo = Integer.parseInt(fnSplit[0]);
					maxSlideNo = newSlideLst.size();
					
					if(arg2 < fName.length)
					{
						mSCanvas.clearScreen();
						mSCanvas.loadSAMMFile(dir.getAbsolutePath() 
								+ "/" + fName[arg2].getName(), true);
					}
					
					Collections.sort(newSlideLst);
					
					String sName = newSlideLst.get(arg2);
					sName = "*" + sName;
					
					newSlideLst.remove(arg2);
					newSlideLst.add(arg2, sName);
					
					addItemsToList(newSlideLst, arg2);
				}
			}
		});
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

    	try{
      		File presFile = getBaseContext().getDir("spen", 0);
      		File folder = new File(presFile.getAbsolutePath() + "/" + fileName);
      		
      		if(!folder.exists())
      		{
      			folder.mkdir();
      		}
      		
      		String savefName = presFile.getAbsolutePath() + "/" + fileName + "/" + slideNo;
      		
      		if(mSCanvas.saveSAMMFile(savefName))
      		{
      			if(!isSaved && isNewSlide  && !isNewPres)
      			{
      				refreshSlideList();
      			}
      			
      			if(isNewSlide)
      			{
      				isNewSlide = false;
      				mSCanvas.clearScreen();
      			}
      			
      			isSaved = true;
      			Toast.makeText(this, "Saved slide " + slideNo + "!", Toast.LENGTH_SHORT).show();
      			
      			if(!isSaveCalled)
      			{
	      			if(maxSlideNo == 0)
	          		{
	          			slideNo++;
	          		}
	          		else
	          		{
	          			slideNo = maxSlideNo;
	          			maxSlideNo = 0;
	          		}
      			}
      			else
      			{
      				isSaveCalled = false;
      			}
      			
      			if(isNewPres)
      			{
      				savedName = "";
    		    	isSaved = false;
    		    	slideNo = 0;
    		    	maxSlideNo = 0;
      				isNewPres = false;
      			}
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
                if (mSCanvas.getCanvasMode() == SCanvasConstants.SCANVAS_MODE_INPUT_TEXT)
                    mSCanvas.toggleShowSettingView(SCanvasConstants.SCANVAS_SETTINGVIEW_TEXT);
                mSCanvas.setCanvasMode(SCanvasConstants.SCANVAS_MODE_INPUT_TEXT);
            	return true;
            case R.id.action3:
                if (mSCanvas.getCanvasMode() == SCanvasConstants.SCANVAS_MODE_INPUT_PEN)
                    mSCanvas.toggleShowSettingView(SCanvasConstants.SCANVAS_SETTINGVIEW_PEN);
                mSCanvas.setCanvasMode(SCanvasConstants.SCANVAS_MODE_INPUT_PEN);
                return true;
            case R.id.action4:            	
                if (mSCanvas.getCanvasMode() == SCanvasConstants.SCANVAS_MODE_INPUT_ERASER)
                    mSCanvas.toggleShowSettingView(SCanvasConstants.SCANVAS_SETTINGVIEW_ERASER);
                mSCanvas.setCanvasMode(SCanvasConstants.SCANVAS_MODE_INPUT_ERASER);
                return true;
            case R.id.action5:
                loadFile();
                return true;
            case R.id.action6:
            	isSaveCalled = true;
                saveDialog();
                return true; 
            case R.id.action7:
                clearDialog();
                return true; 
            case R.id.action8:
            	createNewSlide();
                //pauseResume();
            	return true;
            case R.id.action9:
            	createNewPresentation();
            	return true;          
            case R.id.options:
            	optionsDialog();
            	return true;
           /* 	
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
                return true;*/
            case R.id.voiceOver:
                if (!mSCanvas.isAnimationMode()) {
                    audioManager.setSpeakerphoneOn(true);
                    mSCanvas.clearBGAudio();
                    mSCanvas.setAnimationMode(true);
                    mSCanvas.recordVoiceStart();
                    previewAnimation();
                }
                else
                    Toast.makeText(MainActivity.this, "Can't record until animation is complete!", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.play:
                if (!pauseResume())
                    previewAnimation();
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
    	AlertDialog.Builder alert= new AlertDialog.Builder(this);
    	alert.setTitle("New Presentation");
    	alert.setMessage("Do you want to save current changes?");
    	
    	alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				isNewPres = true;
				
				createNewSlide();
				
		    	List<String> slideLst = new ArrayList<String>();
		        slideLst.add("Slide 0");
		        
		        addItemsToList(slideLst, -1);
			}
		});
    	
    	alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {

				savedName = "";
		    	isSaved = false;
		    	slideNo = 0;
		    	maxSlideNo = 0;
		    	
		    	mSCanvas.clearScreen();
		    	
		    	List<String> slideLst = new ArrayList<String>();
		        slideLst.add("Slide 0");
		        
		        addItemsToList(slideLst, -1);
				
			}
		});
    	alert.show();
    	
    }
    
    public void refreshSlideList()
    {
    	if(null != savedName && savedName != "")
    	{
    	String fn;
    	String[] fnSplit;
    	
    	File dir = getBaseContext().getDir("spen", 0);
		dir = new File(dir.getAbsolutePath() + "/" + savedName);
		
		if(dir.exists())
		{
			File[] fName = dir.listFiles();
			
			List<String> newSlideLst = new ArrayList<String>();
			
			if(fName != null)
			{
				for(File file : fName)
				{
					fn = file.getName();
					fnSplit = fn.split("\\.");
					newSlideLst.add("Slide " + fnSplit[0]);
				}
			}
			
			newSlideLst.add("Slide " + newSlideLst.size());
			
			Collections.sort(newSlideLst);
			addItemsToList(newSlideLst, -1);
			
		}
		else
		{
			List<String> newSlideLst = new ArrayList<String>();
			addItemsToList(newSlideLst, -1);
		}
    	}
    }
    
    /**
     * Adds a new slide to the presentation. If presentation is not saved, saved dialog will pop up.
     */
    public void createNewSlide()
    {
    	isNewSlide = true;
    	
    	if(!isSaved)
    	{
    		saveDialog();
    	}
    	else
    	{
    		screenCapture(savedName);
    	}
    	
    	refreshSlideList();
		
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
     
  /**      
        //Button for text input
        Button textButton = (Button) myDialog.findViewById (R.id.textButton);
        textButton.setText("Text Mode");
        textButton.setOnClickListener( new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mSCanvas.setCanvasMode(SCanvasConstants.SCANVAS_MODE_INPUT_TEXT);
                mSCanvas.toggleShowSettingView(SCanvasConstants.SCANVAS_SETTINGVIEW_TEXT);
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
                mSCanvas.toggleShowSettingView(SCanvasConstants.SCANVAS_SETTINGVIEW_PEN);
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
                mSCanvas.toggleShowSettingView(SCanvasConstants.SCANVAS_SETTINGVIEW_ERASER);
                myDialog.dismiss();
            }
        });
     **/   
        
      //button to delete a presentation
        Button deletePres = (Button) myDialog.findViewById (R.id.deletePresButton);
        deletePres.setText("Delete Presentation");
        deletePres.setOnClickListener( new OnClickListener() {

            @Override
            public void onClick(View arg0) {
            	deletePresentation();
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
        
  /**
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
  */
        
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
  
 /**       
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
 */       
        Button selectorButton = (Button) myDialog.findViewById (R.id.selectorButton);
        selectorButton.setText("Selector Tool");
        selectorButton.setOnClickListener( new OnClickListener() {

            @Override
            public void onClick(View arg0) {
            	mSCanvas.setMultiSelectionMode(true);
                myDialog.dismiss();
            }
        });
        
        Button groupButton = (Button) myDialog.findViewById (R.id.groupButton);
        groupButton.setText("Group Items");
        groupButton.setOnClickListener( new OnClickListener() {

            @Override
            public void onClick(View arg0) {
            	mSCanvas.groupSAMMObjectList(mSCanvas.getSelectedSObjectList(), true);
                myDialog.dismiss();
            }
        });
        
        Button ungroupButton = (Button) myDialog.findViewById (R.id.ungroupButton);
        ungroupButton.setText("Ungroup Item");
        ungroupButton.setOnClickListener( new OnClickListener() {

            @Override
            public void onClick(View arg0) {
            	mSCanvas.ungroupSelectedObjects();
                myDialog.dismiss();
            }
        });
        //displays dialog box
        myDialog.show();     
    }
    
    /**
     * Starts DisplayFileActivity to delete a presentation.
     */
    public void deletePresentation()
    {
    	Intent intent = new Intent(MainActivity.this, DisplayFileActivity.class);
    	intent.putExtra("option", "delete");
    	startActivityForResult(intent, 3);
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
	    	//If presentation is not currently saved it asks for the name for the presentation
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
    		//else add current slide to presentation
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
            	mSCanvas.clearBGAudio();
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
		/*View content = findViewById(R.id.canvas_container);
		
		Bitmap bitmap = Bitmap.createBitmap(
				2000, 800, Bitmap.Config.ARGB_8888);     
		
		Canvas canvas = new Canvas(bitmap);
		
		content.layout(0, 0, 2000, 800);
		content.draw(canvas);*/
        
//     	Bitmap bitmap = content.getDrawingCache(true);
     	
     	//creates a new file 
//    	File file = new File(Environment.getExternalStorageDirectory().getPath()+"/"+fileName);
    	try {
    		//outputs files as bitmap in PNGformat
			/*file.createNewFile();
			FileOutputStream outStream = new FileOutputStream(file);
			bitmap.compress(CompressFormat.PNG, 100, outStream);
			outStream.close();*/
    		
    		mSCanvas.saveSAMMFile(Environment.getExternalStorageDirectory().getPath()+"/"+fileName);
			
		} catch (Exception e) {
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
        	    	
        	    	String fn;
	    			String[] fnSplit;
        	    	
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
        	        	List<String> slideLst = new ArrayList<String>();
        	        	
        	        	if (subFiles != null)
        	        	{
        	        	    for (File file : subFiles)
        	        	    {
        	        	        list.add(file.getName());
        	        	        fn = file.getName();
	        	    			fnSplit = fn.split("\\.");
        	        	        slideLst.add("Slide " + fnSplit[0]);
        	        	    }
        	        	}
        	        	
        	        	Collections.sort(slideLst);
        	        	Collections.sort(list);
        	        	
        	        	addItemsToList(slideLst, -1);
        	        	
        	        	int fileIndex = 0;
        	        	
        	        	if(fileIndex < list.size())
        	        	{
	        	    		mSCanvas.clearScreen();
	        	    		if(mSCanvas.loadSAMMFile(dir.getAbsolutePath() + "/" 
	        	    				+ list.get(0), true))
	        	    		{
	        	    			savedName = dir.getName();
	        	    			isSaved = true;
	        	    			
	        	    			fn = list.get(fileIndex);
	        	    			fnSplit = fn.split("\\.");
	        	    			
	        	    			slideNo = Integer.parseInt(fnSplit[0]);
	        	    			maxSlideNo = list.size();
	        	    			
	        	    			Toast.makeText(MainActivity.this, "File Loaded!", Toast.LENGTH_LONG).show();
	        	    		}
        	        	}
        	    	}
        	    	
        			break;
        			
        		case 2://Exporting Project files to png
        			String dirPath = data.getStringExtra("dirPath"); 
        			int dirNum = data.getIntExtra("dir", 0);
        	    	
        	    	if(dirNum == -1)
        	    	{
        	    		Toast.makeText(MainActivity.this, "No Saved Projects Found!", Toast.LENGTH_LONG).show();
        	    	}
        	    	else
        	    	{
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
        	    	}
        			
        			break;
        			
        		case 3:
        			
        			int noOfFiles = data.getIntExtra("dir", 0);
        			
        			if(noOfFiles == -1)
        			{
        				Toast.makeText(MainActivity.this, "No Files to delete!", Toast.LENGTH_SHORT).show();
        			}
        			else
        			{
        				String status = data.getStringExtra("status");
        				
        				if(status != null && status.equals("ok"))
        				{
        					savedName = "";
            		    	isSaved = false;
            		    	slideNo = 0;
            		    	maxSlideNo = 0;
            		    	
            		    	mSCanvas.clearScreen();
            		    	
            		    	List<String> slideLst = new ArrayList<String>();
            		        slideLst.add("Slide 0");
            		        
            		        addItemsToList(slideLst, -1);
            		    	
        					Toast.makeText(MainActivity.this, "Presentation deleted!", Toast.LENGTH_SHORT).show();
        				}
        				else
        				{
        					Toast.makeText(MainActivity.this, "Delete Cancelled!", Toast.LENGTH_SHORT).show();
        				}
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
        lastMode = mSCanvas.getCanvasMode();
        mSCanvas.setCanvasMode(SCanvasConstants.SCANVAS_MODE_INPUT_PEN);
        mSCanvas.setAnimationMode(true);
        mSCanvas.setAnimationSpeed(animationSpeed);
        if (!mSCanvas.doAnimationStart())
            mSCanvas.setCanvasMode(lastMode);
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
    
    /**
     * Toggle pause/resume when doing animation playback
     * @return true if pause/resume was successful.
     */
    public boolean pauseResume() {
    	int state = mSCanvas.getAnimationState();
        
        if (state == SAMMLibConstants.ANIMATION_STATE_ON_PAUSED) {
            mSCanvas.doAnimationResume();
            return true;
        }
        else if (state == SAMMLibConstants.ANIMATION_STATE_ON_RUNNING) {
            mSCanvas.doAnimationPause();
            return true;
        }
        
        return false;
    }
}
