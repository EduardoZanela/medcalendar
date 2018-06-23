package doctorcalendar.com.br.meuconsultorio.entity;

import java.io.Serializable;
import java.util.List;

public class Doctor implements Serializable {

    private static final long serialVersionUID = -6676662102295310662L;
    private String name;
    private String picture;
    private String specialization;
    private String address;
    private List<List<String>> times;
    private List<String> health_insurance;

    public Doctor(String name, String picture, String specialization, List<List<String>> times, List<String> health_insurance) {
        this.name = name;
        this.picture = picture;
        this.specialization = specialization;
        this.times = times;
        this.health_insurance = health_insurance;
    }

    public Doctor() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public List<List<String>> getTimes() {
        return times;
    }

    public void setTimes(List<List<String>> times) {
        this.times = times;
    }

    public List<String> getHealth_insurance() {
        return health_insurance;
    }

    public void setHealth_insurance(List<String> health_insurance) {
        this.health_insurance = health_insurance;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
