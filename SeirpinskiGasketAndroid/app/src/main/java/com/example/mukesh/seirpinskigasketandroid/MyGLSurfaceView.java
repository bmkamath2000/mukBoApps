package com.example.mukesh.seirpinskigasketandroid;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import android.view.KeyEvent;

public class MyGLSurfaceView extends GLSurfaceView {
    private final MyGLRenderer mRenderer;
    //variable for storing the time of first click
    long startTime;
    public MyGLSurfaceView(Context context) {
        super(context);

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        mRenderer = new MyGLRenderer();

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(mRenderer);
        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        //constant for defining the time duration between the click that can be considered as double-tap
        final int MAX_DURATION = 200;

        if (e.getAction() == MotionEvent.ACTION_DOWN) {

            startTime = System.currentTimeMillis();
        }
        else if (e.getAction() == MotionEvent.ACTION_UP) {

            if(System.currentTimeMillis() - startTime >= MAX_DURATION)
            {
                //DOUBLE TAP
                mRenderer.mTriangle.n++;
                requestRender();
            }
            else {
                //SINGLE TAP
                if(mRenderer.mTriangle.n>0) {
                    mRenderer.mTriangle.n--;
                    requestRender();
                }
            }
        }
        return true;
    }
}
