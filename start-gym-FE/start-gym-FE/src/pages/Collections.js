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
  const [myCourses, setMyCourses] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchUserData = async () => {
      try {
        setLoading(true);
        
        if (user) {
          // Fetch user details for recommended courses
          const detailRes = await request.get("/customer/detail");
          console.log("User details:", detailRes);
          
          // Fetch all user's courses (both recommended and self-enrolled)
          const myCoursesRes = await request.get("/customer-course/my-courses");
          console.log("My courses:", myCoursesRes);
          
          // Set the user data and courses
          setUserData(detailRes);
          setMyCourses(myCoursesRes?.courses || []);
          
          // If user has any courses, auto switch to My Course tab
          if (myCoursesRes?.courses && myCoursesRes.courses.length > 0) {
            console.log('User has courses, setting category to My Course');
            setSelectedCategory("My Course");
            localStorage.setItem('selectedCategory', 'My Course');
          }
        } else {
          setUserData({ recommendedCourses: [] });
          setMyCourses([]);
        }
        
      } catch (error) {
        console.error("Error fetching user information", error);
        setUserData({ recommendedCourses: [] });
        setMyCourses([]);
      } finally {
        setLoading(false);
      }
    };

    fetchUserData();
  }, [user]);

  // Combine recommended courses and user's enrolled courses for the My Course tab
  const allUserCourses = React.useMemo(() => {
    if (!user) return [];
    
    // Start with user's enrolled courses
    const userEnrolledCourses = myCourses.map(course => ({
      courseId: course.courseId,
      courseName: course.courseName,
      description: course.description,
      duration: course.duration,
      image: course.image,
      isRecommended: course.isRecommended,
      joinType: course.joinType,
      courseType: course.courseType
    }));
    
    // Add any recommended courses from user details that aren't already in enrolled courses
    const recommendedFromDetails = userData.recommendedCourses || [];
    const enrolledCourseIds = userEnrolledCourses.map(c => c.courseId);
    
    const additionalRecommended = recommendedFromDetails
      .filter(rec => !enrolledCourseIds.includes(rec.courseId))
      .map(rec => ({
        ...rec,
        isRecommended: true,
        joinType: 'Recommended'
      }));
    
    return [...userEnrolledCourses, ...additionalRecommended];
  }, [myCourses, userData.recommendedCourses, user]);

  console.log("Current category:", selectedCategory);
  console.log("All user courses for My Course tab:", allUserCourses);

  const handleCourseJoinSuccess = (courseId) => {
    // Refresh user courses after successful join
    if (user) {
      request.get("/customer-course/my-courses")
        .then(response => {
          setMyCourses(response?.courses || []);
        })
        .catch(error => {
          console.error("Error refreshing courses after join:", error);
        });
    }
  };

  return (
    <div style={{ backgroundColor: "#f0ece8" }}>
      <Header />
      <div style={{ display: "flex", padding: "15px" }}>
        <Sidebar onCategoryChange={handleCategoryChange} selectedCategory={selectedCategory} />
        <div style={{ flex: 1, maxWidth: "calc(100% - 250px)" }}>
          <Collections_card
            selectedCategory={selectedCategory}
            recommendedCourses={allUserCourses}
            loading={loading}
            onCourseJoinSuccess={handleCourseJoinSuccess}
          />
        </div>
      </div>
      <Footer />
    </div>
  );
};

export default Collections;