import React, { useEffect, useState } from 'react'
import Header from '../components/Header/Header'
import Footer from '../components/Footer/Footer'
import { useAuth } from '../AuthContext'
import { 
  Container, 
  Grid, 
  Card, 
  CardContent, 
  Typography,
  Box,
  Chip,
  CircularProgress,
  Alert,
  Avatar,
  Paper,
  Divider,
  LinearProgress,
  Skeleton,
  Fade,
  Grow,
  Button,
  TextField,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Snackbar
} from '@mui/material';
import {
  Person,
  Email,
  Phone,
  LocationOn,
  Cake,
  Wc,
  AccountBox,
  School,
  Star,
  CalendarToday,
  AttachMoney,
  StarRate,
  TrendingUp,
  FitnessCenter,
  LocalFireDepartment,
  Recommend,
  CheckCircle,
  MonitorWeight,
  Assignment
} from '@mui/icons-material';
import banner from '../images/user_banner.jpg'
import { request, getImageUrl } from '../utils/axiosInstance';
import RecommendedCourses from '../components/RecommendedCourses/RecommendedCourses';
import { courseService } from '../service/courseService';

const DEFAULT_IMAGE = 'data:image/svg+xml;charset=UTF-8,%3Csvg%20width%3D%22286%22%20height%3D%22180%22%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20viewBox%3D%220%200%20286%20180%22%20preserveAspectRatio%3D%22none%22%3E%3Cdefs%3E%3Cstyle%20type%3D%22text%2Fcss%22%3E%23holder_18c66a8c7b3%20text%20%7B%20fill%3A%23999%3Bfont-weight%3Anormal%3Bfont-family%3AArial%2C%20Helvetica%2C%20Open%20Sans%2C%20sans-serif%2C%20monospace%3Bfont-size%3A14pt%20%7D%20%3C%2Fstyle%3E%3C%2Fdefs%3E%3Cg%20id%3D%22holder_18c66a8c7b3%22%3E%3Crect%20width%3D%22286%22%20height%3D%22180%22%20fill%3D%22%23373940%22%3E%3C%2Frect%3E%3Cg%3E%3Ctext%20x%3D%22107.1875%22%20y%3D%2296.3%22%3EImage%20not%20available%3C%2Ftext%3E%3C%2Fg%3E%3C%2Fg%3E%3C%2Fsvg%3E';

