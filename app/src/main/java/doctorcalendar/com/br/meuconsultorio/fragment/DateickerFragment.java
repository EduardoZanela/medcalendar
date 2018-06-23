package doctorcalendar.com.br.meuconsultorio.fragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

import java.util.Calendar;

public class DateickerFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Calendar date = Calendar.getInstance();
        return new DatePickerDialog(
                getActivity(),
                (DatePickerDialog.OnDateSetListener) getActivity(),
                date.get(Calendar.YEAR),
                date.get(Calendar.MONTH),
                date.get(Calendar.DAY_OF_MONTH));
    }
}
