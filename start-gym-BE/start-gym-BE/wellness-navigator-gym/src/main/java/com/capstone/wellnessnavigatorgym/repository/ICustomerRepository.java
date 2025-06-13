package com.capstone.wellnessnavigatorgym.repository;

import com.capstone.wellnessnavigatorgym.entity.Account;
import com.capstone.wellnessnavigatorgym.entity.Cart;
import com.capstone.wellnessnavigatorgym.entity.Customer;
import com.capstone.wellnessnavigatorgym.entity.CustomerType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Tuple;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

@Repository
@Transactional
public interface ICustomerRepository extends JpaRepository<Customer, Integer> {

        @Query(value = "select c.customer_id, c.customer_address, c.customer_code, c.customer_email, c.customer_gender, " +            "c.customer_img, c.customer_name, c.customer_phone, c.date_of_birth, c.is_enable, " +            "ct.customer_type_id, a.account_id " +            "from customer c " +            "inner join customer_type ct on c.customer_type_id = ct.customer_type_id " +            "inner join account a on c.account_id = a.account_id " +            "where c.is_enable = true " +            "order by c.customer_code ",
            countQuery = "select count(c.customer_id) " +
                    "from customer c " +
                    "inner join customer_type ct on c.customer_type_id = ct.customer_type_id " +
                    "inner join account a on c.account_id = a.account_id where c.is_enable = true order by c.customer_code ",
            nativeQuery = true)
    Page<Customer> findAllCustomers(Pageable pageable);


    @Query(value = "select c.customer_id, c.customer_address, c.customer_code, c.customer_email, c.customer_gender, " +
            "c.customer_img, c.customer_name, c.customer_phone, c.date_of_birth, c.is_enable, " +
            "ct.customer_type_id, ct.customer_type_name, ct.price, a.account_id " +
            "from customer c " +
            "inner join customer_type ct on c.customer_type_id = ct.customer_type_id " +
            "inner join account a on c.account_id = a.account_id " +
            "where (ct.customer_type_name like concat('%', :type, '%') and c.customer_name like concat('%', :name, '%') " +
            "and c.customer_address like concat('%', :address, '%')  and c.customer_phone like concat('%', :phone, '%')) " +
            "and (c.is_enable = true) order by c.customer_code ",
            countQuery = "select count(c.customer_id) " +
                    "from customer c " +
                    "inner join customer_type ct on c.customer_type_id = ct.customer_type_id " +
                    "inner join account a on c.account_id = a.account_id " +
                    "where (ct.customer_type_name like concat('%', :type, '%') and c.customer_name like concat('%', :name, '%') " +
                    "and c.customer_address like concat('%', :address, '%')  and c.customer_phone like concat('%', :phone, '%')) " +
                    "and (c.is_enable = true) order by c.customer_code",
            nativeQuery = true)
    Page<Customer> searchCustomer(@Param("type") String type, @Param("name") String name,
                                  @Param("address") String address , @Param("phone") String phone , Pageable pageable);

    @Query(value = "select * from customer order by `customer_code` desc limit 1", nativeQuery = true)
    Customer limitCustomer();

    @Modifying
    @Query(value = "INSERT INTO customer (`customer_code`, `customer_name`, `customer_email`, `customer_phone`, " +
            "`customer_gender`, `date_of_birth`, `customer_address`, `customer_img`, `is_enable`, " +
            "`customer_type_id`) " +
            "VALUES (:customer_code, :customer_name, :customer_email, :customer_phone, :customer_gender, " +
            ":date_of_birth, :customer_address, :customer_img, :is_enable, :customer_type_id)", nativeQuery = true)
    void insertCustomer(@Param("customer_code") String customer_code,
                        @Param("customer_name") String customer_name,
                        @Param("customer_email") String customer_email,
                        @Param("customer_phone") String customer_phone,
                        @Param("customer_gender") String customer_gender,
                        @Param("date_of_birth") Date date_of_birth,
                        @Param("customer_address") String customer_address,
                        @Param("customer_img") String customer_img,
                        @Param("is_enable") Boolean is_enable,
                        @Param("customer_type_id") CustomerType customer_type_id);

