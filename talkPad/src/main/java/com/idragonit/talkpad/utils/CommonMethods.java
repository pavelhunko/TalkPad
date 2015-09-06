package com.idragonit.talkpad.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Patterns;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.idragonit.talkpad.AppConstants;
import com.idragonit.talkpad.R;
import com.idragonit.talkpad.editor.Settings;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CommonMethods {

	static int counter = 0;
    public static final String DISPLAY_MESSAGE_ACTION = "com.crubysoft.click.DISPLAY_MESSAGE";
    public static final String EXTRA_MESSAGE = "message";
    public static final String TAG = "Click GCM"; //Tag used on log messages.
    
    public static void displayMessage(Context context, String message) {
        Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
        intent.putExtra(EXTRA_MESSAGE, message);
        context.sendBroadcast(intent);
    }
    
    /*	end push notification	*/
    public static int getWindowHeight(Activity act) {
		DisplayMetrics metrics = new DisplayMetrics();
		act.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		return metrics.heightPixels;
	}
    
   	public static Point getDefaultDisplayMeasurements(Activity context) {
   		Point size = new Point();

   		WindowManager w = (WindowManager) context
   				.getSystemService(Context.WINDOW_SERVICE);

   		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//   			w.getDefaultDisplay().getSize(size);
   		} else {
   			Display d = w.getDefaultDisplay();
   			size.x = d.getWidth();
   			size.y = d.getHeight();
   		}

   		return size;
   	}
    
	public static double getDistance(double lat1, double lng1, double lat2, double lng2) {
		Location locationA = new Location("point A");
		locationA.setLatitude(lat1);
		locationA.setLongitude(lng1);
		
		Location locationB = new Location("point B");
		locationB.setLatitude(lat2);
		locationB.setLongitude(lng2);
		
		double distance = locationA.distanceTo(locationB);
		return distance;
	}
    
	public static boolean isLogin = false;
	static InputStream is = null;
	static JSONObject jObj = null;
	static String json = "";
	static JSONArray jArray = null;

	// constructor
	public CommonMethods() {
	}

	public static String LOGIN_SERVICE_NAME = "none";
	public static String USER_ACCESS_TOKEN, USER_PROFILE_URL,
			USER_SOCIALAUTH_ID, USER_ACCESS_SECRET;
	public static long USER_ACCESS_EXPIRES;

	
	public static String getLocaleTime(String time) {
		SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		simpledateformat.setTimeZone(TimeZone.getTimeZone("UTC"));
		try {
			Date date = simpledateformat.parse(time);
			simpledateformat.setTimeZone(TimeZone.getDefault());
			return simpledateformat.format(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return time;
	}
	
	public static String getCurrentTime() {
		SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		simpledateformat.setTimeZone(TimeZone.getTimeZone("UTC"));
		return simpledateformat.format(new Date());	
	}
	public static void hideKeyboard(Activity act, View view){
		InputMethodManager inputMethodManager = (InputMethodManager)act.getSystemService(Activity.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}
	
	public static boolean imageReduceAndCompressJPG(String imagePath, boolean isCamera) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(imagePath, options);
		int width = options.outWidth;
		int height = options.outHeight;
		
		Bitmap picture;
		
		// reduce size
		if (width > 320) {
			BitmapFactory.Options bmpOptions = new BitmapFactory.Options();
			bmpOptions.inSampleSize = width / 320 + 1;
			picture = BitmapFactory.decodeFile(imagePath, bmpOptions);
		} else {
			picture = BitmapFactory.decodeFile(imagePath);
		}
		
		// make square size
		boolean cameraRotated = false;
		width = picture.getWidth();
		height=  picture.getHeight();
		int left, top;
		if (width > height) {
			
			if (isCamera)
				cameraRotated = true;
			
			left = (width - height) / 2;
			top = 0;
			width = height;
		} else {
			left = 0;
			top = (height - width) / 2; 
			height = width;
		}
		picture = Bitmap.createBitmap(picture, left, top, width, height);
		
		try {
		    FileOutputStream bmpFile = new FileOutputStream(imagePath);
		    picture.compress(Bitmap.CompressFormat.JPEG, 60, bmpFile);
		    bmpFile.flush();
		    bmpFile.close();
		    picture.recycle();
		    picture = null;
		} catch (Exception e) {
			picture.recycle();
			picture = null;
			
			
			return false;
		}
		
		return true;
		
	}
	
	public static File getTempFilePath(Context context, String str_profile) {
		// it will return /sdcard/image.tmp
		final File path = new File(Environment.getExternalStorageDirectory(),
				context.getPackageName());
		if (!path.exists()) {
			path.mkdir();
		}

		return new File(path, str_profile);
	}

	public static Bitmap decodeSampledBitmapFromFile(String path, int reqWidth,
			int reqHeight) { // BEST QUALITY MATCH

		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		final int height = options.outHeight;
		final int width = options.outWidth;
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		int inSampleSize = 1;

		if (height > reqHeight) {
			inSampleSize = Math.round((float) height / (float) reqHeight);
		}

		int expectedWidth = width / inSampleSize;

		if (expectedWidth > reqWidth) {
			inSampleSize = Math.round((float) width / (float) reqWidth);
		}

		options.inSampleSize = inSampleSize;
		options.inJustDecodeBounds = false;

		return BitmapFactory.decodeFile(path, options);
	}
	
	public static void showMessage(Activity activity, String message) {

		AlertDialog.Builder builder = new AlertDialog.Builder(activity);

		builder.setTitle(activity.getString(R.string.app_name))
				.setIcon(R.drawable.ic_launcher).setMessage(message)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int arg1) {
						dialog.cancel();
					}
				});

		builder.create().show();
	}
	
	public static void showConnectionError(final Activity curAct, final Class<?> tarAct, String message){
		message = "Connection has some problem.";
		AlertDialog.Builder builder = new AlertDialog.Builder(curAct);
		builder.setTitle(curAct.getString(R.string.app_name))
				.setIcon(R.drawable.ic_launcher).setMessage("Connection has some problem.")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int arg1) {
						dialog.cancel();
						curAct.startActivity(new Intent(curAct, tarAct));
						curAct.finish();
					}
				});

		builder.create().show();
	}

	public static void showMessage(Activity activity, String title, String message) {

		AlertDialog.Builder builder = new AlertDialog.Builder(activity);

		builder.setTitle(title)
				.setIcon(R.drawable.ic_launcher).setMessage(message)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int arg1) {
						dialog.cancel();
					}
				});

		builder.create().show();
	}

	public static void showCloseMessage(final Activity activity) {
		final AlertDialog.Builder alertDlgBuilder = new AlertDialog.Builder(
				activity)
				.setTitle("Click")
				.setIcon(R.drawable.ic_launcher)
				.setMessage(
						"Click requires internet connection to work")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						activity.finish();
						System.exit(0);
						android.os.Process.killProcess(android.os.Process
								.myPid());
					}
				}).setCancelable(false);
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				AlertDialog alertDlg = alertDlgBuilder.create();
				alertDlg.getWindow().setType(
						WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
				alertDlg.show();
			}
		});
	}

	public static Boolean checkFbInstalled(Context context) {
		PackageManager pm = context.getPackageManager();
		boolean flag = false;
		try {
			pm.getPackageInfo("com.facebook.katana",
					PackageManager.GET_ACTIVITIES);
			flag = true;
		} catch (NameNotFoundException e) {
			flag = false;
		}
		return flag;
	}

	public boolean checkNetwork(Context context) {
		boolean wifiAvailable = false;
		boolean mobileAvailable = false;
		ConnectivityManager conManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo[] networkInfo = conManager.getAllNetworkInfo();
		for (NetworkInfo netInfo : networkInfo) {
			if (netInfo.getTypeName().equalsIgnoreCase("WIFI"))
				if (netInfo.isConnected())
					wifiAvailable = true;
			if (netInfo.getTypeName().equalsIgnoreCase("MOBILE"))
				if (netInfo.isConnected())
					mobileAvailable = true;
		}
		return wifiAvailable || mobileAvailable;
	}

	public static JSONObject getJSONFromUrl(String url, JSONObject obj) {
		try {
			// defaultHttpClient
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(url);

			// request data
			if (obj != null) {
				StringEntity entity = null;
				try {
					entity = new StringEntity(obj.toString(), "UTF-8");
					entity.setContentType("application/json");
					httpPost.setEntity(entity);
				} catch (Exception e) {
					Log.e(TAG, "Requset Failed!");
					return null;
				}
			}

			HttpResponse httpResponse = httpClient.execute(httpPost);
			HttpEntity httpEntity = httpResponse.getEntity();
			is = httpEntity.getContent();

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, "utf-8"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			json = sb.toString();
		} catch (Exception e) {
			Log.e("Buffer Error", "Error converting result " + e.toString());
		}

		// try parse the string to a JSON object
		try {
			jObj = new JSONObject(json);
		} catch (JSONException e) {
			Log.e("JSON Parser", "Error parsing data " + e.toString());
		}

		// return JSON String
		return jObj;
	}

	public static JSONArray getJSONWithoutArrayNameFromUrl(String url) {

		// Making HTTP request
		try {
			// defaultHttpClient
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(url);

			HttpResponse httpResponse = httpClient.execute(httpPost);
			HttpEntity httpEntity = httpResponse.getEntity();
			is = httpEntity.getContent();

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, "iso-8859-1"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
				Log.e("post", "qqqqqqqqqqq" + sb.toString());
			}
			is.close();
			json = sb.toString();
		} catch (Exception e) {
			Log.e("Buffer Error", "Error converting result " + e.toString());
		}

		// try parse the string to a JSON object
		try {
			jArray = new JSONArray(json);
		} catch (JSONException e) {
			Log.e("JSON Parser", "Error parsing data " + e.toString());
		}

		// return JSON String
		return jArray;
	}

	public static Bitmap loadBitmap(String url) {
		Bitmap Icon = null;
		// Drawable drawable = null;

		try {

			URL myUrl = new URL(url.replace(" ", "%20")); //

			Log.d("url", "url: " + counter++ + " " + myUrl);

			InputStream inputStream = (InputStream) myUrl.getContent();
			// drawable = Drawable.createFromStream(inputStream, null);

			// InputStream in = new URL(url).openStream();
			Icon = BitmapFactory.decodeStream(inputStream);
		} catch (Exception e) {
			Log.e("Error", e.getMessage());
			e.printStackTrace();
		}
		return Icon;
	}

	public static InputStream getJSONFromUrl(String url) {
		try {
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet httpPost = new HttpGet(url);
			HttpResponse httpResponse = httpClient.execute(httpPost);
			HttpEntity httpEntity = httpResponse.getEntity();
			InputStream is = httpEntity.getContent();
			return is;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static boolean isInternetAvailable(Context context) {
		ConnectivityManager mgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = mgr.getActiveNetworkInfo();

		return (netInfo != null && netInfo.isConnected() && netInfo
				.isAvailable());
	}

	public static void showFBLoginFailedDialog(Activity activity) {

		AlertDialog.Builder builder = new AlertDialog.Builder(activity);

		builder.setTitle("Hit The Town")
				.setMessage("Facebook login failed. Please try again.")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int arg1) {
						dialog.cancel();
					}
				});

		builder.create().show();
	}

	// Fetches distance between two locations
	public static float getDistanceBetween(Location loc, String latitude,
			String longitude) {

		float[] resultDistance = new float[3];

		Location.distanceBetween(loc.getLatitude(), loc.getLongitude(),
				Double.parseDouble(latitude), Double.parseDouble(longitude),
				resultDistance);

		return resultDistance[0];

	}

	public static void showNoLocationFoundDialog(Activity activity) {

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				activity);

		// set dialog message
		alertDialogBuilder
				.setMessage("Please try again, we couldn't find you location.")
				.setCancelable(false)
				.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		alertDialog.show();
	}

	// Load prescaled bitmap
	public static Bitmap loadPrescaledBitmap(byte[] image) throws IOException {

		// Facebook image size
		int IMAGE_MAX_SIZE = 150;

		// if (inSample != 0)
		// IMAGE_MAX_SIZE = inSample;

		// FileInputStream fis;

		BitmapFactory.Options opts;
		Bitmap bmp;

		opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		/*
		 * fis = new FileInputStream(file); BitmapFactory.decodeStream(fis,
		 * null, opts); fis.close();
		 */
		BitmapFactory.decodeByteArray(image, 0, image.length, opts);

		// Find the correct scale value. It should be a power of 2
		int resizeScale = 1;

		if (opts.outHeight > IMAGE_MAX_SIZE || opts.outWidth > IMAGE_MAX_SIZE) {
			resizeScale = (int) Math.pow(
					2,
					(int) Math.round(Math.log(IMAGE_MAX_SIZE
							/ (double) Math.max(opts.outHeight, opts.outWidth))
							/ Math.log(0.5)));
		}

		opts = new BitmapFactory.Options();
		opts.inSampleSize = resizeScale;

		bmp = BitmapFactory.decodeByteArray(image, 0, image.length, opts);

		return bmp;
	}

	public static void CopyStream(InputStream is, OutputStream os) {
		final int buffer_size = 1024;
		try {
			byte[] bytes = new byte[buffer_size];
			for (;;) {
				int count = is.read(bytes, 0, buffer_size);
				if (count == -1)
					break;
				os.write(bytes, 0, count);
			}
		} catch (Exception ex) {
		}
	}

	public static Bitmap checkIfSoundIconExists(String urlName, Context context) {
		String currentPath = "";
		PackageManager m = context.getPackageManager();
		String s = context.getPackageName();
		try {
			PackageInfo p = m.getPackageInfo(s, 0);
			currentPath = p.applicationInfo.dataDir;
			currentPath = currentPath + "/SoundStation";
		} catch (NameNotFoundException e) {
			Log.w("yourtag", "Error Package name not found ", e);
		}

		File SDCardRoot = new File(currentPath);
		if (!SDCardRoot.exists()) {
			SDCardRoot.mkdir();
			return null;
		}
		File imageFile = new File(currentPath + "/" + urlName + ".jpg");

		if (imageFile.exists()) {
			return BitmapFactory.decodeFile(imageFile.getAbsolutePath());
		}
		return null;
	}

	public static boolean saveBitmapTosdCard(Bitmap bitmap, String url,
			Context context) {
		try {

			String currentPath = "";
			PackageManager m = context.getPackageManager();
			String s = context.getPackageName();
			try {
				PackageInfo p = m.getPackageInfo(s, 0);
				currentPath = p.applicationInfo.dataDir;
				currentPath = currentPath + "/SoundStation/";
			} catch (NameNotFoundException e) {
				Log.w("yourtag", "Error Package name not found ", e);
			}

			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

			File f = new File(currentPath, url + ".jpg");

			f.createNewFile();

			FileOutputStream fo = new FileOutputStream(f);
			fo.write(bytes.toByteArray());
			fo.close();

			return true;
		} catch (Exception e) {
			return false;
		}
	}

	// Set ListView within a ScrollView
	public static void setListViewHeightBasedOnChildren(ListView listView) {
		try {
			ListAdapter listAdapter = listView.getAdapter();
			if (listAdapter == null) {
				// pre-condition
				return;
			}

			int totalHeight = 0;
			for (int i = 0; i < listAdapter.getCount(); i++) {
				View listItem = listAdapter.getView(i, null, listView);
				if (listItem instanceof ViewGroup)
					listItem.setLayoutParams(new LayoutParams(
							LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT));

				listItem.measure(0, 0);
				totalHeight += listItem.getMeasuredHeight();
			}

			LayoutParams params = listView.getLayoutParams();
			params.height = totalHeight
					+ (listView.getDividerHeight() * (listAdapter.getCount() - 1));
			listView.setLayoutParams(params);
			listView.requestLayout();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void scaleImage(ImageView imgView) {
		// Get the ImageView and its bitmap
		Drawable drawing = imgView.getDrawable();
		if (drawing == null) {
			return; // Checking for null & return, as suggested in comments
		}

		Bitmap bitmap = ((BitmapDrawable) drawing).getBitmap();

		// Get current dimensions AND the desired bounding box
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		int bounding = dpToPx(250, imgView.getContext());

		// Determine how much to scale: the dimension requiring less scaling is
		// closer to the its side. This way the image always stays inside your
		// bounding box AND either x/y axis touches it.
		float xScale = ((float) bounding) / width;
		float yScale = ((float) bounding) / height;
		float scale = (xScale <= yScale) ? xScale : yScale;

		// Create a matrix for the scaling and add the scaling data
		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale);

		// Create a new bitmap and convert it to a format understood by the
		// ImageView
		Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height,
				matrix, true);
		width = scaledBitmap.getWidth(); // re-use
		height = scaledBitmap.getHeight(); // re-use
		BitmapDrawable result = new BitmapDrawable(scaledBitmap);

		// Apply the scaled bitmap
		imgView.setImageDrawable(result);

		// Now change ImageView's dimensions to match the scaled image
		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) imgView
				.getLayoutParams();
		params.width = width;
		params.height = height;
		imgView.setLayoutParams(params);
	}

	private int dpToPx(int dp, Context context) {
		float density = context.getResources().getDisplayMetrics().density;
		return Math.round((float) dp * density);
	}

	// ********************SCALED_IMAGE_FROM_SERVER***********************//

	public static Bitmap getScaledBitmapFromUrl(String imageUrl,
			int requiredWidth, int requiredHeight) {
		Bitmap bm = null;
		URL url;
		try {
			url = new URL(imageUrl);

			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(url.openConnection().getInputStream(),
					null, options);
			if (requiredWidth > 0)
				options.inSampleSize = calculateInSampleSize(options,
						requiredWidth, requiredHeight);
			else
				options.inSampleSize = 1;

			options.inJustDecodeBounds = false;
			bm = BitmapFactory.decodeStream(url.openConnection()
					.getInputStream(), null, options);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return bm;
	}

	private static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			if (width > height) {
				inSampleSize = Math.round((float) height / (float) reqHeight);
			} else {
				inSampleSize = Math.round((float) width / (float) reqWidth);
			}
		}
		return inSampleSize;
	}

	public static boolean isSpecialChar(String str){
		Pattern p = Pattern.compile("[!*'\"();:@&=+$,/?%#%]"); 
		Matcher m = p.matcher(str);
		return m.find();
	}

	public static boolean isNumeric(String s) {
		try {
			Integer.parseInt(s);
		} catch (NumberFormatException numberformatexception) {
			return false;
		}
		return true;
	}

	public static boolean isSet(String s) {
		return s != null && s.trim().length() > 0;
	}

	public static boolean isValidEmail(String s) {
		return Patterns.EMAIL_ADDRESS.matcher(s).matches();
	}
	
	public static void setupUI(final Activity act, View view) {
		if (!(view instanceof EditText)) {
			view.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					hideSoftKeyboard(act, v);
					return false;
				}
			});
		}

		if (view instanceof ViewGroup) {
			for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
				View innerView = ((ViewGroup) view).getChildAt(i);
				setupUI(act, innerView);
			}
		}
	}

	public static void hideSoftKeyboard(Activity act, View v) {
		InputMethodManager imm = (InputMethodManager) act.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}
	
	public static String getRealPathFromURI(Uri uri, Activity act) {
		String filePath;
		if (uri != null && "content".equals(uri.getScheme())) {
			Cursor cursor = act.getContentResolver().query(uri, new String[] { android.provider.MediaStore.Images.ImageColumns.DATA },
							null, null, null);
			cursor.moveToFirst();
			filePath = cursor.getString(0);
			cursor.close();
		} else {
			filePath = uri.getPath();
		}
		return (filePath);
	}
	
	public Bitmap circleImage(Bitmap bitmap) {
    	int targetWidth = 1200;
        int targetHeight = 1200;
        Bitmap targetBitmap = Bitmap.createBitmap(targetWidth, 
                            targetHeight,Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(targetBitmap);
        Path path = new Path();
        path.addCircle(((float) targetWidth - 1) / 2,
            ((float) targetHeight - 1) / 2,
            (Math.min(((float) targetWidth), 
            ((float) targetHeight)) / 2),
            Path.Direction.CCW);

        canvas.clipPath(path);
        Bitmap sourceBitmap = bitmap;
        int x = 0, y = 0;
        int width = sourceBitmap.getWidth();
        int height = sourceBitmap.getHeight();
        if (width > height) {
        	x = (width - height) / 2;
        	width = height;
        } else {
        	y = (height- width) / 2;
        	height = width;
        }
        canvas.drawBitmap(sourceBitmap, 
            new Rect(x, y, x + width, y + height), 
            new Rect(0, 0, targetWidth, targetHeight), null);
        return targetBitmap;
    }
	
	public static Bitmap squareImage(String filePath){
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 8;
		Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
		
		boolean cameraRotated = false;
		
		int targetWidth = 1200;
        int targetHeight = 1200;
        Bitmap targetBitmap = Bitmap.createBitmap(targetWidth, targetHeight,Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(targetBitmap);
        Bitmap sourceBitmap = bitmap;
        int x = 0, y = 0;
        int width = sourceBitmap.getWidth();
        int height = sourceBitmap.getHeight();
        if (width > height) {
        	cameraRotated = true;
        	x = (width - height) / 2;
        	width = height;
        } else {
        	y = (height- width) / 2;
        	height = width;
        }
        
        if (cameraRotated)  {
			Matrix mtx = new Matrix();
			mtx.postRotate(90);
			sourceBitmap = Bitmap.createBitmap(sourceBitmap, x, y, x + width, y + height, mtx, true);
		} else
			sourceBitmap = Bitmap.createBitmap(sourceBitmap, x, y, x + width, y + height);
 
        canvas.drawBitmap(sourceBitmap, new Rect(x, y, x + width, y + height), new Rect(0, 0, targetWidth, targetHeight), null);
        
        try {
		    FileOutputStream bmpFile = new FileOutputStream(filePath);
		    targetBitmap.compress(Bitmap.CompressFormat.JPEG, 60, bmpFile);
		    bmpFile.flush();
		    bmpFile.close();
		    targetBitmap.recycle();
		    targetBitmap = null;
		} catch (Exception e) {
			targetBitmap.recycle();
			targetBitmap = null;
		}
        return targetBitmap;
	}
	
	private static Pattern dateFrmtPtrn = Pattern.compile("((19|20)\\d\\d)-(0?[1-9]|1[012])-(0?[1-9]|[12][0-9]|3[01]) (0?[0-9]|1[0-9]|2[0-3]):(0?[0-9]|[1-5][0-9]|6[0]):(0?[0-9]|[1-5][0-9]|6[0])");
	     
    public static boolean validateDateFormat(String userName){
        Matcher mtch = dateFrmtPtrn.matcher(userName);
        if(mtch.matches()){
            return true;
        }
        return false;
    }
    
    private static Pattern timeFrmtPtrn = Pattern.compile("(0?[0-9]|1[0-9]|2[0-3]):(0?[0-9]|[1-5][0-9]|6[0]):(0?[0-9]|[1-5][0-9]|6[0])");
   
    public static boolean validateTimeFormat(String time){
    	Matcher match = timeFrmtPtrn.matcher(time);
    	if(match.matches())
    		return true;
    	return false;
    }
    
    public static boolean writeFile(String path, String content){
    	try{
    		FileOutputStream fos = new FileOutputStream(path);
    		fos.write(content.getBytes("utf-8"));
    		fos.flush();
    		fos.close();
    		
    		return true;
    	}catch (Exception e){
    		e.printStackTrace();
    	}
    	
    	return false;
    }
    
    public static String readFile(String path){
    	
    	Log.v("ReadFile", path);
    	
    	try{
            StringBuilder sb = new StringBuilder();
            
            try {
                BufferedReader br = new BufferedReader(new FileReader(path));

                while (true) {

                    String line = br.readLine();

                    if (line == null) {
                        break;
                    }

                    sb.append(line);
                }
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            return sb.toString();
    	}catch (Exception e){}
    	
    	return "";
    }
    
}