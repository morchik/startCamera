package com.vmordo.camera;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class CameraActivity extends Activity implements Camera.PictureCallback {
	public static long cnt = 0;
	Paint paint;

	File directory;
	SurfaceView sv, sv2;
	SurfaceHolder holder;
	HolderCallback holderCallback;
	Camera camera;

	final int CAMERA_ID = 0;
	final boolean FULL_SCREEN = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		paint = new Paint();
		paint.setTextSize(32);
		paint.setColor(Color.BLUE);
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_camera);

		sv2 = new DrawView(this);
		LinearLayout ll = (LinearLayout) findViewById(R.id.linearLayout1);
		LinearLayout.LayoutParams labelLayoutParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
	    sv2.setLayoutParams(labelLayoutParams);
		ll.addView(sv2);

		Log.e("CameraActivity", sv2 + "getHeight=" + sv2.getHeight());
		Log.e("CameraActivity", "getWidth=" + sv2.getWidth());
		sv = (SurfaceView) findViewById(R.id.surfaceView);
		holder = sv.getHolder();
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
		holderCallback = new HolderCallback();
		holder.addCallback(holderCallback);
		createDirectory();
	}

	@Override
	protected void onResume() {
		super.onResume();
		camera = Camera.open(CAMERA_ID);
		setPreviewSize(FULL_SCREEN);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (camera != null)
			camera.release();
		camera = null;
	}

	public void setPic() {
		Camera.Parameters param;
		param = camera.getParameters();

		Camera.Size bestSize = null;
		List<Camera.Size> sizeList = camera.getParameters()
				.getSupportedPictureSizes();
		bestSize = sizeList.get(0);
		for (int i = 1; i < sizeList.size(); i++) {
			if ((sizeList.get(i).width * sizeList.get(i).height) > (bestSize.width * bestSize.height)) {
				bestSize = sizeList.get(i);
			}
		}
		/*
		 * List<Integer> supportedPreviewFormats = param
		 * .getSupportedPreviewFormats(); Iterator<Integer>
		 * supportedPreviewFormatsIterator = supportedPreviewFormats
		 * .iterator(); while (supportedPreviewFormatsIterator.hasNext()) {
		 * Integer previewFormat = supportedPreviewFormatsIterator.next(); if
		 * (previewFormat == ImageFormat.YV12) {
		 * param.setPreviewFormat(previewFormat); } }
		 * 
		 * param.setPreviewSize(bestSize.width, bestSize.height);
		 */
		param.setPictureSize(bestSize.width, bestSize.height);

		camera.setParameters(param);
	}

	public void onClick(View v) {
		setPic();
		camera.takePicture(null, null, this);
	}

	class HolderCallback implements SurfaceHolder.Callback {

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			try {
				camera.setPreviewDisplay(holder);
				camera.startPreview();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			camera.stopPreview();
			setCameraDisplayOrientation(CAMERA_ID);
			try {
				camera.setPreviewDisplay(holder);
				camera.startPreview();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {

		}

	}

	void setPreviewSize(boolean fullScreen) {

		// получаем размеры экрана
		Display display = getWindowManager().getDefaultDisplay();
		boolean widthIsMax = display.getWidth() > display.getHeight();

		// определяем размеры превью камеры
		Size size = camera.getParameters().getPreviewSize();

		RectF rectDisplay = new RectF();
		RectF rectPreview = new RectF();

		// RectF экрана, соотвествует размерам экрана
		rectDisplay.set(0, 0, display.getWidth(), display.getHeight());

		// RectF первью
		if (widthIsMax) {
			// превью в горизонтальной ориентации
			rectPreview.set(0, 0, size.width, size.height);
		} else {
			// превью в вертикальной ориентации
			rectPreview.set(0, 0, size.height, size.width);
		}

		Matrix matrix = new Matrix();
		// подготовка матрицы преобразования
		if (!fullScreen) {
			// если превью будет "втиснут" в экран (второй вариант из урока)
			matrix.setRectToRect(rectPreview, rectDisplay,
					Matrix.ScaleToFit.START);
		} else {
			// если экран будет "втиснут" в превью (третий вариант из урока)
			matrix.setRectToRect(rectDisplay, rectPreview,
					Matrix.ScaleToFit.START);
			matrix.invert(matrix);
		}
		// преобразование
		matrix.mapRect(rectPreview);

		// установка размеров surface из получившегося преобразования
		sv.getLayoutParams().height = (int) (rectPreview.bottom);
		sv.getLayoutParams().width = (int) (rectPreview.right);
	}

	void setCameraDisplayOrientation(int cameraId) {
		// определяем насколько повернут экран от нормального положения
		int rotation = getWindowManager().getDefaultDisplay().getRotation();
		int degrees = 0;
		switch (rotation) {
		case Surface.ROTATION_0:
			degrees = 0;
			break;
		case Surface.ROTATION_90:
			degrees = 90;
			break;
		case Surface.ROTATION_180:
			degrees = 180;
			break;
		case Surface.ROTATION_270:
			degrees = 270;
			break;
		}

		int result = 0;

		// получаем инфо по камере cameraId
		CameraInfo info = new CameraInfo();
		Camera.getCameraInfo(cameraId, info);

		// задняя камера
		if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
			result = ((360 - degrees) + info.orientation);
		} else
		// передняя камера
		if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
			result = ((360 - degrees) - info.orientation);
			result += 360;
		}
		result = result % 360;
		camera.setDisplayOrientation(result);
	}

	@Override
	public void onPictureTaken(byte[] paramArrayOfByte, Camera paramCamera) {
		try {
			String fln = String.format(directory.getPath() + "/p%d.jpg",
					System.currentTimeMillis());
			FileOutputStream os = new FileOutputStream(fln);
			os.write(paramArrayOfByte);
			os.close();
			Toast.makeText(this, paramArrayOfByte.length + " " + fln,
					Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			Log.e("onPictureTaken", e.getMessage());
			Toast.makeText(this, "Error " + e.getMessage(), Toast.LENGTH_LONG)
					.show();
		}
		paramCamera.startPreview();
	}

	private void createDirectory() {
		directory = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				"MyFolder");
		if (!directory.exists())
			directory.mkdirs();
	}

	class DrawView extends SurfaceView implements SurfaceHolder.Callback {

		private DrawThread drawThread;

		public DrawView(Context context) {
			super(context);
			this.setBackgroundColor(Color.TRANSPARENT);
			this.setZOrderOnTop(true); //necessary                
		    getHolder().setFormat(PixelFormat.TRANSPARENT); 

		    getHolder().addCallback(this);
			getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
/*
		@Override
		protected void onDraw(Canvas canvas) {
			Log.e("CameraActivity", "onDraw(Canvas canvas)=" + canvas);
			super.onDraw(canvas);
			canvas.drawCircle(50, 60, 3, paint);
		}
*/
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			Log.e("DrawView", "surfaceChanged "+cnt);
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			Log.e("DrawView", "surfaceCreated "+cnt);
			drawThread = new DrawThread(getHolder());
			drawThread.setRunning(true);
			drawThread.start();
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			Log.e("DrawView", drawThread+" surfaceDestroyed "+cnt);
			boolean retry = true;
			drawThread.setRunning(false);
			while (retry) {
				try {
					drawThread.join();
					retry = false;
				} catch (InterruptedException e) {
				}
			}
		}

		class DrawThread extends Thread {
			private boolean running = false;
			private SurfaceHolder surfaceHolderT;

			public DrawThread(SurfaceHolder surfaceHolder) {
				this.surfaceHolderT = surfaceHolder;
			}

			public void setRunning(boolean running) {
				this.running = running;
			}

			@Override
			public void run() {
				Canvas canvas;
				while (running) {
					canvas = null;
					try {
						canvas = surfaceHolderT.lockCanvas(null);
						if (canvas == null) {
							continue;
						}
						//canvas.drawColor(Color.argb(0, 255, 255, 255));
						//canvas.
						paint.setColor(Color.RED);
						canvas.drawText("*" + cnt, 70, 70, paint);
						paint.setColor(Color.BLUE);
						canvas.drawCircle(sv.getWidth(), sv2.getHeight(), 10, paint);
						++cnt;
						if (cnt % 100 == 0) {
							Log.e("DrawView", this+" cnt=" + cnt);
							Log.e("CameraActivity",
									surfaceHolderT + "  getHeight=" + sv2.getHeight());
							Log.e("CameraActivity",
									canvas + " getWidth=" + sv2.getWidth());
							Log.e("CameraActivity",
									holder + " holder=" + cnt);
						}
					} finally {
						if (canvas != null) {
							surfaceHolderT.unlockCanvasAndPost(canvas);
							if (cnt % 200 == 0)
								Log.v("DrawView", "unlockCanvasAndPost cnt ="
										+ cnt);
						}
					}
				}
			}
		}

	}

}
