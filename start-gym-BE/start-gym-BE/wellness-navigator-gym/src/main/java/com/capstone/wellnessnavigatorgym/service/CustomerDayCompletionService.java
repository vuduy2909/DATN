package com.capstone.wellnessnavigatorgym.service;

import com.capstone.wellnessnavigatorgym.entity.*;
import com.capstone.wellnessnavigatorgym.repository.*;
import com.capstone.wellnessnavigatorgym.exception.AttendanceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CustomerDayCompletionService {
    
    private final ICustomerDayCompletionRepository completionRepository;
    private final ICustomerRepository customerRepository;
    private final ICourseRepository courseRepository;
    private final IDayRepository dayRepository;
    private final ICourseDayRepository courseDayRepository;

    @Autowired
    private IUserDataAiService userDataAiService;
    
    @Autowired 
    private ITrackDataAiService trackDataAiService;

    @Autowired
    private ICustomerCourseService customerCourseService;
    
    @PersistenceContext
    private EntityManager entityManager;

    private static final LocalTime ATTENDANCE_START_TIME = LocalTime.of(7, 0); // 7:00 AM
    private static final LocalTime ATTENDANCE_END_TIME = LocalTime.of(22, 0); // 10:00 PM
    
    /**
     * Getter for completionRepository to allow access from controllers
     */
    public ICustomerDayCompletionRepository getCompletionRepository() {
        return completionRepository;
    }
    
    /**
     * Kiểm tra xem có thể điểm danh không
     */
    private void validateAttendance(Integer customerId, Integer courseId, Integer dayId) {
        LocalDateTime now = LocalDateTime.now();
        LocalTime currentTime = now.toLocalTime();

        // Kiểm tra xem user đã tham gia khóa học chưa
        boolean isEnrolled = customerCourseService.isCustomerEnrolledInCourse(customerId, courseId);
        if (!isEnrolled) {
            throw new AttendanceException("Bạn chưa tham gia khóa học này. Vui lòng tham gia khóa học trước khi điểm danh.");
        }

        // Tạm thời comment phần kiểm tra thời gian để test
        /* 
        // Kiểm tra thời gian điểm danh
        if (currentTime.isBefore(ATTENDANCE_START_TIME) || currentTime.isAfter(ATTENDANCE_END_TIME)) {
            throw new AttendanceException("Chỉ được điểm danh từ 7:00 sáng đến 22:00 tối");
        }
        */

        // QUAN TRỌNG: Kiểm tra điểm danh trùng lặp sẽ được thực hiện trong createCompletionRecordOnSuccess
        // Ở đây chỉ cần kiểm tra các điều kiện khác

        // QUAN TRỌNG: Kiểm tra one-day-one-attendance per course per calendar day
        // User chỉ được điểm danh 1 lần cho mỗi khóa học trong 1 ngày
        LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfToday = now.toLocalDate().plusDays(1).atStartOfDay();
        
        // Kiểm tra xem đã điểm danh bất kỳ ngày nào của course này trong hôm nay chưa
        List<CustomerDayCompletion> todayCompletions = completionRepository.findCompletedDaysByCustomerAndDateRange(
                customerId, startOfToday, endOfToday);
        
        // Lọc các completion của course hiện tại
        boolean hasAttendanceInCourseToday = todayCompletions.stream()
                .anyMatch(completion -> completion.getCourse().getCourseId().equals(courseId));
        
        if (hasAttendanceInCourseToday) {
            throw new AttendanceException("Bạn đã điểm danh khóa học này trong hôm nay rồi! Mỗi ngày chỉ được điểm danh 1 lần cho mỗi khóa học.");
        }

        // Kiểm tra điểm danh liên tiếp TRONG CÙNG KHÓA HỌC
        Integer lastCompletedDay = completionRepository.findLastCompletedDayNumber(customerId, courseId);
        log.info("Last completed day for customer {} in course {}: {}", customerId, courseId, lastCompletedDay);
        
        if (lastCompletedDay == null) {
            // Chưa điểm danh ngày nào trong course này, chỉ được điểm danh day 1
            if (dayId != 1) {
                throw new AttendanceException("Bạn phải bắt đầu điểm danh từ ngày 1 trong khóa học này");
            }
        } else {
            // Đã có điểm danh trước đó trong course này, kiểm tra tính liên tiếp
            if (dayId != lastCompletedDay + 1) {
                throw new AttendanceException("Bạn phải điểm danh các ngày liên tiếp trong khóa học này (ngày tiếp theo phải là ngày " + (lastCompletedDay + 1) + ")");
            }
        }
        
        log.info("Attendance validation passed for customer {} in course {}, day {}", customerId, courseId, dayId);
    }
    
    /**
     * Đánh dấu một buổi học là đã hoàn thành
     * CHỈ tạo bản ghi khi user thực sự điểm danh thành công
     */
    public CustomerDayCompletion markDayAsCompleted(Integer customerId, Integer courseId, Integer dayId) {
        log.info("Marking day {} as completed for customer {} in course {}", dayId, customerId, courseId);
        
        // Kiểm tra các điều kiện điểm danh
        validateAttendance(customerId, courseId, dayId);
        
        // CHỈ tạo bản ghi khi điểm danh thành công - không tạo trước
        CustomerDayCompletion completion = createCompletionRecordOnSuccess(customerId, courseId, dayId);
        
        // Đánh dấu hoàn thành ngay khi tạo
        completion.markAsCompleted();
        
        CustomerDayCompletion savedCompletion = completionRepository.save(completion);
        
        // QUAN TRỌNG: Kiểm tra xem có phải ngày cuối của course không
        if (isLastDayOfCourse(courseId, dayId)) {
            log.info("Customer {} completed the last day of course {}! Triggering post-course measurement.", customerId, courseId);
            // Course đã hoàn thành - cần prompt user để nhập measurements mới
            // Việc này sẽ được xử lý ở frontend thông qua API response
        }
        
        return savedCompletion;
    }
    
    /**
     * Đánh dấu một buổi học là chưa hoàn thành
     */
    public CustomerDayCompletion markDayAsIncomplete(Integer customerId, Integer courseId, Integer dayId) {
        log.info("Marking day {} as incomplete for customer {} in course {}", dayId, customerId, courseId);
        
        // Tìm hoặc tạo completion record
        CustomerDayCompletion completion = findOrCreateCompletion(customerId, courseId, dayId);
        
        // Đánh dấu chưa hoàn thành
        completion.markAsIncomplete();
        
        return completionRepository.save(completion);
    }
    
    /**
     * Lấy trạng thái hoàn thành của một buổi học
     */
    public boolean isDayCompleted(Integer customerId, Integer courseId, Integer dayId) {
        Optional<CustomerDayCompletion> completion = completionRepository
                .findByCustomerAndCourseAndDay(customerId, courseId, dayId);
        
        return completion.map(CustomerDayCompletion::getIsCompleted).orElse(false);
    }
    
    /**
     * Lấy tiến độ hoàn thành của customer trong một course
     * KHÔNG tự động tạo completion records - chỉ đếm những gì đã thực sự điểm danh
     */
    public CourseProgressDTO getCourseProgress(Integer customerId, Integer courseId) {
        // Đếm số ngày đã hoàn thành từ bảng customer_day_completion (chỉ những bản ghi thực sự có)
        Long completedDays = completionRepository.countCompletedDaysByCustomerAndCourse(customerId, courseId);
        
        // QUAN TRỌNG: Đếm tổng số ngày từ bảng course_days
        Long totalDays = courseDayRepository.countByCourseId(courseId);
        
        // Nếu course không có ngày nào trong course_days, đây là lỗi cấu hình
        if (totalDays == 0) {
            log.warn("Course {} has no days in course_days table!", courseId);
            totalDays = 1L; // Set minimum để tránh lỗi chia cho 0
        }
        
        // KHÔNG tự động tạo completion records nữa
        // Chỉ tính progress dựa trên những gì user đã thực sự điểm danh
        
        Boolean isCompleted = false;
        if (totalDays > 0) {
            isCompleted = completedDays.equals(totalDays);
        }
        
        log.info("Course progress for customer {} in course {}: {}/{} days completed (no auto-initialization)", 
                customerId, courseId, completedDays, totalDays);
        
        return new CourseProgressDTO(
                customerId,
                courseId,
                completedDays.intValue(),
                totalDays.intValue(),
                isCompleted
        );
    }
    
    /**
     * Lấy danh sách tất cả completion records của customer trong course
     * CHỈ trả về những ngày đã thực sự điểm danh (có completion_date)
     */
    public List<CustomerDayCompletion> getCustomerCourseCompletions(Integer customerId, Integer courseId) {
        List<CustomerDayCompletion> completions = completionRepository.findByCustomerAndCourse(customerId, courseId);
        
        // KHÔNG tự động tạo completion records nữa
        // Chỉ trả về những gì user đã thực sự điểm danh
        log.info("Found {} actual completion records for customer {} in course {}", 
                completions.size(), customerId, courseId);
        
        return completions;
    }
    
    /**
     * Khởi tạo completion records cho customer khi bắt đầu course
     * @deprecated KHÔNG SỬ DỤNG - Method này tạo ra nhiều bản ghi null không cần thiết
     * Chỉ giữ lại để tương thích với code cũ, nhưng không được gọi trong logic mới
     */
    @Deprecated
    public void initializeCompletionRecords(Integer customerId, Integer courseId) {
        log.info("Initializing completion records for customer {} in course {}", customerId, courseId);
        
        // Lấy customer và course
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + customerId));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found: " + courseId));
        
        // Lấy tất cả course days của course
        List<CourseDays> courseDays = courseDayRepository.findByCourseIdOrderByDayId(courseId);
        
        if (courseDays.isEmpty()) {
            log.warn("No days found for course {}, creating default completion records", courseId);
            // Nếu không có ngày nào, thêm ít nhất một ngày mặc định để đảm bảo có tiến độ
            Day defaultDay = dayRepository.findById(1)
                .orElseGet(() -> {
                    Day newDay = new Day();
                    newDay.setDayName("Day 1");
                    return dayRepository.save(newDay);
                });
                
            // Tạo một CourseDays liên kết
            CourseDays defaultCourseDay = new CourseDays();
            defaultCourseDay.setCourse(course);
            defaultCourseDay.setDay(defaultDay);
            defaultCourseDay.setStatus(true);
            courseDayRepository.save(defaultCourseDay);
            
            // Thêm defaultCourseDay vào danh sách courseDays
            courseDays = new ArrayList<>();
            courseDays.add(defaultCourseDay);
            
            log.info("Created default day for course {}", courseId);
        }
        
        // Tạo completion record cho mỗi day
        for (CourseDays courseDay : courseDays) {
            Day day = courseDay.getDay();
            if (day != null) {
                // Kiểm tra xem đã có completion record chưa
                if (!completionRepository.existsByCustomerAndCourseAndDay(customerId, courseId, day.getDayId())) {
                    CustomerDayCompletion completion = new CustomerDayCompletion(customer, course, day);
                    completionRepository.save(completion);
                }
            }
        }
        
        log.info("Initialized {} completion records for customer {} in course {}", 
                courseDays.size(), customerId, courseId);
    }
    
    /**
     * Xóa tất cả completion records của customer trong course
     */
    public void deleteCustomerCourseCompletions(Integer customerId, Integer courseId) {
        log.info("Deleting completion records for customer {} in course {}", customerId, courseId);
        
        int deletedCount = completionRepository.deleteByCustomerAndCourse(customerId, courseId);
        
        log.info("Deleted {} completion records for customer {} in course {}", 
                deletedCount, customerId, courseId);
    }
    
    /**
     * Dọn dẹp tất cả bản ghi null (completion_date = null) trong database
     * CHỈ giữ lại những bản ghi đã thực sự điểm danh (completion_date không null)
     */
    @Transactional
    public Map<String, Object> cleanupNullCompletionRecords() {
        log.info("Starting cleanup of null completion records (completion_date = null)");
        
        try {
            // Đếm số bản ghi null trước khi xóa
            String countNullSql = "SELECT COUNT(*) FROM customer_day_completion WHERE completion_date IS NULL";
            javax.persistence.Query countQuery = entityManager.createNativeQuery(countNullSql);
            Number nullRecordsCount = (Number) countQuery.getSingleResult();
            
            // Đếm tổng số bản ghi
            String countTotalSql = "SELECT COUNT(*) FROM customer_day_completion";
            javax.persistence.Query countTotalQuery = entityManager.createNativeQuery(countTotalSql);
            Number totalRecordsCount = (Number) countTotalQuery.getSingleResult();
            
            // Xóa tất cả bản ghi có completion_date = null
            String deleteSql = "DELETE FROM customer_day_completion WHERE completion_date IS NULL";
            javax.persistence.Query deleteQuery = entityManager.createNativeQuery(deleteSql);
            int deletedCount = deleteQuery.executeUpdate();
            
            // Đếm số bản ghi còn lại sau khi xóa
            Number remainingRecordsCount = (Number) countTotalQuery.getSingleResult();
            
            Map<String, Object> result = new HashMap<>();
            result.put("totalRecordsBefore", totalRecordsCount.intValue());
            result.put("nullRecordsDeleted", deletedCount);
            result.put("remainingRecords", remainingRecordsCount.intValue());
            result.put("cleanupTime", LocalDateTime.now());
            result.put("success", true);
            result.put("message", String.format("Đã xóa %d bản ghi null, còn lại %d bản ghi thực sự", 
                    deletedCount, remainingRecordsCount.intValue()));
            
            log.info("Cleanup completed: Deleted {} null records, {} records remaining", 
                    deletedCount, remainingRecordsCount.intValue());
            
            return result;
            
        } catch (Exception e) {
            log.error("Error during cleanup of null completion records", e);
            
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", e.getMessage());
            errorResult.put("cleanupTime", LocalDateTime.now());
            
            return errorResult;
        }
    }
    
    /**
     * Thống kê số lượng bản ghi null và thực tế trong database
     */
    public Map<String, Object> getCompletionRecordsStatistics() {
        try {
            // Đếm tổng số bản ghi
            String totalSql = "SELECT COUNT(*) FROM customer_day_completion";
            javax.persistence.Query totalQuery = entityManager.createNativeQuery(totalSql);
            Number totalRecords = (Number) totalQuery.getSingleResult();
            
            // Đếm số bản ghi null
            String nullSql = "SELECT COUNT(*) FROM customer_day_completion WHERE completion_date IS NULL";
            javax.persistence.Query nullQuery = entityManager.createNativeQuery(nullSql);
            Number nullRecords = (Number) nullQuery.getSingleResult();
            
            // Đếm số bản ghi thực tế (có completion_date)
            String actualSql = "SELECT COUNT(*) FROM customer_day_completion WHERE completion_date IS NOT NULL";
            javax.persistence.Query actualQuery = entityManager.createNativeQuery(actualSql);
            Number actualRecords = (Number) actualQuery.getSingleResult();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalRecords", totalRecords.intValue());
            stats.put("nullRecords", nullRecords.intValue());
            stats.put("actualRecords", actualRecords.intValue());
            stats.put("nullPercentage", totalRecords.intValue() > 0 ? 
                    (nullRecords.doubleValue() / totalRecords.doubleValue() * 100) : 0);
            stats.put("timestamp", LocalDateTime.now());
            
            log.info("Completion records statistics: Total={}, Null={}, Actual={}", 
                    totalRecords.intValue(), nullRecords.intValue(), actualRecords.intValue());
            
            return stats;
            
        } catch (Exception e) {
            log.error("Error getting completion records statistics", e);
            Map<String, Object> errorStats = new HashMap<>();
            errorStats.put("error", e.getMessage());
            errorStats.put("timestamp", LocalDateTime.now());
            return errorStats;
        }
    }
    
    /**
     * CHỈ tạo completion record khi user điểm danh thành công
     * Không tạo trước các bản ghi cho ngày chưa điểm danh
     */
    private CustomerDayCompletion createCompletionRecordOnSuccess(Integer customerId, Integer courseId, Integer dayId) {
        // Kiểm tra xem đã có bản ghi chưa - nếu có thì báo lỗi
        Optional<CustomerDayCompletion> existing = completionRepository
                .findByCustomerAndCourseAndDay(customerId, courseId, dayId);
        
        if (existing.isPresent()) {
            throw new AttendanceException("Bạn đã điểm danh ngày " + dayId + " của khóa học này rồi!");
        }
        
        // Tạo mới bản ghi chỉ khi điểm danh thành công
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + customerId));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found: " + courseId));
        Day day = dayRepository.findById(dayId)
                .orElseThrow(() -> new RuntimeException("Day not found: " + dayId));
        
        log.info("Creating new completion record for customer {} course {} day {} on successful attendance", 
                customerId, courseId, dayId);
        
        return new CustomerDayCompletion(customer, course, day);
    }
    
    /**
     * Tìm hoặc tạo completion record (chỉ dùng cho markDayAsIncomplete)
     */
    private CustomerDayCompletion findOrCreateCompletion(Integer customerId, Integer courseId, Integer dayId) {
        Optional<CustomerDayCompletion> existing = completionRepository
                .findByCustomerAndCourseAndDay(customerId, courseId, dayId);
        
        if (existing.isPresent()) {
            return existing.get();
        }
        
        // Tạo mới nếu chưa có
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + customerId));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found: " + courseId));
        Day day = dayRepository.findById(dayId)
                .orElseThrow(() -> new RuntimeException("Day not found: " + dayId));
        
        return new CustomerDayCompletion(customer, course, day);
    }
    
    /**
     * DTO class cho course progress
     */
    public static class CourseProgressDTO {
        private Integer customerId;
        private Integer courseId;
        private Integer completedDays;
        private Integer totalDays;
        private Boolean isCompleted;
        private Double progressPercentage;
        
        public CourseProgressDTO(Integer customerId, Integer courseId, Integer completedDays, 
                               Integer totalDays, Boolean isCompleted) {
            this.customerId = customerId;
            this.courseId = courseId;
            this.completedDays = completedDays;
            this.totalDays = totalDays;
            this.isCompleted = isCompleted;
            this.progressPercentage = totalDays > 0 ? (completedDays * 100.0 / totalDays) : 0.0;
        }
        
        // Getters
        public Integer getCustomerId() { return customerId; }
        public Integer getCourseId() { return courseId; }
        public Integer getCompletedDays() { return completedDays; }
        public Integer getTotalDays() { return totalDays; }
        public Boolean getIsCompleted() { return isCompleted; }
        public Double getProgressPercentage() { return progressPercentage; }
    }
    
    /**
     * Kiểm tra xem course đã được hoàn thành chưa (tất cả days đều completed)
     */
    public boolean isCourseFullyCompleted(Integer customerId, Integer courseId) {
        Long totalDays = courseDayRepository.countByCourseId(courseId);
        Long completedDays = completionRepository.countCompletedDaysByCustomerAndCourse(customerId, courseId);
        
        return totalDays > 0 && totalDays.equals(completedDays);
    }
    
    /**
     * Kiểm tra xem day vừa complete có phải là ngày cuối của course không
     */
    public boolean isLastDayOfCourse(Integer courseId, Integer dayId) {
        List<CourseDays> courseDays = courseDayRepository.findByCourseIdOrderByDayId(courseId);
        if (courseDays.isEmpty()) {
            return false;
        }
        
        // Lấy ngày cuối cùng của course
        CourseDays lastDay = courseDays.get(courseDays.size() - 1);
        return lastDay.getDay().getDayId().equals(dayId);
    }

    /**
     * Tính toán Weekly Activity dựa trên số buổi hoàn thành / mục tiêu trong tuần
     */
    public Map<String, Object> calculateWeeklyActivity(String username) {
        // Tính thời gian bắt đầu tuần (thứ Hai) và kết thúc tuần (Chủ nhật)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekStart = now.toLocalDate().with(java.time.DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime weekEnd = weekStart.plusDays(7); // Đến 00:00 thứ Hai tuần sau
        
        log.info("Calculating weekly activity for user {} from {} to {}", username, weekStart, weekEnd);
        
        // Đếm số buổi đã hoàn thành trong tuần
        Integer completedSessions = completionRepository.countCompletedSessionsInCurrentWeek(
                username, weekStart, weekEnd);
        
        // Đếm số khóa học đang tham gia (mỗi khóa học là 3 buổi/tuần)
        Integer activeCourses = completionRepository.countActiveCoursesForCustomer(username);
        
        // Mỗi khóa học yêu cầu 3 buổi/tuần
        final int sessionsPerCoursePerWeek = 3;
        
        // Tính tổng số buổi mục tiêu dựa trên số khóa học
        Integer targetSessions = activeCourses * sessionsPerCoursePerWeek;
        
        // Đặt minimum target là 3 buổi/tuần
        if (targetSessions < 3) {
            targetSessions = 3;
        }
        
        // Tính phần trăm hoàn thành
        double weeklyActivityPercentage;
        if (targetSessions > 0) {
            weeklyActivityPercentage = ((double) completedSessions / targetSessions) * 100;
            // Giới hạn tối đa 100%
            if (weeklyActivityPercentage > 100) {
                weeklyActivityPercentage = 100;
            }
        } else {
            weeklyActivityPercentage = 0;
        }
        
        log.info("Weekly activity for user {}: {}/{} sessions ({}%)", 
                username, completedSessions, targetSessions, weeklyActivityPercentage);
        
        // Trả về map chứa kết quả
        Map<String, Object> result = new HashMap<>();
        result.put("weeklyActivity", weeklyActivityPercentage);
        result.put("completedSessions", completedSessions);
        result.put("targetSessions", targetSessions);
        result.put("weekStart", weekStart);
        result.put("weekEnd", weekEnd);
        
        return result;
    }
} 