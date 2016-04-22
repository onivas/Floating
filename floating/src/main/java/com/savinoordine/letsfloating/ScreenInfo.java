package com.savinoordine.letsfloating;

import android.app.Activity;
import android.graphics.Point;
import android.view.Display;

public class ScreenInfo {
    public int mScreenWidth;
    public int mScreenHeight;
    private Activity mActivity;

    public ScreenInfo(Activity activity) {
        mActivity = activity;
        getScreenSize();
    }

    public int getScreenHeight() {
        return mScreenHeight;
    }

    public void setScreenHeight(int screenHeight) {
        this.mScreenHeight = screenHeight;
    }

    public int getScreenWidth() {
        return mScreenWidth;
    }

    public void setScreenWidth(int screenWidth) {
        this.mScreenWidth = screenWidth;
    }

    private void getScreenSize() {
        Display display = mActivity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        setScreenHeight(size.y);
        setScreenWidth(size.x);
    }
}
