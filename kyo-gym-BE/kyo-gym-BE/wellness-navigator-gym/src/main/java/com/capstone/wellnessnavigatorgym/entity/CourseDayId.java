package com.capstone.wellnessnavigatorgym.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseDayId implements Serializable {
    private Integer courseId;
    private Integer dayId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CourseDayId that = (CourseDayId) o;
        return Objects.equals(courseId, that.courseId) &&
               Objects.equals(dayId, that.dayId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseId, dayId);
    }
} 