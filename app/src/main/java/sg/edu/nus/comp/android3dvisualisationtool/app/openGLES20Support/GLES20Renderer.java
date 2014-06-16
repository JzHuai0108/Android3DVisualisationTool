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
import sg.edu.nus.comp.android3dvisualisationtool.app.util.VirtualSphere;

/**
 * Created by panlong on 6/6/14.
 */
public class GLES20Renderer extends GLRenderer implements Constants {

    private static final String TAG = "GLES20Renderer";
    private Points mPoints;
    private VirtualSphere vs = new VirtualSphere();
    private android.graphics.Point cueCenter = new android.graphics.Point();
    private int cueRadius;

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private float[] mMVPMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mRotationMatrix = sg.edu.nus.comp.android3dvisualisationtool.app.util.Matrix.identity();

    private float cameraDistance = (float) DEFAULT_CAMERA_DISTANCE;
    private float cameraFieldOfView = (float) DEFAULT_FIELD_OF_VIEW;
    private float near = (float) DEFAULT_CAMERA_NEAR_CLIP;
    private float far = (float) DEFAULT_CAMERA_FAR_CLIP;
    private float lookAtX = (float) DEFAULT_LOOK_AT_POINT_X;
    private float lookAtY = (float) DEFAULT_LOOK_AT_POINT_Y;
    private int windowHeight;
    private int windowWidth;
    private float radius = 0;

    private Context context;

    public GLES20Renderer(Context context) {
        this.context = context;
    }

    @Override
    public void onCreate(int width, int height, boolean isContextLost) {
        if (isContextLost) {
            // Set the background frame color
            GLES20.glClearColor(0.8f, 0.8f, 0.8f, 1.0f);

            mRotationMatrix = sg.edu.nus.comp.android3dvisualisationtool.app.util.Matrix.identity();
            Matrix.rotateM(mRotationMatrix, 0, DEFAULT_CAMERA_ANGLE_X, 1, 0, 0);
            Matrix.rotateM(mRotationMatrix, 0, DEFAULT_CAMERA_ANGLE_Y, 0, 1, 0);

            DataReader dr = new DataReader(context, "data.pcd");
            List<Point> lstPoint = dr.getPoints();
            mPoints = new Points(lstPoint);
            radius = mPoints.getRadius();
        } else {
            // Adjust the viewport based on geometry changes,
            // such as screen rotation
            GLES20.glViewport(0, 0, width, height);

            float ratio = (float) width / height;

            // this projection matrix is applied to object coordinates
            // in the onDrawFrame() method
            Matrix.perspectiveM(mProjectionMatrix, 0, cameraFieldOfView, ratio, near, far);
        }

        setupVS(width, height);
        windowHeight = height;
        windowWidth = width;
    }

    @Override
    public void onDrawFrame(boolean isFirstDraw) {

        float[] scratch = new float[16];

        // Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Set the camera position (View matrix)
        float centerX = 0f;
        float centerY = 0f;
        float centerZ = cameraDistance;

        float lookAtZ = 0f;

        float lookUpX = 0f;
        float lookUpY = 1f;
        float lookUpZ = 0f;

        Matrix.setLookAtM(mViewMatrix, 0, centerX, centerY, centerZ, lookAtX, lookAtY, lookAtZ, lookUpX, lookUpY, lookUpZ);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        // Combine the rotation matrix with the projection and camera view
        // Note that the mMVPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mRotationMatrix, 0);

        // Draw triangle
        mPoints.draw(scratch);
    }

