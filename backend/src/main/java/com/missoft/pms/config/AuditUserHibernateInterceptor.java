package com.missoft.pms.config;

import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AuditUserHibernateInterceptor extends EmptyInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(AuditUserHibernateInterceptor.class);

    @Override
    public boolean onSave(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types) {
        Long currentUserId = UserContextHolder.getCurrentUserId();
        logger.debug("onSave for entity: {}, User ID: {}", entity.getClass().getSimpleName(), currentUserId);
        if (currentUserId == null) {
            return false;
        }
        boolean changed = false;
        int createUserIndex = findPropertyIndex(propertyNames, "createUser");
        if (createUserIndex >= 0 && state[createUserIndex] == null) {
            state[createUserIndex] = currentUserId;
            changed = true;
            logger.debug("Set createUser to {} for entity: {}", currentUserId, entity.getClass().getSimpleName());
        }
        int updateUserIndex = findPropertyIndex(propertyNames, "updateUser");
        if (updateUserIndex >= 0) {
            state[updateUserIndex] = currentUserId;
            changed = true;
            logger.debug("Set updateUser to {} for entity: {}", currentUserId, entity.getClass().getSimpleName());
        }
        return changed;
    }

    @Override
    public boolean onFlushDirty(Object entity, Object id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
        Long currentUserId = UserContextHolder.getCurrentUserId();
        if (currentUserId == null) {
            return false;
        }
        int updateUserIndex = findPropertyIndex(propertyNames, "updateUser");
        if (updateUserIndex >= 0) {
            currentState[updateUserIndex] = currentUserId;
            return true;
        }
        return false;
    }

    private int findPropertyIndex(String[] propertyNames, String targetProperty) {
        if (propertyNames == null) {
            return -1;
        }
        for (int i = 0; i < propertyNames.length; i++) {
            if (targetProperty.equals(propertyNames[i])) {
                return i;
            }
        }
        return -1;
    }
}
