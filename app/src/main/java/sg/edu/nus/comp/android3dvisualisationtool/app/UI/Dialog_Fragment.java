package sg.edu.nus.comp.android3dvisualisationtool.app.UI;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import sg.edu.nus.comp.android3dvisualisationtool.app.R;


public class Dialog_Fragment extends DialogFragment {

    private OnFragmentInteractionListener mListener;

    private SeekBar seekBar_camera_distant;
    private TextView textView_camera_distant;
    private double value_camera_distant;

    private SeekBar seekBar_field_of_view;
    private TextView textView_field_of_view;
    private double value_field_of_view;

    private SeekBar seekBar_point_radius;
    private TextView textView_point_radius;
    private double value_point_radius;

    private SeekBar seekBar_curvature_precision;
    private TextView textView_curvature_precision;
    private double value_curvature_precision;

    public Dialog_Fragment() {
        // Required empty public constructor
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.settings)
               .setTitle(R.string.dialog_title);
        return builder.show();
    }

    //
    private static double convertToTick(int progress){
        double output;

        if(progress - 30 < 0)
            output = 1/((30 - progress)/10.0 + 1);
        else if(progress == 0)
            output = 1;
        else
            output = (progress - 30)/10.0 + 1;

        return output;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view_fragment = inflater.inflate(R.layout.fragment_dialog, container, false);

        seekBar_camera_distant = (SeekBar) view_fragment.findViewById(R.id.seekBar1);
        seekBar_camera_distant.setMax(60); // the medium is 3 || [0...6] match [1/4...4]
        textView_camera_distant = (TextView) view_fragment.findViewById(R.id.textView1);
        seekBar_camera_distant.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                value_camera_distant = convertToTick(progress);
                textView_camera_distant.setText(String.format("Camera Distant: %.2f", value_camera_distant));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBar_field_of_view = (SeekBar)view_fragment.findViewById(R.id.seekBar2);
        seekBar_field_of_view.setMax(60);
        textView_field_of_view = (TextView) view_fragment.findViewById(R.id.textView2);
        seekBar_field_of_view.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                value_field_of_view = convertToTick(progress);
                textView_field_of_view.setText(String.format("Field of View: %.2f", value_field_of_view));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBar_point_radius = (SeekBar)view_fragment.findViewById(R.id.seekBar3);
        seekBar_point_radius.setMax(60);
        textView_point_radius = (TextView) view_fragment.findViewById(R.id.textView3);
        seekBar_point_radius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                value_point_radius = convertToTick(progress);
                textView_point_radius.setText(String.format("Point Radius: %.2f", value_point_radius));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBar_curvature_precision = (SeekBar)view_fragment.findViewById(R.id.seekBar4);
        seekBar_curvature_precision.setMax(50);
        textView_curvature_precision = (TextView) view_fragment.findViewById(R.id.textView4);
        seekBar_curvature_precision.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                value_curvature_precision = progress / 50.0;
                textView_curvature_precision.setText("Curvature Precision: " + value_curvature_precision);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        // Inflate the layout for this fragment
        return view_fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {

        public void onFragmentInteraction(Uri uri);

    }

}
