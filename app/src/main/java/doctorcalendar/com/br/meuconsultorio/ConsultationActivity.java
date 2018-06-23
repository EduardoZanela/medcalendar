package doctorcalendar.com.br.meuconsultorio;

import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import doctorcalendar.com.br.meuconsultorio.entity.Consultation;
import doctorcalendar.com.br.meuconsultorio.entity.ConsultationUser;
import doctorcalendar.com.br.meuconsultorio.entity.Doctor;
import doctorcalendar.com.br.meuconsultorio.fragment.DateickerFragment;

public class ConsultationActivity extends BaseActivity implements DatePickerDialog.OnDateSetListener {

    public static final String CONSULTATION_TIMES = "consultation_times";
    public static final String USERS = "users";
    public static final String CONSULTATIONS = "consultations";
    public static final String NOTHING = "nothing";
    public static final String DOCTOR = "doctor";
    public static final String POSITION = "position";
    private Doctor mDoctor;
    private int mPositionId;
    private EditText mDatePicker;
    private Spinner mSpinnerTime;
    private Spinner mSpinnerInsurance;
    private EditText mSymptoms;
    private Button mSchedule;
    private FirebaseAuth mAuth;


    private Calendar selectedDate;

    private DatabaseReference mDoctorDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consultation);

        mAuth = FirebaseAuth.getInstance();

        mDoctor = (Doctor) getIntent().getSerializableExtra(DOCTOR);
        mPositionId = getIntent().getIntExtra(POSITION, 1);

        setDetails(mDoctor);

        mDatePicker = findViewById(R.id.date_picker);
        mSpinnerTime = findViewById(R.id.spin_time);
        mSpinnerInsurance = findViewById(R.id.spin_insurence);
        mSymptoms = findViewById(R.id.symptoms);
        mSchedule = findViewById(R.id.schedule);

        mDatePicker.setKeyListener(null);
        mDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment datePicker = new DateickerFragment();
                datePicker.show(getFragmentManager(), "date picker");
            }
        });

        mSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveConsultation();
            }
        });

        startPicker(Calendar.getInstance());
        fillInsurance();
    }

    private void startPicker(Calendar instance) {
        setPickerDate(instance);
    }

    public void saveConsultation() {

        DatabaseReference child = FirebaseDatabase.getInstance().getReference(USERS)
                .child(String.valueOf(mAuth.getCurrentUser().getUid()))
                .child(CONSULTATIONS)
                .child(getDateAsFirebaseKey(selectedDate));

        child.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.hasChild(mSpinnerTime.getSelectedItem().toString())) {

                    FirebaseUser currentUser = mAuth.getCurrentUser();

                    ConsultationUser consultationUser = new ConsultationUser(mSpinnerInsurance.getSelectedItem().toString(), mSymptoms.getText().toString(), mPositionId, mSpinnerTime.getSelectedItem().toString(), selectedDate);

                    String key =  FirebaseDatabase.getInstance().getReference(CONSULTATIONS)
                            .child(String.valueOf(mAuth.getCurrentUser().getUid()))
                            .push().getKey();

                    FirebaseDatabase.getInstance().getReference(CONSULTATION_TIMES)
                            .child(String.valueOf(mPositionId))
                            .child(getDateAsFirebaseKey(selectedDate))
                            .child(mSpinnerTime.getSelectedItem().toString())
                            .child(currentUser.getUid().toString())
                            .child(key)
                            .setValue(true);

                    FirebaseDatabase.getInstance().getReference(CONSULTATIONS)
                            .child(String.valueOf(mAuth.getCurrentUser().getUid()))
                            .child(key)
                            .setValue(consultationUser);

                    FirebaseDatabase.getInstance().getReference(USERS)
                            .child(String.valueOf(mAuth.getCurrentUser().getUid()))
                            .child(CONSULTATIONS)
                            .child(getDateAsFirebaseKey(selectedDate))
                            .child(mSpinnerTime.getSelectedItem().toString())
                            .child(key)
                            .setValue(true);

                    Intent intent = new Intent(ConsultationActivity.this, MainActivity.class);
                    startActivity(intent);
                } else{
                    Snackbar.make(findViewById(android.R.id.content), R.string.already_have_consultation, Snackbar.LENGTH_LONG).show();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void findFreeTimes(final Calendar date) {

        // Busca no dia selecionado os horarios das consultas que ja foram agendadas
        mDoctorDatabase = FirebaseDatabase.getInstance()
                .getReference(CONSULTATION_TIMES)
                .child(String.valueOf(mPositionId))
                .child(getDateAsFirebaseKey(date));


        mDoctorDatabase.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> mFreeTime = new ArrayList<>();

                // Valida busca de horarios por dia da semana
                // Firebase item 0 -> domingo e assim por diante
                // "nothing" significa que naquele dia não possui horarios
                if(mDoctor.getTimes().size() >= date.get(Calendar.DAY_OF_WEEK)) {
                    mFreeTime = new ArrayList<>(mDoctor.getTimes().get(date.get(Calendar.DAY_OF_WEEK)-1));
                    mFreeTime.remove(NOTHING);
                }
                // Percore os horarios em que o doutor atende e remove os que ja foram agendados
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    mFreeTime.remove(snapshot.getKey());
                }
                if (mFreeTime.size() == 0) {
                    disableEnableScheduleButton(false, R.string.no_more_free_times, R.drawable.edit_text_consultation_disable);
                } else {
                    disableEnableScheduleButton(true, R.string.schedule, R.drawable.edit_text_consultation);
                }

                fillTimeSpinner(mFreeTime);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void disableEnableScheduleButton(boolean isEnable, int text, int resource) {
        mSpinnerTime.setEnabled(isEnable);
        mSpinnerInsurance.setEnabled(isEnable);
        mSymptoms.setEnabled(isEnable);
        mSchedule.setEnabled(isEnable);
        mSchedule.setText(text);
        mSchedule.setBackgroundResource(resource);
    }

    private String getDateAsFirebaseKey(Calendar date) {
        return String.valueOf(date.get(Calendar.DAY_OF_MONTH) + "_" + (date.get(Calendar.MONTH) + 1) + "_" + date.get(Calendar.YEAR));
    }

    public void fillTimeSpinner(List<String> times) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, times);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerTime.setAdapter(adapter);
    }

    public void fillInsurance() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mDoctor.getHealth_insurance());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerInsurance.setAdapter(adapter);
    }

    public void setDetails(Doctor model) {
        ImageView profilePicture = findViewById(R.id.profile_picture);
        TextView name = findViewById(R.id.doctor_name);
        TextView specialization = findViewById(R.id.doctor_specialization);

        name.setText(model.getName());
        specialization.setText(model.getSpecialization());
        Glide.with(getApplicationContext()).load(model.getPicture()).into(profilePicture);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar date = Calendar.getInstance();
        date.set(Calendar.YEAR, year);
        date.set(Calendar.MONTH, month);
        date.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        setPickerDate(date);

    }

    private void setPickerDate(Calendar date) {
        selectedDate = date;
        findFreeTimes(date);
        String dateString = new SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()).format(date.getTime());
        mDatePicker.setText(dateString);
    }
}
