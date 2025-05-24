import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import Header from '../components/Header/Header';
import Footer from '../components/Footer/Footer';
import {
    Container,
    Paper,
    Typography,
    Box,
    Button,
    CircularProgress,
    Alert
} from '@mui/material';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faCheckCircle, faTimesCircle, faSpinner } from '@fortawesome/free-solid-svg-icons';
import { request } from '../utils/axiosInstance';

const PaymentResult = () => {
    const location = useLocation();
    const navigate = useNavigate();
    const [paymentResult, setPaymentResult] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        const urlParams = new URLSearchParams(location.search);
        const vnpParams = {};
        
        // Extract all VNPay parameters from URL
        for (const [key, value] of urlParams.entries()) {
            vnpParams[key] = value;
        }

        if (Object.keys(vnpParams).length > 0) {
            processPaymentResult(vnpParams);
        } else {
            setError('Không tìm thấy thông tin thanh toán');
            setLoading(false);
        }
    }, [location.search]);

    const processPaymentResult = async (vnpParams) => {
        try {
            setLoading(true);
            
            // Call backend to process VNPay return
            const response = await request.get('/api/v1/payment/vnpay-return', {
                params: vnpParams
            });

            setPaymentResult(response.data);
        } catch (error) {
            console.error('Error processing payment result:', error);
            setError('Lỗi khi xử lý kết quả thanh toán: ' + (error.response?.data?.message || error.message));
        } finally {
            setLoading(false);
        }
    };

    const getStatusIcon = () => {
        if (!paymentResult) return null;
        
        switch (paymentResult.status) {
            case 'SUCCESS':
                return <FontAwesomeIcon icon={faCheckCircle} style={{ fontSize: '64px', color: '#4CAF50' }} />;
            case 'FAILED':
                return <FontAwesomeIcon icon={faTimesCircle} style={{ fontSize: '64px', color: '#F44336' }} />;
            default:
                return <FontAwesomeIcon icon={faSpinner} style={{ fontSize: '64px', color: '#FF9800' }} />;
        }
    };

    const getStatusColor = () => {
        if (!paymentResult) return 'info';
        
        switch (paymentResult.status) {
            case 'SUCCESS':
                return 'success';
            case 'FAILED':
                return 'error';
            default:
                return 'warning';
        }
    };

    const handleBackToCourses = () => {
        if (paymentResult?.status === 'SUCCESS') {
            navigate('/user'); // Go to user dashboard to see enrolled courses
        } else {
            navigate('/vip-courses'); // Go back to VIP courses page
        }
    };

    const handleGoHome = () => {
        navigate('/');
    };

    if (loading) {
        return (
            <div>
                <Header />
                <Container maxWidth="md" sx={{ py: 8 }}>
                    <Paper elevation={3} sx={{ p: 4, textAlign: 'center' }}>
                        <CircularProgress size={60} sx={{ mb: 3 }} />
                        <Typography variant="h5" gutterBottom>
                            Đang xử lý kết quả thanh toán...
                        </Typography>
                        <Typography variant="body1" color="text.secondary">
                            Vui lòng đợi trong giây lát
                        </Typography>
                    </Paper>
                </Container>
                <Footer />
            </div>
        );
    }

    return (
        <div>
            <Header />
            <Container maxWidth="md" sx={{ py: 8 }}>
                <Paper elevation={3} sx={{ p: 4 }}>
                    <Box sx={{ textAlign: 'center', mb: 4 }}>
                        {getStatusIcon()}
                        <Typography variant="h4" component="h1" gutterBottom sx={{ mt: 2 }}>
                            {paymentResult?.status === 'SUCCESS' ? 'Thanh toán thành công!' : 
                             paymentResult?.status === 'FAILED' ? 'Thanh toán thất bại!' : 
                             'Trạng thái thanh toán không xác định'}
                        </Typography>
                    </Box>

                    {error && (
                        <Alert severity="error" sx={{ mb: 3 }}>
                            {error}
                        </Alert>
                    )}

                    {paymentResult && (
                        <>
                            <Alert severity={getStatusColor()} sx={{ mb: 3 }}>
                                <Typography variant="body1">
                                    {paymentResult.message}
                                </Typography>
                            </Alert>

                            <Box sx={{ mb: 3 }}>
                                <Typography variant="h6" gutterBottom>
                                    Thông tin giao dịch:
                                </Typography>
                                
                                <Box sx={{ 
                                    backgroundColor: '#f5f5f5', 
                                    p: 2, 
                                    borderRadius: 1,
                                    '& > div': { 
                                        display: 'flex', 
                                        justifyContent: 'space-between',
                                        py: 1,
                                        borderBottom: '1px solid #e0e0e0'
                                    }
                                }}>
                                    <div>
                                        <Typography variant="body2" color="text.secondary">
                                            Mã giao dịch:
                                        </Typography>
                                        <Typography variant="body2" fontWeight="bold">
                                            {paymentResult.transactionRef}
                                        </Typography>
                                    </div>
                                    
                                    <div>
                                        <Typography variant="body2" color="text.secondary">
                                            Mã phản hồi:
                                        </Typography>
                                        <Typography variant="body2" fontWeight="bold">
                                            {paymentResult.responseCode}
                                        </Typography>
                                    </div>
                                    
                                    <div style={{ borderBottom: 'none' }}>
                                        <Typography variant="body2" color="text.secondary">
                                            Trạng thái:
                                        </Typography>
                                        <Typography 
                                            variant="body2" 
                                            fontWeight="bold"
                                            color={paymentResult.status === 'SUCCESS' ? 'success.main' : 'error.main'}
                                        >
                                            {paymentResult.status === 'SUCCESS' ? 'Thành công' : 'Thất bại'}
                                        </Typography>
                                    </div>
                                </Box>
                            </Box>

                            {paymentResult.status === 'SUCCESS' && (
                                <Alert severity="success" sx={{ mb: 3 }}>
                                    <Typography variant="body2">
                                        🎉 Chúc mừng! Bạn đã thành công đăng ký khóa học VIP. 
                                        Khóa học sẽ được thêm vào tài khoản của bạn trong vài phút.
                                    </Typography>
                                </Alert>
                            )}
                        </>
                    )}

                    <Box sx={{ display: 'flex', gap: 2, justifyContent: 'center', mt: 4 }}>
                        <Button
                            variant="contained"
                            onClick={handleBackToCourses}
                            sx={{ minWidth: 150 }}
                        >
                            {paymentResult?.status === 'SUCCESS' ? 'Xem khóa học của tôi' : 'Thử lại'}
                        </Button>
                        
                        <Button
                            variant="outlined"
                            onClick={handleGoHome}
                            sx={{ minWidth: 150 }}
                        >
                            Về trang chủ
                        </Button>
                    </Box>
                </Paper>
            </Container>
            <Footer />
        </div>
    );
};

export default PaymentResult; 