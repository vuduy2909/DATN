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
        
        // Tạo dữ liệu cho biểu đồ tiến độ (phân chia theo 3 giai đoạn)
        List<Map<String, Object>> data = new ArrayList<>();
        
        // Nhóm theo khoảng ngày
        Map<String, Double> progressByRange = new HashMap<>();
        progressByRange.put("Day 1-10", 0.0);
        progressByRange.put("Day 10-20", 0.0);
        progressByRange.put("Day 20-30", 0.0);
        
        // Lấy danh sách khóa học của người dùng
        List<CustomerCourse> customerCourses = customerCourseRepository.findByCustomer_CustomerId(customerId);
        
        Map<String, Integer> totalDaysByRange = new HashMap<>();
        totalDaysByRange.put("Day 1-10", 0);
        totalDaysByRange.put("Day 10-20", 0);
        totalDaysByRange.put("Day 20-30", 0);
        
        Map<String, Integer> completedDaysByRange = new HashMap<>();
        completedDaysByRange.put("Day 1-10", 0);
        completedDaysByRange.put("Day 10-20", 0);
        completedDaysByRange.put("Day 20-30", 0);
        
        for (CustomerCourse customerCourse : customerCourses) {
            Integer courseId = customerCourse.getCourse().getCourseId();
            List<CustomerDayCompletion> completions = completionService.getCustomerCourseCompletions(customerId, courseId);
            
            for (CustomerDayCompletion completion : completions) {
                // Dùng id để xác định ngày (ví dụ: day có id = 1 tương ứng với ngày 1)
                int dayNumber = completion.getDay().getDayId();
                String range = getRangeForDay(dayNumber);
                
                totalDaysByRange.put(range, totalDaysByRange.get(range) + 1);
                
                if (completion.getIsCompleted()) {
                    completedDaysByRange.put(range, completedDaysByRange.get(range) + 1);
                }
            }
        }
        
        // Tính % hoàn thành cho mỗi khoảng
        for (String range : progressByRange.keySet()) {
            double completionPercentage = 0.0;
            
            if (totalDaysByRange.get(range) > 0) {
                completionPercentage = (completedDaysByRange.get(range) * 100.0) / totalDaysByRange.get(range);
            } else {
                // Nếu không có dữ liệu, gán giá trị mặc định
                if (range.equals("Day 1-10")) {
                    completionPercentage = 40.0;
                } else if (range.equals("Day 10-20")) {
                    completionPercentage = 80.0;
                } else {
                    completionPercentage = 60.0;
                }
            }
            
            progressByRange.put(range, completionPercentage);
            
            Map<String, Object> item = new HashMap<>();
            item.put("label", range);
            item.put("value", completionPercentage);
            
            data.add(item);
        }
        
        chartData.put("data", data);
        
        return chartData;
    }

    private String getRangeForDay(int day) {
        if (day <= 10) {
            return "Day 1-10";
        } else if (day <= 20) {
            return "Day 10-20";
        } else {
            return "Day 20-30";
        }
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
     * Phương thức phụ trợ để lấy số khóa học đang theo
     */
    private Integer getCoursesInProgressCount(Integer customerId) {
        // Lấy danh sách khóa học của người dùng
        List<CustomerCourse> customerCourses = customerCourseRepository.findByCustomer_CustomerId(customerId);
        if (customerCourses.isEmpty()) {
            return 0;
        }
        
        // Đếm số khóa học đang tiến hành (chưa hoàn thành)
        int inProgressCount = 0;
        
        for (CustomerCourse customerCourse : customerCourses) {
            Integer courseId = customerCourse.getCourse().getCourseId();
            // Kiểm tra xem khóa học đã hoàn thành chưa
            CustomerDayCompletionService.CourseProgressDTO progress = 
                    completionService.getCourseProgress(customerId, courseId);
            
            if (!progress.getIsCompleted()) {
                inProgressCount++;
            }
        }
        
        return inProgressCount;
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