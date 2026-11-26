/********************************************************************************************************
 * File:  PublicSchool.java Course materials CST 8277
 *
 * @author Teddy Yap
 * @author Shariar (Shawn) Emami
 * 
 */
package acmemedical.entity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonTypeName;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
// @Table(name = "medical_school")
@DiscriminatorValue("1") // Value 1 is public
@JsonTypeName("true")
public class PublicSchool extends MedicalSchool implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public PublicSchool() {
		super(true);
	}
}
