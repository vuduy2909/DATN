import React, { useState, useEffect } from 'react';
import { collectionService } from '../../service/collectionService';
import Step1 from '../Steps/Step1';
import Step2 from '../Steps/Step2';
import Step3 from '../Steps/Step3';
import Step4 from '../Steps/Step4';
import Step5 from '../Steps/Step5';
import Step6 from '../Steps/Step6';

import { useAuth } from '../../AuthContext'
import { request } from '../../utils/axiosInstance';
import { useNavigate } from 'react-router-dom';

const Question = () => {
    const { user } = useAuth();
    const [userData, setUserData] = useState([]);

    const navigate = useNavigate()

    useEffect(() => {
        const fetchUserData = async () => {
            try {
                const res = await request.get('/customer/detail')
                setUserData(res)
            } catch (error) {
                console.log("Error fetching user information", error);
            }
        }

        if (user) {
            fetchUserData();
        }
    }, [user])

    const [formData, setFormData] = useState({
        age: '',
        gender: '',
        bmi: '',
        training_goals: '',
        training_history: '',
        occupation: '',
        work_environment: '',
        heart_condition: '',
        respiratory_condition: '',
        injury: '',
        customerId: undefined
    });

    const [currentStep, setCurrentStep] = useState(1);

    const handleChange = (e) => {
        setFormData((prevFormData) => ({
            ...prevFormData,
            [e.target.name]: e.target.value,
            customerId: userData.customerId
        }));
    };

    const handleNext = () => {
        if (currentStep === 6) {
            handleSubmit();
        } else {
            setCurrentStep(currentStep + 1);
        }
    };

    const handlePrevious = () => {
        setCurrentStep(currentStep - 1);
    };

    const handleSubmit = async () => {
        console.log("Form data before submission:", formData);
        
        // Check if any required fields are missing
        const requiredFields = ['age', 'gender', 'bmi', 'customerId'];
        const missingFields = requiredFields.filter(field => formData[field] === '');
        
        if (missingFields.length > 0) {
            console.error("Missing required fields:", missingFields);
            alert(`Vui lòng điền đầy đủ thông tin: ${missingFields.join(', ')}`);
            return;
        }
        
        try {
            console.log("Submitting survey data...");
            const survey = await collectionService.postRecommend(formData);
            console.log("Survey response:", survey);
            
            if (survey && survey.recommendedCourses && survey.recommendedCourses.length > 0) {
                navigate('/collections');
            } else {
                console.error("No recommended courses returned:", survey);
                alert(survey?.message || 'Không có khóa học phù hợp!');
            }
        } catch (error) {
            console.error('Error submitting survey data:', error);
            if (error.response) {
                console.error('Server error response:', error.response.data);
                alert(`Lỗi: ${error.response.data?.message || 'Có lỗi xảy ra khi gửi dữ liệu!'}`);
            } else {
                alert('Có lỗi xảy ra khi gửi dữ liệu!');
            }
        }
    };

    const renderStep = () => {
        switch (currentStep) {
            case 1:
                return (
                    <Step1 formData={formData} handleChange={handleChange} handleNext={handleNext} />
                );
            case 2:
                return (
                    <Step2 formData={formData} handleChange={handleChange} handleNext={handleNext} handlePrevious={handlePrevious} />
                );
            case 3:
                return (
                    <Step3 formData={formData} handleChange={handleChange} handleNext={handleNext} handlePrevious={handlePrevious} />
                );
            case 4:
                return (
                    <Step4 formData={formData} handleChange={handleChange} handleNext={handleNext} handlePrevious={handlePrevious} />
                );
            case 5:
                return (
                    <Step5 formData={formData} handleChange={handleChange} handleNext={handleNext} handlePrevious={handlePrevious} currentStep={currentStep} />
                );
            case 6:
                return (
                    <Step6 formData={formData} handleChange={handleChange} handleNext={handleNext} handlePrevious={handlePrevious} currentStep={currentStep} />
                );
            default:
                return null;
        }
    };

    return (
        <div>
            {renderStep()}
        </div>
    );
};

export default Question;
