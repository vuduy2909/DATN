import React, { useEffect, useState } from 'react'
import Header from '../components/Header/Header'
import Footer from '../components/Footer/Footer'
import { useAuth } from '../AuthContext'
import { 
  Container, 
  Grid, 
  Card, 
  CardActionArea, 
  CardMedia, 
  CardContent, 
  Typography,
  Box,
  Chip,
  Divider,
  CircularProgress,
  Alert
} from '@mui/material';
import banner from '../images/user_banner.jpg'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faEnvelope } from '@fortawesome/free-regular-svg-icons';
import { 
  faPhone, 
  faLocationDot, 
  faVenusMars, 
  faCakeCandles, 
  faUserGroup,
  faCrown,
  faCalendarAlt,
  faMoneyBill
} from '@fortawesome/free-solid-svg-icons';
import { request } from '../utils/axiosInstance';
import styles from '../components/UserInformation/UserInformation.module.css'

const Profile = () => {
    const { user } = useAuth();
    const [userData, setUserData] = useState([]);
    const [vipCourses, setVipCourses] = useState([]);
    const [loadingVip, setLoadingVip] = useState(false);
    const [vipError, setVipError] = useState(null);

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
                const res = await request.get('/customer/detail')
                console.log("Gender from API:", res.customerGender, typeof res.customerGender)
                
                // Convert gender to proper boolean - handle string case specifically
                if (typeof res.customerGender === 'string') {
                    // Check for "male" string directly
                    const genderLower = res.customerGender.toLowerCase()
                    res.customerGender = (genderLower === 'male') // Will be true for "male", false otherwise
                }
                
                setUserData(res)
            } catch (error) {
                console.log("Error fetching user information", error);
            }
        }

        const fetchVipCourses = async () => {
            try {
                setLoadingVip(true);
                setVipError(null);
                const response = await request.get('/payment/my-vip-courses');
                console.log("VIP courses response:", response);
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

    return (
        <div>
            <Header />
            <div className={styles.container}>
                <Container component="main" maxWidth="100%">
                    <Grid container spacing={2} justifyContent="center">
                        <Grid item xs={8}>
                            <Card className={styles.banner}>
                                <CardActionArea>
                                    <CardMedia
                                        className={styles.img_cover}
                                        component="img"
                                        height="185"
                                        image={banner || ''}
                                        alt="banner"
                                    />
                                </CardActionArea>
                                <CardContent className={styles.under_banner}>
                                    <CardMedia
                                        className={styles.img_ava}
                                        component="img"
                                        image={userData.customerImg || ''}
                                        height='auto'
                                        alt="avatar"
                                    />
                                    <Typography variant="h5" component="div" className={styles.customer_name}>
                                        {userData.customerName || ''}
                                    </Typography>
                                </CardContent>
                            </Card>
                        </Grid>
                        
                        <Grid item xs={8}>
                            <Card className={styles.information}>
                                <CardContent style={{ width: "100%", maxHeight: "450px", overflow: "auto" }}>
                                    <Typography gutterBottom variant="h5" component="div" className={styles.info_title}>
                                        Personal Information
                                    </Typography>
                                    
                                    <div variant="body2" style={{ display: 'flex', marginTop: '5px' }}>
                                        <FontAwesomeIcon icon={faEnvelope} className={styles.info_title_icon} />
                                        <div component="div" variant="body2">
                                            <h2 className={styles.info_title_tag}>Email</h2>
                                            <h1 className={styles.info_title_item}>{userData.accountEmail || ''}</h1>
                                        </div>
                                    </div>
                                    
                                    <div variant="body2" style={{ display: 'flex', marginTop: '5px' }}>
                                        <FontAwesomeIcon icon={faPhone} className={styles.info_title_icon} />
                                        <div component="div" variant="body2">
                                            <h2 className={styles.info_title_tag}>Phone</h2>
                                            <h1 className={styles.info_title_item}>{userData.customerPhone || ''}</h1>
                                        </div>
                                    </div>
                                    
                                    <div variant="body2" style={{ display: 'flex', marginTop: '5px' }}>
                                        <FontAwesomeIcon icon={faLocationDot} className={styles.info_title_icon} />
                                        <div component="div" variant="body2">
                                            <h2 className={styles.info_title_tag}>Location</h2>
                                            <h1 className={styles.info_title_item}>{userData.customerAddress || ''}</h1>
                                        </div>
                                    </div>

                                    <Typography gutterBottom variant="h5" component="div" className={styles.info_title} style={{marginTop: '30px'}}>
                                        Personal Details
                                    </Typography>
                                    
                                    <div variant="body2" style={{ display: 'flex', marginTop: '5px' }}>
                                        <FontAwesomeIcon icon={faVenusMars} className={styles.info_title_icon} />
                                        <div component="div" variant="body2">
                                            <h2 className={styles.info_title_tag}>Gender</h2>
                                            <h1 className={styles.info_title_item}>
                                                {typeof userData.customerGender === 'string' ? 
                                                    userData.customerGender === 'Male' || userData.customerGender === 'male' ? 'Male' : 'Female' 
                                                    : userData.customerGender === true ? 'Male' : 'Female'}
                                            </h1>
                                        </div>
                                    </div>
                                    
                                    <div variant="body2" style={{ display: 'flex', marginTop: '5px' }}>
                                        <FontAwesomeIcon icon={faCakeCandles} className={styles.info_title_icon} />
                                        <div component="div" variant="body2">
                                            <h2 className={styles.info_title_tag}>Date of birth</h2>
                                            <h1 className={styles.info_title_item}>{userData.dateOfBirth || ''}</h1>
                                        </div>
                                    </div>
                                    
                                    <div variant="body2" style={{ display: 'flex', marginTop: '5px' }}>
                                        <FontAwesomeIcon icon={faUserGroup} className={styles.info_title_icon} />
                                        <div component="div" variant="body2">
                                            <h2 className={styles.info_title_tag}>Account Type</h2>
                                            <h1 className={styles.info_title_item}>{userData.customerTypeName || ''}</h1>
                                        </div>
                                    </div>

                                    {/* VIP Courses History Section */}
                                    <Typography gutterBottom variant="h5" component="div" className={styles.info_title} style={{marginTop: '30px'}}>
                                        <FontAwesomeIcon icon={faCrown} style={{ color: '#FFD700', marginRight: '10px' }} />
                                        VIP Courses History
                                    </Typography>
                                    
                                    {loadingVip ? (
                                        <Box display="flex" justifyContent="center" alignItems="center" minHeight="100px">
                                            <CircularProgress size={40} />
                                            <Typography variant="body1" style={{ marginLeft: '10px' }}>
                                                Đang tải khóa học VIP...
                                            </Typography>
                                        </Box>
                                    ) : vipError ? (
                                        <Alert severity="error" style={{ marginTop: '10px' }}>
                                            {vipError}
                                        </Alert>
                                    ) : vipCourses.length === 0 ? (
                                        <Box 
                                            display="flex" 
                                            flexDirection="column" 
                                            alignItems="center" 
                                            padding="20px"
                                            bgcolor="#f9f9f9"
                                            borderRadius="8px"
                                            marginTop="10px"
                                        >
                                            <FontAwesomeIcon 
                                                icon={faCrown} 
                                                style={{ fontSize: '48px', color: '#ccc', marginBottom: '10px' }} 
                                            />
                                            <Typography variant="body1" color="text.secondary">
                                                Bạn chưa mua khóa học VIP nào
                                            </Typography>
                                            <Typography variant="body2" color="text.secondary">
                                                Khám phá các khóa học VIP để có trải nghiệm tốt nhất!
                                            </Typography>
                                        </Box>
                                    ) : (
                                        <Box marginTop="10px">
                                            <Typography variant="body2" color="text.secondary" style={{ marginBottom: '15px' }}>
                                                Bạn đã mua {vipCourses.length} khóa học VIP
                                            </Typography>
                                            
                                            {vipCourses.map((course, index) => (
                                                <Card 
                                                    key={course.courseId || index}
                                                    style={{ 
                                                        marginBottom: '15px',
                                                        border: '2px solid #FFD700',
                                                        borderRadius: '12px',
                                                        background: 'linear-gradient(135deg, #FFF9E6 0%, #FFFFFF 100%)'
                                                    }}
                                                >
                                                    <CardContent>
                                                        <Box display="flex" justifyContent="space-between" alignItems="flex-start">
                                                            <Box flex={1}>
                                                                <Box display="flex" alignItems="center" marginBottom="8px">
                                                                    <Chip
                                                                        icon={<FontAwesomeIcon icon={faCrown} />}
                                                                        label="VIP"
                                                                        size="small"
                                                                        style={{
                                                                            backgroundColor: '#FFD700',
                                                                            color: '#000',
                                                                            fontWeight: 'bold',
                                                                            marginRight: '10px'
                                                                        }}
                                                                    />
                                                                    <Typography variant="h6" component="div" style={{ fontWeight: 'bold' }}>
                                                                        {course.courseName}
                                                                    </Typography>
                                                                </Box>
                                                                
                                                                <Typography variant="body2" color="text.secondary" style={{ marginBottom: '10px' }}>
                                                                    {course.description || 'Khóa học VIP cao cấp với nội dung chuyên sâu'}
                                                                </Typography>
                                                                
                                                                <Box display="flex" alignItems="center" gap="20px" flexWrap="wrap">
                                                                    <Box display="flex" alignItems="center">
                                                                        <FontAwesomeIcon icon={faMoneyBill} style={{ color: '#FF4444', marginRight: '5px' }} />
                                                                        <Typography variant="body2" style={{ fontWeight: 'bold', color: '#FF4444' }}>
                                                                            {formatPrice(course.price)}
                                                                        </Typography>
                                                                    </Box>
                                                                    
                                                                    <Box display="flex" alignItems="center">
                                                                        <FontAwesomeIcon icon={faCalendarAlt} style={{ color: '#666', marginRight: '5px' }} />
                                                                        <Typography variant="body2" color="text.secondary">
                                                                            Mua ngày: {formatDate(course.purchaseDate)}
                                                                        </Typography>
                                                                    </Box>
                                                                    
                                                                    {course.isEnrolled && (
                                                                        <Chip 
                                                                            label="Đã kích hoạt" 
                                                                            size="small" 
                                                                            color="success" 
                                                                            variant="outlined"
                                                                        />
                                                                    )}
                                                                </Box>
                                                            </Box>
                                                        </Box>
                                                    </CardContent>
                                                </Card>
                                            ))}
                                        </Box>
                                    )}
                                </CardContent>
                            </Card>
                        </Grid>
                    </Grid>
                </Container>
            </div>
            <Footer />
        </div>
    )
}

export default Profile 