import React, { useState, useEffect } from "react";
import styles from "./Sidebar.module.css";
import { request } from "../../utils/axiosInstance";

const Sidebar = ({ onCategoryChange, isRecommend }) => {
  const [selected, setSelected] = useState("All");
  const [categories, setCategories] = useState([
    "Recommend", 
    "All"
  ]);

  useEffect(() => {
    // Lấy danh sách loại khóa học từ API
    const fetchCourseTypes = async () => {
      try {
        const res = await request.get('/course-type');
        const courseTypes = res.map(type => type.courseTypeName);
        // Thêm "Recommend" và "All" vào đầu danh sách
        setCategories(["Recommend", "All", ...courseTypes]);
      } catch (error) {
        console.error("Error fetching course types:", error);
      }
    };

    fetchCourseTypes();
  }, []);

  const handleClick = (itemName) => {
    setSelected(itemName);
  };

  return (
    <div className={styles.container}>
      {categories.map((category) => (
        <div
          key={category}
          className={`${styles.item} ${
            selected === category ? styles.selected : ""
          }`}
          onClick={() => {
            handleClick(category);
            onCategoryChange(category);
          }}
        >
          {category}
        </div>
      ))}
    </div>
  );
};

export default Sidebar;