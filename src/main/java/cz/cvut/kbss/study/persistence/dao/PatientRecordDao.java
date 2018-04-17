package cz.cvut.kbss.study.persistence.dao;

import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.jopa.model.query.Query;
import cz.cvut.kbss.study.dto.PatientRecordDto;
import cz.cvut.kbss.study.dto.PatientRecordSummaryDto;
import cz.cvut.kbss.study.model.Institution;
import cz.cvut.kbss.study.model.PatientRecord;
import cz.cvut.kbss.study.model.User;
import cz.cvut.kbss.study.model.Vocabulary;
import cz.cvut.kbss.study.persistence.dao.util.QuestionSaver;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

import java.net.URI;
import java.util.List;
import java.util.Objects;

@Repository
public class PatientRecordDao extends OwlKeySupportingDao<PatientRecord> {

    public PatientRecordDao() {
        super(PatientRecord.class);
    }

    @Override
    protected void persist(PatientRecord entity, EntityManager em) {
        assert entity != null;
        super.persist(entity, em);
        final QuestionSaver questionSaver = new QuestionSaver();
        questionSaver.persistIfNecessary(entity.getQuestion(), em);
    }

    public List<PatientRecordDto> findAllRecords() {
        final EntityManager em = entityManager();
        try {
            return findAllRecords(em);
        } finally {
            em.close();
        }
    }

    /**
     * Gets records of patients treated at the specified institution.
     *
     * @param institution The institution to filter by
     * @return Records of matching patients
     */
    public List<PatientRecordDto> findByInstitution(Institution institution) {
        Objects.requireNonNull(institution);
        final EntityManager em = entityManager();
        try {
            return em.createNativeQuery("SELECT ?r WHERE { ?r a ?type ; ?treatedAt ?institution . }", PatientRecordDto.class)
                .setParameter("type", typeUri)
                .setParameter("treatedAt", URI.create(Vocabulary.s_p_was_treated_at))
                .setParameter("institution", institution.getUri())
                .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Gets records of patients created by specified author.
     *
     * @param author The author to filter by
     * @return Records of matching patients
     */
    public List<PatientRecord> findByAuthor(User author) {
        Objects.requireNonNull(author);
        final EntityManager em = entityManager();
        try {
            return em.createNativeQuery("SELECT ?r WHERE { ?r a ?type ; ?createdBy ?author . }", PatientRecord.class)
                    .setParameter("type", typeUri)
                    .setParameter("createdBy", URI.create(Vocabulary.s_p_has_author))
                    .setParameter("author", author.getUri()).getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Gets record summaries of patients treated at the specified institution.
     *
     * @param institution The institution to filter by
     * @return Record summaries of matching patients
     */
    public List<PatientRecordSummaryDto> getRecordSummaries(Institution institution) {
        Objects.requireNonNull(institution);
        final EntityManager em = entityManager();
        try {
            Query query =
                em.createNativeQuery("SELECT ?r ?localName { ?r a ?type ; ?treatedAt ?institution ; ?label ?localName . }")
                    .setParameter("type", typeUri)
                    .setParameter("treatedAt", URI.create(Vocabulary.s_p_was_treated_at))
                    .setParameter("institution", institution.getUri())
                    .setParameter("label", URI.create(Vocabulary.s_p_label));

            List<?> resultList = query.getResultList();
            List<PatientRecordSummaryDto> summaries = resultList.stream().map(
                rs -> {
                    Object[] result = (Object[]) rs;
                    PatientRecordSummaryDto ps = new PatientRecordSummaryDto();
                    ps.setRecordURI((URI) result[0]);
                    ps.setLocalName((String) result[1]);
                    return ps;
                }
            ).collect(Collectors.toList());

            return summaries;
        } finally {
            em.close();
        }
    }

    public int getNumberOfProcessedRecords() {
        final EntityManager em = entityManager();
        try {
            return (int) em.createNativeQuery(
                    "SELECT (count(?p) as ?patientRecordsCount) WHERE { ?p a ?record . }")
                    .setParameter("record", URI.create(Vocabulary.s_c_patient_record)).getSingleResult();
        } finally {
            em.close();
        }
    }

    private List<PatientRecordDto> findAllRecords(EntityManager em) {
        return em.createNativeQuery("SELECT ?x WHERE { ?x a ?type . }", PatientRecordDto.class)
            .setParameter("type", typeUri)
            .getResultList();
    }

}
