package com.vmordo.camera;

import java.io.File;
import java.util.Calendar;

import android.content.Context;

// easy get to Context but first set it: Cnt.set(getApplicationContext());
public class Cnt {
	private static Context context = null;

	public static void set(Context inCnt) {
		if (inCnt != null)
			context = inCnt;
	}

	public static Context get() {
		if (context == null)
			android.util.Log.e("Cnt", "Error ! first we need set(Context)!!! ");
		return context;
	}

	static String fstr(int inum) {
		String cRes = inum + "";
		if (inum <= 9) {
			cRes = "0" + inum;
		}
		return cRes;
	}

	public static File getFileName() {
		// получаем текущее время
		final Calendar c = Calendar.getInstance();
		int mYear = c.get(Calendar.YEAR);
		String mMonth = fstr(c.get(Calendar.MONTH) + 1);
		String mDay = fstr(c.get(Calendar.DAY_OF_MONTH));
		String mHour = fstr(c.get(Calendar.HOUR_OF_DAY));
		String mMinute = fstr(c.get(Calendar.MINUTE));
		String mSec = fstr(c.get(Calendar.SECOND));

		String str_file_name = +mYear + "/" + mMonth + "/" + mDay + "/a"
				+ mYear + "." + mMonth + "." + mDay + "-" + mHour + "."
				+ mMinute + "." + mSec + ".jpg";
		File fileName = null;
		File sdDir;
		sdDir = new File("/storage/external_SD/DCIM");
		if (!sdDir.isDirectory()) {
			String sdState = android.os.Environment.getExternalStorageState();
			if (sdState.equals(android.os.Environment.MEDIA_MOUNTED)) {
				sdDir = android.os.Environment.getExternalStorageDirectory();
			} else {
				sdDir = android.os.Environment.getDataDirectory();
			}
		}
		fileName = new File(sdDir, str_file_name);
		if (!fileName.getParentFile().exists()) {
			fileName.getParentFile().mkdirs();
		}
		return fileName;
	}

}
