package com.capstone.wellnessnavigatorgym.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service tối ưu hóa truy vấn với cache và batch processing
 * Giải quyết vấn đề hiệu suất khi có nhiều user
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OptimizedQueryService {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Lấy thống kê completion của customer với cache
     * Cache trong 5 phút để tránh query liên tục
     */
    @Cacheable(value = "customerProgress", key = "#customerId + '_' + #courseId")
    public Map<String, Object> getCustomerCourseProgressOptimized(Integer customerId, Integer courseId) {
        log.debug("Fetching optimized progress for customer {} course {}", customerId, courseId);
        
        // Sử dụng index tối ưu: idx_customer_course + idx_completion_status
        String sql = "SELECT " +
                    "COUNT(*) as total_days, " +
                    "SUM(CASE WHEN is_completed = 1 THEN 1 ELSE 0 END) as completed_days, " +
                    "MAX(completion_date) as last_completion " +
                    "FROM customer_day_completion " +
                    "WHERE customer_id = :customerId AND course_id = :courseId";
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("customerId", customerId);
        query.setParameter("courseId", courseId);
        
        Object[] result = (Object[]) query.getSingleResult();
        
        Map<String, Object> progress = new HashMap<>();
        BigInteger totalDays = (BigInteger) result[0];
        BigInteger completedDays = (BigInteger) result[1];
        
        progress.put("totalDays", totalDays.intValue());
        progress.put("completedDays", completedDays.intValue());
        progress.put("progressPercentage", totalDays.intValue() > 0 ? 
            (completedDays.doubleValue() / totalDays.doubleValue() * 100.0) : 0.0);
        progress.put("lastCompletion", result[2]);
        progress.put("isCompleted", totalDays.equals(completedDays) && totalDays.intValue() > 0);
        
        return progress;
    }

    /**
     * Lấy danh sách completion của customer với pagination
     * Tối ưu cho việc hiển thị lịch sử
     */
    public List<Object[]> getCustomerCompletionHistory(Integer customerId, int page, int size) {
        // Sử dụng index: idx_customer_completed_date
        String sql = "SELECT " +
                    "cdc.completion_id, cdc.course_id, c.course_name, " +
                    "cdc.day_id, d.day_name, cdc.completion_date, cdc.is_completed " +
                    "FROM customer_day_completion cdc " +
                    "JOIN course c ON cdc.course_id = c.course_id " +
                    "JOIN days d ON cdc.day_id = d.day_id " +
                    "WHERE cdc.customer_id = :customerId " +
                    "AND cdc.completion_date IS NOT NULL " +
                    "ORDER BY cdc.completion_date DESC " +
                    "LIMIT :limit OFFSET :offset";
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("customerId", customerId);
        query.setParameter("limit", size);
        query.setParameter("offset", page * size);
        
        return query.getResultList();
    }

    /**
     * Thống kê dashboard tối ưu với cache
     * Cache 10 phút cho admin dashboard
     */
    @Cacheable(value = "dashboardStats", key = "'monthly_' + #year + '_' + #month")
    public Map<String, Object> getMonthlyStatsOptimized(int year, int month) {
        Map<String, Object> stats = new HashMap<>();
        
        // 1. Đếm user mới trong tháng - sử dụng index: idx_account_created_date_year_month
        String userCountSql = "SELECT COUNT(*) FROM customer c " +
                             "JOIN account a ON c.account_id = a.account_id " +
                             "WHERE c.is_enable = 1 " +
                             "AND YEAR(a.created_date) = :year " +
                             "AND MONTH(a.created_date) = :month";
        
        Query userQuery = entityManager.createNativeQuery(userCountSql);
        userQuery.setParameter("year", year);
        userQuery.setParameter("month", month);
        BigInteger newUsers = (BigInteger) userQuery.getSingleResult();
        
        // 2. Đếm completion trong tháng - sử dụng index: idx_completion_date_year_month
        String completionSql = "SELECT COUNT(*) FROM customer_day_completion " +
                              "WHERE YEAR(completion_date) = :year " +
                              "AND MONTH(completion_date) = :month " +
                              "AND is_completed = 1";
        
        Query completionQuery = entityManager.createNativeQuery(completionSql);
        completionQuery.setParameter("year", year);
        completionQuery.setParameter("month", month);
        BigInteger completions = (BigInteger) completionQuery.getSingleResult();
        
        // 3. Doanh thu trong tháng - sử dụng index: idx_payment_date_year_month
        String revenueSql = "SELECT COALESCE(SUM(amount), 0) FROM vip_payment " +
                           "WHERE YEAR(created_date) = :year " +
                           "AND MONTH(created_date) = :month " +
                           "AND payment_status = 'SUCCESS'";
        
        Query revenueQuery = entityManager.createNativeQuery(revenueSql);
        revenueQuery.setParameter("year", year);
        revenueQuery.setParameter("month", month);
        Object revenue = revenueQuery.getSingleResult();
        
        stats.put("newUsers", newUsers.longValue());
        stats.put("completions", completions.longValue());
        stats.put("revenue", revenue);
        stats.put("year", year);
        stats.put("month", month);
        
        return stats;
    }

    /**
     * Batch update completion status - tối ưu cho multiple updates
     */
    @Transactional
    @CacheEvict(value = "customerProgress", allEntries = true)
    public int batchUpdateCompletionStatus(List<Integer> completionIds, boolean isCompleted) {
        if (completionIds.isEmpty()) {
            return 0;
        }
        
        // Batch update thay vì update từng record
        String sql = "UPDATE customer_day_completion SET " +
                    "is_completed = :isCompleted, " +
                    "completion_date = CASE WHEN :isCompleted = 1 THEN NOW() ELSE NULL END, " +
                    "updated_at = NOW() " +
                    "WHERE completion_id IN (:ids)";
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("isCompleted", isCompleted ? 1 : 0);
        query.setParameter("ids", completionIds);
        
        int updatedCount = query.executeUpdate();
        log.info("Batch updated {} completion records", updatedCount);
        
        return updatedCount;
    }

    /**
     * Kiểm tra điểm danh hôm nay tối ưu
     * Sử dụng index: idx_completion_date_customer
     */
    @Cacheable(value = "todayAttendance", key = "#customerId + '_' + T(java.time.LocalDate).now()")
    public boolean hasAttendanceToday(Integer customerId) {
        String sql = "SELECT COUNT(*) FROM customer_day_completion " +
                    "WHERE customer_id = :customerId " +
                    "AND DATE(completion_date) = CURDATE() " +
                    "AND is_completed = 1";
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("customerId", customerId);
        
        BigInteger count = (BigInteger) query.getSingleResult();
        return count.intValue() > 0;
    }

    /**
     * Top performers của tháng
     * Cache 1 giờ vì ít thay đổi
     */
    @Cacheable(value = "topPerformers", key = "#year + '_' + #month")
    public List<Object[]> getTopPerformersOfMonth(int year, int month, int limit) {
        // Sử dụng index composite để tối ưu
        String sql = "SELECT " +
                    "c.customer_id, c.customer_name, " +
                    "COUNT(cdc.completion_id) as total_completions, " +
                    "SUM(CASE WHEN cdc.is_completed = 1 THEN 1 ELSE 0 END) as successful_completions " +
                    "FROM customer c " +
                    "JOIN customer_day_completion cdc ON c.customer_id = cdc.customer_id " +
                    "WHERE c.is_enable = 1 " +
                    "AND YEAR(cdc.completion_date) = :year " +
                    "AND MONTH(cdc.completion_date) = :month " +
                    "GROUP BY c.customer_id, c.customer_name " +
                    "ORDER BY successful_completions DESC " +
                    "LIMIT :limit";
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("year", year);
        query.setParameter("month", month);
        query.setParameter("limit", limit);
        
        return query.getResultList();
    }

    /**
     * Thống kê course popularity
     */
    @Cacheable(value = "courseStats", key = "'popularity'")
    public List<Object[]> getCoursePopularityStats() {
        // Sử dụng view đã tối ưu
        String sql = "SELECT " +
                    "course_id, course_name, enrolled_customers, " +
                    "active_customers, total_completions, total_revenue " +
                    "FROM course_stats_summary " +
                    "ORDER BY active_customers DESC, total_completions DESC " +
                    "LIMIT 20";
        
        Query query = entityManager.createNativeQuery(sql);
        return query.getResultList();
    }

    /**
     * Clear cache khi cần
     */
    @CacheEvict(value = {"customerProgress", "dashboardStats", "todayAttendance", "topPerformers", "courseStats"}, allEntries = true)
    public void clearAllCaches() {
        log.info("Cleared all optimization caches");
    }

    /**
     * Clear cache cho specific customer
     */
    @CacheEvict(value = {"customerProgress", "todayAttendance"}, key = "#customerId")
    public void clearCustomerCache(Integer customerId) {
        log.debug("Cleared cache for customer {}", customerId);
    }

    /**
     * Thống kê database performance
     */
    public Map<String, Object> getDatabaseStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 1. Table sizes
        String tableSizeSql = "SELECT table_name, " +
                             "ROUND(((data_length + index_length) / 1024 / 1024), 2) as size_mb, " +
                             "table_rows " +
                             "FROM information_schema.tables " +
                             "WHERE table_schema = DATABASE() " +
                             "ORDER BY (data_length + index_length) DESC " +
                             "LIMIT 10";
        
        Query tableSizeQuery = entityManager.createNativeQuery(tableSizeSql);
        List<Object[]> tableSizes = tableSizeQuery.getResultList();
        stats.put("tableSizes", tableSizes);
        
        // 2. Index cardinality
        String indexSql = "SELECT table_name, index_name, cardinality " +
                         "FROM information_schema.statistics " +
                         "WHERE table_schema = DATABASE() " +
                         "AND index_name != 'PRIMARY' " +
                         "ORDER BY cardinality DESC " +
                         "LIMIT 20";
        
        Query indexQuery = entityManager.createNativeQuery(indexSql);
        List<Object[]> indexStats = indexQuery.getResultList();
        stats.put("indexStats", indexStats);
        
        return stats;
    }

    /**
     * Cleanup old null completion records
     */
    @Transactional
    public int cleanupOldNullCompletions(int daysOld) {
        String sql = "DELETE FROM customer_day_completion " +
                    "WHERE completion_date IS NULL " +
                    "AND created_at < DATE_SUB(NOW(), INTERVAL :days DAY)";
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("days", daysOld);
        
        int deletedCount = query.executeUpdate();
        log.info("Cleaned up {} old null completion records", deletedCount);
        
        return deletedCount;
    }
} 