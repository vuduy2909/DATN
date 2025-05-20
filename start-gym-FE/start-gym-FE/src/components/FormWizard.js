import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Box, Container, Paper, Stepper, Step, StepLabel, Typography, Button, CircularProgress, Alert } from '@mui/material';
import Step1 from './Steps/Step1';
import Step2 from './Steps/Step2';
import Step3 from './Steps/Step3';
import Step4 from './Steps/Step4';
import Step5 from './Steps/Step5';
import Step6 from './Steps/Step6';
import Step7 from './Steps/Step7';
import UserFormService from '../services/UserFormService';

const FormWizard = () => {
  const navigate = useNavigate();
  const { id } = useParams(); // customerId từ URL
  const [activeStep, setActiveStep] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [recommendationResult, setRecommendationResult] = useState(null);
  
  // Khởi tạo formData với các trường cần thiết
  const [formData, setFormData] = useState({
    trainingFrequency: '',
    gender: '',
    age: '',
    height: '',
    weight: '',
    bodyType: '',
    trainingGoal: '',
    desiredFrequency: '',
    occupation: '',
    workEnvironment: '',
    healthIssues: [],
    chestSize: '',
    waistSize: '',
    bodyImage: null,
    customerId: id || ''
  });

  // Xử lý thay đổi dữ liệu form
  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value
    });
  };

  // Chuyển đến bước tiếp theo
  const handleNext = () => {
    setActiveStep((prevActiveStep) => prevActiveStep + 1);
  };

  // Quay lại bước trước
  const handleBack = () => {
    setActiveStep((prevActiveStep) => prevActiveStep - 1);
  };

  // Gửi dữ liệu form khi hoàn thành
  const handleSubmit = async () => {
    try {
      setLoading(true);
      setError('');
      
      // Đảm bảo customerId đã được set
      if (!formData.customerId) {
        console.error('Missing customerId in form data');
        setError('Missing customer ID. Please ensure you are logged in.');
        return;
      }
      
      // Đảm bảo đúng tên trường với backend (chestSize -> bust, waistSize -> waist)
      const apiFormData = {
        training_history: formData.trainingFrequency, // Lịch sử tập luyện hiện tại
        trainingFrequency: formData.desiredFrequency, // Tần suất tập luyện mong muốn
        age: formData.age,
        gender: formData.gender,
        height: formData.height,
        weight: formData.weight,
        bmi: formData.weight / Math.pow(formData.height/100, 2), // Tính BMI
        training_goals: formData.trainingGoal,
        occupation: formData.occupation,
        work_environment: formData.workEnvironment,
        health_issues: formData.healthIssues ? formData.healthIssues.join(',') : '',
        bust: formData.chestSize, // Đổi tên từ chestSize sang bust
        waist: formData.waistSize, // Đổi tên từ waistSize sang waist
        customerId: formData.customerId
      };
      
      console.log('Submitting converted form data:', apiFormData);
      
      // Gọi API để gửi dữ liệu và nhận đề xuất
      const result = await UserFormService.submitFormData(apiFormData);
      console.log('API response:', result);
      
      // Lưu kết quả vào state
      setRecommendationResult(result);
      
      // Set flag đã có đề xuất vào localStorage
      localStorage.setItem('hasRecommendation', 'true');
      localStorage.setItem('selectedCategory', 'Recommend');
      
      // Hiển thị step cuối với kết quả đề xuất
      handleNext();
    } catch (err) {
      console.error('Error submitting form:', err);
      setError(err.message || 'Có lỗi xảy ra khi gửi dữ liệu. Vui lòng thử lại sau.');
    } finally {
      setLoading(false);
    }
  };

  // Xử lý chuyển hướng sang trang Collections sau khi xem kết quả đề xuất
  const handleGoToCollections = () => {
    console.log('Navigating to collections');
    // Đảm bảo trang Collections sẽ hiển thị tab Recommend
    localStorage.setItem('hasRecommendation', 'true');
    localStorage.setItem('selectedCategory', 'Recommend');
    
    // Sử dụng window.location.href để tải lại trang thay vì navigate
    window.location.href = '/collections';
  };

  // Hiển thị bước hiện tại
  const getStepContent = (step) => {
    switch (step) {
      case 0:
        return <Step1 formData={formData} handleChange={handleChange} handleNext={handleNext} />;
      case 1:
        return <Step2 formData={formData} handleChange={handleChange} handleNext={handleNext} handlePrevious={handleBack} />;
      case 2:
        return <Step3 formData={formData} handleChange={handleChange} handleNext={handleNext} handlePrevious={handleBack} />;
      case 3:
        return <Step4 formData={formData} handleChange={handleChange} handleNext={handleNext} handlePrevious={handleBack} />;
      case 4:
        return <Step5 formData={formData} handleChange={handleChange} handleNext={handleNext} handlePrevious={handleBack} />;
      case 5:
        return <Step6 formData={formData} handleChange={handleChange} handleNext={handleSubmit} handlePrevious={handleBack} />;
      case 6:
        return <Step7 
                formData={formData} 
                recommendationResult={recommendationResult} 
                handlePrevious={handleBack}
                handleGoToCollections={handleGoToCollections}
                loading={loading}
                error={error}
               />;
      default:
        return 'Bước không hợp lệ';
    }
  };

  const steps = [
    'Thông tin tập luyện hiện tại',
    'Thông tin cơ bản',
    'Mục tiêu tập luyện',
    'Tần suất tập luyện mong muốn',
    'Thông tin nghề nghiệp và sức khỏe',
    'Số đo cơ thể',
    'Kết quả phân tích'
  ];

  return (
    <Container maxWidth="md" sx={{ py: 4 }}>
      <Paper elevation={3} sx={{ p: 4, borderRadius: 2 }}>
        <Typography variant="h4" align="center" gutterBottom color="primary">
          KYO-GYM Assessment
        </Typography>
        
        <Stepper activeStep={activeStep} alternativeLabel sx={{ mb: 4 }}>
          {steps.map((label) => (
            <Step key={label}>
              <StepLabel>{label}</StepLabel>
            </Step>
          ))}
        </Stepper>
        
        {loading && (
          <Box sx={{ display: 'flex', justifyContent: 'center', my: 4 }}>
            <CircularProgress />
          </Box>
        )}
        
        {error && (
          <Alert severity="error" sx={{ mb: 3 }}>
            {error}
          </Alert>
        )}
        
        <Box>
          {getStepContent(activeStep)}
        </Box>
      </Paper>
    </Container>
  );
};

export default FormWizard;