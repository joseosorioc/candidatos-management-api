package com.seek.candidatosmanagementapi.repository;

import com.seek.candidatosmanagementapi.entity.Candidate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
@DisplayName("CandidateRepository Tests")
class CandidateRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CandidateRepository candidateRepository;

    private Candidate candidate1;
    private Candidate candidate2;

    @BeforeEach
    void setUp() {
        candidateRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        LocalDate birthDate1 = LocalDate.of(1990, 5, 15);
        LocalDate birthDate2 = LocalDate.of(1995, 3, 20);

        int age1 = Period.between(birthDate1, LocalDate.now()).getYears();
        int age2 = Period.between(birthDate2, LocalDate.now()).getYears();

        candidate1 = Candidate.builder()
                .firstName("Juan")
                .lastName("Pérez")
                .age(age1)
                .birthDate(birthDate1)
                .build();

        candidate2 = Candidate.builder()
                .firstName("María")
                .lastName("García")
                .age(age2)
                .birthDate(birthDate2)
                .build();
    }

    @Test
    @DisplayName("Should save candidate successfully")
    void shouldSaveCandidateSuccessfully() {
        Candidate saved = candidateRepository.save(candidate1);
        entityManager.flush();

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getFirstName()).isEqualTo("Juan");
        assertThat(saved.getLastName()).isEqualTo("Pérez");
        assertThat(saved.getAge()).isEqualTo(35);
        assertThat(saved.getBirthDate()).isEqualTo(LocalDate.of(1990, 5, 15));
    }

    @Test
    @DisplayName("Should find candidate by id")
    void shouldFindCandidateById() {
        Candidate saved = entityManager.persistAndFlush(candidate1);
        Long candidateId = saved.getId();


        Optional<Candidate> found = candidateRepository.findById(candidateId);

        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("Juan");
        assertThat(found.get().getLastName()).isEqualTo("Pérez");
        assertThat(found.get().getAge()).isEqualTo(35);
    }

    @Test
    @DisplayName("Should return empty when candidate not found")
    void shouldReturnEmptyWhenCandidateNotFound() {
        Optional<Candidate> found = candidateRepository.findById(999L);
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find all candidates")
    void shouldFindAllCandidates() {
        entityManager.persistAndFlush(candidate1);
        entityManager.persistAndFlush(candidate2);

        List<Candidate> candidates = candidateRepository.findAll();

        assertThat(candidates).hasSize(2);
        assertThat(candidates).extracting(Candidate::getFirstName)
                .containsExactlyInAnyOrder("Juan", "María");
        assertThat(candidates).extracting(Candidate::getLastName)
                .containsExactlyInAnyOrder("Pérez", "García");
    }

    @Test
    @DisplayName("Should return empty list when no candidates")
    void shouldReturnEmptyListWhenNoCandidates() {
        List<Candidate> candidates = candidateRepository.findAll();
        assertThat(candidates).isEmpty();
    }

    @Test
    @DisplayName("Should delete candidate successfully")
    void shouldDeleteCandidateSuccessfully() {
        Candidate saved = entityManager.persistAndFlush(candidate1);
        Long candidateId = saved.getId();

        candidateRepository.deleteById(candidateId);
        entityManager.flush();

        Optional<Candidate> found = candidateRepository.findById(candidateId);
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should update candidate successfully")
    void shouldUpdateCandidateSuccessfully() {
        Candidate saved = entityManager.persistAndFlush(candidate1);
        entityManager.clear();

        saved.setFirstName("Juan Carlos");
        saved.setAge(36);
        Candidate updated = candidateRepository.save(saved);
        entityManager.flush();

        assertThat(updated.getFirstName()).isEqualTo("Juan Carlos");
        assertThat(updated.getAge()).isEqualTo(36);
        assertThat(updated.getLastName()).isEqualTo("Pérez");
        assertThat(updated.getBirthDate()).isEqualTo(LocalDate.of(1990, 5, 15));
    }

    @Test
    @DisplayName("Should count candidates correctly")
    void shouldCountCandidatesCorrectly() {
        entityManager.persistAndFlush(candidate1);
        entityManager.persistAndFlush(candidate2);

        long count = candidateRepository.count();

        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Should handle large datasets")
    void shouldHandleLargeDatasets() {
        for (int i = 0; i < 10; i++) {
            Candidate candidate = Candidate.builder()
                    .firstName("Candidate" + i)
                    .lastName("LastName" + i)
                    .age(20 + i)
                    .birthDate(LocalDate.of(2000 - i, 1, 1))
                    .build();
            entityManager.persist(candidate);
        }
        entityManager.flush();


        List<Candidate> allCandidates = candidateRepository.findAll();
        long totalCount = candidateRepository.count();

        assertThat(allCandidates).hasSize(10);
        assertThat(totalCount).isEqualTo(10);
        assertThat(allCandidates).extracting(Candidate::getFirstName)
                .contains("Candidate0", "Candidate5", "Candidate9");
    }

    @Test
    @DisplayName("Should persist and retrieve dates correctly")
    void shouldPersistAndRetrieveDatesCorrectly() {

        LocalDate specificDate = LocalDate.of(1985, 12, 25);
        candidate1.setBirthDate(specificDate);


        Candidate saved = entityManager.persistAndFlush(candidate1);
        entityManager.clear();

        Optional<Candidate> retrieved = candidateRepository.findById(saved.getId());


        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getBirthDate()).isEqualTo(specificDate);
        assertThat(retrieved.get().getBirthDate().getYear()).isEqualTo(1985);
        assertThat(retrieved.get().getBirthDate().getMonthValue()).isEqualTo(12);
        assertThat(retrieved.get().getBirthDate().getDayOfMonth()).isEqualTo(25);
    }
}