    @Modifying
    @Query(value = "UPDATE customer SET `customer_code`=:customer_code, `customer_name`=:customer_name, " +
            "`customer_email`=:customer_email, `customer_phone`=:customer_phone, `customer_gender`=:customer_gender, " +
            "`date_of_birth`=:date_of_birth, `customer_address`=:customer_address, " +
            "`customer_img`=:customer_img, `is_enable`=:is_enable, `customer_type_id`=:customer_type_id, " +
            "`account_id`=:account_id WHERE `customer_id`=:customer_id", nativeQuery = true)
    void updateCustomer(@Param("customer_id") Integer customer_id,
                        @Param("customer_code") String customer_code,
                        @Param("customer_name") String customer_name,
                        @Param("customer_email") String customer_email,
                        @Param("customer_phone") String customer_phone,
                        @Param("customer_gender") String customer_gender,
                        @Param("date_of_birth") Date date_of_birth,
                        @Param("customer_address") String customer_address,
                        @Param("customer_img") String customer_img,
                        @Param("is_enable") Boolean is_enable,
                        @Param("customer_type_id") CustomerType customer_type_id,
                        @Param("account_id") Account account_id);

    @Modifying
    @Query(value = "update customer set is_enable = false where customer_id = :id", nativeQuery = true)
    void deleteCustomerId(@Param("id") Integer id);

    @Query(value = "SELECT c.customer_id, c.customer_code, c.customer_name, c.customer_phone, c.customer_gender, " +
            "c.date_of_birth, c.customer_address, c.customer_img, c.customer_email, ct.customer_type_name, a.user_name, " +
            "a.email, u.user_data_id, u.age, u.bmi, u.gender, u.training_goals, u.training_history, " +
            "cc.recommended_status, co.course_id, co.course_name, co.description, co.duration, co.image, co.status, " +
            "cty.course_type_name "+
            "FROM customer c " +
            "INNER JOIN customer_type ct ON c.customer_type_id = ct.customer_type_id " +
            "INNER JOIN account a ON c.account_id = a.account_id " +
            "LEFT JOIN user_data_ai u ON c.customer_id = u.customer_id " +
            "LEFT JOIN customer_course cc ON c.customer_id = cc.customer_id " +
            "LEFT JOIN course co ON cc.course_id = co.course_id " +
            "LEFT JOIN course_type cty ON co.course_type_id = cty.course_type_id " +
            "WHERE (c.is_enable = true) AND (a.is_enable = true) AND (a.user_name = :username)", nativeQuery = true)
    List<Tuple> findUserDetailByUsername(@Param("username") String username);
    
    @Query(value = "SELECT c.* FROM customer c " +
            "JOIN account a ON c.account_id = a.account_id " +
            "WHERE a.user_name = :username", nativeQuery = true)
    Customer findByAccountUserName(@Param("username") String username);

    @Query(value = "SELECT c.* FROM customer c " +
            "JOIN account a ON c.account_id = a.account_id " +
            "WHERE a.user_name = :username AND c.is_enable = true", nativeQuery = true)
    Customer findActiveCustomerByAccountUserName(@Param("username") String username);

    @Query(value = "SELECT c.* FROM customer c " +
            "WHERE c.customer_email = :email AND c.is_enable = true", nativeQuery = true)
    Customer findByCustomerEmail(@Param("email") String email);

    @Query(value = "SELECT c.* FROM customer c " +
            "WHERE c.customer_phone = :phone", nativeQuery = true)
    Customer findByCustomerPhone(@Param("phone") String phone);

    @Query(value = "SELECT a.customer_id, a.customer_name, a.customer_phone, a.customer_gender, a.customer_date_of_birth, " +
            "a.customer_address, a.is_delete, a.is_enable, u.user_id, a.create_time, " +
            "a.email, u.user_data_id, u.age, u.bmi, u.gender, u.training_goals, u.training_history, " +
            "u.training_frequency, " +
            "u.occupation, u.work_environment " +
            "FROM customer a LEFT JOIN user_data_ai u " +
            "ON a.customer_id = u.customer_id " +
            "ORDER BY a.create_time DESC", nativeQuery = true)
    List<Tuple> getAllCustomerInfo();

    // Dashboard-specific queries
    @Query(value = "SELECT COUNT(*) FROM customer c " +
            "JOIN account a ON c.account_id = a.account_id " +
            "WHERE c.is_enable = true AND a.is_enable = true", nativeQuery = true)
    Long countAllActiveCustomers();

    @Query(value = "SELECT COUNT(*) FROM customer c " +
            "WHERE c.is_enable = true AND c.customer_type_id = :customerTypeId", nativeQuery = true)
    Long countCustomersByType(@Param("customerTypeId") Integer customerTypeId);

    @Query(value = "SELECT COUNT(*) FROM customer c " +
            "JOIN account a ON c.account_id = a.account_id " +
            "WHERE c.is_enable = true AND a.is_enable = true " +
            "AND DATE(a.create_date) = CURDATE()", nativeQuery = true)
    Long countCustomersRegisteredToday();

