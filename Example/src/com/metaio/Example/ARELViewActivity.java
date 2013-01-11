// Copyright 2007-2012 metaio GmbH. All rights reserved.
package com.metaio.Example;


import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

import com.metaio.sdk.ARELInterpreterAndroidJava;
import com.metaio.sdk.GestureHandlerAndroid;
import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.jni.GestureHandler;
import com.metaio.sdk.jni.IARELInterpreterCallback;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;
import com.metaio.sdk.jni.Vector3d;


public class ARELViewActivity extends MetaioSDKViewActivity 
{

	/**
	 * Path to AREL file to be loaded
	 */
	private String mARELFile;
	
	/**
	 * The WebView where we display the AREL HTML page and take care of JavaScript
	 */
	protected WebView mWebView;
	
	/**
	 * This class is the main interface to AREL
	 */
	protected ARELInterpreterAndroidJava mARELInterpreter;
	
	/**
	 * ARELInterpreter callback
	 */
	protected ARELInterpreterCallback mARELCallback;
	
	/**
	 * Gesture handler
	 */
	protected GestureHandlerAndroid mGestureHandler;

	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		mARELFile = getIntent().getStringExtra("arelConfigFile");
		MetaioDebug.log("Starting AREL with config "+mARELFile);
		
		// create the AREL interpreter and its callback
		mARELInterpreter = new ARELInterpreterAndroidJava();
		mARELCallback = new ARELInterpreterCallback();
		mARELInterpreter.registerCallback(mARELCallback);

	}

	@Override
	protected int getGUILayout() 
	{
		return R.layout.arelwebview;
	}
	
	public void onButtonClick(View v)
	{
		finish();
	}

	@Override
	protected void onStart() 
	{
		super.onStart();
		
		// create and add the AREL WebView
		mWebView = (WebView)findViewById(R.id.arelwebview);
		
		// attach a WebView to the AREL interpreter and initialize it
		mARELInterpreter.initWebView(mWebView, this);
				
	}

	
	@Override
	protected void onStop() 
	{
		super.onStop();
		
	}
	
	@Override
	protected void onDestroy() 
	{
		super.onDestroy();
		mARELInterpreter.release();
		mARELInterpreter.delete();
		mARELInterpreter = null;
		mARELCallback.delete();
		mARELCallback = null;
		mRendererInitialized = false;
		mWebView.setOnTouchListener(null);
		mWebView = null;
		mGestureHandler.delete();
		mGestureHandler = null;
	}
	

	@Override
	public void onDrawFrame() 
	{
		// instead of mobileSDK.render, we call mArel.update()
		if (mRendererInitialized)
			mARELInterpreter.update();

	}
	
	@Override
	public void onSurfaceChanged(int width, int height)
	{
		super.onSurfaceChanged(width, height);
		if( mRendererInitialized)	
			mARELInterpreter.onSurfaceChanged(width, height);
	}
	
	@Override
	protected void onResume() 
	{
		super.onResume();
		if ((mARELInterpreter != null) && (mRendererInitialized))
			mARELInterpreter.onResume();
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		if ((mARELInterpreter != null) && (mRendererInitialized))
			mARELInterpreter.onPause();
	}
	

	@Override
	protected void loadContent() 
	{
		// create gesture handler and initialize AREL interpreter
		mGestureHandler = new GestureHandlerAndroid(metaioSDK, GestureHandler.GESTURE_ALL, mWebView, mSurfaceView);
		mARELInterpreter.initialize( metaioSDK, mGestureHandler );	
	}
	
	/**
	 * Load AREL file once the SDK is ready
	 */
	protected void loadARELScene()
	{
		runOnUiThread(new Runnable() 
		{
			
			@Override
			public void run() 
			{
				// load AREL file
				mARELInterpreter.loadARELFile(mARELFile);
				
				// set custom radar properties
				mARELInterpreter.setRadarProperties(IGeometry.ANCHOR_TL, new Vector3d(1), new Vector3d(1));
				
				// handle all touch events through gesture handler
				mWebView.setOnTouchListener(mGestureHandler);

			}
		});
		
	}
	
	
	@Override
	protected void onGeometryTouched(final IGeometry geometry) 
	{
		MetaioDebug.log("Geometry touched: "+geometry);

	}

	@Override
	protected IMetaioSDKCallback getMetaioSDKCallbackHandler() 
	{
		return null;
	}
	
	class ARELInterpreterCallback extends IARELInterpreterCallback
	{
		@Override
		public void onSDKReady() 
		{
			loadARELScene();
		}
	}
 
}
