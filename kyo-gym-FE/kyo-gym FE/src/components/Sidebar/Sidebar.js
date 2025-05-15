import React, { useState, useEffect } from "react";
import styles from "./Sidebar.module.css";
import { request } from "../../utils/axiosInstance";

const Sidebar = ({ onCategoryChange, isRecommend }) => {
  // Initialize from localStorage if available, otherwise use "All"
  const [selected, setSelected] = useState(() => {
    return localStorage.getItem('selectedCategory') || "All";
  });
  
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

  // Separate useEffect for handling the initial selection
  useEffect(() => {
    // Trigger category change with the initial selected value
    onCategoryChange(selected);
  }, [onCategoryChange, selected]);

  const handleClick = (itemName) => {
    setSelected(itemName);
    // Save to localStorage for persistence
    localStorage.setItem('selectedCategory', itemName);
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