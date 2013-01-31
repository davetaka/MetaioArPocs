package com.metaio.example_custom_renderer;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.hardware.Camera.CameraInfo;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import com.metaio.sdk.SensorsComponentAndroid;
import com.metaio.sdk.jni.ERENDER_SYSTEM;
import com.metaio.sdk.jni.ESCREEN_ROTATION;
import com.metaio.sdk.jni.IMetaioSDKAndroid;
import com.metaio.sdk.jni.IMetaioSDKCallback;
import com.metaio.sdk.jni.ImageStruct;
import com.metaio.sdk.jni.MetaioSDK;
import com.metaio.sdk.jni.TrackingValues;
import com.metaio.tools.Screen;
import com.metaio.tools.SystemInfo;
import com.metaio.tools.io.AssetsManager;

public final class MainActivity extends Activity implements Renderer
{
	private static final String TAG = "MainActivity";

	static
	{
		IMetaioSDKAndroid.loadNativeLibs();
	}

	private CameraImageRenderer mCameraImageRenderer;

	private Cube mCube = new Cube();

	private IMetaioSDKAndroid mMetaioSDK;

	private ESCREEN_ROTATION mScreenRotation;

	private boolean mSDKReady = false;

	private SensorsComponentAndroid mSensors;

	private GLSurfaceView mSurface;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		mSurface = (GLSurfaceView) findViewById(R.id.gl_surface);
		mSurface.setEGLContextClientVersion(1);
		mSurface.setRenderer(this);
		mSurface.setKeepScreenOn(true);

		// Create metaio SDK instance
		mSensors = new SensorsComponentAndroid(getApplicationContext());
		final String signature = "E7wBfSqi5PYqSsmPaoOZYLIRIBQpmaP9h9eui9e9AXg=";
		mMetaioSDK = MetaioSDK.CreateMetaioSDKAndroid(this, signature);
		mMetaioSDK.registerSensorsComponent(mSensors);
		mMetaioSDK.registerCallback(new IMetaioSDKCallback()
		{
			@Override
			public void onNewCameraFrame(ImageStruct cameraFrame)
			{
				super.onNewCameraFrame(cameraFrame);

				if (mCameraImageRenderer != null)
					mCameraImageRenderer.updateFrame(cameraFrame);
			}

			@Override
			public void onSDKReady()
			{
				super.onSDKReady();

				mSDKReady = true;
			}
		});

		// Extract all the assets
		try
		{
			AssetsManager.extractAllAssets(getApplicationContext(), true);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		// Set up custom rendering (metaio SDK will only do tracking and not render any objects itself)
		mMetaioSDK.initializeRenderer(0, 0, Screen.getRotation(this), ERENDER_SYSTEM.ERENDER_SYSTEM_NULL);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		if (mMetaioSDK != null)
		{
			mMetaioSDK.delete();
			mMetaioSDK = null;
		}

		if (mSensors != null)
		{
			mSensors.release();
			mSensors.registerCallback(null);
			mSensors.delete();
			mSensors = null;
		}
	}

