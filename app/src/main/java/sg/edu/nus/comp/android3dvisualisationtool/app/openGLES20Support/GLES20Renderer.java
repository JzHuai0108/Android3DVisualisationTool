package sg.edu.nus.comp.android3dvisualisationtool.app.openGLES20Support;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import java.util.List;

import sg.edu.nus.comp.android3dvisualisationtool.app.configuration.Constants;
import sg.edu.nus.comp.android3dvisualisationtool.app.dataReader.DataReader;
import sg.edu.nus.comp.android3dvisualisationtool.app.points.Point;
import sg.edu.nus.comp.android3dvisualisationtool.app.points.Points;

/**
 * Created by panlong on 6/6/14.
 */
public class GLES20Renderer extends GLRenderer implements Constants {

    private static final String TAG = "GLES20Renderer";
    private Points mPoints;

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mRotationMatrix = new float[16];

    private float mAngle;
    private Context context;

    public GLES20Renderer(Context context) {
        this.context = context;
    }

    @Override
    public void onCreate(int width, int height, boolean isContextLost) {
        if (isContextLost) {
            // Set the background frame color
            GLES20.glClearColor(0.8f, 0.8f, 0.8f, 1.0f);

            DataReader dr = new DataReader(context, "data.pcd");
            List<Point> lstPoint = dr.getPoints();
            mPoints = new Points(lstPoint);
        } else {
            // Adjust the viewport based on geometry changes,
            // such as screen rotation
            GLES20.glViewport(0, 0, width, height);

            float ratio = (float) width / height;

            // this projection matrix is applied to object coordinates
            // in the onDrawFrame() method
            float left = -ratio;
            float right = ratio;
            float bottom = -1;
            float top = 1;
            float near = 0.1f;
            float far = 10000.0f;
            Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
        }
    }

    @Override
    public void onDrawFrame(boolean isFirstDraw) {

        float[] scratch = new float[16];

        // Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Set the camera position (View matrix)
        float centerX = 0f;
        float centerY = 0f;
        float centerZ = -2f;

        float lookAtX = 0f;
        float lookAtY = 0f;
        float lookAtZ = 0f;

        float lookUpX = 0f;
        float lookUpY = 1f;
        float lookUpZ = 0f;

        Matrix.setLookAtM(mViewMatrix, 0, centerX, centerY, centerZ, lookAtX, lookAtY, lookAtZ, lookUpX, lookUpY, lookUpZ);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        // Create a rotation for the triangle

        // Use the following code to generate constant rotation.
        // Leave this code out when using TouchEvents.
        // long time = SystemClock.uptimeMillis() % 4000L;
        // float angle = 0.090f * ((int) time);

        Matrix.setRotateM(mRotationMatrix, 0, mAngle, 0, 0, 1.0f);

        // Combine the rotation matrix with the projection and camera view
        // Note that the mMVPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mRotationMatrix, 0);

        // Draw triangle
        mPoints.draw(scratch);
    }

    /**
     * Utility method for compiling a OpenGL shader.
     *
     * <p><strong>Note:</strong> When developing shaders, use the checkGlError()
     * method to debug shader coding errors.</p>
     *
     * @param type - Vertex or fragment shader type.
     * @param shaderCode - String containing the shader code.
     * @return - Returns an id for the shader.
     */
    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    /**
     * Utility method for debugging OpenGL calls. Provide the name of the call
     * just after making it:
     *
     * <pre>
     * mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
     * MyGLRenderer.checkGlError("glGetUniformLocation");</pre>
     *
     * If the operation is not successful, the check throws an error.
     *
     * @param glOperation - Name of the OpenGL call to check.
     */
    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }

    /**
     * Returns the rotation angle of the triangle shape (mTriangle).
     *
     * @return - A float representing the rotation angle.
     */
    public float getAngle() {
        return mAngle;
    }

    /**
     * Sets the rotation angle of the triangle shape (mTriangle).
     */
    public void setAngle(float angle) {
        mAngle = angle;
    }

}
