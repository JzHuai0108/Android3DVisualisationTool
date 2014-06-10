package sg.edu.nus.comp.android3dvisualisationtool.app;

/**
 * Created by tang on 10/6/14.
 */
public class Point {
    private float x, y, z;
    private DataType type;

    public Point(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.type = DataType.XYZ;
    }

    public DataType getType() {
        return this.type;
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public float getZ() {
        return this.z;
    }
}
