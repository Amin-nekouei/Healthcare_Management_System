/********************************************************************************************************
 * File:  ACMEMedicalService.java Course Materials CST 8277
 *
 * @author Teddy Yap
 * @author Shariar (Shawn) Emami
 * 
 */
package acmemedical.ejb;

import static acmemedical.utility.MyConstants.DEFAULT_KEY_SIZE;
import static acmemedical.utility.MyConstants.DEFAULT_PROPERTY_ALGORITHM;
import static acmemedical.utility.MyConstants.DEFAULT_PROPERTY_ITERATIONS;
import static acmemedical.utility.MyConstants.DEFAULT_SALT_SIZE;
import static acmemedical.utility.MyConstants.DEFAULT_USER_PASSWORD;
import static acmemedical.utility.MyConstants.DEFAULT_USER_PREFIX;
import static acmemedical.utility.MyConstants.PARAM1;
import static acmemedical.utility.MyConstants.PROPERTY_ALGORITHM;
import static acmemedical.utility.MyConstants.PROPERTY_ITERATIONS;
import static acmemedical.utility.MyConstants.PROPERTY_KEY_SIZE;
import static acmemedical.utility.MyConstants.PROPERTY_SALT_SIZE;
import static acmemedical.utility.MyConstants.PU_NAME;
import static acmemedical.utility.MyConstants.USER_ROLE;
import static acmemedical.entity.Physician.ALL_PHYSICIANS_QUERY_NAME;
import static acmemedical.entity.MedicalSchool.ALL_MEDICAL_SCHOOLS_QUERY_NAME;
import static acmemedical.entity.MedicalSchool.IS_DUPLICATE_QUERY_NAME;
import static acmemedical.entity.MedicalSchool.SPECIFIC_MEDICAL_SCHOOL_QUERY_NAME;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.ejb.Singleton;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;
import jakarta.transaction.Transactional;




import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import acmemedical.entity.MedicalTraining;
import acmemedical.entity.Patient;
import acmemedical.entity.MedicalCertificate;
import acmemedical.entity.Medicine;
import acmemedical.entity.Prescription;
import acmemedical.entity.PrescriptionPK;
import acmemedical.entity.SecurityRole;
import acmemedical.entity.SecurityUser;
import acmemedical.entity.Physician;
import acmemedical.entity.MedicalSchool;

@SuppressWarnings("unused")

/**
 * Stateless Singleton EJB Bean - ACMEMedicalService
 */
