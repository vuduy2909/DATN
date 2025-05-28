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

  // Function to fetch user data
    const fetchUserData = async () => {
      try {
        setLoading(true);
        
        // Kiểm tra token trước khi gọi API
        const token = localStorage.getItem('token');
      console.log('=== Collections Debug ===');
      console.log('User:', user);
      console.log('Token exists:', !!token);
      
        if (user && token) {
          try {
            // Fetch user details for recommended courses
          console.log('Fetching user details...');
            const detailRes = await request.get("/customer/detail");
          console.log("User details response:", detailRes);
            
            // Fetch all user's courses (both recommended and self-enrolled)
          console.log('Fetching my courses...');
            const myCoursesRes = await request.get("/customer-course/my-courses");
          console.log("My courses response:", myCoursesRes);
          console.log("My courses array:", myCoursesRes?.courses);
          console.log("Number of courses:", myCoursesRes?.courses?.length || 0);
            
            // Set the user data and courses
          setUserData(detailRes || { recommendedCourses: [] });
            setMyCourses(myCoursesRes?.courses || []);
            
            // If user has any courses, auto switch to My Course tab
            if (myCoursesRes?.courses && myCoursesRes.courses.length > 0) {
              console.log('User has courses, setting category to My Course');
              setSelectedCategory("My Course");
              localStorage.setItem('selectedCategory', 'My Course');
          } else {
            console.log('User has no courses');
            }
          } catch (apiError) {
            console.log("API error, user might not be logged in:", apiError);
          console.log("API error details:", apiError.response);
            // Không set error state, chỉ log để user có thể xem collections công khai
            setUserData({ recommendedCourses: [] });
            setMyCourses([]);
          }
        } else {
          console.log("No user or token, showing public collections only");
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

  useEffect(() => {
    fetchUserData();
  }, [user]);

  // Listen for successful VIP payment completion
  useEffect(() => {
    const handlePaymentSuccess = () => {
      console.log('Payment success detected, refreshing course data...');
      fetchUserData();
    };

    // Check if we just returned from a successful payment
    const urlParams = new URLSearchParams(window.location.search);
    const paymentStatus = urlParams.get('paymentStatus');
    const paymentSuccess = localStorage.getItem('vipPaymentSuccess');
    
    if (paymentStatus === 'success' || paymentSuccess === 'true') {
      console.log('Detected successful VIP payment, refreshing data...');
      // Clear the flag
      localStorage.removeItem('vipPaymentSuccess');
      // Remove URL parameters
      if (paymentStatus) {
        window.history.replaceState({}, document.title, window.location.pathname);
      }
      // Refresh data
      setTimeout(handlePaymentSuccess, 1000); // Small delay to ensure backend processing is complete
    }

    // Listen for storage events (in case payment success is set from another tab/window)
    const handleStorageChange = (e) => {
      if (e.key === 'vipPaymentSuccess' && e.newValue === 'true') {
        handlePaymentSuccess();
      }
    };

    window.addEventListener('storage', handleStorageChange);
    
    return () => {
      window.removeEventListener('storage', handleStorageChange);
    };
  }, [user]);

  // Combine recommended courses and user's enrolled courses for the My Course tab
  const allUserCourses = React.useMemo(() => {
    console.log('=== allUserCourses Debug ===');
    console.log('User exists:', !!user);
    console.log('myCourses:', myCourses);
    console.log('userData.recommendedCourses:', userData.recommendedCourses);
    
    if (!user) {
      console.log('No user, returning empty array');
      return [];
    }
    
    // Start with user's enrolled courses
    const userEnrolledCourses = myCourses.map(course => {
      console.log('Processing course:', course);
      return {
      courseId: course.courseId,
      courseName: course.courseName,
      description: course.description,
      duration: course.duration,
      image: course.image,
      isRecommended: course.isRecommended,
      joinType: course.joinType,
        courseType: course.courseType,
        // Đảm bảo thông tin VIP được truyền
        isVip: course.isVip || false,
        price: course.price
      };
    });
    
    console.log('userEnrolledCourses after mapping:', userEnrolledCourses);
    
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
    
    console.log('additionalRecommended:', additionalRecommended);
    
    const finalCourses = [...userEnrolledCourses, ...additionalRecommended];
    console.log('Final allUserCourses:', finalCourses);
    
    return finalCourses;
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