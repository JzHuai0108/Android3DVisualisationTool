package sg.edu.nus.comp.android3dvisualisationtool.app.dataReader;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import sg.edu.nus.comp.android3dvisualisationtool.app.points.Point;

/**
 * Created by tang on 10/6/14.
 */
public class DataReader{
    private List<Point> points = null;
    private Context context = null;

    public DataReader(Context context, String filename) {
        this.context = context;
        openFile(filename);
    }

    public List<Point> getPoints() {
        return points;
    }

    private void openFile(String filename)
    {
        points = new ArrayList<Point>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(filename)));
            int numOfPoints = 0;

            //read number of points and skip other unused header entries
            for (int i=0; i < 12; i++ ) {
                String[] temp = reader.readLine().split(" ");
                if (temp[0].equals("POINTS")) {
                    numOfPoints = Integer.parseInt(temp[1]);
                } else if (temp[0].equals("DATA")) {
                    break;
                }
            }

            for (int i = 0; i < numOfPoints; i++) {
                String[] coordinates = reader.readLine().split(" ");
                float x = (float) Double.parseDouble(coordinates[0]);
                float y = (float) Double.parseDouble(coordinates[1]);
                float z = (float) Double.parseDouble(coordinates[2]);

                points.add(new Point(x, y, z));
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
