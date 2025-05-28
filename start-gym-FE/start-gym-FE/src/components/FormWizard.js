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
    console.log('handleNext called, current activeStep:', activeStep);
    setActiveStep((prevActiveStep) => {
      console.log('Setting activeStep from', prevActiveStep, 'to', prevActiveStep + 1);
      return prevActiveStep + 1;
    });
  };

  // Quay lại bước trước
  const handleBack = () => {
    console.log('handleBack called, current activeStep:', activeStep);
    setActiveStep((prevActiveStep) => {
      console.log('Setting activeStep from', prevActiveStep, 'to', prevActiveStep - 1);
      return prevActiveStep - 1;
    });
  };

  // Gửi dữ liệu form khi hoàn thành
  const handleSubmit = async () => {
    try {
      console.log('handleSubmit called, current activeStep:', activeStep);
      setLoading(true);
      setError('');
      
      // Đảm bảo customerId đã được set
      if (!formData.customerId) {
        console.error('Missing customerId in form data');
        setError('Missing customer ID. Please ensure you are logged in.');
        return;
      }
      
      // Helper function to handle alert popups
      const handleAlertPopups = () => {
        // Check if there's a dialog overlay visible
        const overlays = document.querySelectorAll('.MuiBackdrop-root, .MuiDialog-root');
        console.log('Found', overlays.length, 'possible overlay elements');
        
        // Try to find buttons in dialogs
        const dialogButtons = document.querySelectorAll('.MuiDialogActions-root button, .MuiDialog-root button, .MuiAlert-action button');
        
        if (dialogButtons.length > 0) {
          console.log('Found', dialogButtons.length, 'buttons in dialogs');
          // Click the first button (usually OK or Close)
          dialogButtons[0].click();
          console.log('Clicked a dialog button to dismiss popup');
        }
        
        // Move to step 7 regardless
        console.log('Moving to step 7 after handling popups');
        setActiveStep(6); // Step 7 (index 6)
      };
      
      // Đảm bảo đúng tên trường với backend (chestSize -> bust, waistSize -> waist)
      const apiFormData = {
        training_history: formData.trainingFrequency, // Lịch sử tập luyện hiện tại
        training_frequency: formData.desiredFrequency, // Tần suất tập luyện mong muốn (renamed from trainingFrequency)
        age: formData.age,
        gender: formData.gender,
        height: formData.height,
        weight: formData.weight,
        bmi: Math.round((formData.weight / Math.pow(formData.height/100, 2)) * 100) / 100, // Tính BMI và làm tròn 2 chữ số thập phân
        training_goals: formData.trainingGoal,
        occupation: formData.occupation,
        work_environment: formData.workEnvironment,
        chest_circumference: formData.chestSize, 
        waist_circumference: formData.waistSize,
        customerId: formData.customerId
      };
      
      console.log('FormWizard: Submitting form data:', apiFormData);
      console.log('FormWizard: training_history =', apiFormData.training_history);
      console.log('FormWizard: training_frequency =', apiFormData.training_frequency);
      console.log('FormWizard: training_goals =', apiFormData.training_goals);
      console.log('FormWizard: occupation =', apiFormData.occupation);
      console.log('FormWizard: work_environment =', apiFormData.work_environment);
      
      try {
        // Gọi API để gửi dữ liệu và nhận đề xuất
        const result = await UserFormService.submitFormData(apiFormData);
        console.log('API response:', result);
        
        // Kiểm tra nếu có userDataId trong kết quả
        if (result && result.userDataId) {
          console.log('Received userDataId:', result.userDataId);
          
          try {
            // Gọi API endpoint recommendation-step-7 để lấy dữ liệu chi tiết cho bước 7
            const step7Data = await UserFormService.getRecommendationStep7Data(result.userDataId);
            console.log('Step 7 data:', step7Data);
            
            // Kết hợp dữ liệu từ cả hai API để có đầy đủ thông tin
            const completeResult = {
              ...result,
              ...step7Data,
              userDataId: result.userDataId
            };
            
            console.log('Combined result data for step 7:', completeResult);
            // Lưu kết quả đầy đủ vào state
            setRecommendationResult(completeResult);

            // Xử lý popup và chuyển sang Step 7
            setTimeout(handleAlertPopups, 800);
            
          } catch (step7Error) {
            console.error('Error fetching step 7 data:', step7Error);
            // Nếu không lấy được dữ liệu step 7, vẫn sử dụng kết quả recommendation 
            setRecommendationResult(result);
            
            // Xử lý popup và chuyển sang Step 7
            setTimeout(handleAlertPopups, 800);
          }
        } else {
          // Không có userDataId, sử dụng kết quả gốc
          console.log('No userDataId in result, using original result:', result);
          setRecommendationResult(result);
          
          // Xử lý popup và chuyển sang Step 7
          setTimeout(handleAlertPopups, 800);
        }
        
        // Set flag đã có đề xuất vào localStorage
        localStorage.setItem('hasRecommendation', 'true');
        localStorage.setItem('selectedCategory', 'Recommend');
        
      } catch (apiError) {
        console.error('API Error:', apiError);
        setError('Có lỗi xảy ra khi gửi dữ liệu. Vui lòng thử lại sau.');
      }
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

  // Add a direct method to navigate to step 7
  const goToStep7 = (result) => {
    console.log('Directly navigating to step 7 with result:', result);
    
    if (result) {
      // Save recommendation result
      setRecommendationResult(result);
      
      // Set flag đã có đề xuất vào localStorage
      localStorage.setItem('hasRecommendation', 'true');
      localStorage.setItem('selectedCategory', 'Recommend');
      localStorage.setItem('recommendationData', JSON.stringify(result));
    }
    
    // Force direct navigation to step 7 immediately
    setActiveStep(6);
    
    // Dismiss any alerts if present - do this right away
    const buttons = document.querySelectorAll('button');
    for (let button of buttons) {
      if (button.textContent === 'OK' || button.textContent === 'Close') {
        console.log('Found and clicking dialog button');
        button.click();
        break;
      }
    }
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
        return <Step6 formData={formData} handleChange={handleChange} handleNext={goToStep7} handlePrevious={handleBack} />;
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

  // Set up MutationObserver to detect alerts
  useEffect(() => {
    // Function to check if an element is a dialog
    const isDialogElement = (node) => {
      return node.classList && 
             (node.classList.contains('MuiDialog-root') || 
              node.classList.contains('MuiAlert-root') ||
              node.classList.contains('MuiBackdrop-root'));
    };
    
    // Function to handle new dialog elements
    const handleNewDialog = (node) => {
      console.log('Detected new dialog element:', node);
      
      // Check if this is the recommendation success dialog
      const dialogText = node.textContent || '';
      if (dialogText.includes('Course recommendations generated successfully')) {
        console.log('Found recommendation success dialog, will auto-dismiss');
        
        // Find and click the OK button
        setTimeout(() => {
          const okButton = node.querySelector('button') || 
                          document.querySelector('.MuiDialogActions-root button') ||
                          document.querySelector('button.MuiButton-containedPrimary');
          
          if (okButton) {
            console.log('Found OK button in dialog, clicking it');
            okButton.click();
          }
          
          // Force move to step 7
          setActiveStep(6);
        }, 500);
      }
    };
    
    // Create a mutation observer to watch for new dialogs
    const observer = new MutationObserver((mutations) => {
      mutations.forEach((mutation) => {
        // Check for added nodes
        if (mutation.addedNodes && mutation.addedNodes.length > 0) {
          mutation.addedNodes.forEach((node) => {
            // Check if the node is a dialog element
            if (node.nodeType === 1) { // Element node
              if (isDialogElement(node)) {
                handleNewDialog(node);
              } else {
                // Check child elements
                const dialogs = node.querySelectorAll('.MuiDialog-root, .MuiAlert-root, .MuiBackdrop-root');
                dialogs.forEach(handleNewDialog);
              }
            }
          });
        }
      });
    });
    
    // Start observing the document body
    observer.observe(document.body, { 
      childList: true,
      subtree: true
    });
    
    // Cleanup
    return () => {
      observer.disconnect();
    };
  }, []);

  return (
    <Container maxWidth="md" sx={{ py: 1 }}>
      <Paper elevation={3} sx={{ p: 2, borderRadius: 2 }}>
        <Typography variant="h4" align="center" gutterBottom color="primary">
          KYO-GYM Assessment
        </Typography>
        
        <Stepper activeStep={activeStep} alternativeLabel sx={{ mb: 2 }}>
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