package com.capstone.wellnessnavigatorgym.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import javax.persistence.*;
import java.io.Serializable;

@Entity
@Setter
@Getter
@RequiredArgsConstructor
@Table(name = "course_days")
@IdClass(CourseDayId.class)
public class CourseDays implements Serializable {
    @Id
    @Column(name = "course_id")
    private Integer courseId;

    @Id
    @Column(name = "day_id")
    private Integer dayId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", insertable = false, updatable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "day_id", insertable = false, updatable = false)
    private Day day;

    @Column(name = "course_day_id")
    private Integer courseDayId;

    @Column(name = "status")
    private Boolean status;
}