//@Singleton
@Stateless
public class ACMEMedicalService implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private static final Logger LOG = LogManager.getLogger();
    
    @PersistenceContext(name = "acmemedical-PU") //PU_NAME
    protected EntityManager em;
    
    @Inject
    protected Pbkdf2PasswordHash pbAndjPasswordHash;

    public List<Physician> getAllPhysicians() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Physician> cq = cb.createQuery(Physician.class);
        cq.select(cq.from(Physician.class));
        return em.createQuery(cq).getResultList();
    }

    public Physician getPhysicianById(int id) {
        return em.find(Physician.class, id);
    }

    @Transactional
    public Physician persistPhysician(Physician newPhysician) {
        em.persist(newPhysician);
        return newPhysician;
    }

    @Transactional
    public void buildUserForNewPhysician(Physician newPhysician) {
        try {
            LOG.debug("Building SecurityUser for Physician: {}", newPhysician.getId());
    
            SecurityUser userForNewPhysician = new SecurityUser();
            userForNewPhysician.setUsername("user_" + newPhysician.getFirstName() + "." + newPhysician.getLastName());
            
            Map<String, String> props = new HashMap<>();
            props.put(PROPERTY_ALGORITHM, DEFAULT_PROPERTY_ALGORITHM);
            props.put(PROPERTY_ITERATIONS, DEFAULT_PROPERTY_ITERATIONS);
            props.put(PROPERTY_SALT_SIZE, DEFAULT_SALT_SIZE);
            props.put(PROPERTY_KEY_SIZE, DEFAULT_KEY_SIZE);
            pbAndjPasswordHash.initialize(props);
            
            String pwHash = pbAndjPasswordHash.generate(DEFAULT_USER_PASSWORD.toCharArray());
            userForNewPhysician.setPwHash(pwHash);
            userForNewPhysician.setPhysician(newPhysician);
            
            TypedQuery<SecurityRole> query = em.createNamedQuery(SecurityRole.FIND_BY_NAME, SecurityRole.class);
            query.setParameter("name", USER_ROLE);
            SecurityRole userRole = query.getSingleResult();
            
            userForNewPhysician.getRoles().add(userRole);
            userRole.getUsers().add(userForNewPhysician);
    
            LOG.debug("Persisting SecurityUser: {}", userForNewPhysician.getUsername());
    
            em.persist(userForNewPhysician);
        } catch (Exception e) {
            LOG.error("Error in buildUserForNewPhysician: {}", e.getMessage(), e);
            throw e;
        }
    }
    

    @Transactional
    public Medicine setMedicineForPhysicianPatient(int physicianId, int patientId, Medicine newMedicine) {
        Physician physicianToBeUpdated = em.find(Physician.class, physicianId);
        if (physicianToBeUpdated != null) { // Physician exists
            Set<Prescription> prescriptions = physicianToBeUpdated.getPrescriptions();
            prescriptions.forEach(p -> {
                if (p.getPatient().getId() == patientId) {
                    if (p.getMedicine() != null) { // Medicine exists
                        Medicine medicine = em.find(Medicine.class, p.getMedicine().getId());
                        medicine.setMedicine(newMedicine.getDrugName(),
                        				  newMedicine.getManufacturerName(),
                        				  newMedicine.getDosageInformation());
                        em.merge(medicine);
                    }
                    else { // Medicine does not exist
                        p.setMedicine(newMedicine);
                        em.merge(physicianToBeUpdated);
                    }
                }
            });
            return newMedicine;
        }
        else return null;  // Physician doesn't exists
    }

    /**
     * To update a physician
     * 
     * @param id - id of entity to update
     * @param physicianWithUpdates - entity with updated information
     * @return Entity with updated information
     */
    @Transactional
    public Physician updatePhysicianById(int id, Physician physicianWithUpdates) {
    	Physician physicianToBeUpdated = getPhysicianById(id);
        if (physicianToBeUpdated != null) {
            em.refresh(physicianToBeUpdated);
            em.merge(physicianWithUpdates);
            em.flush();
        }
        return physicianToBeUpdated;
    }

    /**
     * To delete a physician by id
     * 
     * @param id - physician id to delete
     */
    @Transactional
