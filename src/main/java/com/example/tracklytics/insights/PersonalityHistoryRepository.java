package com.example.tracklytics.insights;

import com.example.tracklytics.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PersonalityHistoryRepository extends JpaRepository<PersonalityHistory, Integer> {

    List<PersonalityHistory> findByUserOrderByAnalyzedAtDesc(User user);

    PersonalityHistory findFirstByUserOrderByAnalyzedAtDesc(User user);
}
