import React, { useState, useEffect, useRef, useCallback } from "react";
import { useParams, useLocation } from "react-router-dom";
import { Link } from "react-router-dom";
import { Button, Popover, Typography, Snackbar, Alert, Dialog, DialogTitle, DialogContent, DialogActions } from "@mui/material";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faArrowRight,
  faArrowLeft,
  faXmark,
  faCalendarCheck,
} from "@fortawesome/free-solid-svg-icons";
import { collectionService } from "../../service/collectionService";
import { useAuth } from "../../AuthContext";
import styles from "./List_Day.module.css";
import { request } from "../../utils/axiosInstance";

const List_Day = () => {
  const [days, setDays] = useState(null);
  const [courseData, setCourseData] = useState(null);
  const { courseId } = useParams();
  const containerRef = useRef(null);
  const [selectedCard, setSelectedCard] = useState(1);
  const [anchorEl, setAnchorEl] = useState(document.body);
  const [lastScrollTop, setLastScrollTop] = useState(0);
  const [userIdData, setUserIdData] = useState(null);
  const [customerId, setCustomerId] = useState(null);
  const [actualUsername, setActualUsername] = useState(null);

  const [reviewPopupOpen, setReviewPopupOpen] = useState(false);
  
  // Snackbar state for notifications
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'info' // 'error', 'warning', 'info', 'success'
  });

  // Confirmation dialog state
  const [confirmDialog, setConfirmDialog] = useState({
    open: false,
    dayId: null,
    currentStatus: false
  });

  // State to show join course dialog
  const [joinCourseDialog, setJoinCourseDialog] = useState({
    open: false,
    message: ''
  });

  const closeReviewPopup = () => {
    setReviewPopupOpen(false);
  };

  const showSnackbar = (message, severity = 'info') => {
    setSnackbar({
      open: true,
      message,
      severity
    });
  };

  const handleSnackbarClose = (event, reason) => {
    if (reason === 'clickaway') {
      return;
    }
    setSnackbar({ ...snackbar, open: false });
  };

  const showConfirmDialog = (dayId, currentStatus) => {
    setConfirmDialog({
      open: true,
      dayId,
      currentStatus
    });
  };

  const handleConfirmClose = () => {
    setConfirmDialog({
      open: false,
      dayId: null,
      currentStatus: false
    });
  };

  const handleConfirmAttendance = () => {
    if (confirmDialog.dayId !== null) {
      toggleDayCompletion(confirmDialog.dayId, confirmDialog.currentStatus);
    }
    handleConfirmClose();
  };

  const { user } = useAuth();

  useEffect(() => {
    const fetchUserData = async () => {
      try {
        const res = await request.get("/customer/detail");
        const lastRecommendedCourse =
          res.recommendedCourses && res.recommendedCourses.length > 0
            ? res.recommendedCourses[res.recommendedCourses.length - 1]
            : null;
        console.log("lastRecommendedCourse", lastRecommendedCourse);
        if (lastRecommendedCourse) {
          setUserIdData(lastRecommendedCourse.userDataId);
        }
        setCustomerId(res.customerId);
        console.log("CustomerId from API:", res.customerId);

        // Attempt to get username from response
        if (res.account && res.account.userName) {
          setActualUsername(res.account.userName);
          console.log("Actual username from API (res.account.userName):", res.account.userName);
        } else if (res.userName) {
          setActualUsername(res.userName);
          console.log("Actual username from API (res.userName):", res.userName);
        } else if (user && typeof user === 'object' && user.username) {
          // Fallback to username from useAuth if user is an object
          setActualUsername(user.username);
          console.log("Actual username from useAuth user object:", user.username);
        } else {
          console.warn("Username not found in /customer/detail response or useAuth user object.");
        }

      } catch (error) {
        console.log("Error fetching user information", error);
        
        // Try to get customerId from user object if available
        if (user && user.customerId) {
          setCustomerId(user.customerId);
        }
      }
    };

    if (user && (typeof user === 'string' || user.username)) {
      fetchUserData();
    }
  }, [user]);

  useEffect(() => {
    const getDaysOfCourse = async () => {
      console.log('=== DEBUGGING DAY COMPLETION SYSTEM ===');
      console.log('CourseId:', courseId, 'CustomerId:', customerId);
      console.log('User:', user);
      
      try {
        console.log('Attempting to load days with completion for courseId:', courseId, 'customerId:', customerId);
        // Sử dụng API mới để lấy days với completion status
        const courseWithCompletion = await collectionService.getDaysWithCompletion(courseId, customerId);
        console.log('API response courseWithCompletion:', courseWithCompletion);
        console.log('Days array from API:', courseWithCompletion.days);
        console.log('Number of days:', courseWithCompletion.days?.length);
        courseWithCompletion.days?.forEach((day, index) => {
          console.log(`Day ${index + 1}:`, day);
        });
        setCourseData(courseWithCompletion);
        setDays(courseWithCompletion.days);
        console.log('Days with completion loaded successfully:', courseWithCompletion);
      } catch (err) {
        console.log('Error loading days:', err);
        // Fallback to old API if new one fails
        try {
          console.log('Falling back to old API...');
          const daysOfCourse = await collectionService.getDays(courseId);
          console.log('Old API response:', daysOfCourse);
          
          // Use the real data from the old API without mock modifications
          const courseData = {
            courseId: parseInt(courseId),
            courseName: daysOfCourse.courseName || 'Course',
            days: daysOfCourse.days || [],
            totalDays: daysOfCourse.days?.length || 0,
            completedDays: 0,
            completionPercentage: 0.0
          };
          
          // Mark all days as not completed if not provided by API
          if (courseData.days) {
            courseData.days = courseData.days.map(day => ({
              ...day,
              isCompleted: day.isCompleted || false
            }));
          }
          
          setCourseData(courseData);
          setDays(courseData.days);
          console.log('Days loaded successfully (fallback):', courseData);
        } catch (fallbackErr) {
          console.log('Error loading days (fallback):', fallbackErr);
        }
      }
    };
    
    if (courseId) {
      getDaysOfCourse();
    }
  }, [courseId, customerId]);

  console.log(days);

  const submitReview = async (effectiveness) => {
    if (!user || !user.username || !userIdData) {
      console.log('User not logged in or userIdData not available');
      return;
    }

    try {
      const course = userIdData;

      const reviewData = {
        effective: effectiveness,
      };

      await collectionService.putReviewCourse(course, reviewData);
      closeReviewPopup();
    } catch (error) {
      console.error("Error submitting course review", error);
    }
  };

  const location = useLocation();
  const isReviewPopupVisible =
    location.pathname === `/collections/${courseId}/days/10`;

  const handleCardClick = (dayId, event) => {
    setSelectedCard(dayId);
    if (event) {
      setAnchorEl(event.currentTarget);
    }
  };

  const openPopup = (event) => {
    setAnchorEl(event ? event.currentTarget : document.body);
  };

  const closePopup = () => {
    setAnchorEl(null);
  };

  const handleScroll = useCallback(() => {
    const st = window.scrollY;
    console.log('Scroll position:', st);
  }, []);

  const showReviewPopup = () => {
    setReviewPopupOpen(true);
  };

  const toggleDayCompletion = async (dayId, currentStatus) => {
    if (!user || !customerId) {
      console.log('User not logged in hoặc thiếu customerId, không thể điểm danh', { userPresent: !!user, customerId });
      return;
    }

    try {
      // Show loading state if needed
      setDays(prevDays => 
        prevDays.map(day => 
          day.dayId === dayId 
            ? { ...day, isLoading: true }
            : day
        )
      );

      // Make the API call
      await collectionService.toggleDayCompletion(customerId, parseInt(courseId), dayId);
      
      // Optimistically update the UI
      setDays(prevDays => 
        prevDays.map(day => 
          day.dayId === dayId 
            ? { ...day, isCompleted: !currentStatus, isLoading: false }
            : day
        )
      );
      
      // Refresh the complete data
      const courseWithCompletion = await collectionService.getDaysWithCompletion(courseId, customerId);
      
      // Update both course data and days
      setCourseData(courseWithCompletion);
      setDays(courseWithCompletion.days);
      
      // Show success message
      const successMessage = !currentStatus ? 
        `Điểm danh Day ${dayId} thành công! ✅` : 
        `Đã hủy điểm danh Day ${dayId}`;
      showSnackbar(successMessage, 'success');
      
      console.log(`Day ${dayId} completion toggled successfully`);
    } catch (error) {
      console.error('Error toggling day completion:', error);
      
      // Revert the optimistic update on error
      setDays(prevDays => 
        prevDays.map(day => 
          day.dayId === dayId 
            ? { ...day, isCompleted: currentStatus, isLoading: false }
            : day
        )
      );
      
      // Hiển thị thông báo lỗi từ backend
      const errorMessage = error.response?.data?.message || 
                         'Không thể cập nhật trạng thái điểm danh. Vui lòng thử lại.';
                         
      // Check if the error is about not being enrolled in the course
      if (errorMessage.includes("chưa tham gia khóa học")) {
        // Show join course dialog instead of snackbar
        setJoinCourseDialog({
          open: true,
          message: errorMessage
        });
      } else {
        // Show regular error in snackbar
        showSnackbar(errorMessage, 'error');
      }
    }
  };

  const handleJoinCourse = () => {
    // Close dialog
    setJoinCourseDialog({
      open: false,
      message: ''
    });
    
    // Redirect to course detail page to join
    window.location.href = `/collections/${courseId}`;
  };

  useEffect(() => {
    window.addEventListener("scroll", handleScroll);
    return () => {
      window.removeEventListener("scroll", handleScroll);
    };
  }, [handleScroll]);

  return (
    <div className={styles.List_Day_container}>
      <Button onClick={openPopup} style={{ color: "#fff", fontSize: "17px" }}>
        Days
        <FontAwesomeIcon
          icon={faCalendarCheck}
          color="#37383c"
          style={{
            marginLeft: "6px",
            marginBottom: "5px",
            color: "#fff",
            fontSize: "17px",
          }}
        />
      </Button>

      {isReviewPopupVisible && user && user.username && (
        <Button
          onClick={showReviewPopup}
          style={{ color: "#fff", fontSize: "17px", marginLeft: "10px" }}
        >
          Show Review
        </Button>
      )}

      <Popover
        open={Boolean(anchorEl)}
        anchorEl={anchorEl}
        onClose={closePopup}
        disableScrollLock={true}
        classes={{
          paper: styles.popoverPaper,
        }}
        anchorOrigin={{
          vertical: "top",
          horizontal: "left",
        }}
        transformOrigin={{
          vertical: "top",
          horizontal: "left",
        }}
      >
        <div className={styles.popupContent}>
          <Typography variant="h6">Choose Days</Typography>
          {courseData && user && user.username && (
            <Typography variant="body2" className={styles.progressInfo}>
              Tiến độ: {courseData.completedDays}/{courseData.totalDays} ngày ({courseData.completionPercentage?.toFixed(1)}%)
            </Typography>
          )}
          <Button className={styles.popupClose} onClick={closePopup}>
            <FontAwesomeIcon icon={faXmark} style={{ color: "#37383c" }} />
          </Button>
          <div className={styles.popupDays}>
            {days &&
              Array.isArray(days) &&
              days.map((day) => (
                <div key={day.dayId} className={styles.dayContainer}>
                  <Link
                    to={`/collections/${courseId}/days/${day.dayId}`}
                    className={`${styles.popupDay} ${
                      selectedCard === day.dayId ? styles.selectedCard : ""
                    } ${day.isCompleted ? styles.completedDay : styles.incompleteDay}`}
                    onClick={() => {
                      handleCardClick(day.dayId);
                      closePopup();
                    }}
                  >
                    {day.dayName}
                  </Link>
                  {/* DEBUGGING: Log user and customerId */}
                  {console.log("Debug Button Display: ", { user, customerId, dayId: day.dayId })}
                  {customerId && (
                    <button
                      className={`${styles.completionButton} ${
                        day.isCompleted ? styles.completedButton : styles.incompleteButton
                      } ${day.isLoading ? styles.loadingButton : ''}`}
                      onClick={(e) => {
                        e.preventDefault();
                        e.stopPropagation();
                        if (!day.isLoading && !day.isCompleted) {
                          showConfirmDialog(day.dayId, day.isCompleted);
                        } else if (day.isCompleted) {
                          showSnackbar("Ngày này đã được điểm danh rồi", 'warning');
                        }
                      }}
                      disabled={day.isLoading || day.isCompleted}
                      title={day.isCompleted ? "Đã điểm danh" : "Chưa điểm danh"}
                    >
                      {day.isLoading ? "..." : (day.isCompleted ? "✓" : "○")}
                    </button>
                  )}
                </div>
              ))}
          </div>
        </div>
      </Popover>

      {user && user.username && (
        <Popover open={reviewPopupOpen} onClose={closeReviewPopup}>
          <div className={styles.reviewPopup}>
            <Typography variant="h6">Review Course Effectiveness</Typography>
            <div className={styles.reviewButtons}>
              <Button onClick={() => submitReview(true)}>Yes</Button>
              <Button onClick={() => submitReview(false)}>No</Button>
            </div>
          </div>
        </Popover>
      )}

      {/* Confirmation Dialog */}
      <Dialog
        open={confirmDialog.open}
        onClose={handleConfirmClose}
        aria-labelledby="confirm-dialog-title"
        PaperProps={{
          style: {
            borderRadius: '12px',
            padding: '8px'
          }
        }}
      >
        <DialogTitle id="confirm-dialog-title" sx={{ textAlign: 'center', paddingBottom: 1 }}>
          🏋️‍♀️ Xác nhận điểm danh
        </DialogTitle>
        <DialogContent sx={{ textAlign: 'center', paddingY: 2 }}>
          <Typography variant="body1">
            Bạn có muốn điểm danh Day {confirmDialog.dayId} không?
          </Typography>
        </DialogContent>
        <DialogActions sx={{ justifyContent: 'center', gap: 1, paddingBottom: 2 }}>
          <Button 
            onClick={handleConfirmClose}
            variant="outlined"
            sx={{ borderRadius: '8px', minWidth: '80px' }}
          >
            Hủy
          </Button>
          <Button 
            onClick={handleConfirmAttendance}
            variant="contained"
            color="primary"
            sx={{ borderRadius: '8px', minWidth: '80px' }}
          >
            Điểm danh
          </Button>
        </DialogActions>
      </Dialog>

      {/* Join Course Dialog */}
      <Dialog
        open={joinCourseDialog.open}
        onClose={() => setJoinCourseDialog({open: false, message: ''})}
        aria-labelledby="join-course-dialog-title"
        PaperProps={{
          style: {
            borderRadius: '12px',
            padding: '8px'
          }
        }}
      >
        <DialogTitle id="join-course-dialog-title" sx={{ textAlign: 'center', paddingBottom: 1 }}>
          🏋️‍♀️ Chưa tham gia khóa học
        </DialogTitle>
        <DialogContent sx={{ textAlign: 'center', paddingY: 2 }}>
          <Typography variant="body1">
            {joinCourseDialog.message}
          </Typography>
        </DialogContent>
        <DialogActions sx={{ justifyContent: 'center', gap: 1, paddingBottom: 2 }}>
          <Button 
            onClick={() => setJoinCourseDialog({open: false, message: ''})}
            variant="outlined"
            sx={{ borderRadius: '8px', minWidth: '80px' }}
          >
            Hủy
          </Button>
          <Button 
            onClick={handleJoinCourse}
            variant="contained"
            color="primary"
            sx={{ borderRadius: '8px', minWidth: '120px' }}
          >
            Tham gia khóa học
          </Button>
        </DialogActions>
      </Dialog>

      {/* Snackbar for notifications */}
      <Snackbar 
        open={snackbar.open} 
        autoHideDuration={4000} 
        onClose={handleSnackbarClose}
        anchorOrigin={{ vertical: 'top', horizontal: 'center' }}
      >
        <Alert 
          onClose={handleSnackbarClose} 
          severity={snackbar.severity} 
          sx={{ width: '100%' }}
          variant="filled"
        >
          {snackbar.message}
        </Alert>
      </Snackbar>
    </div>
  );
};

export default List_Day;
