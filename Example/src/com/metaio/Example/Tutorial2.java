// Copyright 2007-2012 metaio GmbH. All rights reserved.
package com.metaio.Example;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;
import com.metaio.sdk.jni.Rotation;
import com.metaio.sdk.jni.Vector3d;
import com.metaio.tools.io.AssetsManager;

public class Tutorial2 extends MetaioSDKViewActivity 
{

	
	private IGeometry mMetaioMan;
	private IGeometry mImagePlane;
	private IGeometry mMoviePlane;
	private IGeometry mTruck;

	private MetaioSDKCallbackHandler mCallbackHandler;
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		mCallbackHandler = new MetaioSDKCallbackHandler();
	}

	
	@Override
	protected int getGUILayout() 
	{
		// TODO: return 0 in case of no GUI overlay
		return R.layout.tutorial2; 
	}



	@Override
	protected void onStart() 
	{
		super.onStart();
		
		// hide GUI until SDK is ready
		if (!mRendererInitialized)
			mGUIView.setVisibility(View.GONE);
	}

	public void onButtonClick(View v)
	{
		finish();
	}
	
	public void onModelButtonClick(View v)
	{
		mImagePlane.setVisible(false);
		mMetaioMan.setVisible(true);
		mMoviePlane.setVisible(false);
		mTruck.setVisible(false);
		
		mMoviePlane.stopMovieTexture();
	}
	
	public void onImageButtonClick(View v)
	{
		mImagePlane.setVisible(true);
		mMetaioMan.setVisible(false);
		mMoviePlane.setVisible(false);
		mTruck.setVisible(false);
		
		mMoviePlane.stopMovieTexture();
	}
	
	public void onMovieButtonClick(View v)
	{
		mImagePlane.setVisible(false);
		mMetaioMan.setVisible(false);
		mMoviePlane.setVisible(true);
		mTruck.setVisible(false);
		
		mMoviePlane.startMovieTexture(true); // loop = true;
	}
	
	public void onTruckButtonClick(View v)
	{
		mImagePlane.setVisible(false);
		mMetaioMan.setVisible(false);
		mMoviePlane.setVisible(false);
		mTruck.setVisible(true);
		
		mMoviePlane.stopMovieTexture();
	}
	
	@Override
	protected void loadContent() 
	{
		try
		{
			
			// Load desired tracking data for planar marker tracking
			final String trackingConfigFile = AssetsManager.getAssetPath("Assets2/TrackingData_MarkerlessFast.xml");
			
			
			boolean result = metaioSDK.setTrackingConfiguration(trackingConfigFile); 
			MetaioDebug.log("Tracking data loaded: " + result); 
	        
			// Load all the geometries. First - Model
			String modelPath = AssetsManager.getAssetPath("Assets2/metaioman.md2");			
			if (modelPath != null) 
			{
				mMetaioMan = metaioSDK.createGeometry(modelPath);
				if (mMetaioMan != null) 
				{
					// Set geometry properties
					mMetaioMan.setScale(new Vector3d(4.0f, 4.0f, 4.0f));
					MetaioDebug.log("Loaded geometry "+modelPath);
				}
				else
					MetaioDebug.log(Log.ERROR, "Error loading geometry: "+modelPath);
			}
			
			// Loading image geometry
			String imagePath = AssetsManager.getAssetPath("Assets2/frame.png");
			if (imagePath != null)
			{
				mImagePlane = metaioSDK.createGeometryFromImage(imagePath, false);
				if (mImagePlane != null)
				{
					mImagePlane.setScale(new Vector3d(3.0f,3.0f,3.0f));
					mImagePlane.setVisible(false);
					MetaioDebug.log("Loaded geometry "+imagePath);
				}
				else {
					MetaioDebug.log(Log.ERROR, "Error loading geometry: "+imagePath);
				}
			}
			
			// Loading movie geometry
			String moviePath = AssetsManager.getAssetPath("Assets2/demo_movie.3g2");
			if (moviePath != null)
			{
				mMoviePlane = metaioSDK.createGeometryFromMovie(moviePath, true);
				if (mMoviePlane != null)
				{
					mMoviePlane.setScale(new Vector3d(2.0f,2.0f,2.0f));
					mMoviePlane.setRotation(new Rotation(0f, 0f, (float)-Math.PI/2));
					mMoviePlane.setVisible(false);
					MetaioDebug.log("Loaded geometry "+moviePath);
				}
				else {
					MetaioDebug.log(Log.ERROR, "Error loading geometry: "+moviePath);
				}
			}
			
			// loading truck geometry
			String truckPath = AssetsManager.getAssetPath("Assets2/truck/truck.obj");
			if (truckPath != null)
			{
				mTruck = metaioSDK.createGeometry(truckPath);
				if (mTruck != null)
				{
					mTruck.setScale(new Vector3d(2.0f, 2.0f, 2.0f));
					mTruck.setRotation(new Rotation((float)Math.PI/2, 0f, (float)Math.PI));
					mTruck.setVisible(false);
					MetaioDebug.log("Loaded geometry " + truckPath);
				}
				else
				{
					MetaioDebug.log(Log.ERROR, "Error loading geometry: " + truckPath);
				}
			}
			
			// loading environment maps
			String path = AssetsManager.getAssetPath("Assets2/truck/env_map");
			if (path != null)
			{
				boolean loaded = metaioSDK.loadEnvironmentMap(path);
				MetaioDebug.log("environment mapts loaded: " + loaded);
			}
			else
			{
				MetaioDebug.log(Log.ERROR, "Error loading environment maps at: " + path);
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



	@Override
	protected IMetaioSDKCallback getMetaioSDKCallbackHandler() 
	{
		return mCallbackHandler;
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
	}
	
}
