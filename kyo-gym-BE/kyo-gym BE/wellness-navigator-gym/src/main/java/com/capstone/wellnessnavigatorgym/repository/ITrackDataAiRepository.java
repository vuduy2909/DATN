package com.capstone.wellnessnavigatorgym.repository;

import com.capstone.wellnessnavigatorgym.dto.tree.UserDataDTO;
import com.capstone.wellnessnavigatorgym.entity.TrackDataAi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface ITrackDataAiRepository extends JpaRepository<TrackDataAi, Integer> {

    @Query(value = "SELECT * FROM track_data_ai t WHERE " +
            "t.activity_level = :#{#userDataDTO.activity_level} AND " +
            "t.gender = :#{#userDataDTO.gender} AND " +
            "t.training_goals  = :#{#userDataDTO.training_goals} AND "+
            "t.age = :#{#userDataDTO.age} AND "+
            "t.training_history = :#{#userDataDTO.training_history} AND "+
            "t.bmi BETWEEN :#{#userDataDTO.bmi - 1} AND :#{#userDataDTO.bmi + 1}", nativeQuery = true)
    List<TrackDataAi> findCustomByUserData(UserDataDTO userDataDTO);
}
