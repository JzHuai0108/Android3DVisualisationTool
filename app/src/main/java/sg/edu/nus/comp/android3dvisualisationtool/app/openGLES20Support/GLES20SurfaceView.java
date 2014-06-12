package sg.edu.nus.comp.android3dvisualisationtool.app.openGLES20Support;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

/**
 * Created by panlong on 6/6/14.
 */
public class GLES20SurfaceView extends GLSurfaceView {
    private static int NONE = 0;
    private static int ROTATE = 1;
    private static int ZOOM = 2;

    private GLES20Renderer mRenderer = null;
    ScaleGestureDetector detector;
    private float scaleFactor = 1;
    private Context context;
    private int mode = NONE;

    public GLES20SurfaceView(Context context) {
        super(context);
    }

    public GLES20SurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setContext(Context context) {
        this.context = context;
        configureRenderer();
    }

    private void configureRenderer() {
        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);

        // Set the Renderer for drawing on the GLSurfaceView
        mRenderer = new GLES20Renderer(context);
        setRenderer(mRenderer);

        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        //set up pinch gesture
        detector = new ScaleGestureDetector(getContext(), new ScaleListener());
    }

    private float mPreviousX;
    private float mPreviousY;

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        float x = e.getX(), y = e.getY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_POINTER_DOWN:
                mode = ZOOM;
                break;

            case MotionEvent.ACTION_DOWN:
                mPreviousX = x;
                mPreviousY = y;
                break;

            case MotionEvent.ACTION_MOVE:
                int count = e.getPointerCount();
                if (count == 1) {
                    mode = ROTATE;
                    mRenderer.setRotation((int)mPreviousX, (int)mPreviousY, (int)x, (int)y);  // = 180.0f / 320
                    mPreviousX = x;
                    mPreviousY = y;
                }
                break;

            case MotionEvent.ACTION_POINTER_UP:
                mode = ROTATE;
                mRenderer.setRotation((int)mPreviousX, (int)mPreviousY, (int)x, (int)y);  // = 180.0f / 320
                mPreviousX = x;
                mPreviousY = y;
                break;

        }

        detector.onTouchEvent(e);

        if (mode == ROTATE || mode == ZOOM) {
            requestRender();
        }

        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            mRenderer.setCameraDistance(scaleFactor);
            return true;
        }
    }
}