	@Override
	public void onDrawFrame(GL10 gl)
	{
		// Note: The metaio SDK itself does not render anything here because we initialized it with
		// the NULL renderer. This call is necessary to get the camera image and update tracking.
		mMetaioSDK.render();

		mMetaioSDK.requestCameraImage();

		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		gl.glDisable(GL10.GL_DEPTH_TEST);

		mCameraImageRenderer.draw(gl, mScreenRotation);

		gl.glEnable(GL10.GL_DEPTH_TEST);

		//
		// Render cube in front of camera image (if we're currently tracking)
		//

		TrackingValues trackingValues = mMetaioSDK.getTrackingValues(1);

		if (mSDKReady && trackingValues.getQuality() > 0)
		{
			float[] modelMatrix = new float[16];
			// preMultiplyWithStandardViewMatrix=false parameter explained below
			mMetaioSDK.getTrackingValues(1, modelMatrix, false, true);

			// With getTrackingValues(..., preMultiplyWithStandardViewMatrix=true), the metaio SDK
			// would calculate a model-view matrix, i.e. a standard look-at matrix (looking from the
			// origin along the negative Z axis) multiplied by the model matrix (tracking pose).
			// Here we use our own view matrix for demonstration purposes (parameter set to false),
			// for instance if you have your own camera implementation. Additionally, the cube is
			// scaled up by factor 40 and translated by 40 units in order to have its back face lie
			// on the tracked image.
			gl.glMatrixMode(GL10.GL_MODELVIEW);
			gl.glLoadIdentity();

			// Use typical view matrix (camera looking along negative Z axis, see previous hint)
			gl.glLoadIdentity();

			// The order is important here: We first want to scale the cube, then put it 40 units
			// higher (because it's rendered from -1 to +1 on all axes, after scaling that's +-40)
			// so that its back face lies on the tracked image and move it into place
			// (transformation to the coordinate system of the tracked image).
			gl.glMultMatrixf(modelMatrix, 0); // MODEL_VIEW = LOOK_AT * MODEL
			gl.glTranslatef(0, 0, 40);
			gl.glScalef(40, 40, 40); // all sides of the cube then have dimension 80

			gl.glMatrixMode(GL10.GL_PROJECTION);
			float[] projMatrix = new float[16];

			// Use right-handed projection matrix
			mMetaioSDK.getProjectionMatrix(projMatrix, true);

			// Since we render the camera image ourselves, and there are devices whose screen aspect
			// ratio does not match the camera aspect ratio, we have to make up for the stretched
			// and cropped camera image. The CameraImageRenderer class gives us values by which
			// pixels should be scaled from the middle of the screen (e.g. getScaleX() > 1 if the
			// camera image is wider than the screen and thus its width is displayed cropped).
			projMatrix[0] *= mCameraImageRenderer.getScaleX();
			projMatrix[5] *= mCameraImageRenderer.getScaleY();
			gl.glLoadMatrixf(projMatrix, 0);

			mCube.render(gl);
		}
	}

	@Override
	protected void onPause()
	{
		super.onPause();

		if (mSurface != null)
			mSurface.onPause();

		if (mMetaioSDK != null)
			mMetaioSDK.pause();
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		if (mSurface != null)
			mSurface.onResume();

		if (mMetaioSDK != null)
			mMetaioSDK.resume();
	}

	@Override
	protected void onStart()
	{
		super.onStart();

		if (mMetaioSDK != null)
		{
			setScreenRotation();

			// Start camera capturing, this will automatically add the preview to the current
			// activity. That means we do not need to render the camera frames manually.
			// The startCamera call will add a camera preview view to the view hierarchy.
			final int cameraIndex = SystemInfo.getCameraIndex(CameraInfo.CAMERA_FACING_BACK);
			mMetaioSDK.startCamera(cameraIndex, 320, 240);

			String trackingConfigFile = AssetsManager.getAssetPath("TrackingData_MarkerlessFast.xml");
			if (trackingConfigFile == null || !mMetaioSDK.setTrackingConfiguration(trackingConfigFile))
				Log.e(TAG, "Failed to set tracking configuration");
		}
	}

	@Override
	protected void onStop()
	{
		super.onStop();

		if (mMetaioSDK != null)
			mMetaioSDK.stopCamera();
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height)
	{
		if (height == 0)
			height = 1;

		gl.glViewport(0, 0, width, height);

		setScreenRotation();

		if (mMetaioSDK != null)
			mMetaioSDK.resizeRenderer(width, height);
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config)
	{
		if (mCameraImageRenderer == null)
			mCameraImageRenderer = new CameraImageRenderer(this, gl);

		gl.glShadeModel(GL10.GL_SMOOTH);
		gl.glClearColor(0, 0, 0, 0);

		gl.glClearDepthf(1.0f);
		gl.glDepthFunc(GL10.GL_LEQUAL);
		gl.glDisable(GL10.GL_LIGHTING);

		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
	}

	private void setScreenRotation()
	{
		mScreenRotation = Screen.getRotation(this);
		mMetaioSDK.setScreenRotation(mScreenRotation);
	}
}