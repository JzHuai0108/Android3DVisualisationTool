package sg.edu.nus.comp.android3dvisualisationtool.app.points;

import android.opengl.GLES20;
import sg.edu.nus.comp.android3dvisualisationtool.app.MainActivity;
import sg.edu.nus.comp.android3dvisualisationtool.app.UI.NavigationDrawerFragment;
import sg.edu.nus.comp.android3dvisualisationtool.app.UI.SliderFragment;
import sg.edu.nus.comp.android3dvisualisationtool.app.configuration.Constants;
import sg.edu.nus.comp.android3dvisualisationtool.app.configuration.ScaleConfiguration;
import sg.edu.nus.comp.android3dvisualisationtool.app.dataReader.DataType;
import sg.edu.nus.comp.android3dvisualisationtool.app.openGLES20Support.GLES20Renderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

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
    private FloatBuffer lineBuffer;
    private FloatBuffer curvatureBuffer;
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
    static float[] lineCoords;
    static float[] curvaturePointCoords;
    private final int vertexCount;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    private boolean prevSetOrigin = DEFAULT_IS_SET_TO_ORIGIN;
    private boolean prevShowCurvature = DEFAULT_IS_SELECTING_CURVATURE;

    private ArrayList<Point> normalPoints = null;
    private ArrayList<Point> curvaturePoints = null;

    private boolean isNormalVectorVisible = DEFAULT_IS_NORMAL_VECTOR_VISIBLE;
    private boolean isPointContainsNormalVector = false;

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

    private void generateCurvatureCoordsArray(){
        normalPoints = new ArrayList<Point>();
        curvaturePoints = new ArrayList<Point>();

        for (Point p : pointsList) {
            float curvature = p.getCurvature();
            float selectedCurvature = SliderFragment.getCurvature();
            if (curvature+DEFAULT_PRECISION>selectedCurvature && curvature-DEFAULT_PRECISION<selectedCurvature){
                curvaturePoints.add(p);
            } else {
                normalPoints.add(p);
            }
        }

        pointCoords = new float[normalPoints.size() * 3];
        curvaturePointCoords = new float[curvaturePoints.size() * 3];

        for (int i=0; i<normalPoints.size(); i++){
            Point p = normalPoints.get(i);
            double[] shift;
            if (NavigationDrawerFragment.getSetOrigin()) {
                shift = sc.getCenterOfMass();
            } else {
                shift = new double[] {0, 0, 0};
            }
            pointCoords[3 * i] = p.getX() * scaleFactor - (float) shift[0];
            pointCoords[3 * i + 1] = p.getY() * scaleFactor - (float) shift[1];
            pointCoords[3 * i + 2] = p.getZ() * scaleFactor - (float) shift[2];
        }

        for (int i=0; i<curvaturePoints.size(); i++){
            Point p = curvaturePoints.get(i);
            double[] shift;
            if (NavigationDrawerFragment.getSetOrigin()) {
                shift = sc.getCenterOfMass();
            } else {
                shift = new double[] {0, 0, 0};
            }
            curvaturePointCoords[3 * i] = p.getX() * scaleFactor - (float) shift[0];
            curvaturePointCoords[3 * i + 1] = p.getY() * scaleFactor - (float) shift[1];
            curvaturePointCoords[3 * i + 2] = p.getZ() * scaleFactor - (float) shift[2];
        }
    }

    private void generateCoordsArray() {
        pointCoords = new float[vertexCount * 3];
        lineCoords = new float[vertexCount * 6];

        if (pointsList != null) {
            for (int i = 0; i < vertexCount; i ++) {
                Point p = pointsList.get(i);
                double[] shift;

                if (NavigationDrawerFragment.getSetOrigin()) {
                    shift = sc.getCenterOfMass();
                } else {
                    shift = new double[] {0, 0, 0};
                }
                    pointCoords[3 * i] = p.getX() * scaleFactor - (float) shift[0];
                    pointCoords[3 * i + 1] = p.getY() * scaleFactor - (float) shift[1];
                    pointCoords[3 * i + 2] = p.getZ() * scaleFactor - (float) shift[2];

                    if (isNormalVectorVisible && (p.getType() == DataType.XYZNORMAL
                        || p.getType() == DataType.XYZCNORMAL)) {

                        isPointContainsNormalVector = true;

                        lineCoords[6 * i] = (float) (p.getX() * scaleFactor - shift[0]);
                        lineCoords[6 * i + 1] = (float) (p.getY() * scaleFactor - shift[1]);
                        lineCoords[6 * i + 2] = (float) (p.getZ() * scaleFactor - shift[2]);

                        float[] n = p.getNormal();
                        float length = (float) Math.sqrt(n[0] * n[0] + n[1] * n[1]
                                + n[2] * n[2]);

                        lineCoords[6 * i + 3] = (float) (p.getX() * scaleFactor - shift[0] + n[0]
                                / length * DEFAULT_NORMAL_VECTOR_LENGTH * radius
                                / scaleFactor);
                        lineCoords[6 * i + 4] = (float) (p.getY() * scaleFactor - shift[1] + n[1]
                                / length
                                * DEFAULT_NORMAL_VECTOR_LENGTH * radius
                                / scaleFactor);
                        lineCoords[6 * i + 5] = (float) (p.getZ()
                                * scaleFactor - shift[2] + n[2] / length
                                * DEFAULT_NORMAL_VECTOR_LENGTH
                                * radius / scaleFactor);

                    }
            }
        }
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

        if (isNormalVectorVisible && isPointContainsNormalVector) {
            bb = ByteBuffer.allocateDirect(lineCoords.length * 4);
            bb.order(ByteOrder.nativeOrder());
            lineBuffer = bb.asFloatBuffer();
            lineBuffer.put(lineCoords);
            lineBuffer.position(0);
        }

        if (NavigationDrawerFragment.getShowCurvature()) {
            bb = ByteBuffer.allocateDirect(curvaturePointCoords.length * 4);
            bb.order(ByteOrder.nativeOrder());
            curvatureBuffer = bb.asFloatBuffer();
            curvatureBuffer.put(curvaturePointCoords);
            curvatureBuffer.position(0);
        }

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
            prevSetOrigin = NavigationDrawerFragment.getSetOrigin();
            if (NavigationDrawerFragment.getShowCurvature()) {
                generateCurvatureCoordsArray();
                initBuffer();
                prepareProgram();
            } else {
                preSetup();
            }
        }

        if (NavigationDrawerFragment.getShowCurvature() != prevShowCurvature) {
            prevShowCurvature = NavigationDrawerFragment.getShowCurvature();
            if (NavigationDrawerFragment.getShowCurvature()){
                generateCurvatureCoordsArray();
                initBuffer();
                prepareProgram();
            } else {
                preSetup();
            }
        }

        if (radius != (float)(SliderFragment.getRadiusScale() * sc.getRadius() * MainActivity.width / DEFAULT_MAX_ABS_COORIDINATE)){
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
        GLES20.glUniform4fv(mColorHandle, 1, DEFAULT_COLOR, 0);

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        GLES20Renderer.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        GLES20Renderer.checkGlError("glUniformMatrix4fv");

        // Draw the triangle
        if (NavigationDrawerFragment.getShowCurvature()){
            GLES20.glDrawArrays(GLES20.GL_POINTS, 0, normalPoints.size());
        } else {
            GLES20.glDrawArrays(GLES20.GL_POINTS, 0, vertexCount);
        }

        if (isNormalVectorVisible && isPointContainsNormalVector) {
            GLES20.glLineWidth(radius / 2);

            GLES20.glVertexAttribPointer(
                    mPositionHandle, COORDS_PER_VERTEX,
                    GLES20.GL_FLOAT, false,
                    vertexStride, vertexBuffer);
            GLES20.glDrawArrays(GLES20.GL_LINES, 0, vertexCount * 2);
        }

        if (NavigationDrawerFragment.getShowCurvature()){
            GLES20.glUniform4fv(mColorHandle, 1, CURVATURE_COLOR, 0);
            GLES20.glVertexAttribPointer(
                    mPositionHandle, COORDS_PER_VERTEX,
                    GLES20.GL_FLOAT, false,
                    vertexStride, curvatureBuffer);
            GLES20.glDrawArrays(GLES20.GL_POINTS, 0, curvaturePoints.size());
        }

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
