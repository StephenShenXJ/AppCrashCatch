package com.shen.stephen.crashcatch;

import android.app.Activity;

/**
 * Created by ShenXz on 8/1/2019.
 */

public class PatchBaseActivity extends Activity {

    @Override
    final protected void onDestroy() {
        super.onDestroy();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

}
