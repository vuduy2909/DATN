package com.capstone.wellnessnavigatorgym.controller;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.capstone.wellnessnavigatorgym.dto.request.ChangePasswordDto;
import com.capstone.wellnessnavigatorgym.dto.request.LoginRequest;
import com.capstone.wellnessnavigatorgym.dto.request.SignupRequest;
import com.capstone.wellnessnavigatorgym.dto.request.TokenRefreshRequest;
import com.capstone.wellnessnavigatorgym.dto.response.JwtResponse;
import com.capstone.wellnessnavigatorgym.dto.response.MessageResponse;
import com.capstone.wellnessnavigatorgym.dto.response.SocialResponse;
import com.capstone.wellnessnavigatorgym.dto.response.TokenRefreshResponse;
import com.capstone.wellnessnavigatorgym.entity.*;
import com.capstone.wellnessnavigatorgym.exception.TokenRefreshException;
import com.capstone.wellnessnavigatorgym.repository.ICustomerRepository;
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
    private IAccountService accountService;

    @Autowired
    private ICustomerService customerService;
    
    @Autowired
    private ICustomerTypeService customerTypeService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ICustomerRepository customerRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        // ===== ENHANCED PRE-AUTHENTICATION BAN CHECK =====
        try {
            System.out.println("=== PRE-AUTH BAN CHECK ===");
            System.out.println("Checking ban status for login input: " + loginRequest.getUsername());
            
            // Check account by username OR email
            Optional<Account> accountOpt = Optional.empty();
            String loginInput = loginRequest.getUsername();
            
            // First try to find by username
            accountOpt = accountService.findAccountByUserName(loginInput);
            
            // If not found by username, try by email
            if (!accountOpt.isPresent() && loginInput.contains("@")) {
                Account accountByEmail = accountService.findByEmail(loginInput);
                if (accountByEmail != null) {
                    accountOpt = Optional.of(accountByEmail);
                    System.out.println("Found account by email: " + loginInput);
                }
            }
            
            if (accountOpt.isPresent()) {
                Account account = accountOpt.get();
                String actualUsername = account.getUserName(); // Get the actual username for further processing
                
                // Enhanced bit(1) handling for account.is_enable
                Boolean accountIsEnable = account.getIsEnable();
                boolean accountEnabled = (accountIsEnable != null) ? accountIsEnable.booleanValue() : true;
                
                System.out.println("Account check - Raw is_enable: " + accountIsEnable + ", Parsed boolean: " + accountEnabled);
                
                // Check customer - MUST exist for every account
                Customer customer = customerService.findByAccountUserName(actualUsername);
                String customerName = null;
                boolean customerEnabled = true;
                
                if (customer == null) {
                    System.out.println("❌ PRE-AUTH CHECK: Account exists but no customer linked");
                    System.out.println("❌ Username '" + actualUsername + "' has account but missing customer record");
                    
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("error", "Account configuration error");
                    errorResponse.put("message", "Account missing customer data. Please contact support.");
                    errorResponse.put("status", 500);
                    errorResponse.put("timestamp", java.time.LocalDateTime.now());
                    
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
                } else {
                    Boolean customerIsEnable = customer.getIsEnable();
                    customerEnabled = (customerIsEnable != null) ? customerIsEnable.booleanValue() : true;
                    customerName = customer.getCustomerName();
                    
                    System.out.println("Customer check - Raw is_enable: " + customerIsEnable + ", Parsed boolean: " + customerEnabled);
                }
                
                // Ban logic: User is banned if EITHER Account.is_enable = 0 OR Customer.is_enable = 0
                if (!accountEnabled || !customerEnabled) {
                    String banReason = "";
                    if (!accountEnabled && !customerEnabled) {
                        banReason = "Both Account and Customer are disabled";
                    } else if (!accountEnabled) {
                        banReason = "Account is disabled (Account.is_enable = 0)";
                    } else {
                        banReason = "Customer is disabled (Customer.is_enable = 0)";
                    }
                    
                    System.out.println("🚫 PRE-AUTH BAN CHECK FAILED");
                    System.out.println("🚫 User " + loginInput + " is BANNED");
                    System.out.println("🚫 Reason: " + banReason);
                    System.out.println("🚫 Account enabled: " + accountEnabled + " (raw: " + accountIsEnable + ")");
                    System.out.println("🚫 Customer enabled: " + customerEnabled + (customer != null ? " (raw: " + customer.getIsEnable() + ")" : " (no customer)"));
                    
                    // Return specific structure that frontend expects
                    Map<String, Object> bannedResponse = new HashMap<>();
                    bannedResponse.put("error", "Account is banned. Please contact support.");
                    bannedResponse.put("message", "Account is banned. Please contact support.");
                    bannedResponse.put("username", actualUsername);
                    bannedResponse.put("customerName", customerName);
                    bannedResponse.put("isBanned", true);
                    bannedResponse.put("accountEnabled", accountEnabled);
                    bannedResponse.put("customerEnabled", customerEnabled);
                    bannedResponse.put("status", 401);
                    bannedResponse.put("reason", banReason);
                    bannedResponse.put("banDetails", banReason);
                    
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(bannedResponse);
                } else {
                    System.out.println("✅ PRE-AUTH BAN CHECK PASSED");
                    System.out.println("✅ Account enabled: " + accountEnabled + ", Customer enabled: " + customerEnabled);
                    System.out.println("✅ Both Account.is_enable = 1 AND Customer.is_enable = 1");
                }
            } else {
                // ===== USER NOT FOUND HANDLING =====
                System.out.println("❌ PRE-AUTH CHECK: User not found in database");
                System.out.println("❌ Login input '" + loginInput + "' does not exist (checked both username and email)");
                
                // Don't proceed with authentication for non-existent users
                // Return specific error message for user not found
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "User does not exist");
                errorResponse.put("message", "Tài khoản không tồn tại. Vui lòng kiểm tra lại thông tin đăng nhập hoặc đăng ký tài khoản mới.");
                errorResponse.put("status", 401);
                errorResponse.put("timestamp", java.time.LocalDateTime.now());
                errorResponse.put("userExists", false);
                
                System.out.println("❌ Returning 401 for non-existent user without proceeding to authentication");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
        } catch (Exception e) {
            // Log error but continue with normal authentication flow
            System.err.println("Error checking banned status during login: " + e.getMessage());
            e.printStackTrace();
        }
        
        Authentication authentication;
        try {
            // For authentication, we need to use the actual username, not email
            String loginInput = loginRequest.getUsername();
            String actualUsername = loginInput;
            
            // If user logged in with email, convert to username for authentication
            if (loginInput.contains("@")) {
                Account accountByEmail = accountService.findByEmail(loginInput);
                if (accountByEmail != null) {
                    actualUsername = accountByEmail.getUserName();
                }
            }
            
            authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(actualUsername, loginRequest.getPassword())
            );
        } catch (Exception authException) {
            System.out.println("🔍 Authentication failed for user: " + loginRequest.getUsername());
            System.out.println("🔍 Exception type: " + authException.getClass().getSimpleName());
            System.out.println("🔍 Exception message: " + authException.getMessage());
            
            // Check if this is a banned user exception from UserDetailService
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
            
            // For other authentication failures (like bad credentials), return specific error
            System.out.println("❌ Authentication failed - Invalid credentials for user: " + loginRequest.getUsername());
            
            Map<String, Object> credentialsResponse = new HashMap<>();
            credentialsResponse.put("error", "Invalid credentials");
            credentialsResponse.put("message", "Thông tin đăng nhập không chính xác. Vui lòng kiểm tra lại thông tin đăng nhập và mật khẩu.");
            credentialsResponse.put("status", 401);
            credentialsResponse.put("timestamp", java.time.LocalDateTime.now());
            credentialsResponse.put("userExists", true);
            credentialsResponse.put("isBanned", false);
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(credentialsResponse);
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtility.generateJwtToken(authentication);
        UserPrinciple userDetails = (UserPrinciple) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
                
        String refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());
        
        return ResponseEntity.ok(
                new JwtResponse(
                        jwt,
                        refreshToken,
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
            
            // Get account for JWT generation
            Account account = accountService.findByEmail(payload.getEmail());
            if (account == null) {
                return ResponseEntity.badRequest().body("Account not found after creation");
            }
            
            // Create UserPrinciple for JWT generation
            UserPrinciple userPrinciple = UserPrinciple.build(account);
            Authentication auth = new UsernamePasswordAuthenticationToken(userPrinciple, null, userPrinciple.getAuthorities());
            String jwt = jwtUtility.generateJwtToken(auth);
            
            // Create refresh token
            String refreshToken = refreshTokenService.createRefreshToken(account.getAccountId());
            List<String> roles = new ArrayList<>();
            for (Role role : account.getRoles()) {
                roles.add(role.getRoleName());
            }
            
            return ResponseEntity.ok(new JwtResponse(jwt, refreshToken, account.getAccountId(), account.getUserName(), roles));
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

    // ==================== REGISTRATION VALIDATION ENDPOINTS ====================
    
    /**
     * Check if email already exists for public registration
     */
    @GetMapping("/check-email-exists")
    public ResponseEntity<?> checkEmailExistsPublic(@RequestParam String email) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("email", email);
            
            // Check email exists in system
            boolean emailExists = accountService.existsByEmail(email);
            
            response.put("exists", emailExists);
            
            if (emailExists) {
                response.put("message", "Email đã được sử dụng");
            } else {
                response.put("message", "Email có thể sử dụng");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error checking email: " + e.getMessage());
        }
    }

    /**
     * Check if username already exists for public registration
     */
    @GetMapping("/check-username-exists")
    public ResponseEntity<?> checkUsernameExistsPublic(@RequestParam String username) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("username", username);
            
            // Check username exists in system
            boolean usernameExists = accountService.existsByUsername(username);
            
            response.put("exists", usernameExists);
            
            if (usernameExists) {
                response.put("message", "Tên đăng nhập đã được sử dụng");
            } else {
                response.put("message", "Tên đăng nhập có thể sử dụng");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error checking username: " + e.getMessage());
        }
    }

    /**
     * Check if phone number already exists for public registration
     */
    @GetMapping("/check-phone-exists")
    public ResponseEntity<?> checkPhoneExistsPublic(@RequestParam String phone) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("phone", phone);
            
            // Check phone exists in customer table
            List<Customer> existingCustomers = customerRepository.findByCustomerPhone(phone);
            boolean phoneExists = !existingCustomers.isEmpty();
            
            response.put("exists", phoneExists);
            
            if (phoneExists) {
                response.put("message", "Số điện thoại đã được sử dụng");
                // Không trả về customerName để bảo mật thông tin
            } else {
                response.put("message", "Số điện thoại có thể sử dụng");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error checking phone: " + e.getMessage());
        }
    }

    // ==================== FORGOT PASSWORD ENDPOINTS ====================
    
    /**
     * Send password reset email with OTP
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Email is required");
            }
            
            // Check if email exists
            Account account = accountService.findByEmail(email);
            if (account == null) {
                return ResponseEntity.badRequest().body("Email không tồn tại trong hệ thống");
            }
            
            // Generate 6-digit OTP
            String otp = String.format("%06d", new Random().nextInt(999999));
            
            // Store OTP with expiration (15 minutes)
            passwordResetOtps.put(email, new OtpData(otp, System.currentTimeMillis() + 15 * 60 * 1000));
            
            // Send email with OTP
            try {
                emailService.sendPasswordResetOTP(email, otp);
                System.out.println("Password reset OTP sent to email: " + email);
            } catch (Exception e) {
                System.err.println("Failed to send email to " + email + ": " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Không thể gửi email. Vui lòng thử lại sau.");
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Mã xác thực đã được gửi đến email của bạn");
            response.put("email", email);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error in forgot password: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error sending password reset email: " + e.getMessage());
        }
    }
    
    /**
     * Verify OTP only (separate step before password reset)
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOTP(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String otp = request.get("otp");
            
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Email is required");
            }
            
            if (otp == null || otp.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("OTP is required");
            }
            
            // Check if OTP exists and is valid
            OtpData otpData = passwordResetOtps.get(email);
            if (otpData == null) {
                return ResponseEntity.badRequest().body("Mã OTP không tồn tại hoặc đã hết hạn");
            }
            
            // Check if OTP has expired
            if (System.currentTimeMillis() > otpData.expirationTime) {
                passwordResetOtps.remove(email);
                return ResponseEntity.badRequest().body("Mã OTP đã hết hạn");
            }
            
            // Check if OTP matches
            if (!otpData.otp.equals(otp)) {
                return ResponseEntity.badRequest().body("Mã OTP không chính xác");
            }
            
            // OTP is valid - don't remove it yet, keep it for password reset
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Mã OTP hợp lệ. Bạn có thể đặt lại mật khẩu.");
            response.put("email", email);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error verifying OTP: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error verifying OTP: " + e.getMessage());
        }
    }
    
    /**
     * Verify OTP and reset password
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String otp = request.get("otp");
            String newPassword = request.get("newPassword");
            
            if (email == null || otp == null || newPassword == null) {
                return ResponseEntity.badRequest().body("Email, OTP và mật khẩu mới là bắt buộc");
            }
            
            // Verify OTP
            OtpData otpData = passwordResetOtps.get(email);
            if (otpData == null) {
                return ResponseEntity.badRequest().body("Mã OTP không hợp lệ hoặc đã hết hạn");
            }
            
            if (System.currentTimeMillis() > otpData.expirationTime) {
                passwordResetOtps.remove(email);
                return ResponseEntity.badRequest().body("Mã OTP đã hết hạn");
            }
            
            if (!otpData.otp.equals(otp)) {
                return ResponseEntity.badRequest().body("Mã OTP không chính xác");
            }
            
            // Validate new password
            if (newPassword.length() < 8) {
                return ResponseEntity.badRequest().body("Mật khẩu phải có ít nhất 8 ký tự");
            }
            
            if (!newPassword.matches(".*[a-z].*")) {
                return ResponseEntity.badRequest().body("Mật khẩu phải có ít nhất 1 chữ thường");
            }
            
            if (!newPassword.matches(".*[A-Z].*")) {
                return ResponseEntity.badRequest().body("Mật khẩu phải có ít nhất 1 chữ hoa");
            }
            
            if (!newPassword.matches(".*\\d.*")) {
                return ResponseEntity.badRequest().body("Mật khẩu phải có ít nhất 1 số");
            }
            
            if (!newPassword.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
                return ResponseEntity.badRequest().body("Mật khẩu phải có ít nhất 1 ký tự đặc biệt");
            }
            
            // Find account and update password
            Account account = accountService.findByEmail(email);
            if (account == null) {
                return ResponseEntity.badRequest().body("Tài khoản không tồn tại");
            }
            
            // Update password
            account.setEncryptPassword(passwordEncoder.encode(newPassword));
            accountService.save(account);
            
            // Remove used OTP
            passwordResetOtps.remove(email);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Đặt lại mật khẩu thành công");
            response.put("email", email);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error resetting password: " + e.getMessage());
        }
    }
    
    // Simple OTP storage class (use Redis or database in production)
    private static class OtpData {
        String otp;
        long expirationTime;
        
        OtpData(String otp, long expirationTime) {
            this.otp = otp;
            this.expirationTime = expirationTime;
        }
    }
    
    // OTP storage - in production, use Redis or database
    private final Map<String, OtpData> passwordResetOtps = new ConcurrentHashMap<>();

    /**
     * Test endpoint để kiểm tra cấu hình email
     */
    @PostMapping("/test-email")
    public ResponseEntity<?> testEmail(@RequestBody Map<String, String> request) {
        try {
            String testEmail = request.get("email");
            if (testEmail == null || testEmail.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Email is required for testing");
            }
            
            // Test gửi email đơn giản
            emailService.sendSimpleEmail(testEmail, "StartGym Email Test", 
                "Đây là email test từ StartGym. Nếu bạn nhận được email này, cấu hình SMTP đã hoạt động!");
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Test email sent successfully to: " + testEmail);
            response.put("status", "success");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to send test email: " + e.getMessage());
            errorResponse.put("status", "error");
            errorResponse.put("error", e.getClass().getSimpleName());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshtoken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        // Verify token exists and is not expired
        if (!refreshTokenService.verifyExpiration(requestRefreshToken)) {
            throw new TokenRefreshException(requestRefreshToken, "Refresh token is expired. Please make a new signin request");
        }

        return refreshTokenService.findByRefreshToken(requestRefreshToken)
                .map(account -> {
                    // Create UserPrinciple for JWT generation
                    UserPrinciple userPrinciple = UserPrinciple.build(account);
                    Authentication auth = new UsernamePasswordAuthenticationToken(
                            userPrinciple, 
                            null, 
                            userPrinciple.getAuthorities()
                    );
                    String token = jwtUtility.generateJwtToken(auth);
                    return ResponseEntity.ok(new TokenRefreshResponse(token, requestRefreshToken));
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
                        "Refresh token is not in database!"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(@RequestHeader("Authorization") String bearerToken) {
        try {
            if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                String jwt = bearerToken.substring(7);
                String username = jwtUtility.getUserNameFromJwtToken(jwt);
                
                Optional<Account> accountOpt = accountService.findAccountByUserName(username);
                if (accountOpt.isPresent()) {
                    refreshTokenService.deleteRefreshToken(accountOpt.get().getAccountId());
                }
            }
            
            return ResponseEntity.ok(new MessageResponse("Log out successful!"));
        } catch (Exception e) {
            return ResponseEntity.ok(new MessageResponse("Log out successful!"));
        }
    }

}



