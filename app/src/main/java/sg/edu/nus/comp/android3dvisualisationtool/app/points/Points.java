package sg.edu.nus.comp.android3dvisualisationtool.app.points;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import sg.edu.nus.comp.android3dvisualisationtool.app.MainActivity;
import sg.edu.nus.comp.android3dvisualisationtool.app.UI.NavigationDrawerFragment;
import sg.edu.nus.comp.android3dvisualisationtool.app.UI.SliderFragment;
import sg.edu.nus.comp.android3dvisualisationtool.app.configuration.Constants;
import sg.edu.nus.comp.android3dvisualisationtool.app.configuration.ScaleConfiguration;
import sg.edu.nus.comp.android3dvisualisationtool.app.openGLES20Support.GLES20Renderer;

/**
 * Created by panlong on 6/6/14.
 */
public class Points implements Constants{

    private String vertexShaderCode;

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";
    private static ScaleConfiguration sc;
    private FloatBuffer vertexBuffer;
    private int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;
    private static float radius;
    private static float scaleFactor;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    static List<Point> pointsList;
    static float[] pointCoords;
    private final int vertexCount;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    private boolean prevSetOrigin = false;

    float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 0.0f };

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public Points(List<Point> lstPoints) {
        vertexCount = lstPoints.size();
        pointsList = lstPoints;
        sc = new ScaleConfiguration(pointsList, DEFAULT_MAX_ABS_COORIDINATE);
        radius = (float) (sc.getRadius() * MainActivity.width / DEFAULT_MAX_ABS_COORIDINATE);
        scaleFactor = (float) sc.getScaleFactor();

        preSetup();
    }

    private void generateCoordsArray() {
        ArrayList<Float> mutableArrayOfPoint = new ArrayList<Float>();

        for (Point p : pointsList) {
            if (NavigationDrawerFragment.getSetOrigin()) {
                double[] centerOfMass = sc.getCenterOfMass();
                mutableArrayOfPoint.add(p.getX() * scaleFactor - (float)centerOfMass[0]);
                mutableArrayOfPoint.add(p.getY() * scaleFactor - (float)centerOfMass[1]);
                mutableArrayOfPoint.add(p.getZ() * scaleFactor - (float)centerOfMass[2]);
            } else {
                mutableArrayOfPoint.add(p.getX() * scaleFactor);
                mutableArrayOfPoint.add(p.getY() * scaleFactor);
                mutableArrayOfPoint.add(p.getZ() * scaleFactor);
            }
        }

        pointCoords = new float[mutableArrayOfPoint.size()];
        for (int i = 0; i <mutableArrayOfPoint.size(); i ++)
            pointCoords[i] = (mutableArrayOfPoint.get(i) == null) ? Float.NaN : mutableArrayOfPoint.get(i);
    }

    private void prepareProgram() {
        // prepare shaders and OpenGL program
        int vertexShader = GLES20Renderer.loadShader(
                GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = GLES20Renderer.loadShader(
                GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables
    }

    private void initBuffer() {
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                pointCoords.length * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        vertexBuffer.put(pointCoords);
        // set the buffer to read the first coordinate
        vertexBuffer.position(0);
    }

    private void preSetup(){
        vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
            "void main() {" +
            "  gl_Position = uMVPMatrix * vPosition;" +
            "  gl_PointSize = " + radius + ";" +
            "}";

        generateCoordsArray();
        initBuffer();
        prepareProgram();
    }

    /**
     * Encapsulates the OpenGL ES instructions for drawing this shape.
     *
     * @param mvpMatrix - The Model View Project matrix in which to draw
     * this shape.
     */
    public void draw(float[] mvpMatrix) {
        if (NavigationDrawerFragment.getSetOrigin() != prevSetOrigin) {
            preSetup();
            prevSetOrigin = NavigationDrawerFragment.getSetOrigin();
        }
        if (radius != SliderFragment.getRadiusScale()*sc.getRadius()){
            radius = (float)(SliderFragment.getRadiusScale() * sc.getRadius() * MainActivity.width / DEFAULT_MAX_ABS_COORIDINATE);
            preSetup();
        }


        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(
                mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        GLES20Renderer.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        GLES20Renderer.checkGlError("glUniformMatrix4fv");

        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, vertexCount);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

    public static float getRadius() {
        if (radius > 0)
            return radius;
        else if (sc != null) {
            radius = (float) sc.getRadius();
            if (radius > 0)
               return radius;
        }
            return -1;
    }

    public static void setRadius(float newRadius) {
        if (newRadius > 0) {
            radius = newRadius;
        }
    }

    public static float getScaleFactor() {
        if (scaleFactor > 0)
            return scaleFactor;
        return -1;
    }
}
