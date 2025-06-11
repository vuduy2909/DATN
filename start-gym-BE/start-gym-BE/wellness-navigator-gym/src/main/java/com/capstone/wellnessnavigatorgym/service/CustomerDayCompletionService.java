package com.capstone.wellnessnavigatorgym.service;

import com.capstone.wellnessnavigatorgym.entity.*;
import com.capstone.wellnessnavigatorgym.repository.*;
import com.capstone.wellnessnavigatorgym.exception.AttendanceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

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

        // QUAN TRỌNG: Chỉ kiểm tra điểm danh cho cụ thể (customer_id, course_id, day_id)
        // Không giới hạn điểm danh across courses - user có thể điểm danh cùng day_id ở nhiều course khác nhau
        Optional<CustomerDayCompletion> existingCompletion = completionRepository.findByCustomerAndCourseAndDay(customerId, courseId, dayId);
        
        if (existingCompletion.isPresent() && existingCompletion.get().getIsCompleted()) {
            throw new AttendanceException("Bạn đã điểm danh Day " + dayId + " của khóa học này rồi!");
        }

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
     */
    public CustomerDayCompletion markDayAsCompleted(Integer customerId, Integer courseId, Integer dayId) {
        log.info("Marking day {} as completed for customer {} in course {}", dayId, customerId, courseId);
        
        // Kiểm tra các điều kiện điểm danh
        validateAttendance(customerId, courseId, dayId);
        
        // Tìm hoặc tạo completion record
        CustomerDayCompletion completion = findOrCreateCompletion(customerId, courseId, dayId);
        
        // Đánh dấu hoàn thành
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
     */
    public CourseProgressDTO getCourseProgress(Integer customerId, Integer courseId) {
        // Đếm số ngày đã hoàn thành từ bảng customer_day_completion
        Long completedDays = completionRepository.countCompletedDaysByCustomerAndCourse(customerId, courseId);
        
        // QUAN TRỌNG: Đếm tổng số ngày từ bảng course_days, không phải từ completion records
        Long totalDays = courseDayRepository.countByCourseId(courseId);
        
        // Nếu course không có ngày nào trong course_days, khởi tạo completion records
        if (totalDays == 0) {
            log.warn("Course {} has no days in course_days table, initializing...", courseId);
            initializeCompletionRecords(customerId, courseId);
            totalDays = courseDayRepository.countByCourseId(courseId);
        }
        
        // Nếu chưa có completion records cho customer này, tạo chúng
        Long existingCompletionRecords = completionRepository.countTotalDaysByCustomerAndCourse(customerId, courseId);
        if (existingCompletionRecords == 0 && totalDays > 0) {
            log.info("No completion records found for customer {} in course {}, initializing...", customerId, courseId);
            initializeCompletionRecords(customerId, courseId);
            // Đếm lại số ngày đã hoàn thành sau khi khởi tạo
            completedDays = completionRepository.countCompletedDaysByCustomerAndCourse(customerId, courseId);
        }
        
        Boolean isCompleted = false;
        if (totalDays > 0) {
            isCompleted = completedDays.equals(totalDays);
        }
        
        log.info("Course progress for customer {} in course {}: {}/{} days completed", 
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
     */
    public List<CustomerDayCompletion> getCustomerCourseCompletions(Integer customerId, Integer courseId) {
        List<CustomerDayCompletion> completions = completionRepository.findByCustomerAndCourse(customerId, courseId);
        
        // Nếu chưa có completion records, tạo chúng
        if (completions.isEmpty()) {
            initializeCompletionRecords(customerId, courseId);
            completions = completionRepository.findByCustomerAndCourse(customerId, courseId);
        }
        
        return completions;
    }
    
    /**
     * Khởi tạo completion records cho customer khi bắt đầu course
     */
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
     * Tìm hoặc tạo completion record
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