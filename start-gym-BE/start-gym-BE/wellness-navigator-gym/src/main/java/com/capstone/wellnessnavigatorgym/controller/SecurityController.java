package com.capstone.wellnessnavigatorgym.controller;

import java.util.Optional;

import com.capstone.wellnessnavigatorgym.dto.request.ChangePasswordDto;
import com.capstone.wellnessnavigatorgym.dto.request.LoginRequest;
import com.capstone.wellnessnavigatorgym.dto.request.SignupRequest;
import com.capstone.wellnessnavigatorgym.dto.response.JwtResponse;
import com.capstone.wellnessnavigatorgym.dto.response.MessageResponse;
import com.capstone.wellnessnavigatorgym.dto.response.SocialResponse;
import com.capstone.wellnessnavigatorgym.entity.*;
import com.capstone.wellnessnavigatorgym.security.jwt.JwtTokenProvider;
import com.capstone.wellnessnavigatorgym.security.jwt.JwtUtility;
import com.capstone.wellnessnavigatorgym.security.userprinciple.UserPrinciple;
import com.capstone.wellnessnavigatorgym.service.*;
import com.capstone.wellnessnavigatorgym.utils.ConverterMaxCode;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/public")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class SecurityController {

    @Value("${google.clientId}")
    String googleClientId;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtility jwtUtility;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private IAccountService accountService;

    @Autowired
    private ICustomerService customerService;
    
    @Autowired
    private ICustomerTypeService customerTypeService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        // First check if user is banned before authentication
        try {
            // Check account
            Optional<Account> accountOpt = accountService.findAccountByUserName(loginRequest.getUsername());
            if (accountOpt.isPresent()) {
                Account account = accountOpt.get();
                boolean accountEnabled = account.getIsEnable() != null ? account.getIsEnable() : true;
                
                // Check customer if exists
                Customer customer = customerService.findByAccountUserName(loginRequest.getUsername());
                boolean customerEnabled = true;
                if (customer != null) {
                    customerEnabled = customer.getIsEnable() != null ? customer.getIsEnable() : true;
                }
                
                // If either account or customer is disabled, reject login with 401 status
                if (!accountEnabled || !customerEnabled) {
                    System.out.println("🚫 User " + loginRequest.getUsername() + " is BANNED - Account enabled: " + accountEnabled + ", Customer enabled: " + customerEnabled);
                    
                    // Return specific structure that frontend expects
                    Map<String, Object> bannedResponse = new HashMap<>();
                    bannedResponse.put("error", "Account is banned. Please contact support.");
                    bannedResponse.put("message", "Account is banned. Please contact support.");
                    bannedResponse.put("username", loginRequest.getUsername());
                    bannedResponse.put("isBanned", true);
                    bannedResponse.put("accountEnabled", accountEnabled);
                    bannedResponse.put("customerEnabled", customerEnabled);
                    bannedResponse.put("status", 401);
                    
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(bannedResponse);
                }
            }
        } catch (Exception e) {
            // Log error but continue with normal authentication flow
            System.err.println("Error checking banned status during login: " + e.getMessage());
        }
        
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );
        } catch (Exception authException) {
            // Check if this is a banned user exception
            if (authException.getMessage() != null && authException.getMessage().contains("banned")) {
                System.out.println("🚫 Authentication failed due to banned account: " + loginRequest.getUsername());
                
                // Return specific structure that frontend expects
                Map<String, Object> bannedResponse = new HashMap<>();
                bannedResponse.put("error", "Account is banned. Please contact support.");
                bannedResponse.put("message", "Account is banned. Please contact support.");
                bannedResponse.put("username", loginRequest.getUsername());
                bannedResponse.put("isBanned", true);
                bannedResponse.put("status", 401);
                
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(bannedResponse);
            }
            // Re-throw for other authentication failures
            throw authException;
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtility.generateJwtToken(authentication);
        UserPrinciple userDetails = (UserPrinciple) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        return ResponseEntity.ok(
                new JwtResponse(
                        jwt,
                        userDetails.getId(),
                        userDetails.getUsername(),
                        roles
                )
        );
    }



    @PostMapping("/oauth/google")
    @Transactional
    public ResponseEntity<?> loginGoogle(@RequestBody SocialResponse jwtResponseSocial) {
        final NetHttpTransport netHttpTransport = new NetHttpTransport();
        final JacksonFactory jacksonFactory = JacksonFactory.getDefaultInstance();
        GoogleIdTokenVerifier.Builder builder =
                new GoogleIdTokenVerifier.Builder(netHttpTransport, jacksonFactory)
                        .setAudience(Collections.singletonList(googleClientId));

        try {
            final GoogleIdToken googleIdToken = GoogleIdToken.parse(builder.getJsonFactory(), jwtResponseSocial.getToken());
            final GoogleIdToken.Payload payload = googleIdToken.getPayload();

            Account existingAccount = accountService.findByEmail(payload.getEmail());

            if (existingAccount == null) {
                Account newAccount = new Account();
                newAccount.setEmail(payload.getEmail());
                newAccount.setUserName(payload.getEmail());
                newAccount.setIsEnable(true);
                newAccount.setRoles(Collections.singleton(new Role(1, "ROLE_USER")));

                newAccount = accountService.save(newAccount);
                
                if (newAccount == null || newAccount.getAccountId() == null) {
                    throw new RuntimeException("Failed to create account via Google OAuth");
                }

                Customer customerLimit = customerService.customerLimit();
                Customer newCustomer = new Customer();
                newCustomer.setCustomerCode(ConverterMaxCode.generateNextId(
                    customerLimit != null ? customerLimit.getCustomerCode() : null
                ));
                newCustomer.setCustomerName(payload.get("name").toString());
                newCustomer.setCustomerEmail(payload.getEmail());
                newCustomer.setIsEnable(true);
                newCustomer.setAccount(newAccount);
                
                // Set default customer type
                List<CustomerType> customerTypes = customerTypeService.findAllCustomerType();
                if (!customerTypes.isEmpty()) {
                    newCustomer.setCustomerType(customerTypes.get(0));
                }
                
                customerService.save(newCustomer);
            }
            // Create authentication for JWT generation
            Authentication auth = new UsernamePasswordAuthenticationToken(payload.getEmail(), null, Collections.emptyList());
            String jwt = jwtUtility.generateJwtToken(auth);
            return ResponseEntity.ok(new JwtResponse(jwt, payload.getEmail()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/signup")
    @Transactional
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        try {
            // Input validation
            if (signupRequest.getName() == null || signupRequest.getName().trim().isEmpty()) {
                return new ResponseEntity<>(new MessageResponse("Name is required"), HttpStatus.BAD_REQUEST);
            }
            
            if (signupRequest.getUsername() == null || signupRequest.getUsername().trim().isEmpty()) {
                return new ResponseEntity<>(new MessageResponse("Username is required"), HttpStatus.BAD_REQUEST);
            }
            
            if (signupRequest.getPassword() == null || signupRequest.getPassword().trim().isEmpty()) {
                return new ResponseEntity<>(new MessageResponse("Password is required"), HttpStatus.BAD_REQUEST);
            }
            
            if (signupRequest.getEmail() == null || signupRequest.getEmail().trim().isEmpty()) {
                return new ResponseEntity<>(new MessageResponse("Email is required"), HttpStatus.BAD_REQUEST);
            }

            // Enhanced password validation
            String password = signupRequest.getPassword();
            if (password.length() < 8) {
                return new ResponseEntity<>(new MessageResponse("Password must be at least 8 characters long"), HttpStatus.BAD_REQUEST);
            }
            
            if (!password.matches(".*[a-z].*")) {
                return new ResponseEntity<>(new MessageResponse("Password must contain at least one lowercase letter"), HttpStatus.BAD_REQUEST);
            }
            
            if (!password.matches(".*[A-Z].*")) {
                return new ResponseEntity<>(new MessageResponse("Password must contain at least one uppercase letter"), HttpStatus.BAD_REQUEST);
            }
            
            if (!password.matches(".*\\d.*")) {
                return new ResponseEntity<>(new MessageResponse("Password must contain at least one number"), HttpStatus.BAD_REQUEST);
            }
            
            if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
                return new ResponseEntity<>(new MessageResponse("Password must contain at least one special character"), HttpStatus.BAD_REQUEST);
            }

            // Check if username already exists
            if (accountService.existsByUsername(signupRequest.getUsername())) {
                return new ResponseEntity<>(new MessageResponse("Username is already taken!"), HttpStatus.BAD_REQUEST);
            }

            // Check if email already exists
            if (accountService.existsByEmail(signupRequest.getEmail())) {
                return new ResponseEntity<>(new MessageResponse("Email is already in use!"), HttpStatus.BAD_REQUEST);
            }

            // Create account
            Account account = new Account();
            account.setUserName(signupRequest.getUsername());
            account.setEncryptPassword(passwordEncoder.encode(signupRequest.getPassword()));
            account.setEmail(signupRequest.getEmail());
            account.setIsEnable(true);

            // Save account first
            Account savedAccount = accountService.save(account);
            System.out.println("Account saved successfully with ID: " + savedAccount.getAccountId());

            // Assign default customer role (ID 1 is typically customer role)
            Role customerRole = new Role(1, "ROLE_CUSTOMER");
            Set<Role> roles = new HashSet<>();
            roles.add(customerRole);
            savedAccount.setRoles(roles);
            accountService.save(savedAccount); // Save account with roles

            // Get default customer type
            List<CustomerType> customerTypes = customerTypeService.findAllCustomerType();
            CustomerType defaultType;
            if (customerTypes.isEmpty()) {
                throw new RuntimeException("No customer type available");
            }
            defaultType = customerTypes.get(0);

            // Generate customer code
            Customer customerLimit = customerService.customerLimit();
            String customerCode = ConverterMaxCode.generateNextId(
                customerLimit != null ? customerLimit.getCustomerCode() : null
            );

            // Create customer with additional details from signup
            Customer customer = new Customer();
            customer.setCustomerCode(customerCode);
            customer.setCustomerName(signupRequest.getName());
            customer.setCustomerEmail(signupRequest.getEmail());
            customer.setCustomerPhone(signupRequest.getPhone());
            customer.setCustomerGender(signupRequest.getGender());
            customer.setDateOfBirth(signupRequest.getDateOfBirth());
            customer.setCustomerAddress(signupRequest.getAddress());
            customer.setIsEnable(true);
            customer.setAccount(savedAccount); // Use the saved account instance
            customer.setCustomerType(defaultType);
            
            // Save customer
            customerService.save(customer);
            
            System.out.println("Customer saved successfully with ID: " + customer.getCustomerId()); // Debug log
            System.out.println("Account ID linked: " + savedAccount.getAccountId()); // Debug log

            return new ResponseEntity<>(new MessageResponse("Account registration successful!"), HttpStatus.OK);
            
        } catch (Exception e) {
            System.err.println("Registration error: " + e.getMessage()); // Debug log
            e.printStackTrace();
            return new ResponseEntity<>(new MessageResponse("Registration failed: " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordDto changePasswordDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(changePasswordDto.getUsername(), changePasswordDto.getPresentPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String newPass = encoder.encode(changePasswordDto.getConfirmPassword());
        accountService.changePassword(username, newPass);
        return ResponseEntity.ok(new MessageResponse("update successful"));
    }
    

}



