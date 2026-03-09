package com.medicalml.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import com.fasterxml.jackson.annotation.JsonIgnore;

/*
 * Entity class representing biomarker readings of a patient.
 * These values are used for medical analysis and prediction of
 * cardiovascular diseases such as Myocardial Infarction.
 */

@Entity
@Table(name = "biomarkers")
public class Biomarker {

    // Primary key for the biomarker record
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Total cholesterol level (mg/dL)
    // Normal range roughly: 125–200
    @Column(name = "cholesterol")
    @Min(50)
    @Max(400)
    private double cholesterol;

    // Systolic blood pressure (top number)
    // Example: 120 in 120/80
    @Column(name = "systolic_pressure")
    @Min(70)
    @Max(250)
    private int systolicPressure;

    // Diastolic blood pressure (bottom number)
    // Example: 80 in 120/80
    @Column(name = "diastolic_pressure")
    @Min(40)
    @Max(150)
    private int diastolicPressure;

    // Heart rate in beats per minute
    // Normal resting range: 60–100 bpm
    @Column(name = "heart_rate")
    @Min(30)
    @Max(220)
    private double heartRate;

    // Blood glucose level (mg/dL)
    // Used to detect diabetes risk
    @Column(name = "glucose")
    @Min(40)
    @Max(500)
    private double glucose;

    /*
     * Many biomarker records can belong to one patient.
     * This establishes the relationship between Biomarker and Patient.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")

    // Prevents infinite JSON recursion when sending API responses
    @JsonIgnore
    private Patient patient;

    // Default constructor required by JPA
    public Biomarker() {}

    /*
     * Constructor used to create biomarker objects
     * when inserting new medical readings.
     */
    public Biomarker(double cholesterol, int systolicPressure,
                     int diastolicPressure, double heartRate,
                     double glucose, Patient patient) {

        this.cholesterol = cholesterol;
        this.systolicPressure = systolicPressure;
        this.diastolicPressure = diastolicPressure;
        this.heartRate = heartRate;
        this.glucose = glucose;
        this.patient = patient;
    }

    // Getter for biomarker ID
    public Long getId() {
        return id;
    }

    // Getter and Setter for cholesterol
    public double getCholesterol() {
        return cholesterol;
    }

    public void setCholesterol(double cholesterol) {
        this.cholesterol = cholesterol;
    }

    // Getter and Setter for systolic blood pressure
    public int getSystolicPressure() {
        return systolicPressure;
    }

    public void setSystolicPressure(int systolicPressure) {
        this.systolicPressure = systolicPressure;
    }

    // Getter and Setter for diastolic blood pressure
    public int getDiastolicPressure() {
        return diastolicPressure;
    }

    public void setDiastolicPressure(int diastolicPressure) {
        this.diastolicPressure = diastolicPressure;
    }

    // Getter and Setter for heart rate
    public double getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(double heartRate) {
        this.heartRate = heartRate;
    }

    // Getter and Setter for glucose level
    public double getGlucose() {
        return glucose;
    }

    public void setGlucose(double glucose) {
        this.glucose = glucose;
    }

    // Getter and Setter for the associated patient
    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }
}
