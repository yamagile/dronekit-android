package org.droidplanner.sample.hellodrone;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class MyGLView extends GLSurfaceView implements GestureDetector.OnGestureListener {
    MyRenderer myRenderer;

    public MyGLView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLConfigChooser(8, 8, 8, 8, 8, 8);
        myRenderer = new MyRenderer();
        setRenderer(myRenderer);
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2,
                            float distanceX, float distanceY) {
        myRenderer.setMotion(distanceX, distanceY); // pass movement to render
        return true;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {
    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    public void setPositions(double latitude, double longitude, double vehicleAltitude) {
        myRenderer.setPositions((float)latitude, (float)longitude, (float)vehicleAltitude);
    }

    public void clearGLView() {
        myRenderer.clearGLView();
    }
}
