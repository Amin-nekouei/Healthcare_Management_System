/********************************************************************************************************
 * File:  Physician.java Course Materials CST 8277
 *
 * @author Teddy Yap
 * 
 */
package acmemedical.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * The persistent class for the physician database table.
 */
@SuppressWarnings("unused")

//TODO PH01 - Add the missing annotations.
@Entity
@Table(name = "physician")
@AttributeOverride(name = "id", column = @Column(name = "id"))
//TODO PH02 - Do we need a mapped super class? If so, which one?
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) 
@NamedQuery(name = "Physician.ALL_PHYSICIANS_QUERY_NAME", 
            query = "SELECT p FROM Physician p")
// @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Physician extends PojoBase implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public static final String FIND_BY_ID = "MedicalTraining.FIND_BY_ID"; //add by JK
	public static final String ALL_PHYSICIANS_QUERY_NAME = "Physician.ALL_PHYSICIANS_QUERY_NAME";

    public Physician() {
    	super();
    }

	// TODO PH03 - Add annotations.
    @Column(name = "first_name", nullable = false)
	private String firstName;

	// TODO PH04 - Add annotations.
    @Column(name = "last_name", nullable = false) 
	private String lastName;

	// TODO PH05 - Add annotations for 1:M relation.  What should be the cascade and fetch types?
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  
	@JsonIgnore  
	private Set<MedicalCertificate> medicalCertificates = new HashSet<>();

	// TODO PH06 - Add annotations for 1:M relation.  What should be the cascade and fetch types?
    @OneToMany(mappedBy = "physician", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
@JsonManagedReference("physician-prescriptions")
	private Set<Prescription> prescriptions = new HashSet<>();
	@OneToMany(mappedBy = "physician", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JsonIgnore
	private Set<SecurityUser> securityUsers = new HashSet<>();
	
	public Set<SecurityUser> getSecurityUsers() {
		return securityUsers;
	}
	
	public void setSecurityUsers(Set<SecurityUser> securityUsers) {
		this.securityUsers = securityUsers;
	}
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	// TODO PH07 - Is an annotation needed here?
    public Set<MedicalCertificate> getMedicalCertificates() {
		return medicalCertificates;
	}

	public void setMedicalCertificates(Set<MedicalCertificate> medicalCertificates) {
		this.medicalCertificates = medicalCertificates;
	}

	// TODO PH08 - Is an annotation needed here?
    public Set<Prescription> getPrescriptions() {
		return prescriptions;
	}

	public void setPrescriptions(Set<Prescription> prescriptions) {
		this.prescriptions = prescriptions;
	}

	public void setFullName(String firstName, String lastName) {
		setFirstName(firstName);
		setLastName(lastName);
	}
	
	//Inherited hashCode/equals is sufficient for this entity class

}
