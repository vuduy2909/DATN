import React from 'react';
import {
  Container,
  Paper,
  Typography,
  Box,
  Card,
  CardContent,
  Grid,
  Button,
  Divider,
  Alert
} from '@mui/material';
import {
  Email as EmailIcon,
  Phone as PhoneIcon,
  LocationOn as LocationIcon,
  Facebook as FacebookIcon,
  Instagram as InstagramIcon,
  AccessTime as TimeIcon
} from '@mui/icons-material';
import { useLocation, useNavigate } from 'react-router-dom';

const Contact = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const isBannedUser = location.state?.banned || false;

  const handleBackToLogin = () => {
    navigate('/login');
  };

  const handleBackToHome = () => {
    navigate('/');
  };

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Paper elevation={3} sx={{ p: 4 }}>
        {/* Ban Notice */}
        {isBannedUser && (
          <Alert 
            severity="warning" 
            sx={{ mb: 4, fontSize: '1.1rem' }}
          >
            <Typography variant="h6" sx={{ mb: 1 }}>
              🚫 Tài khoản của bạn tạm thời không thể đăng nhập
            </Typography>
            <Typography>
              Tài khoản của bạn đã bị tạm khóa do vi phạm quy định sử dụng dịch vụ. 
              Nếu bạn cho rằng đây là hiểu lầm, vui lòng liên hệ với chúng tôi qua các thông tin bên dưới.
            </Typography>
          </Alert>
        )}

        {/* Header */}
        <Box textAlign="center" mb={4}>
          <Typography variant="h3" component="h1" color="primary" fontWeight="bold" gutterBottom>
            Liên hệ với chúng tôi
          </Typography>
          <Typography variant="h6" color="text.secondary">
            Chúng tôi luôn sẵn sàng hỗ trợ bạn
          </Typography>
        </Box>

        <Grid container spacing={4}>
          {/* Contact Information */}
          <Grid item xs={12} md={6}>
            <Card elevation={2} sx={{ height: '100%' }}>
              <CardContent sx={{ p: 3 }}>
                <Typography variant="h5" color="primary" gutterBottom fontWeight="bold">
                  Thông tin liên hệ
                </Typography>
                <Divider sx={{ mb: 3 }} />

                <Box display="flex" alignItems="center" mb={3}>
                  <EmailIcon color="primary" sx={{ mr: 2, fontSize: 28 }} />
                  <Box>
                    <Typography variant="h6" fontWeight="bold">
                      Email
                    </Typography>
                    <Typography color="text.secondary">
                      support@startgym.com
                    </Typography>
                    <Typography color="text.secondary">
                      admin@startgym.com
                    </Typography>
                  </Box>
                </Box>

                <Box display="flex" alignItems="center" mb={3}>
                  <PhoneIcon color="primary" sx={{ mr: 2, fontSize: 28 }} />
                  <Box>
                    <Typography variant="h6" fontWeight="bold">
                      Hotline
                    </Typography>
                    <Typography color="text.secondary">
                      1900 1234 (Miễn phí)
                    </Typography>
                    <Typography color="text.secondary">
                      024 3456 7890
                    </Typography>
                  </Box>
                </Box>

                <Box display="flex" alignItems="center" mb={3}>
                  <LocationIcon color="primary" sx={{ mr: 2, fontSize: 28 }} />
                  <Box>
                    <Typography variant="h6" fontWeight="bold">
                      Địa chỉ
                    </Typography>
                    <Typography color="text.secondary">
                      123 Đường ABC, Quận 1
                    </Typography>
                    <Typography color="text.secondary">
                      TP. Hồ Chí Minh, Việt Nam
                    </Typography>
                  </Box>
                </Box>

                <Box display="flex" alignItems="center" mb={3}>
                  <TimeIcon color="primary" sx={{ mr: 2, fontSize: 28 }} />
                  <Box>
                    <Typography variant="h6" fontWeight="bold">
                      Giờ làm việc
                    </Typography>
                    <Typography color="text.secondary">
                      Thứ 2 - Thứ 6: 8:00 - 18:00
                    </Typography>
                    <Typography color="text.secondary">
                      Thứ 7 - Chủ nhật: 9:00 - 17:00
                    </Typography>
                  </Box>
                </Box>
              </CardContent>
            </Card>
          </Grid>

          {/* Social Media & Support */}
          <Grid item xs={12} md={6}>
            <Card elevation={2} sx={{ height: '100%' }}>
              <CardContent sx={{ p: 3 }}>
                <Typography variant="h5" color="primary" gutterBottom fontWeight="bold">
                  Kết nối với chúng tôi
                </Typography>
                <Divider sx={{ mb: 3 }} />

                <Box mb={3}>
                  <Typography variant="h6" fontWeight="bold" mb={2}>
                    Mạng xã hội
                  </Typography>
                  
                  <Box display="flex" gap={2} mb={2}>
                    <Button
                      variant="outlined"
                      startIcon={<FacebookIcon />}
                      href="https://facebook.com/startgym"
                      target="_blank"
                      sx={{ flex: 1 }}
                    >
                      Facebook
                    </Button>
                    <Button
                      variant="outlined"
                      startIcon={<InstagramIcon />}
                      href="https://instagram.com/startgym"
                      target="_blank"
                      sx={{ flex: 1 }}
                    >
                      Instagram
                    </Button>
                  </Box>
                </Box>

                {isBannedUser && (
                  <Box mb={3}>
                    <Typography variant="h6" color="error" fontWeight="bold" mb={2}>
                      Khiếu nại về tài khoản
                    </Typography>
                    <Typography color="text.secondary" mb={2}>
                      Nếu bạn cho rằng việc khóa tài khoản là hiểu lầm, vui lòng gửi email kèm theo:
                    </Typography>
                    <Typography component="ul" color="text.secondary" sx={{ pl: 2 }}>
                      <li>Tên đăng nhập</li>
                      <li>Email đăng ký</li>
                      <li>Lý do khiếu nại</li>
                      <li>Thông tin liên quan (nếu có)</li>
                    </Typography>
                  </Box>
                )}

                <Box>
                  <Typography variant="h6" fontWeight="bold" mb={2}>
                    Hỗ trợ khách hàng
                  </Typography>
                  <Typography color="text.secondary" mb={2}>
                    Đội ngũ hỗ trợ của chúng tôi luôn sẵn sàng giải đáp mọi thắc mắc của bạn về:
                  </Typography>
                  <Typography component="ul" color="text.secondary" sx={{ pl: 2 }}>
                    <li>Tài khoản và đăng nhập</li>
                    <li>Khóa học và bài tập</li>
                    <li>Thanh toán và VIP</li>
                    <li>Báo lỗi và góp ý</li>
                  </Typography>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        </Grid>

        {/* Action Buttons */}
        <Box display="flex" justifyContent="center" gap={2} mt={4}>
          {isBannedUser ? (
            <Button
              variant="contained"
              onClick={handleBackToLogin}
              size="large"
            >
              Quay lại trang đăng nhập
            </Button>
          ) : (
            <Button
              variant="contained"
              onClick={handleBackToHome}
              size="large"
            >
              Quay lại trang chủ
            </Button>
          )}
        </Box>
      </Paper>
    </Container>
  );
};

export default Contact; 