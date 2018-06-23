package doctorcalendar.com.br.meuconsultorio;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

import doctorcalendar.com.br.meuconsultorio.entity.ConsultationUser;
import doctorcalendar.com.br.meuconsultorio.entity.Doctor;

public class MainActivity extends BaseActivity {

    private RecyclerView mResultsList;
    private FirebaseRecyclerAdapter<ConsultationUser, MainActivity.UsersViewHolder> firestoreRecyclerAdapter;

    private static final String TAG = "Firelog";
    private DatabaseReference mDoctorDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //find components from view
        mResultsList = findViewById(R.id.results_list);

        mDoctorDatabase = FirebaseDatabase.getInstance().getReference("consultations")
                .child(mAuth.getCurrentUser().getUid().toString());
        mResultsList.setLayoutManager(new LinearLayoutManager(this));

        firebaseConsultationList();
    }

    public void firebaseConsultationList(){


        FirebaseRecyclerOptions<ConsultationUser> options =
                new FirebaseRecyclerOptions.Builder<ConsultationUser>()
                        .setQuery(mDoctorDatabase, ConsultationUser.class)
                        .build();

        firestoreRecyclerAdapter =
                new FirebaseRecyclerAdapter<ConsultationUser, MainActivity.UsersViewHolder>(options) {

                    @NonNull
                    @Override
                    public MainActivity.UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.consultations_list, parent, false);
                        return new MainActivity.UsersViewHolder(view);
                    }

                    @Override
                    protected void onBindViewHolder(@NonNull MainActivity.UsersViewHolder holder, int position, @NonNull ConsultationUser model) {
                        if(model.getCalendarDate().after(Calendar.getInstance())) {
                            holder.setKey(getRef(position).getKey());
                            holder.setConsultation(model);
                            holder.setDetailsFindDoctor(getApplicationContext(), model);
                        } else {
                            getRef(position).removeValue();
                        }
                        Log.d(TAG, "AQUI: " + model.getDate());
                    }
                };

        firestoreRecyclerAdapter.startListening();
        mResultsList.setAdapter(firestoreRecyclerAdapter);
    }

    public class UsersViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        View mView;
        private ConsultationUser consultation;
        private String Key;
        private Doctor doc;

        public UsersViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            ImageView button = mView.findViewById(R.id.map);
            button.setOnClickListener(this);
        }

        public void setDetails(final Context applicationContext, final ConsultationUser model, Doctor doctor){
            ImageView profilePicture = mView.findViewById(R.id.profile_picture);
            TextView name = mView.findViewById(R.id.doctor_name);
            TextView specialization = mView.findViewById(R.id.doctor_specialization);
            TextView healthInsurance = mView.findViewById(R.id.health_insurance);
            TextView dateTime = mView.findViewById(R.id.date_time);

            name.setText(doctor.getName());
            specialization.setText(doctor.getSpecialization());
            Glide.with(applicationContext).load(doctor.getPicture()).into(profilePicture);

            String dateString = new SimpleDateFormat("EEE, d MMM yyyy").format(model.getCalendarDate().getTime());

            dateTime.setText(dateString+" "+model.getTime());
            healthInsurance.setText(getString(R.string.insurance_main) + " " + model.getHealth_insurance());
        }

        public void mapView(Doctor doctor){
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" +
                    doctor.getAddress());
            Intent mapIntent = new Intent(
                    Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);

        }

        public void setDetailsFindDoctor(final Context applicationContext, final ConsultationUser model){

            DatabaseReference doctorReference = FirebaseDatabase.getInstance().getReference("doctor")
                    .child(String.valueOf(model.getDoctor()));

            doctorReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Doctor doctor = dataSnapshot.getValue(Doctor.class);
                    setDoc(doctor);
                    setDetails(applicationContext, model, doctor);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        public ConsultationUser getConsultation() {
            return consultation;
        }

        public void setConsultation(ConsultationUser consultation) {
            this.consultation = consultation;
        }

        public String getKey() {
            return Key;
        }

        public void setKey(String key) {
            Key = key;
        }

        @Override
        public void onClick(View v) {
            mapView(doc);
        }

        public void setDoc(Doctor doc) {
            this.doc = doc;
        }
    }
}