public void deletePhysicianById(int id) {
    Physician physician = getPhysicianById(id);
    if (physician != null) {
        em.refresh(physician);

        TypedQuery<SecurityUser> findUsers = em.createNamedQuery("SecurityUser.FIND_BY_PHYSICIAN_ID", SecurityUser.class);
        findUsers.setParameter("physicianId", id);

        List<SecurityUser> users = findUsers.getResultList();

        for (SecurityUser user : users) {
            em.remove(user);
        }

        em.remove(physician);
    }
}
    
    
    public List<MedicalSchool> getAllMedicalSchools() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<MedicalSchool> cq = cb.createQuery(MedicalSchool.class);
        cq.select(cq.from(MedicalSchool.class));
        return em.createQuery(cq).getResultList();
    }

    // Why not use the build-in em.find?  The named query SPECIFIC_MEDICAL_SCHOOL_QUERY_NAME
    // includes JOIN FETCH that we cannot add to the above API
    public MedicalSchool getMedicalSchoolById(int id) {
        TypedQuery<MedicalSchool> specificMedicalSchoolQuery = em.createNamedQuery(SPECIFIC_MEDICAL_SCHOOL_QUERY_NAME, MedicalSchool.class);
        specificMedicalSchoolQuery.setParameter(PARAM1, id);
        return specificMedicalSchoolQuery.getSingleResult();
    }
    
    // These methods are more generic.

    public <T> List<T> getAll(Class<T> entity, String namedQuery) {
        TypedQuery<T> allQuery = em.createNamedQuery(namedQuery, entity);
        return allQuery.getResultList();
    }
    
    public <T> T getById(Class<T> entity, String namedQuery, int id) {
        TypedQuery<T> allQuery = em.createNamedQuery(namedQuery, entity);
        allQuery.setParameter(PARAM1, id);
        return allQuery.getSingleResult();
    }

    @Transactional
    public MedicalSchool deleteMedicalSchool(int id) {
        //MedicalSchool ms = getMedicalSchoolById(id);
    	MedicalSchool ms = getById(MedicalSchool.class, MedicalSchool.SPECIFIC_MEDICAL_SCHOOL_QUERY_NAME, id);
        if (ms != null) {
            Set<MedicalTraining> medicalTrainings = ms.getMedicalTrainings();
            List<MedicalTraining> list = new LinkedList<>();
            medicalTrainings.forEach(list::add);
            list.forEach(mt -> {
                if (mt.getCertificate() != null) {
                    MedicalCertificate mc = getById(MedicalCertificate.class, MedicalCertificate.ID_CARD_QUERY_NAME, mt.getCertificate().getId());
                    mc.setMedicalTraining(null);
                }
                mt.setCertificate(null);
                em.merge(mt);
            });
            em.remove(ms);
            return ms;
        }
        return null;
    }
    
    // Please study & use the methods below in your test suites
    public boolean isDuplicated(MedicalSchool newMedicalSchool) {
        TypedQuery<Long> allMedicalSchoolsQuery = em.createNamedQuery(IS_DUPLICATE_QUERY_NAME, Long.class);
        allMedicalSchoolsQuery.setParameter(PARAM1, newMedicalSchool.getName());
        return (allMedicalSchoolsQuery.getSingleResult() >= 1);
    }

    @Transactional
    public MedicalSchool persistMedicalSchool(MedicalSchool newMedicalSchool) {
        em.persist(newMedicalSchool);
        return newMedicalSchool;
    }

    @Transactional
    public MedicalSchool updateMedicalSchool(int id, MedicalSchool updatingMedicalSchool) {
    	MedicalSchool medicalSchoolToBeUpdated = getMedicalSchoolById(id);
        if (medicalSchoolToBeUpdated != null) {
            em.refresh(medicalSchoolToBeUpdated);
            medicalSchoolToBeUpdated.setName(updatingMedicalSchool.getName());
            em.merge(medicalSchoolToBeUpdated);
            em.flush();
        }
        return medicalSchoolToBeUpdated;
    }
    
    public MedicalTraining mergeMedicalTraining(MedicalTraining medicalTraining) {
        return em.merge(medicalTraining);
    }
    @Transactional
    public MedicalTraining persistMedicalTraining(MedicalTraining newMedicalTraining) {
        //em.persist(newMedicalTraining);
        em.merge(newMedicalTraining);
        return newMedicalTraining;
    }
    
    public MedicalTraining getMedicalTrainingById(int mtId) {
        TypedQuery<MedicalTraining> allMedicalTrainingQuery = em.createNamedQuery(MedicalTraining.FIND_BY_ID, MedicalTraining.class);
        allMedicalTrainingQuery.setParameter("id", mtId); //Modified Apri02 2025
        return allMedicalTrainingQuery.getSingleResult();
    }

    @Transactional
    public MedicalTraining updateMedicalTraining(int id, MedicalTraining medicalTrainingWithUpdates) {
    	MedicalTraining medicalTrainingToBeUpdated = getMedicalTrainingById(id);
        if (medicalTrainingToBeUpdated != null) {
            em.refresh(medicalTrainingToBeUpdated);
            em.merge(medicalTrainingWithUpdates);
            em.flush();
        }
        return medicalTrainingToBeUpdated;
    }
    
    @Transactional
    public MedicalTraining deleteMedicalTraining(int id) {
        MedicalTraining medicalTraining = getMedicalTrainingById(id);
        if (medicalTraining != null) {
            em.refresh(medicalTraining);
            if (medicalTraining.getCertificate() != null) {
                medicalTraining.getCertificate().setMedicalTraining(null);
            }
            em.remove(medicalTraining);
            return medicalTraining;
        }
        return null;
    }
    
    // MedicalCertificate CRUD operations
    public List<MedicalCertificate> getAllMedicalCertificates() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<MedicalCertificate> cq = cb.createQuery(MedicalCertificate.class);
        cq.select(cq.from(MedicalCertificate.class));
        return em.createQuery(cq).getResultList();
    }

    public MedicalCertificate getMedicalCertificateById(int id) {
        TypedQuery<MedicalCertificate> query = em.createNamedQuery(MedicalCertificate.ID_CARD_QUERY_NAME, MedicalCertificate.class);
        query.setParameter(PARAM1, id);
        return query.getSingleResult();
    }

    @Transactional
    public MedicalCertificate persistMedicalCertificate(MedicalCertificate newMedicalCertificate) {
        em.persist(newMedicalCertificate);
        return newMedicalCertificate;
    }

    @Transactional
    public MedicalCertificate updateMedicalCertificate(int id, MedicalCertificate updatingMedicalCertificate) {
        MedicalCertificate medicalCertificateToBeUpdated = getMedicalCertificateById(id);
        if (medicalCertificateToBeUpdated != null) {
            updatingMedicalCertificate.setId(id);
            updatingMedicalCertificate.setVersion(medicalCertificateToBeUpdated.getVersion());
            medicalCertificateToBeUpdated= em.merge(updatingMedicalCertificate);
            em.flush();
        }
        return medicalCertificateToBeUpdated;
    }

    @Transactional
    public void deleteMedicalCertificateById(int id) {
        MedicalCertificate medicalCertificate = getMedicalCertificateById(id);
        if (medicalCertificate != null) {
            em.refresh(medicalCertificate);
            em.remove(medicalCertificate);
        }
    }
    
    // Patient CRUD operations
    public List<Patient> getAllPatients() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Patient> cq = cb.createQuery(Patient.class);
        cq.select(cq.from(Patient.class));
        return em.createQuery(cq).getResultList();
    }

    public Patient getPatientById(int id) {
        return em.find(Patient.class, id);
    }

    @Transactional
    public Patient persistPatient(Patient newPatient) {
        em.persist(newPatient);
        return newPatient;
    }

    @Transactional
    public Patient updatePatient(int id, Patient updatingPatient) {
        Patient patientToBeUpdated = getPatientById(id);
        if (patientToBeUpdated != null) {
            updatingPatient.setId(id);
            updatingPatient.setVersion(patientToBeUpdated.getVersion());
            patientToBeUpdated = em.merge(updatingPatient);
            em.flush();
        }
        return patientToBeUpdated;
    }

    @Transactional
    public void deletePatientById(int id) {
        Patient patient = getPatientById(id);
        if (patient != null) {
            em.remove(patient);
        }
    }
    // Get all medicines
