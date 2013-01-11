// Copyright 2007-2012 metaio GmbH. All rights reserved.
package com.metaio.Example;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.metaio.sdk.MetaioDebug;
import com.metaio.tools.io.AssetsManager;


@SuppressLint("SetJavaScriptEnabled")
public class MainActivity extends Activity
{
	
	WebView mWebView;

	/**
	 * Task that will extract all the assets
	 */
	AssetsExtracter mTask;
	
	/**
	 * Progress view
	 */
	View mProgress;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.webview);
		 
		mProgress = findViewById(R.id.progress);
		
		// extract all the assets
		mTask = new AssetsExtracter();
		mTask.execute(0);
		
		MetaioDebug.enableLogging(true);
		
		mWebView = (WebView) findViewById(R.id.webview);
        
        WebSettings settings = mWebView.getSettings();
		
        settings.setRenderPriority(RenderPriority.HIGH);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		settings.setJavaScriptEnabled(true);

		//settings.setBuiltInZoomControls(false);
		//settings.setLoadWithOverviewMode(false);
		//settings.setUseWideViewPort(false);
		//settings.setSupportMultipleWindows(false);
		
		mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		//mWebView.setVerticalScrollBarEnabled(false);        
		
		mWebView.setWebViewClient(new WebViewHandler());		
		
	}
	
	@Override
	public void onBackPressed() 
	{
		// if web view can go back, go back
		if (mWebView.canGoBack())
			mWebView.goBack();
		else
			super.onBackPressed();
	}
	
	private class AssetsExtracter extends AsyncTask<Integer, Integer, Boolean>
	{

		@Override
		protected void onPreExecute() 
		{
			mProgress.setVisibility(View.VISIBLE);
		}
		
		@Override
		protected Boolean doInBackground(Integer... params) 
		{
			try 
			{
				AssetsManager.extractAllAssets(getApplicationContext(), true);
			} 
			catch (IOException e) 
			{
				MetaioDebug.printStackTrace(Log.ERROR, e);
				return false;
			}
			
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) 
		{
			mProgress.setVisibility(View.GONE);
			
			if (result)
			{
				mWebView.loadUrl("file:///android_asset/www/index.html");
			}
			else
			{
				MetaioDebug.log(Log.ERROR, "Error extracting assets, closing the application...");
				finish();
			}
	    }
		
	}
	
	
	private class WebViewHandler extends WebViewClient
	{
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) 
		{
			mProgress.setVisibility(View.VISIBLE);
		}
		
		@Override
		public void onPageFinished(WebView view, String url) 
		{
			mProgress.setVisibility(View.GONE);
		}
		
	    @Override
	    public boolean shouldOverrideUrlLoading(WebView view, String url) 
	    {
	    	String tutorialId = url.substring(url.lastIndexOf("=") + 1);
	    	MetaioDebug.log("Tutorial Id detected: "+tutorialId);
	    	if (url.startsWith("metaioSDKExample://"))
	    	{
	    		if (tutorialId != null)
	    		{
	    			MetaioDebug.log("Native code tutorial to be loaded #"+tutorialId);
		    		if (tutorialId.equals("1"))
		    		{
		    			Intent intent = new Intent(getApplicationContext(), Tutorial1.class);
		    			startActivity(intent);
		    		}
		    		else if (tutorialId.equals("2"))
		    		{
		    			Intent intent = new Intent(getApplicationContext(), Tutorial2.class);
		    			startActivity(intent);
		    		}
		    		else if (tutorialId.equals("3"))
		    		{
		    			Intent intent = new Intent(getApplicationContext(), Tutorial3.class);
		    			startActivity(intent);
		    		}
		    		else if (tutorialId.equals("4"))
		    		{
		    			Intent intent = new Intent(getApplicationContext(), Tutorial4.class);
		    			intent.putExtra(getPackageName()+".SCREEN_ORIENTATION", getResources().getConfiguration().orientation);
		    			startActivity(intent);
		    		}
		    		else if (tutorialId.equals("5"))
		    		{
		    			Intent intent = new Intent(getApplicationContext(), Tutorial5.class);
		    			startActivity(intent);
		    		}
		    		else if (tutorialId.equals("6"))
		    		{
		    			Intent intent = new Intent(getApplicationContext(), Tutorial6.class);
		    			startActivity(intent);
		    		}
		    		else if (tutorialId.equals("7"))
		    		{
		    			Intent intent = new Intent(getApplicationContext(), Tutorial7.class);
		    			startActivity(intent);
		    		}
	    		}
	    		return true;
	    	}
	    	else if (url.startsWith("metaioSDKExampleAREL://"))
	    	{
	    		if (tutorialId != null)
	    		{
	    			String arelConfigFile = "arelConfig"+tutorialId+".xml";
	    			String arelConfigFilePath = AssetsManager.getAssetPath("Assets"+tutorialId+"/"+arelConfigFile);
	    			MetaioDebug.log("arelConfig to be passed to intent: "+arelConfigFilePath);
	    			Intent intent = new Intent(getApplicationContext(), ARELViewActivity.class);
	    			intent.putExtra("arelConfigFile", arelConfigFilePath);
	    			startActivity(intent);
	    			
	    		}
	    		return true;
	    	}
	    	
	    	return false;
	    }
	}
}

