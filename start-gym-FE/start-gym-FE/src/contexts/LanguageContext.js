import React, { createContext, useContext, useState, useEffect } from 'react';

const LanguageContext = createContext();

export const useLanguage = () => {
  const context = useContext(LanguageContext);
  if (!context) {
    throw new Error('useLanguage must be used within a LanguageProvider');
  }
  return context;
};

export const LanguageProvider = ({ children }) => {
  const [language, setLanguage] = useState(() => {
    // Load language from localStorage or default to 'en'
    return localStorage.getItem('appLanguage') || 'en';
  });

  const toggleLanguage = () => {
    const newLanguage = language === 'en' ? 'vi' : 'en';
    setLanguage(newLanguage);
    localStorage.setItem('appLanguage', newLanguage);
  };

  const setSpecificLanguage = (lang) => {
    setLanguage(lang);
    localStorage.setItem('appLanguage', lang);
  };

  // Common translations for the entire app
  const translations = {
    en: {
      // Common
      loading: 'Loading...',
      error: 'Error',
      success: 'Success',
      cancel: 'Cancel',
      save: 'Save',
      delete: 'Delete',
      edit: 'Edit',
      view: 'View',
      back: 'Back',
      
      // Course related
      recommended: 'Recommended',
      selfEnrolled: 'Self-enrolled',
      vip: 'VIP',
      free: 'Free',
      courses: 'courses',
      course: 'Course',
      
      // User Dashboard
      dashboard: 'Dashboard',
      profile: 'Profile',
      account: 'Account',
      weeklyActivity: 'Weekly Activity',
      courseInProgress: 'Course In Progress',
      bmi: 'BMI',
      activityChart: 'Activity Chart',
      progress: 'Progress',
      trackProgressByDay: 'Track progress by day',
      
      // Body Metrics
      bodyMetrics: 'Body Metrics',
      basicInformation: 'Basic Information',
      bodyMeasurements: 'Body Measurements',
      beforeAfterComparison: 'Before/After Comparison',
      age: 'Age',
      gender: 'Gender',
      height: 'Height',
      weight: 'Weight',
      waist: 'Waist',
      chest: 'Chest',
      before: 'Before',
      after: 'After',
      effective: 'Effective',
      notEffective: 'Not effective',
      
      // Admin
      enrolledCourses: 'Enrolled Courses',
      progressAttendance: 'Progress & Attendance',
      bodyMetricsHistory: 'Body Metrics History',
      records: 'records',
      total: 'Total',
      totalPaid: 'Total Paid',
      viewProgress: 'View Progress',
      noCoursesFound: 'No courses found',
      noCourseMessage: 'This customer has not enrolled in any courses yet.',
      noBodyMetrics: 'No Body Metrics data available',
      loadingBodyMetrics: 'Loading body metrics data...',
      
      // Profile
      email: 'Email',
      phone: 'Phone',
      address: 'Address',
      dateOfBirth: 'Date of Birth',
      location: 'Location',
      biography: 'Biography',
      information: 'Information'
    },
    vi: {
      // Common
      loading: 'Đang tải...',
      error: 'Lỗi',
      success: 'Thành công',
      cancel: 'Hủy',
      save: 'Lưu',
      delete: 'Xóa',
      edit: 'Sửa',
      view: 'Xem',
      back: 'Quay lại',
      
      // Course related
      recommended: 'Đề xuất',
      selfEnrolled: 'Tự tham gia',
      vip: 'VIP',
      free: 'Miễn phí',
      courses: 'khóa học',
      course: 'Khóa học',
      
      // User Dashboard
      dashboard: 'Bảng điều khiển',
      profile: 'Hồ sơ',
      account: 'Tài khoản',
      weeklyActivity: 'Hoạt động hàng tuần',
      courseInProgress: 'Khóa học đang học',
      bmi: 'BMI',
      activityChart: 'Biểu đồ hoạt động',
      progress: 'Tiến độ',
      trackProgressByDay: 'Theo dõi tiến độ theo ngày',
      
      // Body Metrics
      bodyMetrics: 'Chỉ số cơ thể',
      basicInformation: 'Thông tin cơ bản',
      bodyMeasurements: 'Chỉ số cơ thể',
      beforeAfterComparison: 'So sánh trước/sau',
      age: 'Tuổi',
      gender: 'Giới tính',
      height: 'Chiều cao',
      weight: 'Cân nặng',
      waist: 'Vòng eo',
      chest: 'Vòng ngực',
      before: 'Trước',
      after: 'Sau',
      effective: 'Hiệu quả',
      notEffective: 'Chưa hiệu quả',
      
      // Admin
      enrolledCourses: 'Khóa học đã đăng ký',
      progressAttendance: 'Tiến độ & Điểm danh',
      bodyMetricsHistory: 'Lịch sử chỉ số cơ thể',
      records: 'bản ghi',
      total: 'Tổng',
      totalPaid: 'Tổng đã trả',
      viewProgress: 'Xem tiến độ',
      noCoursesFound: 'Không tìm thấy khóa học',
      noCourseMessage: 'Khách hàng chưa đăng ký khóa học nào.',
      noBodyMetrics: 'Chưa có dữ liệu chỉ số cơ thể',
      loadingBodyMetrics: 'Đang tải dữ liệu chỉ số cơ thể...',
      
      // Profile
      email: 'Email',
      phone: 'Số điện thoại',
      address: 'Địa chỉ',
      dateOfBirth: 'Ngày sinh',
      location: 'Địa điểm',
      biography: 'Tiểu sử',
      information: 'Thông tin'
    }
  };

  const t = translations[language];

  const value = {
    language,
    setLanguage: setSpecificLanguage,
    toggleLanguage,
    t,
    translations
  };

  return (
    <LanguageContext.Provider value={value}>
      {children}
    </LanguageContext.Provider>
  );
}; 