public List<Medicine> getAllMedicines() {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Medicine> cq = cb.createQuery(Medicine.class);
    cq.select(cq.from(Medicine.class));
    return em.createQuery(cq).getResultList();
}

// Get medicine by ID
public Medicine getMedicineById(int id) {
    return em.find(Medicine.class, id);
}

// Create new medicine
@Transactional
public Medicine persistMedicine(Medicine newMedicine) {
    em.persist(newMedicine);
    return newMedicine;
}

// Update existing medicine
@Transactional
public Medicine updateMedicine(Medicine updatedMedicine) {
    Medicine existingMedicine = getMedicineById(updatedMedicine.getId());
    if (existingMedicine != null) {
        existingMedicine.setMedicine(
            updatedMedicine.getDrugName(),
            updatedMedicine.getManufacturerName(),
            updatedMedicine.getDosageInformation()
        );
        em.merge(existingMedicine);
        em.flush();
        return existingMedicine;
    }
    return null;
}

// Delete medicine
@Transactional
public void deleteMedicineById(int id) {
    Medicine medicine = getMedicineById(id);
    if (medicine != null) {
        em.remove(medicine);
    }
}

  // Prescription CRUD operations
    public List<Prescription> getAllPrescriptions() {
        LOG.debug("Getting all prescriptions");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Prescription> cq = cb.createQuery(Prescription.class);
        Root<Prescription> root = cq.from(Prescription.class);
        cq.select(root);
        TypedQuery<Prescription> q = em.createQuery(cq);
        return q.getResultList();
    }

    public Prescription getPrescriptionById(int patientId, int physicianId) {
        LOG.debug("Getting prescription for patient={} and physician={}", patientId, physicianId);
        PrescriptionPK id = new PrescriptionPK();
        id.setPatientId(patientId);
        id.setPhysicianId(physicianId);
        return em.find(Prescription.class, id);
    }

    @Transactional
    public Prescription persistPrescription(Prescription prescription) {
        if (prescription == null) {
            LOG.debug("Prescription is null");
            return null;
        }
    
        Integer patientId = null;
        Integer physicianId = null;
        Integer medicineId = null;
    
        if (prescription.getPatient() != null) {
            patientId = prescription.getPatient().getId();
        } else if (prescription.getId() != null) {
            patientId = prescription.getId().getPatientId();
        }
    
        if (prescription.getPhysician() != null) {
            physicianId = prescription.getPhysician().getId();
        } else if (prescription.getId() != null) {
            physicianId = prescription.getId().getPhysicianId();
        }
    
        if (prescription.getMedicine() != null) {
            medicineId = prescription.getMedicine().getId();
        }
    
        if (patientId == null || physicianId == null || medicineId == null) {
            LOG.debug("Missing required IDs: patientId={}, physicianId={}, medicineId={}", 
                      patientId, physicianId, medicineId);
            return null;
        }
    
        // Lookup references
        Patient patient = em.find(Patient.class, patientId);
        Physician physician = em.find(Physician.class, physicianId);
        Medicine medicine = em.find(Medicine.class, medicineId);
    
        if (patient == null || physician == null || medicine == null) {
            LOG.debug("Entities not found: patient={}, physician={}, medicine={}",
                      patient != null, physician != null, medicine != null);
            return null;
        }
    
        PrescriptionPK id = new PrescriptionPK();
        id.setPatientId(patientId);
        id.setPhysicianId(physicianId);
    
        if (em.find(Prescription.class, id) != null) {
            LOG.debug("Prescription already exists for patient={} and physician={}", 
                     patientId, physicianId);
            return null;
        }
    
        prescription.setId(id);
        prescription.setPatient(patient);
        prescription.setPhysician(physician);
        prescription.setMedicine(medicine);
    
        try {
            em.persist(prescription);
            patient.getPrescriptions().add(prescription);
            physician.getPrescriptions().add(prescription);
            em.flush();
            return prescription;
        } catch (Exception e) {
            LOG.error("Error persisting prescription: {}", e.getMessage());
            return null;
        }
    }
    

    @Transactional
    public Prescription updatePrescription(int patientId, int physicianId, Prescription updatingPrescription) {
        PrescriptionPK id = new PrescriptionPK();
        id.setPatientId(patientId);
        id.setPhysicianId(physicianId);
        Prescription prescriptionToUpdate = em.find(Prescription.class, id);
        if (prescriptionToUpdate != null) {
            prescriptionToUpdate = em.merge(updatingPrescription);
        }
        return prescriptionToUpdate;
    }

    @Transactional
    public void deletePrescription(int patientId, int physicianId) {
        PrescriptionPK id = new PrescriptionPK();
        id.setPatientId(patientId);
        id.setPhysicianId(physicianId);
        Prescription prescription = em.find(Prescription.class, id);
    
        if (prescription != null) {
            em.refresh(prescription);
    
            deleteSecurityUsersByPhysicianId(physicianId);
    
            if (prescription.getPatient() != null) {
                prescription.getPatient().getPrescriptions().remove(prescription);
            }
    
            if (prescription.getPhysician() != null) {
                prescription.getPhysician().getPrescriptions().remove(prescription);
            }
    
            em.remove(prescription);
        }
    }
    
    
@Transactional
public void deleteSecurityUsersByPhysicianId(int physicianId) {
    List<SecurityUser> users = em.createNamedQuery("SecurityUser.FIND_BY_PHYSICIAN_ID", SecurityUser.class)
        .setParameter("physicianId", physicianId)
        .getResultList();

    for (SecurityUser user : users) {
        user.getRoles().clear(); // Remove links from join table
        em.flush(); // Force delete of join table rows
        em.remove(em.contains(user) ? user : em.merge(user));
    }
}
    
}