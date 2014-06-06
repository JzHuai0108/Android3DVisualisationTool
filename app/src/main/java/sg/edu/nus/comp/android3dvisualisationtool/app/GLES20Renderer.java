package sg.edu.nus.comp.android3dvisualisationtool.app;

import android.opengl.GLES20;

/**
 * Created by panlong on 6/6/14.
 */
public class GLES20Renderer extends GLRenderer{

    @Override
    public void onCreate(int width, int height, boolean isContextLost) {
        GLES20.glClearColor(0.5f, 0.6f, 0.7f, 1f);
    }

    @Override
    public void onDrawFrame(boolean isFirstDraw) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
    }
}
