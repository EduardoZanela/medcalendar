package doctorcalendar.com.br.meuconsultorio.entity;

import java.io.Serializable;
import java.util.Date;

public class Consultation implements Serializable {

    private String health_insurance;
    private String symptoms;

    public Consultation() {

    }

    public Consultation(String health_insurance, String symptoms) {
        this.health_insurance = health_insurance;
        this.symptoms = symptoms;
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

}
