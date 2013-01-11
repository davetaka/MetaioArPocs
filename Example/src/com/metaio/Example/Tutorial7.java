// Copyright 2007-2012 metaio GmbH. All rights reserved.

package com.metaio.Example;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.metaio.sdk.GestureHandlerAndroid;
import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.jni.GestureHandler;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;
import com.metaio.sdk.jni.Rotation;
import com.metaio.sdk.jni.TrackingValues;
import com.metaio.sdk.jni.Vector2d;
import com.metaio.sdk.jni.Vector3d;
import com.metaio.tools.io.AssetsManager;

public class Tutorial7 extends MetaioSDKViewActivity 
{
	private MetaioSDKCallbackHandler mCallbackHandler;
	private IGeometry mMetaioMan;
	private IGeometry mTV;
	private IGeometry mScreen;
	private IGeometry mChair;
	private GestureHandlerAndroid mGestureHandler;
	private static ArrayList<String> mAnimations;
	private TrackingValues mTrackingValues;
	private int mGestureMask = GestureHandler.GESTURE_ALL;
	boolean mImageTaken = false;
	CharSequence text = "The screenshot has been saved in the image gallery.";
	int duration = Toast.LENGTH_SHORT;


	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		mCallbackHandler = new MetaioSDKCallbackHandler();
		
		mGestureHandler = new GestureHandlerAndroid(metaioSDK, mGestureMask);