    @Query(value = "SELECT COUNT(*) FROM customer c " +
            "JOIN account a ON c.account_id = a.account_id " +
            "WHERE c.is_enable = true AND a.is_enable = true " +
            "AND YEARWEEK(a.create_date, 1) = YEARWEEK(CURDATE(), 1)", nativeQuery = true)
    Long countCustomersRegisteredThisWeek();

    @Query(value = "SELECT COUNT(*) FROM customer c " +
            "JOIN account a ON c.account_id = a.account_id " +
            "WHERE c.is_enable = true AND a.is_enable = true " +
            "AND YEAR(a.create_date) = YEAR(CURDATE()) " +
            "AND MONTH(a.create_date) = MONTH(CURDATE())", nativeQuery = true)
    Long countCustomersRegisteredThisMonth();

    // Queries for total_vip_spent functionality
    @Query(value = "SELECT c.customer_id, c.customer_name, c.customer_email, c.customer_phone, " +
            "c.total_vip_spent, ct.customer_type_name " +
            "FROM customer c " +
            "LEFT JOIN customer_type ct ON c.customer_type_id = ct.customer_type_id " +
            "WHERE c.is_enable = true " +
            "ORDER BY c.total_vip_spent DESC " +
            "LIMIT :limit", nativeQuery = true)
    List<Tuple> getTopSpendingCustomers(@Param("limit") int limit);
    
    @Query(value = "SELECT COALESCE(SUM(c.total_vip_spent), 0) FROM customer c " +
            "WHERE c.is_enable = true", nativeQuery = true)
    BigDecimal getTotalVipSpendingAllCustomers();
    
    @Query(value = "SELECT COUNT(*) FROM customer c " +
            "WHERE c.is_enable = true AND c.total_vip_spent > 0", nativeQuery = true)
    Long countCustomersWithVipSpending();
    
    @Query(value = "SELECT AVG(c.total_vip_spent) FROM customer c " +
            "WHERE c.is_enable = true AND c.total_vip_spent > 0", nativeQuery = true)
    Double getAverageVipSpending();
    
    @Query(value = "SELECT ct.customer_type_name, COUNT(c.customer_id) as customer_count, " +
            "SUM(c.total_vip_spent) as total_spending, AVG(c.total_vip_spent) as average_spending " +
            "FROM customer c " +
            "LEFT JOIN customer_type ct ON c.customer_type_id = ct.customer_type_id " +
            "WHERE c.is_enable = true " +
            "GROUP BY ct.customer_type_id, ct.customer_type_name " +
            "ORDER BY total_spending DESC", nativeQuery = true)
    List<Tuple> getSpendingStatsByCustomerType();
    
    @Query("SELECT c FROM Customer c WHERE c.isEnable = true AND c.totalVipSpent >= :minSpending " +
           "ORDER BY c.totalVipSpent DESC")
    List<Customer> findCustomersByMinSpending(@Param("minSpending") BigDecimal minSpending);
    
    @Query("SELECT c FROM Customer c WHERE c.isEnable = true AND c.customerType.customerTypeId = :typeId " +
           "AND c.totalVipSpent <> (SELECT COALESCE(SUM(vp.amount), 0) FROM VipPayment vp " +
           "WHERE vp.customer.customerId = c.customerId AND vp.paymentStatus = 'SUCCESS')")
    List<Customer> findCustomersWithIncorrectTotalSpent(@Param("typeId") Integer typeId);
    
    // Query to find customers who need type update based on their spending
    @Query(value = "SELECT c.customer_id, c.customer_name, c.total_vip_spent, " +
            "c.customer_type_id as current_type_id, ct.customer_type_name as current_type_name, " +
            "(SELECT ct2.customer_type_id FROM customer_type ct2 " +
            " WHERE c.total_vip_spent >= ct2.price " +
            " ORDER BY ct2.price DESC LIMIT 1) as suggested_type_id " +
            "FROM customer c " +
            "LEFT JOIN customer_type ct ON c.customer_type_id = ct.customer_type_id " +
            "WHERE c.is_enable = true " +
            "HAVING suggested_type_id IS NOT NULL AND suggested_type_id != current_type_id", 
            nativeQuery = true)
    List<Tuple> findCustomersNeedingTypeUpdate();

    // Lấy customer mới nhất theo id giảm dần
    Customer findTopByOrderByCustomerIdDesc();
}
