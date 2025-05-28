package com.capstone.wellnessnavigatorgym.service;

import com.capstone.wellnessnavigatorgym.entity.*;
import com.capstone.wellnessnavigatorgym.repository.*;
import com.capstone.wellnessnavigatorgym.exception.AttendanceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

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

    private static final LocalTime ATTENDANCE_START_TIME = LocalTime.of(7, 0); // 7:00 AM
    private static final LocalTime ATTENDANCE_END_TIME = LocalTime.of(22, 0); // 10:00 PM
    
    /**
     * Kiểm tra xem có thể điểm danh không
     */
    private void validateAttendance(Integer customerId, Integer courseId, Integer dayId) {
        LocalDateTime now = LocalDateTime.now();
        LocalTime currentTime = now.toLocalTime();

        // Tạm thời comment phần kiểm tra thời gian để test
        /* 
        // Kiểm tra thời gian điểm danh
        if (currentTime.isBefore(ATTENDANCE_START_TIME) || currentTime.isAfter(ATTENDANCE_END_TIME)) {
            throw new AttendanceException("Chỉ được điểm danh từ 7:00 sáng đến 22:00 tối");
        }
        */

        // Kiểm tra xem ngày này đã được điểm danh chưa
        if (isDayCompleted(customerId, courseId, dayId)) {
            throw new AttendanceException("Ngày này đã được điểm danh rồi");
        }

        // QUAN TRỌNG: Kiểm tra xem đã điểm danh BẤT KỲ NGÀY NÀO trong hôm nay chưa
        LocalDateTime startOfToday = now.toLocalDate().atStartOfDay(); // 00:00:00 hôm nay
        LocalDateTime endOfToday = now.toLocalDate().plusDays(1).atStartOfDay(); // 00:00:00 ngày mai
        
        log.info("=== DEBUGGING ATTENDANCE VALIDATION ===");
        log.info("Current time: {}", now);
        log.info("Start of today: {}", startOfToday);
        log.info("End of today: {}", endOfToday);
        log.info("Checking for existing completions for customer {} between {} and {}", customerId, startOfToday, endOfToday);
        
        // Kiểm tra bằng cách lấy TẤT CẢ completion records đã completed và check completion date
        List<CustomerDayCompletion> allCompletions = completionRepository.findByCustomer(customerId);
        log.info("Total completions for customer {}: {}", customerId, allCompletions.size());
        
        for (CustomerDayCompletion completion : allCompletions) {
            if (completion.getIsCompleted() && completion.getCompletionDate() != null) {
                LocalDateTime completionDate = completion.getCompletionDate();
                log.info("Checking completion: Day {}, completion date: {}", 
                        completion.getDay().getDayId(), completionDate);
                
                // Kiểm tra completion date có trong hôm nay không
                if (completionDate.isAfter(startOfToday.minusSeconds(1)) && 
                    completionDate.isBefore(endOfToday)) {
                    log.info("Found completion today! Day {}, completion date: {}", 
                            completion.getDay().getDayId(), completionDate);
                    throw new AttendanceException("Bạn đã điểm danh Day " + completion.getDay().getDayId() + 
                                                " trong hôm nay rồi. Mỗi ngày chỉ được điểm danh 1 day duy nhất!");
                }
            }
        }
        
        log.info("No completions found today, proceeding with attendance validation...");

        // Kiểm tra điểm danh liên tiếp
        Integer lastCompletedDay = completionRepository.findLastCompletedDayNumber(customerId, courseId);
        log.info("Last completed day for customer {} in course {}: {}", customerId, courseId, lastCompletedDay);
        
        if (lastCompletedDay == null) {
            // Chưa điểm danh ngày nào, chỉ được điểm danh day 1
            if (dayId != 1) {
                throw new AttendanceException("Bạn phải bắt đầu điểm danh từ ngày 1");
            }
        } else {
            // Đã có điểm danh trước đó, kiểm tra tính liên tiếp
            if (dayId != lastCompletedDay + 1) {
                throw new AttendanceException("Bạn phải điểm danh các ngày liên tiếp (ngày tiếp theo phải là ngày " + (lastCompletedDay + 1) + ")");
            }
        }
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
        
        return completionRepository.save(completion);
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
        Long completedDays = completionRepository.countCompletedDaysByCustomerAndCourse(customerId, courseId);
        Long totalDays = completionRepository.countTotalDaysByCustomerAndCourse(customerId, courseId);
        
        // Nếu chưa có completion records, tạo chúng
        if (totalDays == 0) {
            initializeCompletionRecords(customerId, courseId);
            totalDays = completionRepository.countTotalDaysByCustomerAndCourse(customerId, courseId);
        }
        
        Boolean isCompleted = completionRepository.isCourseCompletedByCustomer(customerId, courseId);
        
        return new CourseProgressDTO(
                customerId,
                courseId,
                completedDays.intValue(),
                totalDays.intValue(),
                isCompleted != null ? isCompleted : false
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
} 