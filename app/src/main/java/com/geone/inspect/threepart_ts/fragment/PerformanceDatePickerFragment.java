package com.geone.inspect.threepart_ts.fragment;

import java.util.Calendar;
import java.util.TimeZone;

import com.geone.inspect.threepart_ts.util.Utils;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

public class PerformanceDatePickerFragment extends DialogFragment implements
		DatePickerDialog.OnDateSetListener {

	DateSelectListener mDateSelectListener;
	public Object target;

	public interface DateSelectListener {
		public void dateSelectCompleted(String date);
	}
	@Override
	public void onAttach(Activity activity) {		
		super.onAttach(activity);
		mDateSelectListener=(DateSelectListener) activity;//该接口在PerformanceActivity中被实现
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		EditText mEditText = ((EditText) target);
		Calendar c;
		int year, month, day;
		if (!"".equals(mEditText.getText().toString())) {
			String date_string = mEditText.getText().toString();
			c = Utils.stringToCalendar(date_string, "yyyy-MM-dd",
					TimeZone.getDefault());
		} else {
			c = Calendar.getInstance();
		}

		year = c.get(Calendar.YEAR);
		month = c.get(Calendar.MONTH);
		day = c.get(Calendar.DAY_OF_MONTH);

		// Create a new instance of DatePickerDialog and return it
		return new DatePickerDialog(getActivity(), this, year, month, day);
	}

	public void onDateSet(DatePicker view, int year, int month, int day) {
		Calendar c = Calendar.getInstance();
		c.set(year, month, day);

		// String formattedDate = Utils.formatDate(c.getTime(), "yyyy-MM-dd");
		String formattedDate = Utils.formatDate(c.getTime(), "yyyy-MM");

		Log.d("PerformanceDatePickerFragment", "--onDateSet: " + formattedDate);

		TextView et = (TextView) target;
		et.setText(formattedDate);
		et.setError(null);

		if (mDateSelectListener != null) {
			mDateSelectListener.dateSelectCompleted(formattedDate);

		}
	}
}
