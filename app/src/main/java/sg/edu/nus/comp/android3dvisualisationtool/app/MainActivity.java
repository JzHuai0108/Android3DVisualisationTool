package sg.edu.nus.comp.android3dvisualisationtool.app;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.FragmentManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import sg.edu.nus.comp.android3dvisualisationtool.app.UI.NavigationDrawerFragment;
import sg.edu.nus.comp.android3dvisualisationtool.app.UI.SliderFragment;
import sg.edu.nus.comp.android3dvisualisationtool.app.openGLES20Support.GLES20SurfaceView;


public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, SliderFragment.OnFragmentInteractionListener {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private SliderFragment mDialog_fragment = new SliderFragment();
    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    // opengles view
    private GLES20SurfaceView mGLSurfaceView;

    //window size
    public static int width, height;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        mDialog_fragment = (SliderFragment)
                getFragmentManager().findFragmentById(R.id.drawer_layout);

        initialiseOpenGLESView();

        calculateWindowSize();
    }

    private void calculateWindowSize() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        try {
            int id = item.getItemId();
            if (id == R.id.action_dialog) {
                FragmentManager fm = getFragmentManager();
                mDialog_fragment = mDialog_fragment == null ? new SliderFragment() : mDialog_fragment;
                mDialog_fragment.show(fm, "fragment_dialog");
            }
            return super.onOptionsItemSelected(item);
        }catch(Exception ex){
            ex.printStackTrace();
            return super.onOptionsItemSelected(item);
        }
    }

    // methods for opengles support
    private boolean hasGLES20() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        return configurationInfo.reqGlEsVersion >= 0x20000;
    }

    private void initialiseOpenGLESView() {
        if (hasGLES20()) {
            mGLSurfaceView = (GLES20SurfaceView) findViewById(R.id.gl_surface_view);
            mGLSurfaceView.setContext(getApplicationContext());
        } else {
            System.out.println("This phone does not support OpenGLES 2.0, quiting...");
            System.exit(1);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGLSurfaceView != null)
            mGLSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGLSurfaceView != null)
            mGLSurfaceView.onPause();
    }


    // handle dialog event
    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {

    }
}
