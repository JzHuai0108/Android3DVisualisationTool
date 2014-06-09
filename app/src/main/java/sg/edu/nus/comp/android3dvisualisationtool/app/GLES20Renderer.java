package sg.edu.nus.comp.android3dvisualisationtool.app;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by panlong on 6/6/14.
 */
public class GLES20Renderer extends GLRenderer{
    private float[] mModelMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];
    private int mMVPMatrixHandle;
    private int mPositionHandle;

    float[] vertices = {0f, 0f, 0f};
    FloatBuffer vertexBuf;

    @Override
    public void onCreate(int width, int height, boolean isContextLost) {
        System.out.println("onCreate");

        if (isContextLost) {
            vertexBuf = ByteBuffer.allocateDirect(vertices.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            vertexBuf.put(vertices).position(0);

            GLES20.glClearColor(0.5f, 0.5f, 0.5f, 0.5f);

            float eyeX = 0f;
            float eyeY = 0f;
            float eyeZ = 0f;

            float centerX = 0f;
            float centerY = 0f;
            float centerZ = -5f;

            float upX = 0f;
            float upY = 1f;
            float upZ = 0f;

            // set the view matrix
            Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);

            final String vertexShader =
                    "uniform mat4 u_MVPMatrix;      \n"
                            + "attribute vec4 a_Position;     \n"
                            + "void main()                    \n"
                            + "{                              \n"
                            + "   gl_Position = u_MVPMatrix   \n"
                            + "               * a_Position;   \n"
                            + "   gl_PointSize = 10.0;       \n"
                            + "}                              \n";

            final String fragmentShader =
                    "precision mediump float;       \n"
                            + "void main()                    \n"
                            + "{                              \n"
                            + "   gl_FragColor = vec4(1.0,    \n"
                            + "   1.0, 1.0, 1.0);             \n"
                            + "}                              \n";

            // load the vertex shader
            int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);

            if (vertexShaderHandle != 0) {
                // Pass in the shader source.
                GLES20.glShaderSource(vertexShaderHandle, vertexShader);

                // Compile the shader.
                GLES20.glCompileShader(vertexShaderHandle);

                // Get the compilation status.
                final int[] compileStatus = new int[1];
                GLES20.glGetShaderiv(vertexShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

                // If the compilation failed, delete the shader.
                if (compileStatus[0] == 0) {
                    GLES20.glDeleteShader(vertexShaderHandle);
                    vertexShaderHandle = 0;
                }
            }

            if (vertexShaderHandle == 0) {
                throw new RuntimeException("Error creating vertex shader.");
            }

            // Load in the fragment shader shader.
            int fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);

            if (fragmentShaderHandle != 0) {
                // Pass in the shader source.
                GLES20.glShaderSource(fragmentShaderHandle, fragmentShader);

                // Compile the shader.
                GLES20.glCompileShader(fragmentShaderHandle);

                // Get the compilation status.
                final int[] compileStatus = new int[1];
                GLES20.glGetShaderiv(fragmentShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

                // If the compilation failed, delete the shader.
                if (compileStatus[0] == 0) {
                    GLES20.glDeleteShader(fragmentShaderHandle);
                    fragmentShaderHandle = 0;
                }
            }

            if (fragmentShaderHandle == 0) {
                throw new RuntimeException("Error creating fragment shader.");
            }

            // Create a program object and store the handle to it.
            int programHandle = GLES20.glCreateProgram();

            if (programHandle != 0) {
                // Bind the vertex shader to the program.
                GLES20.glAttachShader(programHandle, vertexShaderHandle);
                // Bind the fragment shader to the program.
                GLES20.glAttachShader(programHandle, fragmentShaderHandle);
                // Bind attributes
                GLES20.glBindAttribLocation(programHandle, 0, "a_Position");
                // Link the two shaders together into a program.
                GLES20.glLinkProgram(programHandle);
                // Get the link status.
                final int[] linkStatus = new int[1];
                GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);
                // If the link failed, delete the program.
                if (linkStatus[0] == 0) {
                    GLES20.glDeleteProgram(programHandle);
                    programHandle = 0;
                }
            }

            if (programHandle == 0) {
                throw new RuntimeException("Error creating program.");
            }

            // Set program handles. These will later be used to pass in values to the program.
            mMVPMatrixHandle = GLES20.glGetUniformLocation(programHandle, "u_MVPMatrix");
            mPositionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position");

            // Tell OpenGL to use this program when rendering.
            GLES20.glUseProgram(programHandle);
        } else {
            // original in onSurfaceChanged
            GLES20.glViewport(0, 0, width, height);

            final float ratio = (float) width / (float) height;
            final float left = -ratio;
            final float right = ratio;
            final float bottom = -1f;
            final float top = 1f;
            final float near = 0.1f;
            final float far = 10000f;

            Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top
                    , near, far);
        }
    }

    @Override
    public void onDrawFrame(boolean isFirstDraw) {
        System.out.println("onDrawFrame");

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        Matrix.setIdentityM(mModelMatrix, 0);
        //Push to the distance - note this will have no effect on a point size
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -5.0f);
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        //Send the vertex
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuf);
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        //Draw the point
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1);
    }
}
