import React, { useState, useEffect } from "react";
import styles from "./Sidebar.module.css";
import { request } from "../../utils/axiosInstance";

const Sidebar = ({ onCategoryChange, selectedCategory: propSelectedCategory }) => {
  // Initialize from prop or localStorage as fallback
  const [selected, setSelected] = useState(() => {
    return propSelectedCategory || localStorage.getItem('selectedCategory') || "All";
  });
  
  const [categories, setCategories] = useState([
    "Recommend", 
    "All"
  ]);

  // Update local state when prop changes
  useEffect(() => {
    if (propSelectedCategory && propSelectedCategory !== selected) {
      setSelected(propSelectedCategory);
    }
  }, [propSelectedCategory]);

  useEffect(() => {
    // Get course types from API
    const fetchCourseTypes = async () => {
      try {
        const res = await request.get('/course-type');
        const courseTypes = res.map(type => type.courseTypeName);
        // Add "Recommend" and "All" at the beginning of the list
        setCategories(["Recommend", "All", ...courseTypes]);
      } catch (error) {
        console.error("Error fetching course types:", error);
      }
    };

    fetchCourseTypes();
  }, []);

  // Send initial selection to parent
  useEffect(() => {
    onCategoryChange(selected);
  }, []);

  const handleClick = (itemName) => {
    setSelected(itemName);
    // Notify parent component of category change
    onCategoryChange(itemName);
  };

  return (
    <div className={styles.container}>
      {categories.map((category) => (
        <div
          key={category}
          className={`${styles.item} ${
            selected === category ? styles.selected : ""
          } ${category === "Recommend" ? styles.recommend : ""}`}
          onClick={() => handleClick(category)}
        >
          {category}
        </div>
      ))}
    </div>
  );
};

export default Sidebar;