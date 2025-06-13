import React, { useState, useEffect } from 'react';
import { useAuth } from '../AuthContext';
import { useLocation, useNavigate } from 'react-router-dom';
import Collections_card from '../components/Collections_card/Collections_card';
import { request } from '../utils/axiosInstance';
import Header from "../components/Header/Header";
import Footer from "../components/Footer/Footer";
import Sidebar from "../components/Sidebar/Sidebar";
import { Dialog, DialogTitle, DialogContent, DialogActions, Button } from '@mui/material';

const Collections = () => {
  // Initialize from localStorage if available, otherwise use "All"
  const [selectedCategory, setSelectedCategory] = useState(() => {
    return localStorage.getItem('selectedCategory') || "All";
  });
  const [searchTerm, setSearchTerm] = useState('');
  const location = useLocation();
  const navigate = useNavigate();

  // Extract search term from URL if present
  useEffect(() => {
    const searchParams = new URLSearchParams(location.search);
    const search = searchParams.get('search');
    if (search) {
      setSearchTerm(search);
    } else {
      setSearchTerm('');
    }
  }, [location.search]);

  const handleCategoryChange = (category) => {
    setSelectedCategory(category);
    // Save to localStorage for persistence
    localStorage.setItem('selectedCategory', category);
  };

  const { user } = useAuth();
  const [userData, setUserData] = useState({ recommendedCourses: [] });
  const [myCourses, setMyCourses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loginDialogOpen, setLoginDialogOpen] = useState(false);

  // Function to fetch user data
    const fetchUserData = async () => {
      try {
        setLoading(true);
        
        // Kiểm tra token trước khi gọi API
        const token = localStorage.getItem('token');
      
        if (user && token) {
          try {
            // Fetch user details for recommended courses
            const detailRes = await request.get("/customer/detail");
            
            // Fetch all user's courses (both recommended and self-enrolled)
            const myCoursesRes = await request.get("/customer-course/my-courses");
            
            // Set the user data and courses
            setUserData(detailRes || { recommendedCourses: [] });
            setMyCourses(myCoursesRes?.courses || []);
            
            // If user has any courses, auto switch to My Course tab
            if (myCoursesRes?.courses && myCoursesRes.courses.length > 0) {
              setSelectedCategory("My Course");
              localStorage.setItem('selectedCategory', 'My Course');
          } else {
            }
          } catch (apiError) {
            // Không set error state, chỉ log để user có thể xem collections công khai
            setUserData({ recommendedCourses: [] });
            setMyCourses([]);
          }
        } else {
          setUserData({ recommendedCourses: [] });
          setMyCourses([]);
        }
        
      } catch (error) {
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
      fetchUserData();
    };

    // Check if we just returned from a successful payment
    const urlParams = new URLSearchParams(window.location.search);
    const paymentStatus = urlParams.get('paymentStatus');
    const paymentSuccess = localStorage.getItem('vipPaymentSuccess');
    
    if (paymentStatus === 'success' || paymentSuccess === 'true') {
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
    if (!user) {
      return [];
    }
    
    // Start with user's enrolled courses
    const userEnrolledCourses = myCourses.map(course => {
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
    
    // Add any recommended courses from user details that aren't already in enrolled courses
    const recommendedFromDetails = userData.recommendedCourses || [];
    const enrolledCourseIds = userEnrolledCourses.map(c => c.courseId);
    
    // Ensure recommendedFromDetails is always an array
    const recommendedArray = Array.isArray(recommendedFromDetails) 
      ? recommendedFromDetails 
      : recommendedFromDetails ? [recommendedFromDetails] : [];
    
    const additionalRecommended = recommendedArray
      .filter(rec => rec && !enrolledCourseIds.includes(rec.courseId))
      .map(rec => ({
        ...rec,
        isRecommended: true,
        joinType: 'Recommended'
      }));
    
    const finalCourses = [...userEnrolledCourses, ...additionalRecommended];
    
    console.log('All user courses:', finalCourses);
    return finalCourses;
  }, [myCourses, userData.recommendedCourses, user]);

  const handleCourseJoinSuccess = (courseId) => {
    // Refresh user courses after successful join
    if (user) {
      request.get("/customer-course/my-courses")
        .then(response => {
          setMyCourses(response?.courses || []);
        })
        .catch(error => {
        });
    }
  };

  const handleRecommendClick = () => {
    if (!user) {
      setLoginDialogOpen(true);
    } else {
      navigate('/recommend');
    }
  };

  return (
    <div style={{ backgroundColor: "#f0ece8", minHeight: "100vh" }}>
      <Header />
      
      <div style={{ display: "flex", padding: "15px" }}>
        <Sidebar onCategoryChange={handleCategoryChange} selectedCategory={selectedCategory} />
        <div style={{ flex: 1, maxWidth: "calc(100% - 250px)" }}>
          <Collections_card
            selectedCategory={selectedCategory}
            recommendedCourses={allUserCourses}
            loading={loading}
            onCourseJoinSuccess={handleCourseJoinSuccess}
            searchTerm={searchTerm}
          />
        </div>
      </div>
      <Footer />
      {/* Material UI Dialog for login */}
      <Dialog open={loginDialogOpen} onClose={() => setLoginDialogOpen(false)}>
        <DialogTitle>Bạn cần đăng nhập</DialogTitle>
        <DialogContent>Bạn cần đăng nhập để sử dụng tính năng nhận gợi ý khóa học. Bạn có muốn đăng nhập không?</DialogContent>
        <DialogActions>
          <Button onClick={() => setLoginDialogOpen(false)}>Hủy</Button>
          <Button variant="contained" color="primary" onClick={() => { setLoginDialogOpen(false); navigate('/login', { state: { returnUrl: '/recommend' } }); }}>Đăng nhập</Button>
        </DialogActions>
      </Dialog>
    </div>
  );
};

export default Collections;