import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Box,
  Paper,
  Typography,
  Grid,
  Card,
  CardContent,
  Avatar,
  Chip,
  LinearProgress,
  Tab,
  Tabs,
  Button,
  IconButton,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  Divider,
  CircularProgress,
  Alert
} from '@mui/material';
import {
  ArrowBack as ArrowBackIcon,
  FitnessCenter as FitnessCenterIcon,
  School as SchoolIcon,
  CheckCircle as CheckCircleIcon,
  ExpandMore as ExpandMoreIcon,
  TrendingUp as TrendingUpIcon,
  Schedule as ScheduleIcon,
  Assignment as AssignmentIcon,
  MonetizationOn as MoneyIcon
} from '@mui/icons-material';
import { axiosInstance, getImageUrl } from '../../utils/axiosInstance';
import { useDialogs } from '../../components/MaterialUIDialogs';
import { useLanguage } from '../../contexts/LanguageContext';

const CustomerCourses = () => {
  const { customerId } = useParams();
  const navigate = useNavigate();
  const { showAlert, showLoading, hideLoading, DialogComponents } = useDialogs();
  const { language, t } = useLanguage();
  
  const [customer, setCustomer] = useState(null);
  const [courses, setCourses] = useState([]);
  const [selectedTab, setSelectedTab] = useState(0);
  const [courseProgress, setCourseProgress] = useState({});
  const [bodyMetrics, setBodyMetrics] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadingBodyMetrics, setLoadingBodyMetrics] = useState(false);
  const [error, setError] = useState(null);
  const [expandedAccordion, setExpandedAccordion] = useState(null);



  useEffect(() => {
    fetchCustomerCourses();
  }, [customerId]);

  const fetchCustomerCourses = async () => {
    try {
      setLoading(true);
      console.log('Fetching customer courses - Token:', localStorage.getItem('token'));
      const response = await axiosInstance.get(`/admin/customers/${customerId}/courses`);
      
      setCourses(response.data.courses || []);
      setCustomer(response.data.customer || null);
      setError(null);
    } catch (error) {
      console.error('Error fetching customer courses:', error);
      console.error('Error response:', error.response);
      setError(error.response?.data?.error || 'Failed to fetch customer courses');
      showAlert('Error', 'Failed to fetch customer courses', 'error');
    } finally {
      setLoading(false);
    }
  };

  const fetchBodyMetrics = async () => {
    try {
      setLoadingBodyMetrics(true);
      console.log('Fetching body metrics for customer:', customerId);
      const response = await axiosInstance.get(`/admin/customers/${customerId}/body-metrics`);
      
      setBodyMetrics(response.data.bodyMetrics || []);
      console.log('Body metrics loaded:', response.data.bodyMetrics);
    } catch (error) {
      console.error('Error fetching body metrics:', error);
      showAlert('Error', 'Failed to fetch body metrics', 'error');
      setBodyMetrics([]);
    } finally {
      setLoadingBodyMetrics(false);
    }
  };

  const fetchCourseProgress = async (courseId) => {
    try {
      console.log(`Fetching progress for course ${courseId} of customer ${customerId}`);
      console.log('Token in localStorage:', localStorage.getItem('token'));
      console.log('Is admin in localStorage:', localStorage.getItem('isAdmin'));
      
      // Test basic auth first
      console.log('Testing auth with simple endpoint...');
      try {
        const testResponse = await axiosInstance.get('/admin/courses');
        console.log('Auth test passed:', testResponse.status);
      } catch (authError) {
        console.error('Auth test failed:', authError);
        showAlert('Error', 'Authentication failed. Please login again.', 'error');
        return;
      }
      
      const response = await axiosInstance.get(`/admin/customers/${customerId}/courses/${courseId}/progress`);
      console.log('Progress response:', response.data);
      
      setCourseProgress(prev => ({
        ...prev,
        [courseId]: response.data
      }));
    } catch (error) {
      console.error('Error fetching course progress:', error);
      console.error('Error response:', error.response);
      console.error('Error status:', error.response?.status);
      console.error('Error data:', error.response?.data);
      showAlert('Error', 'Failed to fetch course progress', 'error');
    }
  };

  const handleAccordionChange = (courseId) => (event, isExpanded) => {
    setExpandedAccordion(isExpanded ? courseId : null);
    
    if (isExpanded && !courseProgress[courseId]) {
      fetchCourseProgress(courseId);
    }
  };

  const handleTabChange = (event, newValue) => {
    setSelectedTab(newValue);
    
    // Fetch body metrics when switching to Body Metrics tab
    if (newValue === 2 && bodyMetrics.length === 0) {
      fetchBodyMetrics();
    }
  };

  const handleViewProgress = (courseId) => {
    if (!courseProgress[courseId]) {
      fetchCourseProgress(courseId);
    }
    setSelectedTab(1); // Chuyển sang tab "Progress & Attendance"
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'completed':
        return 'success';
      case 'in-progress':
        return 'primary';
      case 'enrolled':
        return 'warning';
      default:
        return 'default';
    }
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(amount);
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString('vi-VN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const TabPanel = ({ children, value, index, ...other }) => (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`customer-courses-tabpanel-${index}`}
      aria-labelledby={`customer-courses-tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
    </div>
  );

  const renderCourseCard = (course) => (
    <Card key={course.courseId} sx={{ mb: 2, elevation: 2 }}>
      <CardContent>
        <Grid container spacing={2} alignItems="center">
          <Grid item>
            <Avatar
              src={getImageUrl(course.image)}
              variant="rounded"
              sx={{ width: 80, height: 80 }}
            >
              <SchoolIcon />
            </Avatar>
          </Grid>
          
          <Grid item xs>
            <Box display="flex" justifyContent="space-between" alignItems="flex-start">
              <Box>
                <Typography variant="h6" fontWeight="bold" gutterBottom>
                  {course.courseName}
                </Typography>
                
                <Box display="flex" gap={1} mb={1} flexWrap="wrap">
                  <Chip 
                    label={course.courseTypeName || 'General'} 
                    size="small" 
                    variant="outlined" 
                  />
                  <Chip 
                    label={course.difficultyLevel} 
                    size="small" 
                    color="secondary" 
                  />
                  
                  {/* Course Type Labels với màu sắc rõ ràng */}
                  {course.isVip ? (
                    <Chip 
                      label="VIP" 
                      size="small" 
                      sx={{ 
                        backgroundColor: '#FFD700',
                        color: '#000',
                        fontWeight: 'bold'
                      }}
                      icon={<MoneyIcon />}
                    />
                  ) : (
                    <Chip 
                      label="Free" 
                      size="small" 
                      sx={{ 
                        backgroundColor: '#4CAF50',
                        color: '#fff'
                      }}
                    />
                  )}
                  
                  {/* Enrollment Type Labels */}
                  {course.recommendedStatus ? (
                    <Chip 
                      label={t.recommended} 
                      size="small" 
                      sx={{ 
                        backgroundColor: '#FF9800',
                        color: '#fff'
                      }}
                    />
                  ) : (
                    <Chip 
                      label={t.selfEnrolled} 
                      size="small" 
                      sx={{ 
                        backgroundColor: '#2196F3',
                        color: '#fff'
                      }}
                    />
                  )}
                  
                  <Chip 
                    label={course.enrollmentStatus || 'Active'} 
                    size="small" 
                    color={getStatusColor(course.enrollmentStatus)}
                  />
                </Box>
                
                <Typography variant="body2" color="textSecondary" gutterBottom>
                  {course.description}
                </Typography>
                
                <Box display="flex" gap={2} alignItems="center" flexWrap="wrap">
                  <Box display="flex" alignItems="center" gap={0.5}>
                    <ScheduleIcon fontSize="small" color="action" />
                    <Typography variant="body2">
                      {course.totalDays} days total
                    </Typography>
                  </Box>
                  <Box display="flex" alignItems="center" gap={0.5}>
                    <FitnessCenterIcon fontSize="small" color="action" />
                    <Typography variant="body2">
                      {course.duration} weeks • {course.weeklySessions}/week
                    </Typography>
                  </Box>
                  {course.enrollmentDate && (
                    <Typography variant="caption" color="textSecondary">
                      Enrolled: {new Date(course.enrollmentDate).toLocaleDateString()}
                    </Typography>
                  )}
                </Box>
              </Box>
              
              <Box textAlign="right">
                <Typography variant="h6" color="primary" fontWeight="bold">
                  {Math.round(course.completionPercentage || 0)}%
                </Typography>
                <LinearProgress 
                  variant="determinate" 
                  value={course.completionPercentage || 0} 
                  sx={{ width: 100, mb: 1 }}
                />
                <Typography variant="caption" color="textSecondary">
                  {course.completedDays || 0}/{course.totalDays || 0} days
                </Typography>
              </Box>
            </Box>
            
            <Box mt={2} display="flex" gap={1} justifyContent="space-between" alignItems="center">
              <Button
                variant="outlined"
                size="small"
                startIcon={<TrendingUpIcon />}
                onClick={() => handleViewProgress(course.courseId)}
              >
                {t.viewProgress}
              </Button>
              {course.isVip && (
                <Typography variant="body2" color="primary" sx={{ fontWeight: 'bold' }}>
                  {formatCurrency(course.price)}
                </Typography>
              )}
            </Box>
          </Grid>
        </Grid>
      </CardContent>
    </Card>
  );

  const renderProgressDetails = (courseId) => {
    const progress = courseProgress[courseId];
    if (!progress) {
      return (
        <Box display="flex" justifyContent="center" p={2}>
          <CircularProgress size={40} />
        </Box>
      );
    }

    return (
      <Box mt={2}>
        <Grid container spacing={3}>
          {/* Daily Progress */}
          <Grid item xs={12}>
            <Paper sx={{ p: 2 }}>
              <Typography variant="h6" gutterBottom>
                Daily Progress Tracking
              </Typography>
              
              {progress.dailyProgress && progress.dailyProgress.length > 0 ? (
                <List>
                  {progress.dailyProgress.map((day, index) => (
                    <React.Fragment key={day.dayId}>
                      <ListItem>
                        <ListItemIcon>
                          <Avatar
                            sx={{ 
                              width: 32, 
                              height: 32, 
                              bgcolor: day.isCompleted ? 'success.main' : 'grey.300' 
                            }}
                          >
                            {day.isCompleted ? (
                              <CheckCircleIcon fontSize="small" />
                            ) : (
                              <Typography variant="caption">
                                {index + 1}
                              </Typography>
                            )}
                          </Avatar>
                        </ListItemIcon>
                        <ListItemText
                          primary={day.dayName}
                          secondary={
                            <Box>
                              <Typography variant="body2" color="textSecondary">
                                {day.totalExercises} exercises
                              </Typography>
                              {day.isCompleted && day.completionDateDisplay && (
                                <Typography variant="caption" color="success.main">
                                  ✅ Completed on {day.completionDateDisplay}
                                </Typography>
                              )}
                              {day.isCompleted && day.completionDate && !day.completionDateDisplay && (
                                <Typography variant="caption" color="success.main">
                                  ✅ Completed on {new Date(day.completionDate).toLocaleDateString('vi-VN', {
                                    year: 'numeric',
                                    month: '2-digit', 
                                    day: '2-digit',
                                    hour: '2-digit',
                                    minute: '2-digit'
                                  })}
                                </Typography>
                              )}
                              {day.notes && (
                                <Typography variant="caption" display="block" sx={{ mt: 0.5 }}>
                                  📝 Note: {day.notes}
                                </Typography>
                              )}
                              {!day.isCompleted && (
                                <Typography variant="caption" color="textSecondary">
                                  🔄 Not completed yet
                                </Typography>
                              )}
                            </Box>
                          }
                        />
                      </ListItem>
                      {index < progress.dailyProgress.length - 1 && <Divider />}
                    </React.Fragment>
                  ))}
                </List>
              ) : (
                <Typography variant="body2" color="textSecondary">
                  No daily progress data available
                </Typography>
              )}
            </Paper>
          </Grid>


        </Grid>
      </Box>
    );
  };

  const renderBodyMetricsCard = (metrics, index) => (
    <Card key={metrics.userDataId || index} sx={{ mb: 2, elevation: 2 }}>
      <CardContent>
        <Box display="flex" justifyContent="space-between" alignItems="flex-start" mb={2}>
          <Box>
            <Typography variant="h6" fontWeight="bold" gutterBottom>
              {metrics.courseName ? `Course: ${metrics.courseName}` : 'General Information'}
            </Typography>
            <Typography variant="body2" color="textSecondary">
              User Data ID: {metrics.userDataId} • Customer ID: {metrics.customerId}
              {metrics.trackDataAiId && <> • Track Data AI ID: {metrics.trackDataAiId}</>}
              {metrics.courseId && <> • Course ID: {metrics.courseId}</>}
            </Typography>
            <Typography variant="body2" color="textSecondary">
              Recorded: {formatDate(metrics.createdDate)}
              {metrics.updatedDate && metrics.updatedDate !== metrics.createdDate && (
                <> • Updated: {formatDate(metrics.updatedDate)}</>
              )}
            </Typography>
          </Box>
          {metrics.effective !== null && (
            <Chip 
              label={metrics.effective ? (language === 'en' ? 'Effective' : 'Hiệu quả') : (language === 'en' ? 'Not effective' : 'Chưa hiệu quả')} 
              color={metrics.effective ? 'success' : 'warning'}
              size="small"
            />
          )}
        </Box>

        <Grid container spacing={3}>
          {/* Basic Info */}
          <Grid item xs={12} md={6}>
            <Typography variant="subtitle2" fontWeight="bold" gutterBottom>
              {language === 'en' ? 'Basic Information' : 'Thông tin cơ bản'}
            </Typography>
            <Box display="flex" flexDirection="column" gap={1}>
              <Typography variant="body2">
                <strong>{language === 'en' ? 'Age' : 'Tuổi'}:</strong> {metrics.age || 'N/A'}
              </Typography>
              <Typography variant="body2">
                <strong>{language === 'en' ? 'Gender' : 'Giới tính'}:</strong> {metrics.gender || 'N/A'}
              </Typography>
              <Typography variant="body2">
                <strong>{language === 'en' ? 'Occupation' : 'Nghề nghiệp'}:</strong> {metrics.occupation || 'N/A'}
              </Typography>
              <Typography variant="body2">
                <strong>{language === 'en' ? 'Work Environment' : 'Môi trường làm việc'}:</strong> {metrics.workEnvironment || 'N/A'}
              </Typography>
              <Typography variant="body2">
                <strong>{language === 'en' ? 'Training History' : 'Kinh nghiệm tập luyện'}:</strong> {metrics.trainingHistory || 'N/A'}
              </Typography>
            </Box>
          </Grid>

          {/* Body Measurements */}
          <Grid item xs={12} md={6}>
            <Typography variant="subtitle2" fontWeight="bold" gutterBottom>
              {language === 'en' ? 'Body Measurements' : 'Chỉ số cơ thể'}
            </Typography>
            <Box display="flex" flexDirection="column" gap={1}>
              <Typography variant="body2">
                <strong>{language === 'en' ? 'Height' : 'Chiều cao'}:</strong> {metrics.height ? `${metrics.height} cm` : 'N/A'}
              </Typography>
              <Typography variant="body2">
                <strong>BMI:</strong> {metrics.bmi || 'N/A'}
              </Typography>
              <Typography variant="body2">
                <strong>{language === 'en' ? 'Body Type' : 'Dáng người'}:</strong> {metrics.bodyType || 'N/A'}
              </Typography>
              <Typography variant="body2">
                <strong>{language === 'en' ? 'Training Goals' : 'Mục tiêu tập luyện'}:</strong> {metrics.trainingGoals || 'N/A'}
              </Typography>
              <Typography variant="body2">
                <strong>{language === 'en' ? 'Training Frequency' : 'Tần suất tập luyện'}:</strong> {metrics.trainingFrequency || 'N/A'}
              </Typography>
            </Box>
          </Grid>

          {/* Before/After Measurements */}
          {(metrics.weight || metrics.weightAfter || metrics.waistCircumference || metrics.waistCircumferenceAfter) && (
            <Grid item xs={12}>
              <Typography variant="subtitle2" fontWeight="bold" gutterBottom>
                {language === 'en' ? 'Before/After Comparison' : 'So sánh trước/sau'}
              </Typography>
              <Grid container spacing={2}>
                {/* Weight Comparison */}
                {(metrics.weight || metrics.weightAfter) && (
                  <Grid item xs={12} sm={6} md={4}>
                    <Paper sx={{ p: 2, textAlign: 'center' }}>
                      <Typography variant="body2" color="textSecondary">{language === 'en' ? 'Weight' : 'Cân nặng'}</Typography>
                      <Box display="flex" justifyContent="space-around" alignItems="center" mt={1}>
                        <Box>
                          <Typography variant="h6">{metrics.weight || '-'}</Typography>
                          <Typography variant="caption">{language === 'en' ? 'Before (kg)' : 'Trước (kg)'}</Typography>
                        </Box>
                        <Typography variant="h6" color="primary">→</Typography>
                        <Box>
                          <Typography variant="h6">{metrics.weightAfter || '-'}</Typography>
                          <Typography variant="caption">{language === 'en' ? 'After (kg)' : 'Sau (kg)'}</Typography>
                        </Box>
                      </Box>
                      {metrics.weightChange && (
                        <Typography 
                          variant="body2" 
                          color={metrics.weightChange < 0 ? 'success.main' : 'error.main'}
                          sx={{ mt: 1, fontWeight: 'bold' }}
                        >
                          {metrics.weightChange > 0 ? '+' : ''}{metrics.weightChange} kg
                          ({metrics.weightChangePercentage > 0 ? '+' : ''}{metrics.weightChangePercentage}%)
                        </Typography>
                      )}
                    </Paper>
                  </Grid>
                )}

                {/* Waist Comparison */}
                {(metrics.waistCircumference || metrics.waistCircumferenceAfter) && (
                  <Grid item xs={12} sm={6} md={4}>
                    <Paper sx={{ p: 2, textAlign: 'center' }}>
                      <Typography variant="body2" color="textSecondary">{language === 'en' ? 'Waist' : 'Vòng eo'}</Typography>
                      <Box display="flex" justifyContent="space-around" alignItems="center" mt={1}>
                        <Box>
                          <Typography variant="h6">{metrics.waistCircumference || '-'}</Typography>
                          <Typography variant="caption">{language === 'en' ? 'Before (cm)' : 'Trước (cm)'}</Typography>
                        </Box>
                        <Typography variant="h6" color="primary">→</Typography>
                        <Box>
                          <Typography variant="h6">{metrics.waistCircumferenceAfter || '-'}</Typography>
                          <Typography variant="caption">{language === 'en' ? 'After (cm)' : 'Sau (cm)'}</Typography>
                        </Box>
                      </Box>
                      {metrics.waistChange && (
                        <Typography 
                          variant="body2" 
                          color={metrics.waistChange < 0 ? 'success.main' : 'error.main'}
                          sx={{ mt: 1, fontWeight: 'bold' }}
                        >
                          {metrics.waistChange > 0 ? '+' : ''}{metrics.waistChange} cm
                          ({metrics.waistChangePercentage > 0 ? '+' : ''}{metrics.waistChangePercentage}%)
                        </Typography>
                      )}
                    </Paper>
                  </Grid>
                )}

                {/* Chest Comparison */}
                {(metrics.chestCircumference || metrics.chestCircumferenceAfter) && (
                  <Grid item xs={12} sm={6} md={4}>
                    <Paper sx={{ p: 2, textAlign: 'center' }}>
                      <Typography variant="body2" color="textSecondary">{language === 'en' ? 'Chest' : 'Vòng ngực'}</Typography>
                      <Box display="flex" justifyContent="space-around" alignItems="center" mt={1}>
                        <Box>
                          <Typography variant="h6">{metrics.chestCircumference || '-'}</Typography>
                          <Typography variant="caption">{language === 'en' ? 'Before (cm)' : 'Trước (cm)'}</Typography>
                        </Box>
                        <Typography variant="h6" color="primary">→</Typography>
                        <Box>
                          <Typography variant="h6">{metrics.chestCircumferenceAfter || '-'}</Typography>
                          <Typography variant="caption">{language === 'en' ? 'After (cm)' : 'Sau (cm)'}</Typography>
                        </Box>
                      </Box>
                      {metrics.chestChange && (
                        <Typography 
                          variant="body2" 
                          color={metrics.chestChange > 0 ? 'success.main' : 'error.main'}
                          sx={{ mt: 1, fontWeight: 'bold' }}
                        >
                          {metrics.chestChange > 0 ? '+' : ''}{metrics.chestChange} cm
                          ({metrics.chestChangePercentage > 0 ? '+' : ''}{metrics.chestChangePercentage}%)
                        </Typography>
                      )}
                    </Paper>
                  </Grid>
                )}
              </Grid>
            </Grid>
          )}
        </Grid>
      </CardContent>
    </Card>
  );

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="50vh">
        <CircularProgress size={60} />
        <Typography variant="h6" sx={{ ml: 2 }}>{t.loading}</Typography>
      </Box>
    );
  }

  if (error) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
        <Button
          startIcon={<ArrowBackIcon />}
          onClick={() => navigate('/admin', { state: { activeTab: 'customers' } })}
        >
          Back to Customers
        </Button>
      </Box>
    );
  }

  return (
    <Box sx={{ p: 3, backgroundColor: '#fff', minHeight: '100vh' }}>
      {/* Header */}
      <Box display="flex" alignItems="center" justifyContent="space-between" mb={3}>
        <Box display="flex" alignItems="center">
          <Button
            startIcon={<ArrowBackIcon />}
            onClick={() => navigate('/admin', { state: { activeTab: 'customers' } })}
            sx={{ mr: 2 }}
          >
            Back to Customers
          </Button>
          
          <Box>
            <Typography variant="h4" fontWeight="bold">
              Customer Courses Management
            </Typography>
            {customer && (
              <Typography variant="body1" color="textSecondary">
                {customer.customerName} ({customer.customerEmail}) - {customer.customerType}
              </Typography>
            )}
          </Box>
        </Box>


      </Box>

      {/* Tabs */}
      <Tabs value={selectedTab} onChange={handleTabChange} sx={{ borderBottom: 1, borderColor: 'divider', mb: 3 }}>
        <Tab 
          icon={<SchoolIcon />} 
          label={t.enrolledCourses} 
          iconPosition="start" 
        />
        <Tab 
          icon={<TrendingUpIcon />} 
          label={t.progressAttendance} 
          iconPosition="start" 
        />
        <Tab 
          icon={<AssignmentIcon />} 
          label={t.bodyMetrics} 
          iconPosition="start" 
        />
      </Tabs>

      {/* Content */}
      {selectedTab === 0 && (
        <Box>
          <Typography variant="h6" gutterBottom>
            {t.total}: {courses.length} {t.courses} • {t.totalPaid}: {formatCurrency(
              courses.reduce((sum, course) => sum + (course.price || 0), 0)
            )}
          </Typography>
          
          {courses.length === 0 ? (
            <Paper sx={{ p: 4, textAlign: 'center' }}>
              <SchoolIcon sx={{ fontSize: 60, color: 'grey.400', mb: 2 }} />
              <Typography variant="h6" color="textSecondary">
                {t.noCoursesFound}
              </Typography>
              <Typography variant="body2" color="textSecondary">
                {t.noCourseMessage}
              </Typography>
            </Paper>
          ) : (
            courses.map(course => renderCourseCard(course))
          )}
        </Box>
      )}

      {selectedTab === 1 && (
        <Box>
          {courses.length === 0 ? (
            <Paper sx={{ p: 4, textAlign: 'center' }}>
              <TrendingUpIcon sx={{ fontSize: 60, color: 'grey.400', mb: 2 }} />
              <Typography variant="h6" color="textSecondary">
                {t.noProgressData}
              </Typography>
            </Paper>
          ) : (
            courses.map(course => (
              <Accordion 
                key={course.courseId} 
                sx={{ mb: 2 }}
                expanded={expandedAccordion === course.courseId}
                onChange={handleAccordionChange(course.courseId)}
              >
                <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                  <Box display="flex" alignItems="center" width="100%">
                    <Avatar
                      src={getImageUrl(course.image)}
                      sx={{ width: 40, height: 40, mr: 2 }}
                    >
                      <SchoolIcon />
                    </Avatar>
                    <Box flexGrow={1}>
                      <Typography variant="h6">{course.courseName}</Typography>
                      <Typography variant="body2" color="textSecondary">
                        {Math.round(course.completionPercentage || 0)}% complete - 
                        {course.completedDays || 0}/{course.totalDays || 0} days
                      </Typography>
                    </Box>
                    <LinearProgress 
                      variant="determinate" 
                      value={course.completionPercentage || 0} 
                      sx={{ width: 100, mr: 2 }}
                    />
                  </Box>
                </AccordionSummary>
                <AccordionDetails>
                  {renderProgressDetails(course.courseId)}
                </AccordionDetails>
              </Accordion>
            ))
          )}
        </Box>
      )}

      {selectedTab === 2 && (
        <Box>
          {loadingBodyMetrics ? (
            <Box display="flex" justifyContent="center" p={4}>
              <CircularProgress size={60} />
              <Typography variant="h6" sx={{ ml: 2, alignSelf: 'center' }}>
                {t.loadingBodyMetrics}
              </Typography>
            </Box>
          ) : bodyMetrics.length === 0 ? (
            <Paper sx={{ p: 4, textAlign: 'center' }}>
              <AssignmentIcon sx={{ fontSize: 60, color: 'grey.400', mb: 2 }} />
              <Typography variant="h6" color="textSecondary">
                {t.noBodyMetrics}
              </Typography>
              <Typography variant="body2" color="textSecondary">
                {t.noBodyMetricsMessage}
              </Typography>
              <Button
                variant="outlined"
                onClick={fetchBodyMetrics}
                sx={{ mt: 2 }}
                startIcon={<AssignmentIcon />}
              >
                {t.reload}
              </Button>
            </Paper>
          ) : (
            <>
              <Typography variant="h6" gutterBottom>
                {t.bodyMetricsHistory} ({bodyMetrics.length} {t.records})
              </Typography>
              <Typography variant="body2" color="textSecondary" sx={{ mb: 3 }}>
                {language === 'en' 
                  ? 'Measurement history and body metrics changes for each recommended course'
                  : 'Lịch sử đo lường và thay đổi chỉ số cơ thể của khách hàng theo từng khóa học đề xuất'
                }
              </Typography>
              
              {bodyMetrics.map((metrics, index) => renderBodyMetricsCard(metrics, index))}
            </>
          )}
        </Box>
      )}

      <DialogComponents />
    </Box>
  );
};

export default CustomerCourses; 