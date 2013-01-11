// Copyright 2007-2012 metaio GmbH. All rights reserved.
package com.metaio.Example;

import java.util.HashMap;

import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.View;

import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;
import com.metaio.sdk.jni.Rotation;
import com.metaio.sdk.jni.TrackingValues;
import com.metaio.sdk.jni.Vector3d;
import com.metaio.tools.io.AssetsManager;

public class Tutorial6 extends MetaioSDKViewActivity 
{

	
	private IGeometry mTigerModel;
	private MetaioSDKCallbackHandler mCallbackHandler;

	String trackingConfigFile = null;
	boolean mIsCloseToModel;
	
	private SoundPool mSoundPool;
    private HashMap<String, Integer> mSoundPoolMap;
	private int mCurrentStreamID;
	

	/* This method is regularly called, calculates the distance between phone and target
	 * and performs actions based on the distance 
	 */
	private void checkDistanceToTarget() {
		
		// get tracing values for COS 1
		TrackingValues currentPose = metaioSDK.getTrackingValues(1);
		
		// if the quality value > 0, it means we're currently tracking
		// Note, you can use this mechanism also to detect if something is tracking or not.
		// (e.g. for triggering an action as soon as some target is visible on screen)
		if (currentPose.getQuality() > 0)
		{
			// get the translation part of the pose
			Vector3d poseTranslation = currentPose.getTranslation();
			
			// calculate the distance as sqrt( x^2 + y^2 + z^2 )
			float distanceToTarget = FloatMath.sqrt(poseTranslation.getX() * poseTranslation.getX() +  poseTranslation.getY() * poseTranslation.getY() + poseTranslation.getZ() * poseTranslation.getZ());
			
			// define a threshold distance
			float threshold = 150;
			
			// if we are already close to the model
			if (mIsCloseToModel)
			{
				// if our distance is larger than our threshold (+ a little)
				if (distanceToTarget > (threshold + 10))
				{
					// we flip this variable again
					mIsCloseToModel = false;
					playSound();
					mTigerModel.startAnimation("meow", false);
				}
			}
			else 
			{
				// we're not close yet, let's check if we are now
				if (distanceToTarget < threshold)
				{
					// flip the variable
					mIsCloseToModel = true;
					playSound();
					mTigerModel.startAnimation("meow", false);
				}
			}
			
		}
	}

	private void playSound()
	{
		try
		{
			MetaioDebug.log("Playing sound");
			mSoundPool.stop(mCurrentStreamID);
	        mCurrentStreamID = mSoundPool.play(mSoundPoolMap.get("meow"), 1, 1, 1, 0, 1f);
		}
		catch (Exception e)
		{
			MetaioDebug.log("Error playing sound: "+e.getMessage());
		}
	}
	
	@Override
	protected int getGUILayout() 
	{
		// TODO: return 0 in case of no GUI overlay
		return R.layout.tutorial6; 
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		mCallbackHandler = new MetaioSDKCallbackHandler();
		super.onCreate(savedInstanceState);
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
		
		if (metaioSDK != null)
		{
			if (mTigerModel.isVisible())
			{
				checkDistanceToTarget();
			}
		}
	}

	@Override
	protected void onStart() 
	{
		super.onStart();
		
		// hide GUI until SDK is ready
		if (!mRendererInitialized)
			mGUIView.setVisibility(View.GONE);
		
		mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        mSoundPoolMap = new HashMap<String, Integer>();
        String soundPath = AssetsManager.getAssetPath("Assets6/meow.mp3");
        MetaioDebug.log("Sound is loaded: "+soundPath);
        mSoundPoolMap.put("meow", mSoundPool.load(soundPath, 1));
	}
	
	@Override
	protected void onStop() 
	{
		mSoundPool.stop(mCurrentStreamID);
		mSoundPool.release();
		mSoundPool = null;
		super.onStop();
	}

	public void onButtonClick(View v)
	{
		finish();
	}
	
	public void onInstant2dButtonClick(View v)
	{
		mTigerModel.setRotation(new Rotation(0f, 0f, (float)Math.PI));
		metaioSDK.startInstantTracking("INSTANT_2D");
		MetaioDebug.log("Instant snapshot is made");
	}
	
	public void onInstant2dRectifiedButtonClick(View v)
	{
		mTigerModel.setRotation(new Rotation(0f, 0f, (float)-Math.PI));
		metaioSDK.startInstantTracking("INSTANT_2D_GRAVITY");
		MetaioDebug.log("Instant rectified snapshot is made");
	}
	
	public void onInstant3dButtonClick(View v)
	{
		metaioSDK.startInstantTracking("INSTANT_3D");
		MetaioDebug.log("SLAM is initialized");
	}
	
	@Override
	protected void loadContent() 
	{
		try
		{
			
			        
			// Load tiger model
			String tigerModelPath = AssetsManager.getAssetPath("Assets6/tiger.md2");			
			if (tigerModelPath != null) 
			{
				mTigerModel = metaioSDK.createGeometry(tigerModelPath);
				if (mTigerModel != null) 
				{
					// Set geometry properties
					mTigerModel.setScale(new Vector3d(8f, 8f, 8f));
					MetaioDebug.log("Loaded geometry "+tigerModelPath);
				}
				else
					MetaioDebug.log(Log.ERROR, "Error loading geometry: "+tigerModelPath);
			}
			
		
		}       
		catch (Exception e)
		{
			
		}
	}
	
  
	@Override
	protected void onGeometryTouched(IGeometry geometry) {
		// TODO Auto-generated method stub
		
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
		public void onInstantTrackingEvent(boolean success, String file)
		{
			if(success)
			{
				MetaioDebug.log("MetaioSDKCallbackHandler.onInstantTrackingEvent: "+file);
				metaioSDK.setTrackingConfiguration(file);
			}
			else 
			{
				MetaioDebug.log("Failed to create instant tracking configuration!");
			}
		}
	}
	
}
