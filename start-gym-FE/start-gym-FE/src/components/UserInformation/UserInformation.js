import React, { useEffect, useState } from 'react'
import { useAuth } from '../../AuthContext'
import { useLanguage } from '../../contexts/LanguageContext'
import { 
    Container, 
    Grid, 
    Card, 
    CardActionArea, 
    CardMedia, 
    CardContent, 
    Typography,
    Box,
    Paper,
    Avatar,
    Chip,
    LinearProgress,
    IconButton,
    Divider,
    Fade,
    Grow,
    useTheme
} from '@mui/material';

import {
    Email,
    Phone,
    LocationOn,
    TrendingUp,
    Schedule,
    FolderOpen,
    FitnessCenter,
    Wc,
    Cake,
    AccountBox,
    BarChart,
    Analytics,
    Timeline,
    CalendarToday,
    LocalFireDepartment,
    Star
} from '@mui/icons-material';
import banner from '../../images/user_banner.jpg'
import { request } from '../../utils/axiosInstance';
import ChartCircle from '../ChartCircle/ChartCircle'
import ChartLine from '../ChartLine/ChartLine';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { DateCalendar } from '@mui/x-date-pickers/DateCalendar';
import { useNavigate } from 'react-router-dom';

const UserInformation = () => {
    const { user } = useAuth();
    const { language, toggleLanguage, t } = useLanguage();
    const [userData, setUserData] = useState([]);
    const [recommendedCourse, setRecommendedCourse] = useState('')
    const [lengthCourse, setLengthCourse] = useState(0)
    const [dashboardStats, setDashboardStats] = useState(null);
    const [activityChartData, setActivityChartData] = useState([]);
    const [progressChartData, setProgressChartData] = useState([]);
    const theme = useTheme();
    const navigate = useNavigate();

    // StartGym Red Theme Colors
    const redThemeColors = {
        primary: '#CF4044',
        secondary: '#B8363C', 
        accent: '#ff4757',
        success: '#27ae60',
        warning: '#f39c12',
        info: '#3498db',
        dark: '#2c3e50'
    };

    useEffect(() => {
        const fetchUserData = async () => {
            try {
                const res = await request.get('/customer/detail')
                
                // Convert gender to proper boolean - handle string case specifically
                if (typeof res.customerGender === 'string') {
                    // Check for "male" string directly
                    const genderLower = res.customerGender.toLowerCase()
                    res.customerGender = (genderLower === 'male') // Will be true for "male", false otherwise
                }
                
                setUserData(res)
                setRecommendedCourse(res.recommendedCourses.find(course => course.recommendedStatus === true))
                setLengthCourse(res.recommendedCourses.length)
                
                // Lấy thống kê dashboard
                fetchDashboardStats();
            } catch (error) {
                console.error("Error fetching user data:", error);
            }
        }
        
        // Hàm lấy thông tin dashboard
        const fetchDashboardStats = async () => {
            try {
                const dashboardResponse = await request.get('/user-dashboard/stats');
                
                // Lưu thông tin dashboard
                if (dashboardResponse) {
                    setDashboardStats(dashboardResponse);
                    
                    // Cập nhật dữ liệu biểu đồ từ API response
                    if (dashboardResponse.activityChartData) {
                        setActivityChartData(dashboardResponse.activityChartData);
                    }
                    
                    if (dashboardResponse.progressChartData) {
                        setProgressChartData(dashboardResponse.progressChartData);
                    }
                }
            } catch (error) {
                console.error("Error fetching dashboard stats:", error);
                // Fallback to default data if API fails
                setActivityChartData({
                    data: [
                        { day: 'Week 1', activity: 60 },
                        { day: 'Week 2', activity: 75 },
                        { day: 'Week 3', activity: 45 },
                        { day: 'Week 4', activity: 80 }
                    ]
                });
                setProgressChartData({ percentage: 0, completed: 0, total: 0 });
            }
        };

        if (user) {
            fetchUserData();
        }
    }, [user])

    // Enhanced stat card component
    const StatCard = ({ title, value, subtitle, icon, color, gradient }) => (
        <Grow in timeout={600}>
            <Card 
                elevation={8}
                sx={{ 
                    height: '140px',
                    background: gradient,
                    color: 'white',
                    position: 'relative',
                    overflow: 'hidden',
                    borderRadius: 3,
                    transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
                    cursor: 'pointer',
                    '&:hover': {
                        transform: 'translateY(-8px) scale(1.02)',
                        boxShadow: theme.shadows[20],
                    }
                }}
            >
                <CardContent sx={{ p: 2, height: '100%', display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                        <Typography variant="body2" sx={{ opacity: 0.9, fontWeight: 500, fontSize: '0.9rem' }}>
                            {title}
                        </Typography>
                        <Box sx={{ 
                            display: 'flex', 
                            alignItems: 'center', 
                            justifyContent: 'center',
                            width: 40, 
                            height: 40, 
                            borderRadius: '50%', 
                            backgroundColor: 'rgba(255,255,255,0.2)',
                            backdropFilter: 'blur(10px)'
                        }}>
                            {React.cloneElement(icon, { sx: { fontSize: 20 } })}
                        </Box>
                    </Box>
                    
                    <Box>
                        <Typography variant="h3" component="div" sx={{ fontWeight: 'bold', fontSize: '2rem' }}>
                            {value}
                        </Typography>
                        {subtitle && (
                            <Typography variant="body2" sx={{ opacity: 0.85, mt: 0.5 }}>
                                {subtitle}
                            </Typography>
                        )}
                    </Box>
                </CardContent>
                
                {/* Decorative elements */}
                <Box
                    sx={{
                        position: 'absolute',
                        top: -20,
                        right: -20,
                        width: 60,
                        height: 60,
                        borderRadius: '50%',
                        background: 'rgba(255,255,255,0.1)',
                        zIndex: 1
                    }}
                />
            </Card>
        </Grow>
    );

    const InfoItem = ({ icon, label, value, color = redThemeColors.primary }) => (
        <Box sx={{ 
            display: 'flex', 
            alignItems: 'center', 
            mb: 2, 
            p: 2, 
            borderRadius: 2, 
            backgroundColor: '#f8f9fa',
            transition: 'all 0.3s',
            '&:hover': { 
                backgroundColor: '#e9ecef', 
                transform: 'translateX(5px)' 
            } 
        }}>
            <Box sx={{ 
                display: 'flex', 
                alignItems: 'center', 
                justifyContent: 'center',
                width: 45, 
                height: 45, 
                borderRadius: '50%', 
                backgroundColor: color + '20',
                color: color,
                mr: 2,
                boxShadow: '0 4px 8px rgba(0,0,0,0.1)'
            }}>
                {icon}
            </Box>
            <Box sx={{ flex: 1 }}>
                <Typography variant="body2" sx={{ color: '#666', fontWeight: 500, mb: 0.5 }}>
                    {label}
                </Typography>
                <Typography variant="body1" sx={{ color: '#2c3e50', fontWeight: 600 }}>
                    {value || 'Chưa cập nhật'}
                </Typography>
            </Box>
        </Box>
    );

    return (
        <Box sx={{ 
            backgroundColor: '#f5f5f5', 
            minHeight: '100vh', 
            py: 4,
            backgroundImage: 'radial-gradient(circle at 1px 1px, rgba(231,76,60,0.1) 1px, transparent 0)',
            backgroundSize: '20px 20px'
        }}>
            <Container maxWidth="xl">
                <Grid container spacing={4}>
                    {/* Main Content */}
                    <Grid item xs={12} md={9}>
                        {/* Profile Header Card */}
                        <Fade in timeout={800}>
                            <Card 
                                elevation={12}
                                sx={{ 
                                    mb: 4,
                                    background: `linear-gradient(135deg, ${redThemeColors.primary} 0%, ${redThemeColors.accent} 100%)`,
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
                                            background: `linear-gradient(135deg, ${redThemeColors.primary}80, ${redThemeColors.accent}80)`,
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
                                            <Typography variant="h4" sx={{ fontWeight: 'bold', mb: 1 }}>
                                                {userData.customerName || 'Chưa cập nhật tên'}
                                            </Typography>
                                            <Chip 
                                                icon={<LocalFireDepartment />}
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

                        {/* Stats Cards */}
                        <Grid container spacing={3} sx={{ mb: 4 }}>
                            <Grid item xs={12} sm={6} md={4}>
                                <StatCard
                                    title={t.weeklyActivity}
                                    value={dashboardStats?.weeklyActivity ? `${Math.round(dashboardStats.weeklyActivity)}%` : "0%"}
                                    subtitle={dashboardStats?.completedSessions && dashboardStats?.targetSessions 
                                        ? `${dashboardStats.completedSessions}/${dashboardStats.targetSessions} ${language === 'en' ? 'sessions' : 'buổi'}`
                                        : `0/3 ${language === 'en' ? 'sessions' : 'buổi'}`}
                                    icon={<TrendingUp />}
                                    gradient={`linear-gradient(135deg, ${redThemeColors.primary} 0%, ${redThemeColors.secondary} 100%)`}
                                />
                            </Grid>
                            <Grid item xs={12} sm={6} md={4}>
                                <StatCard
                                    title={t.courseInProgress}
                                    value={dashboardStats?.coursesInProgress || lengthCourse}
                                    icon={<FolderOpen />}
                                    gradient={`linear-gradient(135deg, ${redThemeColors.info} 0%, #2980b9 100%)`}
                                />
                            </Grid>
                            <Grid item xs={12} sm={6} md={4}>
                                <StatCard
                                    title={t.bmi}
                                    value={dashboardStats?.bmi ? Number(dashboardStats.bmi).toFixed(2) : recommendedCourse?.bmi ? Number(recommendedCourse.bmi).toFixed(2) : '0.00'}
                                    icon={<FitnessCenter />}
                                    gradient={`linear-gradient(135deg, ${redThemeColors.success} 0%, #2ecc71 100%)`}
                                />
                            </Grid>
                        </Grid>

                        {/* Charts Section */}
                        <Grid container spacing={3}>
                            <Grid item xs={12} md={9}>
                                <Grow in timeout={1200}>
                                    <Card 
                                        elevation={8}
                                        sx={{ 
                                            p: 3, 
                                            borderRadius: 3,
                                            background: 'linear-gradient(135deg, #ffffff 0%, #f8fafc 100%)',
                                            border: `1px solid ${redThemeColors.primary}20`
                                        }}
                                    >
                                        <Typography 
                                            variant="h6" 
                                            sx={{ 
                                                fontWeight: 'bold', 
                                                color: redThemeColors.dark,
                                                mb: 2,
                                                display: 'flex',
                                                alignItems: 'center'
                                            }}
                                        >
                                            <Analytics sx={{ mr: 1, color: redThemeColors.primary }} />
                                            {t.activityChart}
                                        </Typography>
                                        <ChartLine chartData={activityChartData} />
                                    </Card>
                                </Grow>
                            </Grid>
                            <Grid item xs={12} md={3}>
                                <Grow in timeout={1400}>
                                    <Card 
                                        elevation={8}
                                        sx={{ 
                                            p: 3, 
                                            borderRadius: 3,
                                            background: 'linear-gradient(135deg, #ffffff 0%, #f8fafc 100%)',
                                            border: `1px solid ${redThemeColors.primary}20`
                                        }}
                                    >
                                        <Typography 
                                            variant="h6" 
                                            sx={{ 
                                                fontWeight: 'bold', 
                                                color: redThemeColors.dark,
                                                mb: 2,
                                                display: 'flex',
                                                alignItems: 'center'
                                            }}
                                        >
                                            <Timeline sx={{ mr: 1, color: redThemeColors.primary }} />
                                            {t.progress}
                                        </Typography>
                                        <ChartCircle chartData={progressChartData} />
                                    </Card>
                                </Grow>
                            </Grid>
                        </Grid>
                    </Grid>

                    {/* Sidebar */}
                    <Grid item xs={12} md={3}>
                        <Grid container spacing={3}>
                            {/* Information Card */}
                            <Grid item xs={12}>
                                <Grow in timeout={1000}>
                                    <Card 
                                        elevation={8}
                                        sx={{ 
                                            borderRadius: 3,
                                            background: 'linear-gradient(135deg, #ffffff 0%, #f8fafc 100%)',
                                            border: `1px solid ${redThemeColors.primary}20`
                                        }}
                                    >
                                        <CardContent sx={{ p: 3 }}>
                                            <Typography 
                                                variant="h6" 
                                                sx={{ 
                                                    fontWeight: 'bold', 
                                                    color: redThemeColors.dark,
                                                    mb: 3,
                                                    display: 'flex',
                                                    alignItems: 'center'
                                                }}
                                            >
                                                <Star sx={{ mr: 1, color: redThemeColors.primary }} />
                                                {t.information}
                                            </Typography>
                                            
                                            <InfoItem 
                                                icon={<Email />}
                                                label={t.email}
                                                value={userData.customerEmail}
                                                color={redThemeColors.primary}
                                            />
                                            <InfoItem 
                                                icon={<Phone />}
                                                label={t.phone}
                                                value={userData.customerPhone}
                                                color={redThemeColors.secondary}
                                            />
                                            <InfoItem 
                                                icon={<LocationOn />}
                                                label={t.location}
                                                value={userData.customerAddress}
                                                color={redThemeColors.success}
                                            />

                                            <Divider sx={{ my: 3 }} />

                                            <Typography 
                                                variant="h6" 
                                                sx={{ 
                                                    fontWeight: 'bold', 
                                                    color: redThemeColors.dark,
                                                    mb: 3,
                                                    display: 'flex',
                                                    alignItems: 'center'
                                                }}
                                            >
                                                <AccountBox sx={{ mr: 1, color: redThemeColors.primary }} />
                                                {t.biography}
                                            </Typography>
                                            
                                            <InfoItem 
                                                icon={<Wc />}
                                                label={t.gender}
                                                value={typeof userData.customerGender === 'string' ? 
                                                    userData.customerGender === 'Male' || userData.customerGender === 'male' ? (language === 'en' ? 'Male' : 'Nam') : (language === 'en' ? 'Female' : 'Nữ')
                                                    : userData.customerGender === true ? (language === 'en' ? 'Male' : 'Nam') : (language === 'en' ? 'Female' : 'Nữ')}
                                                color={redThemeColors.info}
                                            />
                                            <InfoItem 
                                                icon={<Cake />}
                                                label={t.dateOfBirth}
                                                value={userData.dateOfBirth}
                                                color={redThemeColors.warning}
                                            />
                                        </CardContent>
                                    </Card>
                                </Grow>
                            </Grid>

                            {/* Calendar Card */}
                            <Grid item xs={12}>
                                <Grow in timeout={1600}>
                                    <Card 
                                        elevation={8}
                                        sx={{ 
                                            borderRadius: 3,
                                            background: 'linear-gradient(135deg, #ffffff 0%, #f8fafc 100%)',
                                            border: `1px solid ${redThemeColors.primary}20`
                                        }}
                                    >
                                        <CardContent sx={{ p: 3 }}>
                                            <Typography 
                                                variant="h6" 
                                                sx={{ 
                                                    fontWeight: 'bold', 
                                                    color: redThemeColors.dark,
                                                    mb: 2,
                                                    display: 'flex',
                                                    alignItems: 'center'
                                                }}
                                            >
                                                <CalendarToday sx={{ mr: 1, color: redThemeColors.primary }} />
                                                Calendar
                                            </Typography>
                                            <LocalizationProvider dateAdapter={AdapterDayjs}>
                                                <DateCalendar 
                                                    sx={{
                                                        '& .MuiPickersDay-root': {
                                                            '&.Mui-selected': {
                                                                backgroundColor: redThemeColors.primary,
                                                                '&:hover': {
                                                                    backgroundColor: redThemeColors.secondary
                                                                }
                                                            }
                                                        }
                                                    }}
                                                />
                                            </LocalizationProvider>
                                        </CardContent>
                                    </Card>
                                </Grow>
                            </Grid>
                        </Grid>
                    </Grid>
                </Grid>
            </Container>
        </Box>
    )
}

export default UserInformation