import React, { useState, useEffect } from 'react';
import { 
  Card, 
  CardContent, 
  Typography, 
  Box, 
  Button, 
  Grid, 
  Avatar, 
  Chip, 
  LinearProgress,
  Alert,
  CircularProgress,
  Snackbar
} from '@mui/material';
import { 
  Star, 
  CheckCircle, 
  AccessTime, 
  Send, 
  FitnessCenter,
  Info
} from '@mui/icons-material';
import { courseService } from '../../service/courseService';
import { updatePostCourseMetrics } from '../../service/collectionService';
import { userService } from '../../service/userService';
import FitnessResultForm from '../FitnessResultForm/FitnessResultForm';
import { getImageUrl } from '../../utils/axiosInstance';
import { request } from '../../utils/axiosInstance';

const RecommendedCourses = ({ customerId }) => {
  const [recommendedCourses, setRecommendedCourses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedCourse, setSelectedCourse] = useState(null);
  const [showFitnessForm, setShowFitnessForm] = useState(false);
  const [courseCompletions, setCourseCompletions] = useState({});
  const [lastDayCompletions, setLastDayCompletions] = useState({});
  const [userData, setUserData] = useState({});
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'info'
  });
  
  const redTheme = {
    primary: '#CF4044',
    secondary: '#B8363C',
    accent: '#f39c12',
    background: '#fdf2f2',
    success: '#27ae60',
    warning: '#f39c12',
    error: '#CF4044'
  };
  
  // Fetch recommended courses and user data
  useEffect(() => {
    const fetchData = async () => {
      if (!customerId) {
        console.error('No customerId provided to RecommendedCourses:', customerId);
        setLoading(false);
        return;
      }
      
      console.log('RecommendedCourses: Starting fetchData with customerId:', customerId);
      
      try {
        setLoading(true);
        
        // Get recommended courses from API
        try {
          const recommendedCoursesResponse = await courseService.getRecommendedCourses();
          console.log('Recommended courses API response:', recommendedCoursesResponse);
          
          if (Array.isArray(recommendedCoursesResponse) && recommendedCoursesResponse.length > 0) {
            setRecommendedCourses(recommendedCoursesResponse);
            
            // Fetch completion status for the courses
            const completionPromises = recommendedCoursesResponse.map(course => 
              courseService.getCourseCompletion(course.courseId, customerId)
                .then(completion => ({ courseId: course.courseId, completion }))
                .catch(() => ({ courseId: course.courseId, completion: null }))
            );
            
            // Check if last day is completed for each course
            const lastDayPromises = recommendedCoursesResponse.map(course => 
              courseService.isLastDayCompleted(course.courseId, customerId)
                .then(isLastDayCompleted => ({ courseId: course.courseId, isLastDayCompleted }))
                .catch(() => ({ courseId: course.courseId, isLastDayCompleted: false }))
            );
            
            const completionResults = await Promise.all(completionPromises);
            const lastDayResults = await Promise.all(lastDayPromises);
            
            const completionMap = {};
            completionResults.forEach(result => {
              if (result.completion) {
                completionMap[result.courseId] = result.completion;
              }
            });
            
            const lastDayCompletionMap = {};
            lastDayResults.forEach(result => {
              lastDayCompletionMap[result.courseId] = result.isLastDayCompleted;
            });
            
            console.log('Course completions:', completionMap);
            console.log('Last day completions:', lastDayCompletionMap);
            
            setCourseCompletions(completionMap);
            setLastDayCompletions(lastDayCompletionMap);
          } else {
            console.log('No recommended courses found');
            setRecommendedCourses([]);
            
            // Debug to see what's in the customer_course table
            try {
              const debugResponse = await courseService.debugRecommendations();
              console.log('Debug recommendations:', debugResponse);
            } catch (debugErr) {
              console.error('Debug failed:', debugErr);
            }
          }
        } catch (error) {
          console.error('Error fetching recommended courses:', error);
          setError('Không thể tải khóa học được đề xuất. Vui lòng thử lại sau.');
          setRecommendedCourses([]);
        }
        
        // Get user data from the user_data_ai table
        try {
          console.log('Fetching user data for customerId:', customerId);
          const userDataResponse = await request.get(`/track-data-ai/customer/${customerId}/user-data`);
          console.log('User data from AI response:', userDataResponse);
          
          if (userDataResponse && userDataResponse.user_data_id) {
            const userData = {
              userDataId: userDataResponse.user_data_id,
              weight: userDataResponse.weight,
              chest_circumference: userDataResponse.chest_circumference,
              waist_circumference: userDataResponse.waist_circumference
            };
            console.log('Setting userData:', userData);
            setUserData(userData);
          } else {
            console.log('No user data found for this customer, response:', userDataResponse);
            setUserData({});
          }
        } catch (userDataError) {
          console.error('Error fetching user data for customerId', customerId, ':', userDataError);
          console.error('Error details:', userDataError.response?.data);
          setUserData({});
        }
        
      } catch (err) {
        console.error('Error fetching data:', err);
        setError('Không thể tải dữ liệu. Vui lòng thử lại sau.');
      } finally {
        setLoading(false);
      }
    };
    
    fetchData();
  }, [customerId]);
  
  const handleSendClick = (course) => {
    setSelectedCourse(course);
    setShowFitnessForm(true);
  };
  
  const handleFormClose = () => {
    setShowFitnessForm(false);
  };
  
  const handleFormSuccess = (response) => {
    setSnackbar({
      open: true,
      message: 'Cập nhật chỉ số thành công!',
      severity: 'success'
    });
  };
  
  const isCourseCompleted = (courseId) => {
    // Check if the last day of the course is completed
    return lastDayCompletions[courseId] === true;
  };
  
  const handleSnackbarClose = () => {
    setSnackbar({...snackbar, open: false});
  };
  
  if (loading) {
    return (
      <Box sx={{ textAlign: 'center', p: 4 }}>
        <CircularProgress sx={{ color: redTheme.primary }} />
        <Typography sx={{ mt: 2 }}>Đang tải khóa học được đề xuất...</Typography>
      </Box>
    );
  }
  
  if (error) {
    return (
      <Alert severity="error" sx={{ mt: 2 }}>
        {error}
      </Alert>
    );
  }
  
  if (recommendedCourses.length === 0) {
    return (
      <Alert 
        severity="info" 
        sx={{ 
          mt: 2, 
          display: 'flex',
          alignItems: 'center',
          padding: 3,
          borderRadius: 2 
        }}
        icon={<Info sx={{ fontSize: 30 }} />}
      >
        <Typography variant="body1" sx={{ fontWeight: 500 }}>
          Hiện tại chưa có khóa học được đề xuất
        </Typography>
      </Alert>
    );
  }
  
  return (
    <>
      <Grid container spacing={3}>
        {recommendedCourses.map(course => (
          <Grid item xs={12} key={course.courseId}>
            <Card 
              elevation={4}
              sx={{ 
                transition: 'all 0.3s',
                borderRadius: 2,
                border: `1px solid ${redTheme.background}`,
                height: '100%',
                maxWidth: '100%',
                '&:hover': { 
                  transform: 'translateY(-5px)',
                  boxShadow: `0 8px 25px ${redTheme.primary}30`
                }
              }}
            >
              <CardContent sx={{ p: 3, height: '100%', display: 'flex', flexDirection: 'column' }}>
                <Box sx={{ display: 'flex', alignItems: 'flex-start', mb: 2 }}>
                  <Avatar 
                    src={getImageUrl(course.image || course.courseImage)}
                    sx={{ 
                      width: 80, 
                      height: 80, 
                      mr: 3,
                      border: `2px solid ${redTheme.primary}`
                    }}
                  />
                  <Box sx={{ flex: 1 }}>
                    <Typography variant="h6" sx={{ fontWeight: 'bold', mb: 1, color: redTheme.primary, fontSize: '1.2rem' }}>
                      {course.courseName || course.name}
                    </Typography>
                    <Chip 
                      icon={<Star sx={{ color: '#333' }} />}
                      label="Đề xuất"
                      size="small"
                      sx={{ 
                        backgroundColor: redTheme.accent,
                        color: '#fff',
                        fontWeight: 'bold',
                        mb: 1
                      }}
                    />
                    <Typography variant="body2" sx={{ mt: 1, color: '#555' }}>
                      {course.description}
                    </Typography>
                  </Box>
                </Box>
                
                {/* Completion Status */}
                <Box sx={{ mb: 3, mt: 2 }}>
                  {courseCompletions[course.courseId] ? (
                    <Box>
                      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 1 }}>
                        <Typography variant="body2" sx={{ fontWeight: 'bold' }}>
                          Tiến độ hoàn thành:
                        </Typography>
                        <Typography variant="body2">
                          {courseCompletions[course.courseId].completedDays} / {courseCompletions[course.courseId].totalDays} ngày
                        </Typography>
                      </Box>
                      <LinearProgress 
                        variant="determinate" 
                        value={courseCompletions[course.courseId].progressPercentage} 
                        sx={{ 
                          height: 8, 
                          borderRadius: 4,
                          backgroundColor: 'rgba(0,0,0,0.1)',
                          '& .MuiLinearProgress-bar': {
                            backgroundColor: redTheme.primary
                          }
                        }}
                      />
                    </Box>
                  ) : (
                    <Box sx={{ display: 'flex', alignItems: 'center' }}>
                      <AccessTime sx={{ fontSize: 16, mr: 1, color: redTheme.secondary }} />
                      <Typography variant="body2" color="textSecondary">
                        Chưa bắt đầu
                      </Typography>
                    </Box>
                  )}
                </Box>
                
                {/* Completion Status & Send Button */}
                <Box sx={{ 
                  display: 'flex', 
                  justifyContent: 'space-between', 
                  alignItems: 'center', 
                  mt: 'auto', 
                  pt: 2, 
                  borderTop: '1px solid #eee',
                  width: '100%'
                }}>
                  <Box sx={{ display: 'flex', alignItems: 'center' }}>
                    {isCourseCompleted(course.courseId) ? (
                      <>
                        <CheckCircle sx={{ color: redTheme.success, mr: 1 }} />
                        <Typography variant="body2" sx={{ color: redTheme.success, fontWeight: 'bold' }}>
                          Đã hoàn thành
                        </Typography>
                      </>
                    ) : (
                      <>
                        <Info sx={{ color: redTheme.warning, mr: 1 }} />
                        <Typography variant="body2" color="textSecondary">
                          Chưa hoàn thành
                        </Typography>
                      </>
                    )}
                  </Box>
                  
                  <Button
                    variant="contained"
                    color="primary"
                    startIcon={<Send />}
                    onClick={() => handleSendClick(course)}
                    sx={{
                      backgroundColor: redTheme.primary,
                      '&:hover': {
                        backgroundColor: redTheme.secondary
                      }
                    }}
                  >
                    Gửi
                  </Button>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
      
      {/* Fitness Result Form Dialog */}
      {selectedCourse && (
        <FitnessResultForm 
          open={showFitnessForm}
          onClose={handleFormClose}
          userDataId={userData?.userDataId}
          initialData={{
            weight: userData?.weight,
            chest_circumference: userData?.chest_circumference,
            waist_circumference: userData?.waist_circumference
          }}
          disabled={!isCourseCompleted(selectedCourse.courseId)}
          onSuccess={handleFormSuccess}
        />
      )}
      
      {/* Success Snackbar */}
      <Snackbar
        open={snackbar.open}
        autoHideDuration={6000}
        onClose={handleSnackbarClose}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
      >
        <Alert 
          onClose={handleSnackbarClose} 
          severity={snackbar.severity} 
          sx={{ width: '100%' }}
        >
          {snackbar.message}
        </Alert>
      </Snackbar>
    </>
  );
};

export default RecommendedCourses; 