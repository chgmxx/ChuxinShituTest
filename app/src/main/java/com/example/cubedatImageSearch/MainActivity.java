package com.example.cubedatImageSearch;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

//@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class MainActivity extends Activity implements OnClickListener,  SurfaceHolder.Callback{
	private static final String TAG = null;
	private static final String requestURL = "http://image.baidu.com/pictureup/uploadshitu?fr=flash&fm=index&pos=upload";
	private Button selectImage, uploadImage;
	private ImageView imageView;
	private TextView textView;

	Camera camera;
	SurfaceView surfaceView;
	SurfaceHolder surfaceHolder;
	Button buttonStartCameraPreview, buttonStopCameraPreview;
	boolean previewing = false;
	LinearLayout get_more;
	Camera.Size optimalSize;
	private String picPath = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		selectImage = (Button) this.findViewById(R.id.selectImage);
		uploadImage = (Button) this.findViewById(R.id.uploadImage);
		selectImage.setOnClickListener(this);
		uploadImage.setOnClickListener(this);
		textView = (TextView) findViewById(R.id.textView);
		imageView = (ImageView) this.findViewById(R.id.imageView);

		buttonStartCameraPreview = (Button) findViewById(R.id.startcamerapreview);
		buttonStopCameraPreview = (Button) findViewById(R.id.stopcamerapreview);
		surfaceView = (SurfaceView) findViewById(R.id.surfaceview);
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		buttonStartCameraPreview.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				if (!previewing)
				{
					camera = Camera.open();
					if (camera != null)
					{
						try
						{
							camera.setDisplayOrientation(90);
							Camera.Parameters parameters = camera.getParameters();
							List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
							Camera.Size optimalSize = getOptimalPreviewSize(sizes, getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels);
							parameters.setPreviewSize(optimalSize.width, optimalSize.height);
							camera.setParameters(parameters);
							camera.setPreviewDisplay(surfaceHolder);
							camera.startPreview();
							previewing = true;
						} catch (IOException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}

			}
		});
		buttonStopCameraPreview.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				if (camera != null && previewing)
				{
					camera.stopPreview();
					camera.release();
					camera = null;
					previewing = false;
				}

			}
		});

	}
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		// TODO Auto-generated method stub

	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
			case R.id.selectImage:
				/***
				 * 这个是调用android内置的intent，来过滤图片文件 ，同时也可以过滤其他的
				 */
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_PICK);
				startActivityForResult(intent, 1);
				break;
			case R.id.uploadImage:
				if (picPath == null) {
					Toast.makeText(MainActivity.this, "请选择图片！", 1000).show();
				}
				else{
					new CaptureTask().execute(picPath, requestURL);
				}
				break;
			default:
				break;
		}
	}
	class CaptureTask extends AsyncTask<String, String, String>{
		private ProgressDialog pd;
		@Override
		protected void onPreExecute() {
			pd= ProgressDialog.show(MainActivity.this, "处理", "正在处理…", false, true);
			pd.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
		}

		@Override
		protected String doInBackground(String... params)
		{
			publishProgress("正在连接...");
			String postResponseUrl = ShituUtils.postFile(params[0], params[1]);
			publishProgress("连接成功");
			publishProgress("正在识别...");
			String shituResult = null;
			shituResult = ShituUtils.resolvePostResponse(postResponseUrl);
			Log.i("AsyncTask", picPath);
			/*try
			{
				Thread.sleep(10);
			} catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
			return shituResult;
		}

		@Override
		protected void onProgressUpdate(String... params) {
			// 更新进度
			pd.setMessage(params[0]);
		}

		@Override
		protected void onPostExecute(String result)
		{
			super.onPostExecute(result);
			textView.setText(result);
			pd.dismiss();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			/**
			 * 当选择的图片不为空的话，在获取到图片的途径
			 */
			Uri uri = data.getData();
			try {
				String[] pojo = { MediaStore.Images.Media.DATA };

				Cursor cursor = managedQuery(uri, pojo, null, null, null);
				if (cursor != null) {
					ContentResolver cr = this.getContentResolver();
					int colunm_index = cursor
							.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
					cursor.moveToFirst();
					String path = cursor.getString(colunm_index);
					/***
					 * 这里加这样一个判断主要是为了第三方的软件选择，比如：使用第三方的文件管理器的话，你选择的文件就不一定是图片了，
					 * 这样的话，我们判断文件的后缀名 如果是图片格式的话，那么才可以
					 */
					if (path.endsWith("jpg") || path.endsWith("png")) {
						Log.e(TAG, "uri = " + path);
						picPath = path;
						Bitmap bitmap = BitmapFactory.decodeStream(cr
								.openInputStream(uri));
						imageView.setImageBitmap(bitmap);
					} else {
						alert();
					}
				} else {
					alert();
				}

			} catch (Exception e) {
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	private void alert() {
		Dialog dialog = new AlertDialog.Builder(this).setTitle("提示")
				.setMessage("您选择的不是有效的图片")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						picPath = null;
					}
				}).create();
		dialog.show();
	}
	public static Camera.Size getOptimalPreviewSize(List<Camera.Size> cameraPreviewSizes, int targetWidth, int targetHeight, boolean isActivityPortrait) {
		if (null == cameraPreviewSizes) {
			return null;
		}

		int optimalHeight = Integer.MIN_VALUE;
		int optimalWidth = Integer.MIN_VALUE;

		for (Camera.Size cameraPreviewSize : cameraPreviewSizes) {
			boolean isCameraPreviewHeightBigger = cameraPreviewSize.height > cameraPreviewSize.width;
			int actualCameraWidth = cameraPreviewSize.width;
			int actualCameraHeight = cameraPreviewSize.height;

			if (isActivityPortrait) {
				if (!isCameraPreviewHeightBigger) {
					int temp = cameraPreviewSize.width;
					actualCameraWidth = cameraPreviewSize.height;
					actualCameraHeight = temp;
				}
			} else {
				if (isCameraPreviewHeightBigger) {
					int temp = cameraPreviewSize.width;
					actualCameraWidth = cameraPreviewSize.height;
					actualCameraHeight = temp;
				}
			}

			if (actualCameraWidth > targetWidth || actualCameraHeight > targetHeight) {
				// finds only smaller preview sizes than target size
				continue;
			}

			if (actualCameraWidth > optimalWidth && actualCameraHeight > optimalHeight) {
				// finds only better sizes
				optimalWidth = actualCameraWidth;
				optimalHeight = actualCameraHeight;
			}
		}

		Size optimalSize = null;

		if (optimalHeight != Integer.MIN_VALUE && optimalWidth != Integer.MIN_VALUE) {
			optimalSize = new Size(optimalWidth, optimalHeight);
		}

		return optimalSize;
	}
}
