/**
 * Các tiện ích hỗ trợ điều hướng giữa các trang
 */

/**
 * Chuyển hướng về trang chủ từ trang admin
 * Xử lý các vấn đề liên quan đến routing và quyền admin
 */
export const navigateToHome = () => {
  console.log("Bắt đầu quá trình chuyển hướng về trang chủ từ admin...");
  
  // Đặt cờ trạng thái để thông báo rằng đang chuyển hướng từ trang admin về trang chủ
  sessionStorage.setItem('navigatingFromAdmin', 'true');
  
  // Sử dụng replace thay vì href để tránh các vấn đề lịch sử duyệt
  window.location.replace('/');
}; 