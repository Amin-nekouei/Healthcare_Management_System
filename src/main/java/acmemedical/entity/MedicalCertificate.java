/********************************************************************************************************
 * File:  MedicalCertificate.java Course Materials CST 8277
 *
 * @author Teddy Yap
 * 
 */
package acmemedical.entity;

import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import static acmemedical.utility.MyConstants.PARAM1;

@SuppressWarnings("unused")

/**
 * The persistent class for the medical_certificate database table.
 */
@Entity 
@Table(name = "medical_certificate")
@AttributeOverride(name = "id", column = @Column(name = "certificate_id"))
@NamedQuery(name = MedicalCertificate.ID_CARD_QUERY_NAME,
	query = "SELECT mc FROM MedicalCertificate mc WHERE mc.id = :" + PARAM1)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class MedicalCertificate extends PojoBase implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public static final String ID_CARD_QUERY_NAME = "MedicalCertificate.ID_CARD_QUERY_NAME";
    
    @OneToOne
    @JoinColumn(name = "training_id", referencedColumnName = "training_id")
    @JsonIgnore  // Prevent full serialization of the training object
    private MedicalTraining medicalTraining;

    //@ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "physician_id", referencedColumnName = "id", nullable = false)
    @JsonIgnore // Prevent full serialization of the Physician object
    private Physician owner;

    @Column(name = "signed", nullable = false)
    private byte signed;

    public MedicalCertificate() {
        super();
    }

    // Add these methods to handle JSON deserialization
    @JsonProperty("owner")
    public void setOwnerFromJson(Map<String, Object> ownerMap) {
        if (ownerMap != null && ownerMap.containsKey("id")) {
            Physician physician = new Physician();
            physician.setId(((Number)ownerMap.get("id")).intValue());
            if (ownerMap.containsKey("version")) {
                physician.setVersion(((Number)ownerMap.get("version")).intValue());
            }
            this.owner = physician;
        }
    }

    @JsonProperty("medicalTraining")
    public void setMedicalTrainingFromJson(Map<String, Object> trainingMap) {
        if (trainingMap != null && trainingMap.containsKey("id")) {
            MedicalTraining training = new MedicalTraining();
            training.setId(((Number)trainingMap.get("id")).intValue());
            if (trainingMap.containsKey("version")) {
                training.setVersion(((Number)trainingMap.get("version")).intValue());
            }
            this.medicalTraining = training;
        }
    }
    
    public MedicalCertificate(MedicalTraining medicalTraining, Physician owner, byte signed) {
        this();
        this.medicalTraining = medicalTraining;
        this.owner = owner;
        this.signed = signed;
    }

    public MedicalTraining getMedicalTraining() {
        return medicalTraining;
    }

    @JsonProperty("training_id")
    public Long getTrainingId() {
        return medicalTraining != null ? Long.valueOf(medicalTraining.getId()) : null;
    }

    @JsonProperty("owner_id")
    public Long getOwnerId() {
        return owner != null ? Long.valueOf(owner.getId()) : null;
    }

    public void setMedicalTraining(MedicalTraining medicalTraining) {
        this.medicalTraining = medicalTraining;
    }

    public Physician getOwner() {
        return owner;
    }

    public void setOwner(Physician owner) {
        this.owner = owner;
    }

    public byte getSigned() {
        return signed;
    }

    public void setSigned(byte signed) {
        this.signed = signed;
    }

    @JsonIgnore
    public void setSigned(boolean signed) {
        this.signed = (byte) (signed ? 0b0001 : 0b0000);
    }
}