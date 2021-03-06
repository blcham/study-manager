package cz.cvut.kbss.study.service.security;

import cz.cvut.kbss.study.model.PatientRecord;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.persistence.dao.PatientRecordDao;
import cz.cvut.kbss.study.persistence.dao.UserDao;
import cz.cvut.kbss.study.security.model.UserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SecurityUtils {

    @Autowired
    private UserDao userDao;

    @Autowired
    private PatientRecordDao patientRecordDao;

    /**
     * Gets the currently authenticated user.
     *
     * @return Current user
     */
    public User getCurrentUser() {
        final SecurityContext context = SecurityContextHolder.getContext();
        assert context != null;
        final UserDetails userDetails = (UserDetails) context.getAuthentication().getPrincipal();
        return userDao.findByUsername(userDetails.getUser().getUsername());
    }

    /**
     * Gets details of the currently authenticated user.
     *
     * @return Currently authenticated user details or null, if no one is currently authenticated
     */
    public UserDetails getCurrentUserDetails() {
        final SecurityContext context = SecurityContextHolder.getContext();
        if (context.getAuthentication() != null && context.getAuthentication().getDetails() instanceof UserDetails) {
            return (UserDetails) context.getAuthentication().getDetails();
        } else {
            return null;
        }
    }

    /**
     * Checks whether the current user is a member of a institution with the specified key.
     *
     * @param institutionKey Institution identifier
     * @return Membership status of the current user
     */
    public boolean isMemberOfInstitution(String institutionKey) {
        final User user = getCurrentUser();
        return user.getInstitution() != null && user.getInstitution().getKey().equals(institutionKey);
    }

    /**
     * Checks whether the current user is in same institution as the patient record was created.
     *
     * @param recordKey PatientRecord identifier
     * @return Membership status of the current user and patient record
     */
    public boolean isRecordInUsersInstitution(String recordKey) {
        final User user = getCurrentUser();
        final PatientRecord record = patientRecordDao.findByKey(recordKey);
        return user.getInstitution().getKey().equals(record.getInstitution().getKey());
    }

    /**
     * Checks whether the current user is in same institution as user we are asking for.
     *
     * @param username String identifier
     * @return Membership status of the current user and another user
     */
    public boolean areFromSameInstitution(String username) {
        final User user = getCurrentUser();
        final List<User> users = userDao.findByInstitution(user.getInstitution());
        return users.stream().anyMatch(o -> o.getUsername().equals(username));
    }
}
