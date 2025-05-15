import { useNavigate } from 'react-router-dom';

// Trong component Question
const Question = () => {
const navigate = useNavigate();

    // Tìm hàm xử lý khi nhấn nút "Hoàn thành"
    // Có thể là hàm handleSubmit hoặc tương tự
    
    const handleComplete = async (formData) => {
        try {
            const survey = await collectionService.postRecommend(formData);
            
            if (survey) {
                // Lưu kết quả recommend vào localStorage hoặc state global
                localStorage.setItem('hasRecommendation', 'true');
                
                // Chuyển hướng sang trang collections
                navigate('/collections');
            }
        } catch (error) {
            console.error("Error submitting recommendation:", error);
        }
    }
    
    // Cập nhật sự kiện onClick của nút Hoàn thành
    return (
        // ...
        <button onClick={() => handleComplete(formData)}>Hoàn thành</button>
        // ...
    );
}import { useState, useEffect } from 'react';

// Trong component Collections
const Collections = () => {
const [showRecommendSidebar, setShowRecommendSidebar] = useState(false);

    useEffect(() => {
        // Kiểm tra xem đã có recommendation chưa
        const hasRecommendation = localStorage.getItem('hasRecommendation') === 'true';
        
        if (hasRecommendation) {
            setShowRecommendSidebar(true);
        }
    }, []);
    
    return (
        <div className="collections-container">
            {/* Main content */}
            <div className="main-content">
                {/* Nội dung chính */}
            </div>
            
            {/* Sidebar */}
            {showRecommendSidebar && (
                <div className="recommend-sidebar">
                    {/* Hiển thị nội dung recommend ở đây */}
                </div>
            )}
        </div>
    );
}// Trong App.js
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Recommend from './pages/Recommend';
import Collections from './pages/Collections';

function App() {
return (
<Router>
<Routes>
<Route path="/recommend" element={<Recommend />} />
<Route path="/collections" element={<Collections />} />
{/* Các routes khác */}
</Routes>
</Router>
);
}# DATN