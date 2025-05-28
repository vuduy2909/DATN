import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import styles from "./Collections_card.module.css";
import Card from "react-bootstrap/Card";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { collectionService } from "../../service/collectionService";
import { faStar, faCrown, faInfoCircle, faShoppingCart, faClock, faUser, faDumbbell } from "@fortawesome/free-solid-svg-icons";
import Button from "@mui/material/Button";
import { 
  CircularProgress, 
  Snackbar, 
  Alert, 
  Dialog, 
  DialogTitle, 
  DialogContent, 
  DialogActions,
  Typography,
  Box,
  Chip,
  Divider
} from "@mui/material";
import CourseJoinButton from "../Course/CourseJoinButton";
import { useAuth } from '../../AuthContext';
import { request } from '../../utils/axiosInstance';

const CollectionCard = ({ 
  id, 
  title, 
  image, 
  duration, 
  onJoinSuccess, 
  isUserCourse = false, 
  joinType = null, 
  isRecommended = false,
  onDeleteCourse,
  courseData = null  // Add courseData prop for VIP info
}) => {
  const { user } = useAuth();
  const [isDeleting, setIsDeleting] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [paymentLoading, setPaymentLoading] = useState(false);
  
  // Dialog states
  const [showInfoDialog, setShowInfoDialog] = useState(false);
  const [showPurchaseDialog, setShowPurchaseDialog] = useState(false);
  
  // Snackbar state for notifications
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'info'
  });

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

  // Check if this is a VIP course
  const isVipCourse = courseData && courseData.isVip === true && courseData.price && Number(courseData.price) > 0;
  
  // Check if this is a purchased VIP course (in My Course context)
  const isPurchasedVip = isUserCourse && courseData && courseData.isVip === true;
  
  // Debug VIP course detection
  console.log(`Course ${id} (${title}) debug:`, {
    courseData,
    isVipCourse,
    isPurchasedVip,
    isUserCourse,
    hasPrice: courseData?.price,
    courseDataIsVip: courseData?.isVip
  });

  // Special debug for course 65
  if (id === 65) {
    console.log('=== COURSE 65 DETAILED DEBUG ===');
    console.log('Raw courseData:', JSON.stringify(courseData, null, 2));
    console.log('courseData.isVip:', courseData?.isVip, typeof courseData?.isVip);
    console.log('courseData.price:', courseData?.price, typeof courseData?.price);
    console.log('isVipCourse calculation:', courseData && courseData.isVip === true && courseData.price && Number(courseData.price) > 0);
    console.log('Should show VIP buttons?', isVipCourse && !isPurchasedVip);
  }

  const formatPrice = (price) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(price);
  };

  const handleInfoClick = () => {
    setShowInfoDialog(true);
  };

  const handlePurchaseClick = () => {
    if (!user) {
      showSnackbar('Vui lòng đăng nhập để mua khóa học', 'error');
      return;
    }
    setShowPurchaseDialog(true);
  };

  const handleConfirmPurchase = async () => {
    try {
      setPaymentLoading(true);
      setShowPurchaseDialog(false);
      
      const paymentData = {
        courseId: parseInt(id), // Ensure it's a number
        amount: parseFloat(courseData.price), // Convert to number
        currency: 'VND',
        orderInfo: `VIP Course Payment: ${title}`,
        returnUrl: window.location.origin + '/payment/result'
      };

      console.log('Payment data:', paymentData);
      const response = await request.post('/payment/create', paymentData);
      console.log('Payment response:', response);
      
      if (response && response.paymentUrl) {
        showSnackbar('Đang chuyển hướng đến trang thanh toán...', 'info');
        setTimeout(() => {
          window.location.href = response.paymentUrl;
        }, 1000);
      } else {
        throw new Error('Không nhận được URL thanh toán từ server');
      }

    } catch (error) {
      console.error('Payment error:', error);
      
      let errorMessage = 'Lỗi khi tạo thanh toán. Vui lòng thử lại.';
      
      if (error.response?.data) {
        const responseData = error.response.data;
        if (typeof responseData === 'string') {
          errorMessage = responseData;
        } else if (responseData.message) {
          errorMessage = responseData.message;
        } else if (responseData.error) {
          errorMessage = responseData.error;
        }
      } else if (error.message && error.message !== 'Network Error') {
        errorMessage = error.message;
      }
      
      showSnackbar(errorMessage, 'error');
    } finally {
      setPaymentLoading(false);
    }
  };

  const renderRating = () => {
    const stars = [];
    const ratingValue = parseInt(duration) || 3; // Default to 3 if can't parse
    
    for (let i = 1; i <= 5; i++) {
      if (i <= ratingValue) {
        stars.push(
          <span key={i}>
            <FontAwesomeIcon icon={faStar} key={i} className={styles.star} />
          </span>
        );
      } else {
        stars.push(
          <span key={i}>
            <FontAwesomeIcon
              icon={faStar}
              key={stars.length}
              className={styles.star}
            />
          </span>
        );
      }
    }

    return stars;
  };

  const handleDeleteClick = () => {
    setShowDeleteModal(true);
  };

  const handleConfirmDelete = async () => {
    setIsDeleting(true);
    setShowDeleteModal(false);
    
    try {
        const response = await collectionService.deleteUserCourse(id);
        
        // Xử lý response thành công
        if (response && (response.success || response.message)) {
            // Callback để update UI parent component
            if (onDeleteCourse) {
                onDeleteCourse(id);
            }
            showSnackbar(`Đã xoá khóa học "${title}" thành công! 🗑️`, 'success');
            console.log('Course deleted successfully:', response);
        } else {
            // Response không có success flag hoặc message
            throw new Error('Không nhận được xác nhận từ server');
        }
    } catch (error) {
        console.error('Error deleting course:', error);
        
        let errorMessage = 'Có lỗi xảy ra khi xoá khóa học. Vui lòng thử lại sau.';
        
        if (error.response?.data) {
            // Lỗi từ server với data response
            const responseData = error.response.data;
            if (typeof responseData === 'string') {
                errorMessage = responseData;
            } else if (responseData.message) {
                errorMessage = responseData.message;
            } else if (responseData.error) {
                errorMessage = responseData.error;
            }
        } else if (error.message && error.message !== 'Network Error') {
            // Lỗi JavaScript hoặc custom error
            errorMessage = error.message;
        }
        
        showSnackbar(errorMessage, 'error');
    } finally {
        setIsDeleting(false);
    }
  };

  const handleCancelDelete = () => {
    setShowDeleteModal(false);
  };

  return (
    <Card key={id} className={styles.collection_card}>
      <div className={styles.card_header}>
        <Card.Img variant="top" src={image || 'https://via.placeholder.com/300x180?text=Course+Image'} className={styles.img} />
        {isUserCourse && joinType && (
          <div className={styles.course_badge}>
            <span className={`${styles.badge} ${isRecommended ? styles.recommended : styles.self_enrolled}`}>
              {joinType}
            </span>
          </div>
        )}
        {(isVipCourse || isPurchasedVip) && (
          <div className={styles.vip_badge}>
            <Chip
              label="VIP"
              sx={{
                backgroundColor: '#FFD700',
                color: '#000',
                fontWeight: 'bold',
                fontSize: '12px'
              }}
            />
          </div>
        )}
      </div>
      
      <Card.Body className={styles.card_body}>
        <Card.Title>{title}</Card.Title>
        <div className={styles.rating}>
          {renderRating()}
          <p style={{ marginLeft: "10px" }}>5 rating</p>
        </div>
        <svg
          className={styles.line}
          xmlns="http://www.w3.org/2000/svg"
          width="414"
          height="2"
          viewBox="0 0 414 2"
          fill="none"
        >
          <path d="M0 1H413.5" stroke="#6A6B6C" strokeWidth="0.5" />
        </svg>
        
          <p className={styles.collection_duration}>Duration: {duration || 'Unknown'}</p>
        
        {isVipCourse && !isPurchasedVip && courseData.price && (
          <p className={styles.vip_price}>
            <strong style={{ color: '#FF4444', fontSize: '18px' }}>
              {formatPrice(courseData.price)}
            </strong>
          </p>
        )}
        
          <div className={styles.course_actions}>
          {isVipCourse && !isPurchasedVip ? (
            // VIP Course not purchased yet - show purchase buttons
            <>
              <Button
                variant="outlined"
                size="small"
                onClick={handleInfoClick}
                sx={{ 
                  color: '#FFD700',
                  borderColor: '#FFD700',
                  height: '48px',
                  minWidth: '100px',
                  display: 'flex',
                  flexDirection: 'row',
                  alignItems: 'center',
                  justifyContent: 'flex-start',
                  paddingLeft: '8px',
                  paddingRight: '8px',
                  gap: '6px',
                  '&:hover': {
                    borderColor: '#FFC000',
                    backgroundColor: 'rgba(255, 215, 0, 0.1)'
                  }
                }}
              >
                <FontAwesomeIcon icon={faInfoCircle} style={{ fontSize: '14px', flexShrink: 0 }} />
                <Box sx={{ 
                  display: 'flex', 
                  flexDirection: 'column', 
                  alignItems: 'center',
                  justifyContent: 'center',
                  lineHeight: 1,
                  fontSize: '11px',
                  fontWeight: 500
                }}>
                  <div>XEM</div>
                  <div>THÔNG TIN</div>
                </Box>
              </Button>
              <Button
                variant="contained"
                size="small"
                onClick={handlePurchaseClick}
                disabled={paymentLoading}
                sx={{
                  backgroundColor: '#FFD700',
                  color: '#000',
                  fontWeight: 'bold',
                  height: '48px',
                  minWidth: '100px',
                  display: 'flex',
                  flexDirection: 'row',
                  alignItems: 'center',
                  justifyContent: 'flex-start',
                  paddingLeft: '8px',
                  paddingRight: '8px',
                  gap: '6px',
                  '&:hover': {
                    backgroundColor: '#FFC000'
                  }
                }}
              >
                {paymentLoading ? (
                  <>
                    <CircularProgress size={14} style={{ flexShrink: 0 }} />
                    <Box sx={{ 
                      display: 'flex', 
                      flexDirection: 'column', 
                      alignItems: 'center',
                      justifyContent: 'center',
                      lineHeight: 1,
                      fontSize: '11px',
                      fontWeight: 500
                    }}>
                      <div>ĐANG</div>
                      <div>XỬ LÝ...</div>
                    </Box>
                  </>
                ) : (
                  <>
                    <FontAwesomeIcon icon={faShoppingCart} style={{ fontSize: '14px', flexShrink: 0 }} />
                    <Box sx={{ 
                      display: 'flex', 
                      flexDirection: 'column', 
                      alignItems: 'center',
                      justifyContent: 'center',
                      lineHeight: 1,
                      fontSize: '11px',
                      fontWeight: 500
                    }}>
                      <div>MUA</div>
                      <div>KHÓA HỌC</div>
                    </Box>
                  </>
                )}
              </Button>
            </>
          ) : (
            // Regular course or purchased VIP course - show study/join buttons
            <>
              <Link to={`/collections/${id}/days/1`} className={styles.link}>
                <button className={isPurchasedVip ? styles.detail_btn_center : styles.detail_btn}>
                {isUserCourse ? 'Tiếp tục học' : (
                  <>
                    Xem<br/>chi tiết
                  </>
                )}
              </button>
            </Link>
            {!isUserCourse ? (
              <CourseJoinButton 
                courseId={id}
                courseName={title}
                onJoinSuccess={onJoinSuccess}
              />
            ) : (
                // Only show delete button for non-VIP courses
                !isPurchasedVip && (
              <button 
                className={styles.delete_btn}
                onClick={handleDeleteClick}
                disabled={isDeleting}
              >
                {isDeleting ? 'Đang xoá...' : 'Xoá'}
              </button>
                )
              )}
            </>
            )}
        </div>
      </Card.Body>

      {/* VIP Course Info Dialog */}
      <Dialog 
        open={showInfoDialog} 
        onClose={() => setShowInfoDialog(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <FontAwesomeIcon icon={faCrown} style={{ color: '#FFD700' }} />
          Thông tin khóa học VIP
        </DialogTitle>
        <DialogContent>
          <Box sx={{ mb: 2 }}>
            <img 
              src={image || 'https://via.placeholder.com/400x200?text=Course+Image'} 
              alt={title}
              style={{ width: '100%', maxHeight: '200px', objectFit: 'cover', borderRadius: '8px' }}
            />
          </Box>
          
          <Typography variant="h5" gutterBottom color="primary">
            {title}
          </Typography>
          
          {courseData && (
            <>
              <Typography variant="body1" paragraph>
                {courseData.description || 'Khóa học VIP chuyên nghiệp với nội dung nâng cao và hướng dẫn chi tiết.'}
              </Typography>
              
              <Divider sx={{ my: 2 }} />
              
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <FontAwesomeIcon icon={faClock} />
                  <Typography><strong>Thời lượng:</strong> {duration}</Typography>
                </Box>
                
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <FontAwesomeIcon icon={faDumbbell} />
                  <Typography><strong>Độ khó:</strong> {courseData.difficulty || 'Nâng cao'}</Typography>
                </Box>
                
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <FontAwesomeIcon icon={faUser} />
                  <Typography><strong>Phù hợp với:</strong> {courseData.recommendedFor || 'Fit'}</Typography>
                </Box>
                
                {courseData.courseType && (
                  <Box sx={{ mt: 1 }}>
                    <Chip 
                      label={courseData.courseType.courseTypeName} 
                      variant="outlined" 
                      color="primary"
                    />
                  </Box>
                )}
              </Box>
              
              <Divider sx={{ my: 2 }} />
              
              <Box sx={{ textAlign: 'center' }}>
                <Typography variant="h4" sx={{ color: '#FF4444', fontWeight: 'bold' }}>
                  {formatPrice(courseData.price)}
                </Typography>
              </Box>
            </>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setShowInfoDialog(false)}>Đóng</Button>
          <Button 
            variant="contained" 
            onClick={() => {
              setShowInfoDialog(false);
              handlePurchaseClick();
            }}
            sx={{
              backgroundColor: '#FFD700',
              color: '#000',
              fontWeight: 'bold',
              height: '48px',
              minWidth: '100px',
              display: 'flex',
              flexDirection: 'row',
              alignItems: 'center',
              justifyContent: 'flex-start',
              paddingLeft: '8px',
              paddingRight: '8px',
              gap: '6px',
              '&:hover': {
                backgroundColor: '#FFC000'
              }
            }}
          >
            <FontAwesomeIcon icon={faShoppingCart} style={{ fontSize: '14px', flexShrink: 0 }} />
            <Box sx={{ 
              display: 'flex', 
              flexDirection: 'column', 
              alignItems: 'center',
              justifyContent: 'center',
              lineHeight: 1,
              fontSize: '11px',
              fontWeight: 500
            }}>
              <div>MUA</div>
              <div>NGAY</div>
            </Box>
          </Button>
        </DialogActions>
      </Dialog>

      {/* Purchase Confirmation Dialog */}
      <Dialog 
        open={showPurchaseDialog} 
        onClose={() => setShowPurchaseDialog(false)}
        maxWidth="xs"
        fullWidth
      >
        <DialogTitle>Xác nhận mua khóa học</DialogTitle>
        <DialogContent>
          <Typography variant="body1" gutterBottom>
            Bạn muốn mua khóa học nâng cao này?
          </Typography>
          <Typography variant="h6" sx={{ color: '#FF4444', fontWeight: 'bold', mt: 2 }}>
            {title}
          </Typography>
          {courseData && (
            <Typography variant="h5" sx={{ color: '#FF4444', fontWeight: 'bold', mt: 1 }}>
              {formatPrice(courseData.price)}
            </Typography>
          )}
          <Typography variant="body2" color="text.secondary" sx={{ mt: 2 }}>
            Bạn sẽ được chuyển đến trang thanh toán VNPay để hoàn tất giao dịch.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setShowPurchaseDialog(false)}>Hủy</Button>
          <Button 
            variant="contained" 
            onClick={handleConfirmPurchase}
            disabled={paymentLoading}
            sx={{
              backgroundColor: '#FFD700',
              color: '#000',
              fontWeight: 'bold',
              height: '48px',
              minWidth: '100px',
              display: 'flex',
              flexDirection: 'row',
              alignItems: 'center',
              justifyContent: 'flex-start',
              paddingLeft: '8px',
              paddingRight: '8px',
              gap: '6px',
              '&:hover': {
                backgroundColor: '#FFC000'
              }
            }}
          >
            {paymentLoading ? (
              <>
                <CircularProgress size={14} style={{ flexShrink: 0 }} />
                <Box sx={{ 
                  display: 'flex', 
                  flexDirection: 'column', 
                  alignItems: 'center',
                  justifyContent: 'center',
                  lineHeight: 1,
                  fontSize: '11px',
                  fontWeight: 500
                }}>
                  <div>ĐANG</div>
                  <div>XỬ LÝ...</div>
                </Box>
              </>
            ) : (
              <>
                <FontAwesomeIcon icon={faShoppingCart} style={{ fontSize: '14px', flexShrink: 0 }} />
                <Box sx={{ 
                  display: 'flex', 
                  flexDirection: 'column', 
                  alignItems: 'center',
                  justifyContent: 'center',
                  lineHeight: 1,
                  fontSize: '11px',
                  fontWeight: 500
                }}>
                  <div>XÁC NHẬN</div>
                  <div>MUA</div>
                </Box>
              </>
            )}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Modal */}
      {showDeleteModal && (
        <div className={styles.modal_overlay}>
          <div className={styles.modal_content}>
            <h3>Xác nhận xoá khóa học</h3>
            <p>
              Bạn có chắc chắn muốn xoá khóa học <strong>"{title}"</strong> khỏi danh sách của mình không?
              <br />
              Hành động này không thể hoàn tác.
            </p>
            <div className={styles.modal_actions}>
              <button 
                className={styles.confirm_delete_btn}
                onClick={handleConfirmDelete}
              >
                Có, xoá khóa học
              </button>
              <button 
                className={styles.cancel_btn}
                onClick={handleCancelDelete}
              >
                Hủy
              </button>
            </div>
          </div>
        </div>
      )}

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
    </Card>
  );
};

