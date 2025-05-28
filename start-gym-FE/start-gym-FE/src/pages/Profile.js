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
  Grow
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
  LocalFireDepartment
} from '@mui/icons-material';
import banner from '../images/user_banner.jpg'
import { request } from '../utils/axiosInstance';

const Profile = () => {
    const { user } = useAuth();
    const [userData, setUserData] = useState([]);
    const [vipCourses, setVipCourses] = useState([]);
    const [loadingVip, setLoadingVip] = useState(false);
    const [loadingUser, setLoadingUser] = useState(true);
    const [vipError, setVipError] = useState(null);

    // StartGym Red Theme Colors with #CF4044
    const redTheme = {
        primary: '#CF4044',      // Main red
        secondary: '#B8363C',    // Darker red
        accent: '#f39c12',       // Orange accent
        background: '#fdf2f2',   // Very light red background
        cardBg: '#ffffff',       // White cards
        border: '#fadbd8',       // Light red border
        text: '#2c3e50',         // Dark gray text
        textLight: '#64748b',    // Light gray text
        success: '#27ae60',      // Green for success
        warning: '#f39c12',      // Orange for warning
        error: '#CF4044'         // Red for errors
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
                setVipCourses(response.vipCourses || []);
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
            fetchVipCourses();
        }
    }, [user])

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
                                    background: `linear-gradient(135deg, ${redTheme.primary}CC, ${redTheme.secondary}CC)`,
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
                                    border: `2px solid ${redTheme.border}`
                                }}
                            >
                                <CardContent sx={{ p: 3 }}>
                                    <Typography variant="h5" sx={{ mb: 3, color: redTheme.text, fontWeight: 'bold', display: 'flex', alignItems: 'center' }}>
                                        <StarRate sx={{ mr: 1, color: '#FFD700' }} />
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
                                                p: 4, 
                                                textAlign: 'center',
                                                backgroundColor: redTheme.background,
                                                border: `2px dashed ${redTheme.border}`,
                                                borderRadius: 3
                                            }}
                                        >
                                            <School sx={{ fontSize: 60, color: redTheme.border, mb: 2 }} />
                                            <Typography variant="h6" sx={{ color: redTheme.textLight, mb: 1 }}>
                                                Chưa có khóa học VIP
                                            </Typography>
                                            <Typography variant="body2" sx={{ color: redTheme.textLight }}>
                                                Hãy đăng ký các khóa học VIP để có trải nghiệm tốt nhất!
                                            </Typography>
                                        </Paper>
                                    ) : (
                                        <Grid container spacing={2}>
                                            {vipCourses.map((course, index) => (
                                                <Grid item xs={12} md={6} key={index}>
                                                    <Card 
                                                        elevation={4}
                                                        sx={{ 
                                                            transition: 'all 0.3s',
                                                            borderRadius: 2,
                                                            border: `1px solid ${redTheme.border}`,
                                                            '&:hover': { 
                                                                transform: 'translateY(-5px)',
                                                                boxShadow: `0 8px 25px ${redTheme.primary}30`
                                                            }
                                                        }}
                                                    >
                                                        <CardContent>
                                                            <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                                                                <Avatar 
                                                                    src={course.courseImage}
                                                                    sx={{ 
                                                                        width: 60, 
                                                                        height: 60, 
                                                                        mr: 2,
                                                                        border: `2px solid ${redTheme.primary}`
                                                                    }}
                                                                />
                                                                <Box sx={{ flex: 1 }}>
                                                                    <Typography variant="h6" sx={{ fontWeight: 'bold', mb: 0.5, color: redTheme.text }}>
                                                                        {course.courseName}
                                                                    </Typography>
                                                                    <Chip 
                                                                        icon={<StarRate />}
                                                                        label="VIP"
                                                                        size="small"
                                                                        sx={{ 
                                                                            backgroundColor: '#FFD700',
                                                                            color: '#000',
                                                                            fontWeight: 'bold'
                                                                        }}
                                                                    />
                                                                </Box>
                                                            </Box>
                                                            
                                                            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                                                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                                                                    <AttachMoney sx={{ color: redTheme.success, mr: 0.5 }} />
                                                                    <Typography variant="body2" sx={{ fontWeight: 'bold', color: redTheme.success }}>
                                                                        {formatPrice(course.amount)}
                                                                    </Typography>
                                                                </Box>
                                                                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                                                                    <CalendarToday sx={{ color: redTheme.textLight, mr: 0.5, fontSize: 16 }} />
                                                                    <Typography variant="body2" sx={{ color: redTheme.textLight }}>
                                                                        {formatDate(course.paymentDate)}
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
        </div>
    );
};

export default Profile;