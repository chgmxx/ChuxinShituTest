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
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

//@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class MainActivity extends Activity implements OnClickListener,  SurfaceHolder.Callback{
	private static final String TAG = null;
	private static final String requestURL = "http://image.baidu.com/pictureup/uploadshitu?fr=flash&fm=index&pos=upload";
	private Button selectImage, uploadImage;
	private ImageView imageView;
	private TextView textView;
	private  Button queryPreviewPicture;

	private String strCaptureFilePath = Environment
			.getExternalStorageDirectory() + "/DCIM/Camera/";// 保存图像的路径
	private String strCaptureFileFullName ;
	Camera mCamera;
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
		queryPreviewPicture =   (Button) this.findViewById(R.id.query_preview_picture);
		queryPreviewPicture.setOnClickListener(this);
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
					mCamera = Camera.open();
					if (mCamera != null)
					{
						try
						{
							mCamera.setDisplayOrientation(90);
							Camera.Parameters parameters = mCamera.getParameters();
							List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
							Camera.Size optimalSize = getOptimalPreviewSize(sizes, getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels,false);
							parameters.setPreviewSize(1280, 720);
							Log.d(TAG,"preivew size width ="+sizes.get(0).width+",height="+sizes.get(0).height);
							mCamera.setParameters(parameters);
							mCamera.setPreviewDisplay(surfaceHolder);
							mCamera.startPreview();
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
				if (mCamera != null && previewing)
				{
					//takePicture();
					mCamera.stopPreview();
					mCamera.release();
					mCamera = null;
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
					Toast.makeText(MainActivity.this, "请选择图片！", Toast.LENGTH_SHORT).show();
				}
				else{
					new CaptureTask().execute(picPath, requestURL);
				}
				break;
			case R.id.query_preview_picture:
				takePicture();

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
			Log.d(TAG,"postResponseUrl:"+postResponseUrl);
			publishProgress("连接成功");
			publishProgress("正在识别...");
			String shituResult = null;
			shituResult = ShituUtils.resolvePostResponse(postResponseUrl);
			Log.i("AsyncTask", params[0]);

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
			//optimalSize = new Size(optimalWidth, optimalHeight);
		}

		return optimalSize;
	}

	/* 拍照的method */
	private void takePicture() {
		if (mCamera != null) {
			mCamera.takePicture(null, null, jpegCallback);
		}
	}
	//在takepicture中调用的回调方法之一，接收jpeg格式的图像
	private Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
		public void onPictureTaken(byte[] _data, Camera _camera) {

            /*
             * if (Environment.getExternalStorageState().equals(
             * Environment.MEDIA_MOUNTED)) // 判断SD卡是否存在，并且可以可以读写 {
             *
             * } else { Toast.makeText(EX07_16.this, "SD卡不存在或写保护",
             * Toast.LENGTH_LONG) .show(); }
             */
			// Log.w("============", _data[55] + "");

			try {
                /* 取得相片 */
				Bitmap bm = BitmapFactory.decodeByteArray(_data, 0,
						_data.length);

                /* 创建文件 */


				File myCaptureFile = new File(strCaptureFilePath, getCurrentTimeString()+".jpg");
				BufferedOutputStream bos = new BufferedOutputStream(
						new FileOutputStream(myCaptureFile));
				strCaptureFileFullName = myCaptureFile.getAbsolutePath();
                /* 采用压缩转档方法 */
				bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);

                /* 调用flush()方法，更新BufferStream */
				bos.flush();

                /* 结束OutputStream */
				bos.close();

                /* 让相片显示3秒后圳重设相机 */
				// Thread.sleep(2000);
                /* 重新设定Camera */
				if(!TextUtils.isEmpty(strCaptureFileFullName)) {
					new CaptureTask().execute(strCaptureFileFullName, requestURL);
				}
				if (mCamera != null && previewing)
				{
					mCamera.stopPreview();
					mCamera.release();
					mCamera = null;
					previewing = false;
				}

				//initCamera();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
	/* 自定义class AutoFocusCallback */
	public final class AutoFocusCallback implements
			android.hardware.Camera.AutoFocusCallback {
		public void onAutoFocus(boolean focused, Camera camera) {

            /* 对到焦点拍照 */
			if (focused) {
				takePicture();
			}
		}
	};

	private String getCurrentTimeString(){
		SimpleDateFormat formatter    =   new    SimpleDateFormat    ("yyyyMMdd_HHmmss");
		Date curDate    =   new    Date(System.currentTimeMillis());//获取当前时间
		String    str    =    formatter.format(curDate);
		return str;
	}
}
