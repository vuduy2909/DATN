import React, { useEffect } from 'react';
import { 
    Container, 
    Paper, 
    Typography, 
    Box, 
    Grid, 
    Card, 
    CardContent, 
    Avatar,
    Button,
    Divider,
    List,
    ListItem,
    ListItemIcon,
    ListItemText,
    useTheme,
    createTheme,
    ThemeProvider
} from '@mui/material';
import {
    Phone,
    Email,
    LocationOn,
    AccessTime,
    Error,
    FitnessCenter,
    ArrowBack
} from '@mui/icons-material';
import { useNavigate, useLocation } from 'react-router-dom';

// StartGym Theme
const startGymTheme = createTheme({
    palette: {
        primary: {
            main: '#CF4044',
            light: '#E55A5E',
            dark: '#B8363C',
            contrastText: '#ffffff',
        },
        secondary: {
            main: '#f39c12',
            light: '#f7b733',
            dark: '#e67e22',
        },
    },
    typography: {
        fontFamily: '"Roboto", "Helvetica", "Arial", sans-serif',
        h3: {
            fontWeight: 700,
        },
        h4: {
            fontWeight: 600,
        },
        h5: {
            fontWeight: 600,
        },
    },
});

const Contact = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const isBanned = location.state?.isBanned || false;
    const username = location.state?.username || localStorage.getItem('username') || '';
    const customerName = location.state?.customerName || '';
    
    useEffect(() => {
        // Check if user accessed this page without being banned
        if (!isBanned && !location.state) {
            // If there's no state at all, this may be a direct access to the page
            console.log("Contact page accessed directly (not through banned redirect)");
        } else if (isBanned) {
            // Log the banned user information for debugging
            console.log("Banned user redirected to contact page:", { 
                username, 
                customerName: customerName || 'Not provided' 
            });
        }
    }, [isBanned, location.state, username, customerName]);

    return (
        <ThemeProvider theme={startGymTheme}>
            <Box sx={{ 
                minHeight: 'calc(100vh - 64px)',
                py: 8, 
                backgroundColor: '#f5f7fa'
            }}>
                <Container>
                    {/* Banned Account Message - Only show if user was redirected due to being banned */}
                    {isBanned && (
                        <Paper 
                            elevation={3} 
                            sx={{ 
                                p: 4, 
                                mb: 4, 
                                borderRadius: 2,
                                borderLeft: '5px solid #CF4044',
                                backgroundColor: '#fff8f8'
                            }}
                        >
                            <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                                <Avatar sx={{ bgcolor: 'primary.main', mr: 2 }}>
                                    <Error />
                                </Avatar>
                                <Typography variant="h5" component="h1" color="primary.dark">
                                    Tài khoản của bạn đã bị tạm dừng
                                </Typography>
                            </Box>
                            
                            <Typography variant="body1" paragraph sx={{ mb: 2 }}>
                                Tài khoản <strong>{username}</strong> 
                                {customerName && username !== customerName && (
                                    <> (thuộc về <strong>{customerName}</strong>)</>
                                )} 
                                hiện tại đang bị tạm dừng đăng nhập. 
                            </Typography>
                            
                            <Typography variant="body1" paragraph sx={{ mb: 3 }}>
                                Nếu bạn nghĩ đây là một hiểu lầm, hãy liên hệ với bộ phận chăm sóc khách hàng 
                                của chúng tôi qua các thông tin liên lạc bên dưới. 
                                Chúng tôi rất tiếc về sự bất tiện này.
                            </Typography>
                            
                            <Button 
                                variant="contained" 
                                color="primary" 
                                startIcon={<ArrowBack />}
                                onClick={() => navigate('/')}
                                sx={{ mt: 1 }}
                            >
                                Quay về trang chủ
                            </Button>
                        </Paper>
                    )}
                    
                    {/* Title if not banned */}
                    {!isBanned && (
                        <Box sx={{ mb: 4, textAlign: 'center' }}>
                            <Typography variant="h3" component="h1" gutterBottom sx={{ fontWeight: 700 }}>
                                Liên hệ với chúng tôi
                            </Typography>
                            <Typography variant="body1" color="text.secondary" sx={{ maxWidth: '700px', mx: 'auto', mb: 4 }}>
                                Chúng tôi luôn sẵn sàng hỗ trợ bạn. Hãy liên hệ với chúng tôi qua các thông tin dưới đây 
                                hoặc ghé thăm trung tâm để được tư vấn trực tiếp.
                            </Typography>
                        </Box>
                    )}

                    {/* Contact Information */}
                    <Grid container spacing={4}>
                        {/* Contact Details */}
                        <Grid item xs={12} md={6}>
                            <Card elevation={3} sx={{ height: '100%', borderRadius: 2 }}>
                                <CardContent sx={{ p: 4 }}>
                                    <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
                                        <FitnessCenter sx={{ color: 'primary.main', fontSize: 30, mr: 1.5 }} />
                                        <Typography variant="h4" component="h2" color="textPrimary">
                                            Liên hệ với chúng tôi
                                        </Typography>
                                    </Box>
                                    
                                    <Typography variant="body1" paragraph color="textSecondary">
                                        Bạn có thể liên hệ với đội ngũ hỗ trợ của chúng tôi thông qua các phương thức sau:
                                    </Typography>
                                    
                                    <List>
                                        <ListItem>
                                            <ListItemIcon sx={{ color: 'primary.main' }}>
                                                <Phone />
                                            </ListItemIcon>
                                            <ListItemText 
                                                primary="Điện thoại" 
                                                secondary="+84 123 456 789"
                                                primaryTypographyProps={{ fontWeight: 'bold' }}
                                            />
                                        </ListItem>
                                        
                                        <Divider variant="inset" component="li" />
                                        
                                        <ListItem>
                                            <ListItemIcon sx={{ color: 'primary.main' }}>
                                                <Email />
                                            </ListItemIcon>
                                            <ListItemText 
                                                primary="Email" 
                                                secondary="support@startgym.com"
                                                primaryTypographyProps={{ fontWeight: 'bold' }}
                                            />
                                        </ListItem>
                                        
                                        <Divider variant="inset" component="li" />
                                        
                                        <ListItem>
                                            <ListItemIcon sx={{ color: 'primary.main' }}>
                                                <LocationOn />
                                            </ListItemIcon>
                                            <ListItemText 
                                                primary="Địa chỉ" 
                                                secondary="54 Nguyễn Lương Bằng, Liên Chiểu, Đà Nẵng"
                                                primaryTypographyProps={{ fontWeight: 'bold' }}
                                            />
                                        </ListItem>
                                        
                                        <Divider variant="inset" component="li" />
                                        
                                        <ListItem>
                                            <ListItemIcon sx={{ color: 'primary.main' }}>
                                                <AccessTime />
                                            </ListItemIcon>
                                            <ListItemText 
                                                primary="Giờ làm việc" 
                                                secondary="Thứ 2 - Thứ 6: 8:00 - 18:00, Thứ 7: 8:00 - 12:00"
                                                primaryTypographyProps={{ fontWeight: 'bold' }}
                                            />
                                        </ListItem>
                                    </List>
                                </CardContent>
                            </Card>
                        </Grid>
                        
                        {/* Map */}
                        <Grid item xs={12} md={6}>
                            <Card elevation={3} sx={{ height: '100%', borderRadius: 2, overflow: 'hidden' }}>
                                <CardContent sx={{ p: 0, height: '100%', '&:last-child': { paddingBottom: 0 } }}>
                                    <Box sx={{ height: '100%', minHeight: 400 }}>
                                        <iframe 
                                            src="https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d3834.1104389757488!2d108.1496245!3d16.060869!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x3142191628555555%3A0x9bc79c67d38173!2zMTUgTmd1eeG7hW4gTMawxqFuZyBC4bqxbmcsIEhvw6AgS2jDoW5oIELhuq9jLCBMacOqbiBDaGnhu4N1LCDEkMOgIE7hurVuZywgVmnhu4d0IE5hbQ!5e0!3m2!1svi!2s!4v1699099937320!5m2!1svi!2s"
                                            width="100%" 
                                            height="100%" 
                                            style={{ border: 0 }} 
                                            allowFullScreen="" 
                                            loading="lazy" 
                                            referrerPolicy="no-referrer-when-downgrade">
                                        </iframe>
                                    </Box>
                                </CardContent>
                            </Card>
                        </Grid>
                    </Grid>
                </Container>
            </Box>
        </ThemeProvider>
    );
};

export default Contact; 