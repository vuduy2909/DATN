import React, { useState } from 'react';
import { useAuth } from '../../AuthContext';
import { request } from '../../utils/axiosInstance';
import './CourseJoinButton.css';

const CourseJoinButton = ({ courseId, courseName, onJoinSuccess }) => {
  const [isJoining, setIsJoining] = useState(false);
  const [showConfirmModal, setShowConfirmModal] = useState(false);
  const [message, setMessage] = useState('');
  const [messageType, setMessageType] = useState(''); // 'success' | 'error'
  const { user } = useAuth();

  const handleJoinClick = () => {
    if (!user) {
      setMessage('Vui lòng đăng nhập để tham gia khóa học');
      setMessageType('error');
      return;
    }
    setShowConfirmModal(true);
  };

  const handleConfirmJoin = async () => {
    setIsJoining(true);
    setShowConfirmModal(false);
    
    try {
      const response = await request.post(`/customer-course/join/${courseId}`);
      
      if (response.success) {
        setMessage('Tham gia khóa học thành công!');
        setMessageType('success');
        
        // Callback để update UI parent component
        if (onJoinSuccess) {
          onJoinSuccess(courseId);
        }
      } else {
        setMessage(response.message || 'Có lỗi xảy ra khi tham gia khóa học');
        setMessageType('error');
      }
    } catch (error) {
      if (error.response?.status === 409) {
        setMessage('Bạn đã tham gia khóa học này rồi!');
      } else if (error.response?.status === 401) {
        setMessage('Vui lòng đăng nhập để tham gia khóa học');
      } else {
        setMessage('Có lỗi xảy ra. Vui lòng thử lại sau.');
      }
      setMessageType('error');
    } finally {
      setIsJoining(false);
      
      // Auto hide message after 3 seconds
      setTimeout(() => {
        setMessage('');
        setMessageType('');
      }, 3000);
    }
  };

  const handleCancelJoin = () => {
    setShowConfirmModal(false);
  };

  return (
    <div className="course-join-container">
      <button 
        className="join-course-btn"
        onClick={handleJoinClick}
        disabled={isJoining}
      >
        {isJoining ? (
          <div className="loading-spinner">
            <div className="spinner"></div>
            Đang tham gia...
          </div>
        ) : (
          <>
            <span className="join-icon">➕</span>
            Tham gia khóa học
          </>
        )}
      </button>

      {/* Confirmation Modal */}
      {showConfirmModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h3>Xác nhận tham gia khóa học</h3>
            <p>
              Bạn có muốn tham gia khóa học <strong>"{courseName}"</strong> không?
              <br />
              Sau khi tham gia, bạn sẽ có thể truy cập tất cả tài liệu và bài tập của khóa học.
            </p>
            <div className="modal-actions">
              <button 
                className="confirm-btn"
                onClick={handleConfirmJoin}
              >
                Có, tham gia
              </button>
              <button 
                className="cancel-btn"
                onClick={handleCancelJoin}
              >
                Hủy
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Message Toast */}
      {message && (
        <div className={`message-toast ${messageType}`}>
          <span className={messageType === 'success' ? 'success-icon' : 'error-icon'}>
            {messageType === 'success' ? '✅' : '❌'}
          </span>
          {message}
        </div>
      )}
    </div>
  );
};

export default CourseJoinButton; 