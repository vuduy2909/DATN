package com.capstone.wellnessnavigatorgym.dto.tree;

import com.capstone.wellnessnavigatorgym.entity.Course;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class TreeNode {
    @XmlElement
    private String attributeName;  // Thuộc tính tại nút hiện tại

    @XmlElement
    private Map<Object, TreeNode> children = new HashMap<>(); // Bản đồ các nút con

    @XmlElement
    private Course courseDataId;

    @XmlElement
    private boolean isLeaf; // Kiểm tra xem nút có phải là lá không

    @XmlElement
    private boolean classification; // Kết quả phân loại (true hoặc false) nếu nút là lá

    @XmlElement
    private double gainRatio; // Thêm thuộc tính cho Gain Ratio

    @XmlElement
    private List<Course> recommendation;

    public Course getCourseDataId() {
        return courseDataId;
    }

    public TreeNode(String attributeName) {
        this.attributeName = attributeName;
        this.children = new HashMap<>();
        this.isLeaf = false;
        this.classification = false;
        this.gainRatio = 0.0;
        this.courseDataId = getCourseDataId(); // Khởi tạo Gain Ratio
    }
}
