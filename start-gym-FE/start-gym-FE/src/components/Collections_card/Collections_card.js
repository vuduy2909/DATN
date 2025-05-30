import React, { useState, useEffect } from 'react';
import { Link } from "react-router-dom";
import styles from "./Collections_card.module.css";
import Card from "react-bootstrap/Card";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { collectionService } from "../../service/collectionService";
import { faStar, faCrown, faInfoCircle, faShoppingCart, faClock, faUser, faDumbbell } from "@fortawesome/free-solid-svg-icons";
import { 
  Button,
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
import { getCsrfToken } from '../../utils/axiosInstance';
import { Tooltip } from '@mui/material';

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
  courseData = null,  // Add courseData prop for VIP info
  isPurchasedVip = false  // Thêm prop mới
}) => {
  const { user } = useAuth();
  const [isDeleting, setIsDeleting] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [paymentLoading, setPaymentLoading] = useState(false);
  
  // Debug log
  console.log(`CollectionCard ${id} - ${title}:`, { 
    isUserCourse, 
    isVipCourse: courseData?.isVip, 
    isPurchasedVip 
  });
  
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

      const response = await request.post('/payment/create', paymentData);
      
      if (response && response.paymentUrl) {
        showSnackbar('Đang chuyển hướng đến trang thanh toán...', 'info');
        setTimeout(() => {
          window.location.href = response.paymentUrl;
        }, 1000);
      } else {
        throw new Error('Không nhận được URL thanh toán từ server');
      }

    } catch (error) {
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
        } else {
            // Response không có success flag hoặc message
            throw new Error('Không nhận được xác nhận từ server');
        }
    } catch (error) {
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
                {isUserCourse || isPurchasedVip ? 'Tiếp tục học' : (
                  <>
                    Xem<br/>chi tiết
                  </>
                )}
              </button>
            </Link>
            {!isUserCourse && !isPurchasedVip ? (
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
          {!isPurchasedVip && (
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
          )}
          {isPurchasedVip && (
            <Link to={`/collections/${id}/days/1`} style={{ textDecoration: 'none' }}>
              <Button 
                variant="contained" 
                sx={{
                  backgroundColor: '#4CAF50',
                  color: 'white',
                  fontWeight: 'bold',
                  height: '48px',
                  minWidth: '100px',
                  '&:hover': {
                    backgroundColor: '#388E3C'
                  }
                }}
              >
                Tiếp tục học
              </Button>
            </Link>
          )}
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

  // Debug log for recommended courses
  console.log('Collections_card received recommendedCourses:', recommendedCourses);

  // Debug purchased VIP courses
  useEffect(() => {
    if (userCourses && userCourses.length > 0) {
      const purchasedVipCourses = userCourses.filter(course => course.isVip === true);
      console.log('Purchased VIP courses:', purchasedVipCourses);
    }
  }, [userCourses]);

  const getCardsFunction = async () => {
    try {
      setLoadingCourses(true);
      setError(null);
      const cards = await collectionService.getCards();
      
      if (!cards) {
        setError("No courses data received from server");
        setDataCourse([]);
        return;
      }
      
      if (!Array.isArray(cards)) {
        setError("Invalid courses data format received");
        setDataCourse([]);
        return;
      }

      if (cards.length === 0) {
        setDataCourse([]);
        setError(null); // Don't set error for empty array
        return;
      }

      setDataCourse(cards);
      setError(null);
    } catch (error) {
      const errorMessage = error.response?.status === 500 
        ? "Server error - please check if the database is running"
        : error.response?.status === 404
        ? "Course API endpoint not found"
        : error.message || "Error loading courses";
        
      setError(errorMessage);
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
    
    // Lấy danh sách courseId của các khóa học người dùng đã mua
    const purchasedCourseIds = userCourses.map(course => course.courseId);
    console.log('Purchased course IDs:', purchasedCourseIds);
    
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
          price: userCourse.price,
          // Khóa học VIP trong My Course đã được mua rồi
          isPurchasedVip: userCourse.isVip === true
        }));
        
        // Sắp xếp: khóa học VIP hiển thị đầu tiên
        filtered.sort((a, b) => {
          if (a.isVip && !b.isVip) return -1;
          if (!a.isVip && b.isVip) return 1;
          return 0;
        });
      } else {
        // No user courses, show empty state
        filtered = [];
      }
    } else if (selectedCategory === "All") {
      // Đánh dấu các khóa học đã mua trong tab All
      filtered = dataCourse.map(course => {
        const isPurchased = purchasedCourseIds.includes(course.courseId);
        return {
          ...course,
          isUserCourse: isPurchased,
          // Nếu khóa học đã mua và là VIP, đánh dấu là VIP đã mua
          isPurchasedVip: isPurchased && course.isVip === true
        };
      });
      
      // Sắp xếp: khóa học VIP hiển thị đầu tiên
      filtered.sort((a, b) => {
        if (a.isVip && !b.isVip) return -1;
        if (!a.isVip && b.isVip) return 1;
        return 0;
      });
    } else {
      filtered = dataCourse.filter(course => {
        const typeName = course.courseType?.courseTypeName;
        return typeName === selectedCategory;
      }).map(course => {
        const isPurchased = purchasedCourseIds.includes(course.courseId);
        return {
          ...course,
          isUserCourse: isPurchased,
          // Nếu khóa học đã mua và là VIP, đánh dấu là VIP đã mua
          isPurchasedVip: isPurchased && course.isVip === true
        };
      });
      
      // Sắp xếp: khóa học VIP hiển thị đầu tiên
      filtered.sort((a, b) => {
        if (a.isVip && !b.isVip) return -1;
        if (!a.isVip && b.isVip) return 1;
        return 0;
      });
    }
    
    console.log('Filtered cards:', filtered);
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
            isPurchasedVip={course.isPurchasedVip || false}
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