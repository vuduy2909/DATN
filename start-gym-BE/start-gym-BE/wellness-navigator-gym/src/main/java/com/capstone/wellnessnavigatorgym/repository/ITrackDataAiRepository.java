package com.capstone.wellnessnavigatorgym.repository;

import com.capstone.wellnessnavigatorgym.dto.tree.UserDataDTO;
import com.capstone.wellnessnavigatorgym.entity.TrackDataAi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface ITrackDataAiRepository extends JpaRepository<TrackDataAi, Integer> {

    @Query("SELECT t FROM TrackDataAi t LEFT JOIN FETCH t.course c LEFT JOIN FETCH c.courseType")
    List<TrackDataAi> findAllWithCourse();

    @Query("SELECT t FROM TrackDataAi t " +
            "WHERE t.age = :#{#userDataDTO.age} AND " +
            "t.gender = :#{#userDataDTO.gender} AND " +
            "t.bmi BETWEEN :#{#userDataDTO.bmi - 5} AND :#{#userDataDTO.bmi + 5} AND " +
            "t.training_goals = :#{#userDataDTO.training_goals} AND " +
            "t.training_history = :#{#userDataDTO.training_history}")
    List<TrackDataAi> findSimilarData(@Param("userDataDTO") UserDataDTO userDataDTO);

    @Query("SELECT t FROM TrackDataAi t " +
            "WHERE t.age = :#{#userDataDTO.age} AND " +
            "t.gender = :#{#userDataDTO.gender}")
    List<TrackDataAi> findByAgeAndGender(@Param("userDataDTO") UserDataDTO userDataDTO);

    @Query(value = "SELECT COALESCE(MAX(track_data_ai_id), 0) FROM track_data_ai", nativeQuery = true)
    Integer findMaxTrackDataAiId();
}
