package acmemedical.entity;

import java.time.LocalDateTime;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

public class PojoCompositeListener {

    @PrePersist
    public void setCreatedOnDate(Object entity) {
        if (entity instanceof PojoBaseCompositeKey<?>) {
            PojoBaseCompositeKey<?> pojo = (PojoBaseCompositeKey<?>) entity;
            LocalDateTime now = LocalDateTime.now();
            pojo.setCreated(now);
            pojo.setUpdated(now);
        }
    }

    @PreUpdate
    public void setUpdatedDate(Object entity) {
        if (entity instanceof PojoBaseCompositeKey<?>) {
            PojoBaseCompositeKey<?> pojo = (PojoBaseCompositeKey<?>) entity;
            pojo.setUpdated(LocalDateTime.now());
        }
    }
}
