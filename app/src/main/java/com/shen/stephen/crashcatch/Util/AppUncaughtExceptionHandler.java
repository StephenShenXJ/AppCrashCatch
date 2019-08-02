package com.shen.stephen.crashcatch.Util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.shen.stephen.crashcatch.CatchCrashApplication;
import com.shen.stephen.crashcatch.PatchDialogActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ShenXz on 8/1/2019.
 */

public class AppUncaughtExceptionHandler implements UncaughtExceptionHandler {

	private Context applicationContext;

	private volatile boolean crashing;

	private DateFormat mFormatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

	private Thread.UncaughtExceptionHandler mDefaultHandler;

	public static AppUncaughtExceptionHandler sAppUncaughtExceptionHandler;

	public static synchronized AppUncaughtExceptionHandler getInstance() {
		if (sAppUncaughtExceptionHandler == null) {
			synchronized (AppUncaughtExceptionHandler.class) {
				if (sAppUncaughtExceptionHandler == null) {
					sAppUncaughtExceptionHandler = new AppUncaughtExceptionHandler();
				}
			}
		}
		return sAppUncaughtExceptionHandler;
	}

	/**
	 * init
	 *
	 * @param context
	 */
	public void init(Context context) {
		applicationContext = context.getApplicationContext();
		crashing = false;
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		if (crashing) {
			return;
		}
		crashing = true;

		ex.printStackTrace();
		if (!handlelException(ex) && mDefaultHandler != null) {
			mDefaultHandler.uncaughtException(thread, ex);
		}
		byebye();
	}

	private void byebye() {
		android.os.Process.killProcess(android.os.Process.myPid());
		System.exit(0);
	}

	private boolean handlelException(Throwable ex) {
		if (ex == null) {
			return false;
		}
		try {
			// crash info
			String crashReport = getCrashReport(ex);
			// TODO: 上传日志到服务器
			// save to sdcard
			saveExceptionToSdcard(crashReport);
			// show dialog
			showPatchDialog();
		} catch (Exception e) {
			return false;
		}
		return true;
	}


	public void showPatchDialog() {
		Intent intent = PatchDialogActivity.newIntent(applicationContext, getApplicationName(applicationContext), null);
		applicationContext.startActivity(intent);
	}

	private String getApplicationName(Context context) {
		PackageManager packageManager = context.getPackageManager();
		ApplicationInfo applicationInfo = null;
		String name = null;
		try {
			applicationInfo = packageManager.getApplicationInfo(
				context.getApplicationInfo().packageName, 0);
			name = (String) packageManager.getApplicationLabel(applicationInfo);
		} catch (final PackageManager.NameNotFoundException e) {
			String[] packages = context.getPackageName().split(".");
			name = packages[packages.length - 1];
		}
		return name;
	}

	/**
	 * get crash info
	 * @param ex
	 * @return
	 */
	private String getCrashReport(Throwable ex) {
		StringBuffer exceptionStr = new StringBuffer();
		PackageInfo pinfo = CatchCrashApplication.getInstance().getLocalPackageInfo();
		if (pinfo != null) {
			if (ex != null) {
				//app version
				exceptionStr.append("App Version：" + pinfo.versionName);
				exceptionStr.append("_" + pinfo.versionCode + "\n");

				//phone system info
				exceptionStr.append("OS Version：" + Build.VERSION.RELEASE);
				exceptionStr.append("_");
				exceptionStr.append(Build.VERSION.SDK_INT + "\n");

				//phone vendor
				exceptionStr.append("Vendor: " + Build.MANUFACTURER+ "\n");

				//phone model
				exceptionStr.append("Model: " + Build.MODEL+ "\n");

				String errorStr = ex.getLocalizedMessage();
				if (TextUtils.isEmpty(errorStr)) {
					errorStr = ex.getMessage();
				}
				if (TextUtils.isEmpty(errorStr)) {
					errorStr = ex.toString();
				}
				exceptionStr.append("Exception: " + errorStr + "\n");
				StackTraceElement[] elements = ex.getStackTrace();
				if (elements != null) {
					for (int i = 0; i < elements.length; i++) {
						exceptionStr.append(elements[i].toString() + "\n");
					}
				}
			} else {
				exceptionStr.append("no exception. Throwable is null\n");
			}
			return exceptionStr.toString();
		} else {
			return "";
		}
	}

	/**
	 * save error repory to sdcard
	 * @param errorReason
	 */
	private void saveExceptionToSdcard(String errorReason) {
		try {
			Log.e("CrashDemo", "AppUncaughtExceptionHandler save to sdcard");
			String time = mFormatter.format(new Date());
			String fileName = "Crash-" + time + ".log";
			if (SdcardConfig.getInstance().hasSDCard()) {
				String path = SdcardConfig.LOG_FOLDER;
				File dir = new File(path);
				if (!dir.exists()) {
					dir.mkdirs();
				}
				FileOutputStream fos = new FileOutputStream(path + fileName);
				fos.write(errorReason.getBytes());
				fos.close();
			}
		} catch (Exception e) {
			Log.e("CrashDemo", "an error occured while writing file..." + e.getMessage());
		}
	}

}
