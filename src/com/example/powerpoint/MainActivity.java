package com.example.powerpoint;

import java.io.File;

import com.samsung.samm.common.SAMMLibConstants;
import com.samsung.samm.common.SOptionPlay;
import com.samsung.samm.common.SOptionSCanvas;
import com.samsung.spensdk.SCanvasView;
import com.samsung.spensdk.applistener.AnimationProcessListener;
import com.samsung.spensdk.applistener.SCanvasInitializeListener;

import android.os.Build;
import android.os.Bundle;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
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
    
    String saveFile;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        mSCanvas.setOnTouchListener(new View.OnTouchListener() {      
            @Override
            public boolean onTouch(final View v, final MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    //We might be able to make this useful later.
                    
                    //System.out.println("OBJECTLIST" + mSCanvas.getSObjectList(false));
                    Toast.makeText(MainActivity.this, "Stroke completed", Toast.LENGTH_SHORT).show(); //To test stroke completion
                }
                return false;
            }
        });
        mCanvasContainer.addView(mSCanvas);
        
        File folder = getBaseContext().getCacheDir();
        //File folder = getBaseContext().getDir("folder", Context.MODE_PRIVATE);
        String folderPath = folder.getAbsolutePath();
        System.out.println(folderPath);
        saveFile = folderPath + "/" + "SAMM.data";
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Need to find a way to redo() after previewing the animation. Perhaps keep
    //a history of saveSammData()?.
    public void previewAnimation() {
        //This line ensures that the animation plays only up to the current state.
        //It will also wipe any redo() history.
        //mSCanvas.loadSAMMData(mSCanvas.saveSAMMData());
       
        
        System.out.println(saveFile);
        mSCanvas.saveSAMMFile(saveFile);
        mSCanvas.loadSAMMFile(saveFile, true, true);
        
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
