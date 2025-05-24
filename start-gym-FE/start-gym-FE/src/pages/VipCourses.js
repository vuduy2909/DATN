import React, { useEffect, useState } from 'react';
import Header from '../components/Header/Header';
import Footer from '../components/Footer/Footer';
import { useAuth } from '../AuthContext';
import { 
    Container, 
    Grid, 
    Card, 
    CardContent, 
    CardMedia,
    Typography, 
    Button, 
    Chip,
    Box,
    Alert
} from '@mui/material';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faCrown, faPlay, faClock } from '@fortawesome/free-solid-svg-icons';
import { request } from '../utils/axiosInstance';

const VipCourses = () => {
    const { user } = useAuth();
    const [courses, setCourses] = useState([]);
    const [loading, setLoading] = useState(false);
    const [paymentLoading, setPaymentLoading] = useState(false);
    const [message, setMessage] = useState('');

    useEffect(() => {
        fetchVipCourses();
    }, []);

    const fetchVipCourses = async () => {
        try {
            setLoading(true);
            const response = await request.get('/api/v1/course');
            // Filter only VIP courses (courses with price > 0)
            const vipCourses = response.data.filter(course => 
                course.price && course.price > 0 && course.isVip
            );
            setCourses(vipCourses);
        } catch (error) {
            console.error('Error fetching VIP courses:', error);
            setMessage('Lỗi khi tải danh sách khóa học VIP');
        } finally {
            setLoading(false);
        }
    };

    const handlePayment = async (course) => {
        if (!user) {
            setMessage('Vui lòng đăng nhập để thanh toán');
            return;
        }

        try {
            setPaymentLoading(true);
            setMessage('');

            const paymentData = {
                courseId: course.courseId,
                amount: course.price,
                currency: 'VND',
                orderInfo: `Thanh toán khóa học VIP: ${course.courseName}`,
                returnUrl: window.location.origin + '/payment/result'
            };

            const response = await request.post('/api/v1/payment/create', paymentData);
            
            if (response.data && response.data.paymentUrl) {
                // Redirect to VNPay
                window.location.href = response.data.paymentUrl;
            } else {
                throw new Error('Không nhận được URL thanh toán');
            }

        } catch (error) {
            console.error('Payment error:', error);
            setMessage('Lỗi khi tạo thanh toán: ' + (error.response?.data?.message || error.message));
        } finally {
            setPaymentLoading(false);
        }
    };

    const formatPrice = (price) => {
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND'
        }).format(price);
    };

    return (
        <div>
            <Header />
            <Container maxWidth="lg" sx={{ py: 4 }}>
                <Box sx={{ mb: 4, textAlign: 'center' }}>
                    <Typography variant="h3" component="h1" gutterBottom sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 2 }}>
                        <FontAwesomeIcon icon={faCrown} style={{ color: '#FFD700' }} />
                        Khóa Học VIP
                    </Typography>
                    <Typography variant="h6" color="text.secondary">
                        Nâng cao trình độ với các khóa học chuyên nghiệp
                    </Typography>
                </Box>

                {message && (
                    <Alert severity={message.includes('Lỗi') ? 'error' : 'info'} sx={{ mb: 3 }}>
                        {message}
                    </Alert>
                )}

                {loading ? (
                    <Box sx={{ textAlign: 'center', py: 4 }}>
                        <Typography>Đang tải...</Typography>
                    </Box>
                ) : (
                    <Grid container spacing={3}>
                        {courses.length === 0 ? (
                            <Grid item xs={12}>
                                <Box sx={{ textAlign: 'center', py: 4 }}>
                                    <Typography variant="h6" color="text.secondary">
                                        Hiện tại chưa có khóa học VIP nào
                                    </Typography>
                                </Box>
                            </Grid>
                        ) : (
                            courses.map((course) => (
                                <Grid item xs={12} sm={6} md={4} key={course.courseId}>
                                    <Card 
                                        sx={{ 
                                            height: '100%', 
                                            display: 'flex', 
                                            flexDirection: 'column',
                                            position: 'relative',
                                            border: '2px solid #FFD700',
                                            '&:hover': {
                                                transform: 'translateY(-4px)',
                                                boxShadow: 4
                                            },
                                            transition: 'all 0.3s ease'
                                        }}
                                    >
                                        {/* VIP Badge */}
                                        <Chip
                                            icon={<FontAwesomeIcon icon={faCrown} />}
                                            label="VIP"
                                            sx={{
                                                position: 'absolute',
                                                top: 8,
                                                right: 8,
                                                backgroundColor: '#FFD700',
                                                color: '#000',
                                                fontWeight: 'bold',
                                                zIndex: 1
                                            }}
                                        />

                                        <CardMedia
                                            component="img"
                                            height="200"
                                            image={course.image || '/default-course-image.jpg'}
                                            alt={course.courseName}
                                        />
                                        
                                        <CardContent sx={{ flexGrow: 1, display: 'flex', flexDirection: 'column' }}>
                                            <Typography variant="h6" component="h2" gutterBottom>
                                                {course.courseName}
                                            </Typography>
                                            
                                            <Typography variant="body2" color="text.secondary" sx={{ mb: 2, flexGrow: 1 }}>
                                                {course.description}
                                            </Typography>

                                            <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                                                <FontAwesomeIcon icon={faClock} style={{ marginRight: '8px' }} />
                                                <Typography variant="body2">
                                                    {course.duration}
                                                </Typography>
                                            </Box>

                                            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                                                <Typography variant="h5" component="div" sx={{ color: '#FF4444', fontWeight: 'bold' }}>
                                                    {formatPrice(course.price)}
                                                </Typography>
                                                {course.courseType && (
                                                    <Chip 
                                                        label={course.courseType.courseTypeName} 
                                                        size="small" 
                                                        variant="outlined" 
                                                    />
                                                )}
                                            </Box>

                                            <Button
                                                variant="contained"
                                                fullWidth
                                                sx={{
                                                    backgroundColor: '#FFD700',
                                                    color: '#000',
                                                    fontWeight: 'bold',
                                                    '&:hover': {
                                                        backgroundColor: '#FFC000'
                                                    }
                                                }}
                                                onClick={() => handlePayment(course)}
                                                disabled={paymentLoading}
                                                startIcon={<FontAwesomeIcon icon={faPlay} />}
                                            >
                                                {paymentLoading ? 'Đang xử lý...' : 'Mua Ngay'}
                                            </Button>
                                        </CardContent>
                                    </Card>
                                </Grid>
                            ))
                        )}
                    </Grid>
                )}
            </Container>
            <Footer />
        </div>
    );
};

export default VipCourses; 