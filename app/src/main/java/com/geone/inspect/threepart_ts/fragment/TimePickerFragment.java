package com.geone.inspect.threepart_ts.fragment;

import java.util.Calendar;
import java.util.TimeZone;

import com.geone.inspect.threepart_ts.util.Utils;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

public class TimePickerFragment extends DialogFragment implements
		TimePickerDialog.OnTimeSetListener {

	public Object target;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		EditText mEditText = ((EditText) target);
		Calendar c;
		int hour, minute;
		if (!"".equals(mEditText.getText().toString())) {
			String date_string = mEditText.getText().toString();
			c = Utils.stringToCalendar(date_string, "HH:mm",
					TimeZone.getDefault());
		} else {
			c = Calendar.getInstance();
		}

		hour = c.get(Calendar.HOUR_OF_DAY);
		minute = c.get(Calendar.MINUTE);

		// Create a new instance of TimePickerDialog and return it
		return new TimePickerDialog(getActivity(), this, hour, minute,
				DateFormat.is24HourFormat(getActivity()));
	}

	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, hourOfDay);
		c.set(Calendar.MINUTE, minute);
		String formattedTime = Utils.formatDate(c.getTime(), "HH:mm");

		TextView et = (TextView) target;
		et.setText(formattedTime);
		et.setError(null);
	}
}