    public void setupVS(int w, int h) {
        cueCenter.x = w / 2;
        cueCenter.y = h / 2;
        cueRadius = (int) (Math.sqrt(w * w + h * h) / 2);
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

    private float[] mouseMtx = new float[16];
    /**
     * @param previousX - x coordinate of previous position of touch event
     * @param previousY - y coordinate of previous position of touch event
     * @param x - x coordinate of new position of touch event
     * @param y - y coordinate of new position of touch event
     * set new rotation matrix
     */
    public void setRotation(int previousX, int previousY, int x, int y) {
        vs.makeRotationMtx(new android.graphics.Point(previousX, previousY), new android.graphics.Point(x, y), cueCenter, cueRadius,
                mouseMtx);

        mRotationMatrix = sg.edu.nus.comp.android3dvisualisationtool.app.util.Matrix.multiply(mRotationMatrix, mouseMtx);
        mRotationMatrix = sg.edu.nus.comp.android3dvisualisationtool.app.util.Matrix.multiply(mRotationMatrix, mouseMtx);
        fixRotationMatrix();


    }

    private void fixRotationMatrix() {
        mRotationMatrix[3] = mRotationMatrix[7] = mRotationMatrix[11] = mRotationMatrix[12] = mRotationMatrix[13] = mRotationMatrix[14] = 0.0f;
        mRotationMatrix[15] = 1.0f;
        float fac;
        if ((fac = (float) Math.sqrt((mRotationMatrix[0] * mRotationMatrix[0])
                + (mRotationMatrix[4] * mRotationMatrix[4])
                + (mRotationMatrix[8] * mRotationMatrix[8]))) != 1.0f) {
            if (fac != 0.0f) {
                fac = 1.0f / fac;
                mRotationMatrix[0] *= fac;
                mRotationMatrix[4] *= fac;
                mRotationMatrix[8] *= fac;
            }
        }
        if ((fac = (float) Math.sqrt((mRotationMatrix[1] * mRotationMatrix[1])
                + (mRotationMatrix[5] * mRotationMatrix[5])
                + (mRotationMatrix[9] * mRotationMatrix[9]))) != 1.0f) {
            if (fac != 0.0f) {
                fac = 1.0f / fac;
                mRotationMatrix[1] *= fac;
                mRotationMatrix[5] *= fac;
                mRotationMatrix[9] *= fac;
            }
        }
        if ((fac = (float) Math.sqrt((mRotationMatrix[2] * mRotationMatrix[2])
                + (mRotationMatrix[6] * mRotationMatrix[6])
                + (mRotationMatrix[10] * mRotationMatrix[10]))) != 1.0f) {
            if (fac != 0.0f) {
                fac = 1.0f / fac;
                mRotationMatrix[2] *= fac;
                mRotationMatrix[6] *= fac;
                mRotationMatrix[10] *= fac;
            }
        }
    }

    public float getCameraDistance() {
        return cameraDistance;
    }

    public void setCameraDistance(float dis) {
        if (dis != 0)
            cameraDistance = dis;
    }

    /**
     * @param scale
     * set new camera distance according to the scale passed from touch event
     */
    public void setCameraDistanceByScale(float scale) { cameraDistance = (float)DEFAULT_CAMERA_DISTANCE / scale; };

    public float getCameraFieldOfView() {
        return cameraFieldOfView;
    }

    public void setCameraFieldOfView(float angle) {
        if (angle > 0 && angle < 180) {
            cameraFieldOfView = angle;
        }
    }
    /**
     * @param deltaX - camera look at point shift distance on x axis
     * @param deltaY - camera look at point shift distance on y axis
     * shift camera look point by (deltaX, deltaY)
     */
    public void shiftCameraLookAtPoint(int deltaX, int deltaY) {
        lookAtX += deltaX;
        lookAtY += deltaY;
    }

    public float getRadius() {
        if (radius > 0)
            return radius;
        else if (mPoints != null) {
            radius = mPoints.getRadius();
            if (radius > 0)
                return radius;
        }
        return -1;
    }

    public void setRadius(float newRadius) {
        if (newRadius > 0) {
            radius = newRadius;
            mPoints.setRadius(newRadius);
        }
    }

    public int getWindowHeight() {
        return windowHeight;
    }

    public int getWindowWidth() {
        return windowWidth;
    }
}
