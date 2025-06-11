package com.capstone.wellnessnavigatorgym.controller;

import com.capstone.wellnessnavigatorgym.entity.CustomerType;
import com.capstone.wellnessnavigatorgym.entity.Customer;
import com.capstone.wellnessnavigatorgym.service.ICustomerTypeService;
import com.capstone.wellnessnavigatorgym.repository.ICustomerTypeRepository;
import com.capstone.wellnessnavigatorgym.repository.ICustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/customer-type")
@CrossOrigin(origins = "http://localhost:3000")
public class CustomerTypeController {

    @Autowired
    private ICustomerTypeService customerTypeService;
    
    @Autowired
    private ICustomerTypeRepository customerTypeRepository;
    
    @Autowired 
    private ICustomerRepository customerRepository;

    @GetMapping("")
    public ResponseEntity<List<CustomerType>> getAllCustomerType() {
        List<CustomerType> customerTypeList = this.customerTypeService.findAllCustomerType();
        if (customerTypeList.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(customerTypeList, HttpStatus.OK);
    }
    
    /**
     * Lấy tất cả customer types với sorting
     */
    @GetMapping("/sorted")
    public ResponseEntity<?> getAllCustomerTypesSorted(
            @RequestParam(value = "sortBy", defaultValue = "price") String sortBy,
            @RequestParam(value = "sortOrder", defaultValue = "asc") String sortOrder) {
        try {
            List<CustomerType> customerTypes;
            
            if ("price".equals(sortBy)) {
                if ("desc".equals(sortOrder.toLowerCase())) {
                    customerTypes = customerTypeRepository.findAllOrderByPriceDesc();
                } else {
                    customerTypes = customerTypeRepository.findAllOrderByPriceAsc();
                }
            } else {
                // Default sorting by ID
                customerTypes = customerTypeRepository.findAll();
            }
            
            // Thêm thống kê số lượng customers cho mỗi type
            List<Map<String, Object>> customerTypesWithStats = customerTypes.stream()
                .map(type -> {
                    Map<String, Object> typeInfo = new HashMap<>();
                    typeInfo.put("customerTypeId", type.getCustomerTypeId());
                    typeInfo.put("customerTypeName", type.getCustomerTypeName());
                    typeInfo.put("price", type.getPrice());
                    
                    // Đếm số customers có type này
                    long customerCount = customerRepository.findAll().stream()
                        .filter(c -> c.getIsEnable() && c.getCustomerType() != null)
                        .filter(c -> c.getCustomerType().getCustomerTypeId().equals(type.getCustomerTypeId()))
                        .count();
                    typeInfo.put("customerCount", customerCount);
                    
                    return typeInfo;
                })
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("customerTypes", customerTypesWithStats);
            response.put("totalTypes", customerTypes.size());
            response.put("sortBy", sortBy);
            response.put("sortOrder", sortOrder);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting sorted customer types: " + e.getMessage());
        }
    }
    
    /**
     * Lấy customer type theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getCustomerTypeById(@PathVariable Integer id) {
        try {
            CustomerType customerType = customerTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer type not found with ID: " + id));
            
            Map<String, Object> response = new HashMap<>();
            response.put("customerType", customerType);
            response.put("message", "Customer type retrieved successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error getting customer type: " + e.getMessage());
        }
    }
    
    /**
     * Tạo customer type mới với sequential ID
     */
    @PostMapping("")
    public ResponseEntity<?> createCustomerType(@RequestBody Map<String, Object> request) {
        try {
            String customerTypeName = (String) request.get("customerTypeName");
            Double price = request.get("price") != null ? 
                Double.valueOf(request.get("price").toString()) : 0.0;
            
            // Validation
            if (customerTypeName == null || customerTypeName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Customer type name is required");
            }
            
            if (price < 0) {
                return ResponseEntity.badRequest().body("Price cannot be negative");
            }
            
            // Kiểm tra tên đã tồn tại chưa
            CustomerType existingType = customerTypeRepository.findByCustomerTypeName(customerTypeName);
            if (existingType != null) {
                return ResponseEntity.badRequest().body("Customer type name already exists");
            }
            
            // Get next sequential ID (MAX + 1)
            Integer nextId = customerTypeRepository.findAll().stream()
                .mapToInt(CustomerType::getCustomerTypeId)
                .max()
                .orElse(0) + 1;
            
            CustomerType newCustomerType = new CustomerType();
            newCustomerType.setCustomerTypeId(nextId);
            newCustomerType.setCustomerTypeName(customerTypeName.trim());
            newCustomerType.setPrice(price);
            
            CustomerType savedType = customerTypeRepository.save(newCustomerType);
            
            Map<String, Object> response = new HashMap<>();
            response.put("customerType", savedType);
            response.put("message", "Customer type created successfully with ID: " + nextId);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating customer type: " + e.getMessage());
        }
    }
    
    /**
     * Cập nhật customer type
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCustomerType(@PathVariable Integer id, @RequestBody Map<String, Object> request) {
        try {
            CustomerType existingType = customerTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer type not found with ID: " + id));
            
            String customerTypeName = (String) request.get("customerTypeName");
            Double price = request.get("price") != null ? 
                Double.valueOf(request.get("price").toString()) : existingType.getPrice();
            
            // Validation
            if (customerTypeName != null && !customerTypeName.trim().isEmpty()) {
                // Kiểm tra tên mới có trùng với type khác không
                CustomerType typeWithSameName = customerTypeRepository.findByCustomerTypeName(customerTypeName);
                if (typeWithSameName != null && !typeWithSameName.getCustomerTypeId().equals(id)) {
                    return ResponseEntity.badRequest().body("Customer type name already exists");
                }
                existingType.setCustomerTypeName(customerTypeName.trim());
            }
            
            if (price < 0) {
                return ResponseEntity.badRequest().body("Price cannot be negative");
            }
            existingType.setPrice(price);
            
            CustomerType updatedType = customerTypeRepository.save(existingType);
            
            Map<String, Object> response = new HashMap<>();
            response.put("customerType", updatedType);
            response.put("message", "Customer type updated successfully");
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating customer type: " + e.getMessage());
        }
    }
    
    /**
     * Fix sequential IDs after deletion
     */
    private void reorderCustomerTypeIds(Integer deletedId) {
        try {
            // Get all customer types with ID greater than deleted ID
            List<CustomerType> typesToUpdate = customerTypeRepository.findAll().stream()
                .filter(type -> type.getCustomerTypeId() > deletedId)
                .sorted((a, b) -> a.getCustomerTypeId().compareTo(b.getCustomerTypeId()))
                .collect(Collectors.toList());
            
            // Update each type's ID and all customer references
            for (CustomerType type : typesToUpdate) {
                Integer oldId = type.getCustomerTypeId();
                Integer newId = oldId - 1;
                
                // Update customers that reference this type
                List<Customer> customersToUpdate = customerRepository.findAll().stream()
                    .filter(c -> c.getCustomerType() != null && 
                               c.getCustomerType().getCustomerTypeId().equals(oldId))
                    .collect(Collectors.toList());
                
                // Create new CustomerType with new ID
                CustomerType newType = new CustomerType();
                newType.setCustomerTypeId(newId);
                newType.setCustomerTypeName(type.getCustomerTypeName());
                newType.setPrice(type.getPrice());
                
                // Save new type first
                customerTypeRepository.save(newType);
                
                // Update customer references
                for (Customer customer : customersToUpdate) {
                    customer.setCustomerType(newType);
                    customerRepository.save(customer);
                }
                
                // Delete old type
                customerTypeRepository.delete(type);
                customerTypeRepository.flush();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            // Log error but don't fail the operation
        }
    }
    
    /**
     * Xóa customer type với sequential ID adjustment
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCustomerType(@PathVariable Integer id) {
        try {
            // Check if customer type exists first
            Optional<CustomerType> customerTypeOpt = customerTypeRepository.findById(id);
            if (!customerTypeOpt.isPresent()) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Customer type not found");
                error.put("message", "Customer type with ID " + id + " does not exist");
                error.put("customerTypeId", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            CustomerType customerType = customerTypeOpt.get();
            
            // Kiểm tra xem có customer nào đang sử dụng type này không
            long customersUsingType = customerRepository.findAll().stream()
                .filter(c -> c.getCustomerType() != null)
                .filter(c -> c.getCustomerType().getCustomerTypeId().equals(id))
                .count();
            
            if (customersUsingType > 0) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Cannot delete customer type");
                error.put("message", "There are " + customersUsingType + " customers using this type");
                error.put("customersCount", customersUsingType);
                return ResponseEntity.badRequest().body(error);
            }
            
            // Xóa customer type trước
            customerTypeRepository.delete(customerType);
            customerTypeRepository.flush();
            
            // Fix sequential IDs sau khi xóa
            reorderCustomerTypeIds(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Customer type deleted successfully and IDs reordered to maintain sequential order");
            response.put("deletedType", customerType);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error deleting customer type");
            error.put("message", e.getMessage());
            error.put("customerTypeId", id);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
