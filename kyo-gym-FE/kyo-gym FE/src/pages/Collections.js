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
  };

  const { user } = useAuth();
  const [userData, setUserData] = useState({ recommendedCourses: [] });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchUserData = async () => {
      try {
        setLoading(true);
        const res = await request.get("/customer/detail");
        const uniqueRecommendedCourses = res.recommendedCourses.filter(
          (course) => course.recommendedStatus
        );

        setUserData({
          ...res,
          recommendedCourses: uniqueRecommendedCourses,
        });
        
        // Check for recommendations
        const hasRecommendation = localStorage.getItem('hasRecommendation') === 'true';
        console.log('hasRecommendation flag:', hasRecommendation);
        
        // If we have recommendations and the flag is set, select the Recommend category
        if (uniqueRecommendedCourses.length > 0 && hasRecommendation) {
          console.log('Setting category to Recommend based on flag');
          setSelectedCategory("Recommend");
          
          // Clear the hasRecommendation flag so it doesn't affect future visits
          localStorage.removeItem('hasRecommendation');
        } else if (hasRecommendation && uniqueRecommendedCourses.length === 0) {
          // If flag is set but no recommendations found, clear the flag
          console.log('No recommendations found, clearing flag');
          localStorage.removeItem('hasRecommendation');
        }
      } catch (error) {
        console.log("Error fetching user information", error);
      } finally {
        setLoading(false);
      }
    };

    if (user) {
      fetchUserData();
    }
  }, [user]);

  return (
    <div style={{ backgroundColor: "#f0ece8" }}>
      <Header />
      <div style={{ display: "flex", padding: "15px" }}>
        <Sidebar onCategoryChange={handleCategoryChange} />
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