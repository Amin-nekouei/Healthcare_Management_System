/********************************************************************************************************
 * File:  MedicalSchool.java Course Materials CST 8277
 *
 * @author Teddy Yap
 * 
 */
package acmemedical.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * The persistent class for the medical_school database table.
 */
//Add the missing annotations.
//MedicalSchool has subclasses PublicSchool and PrivateSchool.  Look at Week 9 slides for InheritanceType.
//Do we need a mapped super class?  If so, which one?
//Add in JSON annotations to indicate different sub-classes of MedicalSchool
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity (name = "MedicalSchool")
@Table(name = "medical_school")
@AttributeOverride(name = "id", column = @Column(name = "school_id"))
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)  
@DiscriminatorColumn(name = "public", discriminatorType = DiscriminatorType.INTEGER,columnDefinition = "BIT(1)")  
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "public", visible = true)
@JsonSubTypes({
	@JsonSubTypes.Type(value = PublicSchool.class, name="true"),
	@JsonSubTypes.Type(value = PrivateSchool.class, name="false")
})
@NamedQueries({
	@NamedQuery(name = "MedicalSchool.ALL_MEDICAL_SCHOOLS_QUERY_NAME",
            query = "SELECT distinct ms FROM MedicalSchool ms LEFT JOIN FETCH ms.medicalTrainings"),
    @NamedQuery(name = "MedicalSchool.IS_DUPLICATE_QUERY_NAME", 
                query = "SELECT COUNT(ms) FROM MedicalSchool ms WHERE ms.name = :param1"),
    @NamedQuery(name = "MedicalSchool.SPECIFIC_MEDICAL_SCHOOL_QUERY_NAME", 
                query = "SELECT ms FROM MedicalSchool ms WHERE ms.id = :param1")
})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public abstract class MedicalSchool extends PojoBase implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	//added a variable by JK
	public static final String ALL_MEDICAL_SCHOOLS_QUERY_NAME = "MedicalSchool.ALL_MEDICAL_SCHOOLS_QUERY_NAME";
	public static final String SPECIFIC_MEDICAL_SCHOOL_QUERY_NAME = "MedicalSchool.SPECIFIC_MEDICAL_SCHOOL_QUERY_NAME";
	public static final String IS_DUPLICATE_QUERY_NAME = "MedicalSchool.IS_DUPLICATE_QUERY_NAME";
	
	@Column(name = "name", unique = true, nullable = false)
	private String name;

	@OneToMany(mappedBy = "school", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JsonIgnore
	private Set<MedicalTraining> medicalTrainings = new HashSet<>();

	@Column(name = "public", insertable = false, updatable = false)
	private boolean isPublic;

	public MedicalSchool() {
		super();
	}

    public MedicalSchool(boolean isPublic) {
        this();
        this.isPublic = isPublic;
    }

	public Set<MedicalTraining> getMedicalTrainings() {
		return medicalTrainings;
	}

	public void setMedicalTrainings(Set<MedicalTraining> medicalTrainings) {
		this.medicalTrainings = medicalTrainings;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	//Inherited hashCode/equals is NOT sufficient for this entity class
	
	/**
	 * Very important:  Use getter's for member variables because JPA sometimes needs to intercept those calls<br/>
	 * and go to the database to retrieve the value
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		// Only include member variables that really contribute to an object's identity
		// i.e. if variables like version/updated/name/etc. change throughout an object's lifecycle,
		// they shouldn't be part of the hashCode calculation
		
		// The database schema for the MEDICAL_SCHOOL table has a UNIQUE constraint for the NAME column,
		// so we should include that in the hash/equals calculations
		
		return prime * result + Objects.hash(getId(), getName());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		
		if (obj instanceof MedicalSchool otherMedicalSchool) {
			// See comment (above) in hashCode():  Compare using only member variables that are
			// truly part of an object's identity
			return Objects.equals(this.getId(), otherMedicalSchool.getId()) &&
				Objects.equals(this.getName(), otherMedicalSchool.getName());
		}
		return false;
	}
}
