package es.unex.heatmapsc.datemanager;



import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import java.util.Calendar;

import es.unex.heatmapsc.MainActivity;


/**
 * Created by jgaralo on 11/10/17.
 */

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    private String date;

    private FragmentActivity myContext;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        this.date=getArguments().getString("date");

        Dialog timeDialog= new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
        if(date.equals("start")) {
            timeDialog.setTitle("Pick start date");
        }
        else{
            timeDialog.setTitle("Pick end date");
        }
        return timeDialog;
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        if(date.equals("start")) {
            ((MainActivity) getActivity()).setStartTime(hourOfDay, minute);
            Bundle bundle = new Bundle();
            bundle.putString("date", "end");
            DialogFragment newFragment = new DatePickerFragment();
            newFragment.setArguments(bundle);
            newFragment.show(myContext.getSupportFragmentManager(), "datePicker");
        }
        else{
            ((MainActivity) getActivity()).setEndTime(hourOfDay, minute);
            ((MainActivity) getActivity()).getHeatMap();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentActivity){
            myContext=(FragmentActivity) context;
        }
    }
}
