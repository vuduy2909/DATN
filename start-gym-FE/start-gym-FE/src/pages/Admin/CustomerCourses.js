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

const CustomerCourses = () => {
  const { customerId } = useParams();
  const navigate = useNavigate();
  const { showAlert, showLoading, hideLoading, DialogComponents } = useDialogs();
  
  const [customer, setCustomer] = useState(null);
  const [courses, setCourses] = useState([]);
  const [selectedTab, setSelectedTab] = useState(0);
  const [courseProgress, setCourseProgress] = useState({});
  const [loading, setLoading] = useState(true);
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
                
                <Box display="flex" gap={1} mb={1}>
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
                  <Chip 
                    label={course.isVip ? 'VIP' : 'Free'} 
                    size="small" 
                    color={course.isVip ? 'warning' : 'default'}
                    icon={course.isVip ? <MoneyIcon /> : undefined}
                  />
                  <Chip 
                    label={course.enrollmentStatus || 'Active'} 
                    size="small" 
                    color={getStatusColor(course.enrollmentStatus)}
                  />
                </Box>
                
                <Typography variant="body2" color="textSecondary" gutterBottom>
                  {course.description}
                </Typography>
                
                <Box display="flex" gap={2} alignItems="center">
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
            
            <Box mt={2} display="flex" gap={1}>
              <Button
                variant="outlined"
                size="small"
                startIcon={<TrendingUpIcon />}
                onClick={() => handleViewProgress(course.courseId)}
              >
                View Progress
              </Button>
              {course.isVip && (
                <Typography variant="body2" color="primary" sx={{ ml: 'auto', alignSelf: 'center' }}>
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
          <Grid item xs={12} md={8}>
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

          {/* Body Metrics */}
          <Grid item xs={12} md={4}>
            <Paper sx={{ p: 2 }}>
              <Typography variant="h6" gutterBottom>
                Body Metrics Comparison
              </Typography>
              
              {progress.bodyMetrics?.before ? (
                <Box>
                  <Typography variant="subtitle2" color="primary" gutterBottom>
                    Before Course
                  </Typography>
                  <Typography variant="body2">
                    BMI: {progress.bodyMetrics.before.bmi || 'N/A'}
                  </Typography>
                  <Typography variant="body2">
                    Weight: {progress.bodyMetrics.before.weight || 'N/A'} kg
                  </Typography>
                  <Typography variant="body2">
                    Height: {progress.bodyMetrics.before.height || 'N/A'} cm
                  </Typography>
                  <Typography variant="caption" color="textSecondary">
                    Recorded: {progress.bodyMetrics.before.recordDate ? 
                      new Date(progress.bodyMetrics.before.recordDate).toLocaleDateString() : 'N/A'}
                  </Typography>

                  {progress.bodyMetrics.after && typeof progress.bodyMetrics.after === 'object' ? (
                    <Box mt={2}>
                      <Typography variant="subtitle2" color="success.main" gutterBottom>
                        After Course
                      </Typography>
                      <Typography variant="body2">
                        BMI: {progress.bodyMetrics.after.bmi || 'N/A'}
                      </Typography>
                      <Typography variant="body2">
                        Weight: {progress.bodyMetrics.after.weight || 'N/A'} kg
                      </Typography>
                      <Typography variant="body2">
                        Height: {progress.bodyMetrics.after.height || 'N/A'} cm
                      </Typography>
                      <Typography variant="caption" color="textSecondary">
                        Recorded: {progress.bodyMetrics.after.recordDate ? 
                          new Date(progress.bodyMetrics.after.recordDate).toLocaleDateString() : 'N/A'}
                      </Typography>
                    </Box>
                  ) : (
                    <Box mt={2}>
                      <Typography variant="body2" color="textSecondary">
                        {progress.bodyMetrics.after || 'No updated metrics available'}
                      </Typography>
                    </Box>
                  )}
                </Box>
              ) : (
                <Typography variant="body2" color="textSecondary">
                  No body metrics data available
                </Typography>
              )}
            </Paper>
          </Grid>
        </Grid>
      </Box>
    );
  };

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="50vh">
        <CircularProgress size={60} />
        <Typography variant="h6" sx={{ ml: 2 }}>Loading customer courses...</Typography>
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
      <Box display="flex" alignItems="center" mb={3}>
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

      {/* Tabs */}
      <Tabs value={selectedTab} onChange={handleTabChange} sx={{ borderBottom: 1, borderColor: 'divider', mb: 3 }}>
        <Tab 
          icon={<SchoolIcon />} 
          label="Enrolled Courses" 
          iconPosition="start" 
        />
        <Tab 
          icon={<TrendingUpIcon />} 
          label="Progress & Attendance" 
          iconPosition="start" 
        />
        <Tab 
          icon={<AssignmentIcon />} 
          label="Body Metrics" 
          iconPosition="start" 
        />
      </Tabs>

      {/* Content */}
      {selectedTab === 0 && (
        <Box>
          <Typography variant="h6" gutterBottom>
            Total: {courses.length} courses • Total Paid: {formatCurrency(
              courses.reduce((sum, course) => sum + (course.price || 0), 0)
            )}
          </Typography>
          
          {courses.length === 0 ? (
            <Paper sx={{ p: 4, textAlign: 'center' }}>
              <SchoolIcon sx={{ fontSize: 60, color: 'grey.400', mb: 2 }} />
              <Typography variant="h6" color="textSecondary">
                No courses found
              </Typography>
              <Typography variant="body2" color="textSecondary">
                This customer has not enrolled in any courses yet.
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
                No progress data available
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
          <Typography variant="h6" gutterBottom>
            Body Metrics Overview
          </Typography>
          
          {courses.length === 0 ? (
            <Paper sx={{ p: 4, textAlign: 'center' }}>
              <AssignmentIcon sx={{ fontSize: 60, color: 'grey.400', mb: 2 }} />
              <Typography variant="h6" color="textSecondary">
                No metrics data available
              </Typography>
            </Paper>
          ) : (
            <Typography variant="body1" color="textSecondary">
              Detailed body metrics are available in the Progress & Attendance tab for each course.
            </Typography>
          )}
        </Box>
      )}

      <DialogComponents />
    </Box>
  );
};

export default CustomerCourses; 