package com.capstone.wellnessnavigatorgym.controller;

import com.capstone.wellnessnavigatorgym.dto.customer.CustomerInfo;
import com.capstone.wellnessnavigatorgym.dto.customer.CustomerUserDetailDto;
import com.capstone.wellnessnavigatorgym.dto.response.MessageResponse;
import com.capstone.wellnessnavigatorgym.entity.Customer;
import com.capstone.wellnessnavigatorgym.service.ICustomerService;
import com.capstone.wellnessnavigatorgym.utils.ConverterMaxCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/api/v1/customer")
@CrossOrigin(origins = "http://localhost:3000")
public class CustomerController {

    @Autowired
    private ICustomerService customerService;

    @GetMapping("/list")
    public ResponseEntity<Page<Customer>> findAllCustomer(@RequestParam(value = "type", required = false) Optional<String> type,
                                                          @RequestParam(value = "name", required = false) Optional<String> name,
                                                          @RequestParam(value = "address", required = false) Optional<String> address,
                                                          @RequestParam(value = "phone", required = false) Optional<String> phone,
                                                           @RequestParam("page") Optional<Integer> page,
                                                          @RequestParam("size") Optional<Integer> size,
                                                          @RequestParam("sort") Optional<String> sort) {
        String searchType = type.orElse("");
        String searchName = name.orElse("");
        String searchAddress = address.orElse("");
        String searchPhone = phone.orElse("");
        int pages = page.orElse(1);
        int pageSize = size.orElse(5);
        String sortName = sort.orElse("customer_name");
        Page<Customer> searchCustomer = this.customerService.searchCustomers(searchType, searchName, searchAddress,
                searchPhone, PageRequest.of(pages - 1, pageSize, Sort.by(sortName)));
        if (searchCustomer.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(searchCustomer, HttpStatus.OK);
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> saveCustomer(@Valid @RequestBody CustomerInfo customerInfo, BindingResult bindingResult) {
        new CustomerInfo().validate(customerInfo, bindingResult);
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(
                    error -> {
                        String fieldName = error.getField();
                        String errorMessage = error.getDefaultMessage();
                        errors.put(fieldName, errorMessage);
                    });
            return ResponseEntity.badRequest().body(errors);
        } else {
            Customer customerLimit = customerService.customerLimit();
            customerInfo.setCustomerCode(ConverterMaxCode.generateNextId(customerLimit.getCustomerCode()));
            customerService.saveCustomer(customerInfo);
        }
        return new ResponseEntity<>(new MessageResponse("New customer successfully created!"), HttpStatus.CREATED);
    }


    @GetMapping("/list/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Integer id) {
        return new ResponseEntity<>(customerService.findById(id), HttpStatus.OK);
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<?> updateCustomer(@Valid @PathVariable Integer id, @RequestBody CustomerInfo customerInfo, BindingResult bindingResult) {
        new CustomerInfo().validate(customerInfo, bindingResult);
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(
                    error -> {
                        String fieldName = error.getField();
                        String errorMessage = error.getDefaultMessage();
                        errors.put(fieldName, errorMessage);
                    });
            return ResponseEntity.badRequest().body(errors);
        } else {
            customerService.updateCustomer(customerInfo, id);
        }
        return new ResponseEntity<>(new MessageResponse("The customer has successfully edited!"), HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteCustomer(@PathVariable Integer id) {
        customerService.deleteById(id);
        return new ResponseEntity<>(new MessageResponse("Customer has successfully deleted!"), HttpStatus.OK);
    }

    @GetMapping("/detail")
    public ResponseEntity<CustomerUserDetailDto> getDetail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        CustomerUserDetailDto customerUserDetailDto = customerService.findUserDetailByUsername(username);

        if (customerUserDetailDto == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(customerUserDetailDto, HttpStatus.OK);
    }
}