		// animations for the geometry
		mAnimations = new ArrayList<String>();
		mAnimations.add("idle");
		mAnimations.add("close_down");
		mAnimations.add("close_idle");
		mAnimations.add("close_up");
		mAnimations.add("shock_down");
		mAnimations.add("shock_idle");
		mAnimations.add("shock_up");


	}

	@Override
	public boolean onTouch(View v, MotionEvent event) 
	{
		super.onTouch(v, event);

		mGestureHandler.onTouch(v, event);

		return true;
	}

	@Override
	protected int getGUILayout() 
	{
		// TODO: return 0 in case of no GUI overlay
		return R.layout.tutorial7;
	}

	@Override
	protected IMetaioSDKCallback getMetaioSDKCallbackHandler() 
	{
		return mCallbackHandler;
	}

	@Override
	public void onDrawFrame() 
	{
		super.onDrawFrame();
		
		// reset the location and scale of the geometries 
		if (mImageTaken == true)
		{
	
			// load the dummy tracking config file
			String path = AssetsManager.getAssetPath("Assets7/TrackingData_Dummy.xml");
			boolean result = metaioSDK.setTrackingConfiguration(path);
			MetaioDebug.log("Tracking data Dummy loaded: " + result);
			
			metaioSDK.setCosOffset(1, mTrackingValues);
			
			DisplayMetrics dm = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(dm);
			
		    Vector3d translation = metaioSDK.get3DPositionFromScreenCoordinates(1, new Vector2d(dm.widthPixels/2,dm.heightPixels/2));
		    
		    // reset the translation and scale of the geometries in case they are already activated before an image is taken
		    mTV.setTranslation(translation);
		    mTV.setScale(new Vector3d(10f, 10f, 10f));
		    mScreen.setTranslation(translation);
		    mScreen.setScale(new Vector3d(10f, 10f, 10f));
		    
		    mChair.setTranslation(translation);
		    mChair.setScale(new Vector3d(10f, 10f, 10f));
		    
		    mMetaioMan.setTranslation(translation);
		    mMetaioMan.setScale(new Vector3d(1f, 1f, 1f));
		    
		    mImageTaken = false;
		}
		

	}

	@Override
	protected void onStart() 
	{
		super.onStart();
		
		// hide GUI until SDK is ready
		if (!mRendererInitialized)
			mGUIView.setVisibility(View.GONE);
		
		String imagepath = Environment.getExternalStorageDirectory().getPath() + "/target.jpg";
		File file = new File(imagepath); 
		
		// if a tracking target image exists, then the app is still running in the background
		if (file.exists() && mTrackingValues != null)
		{
			// the tracking target has to be reset and so are the tracking values
			metaioSDK.setImage(imagepath);
			metaioSDK.setCosOffset(1, mTrackingValues);

		}

	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		
		// delete the tracking target image before exit if it has been generated
		String imagepath = Environment.getExternalStorageDirectory().getPath() + "/target.jpg";
		File file = new File(imagepath); 
		if (file.exists())
		{
			boolean result = file.delete();
			MetaioDebug.log("The file has been deleted: " + result);
		}
	}

	public void onButtonClick(View v) 
	{
		finish();
	}

	// called when the save screenshot button has been pressed
	public void onSaveScreen(View v) 
	{
		
		// a toast message to alert the user
		Context context = getApplicationContext();
		Toast toast = Toast.makeText(context, text, duration);
		toast.setGravity(Gravity.TOP|Gravity.LEFT, 0, 0);
		toast.show();
		
		// save a screenshot using SDK save it to an arbitrary directory
		String filename = "/testImg.jpg";
		String path = Environment.getExternalStorageDirectory().getPath();
		MetaioDebug.log("The imagePath is: " + path);
		String comPath = path + filename;
		metaioSDK.requestScreenshot(comPath);

	}

	// called when the reset button has been pressed
	public void onClearScreen(View v) 
	{
		// start the camera sensor
		metaioSDK.startCamera(0, mCameraResolution.getX(), mCameraResolution.getY());
		
		// delete the tracking target if generated
		String imagepath = Environment.getExternalStorageDirectory().getPath() + "/target.jpg";
		File file = new File(imagepath); 
		if (file.exists())
		{
			boolean result = file.delete(); 
			MetaioDebug.log("The file has been deleted: " + result);
		}
		
		// load the GPS tracking config file again
		String filepath = AssetsManager.getAssetPath("Assets7/TrackingData_GPSCompass.xml");
		boolean result = metaioSDK.setTrackingConfiguration(filepath);
		MetaioDebug.log("Tracking data loaded: " + result);
		
		// set the cos offset to move the 3D models along the z axis so that they can be viewed properly on the screen
		TrackingValues pose = new TrackingValues();
		pose.setTranslation(new Vector3d(0, 0, -140));
		metaioSDK.setCosOffset(1, pose);
		
		// reset the geometry buttons to unselected and hide the geometries
		ImageButton button = (ImageButton) findViewById(R.id.buttonTV);
		button.setImageResource(R.drawable.button_tv_unselected);
		button = (ImageButton) findViewById(R.id.buttonChair);
		button.setSelected(false);
		button.setImageResource(R.drawable.button_chair_unselected);
		button = (ImageButton) findViewById(R.id.buttonMan);
		button.setSelected(false);
		button.setImageResource(R.drawable.button_man_unselected);
		button.setSelected(false);

		setVisibleTV(false);
		setVisibleChair(false);
		setVisibleMan(false);
		
		// show the application layout again
		mGUIView.bringToFront();
		
	
	}

	// called when the take picture button has been pressed
	public void onTakePicture(View v) 
	{
		// take a picture using the SDK and save it to external storage
		String imagepath = Environment.getExternalStorageDirectory().getPath() + "/target.jpg";
		metaioSDK.requestCameraImage(imagepath, mCameraResolution.getX(), mCameraResolution.getY());	

	}


	// called when the TV button has been pressed
	public void onTVButtonClick(View v) 
	{
		ImageButton button = (ImageButton) v;
		button.setSelected(!button.isSelected());
		
		if (button.isSelected())
		{
			button.setImageResource(R.drawable.button_tv_selected);
			
			// reset the location and scale of the geometries
			DisplayMetrics dm = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(dm);
			
		    Vector3d translation = metaioSDK.get3DPositionFromScreenCoordinates(1, new Vector2d(dm.widthPixels/2,dm.heightPixels/2));
			mTV.setTranslation(translation);
			mScreen.setTranslation(translation);
			mTV.setScale(new Vector3d(10f, 10f, 10f));
			mScreen.setScale(new Vector3d(10f, 10f, 10f));
		}
		else
			button.setImageResource(R.drawable.button_tv_unselected);
		setVisibleTV(button.isSelected());

	}

	public void onChairButtonClick(View v) 
	{
		ImageButton button = (ImageButton) v;
		button.setSelected(!button.isSelected());
		
		if (button.isSelected())
		{
			button.setImageResource(R.drawable.button_chair_selected);
			
			DisplayMetrics dm = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(dm);			
		    Vector3d translation = metaioSDK.get3DPositionFromScreenCoordinates(1, new Vector2d(dm.widthPixels/2,dm.heightPixels/2));
			mChair.setTranslation(translation);
			mChair.setScale(new Vector3d(10f, 10f, 10f));
		}
		else
			button.setImageResource(R.drawable.button_chair_unselected);
		setVisibleChair(button.isSelected());

	}

	public void onManButtonClick(View v) 
	{
		ImageButton button = (ImageButton) v;
		button.setSelected(!button.isSelected());
		
		if (button.isSelected())
		{
			button.setImageResource(R.drawable.button_man_selected);
			
			DisplayMetrics dm = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(dm);	
		    Vector3d translation = metaioSDK.get3DPositionFromScreenCoordinates(1, new Vector2d(dm.widthPixels/2,dm.heightPixels/2));
			mMetaioMan.setTranslation(translation);
			mMetaioMan.setScale(new Vector3d(1f, 1f, 1f));
		}
		else
			button.setImageResource(R.drawable.button_man_unselected);
		setVisibleMan(button.isSelected());

	}

	@Override
	protected void loadContent() 
	{
		try 
		{

			// TODO: Load desired tracking data for planar marker tracking
			String filepath = AssetsManager.getAssetPath("Assets7/TrackingData_GPSCompass.xml");
			boolean result = metaioSDK.setTrackingConfiguration(filepath);
			
			// set the cos offset to move the 3D models along the z axis so that they can be viewed properly on the screen
			TrackingValues pose = new TrackingValues();
			pose.setTranslation(new Vector3d(0, 0, -140));
			metaioSDK.setCosOffset(1, pose);
			MetaioDebug.log("Tracking data loaded: " + result);

			// Load all the geometries
			// Load TV
			
			
			filepath = AssetsManager.getAssetPath("Assets7/tv.obj");
			if (filepath != null) 
			{
				mTV = metaioSDK.createGeometry(filepath);
				
				if (mTV != null) 
				{
					mTV.setScale(new Vector3d(10f, 10f, 10f));
					mTV.setRotation(new Rotation((float) Math.PI / 2f, 0f, -(float) Math.PI / 4f));
					mTV.setTranslation(new Vector3d(0f, 10f, 0f));

					mGestureHandler.addObject(mTV, 1, true);
				} 
				else
					MetaioDebug.log(Log.ERROR, "Error loading geometry: "
							+ filepath);
			}
			

			// Load screen
			filepath = AssetsManager.getAssetPath("Assets7/screen.obj");
			if (filepath != null) 
			{
				mScreen = metaioSDK.createGeometry(filepath);
				
				// the parameters for the screen should be exactly the same as the ones for the TV
				if (mScreen != null) 
				{
					mScreen.setScale(new Vector3d(10f, 10f, 10f));
					mScreen.setRotation(new Rotation((float) Math.PI / 2f, 0f,
							-(float) Math.PI / 4f));
					mScreen.setTranslation(new Vector3d(0f, 10f, 0f));

					mScreen.setMovieTexture(AssetsManager.getAssetPath("Assets7/sintel.3g2"));
					mScreen.startMovieTexture();

					mGestureHandler.addObject(mScreen, 1, true);
					setVisibleTV(false);
				} 
				else
					MetaioDebug.log(Log.ERROR, "Error loading geometry: "
							+ filepath);
			}

			// Load chair
			filepath = AssetsManager.getAssetPath("Assets7/stuhl.obj");
			if (filepath != null) 
			{
				mChair = metaioSDK.createGeometry(filepath);
				
				if (mChair != null) 
				{
					mChair.setScale(new Vector3d(10f, 10f, 10f));
					mChair.setTranslation(new Vector3d(0f, 0f, 0f));
					mChair.setRotation(new Rotation((float) Math.PI / 2f, 0f,
							0f));

					mGestureHandler.addObject(mChair, 2, true);
					setVisibleChair(false);
				} 
				else
					MetaioDebug.log(Log.ERROR, "Error loading geometry: "
							+ filepath);
			}
			

			// Load metaio man
			filepath = AssetsManager.getAssetPath("Assets7/metaioman.md2");
			if (filepath != null) 
			{
				mMetaioMan = metaioSDK.createGeometry(filepath);
				
				if (mMetaioMan != null) 
				{
					// Set geometry properties
					mMetaioMan.setScale(new Vector3d(1f, 1f, 1f));
					mMetaioMan.setTranslation(new Vector3d(0f, 0f, 0f));

					mGestureHandler.addObject(mMetaioMan, 3, true); // true
																		// is
																		// for
																		// make
																		// it
																		// touchable

					// Start first animation
					mMetaioMan.startAnimation(mAnimations.get(0), true);
					setVisibleMan(false);

				} 
				else
					MetaioDebug.log(Log.ERROR, "Error loading geometry: "
							+ filepath);
			}	
			

		} 
		catch (Exception e) 
		{

		}


	}

	private void setVisibleTV(boolean visible) 
	{
		if (mTV != null && mScreen != null)
		{
			mTV.setVisible(visible);
			mScreen.setVisible(visible);
		}
		if (visible) 
		{
			mScreen.startMovieTexture();
		} 
		else
			mScreen.stopMovieTexture();
	}

	private void setVisibleChair(boolean visible) 
	{
		if (mChair != null)
			mChair.setVisible(visible);
	}

	private void setVisibleMan(boolean visible) 
	{
		if (mMetaioMan != null)
			mMetaioMan.setVisible(visible);
	}

	@Override
	protected void onGeometryTouched(final IGeometry geometry) 
	{
		MetaioDebug
				.log("MetaioSDKCallbackHandler.onGeometryTouched: " + geometry);

		if (geometry.equals(mMetaioMan))
			geometry.startAnimation(mAnimations.get(4), false);

	}

	final class MetaioSDKCallbackHandler extends IMetaioSDKCallback 
	{

		@Override
		public void onSDKReady() 
		{
			// show GUI
			runOnUiThread(new Runnable() 
			{
				@Override
				public void run() 
				{
					mGUIView.setVisibility(View.VISIBLE);
				}
			});
		}
		
		@Override
		public void onAnimationEnd(IGeometry geometry, String animationName) 
		{
			MetaioDebug.log("MetaioSDKCallbackHandler.onAnimationEnd: "
					+ animationName);

			// Start a random animation from the list
			// final int index = (int)(Math.random()*mAnimations.size());
			geometry.startAnimation(mAnimations.get(0), false);
		}

		

		// callback function for taking images using SDK
        @Override
		public void onCameraImageSaved(String filepath) 
        {
			if (filepath.length() > 0)
				metaioSDK.setImage(filepath);

			// save the tracking values in case the application exits improperly
			mTrackingValues = new TrackingValues();
			mTrackingValues = metaioSDK.getTrackingValues(1);
					
			mImageTaken = true;

		}
        
        @Override
        public void onScreenshot(String filepath)
        {
    		// the screenshot can also be saved to the image gallery
    		Bitmap bm = BitmapFactory.decodeFile(filepath);
    		MediaStore.Images.Media.insertImage(getContentResolver(), bm, "screenshot_"+System.currentTimeMillis(), "screenshot");
    		// delete the screenshot outside the image gallery
    		File file = new File(filepath); 
    		boolean deleted = file.delete(); 
    		if (deleted)
    			MetaioDebug.log("The image file has been succesfully deleted.");
        }

	}

}