const Profile = () => {
    const { user } = useAuth();
    const [userData, setUserData] = useState([]);
    const [vipCourses, setVipCourses] = useState([]);
    const [loadingVip, setLoadingVip] = useState(false);
    const [loadingUser, setLoadingUser] = useState(true);
    const [vipError, setVipError] = useState(null);
    const [courseCompletions, setCourseCompletions] = useState({});

    // Post-course measurement states
    const [showMeasurementDialog, setShowMeasurementDialog] = useState(false);
    const [completedCourseId, setCompletedCourseId] = useState(null);
    const [initialMeasurements, setInitialMeasurements] = useState(null);
    const [postMeasurements, setPostMeasurements] = useState({
        weightAfter: '',
        waistCircumferenceAfter: '',
        chestCircumferenceAfter: ''
    });
    const [measurementSubmitting, setMeasurementSubmitting] = useState(false);
    const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' });

    // StartGym Red Theme Colors with #e74c3c
    const redTheme = {
        primary: '#e74c3c',      // Main red
        secondary: '#c0392b',    // Darker red
        accent: '#f39c12',       // Orange accent
        background: '#fdf2f2',   // Very light red background
        cardBg: '#ffffff',       // White cards
        border: '#fadbd8',       // Light red border
        text: '#2c3e50',         // Dark gray text
        textLight: '#64748b',    // Light gray text
        success: '#27ae60',      // Green for success
        warning: '#f39c12',      // Orange for warning
        error: '#e74c3c'         // Red for errors
    };

    const formatPrice = (price) => {
        return new Intl.NumberFormat('vi-VN', {
          style: 'currency',
          currency: 'VND'
        }).format(price);
    };

    const formatDate = (dateString) => {
        return new Date(dateString).toLocaleDateString('vi-VN', {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
    };

    // Handle course completion and measurement prompt
    const handleCourseCompletion = async (courseId) => {
        try {
            // Fetch initial measurements for the completed course
            const response = await request.get(`/post-course-measurement/initial-data/${courseId}`);
            setInitialMeasurements(response.initialMeasurements);
            setCompletedCourseId(courseId);
            
            if (!response.hasPostMeasurements) {
                setShowMeasurementDialog(true);
            } else {
                setSnackbar({
                    open: true,
                    message: 'Chúc mừng! Bạn đã hoàn thành khóa học và đã cập nhật chỉ số cơ thể.',
                    severity: 'success'
                });
            }
        } catch (error) {
            console.error('Error fetching initial measurements:', error);
            setSnackbar({
                open: true,
                message: 'Có lỗi khi tải thông tin chỉ số ban đầu.',
                severity: 'error'
            });
        }
    };

    // Submit post-course measurements
    const handleSubmitMeasurements = async () => {
        try {
            setMeasurementSubmitting(true);
            
            const response = await request.post('/post-course-measurement/submit', {
                courseId: completedCourseId,
                weightAfter: parseFloat(postMeasurements.weightAfter),
                waistCircumferenceAfter: parseFloat(postMeasurements.waistCircumferenceAfter),
                chestCircumferenceAfter: parseFloat(postMeasurements.chestCircumferenceAfter)
            });
            
            setShowMeasurementDialog(false);
            setPostMeasurements({
                weightAfter: '',
                waistCircumferenceAfter: '',
                chestCircumferenceAfter: ''
            });
            
            const effectiveness = response.effective ? 'hiệu quả' : 'cần cải thiện';
            setSnackbar({
                open: true,
                message: `Cập nhật chỉ số thành công! Khóa học được đánh giá là ${effectiveness}.`,
                severity: response.effective ? 'success' : 'warning'
            });
            
            // Refresh VIP courses to update completion status
            if (user && userData.customerId) {
                // Call fetchVipCourses - will be defined below
                window.location.reload(); // Simple reload for now
            }
            
        } catch (error) {
            console.error('Error submitting measurements:', error);
            setSnackbar({
                open: true,
                message: 'Có lỗi khi cập nhật chỉ số cơ thể.',
                severity: 'error'
            });
        } finally {
            setMeasurementSubmitting(false);
        }
    };

    useEffect(() => {
        const fetchUserData = async () => {
            try {
                setLoadingUser(true);
                const res = await request.get('/customer/detail')
                
                // Convert gender to proper boolean - handle string case specifically
                if (typeof res.customerGender === 'string') {
                    // Check for "male" string directly
                    const genderLower = res.customerGender.toLowerCase()
                    res.customerGender = (genderLower === 'male') // Will be true for "male", false otherwise
                }
                
                setUserData(res)
            } catch (error) {
                console.error("Error fetching user information", error);
            } finally {
                setLoadingUser(false);
            }
        }

        const fetchVipCourses = async () => {
            try {
                setLoadingVip(true);
                setVipError(null);
                const response = await request.get('/payment/my-vip-courses');
                const courses = response.vipCourses || [];
                
                // Log để debug
                console.log('VIP Courses API Response:', response);
                console.log('VIP Courses Array:', courses);
                
                setVipCourses(courses);
                
                // Lấy thông tin tiến độ cho mỗi khóa học VIP
                if (courses.length > 0 && user && userData.customerId) {
                    const customerId = userData.customerId;
                    
                    // Khởi tạo hoặc cập nhật các bản ghi completion cho khóa học VIP
                    for (const course of courses) {
                        try {
                            // Khởi tạo completion records trước tiên
                            await request.post('/customer-progress/initialize', null, {
                                params: {
                                    customerId: customerId,
                                    courseId: course.courseId
                                }
                            });
                        } catch (initErr) {
                            console.error(`Error initializing completion for course ${course.courseId}:`, initErr);
                        }
                    }
                    
                    // Sau khi khởi tạo, lấy dữ liệu tiến độ
                    const completionPromises = courses.map(course => 
                        courseService.getCourseCompletion(course.courseId, customerId)
                            .then(completion => ({ courseId: course.courseId, completion }))
                            .catch((err) => {
                                console.error(`Error getting completion for course ${course.courseId}:`, err);
                                return { 
                                    courseId: course.courseId, 
                                    completion: {
                                        completedDays: 0,
                                        totalDays: 0,
                                        progressPercentage: 0,
                                        isCompleted: false
                                    } 
                                };
                            })
                    );
                    
                    const completionResults = await Promise.all(completionPromises);
                    const completionMap = {};
                    completionResults.forEach(result => {
                        if (result.completion) {
                            completionMap[result.courseId] = result.completion;
                            console.log(`Course ${result.courseId} completion:`, result.completion);
                        }
                    });
                    
                    setCourseCompletions(completionMap);
                }
            } catch (error) {
                console.error("Error fetching VIP courses:", error);
                setVipError("Không thể tải danh sách khóa học VIP");
                setVipCourses([]);
            } finally {
                setLoadingVip(false);
            }
        };

        if (user) {
            fetchUserData();
        }
        
        // Chỉ fetch khóa học VIP khi đã có thông tin user và customerId
        if (user && userData.customerId) {
            fetchVipCourses();
        }
    }, [user, userData.customerId]);

    const InfoItem = ({ icon, label, value, color = redTheme.primary }) => (
        <Box sx={{ 
            display: 'flex', 
            alignItems: 'center', 
            mb: 2, 
            p: 2, 
            borderRadius: 2, 
            backgroundColor: redTheme.background,
            border: `1px solid ${redTheme.border}`,
            transition: 'all 0.3s', 
            '&:hover': { 
                backgroundColor: redTheme.accent + '30', 
                transform: 'translateX(5px)',
                boxShadow: `0 4px 12px ${redTheme.primary}20`
            } 
        }}>
            <Box sx={{ 
                display: 'flex', 
                alignItems: 'center', 
                justifyContent: 'center',
                width: 50, 
                height: 50, 
                borderRadius: '50%', 
                backgroundColor: color + '20',
                color: color,
                mr: 2,
                boxShadow: `0 4px 8px ${color}20`
            }}>
                {icon}
            </Box>
            <Box sx={{ flex: 1 }}>
                <Typography variant="body2" sx={{ color: redTheme.textLight, fontWeight: 500, mb: 0.5 }}>
                    {label}
                </Typography>
                <Typography variant="h6" sx={{ color: redTheme.text, fontWeight: 600 }}>
                    {value || 'Chưa cập nhật'}
                </Typography>
            </Box>
        </Box>
    );

    if (loadingUser) {
        return (
            <div style={{ backgroundColor: redTheme.background }}>
                <Header />
                <Container maxWidth="lg" sx={{ py: 4 }}>
                    <Grid container spacing={3}>
                        <Grid item xs={12}>
                            <Skeleton variant="rectangular" height={300} sx={{ borderRadius: 2 }} />
                        </Grid>
                        <Grid item xs={12} md={8}>
                            <Card>
                                <CardContent>
                                    <Skeleton variant="text" height={40} width="60%" />
                                    {[...Array(6)].map((_, i) => (
                                        <Skeleton key={i} variant="rectangular" height={80} sx={{ my: 1, borderRadius: 1 }} />
                                    ))}
                                </CardContent>
                            </Card>
                        </Grid>
                        <Grid item xs={12} md={4}>
                            <Skeleton variant="rectangular" height={400} sx={{ borderRadius: 2 }} />
                        </Grid>
                    </Grid>
                </Container>
                <Footer />
            </div>
        );
    }

    return (
        <div style={{ backgroundColor: redTheme.background, minHeight: '100vh' }}>
            <Header />
            <Container maxWidth="lg" sx={{ py: 4 }}>
                {/* Profile Header Card */}
                <Fade in timeout={800}>
                    <Card 
                        elevation={12}
                        sx={{ 
                            mb: 4,
                            background: `linear-gradient(135deg, ${redTheme.primary} 0%, ${redTheme.secondary} 100%)`,
                            color: 'white',
                            position: 'relative',
                            overflow: 'hidden',
                            borderRadius: 3
                        }}
                    >
                        {/* Background Banner */}
                        <Box
                            sx={{
                                height: 200,
                                backgroundImage: `url(${banner})`,
                                backgroundSize: 'cover',
                                backgroundPosition: 'center',
                                position: 'relative',
                                '&::before': {
                                    content: '""',
                                    position: 'absolute',
                                    top: 0,
                                    left: 0,
                                    right: 0,
                                    bottom: 0,
                                    background: `linear-gradient(135deg, ${redTheme.primary}80, ${redTheme.secondary}80)`,
                                }
                            }}
                        />
                        
                        {/* Profile Info */}
                        <CardContent sx={{ position: 'relative', mt: -8, zIndex: 2 }}>
                            <Box sx={{ display: 'flex', alignItems: 'end', mb: 3 }}>
                                <Avatar
                                    src={userData.customerImg}
                                    sx={{ 
                                        width: 120, 
                                        height: 120, 
                                        border: '4px solid white',
                                        boxShadow: '0 8px 24px rgba(0,0,0,0.2)',
                                        mr: 3
                                    }}
                                />
                                <Box sx={{ flex: 1 }}>
                                    <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                                        <FitnessCenter sx={{ mr: 1, fontSize: 30 }} />
                                        <Typography variant="h4" sx={{ fontWeight: 'bold' }}>
                                            {userData.customerName || 'Chưa cập nhật tên'}
                                        </Typography>
                                        <LocalFireDepartment sx={{ ml: 1, fontSize: 30 }} />
                                    </Box>
                                    <Chip 
                                        icon={<Star />}
                                        label="StartGym Member"
                                        sx={{ 
                                            backgroundColor: 'rgba(255,255,255,0.2)',
                                            color: 'white',
                                            fontWeight: 'bold',
                                            backdropFilter: 'blur(10px)'
                                        }}
                                    />
                                </Box>
                            </Box>
                        </CardContent>
                    </Card>
                </Fade>

                <Grid container spacing={4}>
                    {/* Personal Information */}
                    <Grid item xs={12} md={8}>
                        <Grow in timeout={1000}>
                            <Card 
                                elevation={8} 
                                sx={{ 
                                    mb: 3,
                                    borderRadius: 3,
                                    border: `2px solid ${redTheme.border}`
                                }}
                            >
                                <CardContent sx={{ p: 3 }}>
                                    <Typography variant="h5" sx={{ mb: 3, color: redTheme.text, fontWeight: 'bold', display: 'flex', alignItems: 'center' }}>
                                        <Person sx={{ mr: 1, color: redTheme.primary }} />
                                        Thông tin cá nhân
                                    </Typography>
                                    
                                    <Grid container spacing={2}>
                                        <Grid item xs={12} md={6}>
                                            <InfoItem 
                                                icon={<Email />}
                                                label="Email"
                                                value={userData.customerEmail}
                                                color={redTheme.primary}
                                            />
                                        </Grid>
                                        <Grid item xs={12} md={6}>
                                            <InfoItem 
                                                icon={<Phone />}
                                                label="Số điện thoại"
                                                value={userData.customerPhone}
                                                color={redTheme.secondary}
                                            />
                                        </Grid>
                                        <Grid item xs={12}>
                                            <InfoItem 
                                                icon={<LocationOn />}
                                                label="Địa chỉ"
                                                value={userData.customerAddress}
                                                color={redTheme.success}
                                            />
                                        </Grid>
                                        <Grid item xs={12} md={6}>
                                            <InfoItem 
                                                icon={<Wc />}
                                                label="Giới tính"
                                                value={typeof userData.customerGender === 'string' ? 
                                                    userData.customerGender === 'Male' || userData.customerGender === 'male' ? 'Nam' : 'Nữ' 
                                                    : userData.customerGender === true ? 'Nam' : 'Nữ'}
                                                color={redTheme.warning}
                                            />
                                        </Grid>
                                        <Grid item xs={12} md={6}>
                                            <InfoItem 
                                                icon={<Cake />}
                                                label="Ngày sinh"
                                                value={userData.dateOfBirth}
                                                color={redTheme.accent}
                                            />
                                        </Grid>
                                    </Grid>
                                </CardContent>
                            </Card>
                        </Grow>

                        {/* VIP Courses Section */}
                        <Grow in timeout={1200}>
                            <Card 
                                elevation={8}
                                sx={{
                                    borderRadius: 3,
                                    border: `2px solid ${redTheme.border}`,
                                    mb: 3
                                }}
                            >
                                <CardContent sx={{ p: 4 }}>
                                    <Typography variant="h5" sx={{ mb: 4, color: redTheme.text, fontWeight: 'bold', display: 'flex', alignItems: 'center' }}>
                                        <StarRate sx={{ mr: 1, fontSize: 28, color: '#FFD700' }} />
                                        Lịch sử khóa học VIP
                                    </Typography>
                                    
                                    {loadingVip ? (
                                        <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
                                            <CircularProgress sx={{ color: redTheme.primary }} />
                                        </Box>
                                    ) : vipError ? (
                                        <Alert severity="error" sx={{ mb: 2 }}>
                                            {vipError}
                                        </Alert>
                                    ) : vipCourses.length === 0 ? (
                                        <Paper 
                                            elevation={0} 
                                            sx={{ 
                                                p: 5, 
                                                textAlign: 'center',
                                                backgroundColor: redTheme.background,
                                                border: `2px dashed ${redTheme.border}`,
                                                borderRadius: 3
                                            }}
                                        >
                                            <School sx={{ fontSize: 80, color: redTheme.border, mb: 2 }} />
                                            <Typography variant="h6" sx={{ color: redTheme.textLight, mb: 1 }}>
                                                Chưa có khóa học VIP
                                            </Typography>
                                            <Typography variant="body1" sx={{ color: redTheme.textLight }}>
                                                Hãy đăng ký các khóa học VIP để có trải nghiệm tốt nhất!
                                            </Typography>
                                        </Paper>
                                    ) : (
                                        <Grid container spacing={3}>
                                            {vipCourses.map((course, index) => (
                                                <Grid item xs={12} key={index}>
                                                    <Card 
                                                        elevation={4}
                                                        sx={{ 
                                                            transition: 'all 0.3s',
                                                            borderRadius: 2,
                                                            border: `1px solid ${redTheme.border}`,
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
                                                                    src={getImageUrl(course.courseImage || course.image || DEFAULT_IMAGE)}
                                                                    sx={{ 
                                                                        width: 80, 
                                                                        height: 80, 
                                                                        mr: 3,
                                                                        border: `2px solid ${redTheme.primary}`
                                                                    }}
                                                                    onError={(e) => {
                                                                        e.target.src = DEFAULT_IMAGE;
                                                                        e.target.onerror = null;
                                                                    }}
                                                                />
                                                                <Box sx={{ flex: 1 }}>
                                                                    <Typography variant="h6" sx={{ fontWeight: 'bold', mb: 1, color: redTheme.text, fontSize: '1.2rem' }}>
                                                                        {course.courseName}
                                                                    </Typography>
                                                                    <Chip 
                                                                        icon={<StarRate />}
                                                                        label="VIP"
                                                                        size="small"
                                                                        sx={{ 
                                                                            backgroundColor: '#FFD700',
                                                                            color: '#000',
                                                                            fontWeight: 'bold',
                                                                            px: 1,
                                                                            mb: 1
                                                                        }}
                                                                    />
                                                                    {course.description && (
                                                                        <Typography variant="body2" sx={{ mt: 1, color: '#555' }}>
                                                                            {course.description}
                                                                        </Typography>
                                                                    )}
                                                                </Box>
                                                            </Box>
                                                            
                                                            {/* Hiển thị tiến trình */}
                                                            {courseCompletions[course.courseId] && (
                                                                <Box sx={{ mb: 3, mt: 2 }}>
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
                                                                    
                                                                    {/* Course completion status and measurement button */}
                                                                    {courseCompletions[course.courseId].isCompleted && (
                                                                        <Box sx={{ mt: 2 }}>
                                                                            <Chip 
                                                                                icon={<CheckCircle />}
                                                                                label="Đã hoàn thành"
                                                                                color="success"
                                                                                sx={{ mb: 1 }}
                                                                            />
                                                                            <Box sx={{ display: 'flex', gap: 1, mt: 1 }}>
                                                                                <Button
                                                                                    size="small"
                                                                                    variant="outlined"
                                                                                    startIcon={<MonitorWeight />}
                                                                                    onClick={() => handleCourseCompletion(course.courseId)}
                                                                                    sx={{
                                                                                        borderColor: redTheme.primary,
                                                                                        color: redTheme.primary,
                                                                                        '&:hover': {
                                                                                            borderColor: redTheme.secondary,
                                                                                            backgroundColor: redTheme.background
                                                                                        }
                                                                                    }}
                                                                                >
                                                                                    Cập nhật chỉ số
                                                                                </Button>
                                                                            </Box>
                                                                        </Box>
                                                                    )}
                                                                </Box>
                                                            )}
                                                            
                                                            <Box sx={{ 
                                                                display: 'flex', 
                                                                justifyContent: 'space-between', 
                                                                alignItems: 'center', 
                                                                mt: 'auto', 
                                                                pt: 2, 
                                                                borderTop: '1px solid #eee',
                                                                width: '100%'
                                                            }}>
                                                                <Box sx={{ display: 'flex', alignItems: 'center', p: 1, bgcolor: 'rgba(46, 125, 50, 0.1)', borderRadius: 1 }}>
                                                                    <AttachMoney sx={{ color: redTheme.success, mr: 0.5 }} />
                                                                    <Typography variant="body1" sx={{ fontWeight: 'bold', color: redTheme.success }}>
                                                                        {formatPrice(course.amount || course.price || 0)}
                                                                    </Typography>
                                                                </Box>
                                                                <Box sx={{ display: 'flex', alignItems: 'center', p: 1, bgcolor: 'rgba(0, 0, 0, 0.05)', borderRadius: 1 }}>
                                                                    <CalendarToday sx={{ color: redTheme.textLight, mr: 0.5, fontSize: 16 }} />
                                                                    <Typography variant="body2" sx={{ color: redTheme.textLight }}>
                                                                        {formatDate(course.paymentDate || course.purchaseDate)}
                                                                    </Typography>
                                                                </Box>
                                                            </Box>
                                                        </CardContent>
                                                    </Card>
                                                </Grid>
                                            ))}
                                        </Grid>
                                    )}
                                </CardContent>
                            </Card>
                        </Grow>

                        {/* Recommended Courses Section */}
                        <Grow in timeout={1300}>
                            <Card 
                                elevation={8}
                                sx={{
                                    borderRadius: 3,
                                    border: `2px solid ${redTheme.border}`
                                }}
                            >
                                <CardContent sx={{ p: 4 }}>
                                    <Typography variant="h5" sx={{ mb: 4, color: redTheme.text, fontWeight: 'bold', display: 'flex', alignItems: 'center' }}>
                                        <Recommend sx={{ mr: 1, fontSize: 28, color: redTheme.primary }} />
                                        Khóa học được đề xuất
                                    </Typography>
                                    
                                    <Box sx={{ px: { xs: 0, md: 2 }, maxWidth: '100%' }}>
                                        <RecommendedCourses customerId={userData.customerId} />
                                    </Box>
                                </CardContent>
                            </Card>
                        </Grow>
                    </Grid>

                    {/* Stats Sidebar */}
                    <Grid item xs={12} md={4}>
                        <Grow in timeout={1400}>
                            <Card 
                                elevation={8}
                                sx={{
                                    background: `linear-gradient(135deg, ${redTheme.primary} 0%, ${redTheme.secondary} 100%)`,
                                    color: 'white',
                                    borderRadius: 3
                                }}
                            >
                                <CardContent sx={{ p: 3 }}>
                                    <Typography variant="h6" sx={{ fontWeight: 'bold', mb: 3, textAlign: 'center', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                                        <TrendingUp sx={{ mr: 1 }} />
                                        Thống kê StartGym
                                    </Typography>
                                    
                                    <Box sx={{ mb: 3 }}>
                                        <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                                            <Typography variant="body2">Khóa học VIP</Typography>
                                            <Typography variant="body2" sx={{ fontWeight: 'bold' }}>
                                                {vipCourses.length}
                                            </Typography>
                                        </Box>
                                        <LinearProgress 
                                            variant="determinate" 
                                            value={Math.min(vipCourses.length * 20, 100)} 
                                            sx={{ 
                                                height: 8, 
                                                borderRadius: 4,
                                                backgroundColor: 'rgba(255,255,255,0.3)',
                                                '& .MuiLinearProgress-bar': {
                                                    backgroundColor: 'white'
                                                }
                                            }}
                                        />
                                    </Box>

                                    <Divider sx={{ my: 3, backgroundColor: 'rgba(255,255,255,0.3)' }} />
                                    
                                    <Box sx={{ textAlign: 'center' }}>
                                        <Typography variant="body2" sx={{ mb: 2, opacity: 0.9 }}>
                                            Loại tài khoản
                                        </Typography>
                                        <Chip 
                                            icon={<AccountBox />}
                                            label={userData.customerTypeName || 'Regular'}
                                            sx={{ 
                                                backgroundColor: 'rgba(255,255,255,0.2)',
                                                color: 'white',
                                                fontWeight: 'bold',
                                                fontSize: '1rem',
                                                backdropFilter: 'blur(10px)'
                                            }}
                                        />
                                    </Box>
                                </CardContent>
                            </Card>
                        </Grow>
                        </Grid>
                    </Grid>
                </Container>
            <Footer />
            
            {/* Post-Course Measurement Dialog */}
            <Dialog 
                open={showMeasurementDialog} 
                onClose={() => setShowMeasurementDialog(false)}
                maxWidth="md"
                fullWidth
            >
                <DialogTitle sx={{ 
                    backgroundColor: redTheme.primary, 
                    color: 'white',
                    display: 'flex',
                    alignItems: 'center'
                }}>
                    <CheckCircle sx={{ mr: 1 }} />
                    Chúc mừng! Khóa học đã hoàn thành
                </DialogTitle>
                <DialogContent sx={{ pt: 3 }}>
                    <Alert severity="success" sx={{ mb: 3 }}>
                        <Typography variant="body1" sx={{ fontWeight: 'bold' }}>
                            🎉 Bạn đã hoàn thành khóa học thành công!
                        </Typography>
                        <Typography variant="body2">
                            Để đánh giá hiệu quả của khóa học, vui lòng cập nhật chỉ số cơ thể hiện tại của bạn.
                        </Typography>
                    </Alert>

                    {initialMeasurements && (
                        <Box sx={{ mb: 3 }}>
                            <Typography variant="h6" sx={{ mb: 2, display: 'flex', alignItems: 'center' }}>
                                <Assignment sx={{ mr: 1, color: redTheme.primary }} />
                                Chỉ số ban đầu của bạn:
                            </Typography>
                            <Grid container spacing={2}>
                                <Grid item xs={4}>
                                    <Paper sx={{ p: 2, textAlign: 'center', bgcolor: redTheme.background }}>
                                        <Typography variant="body2" color="textSecondary">Cân nặng</Typography>
                                        <Typography variant="h6" color={redTheme.primary}>
                                            {initialMeasurements.weight} kg
                                        </Typography>
                                    </Paper>
                                </Grid>
                                <Grid item xs={4}>
                                    <Paper sx={{ p: 2, textAlign: 'center', bgcolor: redTheme.background }}>
                                        <Typography variant="body2" color="textSecondary">Vòng eo</Typography>
                                        <Typography variant="h6" color={redTheme.primary}>
                                            {initialMeasurements.waistCircumference} cm
                                        </Typography>
                                    </Paper>
                                </Grid>
                                <Grid item xs={4}>
                                    <Paper sx={{ p: 2, textAlign: 'center', bgcolor: redTheme.background }}>
                                        <Typography variant="body2" color="textSecondary">Vòng ngực</Typography>
                                        <Typography variant="h6" color={redTheme.primary}>
                                            {initialMeasurements.chestCircumference} cm
                                        </Typography>
                                    </Paper>
                                </Grid>
                            </Grid>
                        </Box>
                    )}

                    <Typography variant="h6" sx={{ mb: 2, display: 'flex', alignItems: 'center' }}>
                        <MonitorWeight sx={{ mr: 1, color: redTheme.primary }} />
                        Chỉ số hiện tại của bạn:
                    </Typography>
                    
                    <Grid container spacing={2}>
                        <Grid item xs={12} md={4}>
                            <TextField
                                fullWidth
                                label="Cân nặng hiện tại (kg)"
                                type="number"
                                value={postMeasurements.weightAfter}
                                onChange={(e) => setPostMeasurements(prev => ({
                                    ...prev,
                                    weightAfter: e.target.value
                                }))}
                                InputProps={{
                                    inputProps: { min: 30, max: 200, step: 0.1 }
                                }}
                            />
                        </Grid>
                        <Grid item xs={12} md={4}>
                            <TextField
                                fullWidth
                                label="Vòng eo hiện tại (cm)"
                                type="number"
                                value={postMeasurements.waistCircumferenceAfter}
                                onChange={(e) => setPostMeasurements(prev => ({
                                    ...prev,
                                    waistCircumferenceAfter: e.target.value
                                }))}
                                InputProps={{
                                    inputProps: { min: 50, max: 150, step: 0.1 }
                                }}
                            />
                        </Grid>
                        <Grid item xs={12} md={4}>
                            <TextField
                                fullWidth
                                label="Vòng ngực hiện tại (cm)"
                                type="number"
                                value={postMeasurements.chestCircumferenceAfter}
                                onChange={(e) => setPostMeasurements(prev => ({
                                    ...prev,
                                    chestCircumferenceAfter: e.target.value
                                }))}
                                InputProps={{
                                    inputProps: { min: 60, max: 180, step: 0.1 }
                                }}
                            />
                        </Grid>
                    </Grid>
                </DialogContent>
                <DialogActions sx={{ p: 3 }}>
                    <Button 
                        onClick={() => setShowMeasurementDialog(false)}
                        disabled={measurementSubmitting}
                    >
                        Để sau
                    </Button>
                    <Button 
                        variant="contained" 
                        onClick={handleSubmitMeasurements}
                        disabled={
                            measurementSubmitting ||
                            !postMeasurements.weightAfter || 
                            !postMeasurements.waistCircumferenceAfter || 
                            !postMeasurements.chestCircumferenceAfter
                        }
                        sx={{ 
                            backgroundColor: redTheme.primary,
                            '&:hover': { backgroundColor: redTheme.secondary }
                        }}
                    >
                        {measurementSubmitting ? <CircularProgress size={20} color="inherit" /> : 'Cập nhật'}
                    </Button>
                </DialogActions>
            </Dialog>

            {/* Snackbar for notifications */}
            <Snackbar
                open={snackbar.open}
                autoHideDuration={6000}
                onClose={() => setSnackbar(prev => ({ ...prev, open: false }))}
            >
                <Alert 
                    onClose={() => setSnackbar(prev => ({ ...prev, open: false }))}
                    severity={snackbar.severity}
                    sx={{ width: '100%' }}
                >
                    {snackbar.message}
                </Alert>
            </Snackbar>
        </div>
    );
};

export default Profile;