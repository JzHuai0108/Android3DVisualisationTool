package sg.edu.nus.comp.android3dvisualisationtool.app.axis;

import sg.edu.nus.comp.android3dvisualisationtool.app.configuration.Constants;

/**
 * Created by panlong on 17/6/14.
 */
public class Axes implements Constants{
    public static void draw(float[] mvpMatrix, float length, float width) {
        Axis.draw(mvpMatrix, length, width, width, new float[] {1f, 0f, 0f, 1f});
        Axis.draw(mvpMatrix, width, length, width, new float[] {0f, 1f, 0f, 1f});
        Axis.draw(mvpMatrix, width, width, length, new float[] {0f, 0f, 1f, 1f});
    }
}
