package doctorcalendar.com.br.meuconsultorio.entity;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.Calendar;

public class ConsultationUser implements Serializable {

    private String health_insurance;
    private String symptoms;
    private int doctor;
    private String time;
    private Calendar date;

    public ConsultationUser() {

    }

    public ConsultationUser(String health_insurance, String symptoms, int doctor, String time, Calendar date) {
        this.health_insurance = health_insurance;
        this.symptoms = symptoms;
        this.doctor = doctor;
        this.time = time;
        this.date = date;
    }

    public String getHealth_insurance() {
        return health_insurance;
    }

    public void setHealth_insurance(String health_insurance) {
        this.health_insurance = health_insurance;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public int getDoctor() {
        return doctor;
    }

    public void setDoctor(int doctor) {
        this.doctor = doctor;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    private String getDateAsFirebaseKey(Calendar date) {
        return String.valueOf(date.get(Calendar.DAY_OF_MONTH) + "_" + (date.get(Calendar.MONTH) + 1) + "_" + date.get(Calendar.YEAR));
    }

    public String getDate() {
        return getDateAsFirebaseKey(date);
    }

    @Exclude
    public Calendar getCalendarDate(){
        return date;
    }

    public void setDate(String dateString) {
        String[] split = dateString.split("_");
        Calendar date = Calendar.getInstance();
        date.set(Calendar.YEAR, Integer.valueOf(split[2]));
        date.set(Calendar.MONTH, Integer.valueOf(split[1])-1);
        date.set(Calendar.DAY_OF_MONTH, Integer.valueOf(split[0]));
        this.date = date;
    }
}
