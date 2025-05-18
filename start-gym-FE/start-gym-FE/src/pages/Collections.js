import React, { useEffect, useState } from "react";
import Header from "../components/Header/Header";
import Footer from "../components/Footer/Footer";
import Collections_card from "../components/Collections_card/Collections_card";
import Sidebar from "../components/Sidebar/Sidebar";
import { useAuth } from "../AuthContext";
import { request } from "../utils/axiosInstance";

const Collections = () => {
  // Initialize from localStorage if available, otherwise use "All"
  const [selectedCategory, setSelectedCategory] = useState(() => {
    return localStorage.getItem('selectedCategory') || "All";
  });

  const handleCategoryChange = (category) => {
    setSelectedCategory(category);
    // Save to localStorage for persistence
    localStorage.setItem('selectedCategory', category);
  };

  const { user } = useAuth();
  const [userData, setUserData] = useState({ recommendedCourses: [] });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchUserData = async () => {
      try {
        setLoading(true);
        
        // User details API call - contains recommended courses in response
        const detailRes = await request.get("/customer/detail");
        console.log("User details:", detailRes);
        
        // Check if we have recommended courses in the user details
        const hasRecommendationsInDetails = detailRes?.recommendedCourses?.some(
          course => course.recommendedStatus === true && course.courseId === 5
        );
        
        console.log("Has recommendations in details:", hasRecommendationsInDetails);

        // Set the state with user data
        setUserData(detailRes);
        
        // Always set to Recommend tab if we're in this scenario - the course is there
        console.log('Setting category to Recommend - recommend was clicked');
        setSelectedCategory("Recommend");
        localStorage.setItem('selectedCategory', 'Recommend');
        localStorage.setItem('hasRecommendation', 'true');
        
      } catch (error) {
        console.error("Error fetching user information", error);
        setUserData({ recommendedCourses: [] });
      } finally {
        setLoading(false);
      }
    };

    if (user) {
      fetchUserData();
    }
  }, [user]);

  console.log("Current category:", selectedCategory);
  console.log("Available recommended courses:", userData.recommendedCourses);

  return (
    <div style={{ backgroundColor: "#f0ece8" }}>
      <Header />
      <div style={{ display: "flex", padding: "15px" }}>
        <Sidebar onCategoryChange={handleCategoryChange} selectedCategory={selectedCategory} />
        <div style={{ flex: 1, maxWidth: "calc(100% - 250px)" }}>
          <Collections_card
            selectedCategory={selectedCategory}
            recommendedCourses={userData.recommendedCourses}
            loading={loading}
          />
        </div>
      </div>
      <Footer />
    </div>
  );
};

export default Collections;