const Collections_card = ({ selectedCategory, recommendedCourses, loading, onCourseJoinSuccess }) => {
  const { user } = useAuth();
  const [dataCourse, setDataCourse] = useState([]);
  const [loadingCourses, setLoadingCourses] = useState(true);
  const [error, setError] = useState(null);
  const [filteredCards, setFilteredCards] = useState([]);
  const [userCourses, setUserCourses] = useState(recommendedCourses || []);

  const getCardsFunction = async () => {
    try {
      setLoadingCourses(true);
      setError(null);
      console.log("Starting to fetch courses...");
      const cards = await collectionService.getCards();
      console.log("API Response:", cards);
      
      if (!cards) {
        console.error("No cards data received from API");
        setError("No courses data received");
        return;
      }
      
      if (!Array.isArray(cards)) {
        console.error("Received data is not an array:", cards);
        setError("Invalid courses data format");
        return;
      }

      if (cards.length === 0) {
        console.log("Received empty courses array");
        setError("No courses available");
        return;
      }

      console.log("Successfully loaded", cards.length, "courses");
      setDataCourse(cards);
    } catch (error) {
      console.error("Error details:", {
        message: error.message,
        stack: error.stack,
        response: error.response
      });
      setError(error.message || "Error loading courses");
      setDataCourse([]);
    } finally {
      setLoadingCourses(false);
    }
  };

  // Handle course deletion
  const handleDeleteCourse = (courseId) => {
    // Remove course from userCourses state
    const updatedUserCourses = userCourses.filter(course => course.courseId !== courseId);
    setUserCourses(updatedUserCourses);
    
    // Also update filteredCards if we're in My Course tab
    if (selectedCategory === "My Course") {
      const updatedFilteredCards = filteredCards.filter(course => course.courseId !== courseId);
      setFilteredCards(updatedFilteredCards);
    }
    
    console.log(`Course ${courseId} deleted successfully`);
  };

  // Load courses when component mounts
  useEffect(() => {
    getCardsFunction();
  }, []);

  // Update userCourses when recommendedCourses prop changes
  useEffect(() => {
    setUserCourses(recommendedCourses || []);
  }, [recommendedCourses]);

  // Filter courses whenever the category changes or courses/recommended courses update
  useEffect(() => {
    if (!dataCourse || dataCourse.length === 0) return;
    
    console.log("Filtering with category:", selectedCategory);
    console.log("Available courses:", dataCourse);
    console.log("User courses:", userCourses);
    
    let filtered;
    
    if (selectedCategory === "My Course") {
      // For My Course tab, show user's courses if any exist
      if (userCourses && userCourses.length > 0) {
        // Map user's courses to match the expected format
        filtered = userCourses.map(userCourse => ({
          courseId: userCourse.courseId,
          courseName: userCourse.courseName,
          description: userCourse.description,
          duration: userCourse.duration,
          image: userCourse.image,
          courseType: userCourse.courseType,
          isUserCourse: true,
          joinType: userCourse.joinType || (userCourse.isRecommended ? 'Recommended' : 'Self-enrolled'),
          isRecommended: userCourse.isRecommended,
          // Đảm bảo thông tin VIP được truyền đúng
          isVip: userCourse.isVip || false,
          price: userCourse.price
        }));
        console.log("Showing user's courses in My Course tab:", filtered);
      } else {
        // No user courses, show empty state
        filtered = [];
        console.log("No user courses found for My Course tab");
      }
    } else if (selectedCategory === "All") {
      filtered = dataCourse;
    } else {
      filtered = dataCourse.filter(course => {
        const typeName = course.courseType?.courseTypeName;
        return typeName === selectedCategory;
      });
    }
    
    setFilteredCards(filtered || []);
  }, [selectedCategory, dataCourse, userCourses]);

  if (loading || loadingCourses) {
    return (
      <div className={styles.loading_container}>
        <CircularProgress color="error" />
        <p>Đang tải khóa học...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className={styles.error_container}>
        <p className={styles.error_message}>{error}</p>
        <Button 
          variant="contained" 
          color="error" 
          onClick={getCardsFunction}
          style={{ marginTop: '10px' }}
        >
          Thử lại
        </Button>
      </div>
    );
  }

  const isMyCourseWithNoData = selectedCategory === "My Course" && filteredCards.length === 0;

  return (
    <div className={styles.collection}>
      {filteredCards.length > 0 ? (
        filteredCards.map((course) => (
          <CollectionCard
            key={course.courseId}
            id={course.courseId}
            title={course.courseName}
            description={course.description}
            duration={course.duration}
            image={course.image}
            isUserCourse={course.isUserCourse || false}
            joinType={course.joinType}
            isRecommended={course.isRecommended || false}
            onJoinSuccess={onCourseJoinSuccess}
            onDeleteCourse={handleDeleteCourse}
            courseData={course}
          />
        ))
      ) : (
        <div className={styles.bn_left}>
          <div className={styles.bn_left_1}>
            <p className={styles.typo}>
              <span className={styles.typo_red}>
                {isMyCourseWithNoData ? "Chưa có" : "No"}
              </span> {isMyCourseWithNoData ? "khóa học nào" : "courses found"}
            </p>
            <p className={styles.typo_des}>
              {isMyCourseWithNoData ? (
                <>
                  <span>Bạn chưa tham gia khóa học nào. Hãy khám phá các khóa học khác!</span>
                  <Button variant="contained" className={styles.bn_left_btn}>
                    <Link to="/recommend" style={{ color: "#fff" }}>
                      Nhận gợi ý
                    </Link>
                  </Button>
                </>
              ) : (
                <>
                  <Button variant="contained" className={styles.bn_left_btn}>
                    <Link to="/recommend" style={{ color: "#fff" }}>
                      Start now
                    </Link>
                  </Button>
                  <span>to receive personalized recommendations.</span>
                </>
              )}
            </p>
          </div>
        </div>
      )}
    </div>
  );
};

export default Collections_card;