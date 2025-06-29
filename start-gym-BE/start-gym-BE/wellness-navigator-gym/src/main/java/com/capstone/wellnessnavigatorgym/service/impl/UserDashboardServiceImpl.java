package com.capstone.wellnessnavigatorgym.service.impl;

import com.capstone.wellnessnavigatorgym.dto.UserDashboardStatsDTO;
import com.capstone.wellnessnavigatorgym.entity.Customer;
import com.capstone.wellnessnavigatorgym.entity.CustomerCourse;
import com.capstone.wellnessnavigatorgym.entity.CustomerDayCompletion;
import com.capstone.wellnessnavigatorgym.entity.UserDataAi;
import com.capstone.wellnessnavigatorgym.repository.ICustomerCourseRepository;
import com.capstone.wellnessnavigatorgym.repository.ICustomerRepository;
import com.capstone.wellnessnavigatorgym.repository.UserDataAiRepository;
import com.capstone.wellnessnavigatorgym.service.CustomerDayCompletionService;
import com.capstone.wellnessnavigatorgym.service.UserDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDashboardServiceImpl implements UserDashboardService {

    private final ICustomerRepository customerRepository;
    private final CustomerDayCompletionService completionService;
    private final UserDataAiRepository userDataAiRepository;
    private final ICustomerCourseRepository customerCourseRepository;
    
    @javax.persistence.PersistenceUnit
    private javax.persistence.EntityManagerFactory entityManagerFactory;

    @Override
    public UserDashboardStatsDTO getUserDashboardStats(Integer customerId) {
        log.info("Getting dashboard stats for customer ID: {}", customerId);
        
        // Lấy thông tin từ các API riêng lẻ
        Double weeklyActivity = getWeeklyActivityPercentage(customerId);
        String workedTime = getWorkedTimeThisWeek(customerId);
        Integer coursesInProgress = getCoursesInProgressCount(customerId);
        Double bmi = getBmiValue(customerId);
        
        // Lấy dữ liệu biểu đồ
        Map<String, Object> progressChartData = getProgressChartData(customerId);
        Map<String, Object> activityChartData = getActivityChartData(customerId);
        
        // Tạo đối tượng DTO
        return UserDashboardStatsDTO.builder()
                .weeklyActivity(weeklyActivity)
                .workedTimeThisWeek(workedTime)
                .coursesInProgress(coursesInProgress)
                .bmi(bmi)
                .progressChartData(progressChartData)
                .activityChartData(activityChartData)
                .build();
    }

    @Override
    public Double getWeeklyActivityPercentage(Integer customerId) {
        // Tính % hoạt động trong tuần dựa trên số ngày hoàn thành / tổng số ngày
        LocalDate startOfWeek = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        
        // Lấy danh sách khóa học của người dùng
        List<CustomerCourse> customerCourses = customerCourseRepository.findByCustomer_CustomerId(customerId);
        if (customerCourses.isEmpty()) {
            return 35.0; // Giá trị mặc định nếu không có khóa học
        }
        
        // Tính số ngày đã hoàn thành trong tuần
        int totalCompletions = 0;
        int completedCompletions = 0;
        
        for (CustomerCourse customerCourse : customerCourses) {
            Integer courseId = customerCourse.getCourse().getCourseId();
            List<CustomerDayCompletion> completions = completionService.getCustomerCourseCompletions(customerId, courseId);
            
            for (CustomerDayCompletion completion : completions) {
                if (completion.getCompletionDate() != null && 
                    !completion.getCompletionDate().toLocalDate().isBefore(startOfWeek) && 
                    !completion.getCompletionDate().toLocalDate().isAfter(endOfWeek)) {
                    totalCompletions++;
                    if (completion.getIsCompleted()) {
                        completedCompletions++;
                    }
                }
            }
        }
        
        // Tính % hoạt động (mặc định là 35% nếu không có dữ liệu)
        return totalCompletions > 0 ? 
                (completedCompletions * 100.0 / totalCompletions) : 35.0;
    }

    @Override
    public String getWorkedTimeThisWeek(Integer customerId) {
        // Giả lập thời gian luyện tập trong tuần
        // Thực tế cần tính từ dữ liệu thực
        LocalDate startOfWeek = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        
        // Lấy danh sách khóa học của người dùng
        List<CustomerCourse> customerCourses = customerCourseRepository.findByCustomer_CustomerId(customerId);
        
        // Tính số ngày đã hoàn thành trong tuần
        int completedDays = 0;
        
        for (CustomerCourse customerCourse : customerCourses) {
            Integer courseId = customerCourse.getCourse().getCourseId();
            List<CustomerDayCompletion> completions = completionService.getCustomerCourseCompletions(customerId, courseId);
            
            for (CustomerDayCompletion completion : completions) {
                if (completion.getIsCompleted() && completion.getCompletionDate() != null && 
                    !completion.getCompletionDate().toLocalDate().isBefore(startOfWeek) && 
                    !completion.getCompletionDate().toLocalDate().isAfter(endOfWeek)) {
                    completedDays++;
                }
            }
        }
        
        // Mỗi buổi tập tính trung bình 45 phút
        long totalMinutes = completedDays * 45;
        
        // Nếu không có dữ liệu, trả về giá trị mặc định
        if (totalMinutes == 0) {
            return "40:00:05";
        }
        
        // Chuyển đổi thành giờ:phút:giây
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        
        return String.format("%02d:%02d:00", hours, minutes);
    }

    @Override
    public Map<String, Object> getProgressChartData(Integer customerId) {
        Map<String, Object> chartData = new HashMap<>();
        
        // Tạo dữ liệu cho biểu đồ tiến độ theo từng day (cải thiện để dễ quan sát)
        List<Map<String, Object>> data = new ArrayList<>();
        
        // Lấy danh sách khóa học của người dùng
        List<CustomerCourse> customerCourses = customerCourseRepository.findByCustomer_CustomerId(customerId);
        
        // Map lưu trữ tiến độ hoàn thành theo từng day
        Map<Integer, Boolean> dayCompletionMap = new HashMap<>();
        Set<Integer> allDaysInCourses = new HashSet<>();
        
        // Thu thập tất cả dữ liệu completion từ các khóa học
        for (CustomerCourse customerCourse : customerCourses) {
            Integer courseId = customerCourse.getCourse().getCourseId();
            List<CustomerDayCompletion> completions = completionService.getCustomerCourseCompletions(customerId, courseId);
            
            for (CustomerDayCompletion completion : completions) {
                Integer dayNumber = completion.getDay().getDayId(); // Sử dụng dayId để đại diện cho số thứ tự ngày
                allDaysInCourses.add(dayNumber);
                
                // Chỉ ghi đè nếu day đã completed hoặc chưa có trong map
                if (completion.getIsCompleted() || !dayCompletionMap.containsKey(dayNumber)) {
                    dayCompletionMap.put(dayNumber, completion.getIsCompleted());
                }
            }
        }
        
        // Nếu không có dữ liệu thực tế, tạo dữ liệu mẫu cho 30 days
        if (allDaysInCourses.isEmpty()) {
            log.info("No actual progress data for customer {}, creating sample data", customerId);
            for (int day = 1; day <= 30; day++) {
                allDaysInCourses.add(day);
                // Tạo pattern tiến độ realistic: bắt đầu tốt, giảm dần, rồi tăng lại
                boolean isCompleted = false;
                if (day <= 7) isCompleted = day % 2 == 1; // 50% completion đầu tuần
                else if (day <= 14) isCompleted = day % 3 == 1; // 33% completion tuần 2
                else if (day <= 21) isCompleted = day % 4 == 1; // 25% completion tuần 3
                else isCompleted = day % 2 == 1; // 50% completion tuần cuối
                
                dayCompletionMap.put(day, isCompleted);
            }
        }
        
        // Sắp xếp days và tạo dữ liệu biểu đồ
        List<Integer> sortedDays = new ArrayList<>(allDaysInCourses);
        Collections.sort(sortedDays);
        
        // Chỉ lấy tối đa 30 days gần nhất để biểu đồ không quá dày
        int maxDays = Math.min(30, sortedDays.size());
        List<Integer> displayDays = sortedDays.subList(Math.max(0, sortedDays.size() - maxDays), sortedDays.size());
        
        for (Integer dayNumber : displayDays) {
            Boolean isCompleted = dayCompletionMap.get(dayNumber);
            
            Map<String, Object> item = new HashMap<>();
            item.put("label", "Day " + dayNumber);
            item.put("day", dayNumber);
            item.put("value", isCompleted ? 100.0 : 0.0); // 100% nếu completed, 0% nếu chưa
            item.put("isCompleted", isCompleted);
            
            data.add(item);
        }
        
        // Tính tổng statistics
        long totalDays = data.size();
        long completedDays = data.stream().mapToLong(item -> 
            (Boolean) item.get("isCompleted") ? 1 : 0).sum();
        double overallProgress = totalDays > 0 ? (completedDays * 100.0) / totalDays : 0.0;
        
        chartData.put("data", data);
        chartData.put("totalDays", totalDays);
        chartData.put("completedDays", completedDays);
        chartData.put("overallProgress", Math.round(overallProgress * 100.0) / 100.0);
        chartData.put("chartType", "day-by-day"); // Indicator cho frontend biết đây là biểu đồ theo day
        
        log.info("Generated progress chart for customer {}: {}/{} days completed ({}%)", 
            customerId, completedDays, totalDays, Math.round(overallProgress));
        
        return chartData;
    }

    @Override
    public Map<String, Object> getActivityChartData(Integer customerId) {
        Map<String, Object> chartData = new HashMap<>();
        
        // Tạo dữ liệu cho biểu đồ hoạt động (6 mốc thời gian)
        List<Map<String, Object>> data = new ArrayList<>();
        
        // Các mốc thời gian cố định
        String[] timePoints = {"Day 1", "Day 10", "Day 15", "Day 20", "Day 25", "Day 30"};
        
        // Giá trị mặc định nếu không có dữ liệu
        double[] defaultValues = {50.0, 60.0, 80.0, 70.0, 70.0, 90.0};
        
        // Lấy danh sách khóa học của người dùng
        List<CustomerCourse> customerCourses = customerCourseRepository.findByCustomer_CustomerId(customerId);
        
        for (int i = 0; i < timePoints.length; i++) {
            String timePoint = timePoints[i];
            int dayNumber = extractDayNumber(timePoint);
            
            // Tìm completion tương ứng với day number
            CustomerDayCompletion matchingCompletion = null;
            
            for (CustomerCourse customerCourse : customerCourses) {
                List<CustomerDayCompletion> completions = completionService.getCustomerCourseCompletions(
                    customerId, customerCourse.getCourse().getCourseId());
                
                for (CustomerDayCompletion completion : completions) {
                    // Dùng id để xác định ngày
                    if (completion.getDay().getDayId() == dayNumber) {
                        matchingCompletion = completion;
                        break;
                    }
                }
                
                if (matchingCompletion != null) {
                    break;
                }
            }
            
            // Tính giá trị hoạt động (mặc định nếu không có dữ liệu)
            double activityValue = defaultValues[i];
            
            // Nếu có dữ liệu thì cập nhật giá trị
            if (matchingCompletion != null && matchingCompletion.getIsCompleted() && 
                matchingCompletion.getCompletionDate() != null && matchingCompletion.getCreatedAt() != null) {
                
                // Tính toán giá trị thực tế dựa trên thời gian hoàn thành
                long minutesTaken = ChronoUnit.MINUTES.between(
                        matchingCompletion.getCreatedAt(), 
                        matchingCompletion.getCompletionDate()
                );
                
                // Điều chỉnh giá trị dựa trên thời gian hoàn thành
                // Giả sử thời gian trung bình là 45 phút
                if (minutesTaken < 30) {
                    activityValue = 90.0; // Hoàn thành nhanh
                } else if (minutesTaken < 45) {
                    activityValue = 75.0; // Hoàn thành đúng hạn
                } else {
                    activityValue = 60.0; // Hoàn thành chậm
                }
            }
            
            Map<String, Object> item = new HashMap<>();
            item.put("day", timePoint);
            item.put("activity", activityValue);
            
            data.add(item);
        }
        
        chartData.put("data", data);
        
        return chartData;
    }
    
    /**
     * Phương thức phụ trợ để lấy số ngày từ chuỗi (vd: "Day 10" -> 10)
     */
    private int extractDayNumber(String dayString) {
        try {
            return Integer.parseInt(dayString.replace("Day ", ""));
        } catch (NumberFormatException e) {
            return 1; // Giá trị mặc định
        }
    }
    
    /**
     * Lấy số khóa học đang theo
     */
    @Override
    public Integer getCoursesInProgressCount(Integer customerId) {
        // Lấy danh sách khóa học của người dùng với status ACTIVE
        List<CustomerCourse> customerCourses = customerCourseRepository.findByCustomer_CustomerId(customerId);
        log.info("=== DEBUG Course In Progress Count ===");
        log.info("Customer ID: {}", customerId);
        log.info("Total courses found: {}", customerCourses.size());
        
        // Debug: Log raw database query result
        log.info("=== Debug Database Direct Query ===");
        try {
            javax.persistence.EntityManager em = entityManagerFactory.createEntityManager();
            javax.persistence.Query rawQuery = em.createNativeQuery(
                "SELECT cc.customer_course_id, cc.customer_id, cc.course_id, cc.recommended_status, c.course_name " +
                "FROM customer_course cc LEFT JOIN course c ON cc.course_id = c.course_id " +
                "WHERE cc.customer_id = ?");
            rawQuery.setParameter(1, customerId);
            
            @SuppressWarnings("unchecked")
            java.util.List<Object[]> rawResults = rawQuery.getResultList();
            log.info("Raw database query found {} records for customer {}", rawResults.size(), customerId);
            
            for (Object[] row : rawResults) {
                log.info("Raw record: customer_course_id={}, customer_id={}, course_id={}, recommended_status={}, course_name={}", 
                    row[0], row[1], row[2], row[3], row[4]);
            }
            em.close();
        } catch (Exception e) {
            log.error("Error in raw database query: {}", e.getMessage());
        }
        
        if (customerCourses.isEmpty()) {
            log.info("No courses found for customer {}", customerId);
            return 0;
        }
        
        // Log chi tiết từng khóa học
        for (int i = 0; i < customerCourses.size(); i++) {
            CustomerCourse cc = customerCourses.get(i);
            log.info("Course {}: ID={}, Name={}, RecommendedStatus={}", 
                (i + 1), 
                cc.getCourse() != null ? cc.getCourse().getCourseId() : "NULL_COURSE",
                cc.getCourse() != null ? cc.getCourse().getCourseName() : "NULL_COURSE_NAME",
                cc.getRecommendedStatus());
        }
        
        int coursesInProgressCount = 0;
        
        for (CustomerCourse customerCourse : customerCourses) {
            if (customerCourse.getCourse() == null) {
                log.warn("CustomerCourse {} has null course - skipping", customerCourse.getCustomerCourseId());
                continue;
            }
            
            Integer courseId = customerCourse.getCourse().getCourseId();
            String courseName = customerCourse.getCourse().getCourseName();
            
            // Kiểm tra xem khóa học đã hoàn thành chưa
            CustomerDayCompletionService.CourseProgressDTO progress = completionService.getCourseProgress(customerId, courseId);
            log.info("Course {}: {}/{} days completed, isCompleted={}", 
                courseName, progress.getCompletedDays(), progress.getTotalDays(), progress.getIsCompleted());
            
            if (!progress.getIsCompleted()) {
                coursesInProgressCount++;
                log.info("✓ Course {} counted as IN PROGRESS", courseName);
            } else {
                log.info("✗ Course {} is COMPLETED - not counted", courseName);
            }
        }
        
        log.info("=== Final Result: Customer {} has {} courses in progress ===", customerId, coursesInProgressCount);
        return coursesInProgressCount;
    }
    
    /**
     * Phương thức phụ trợ để lấy chỉ số BMI
     */
    private Double getBmiValue(Integer customerId) {
        // Lấy dữ liệu BMI từ bảng UserDataAi
        Optional<UserDataAi> userData = userDataAiRepository
                .findTopByCustomer_CustomerIdOrderByCreatedDateDesc(customerId);
        
        if (userData.isPresent() && userData.get().getBmi() != null) {
            return userData.get().getBmi();
        }
        
        // Giá trị mặc định nếu không có dữ liệu
        return 0.0;
    }
} 