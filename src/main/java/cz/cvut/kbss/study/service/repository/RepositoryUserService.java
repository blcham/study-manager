package cz.cvut.kbss.study.service.repository;

import cz.cvut.kbss.study.exception.UsernameExistsException;
import cz.cvut.kbss.study.exception.ValidationException;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.model.Vocabulary;
import cz.cvut.kbss.study.persistence.dao.GenericDao;
import cz.cvut.kbss.study.persistence.dao.PatientRecordDao;
import cz.cvut.kbss.study.persistence.dao.UserDao;
import cz.cvut.kbss.study.service.UserService;
import cz.cvut.kbss.study.service.security.SecurityUtils;
import cz.cvut.kbss.study.util.Email;
import cz.cvut.kbss.study.util.GeneratePassword;
import cz.cvut.kbss.study.util.etemplates.BaseEmailTemplate;
import cz.cvut.kbss.study.util.etemplates.PasswordReset;
import org.apache.commons.lang.StringUtils;
import org.eclipse.rdf4j.http.protocol.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class RepositoryUserService extends BaseRepositoryService<User> implements UserService {

    @Autowired
    private SecurityUtils securityUtils;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserDao userDao;

    @Autowired
    private PatientRecordDao patientRecordDao;

    @Override
    protected GenericDao<User> getPrimaryDao() {
        return userDao;
    }

    @Override
    public User findByUsername(String username) {
        return userDao.findByUsername(username);
    }

    @Override
    public List<User> findByInstitution(Institution institution) {
        Objects.requireNonNull(institution);
        return userDao.findByInstitution(institution);
    }

    @Override
    public User findByEmail(String email) {
        return userDao.findByEmail(email);
    }

    @Override
    public String generateUsername(String usernamePrefix) {
        return usernamePrefix + (userDao.findAll().stream()
                .filter(u -> u.getUsername().startsWith(usernamePrefix))
                .map(u -> u.getUsername().replaceFirst(usernamePrefix, ""))
                .filter(s -> StringUtils.isNotBlank(s) && StringUtils.isNumeric(s))
                .map(s -> Integer.parseInt(s))
                .max(Comparator.naturalOrder())
                .orElse(0) + 1);
    }

    @Override
    public void changePassword(User user, String newPassword, String currentPassword) {
        final User currentUser = securityUtils.getCurrentUser();
        if (currentUser.getUsername().equals(user.getUsername()) && !passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new ValidationException("The passed user's current password is different from the specified one.");
        }
        user.setPassword(newPassword);
        user.encodePassword(passwordEncoder);
        userDao.update(user);
    }

    @Override
    public void resetPassword(User user, String emailAddress) {
        Objects.requireNonNull(user);
        String newPassword = GeneratePassword.generatePassword();
        user.setPassword(newPassword);
        user.encodePassword(passwordEncoder);
        userDao.update(user);
        BaseEmailTemplate emailTemplate = new PasswordReset(user.getUsername(), newPassword);
        Email email = new Email(emailTemplate, emailAddress);
        email.sendEmail();
    }

    @Override
    protected void prePersist(User instance) {
        if (findByUsername(instance.getUsername()) != null) {
            throw new UsernameExistsException("User with specified username already exists.");
        }
        try {
            instance.encodePassword(passwordEncoder);
            instance.validateUsername();
        } catch (IllegalStateException e) {
            throw new ValidationException(e.getMessage());
        }
    }

    @Override
    protected void preUpdate(User instance) {
        final User currentUser = securityUtils.getCurrentUser();
        if (!currentUser.getTypes().contains(Vocabulary.s_c_administrator) &&
                (!instance.getTypes().equals(currentUser.getTypes()) || (instance.getInstitution() != null &&
                !instance.getInstitution().getKey().equals(currentUser.getInstitution().getKey())))) {
            throw new UnauthorizedException("Cannot update user.");
        }
        if (!findByUsername(instance.getUsername()).getUri().equals(instance.getUri())) {
            throw new UsernameExistsException("User with specified username already exists.");
        }
        final User orig = userDao.find(instance.getUri());
        if (orig == null) {
            throw new IllegalArgumentException("Cannot update user URI.");
        }
        if (StringUtils.isBlank(instance.getPassword())) {
            instance.setPassword(orig.getPassword());
        }
    }

    @Override
    protected void preRemove(User instance) {
        if (!patientRecordDao.findByAuthor(instance).isEmpty()) {
            throw new ValidationException("User with patient records cannot be deleted.");
        }
    }
}
