import React, { useState, useEffect } from 'react';
import {
    Box,
    Button,
    Container,
    Paper,
    Typography,
    Alert,
    Grid,
    TextField,
    Card,
    CardContent,
    LinearProgress,
    Chip,
    Divider
} from '@mui/material';
import UserFormService from '../services/UserFormService';
import { useAuth } from '../AuthContext';

const TestImageAnalysis = () => {
    const [selectedFile, setSelectedFile] = useState(null);
    const [previewUrl, setPreviewUrl] = useState('');
    const [loading, setLoading] = useState(false);
    const [result, setResult] = useState(null);
    const [error, setError] = useState('');
    const [savedAnalysis, setSavedAnalysis] = useState(null);
    const [testData, setTestData] = useState({
        height: 170,
        weight: 70,
        gender: 'Nam',
        waist_circumference: '',
        chest_circumference: ''
    });
    
    const { user } = useAuth();

    // Load saved analysis on component mount
    useEffect(() => {
        if (user?.id) {
            loadSavedAnalysis();
        }
    }, [user?.id]);

    const loadSavedAnalysis = async () => {
        try {
            const response = await UserFormService.getSavedAnalysis(user.id);
            if (response && response.hasAnalysis) {
                setSavedAnalysis(response.analysisData);
                console.log('Loaded saved analysis:', response.analysisData);
            }
        } catch (error) {
            console.error('Error loading saved analysis:', error);
        }
    };

    const handleFileChange = (event) => {
        const file = event.target.files[0];
        if (file) {
            setSelectedFile(file);
            setError('');
            setResult(null);
            
            // Create preview
            const reader = new FileReader();
            reader.onload = () => {
                setPreviewUrl(reader.result);
            };
            reader.readAsDataURL(file);
        }
    };

    const handleInputChange = (field, value) => {
        setTestData(prev => ({
            ...prev,
            [field]: value
        }));
    };

    const testAnalysis = async () => {
        if (!selectedFile) {
            setError('Vui lòng chọn ảnh để test');
            return;
        }

        if (!testData.height || !testData.weight || !testData.gender) {
            setError('Vui lòng điền đầy đủ chiều cao, cân nặng và giới tính');
            return;
        }

        try {
            setLoading(true);
            setError('');
            setResult(null);

            console.log('Testing image analysis with data:', {
                ...testData,
                fileName: selectedFile.name,
                fileSize: selectedFile.size,
                customerId: user?.id
            });

            const analysisResult = await UserFormService.analyzeBodyImage(
                selectedFile,
                testData.height,
                testData.weight,
                testData.gender,
                testData.waist_circumference || null,
                testData.chest_circumference || null,
                user?.id || null
            );

            console.log('Analysis result:', analysisResult);
            setResult(analysisResult);

            // Reload saved analysis to see if it was saved
            if (user?.id) {
                setTimeout(() => loadSavedAnalysis(), 1000);
            }

        } catch (error) {
            console.error('Test analysis error:', error);
            
            let errorMessage = 'Có lỗi xảy ra trong quá trình test';
            
            if (error.response) {
                if (error.response.status === 503) {
                    errorMessage = 'Hệ thống AI đang quá tải. Vui lòng thử lại sau.';
                } else if (error.response.data && error.response.data.analysis) {
                    errorMessage = error.response.data.analysis;
                }
            } else if (error.request) {
                errorMessage = 'Không thể kết nối đến server. Kiểm tra kết nối mạng.';
            }
            
            setError(errorMessage);
        } finally {
            setLoading(false);
        }
    };

    const clearData = () => {
        setSelectedFile(null);
        setPreviewUrl('');
        setResult(null);
        setError('');
        setSavedAnalysis(null);
    };

    return (
        <Container maxWidth="lg" sx={{ py: 4 }}>
            <Typography variant="h4" gutterBottom align="center">
                Test Phân Tích Hình Ảnh Cơ Thể
            </Typography>

            <Paper elevation={3} sx={{ p: 3, mb: 3 }}>
                <Typography variant="h6" gutterBottom>
                    🧪 Test Interface - Debug Mode
                </Typography>
                
                {user?.id ? (
                    <Alert severity="info" sx={{ mb: 2 }}>
                        Đã đăng nhập với User ID: {user.id} - Kết quả sẽ được lưu vào server
                    </Alert>
                ) : (
                    <Alert severity="warning" sx={{ mb: 2 }}>
                        Chưa đăng nhập - Kết quả sẽ không được lưu
                    </Alert>
                )}

                <Grid container spacing={3}>
                    {/* Input form */}
                    <Grid item xs={12} md={6}>
                        <Typography variant="h6" gutterBottom>
                            Thông tin cơ bản
                        </Typography>
                        
                        <Grid container spacing={2}>
                            <Grid item xs={6}>
                                <TextField
                                    fullWidth
                                    label="Chiều cao (cm)"
                                    type="number"
                                    value={testData.height}
                                    onChange={(e) => handleInputChange('height', parseFloat(e.target.value))}
                                />
                            </Grid>
                            <Grid item xs={6}>
                                <TextField
                                    fullWidth
                                    label="Cân nặng (kg)"
                                    type="number"
                                    value={testData.weight}
                                    onChange={(e) => handleInputChange('weight', parseFloat(e.target.value))}
                                />
                            </Grid>
                            <Grid item xs={6}>
                                <TextField
                                    fullWidth
                                    select
                                    label="Giới tính"
                                    value={testData.gender}
                                    onChange={(e) => handleInputChange('gender', e.target.value)}
                                    SelectProps={{
                                        native: true,
                                    }}
                                >
                                    <option value="Nam">Nam</option>
                                    <option value="Nữ">Nữ</option>
                                </TextField>
                            </Grid>
                            <Grid item xs={6}>
                                <TextField
                                    fullWidth
                                    label="Vòng eo (cm)"
                                    type="number"
                                    value={testData.waist_circumference}
                                    onChange={(e) => handleInputChange('waist_circumference', e.target.value)}
                                />
                            </Grid>
                            <Grid item xs={12}>
                                <TextField
                                    fullWidth
                                    label="Vòng ngực (cm)"
                                    type="number"
                                    value={testData.chest_circumference}
                                    onChange={(e) => handleInputChange('chest_circumference', e.target.value)}
                                />
                            </Grid>
                        </Grid>

                        <Box sx={{ mt: 3 }}>
                            <input
                                type="file"
                                accept="image/*"
                                id="test-image-input"
                                style={{ display: 'none' }}
                                onChange={handleFileChange}
                            />
                            <label htmlFor="test-image-input">
                                <Button variant="outlined" component="span" fullWidth>
                                    Chọn ảnh test
                                </Button>
                            </label>
                        </Box>

                        {selectedFile && (
                            <Box sx={{ mt: 2 }}>
                                <Typography variant="body2" color="text.secondary">
                                    File: {selectedFile.name} ({(selectedFile.size / 1024 / 1024).toFixed(2)} MB)
                                </Typography>
                            </Box>
                        )}

                        <Box sx={{ mt: 3, display: 'flex', gap: 2 }}>
                            <Button
                                variant="contained"
                                onClick={testAnalysis}
                                disabled={!selectedFile || loading}
                                fullWidth
                            >
                                {loading ? 'Đang phân tích...' : 'Test Phân Tích'}
                            </Button>
                            <Button
                                variant="outlined"
                                color="secondary"
                                onClick={clearData}
                                disabled={loading}
                            >
                                Clear
                            </Button>
                        </Box>

                        {loading && <LinearProgress sx={{ mt: 2 }} />}
                    </Grid>

                    {/* Preview and results */}
                    <Grid item xs={12} md={6}>
                        {previewUrl && (
                            <Box sx={{ mb: 3 }}>
                                <Typography variant="h6" gutterBottom>
                                    Preview ảnh
                                </Typography>
                                <img
                                    src={previewUrl}
                                    alt="Preview"
                                    style={{
                                        maxWidth: '100%',
                                        maxHeight: 300,
                                        borderRadius: 8,
                                        border: '1px solid #ddd'
                                    }}
                                />
                            </Box>
                        )}

                        {error && (
                            <Alert severity="error" sx={{ mb: 2 }}>
                                {error}
                            </Alert>
                        )}

                        {result && (
                            <Card sx={{ mb: 2 }}>
                                <CardContent>
                                    <Typography variant="h6" gutterBottom>
                                        🎯 Kết quả phân tích
                                    </Typography>
                                    <Chip 
                                        label={`Loại cơ thể: ${result.bodyType}`}
                                        color="primary"
                                        sx={{ mb: 2 }}
                                    />
                                    <Typography variant="body2" gutterBottom>
                                        <strong>Phân tích:</strong> {result.analysis}
                                    </Typography>
                                    <Divider sx={{ my: 2 }} />
                                    <Typography variant="body2">
                                        <strong>Khuyến nghị:</strong> {result.recommendations}
                                    </Typography>
                                </CardContent>
                            </Card>
                        )}

                        {savedAnalysis && (
                            <Card>
                                <CardContent>
                                    <Typography variant="h6" gutterBottom>
                                        💾 Kết quả đã lưu
                                    </Typography>
                                    <Chip 
                                        label={`Loại cơ thể: ${savedAnalysis.bodyType || 'N/A'}`}
                                        color="secondary"
                                        sx={{ mb: 2 }}
                                    />
                                    <Typography variant="body2" sx={{ mb: 1 }}>
                                        <strong>Cập nhật lần cuối:</strong> {savedAnalysis.lastUpdated || 'N/A'}
                                    </Typography>
                                    {savedAnalysis.height && (
                                        <Typography variant="body2" sx={{ mb: 1 }}>
                                            <strong>Chiều cao:</strong> {savedAnalysis.height}cm
                                        </Typography>
                                    )}
                                    {savedAnalysis.weight && (
                                        <Typography variant="body2" sx={{ mb: 1 }}>
                                            <strong>Cân nặng:</strong> {savedAnalysis.weight}kg
                                        </Typography>
                                    )}
                                </CardContent>
                            </Card>
                        )}
                    </Grid>
                </Grid>
            </Paper>
        </Container>
    );
};

export default TestImageAnalysis; 