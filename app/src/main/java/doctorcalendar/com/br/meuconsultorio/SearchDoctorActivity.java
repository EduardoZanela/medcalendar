package doctorcalendar.com.br.meuconsultorio;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import doctorcalendar.com.br.meuconsultorio.entity.Doctor;

public class SearchDoctorActivity extends BaseActivity {

    private EditText mSearchField;
    private RecyclerView mResultsList;
    private FirebaseRecyclerAdapter<Doctor, UsersViewHolder> firestoreRecyclerAdapter;

    private static final String TAG = "Firelog";
    private DatabaseReference mDoctorDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_doctor);

        //find components from view
        mResultsList = findViewById(R.id.results_list);
        mSearchField = findViewById(R.id.search_field);

        mDoctorDatabase = FirebaseDatabase.getInstance().getReference("doctor");
        mResultsList.setLayoutManager(new LinearLayoutManager(this));

        mSearchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                firebaseDoctorSearch(s.toString());
            }
        });

        firebaseDoctorSearch("");
    }

    public void firebaseDoctorSearch(String filter){

        Query firebaseQuery = mDoctorDatabase;
        if(!filter.isEmpty()) {
            firebaseQuery = mDoctorDatabase.orderByChild("name").startAt(filter).endAt(filter + "\uf8ff");
        }

        FirebaseRecyclerOptions<Doctor> options =
                new FirebaseRecyclerOptions.Builder<Doctor>()
                        .setQuery(firebaseQuery, Doctor.class)
                        .build();

        firestoreRecyclerAdapter =
                new FirebaseRecyclerAdapter<Doctor, UsersViewHolder>(options) {

            @NonNull
            @Override
            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.doctors_list, parent, false);
                return new UsersViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull UsersViewHolder holder, int position, @NonNull Doctor model) {
                holder.setDetails(getApplicationContext(),model);
                getRef(position).getKey();
                holder.setDoctor(model);
            }
        };

        firestoreRecyclerAdapter.startListening();
        mResultsList.setAdapter(firestoreRecyclerAdapter);
    }

    public class UsersViewHolder extends RecyclerView.ViewHolder implements  View.OnClickListener{

        View mView;
        Doctor doctor;

        public UsersViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            itemView.setOnClickListener(this);
        }

        public void setDetails(Context applicationContext, Doctor model){
            ImageView profilePicture = mView.findViewById(R.id.profile_picture);
            TextView name = mView.findViewById(R.id.doctor_name);
            TextView specialization = mView.findViewById(R.id.doctor_specialization);
            TextView healthInsurance = mView.findViewById(R.id.health_insurance);

            name.setText(model.getName());
            specialization.setText(model.getSpecialization());
            Glide.with(applicationContext).load(model.getPicture()).into(profilePicture);
            //healthInsurance.setText(String.join(",", model.getHealth_insurance().toArray().toString()));
            String result = ("" + Arrays.asList(model.getHealth_insurance())).replaceAll("(^\\[\\[|\\]\\]$)", "").replace(", ", ", " );
            healthInsurance.setText(getString(R.string.insurances) + " " + result);
        }
        @Override
        public void onClick(View v) {
            getAdapterPosition();
            scheduleConsultation(v, doctor, getAdapterPosition());
        }

        public void scheduleConsultation(View view, Doctor doc, int position) {

            Intent intent = new Intent(SearchDoctorActivity.this, ConsultationActivity.class);
            intent.putExtra("doctor", doc);
            intent.putExtra("position", position);
            startActivity(intent);
        }

        public Doctor getDoctor() {
            return doctor;
        }

        public void setDoctor(Doctor doctor) {
            this.doctor = doctor;
        }
    }
}