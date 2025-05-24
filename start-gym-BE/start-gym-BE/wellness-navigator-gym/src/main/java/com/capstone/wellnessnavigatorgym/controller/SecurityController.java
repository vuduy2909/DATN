package com.capstone.wellnessnavigatorgym.controller;

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
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

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

                Customer customerLimit = customerService.customerLimit();
                Customer newCustomer = new Customer();
                newCustomer.setCustomerCode(ConverterMaxCode.generateNextId(customerLimit.getCustomerCode()));
                newCustomer.setCustomerName(payload.get("name").toString());
                newCustomer.setCustomerEmail(payload.getEmail());
                newCustomer.setIsEnable(true);
                newCustomer.setAccount(newAccount);
                customerService.save(newCustomer);
            }
            String jwt = jwtTokenProvider.generateToken(payload.getEmail());
            return ResponseEntity.ok(new JwtResponse(jwt, payload.getEmail()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest signupRequest) {
        try {
            // Validate input
            if (signupRequest.getUsername() == null || signupRequest.getUsername().trim().isEmpty()) {
                return new ResponseEntity<>(new MessageResponse("Username is required!"), HttpStatus.BAD_REQUEST);
            }
            
            if (signupRequest.getEmail() == null || signupRequest.getEmail().trim().isEmpty()) {
                return new ResponseEntity<>(new MessageResponse("Email is required!"), HttpStatus.BAD_REQUEST);
            }
            
            if (signupRequest.getPassword() == null || signupRequest.getPassword().trim().isEmpty()) {
                return new ResponseEntity<>(new MessageResponse("Password is required!"), HttpStatus.BAD_REQUEST);
            }
            
            if (signupRequest.getName() == null || signupRequest.getName().trim().isEmpty()) {
                return new ResponseEntity<>(new MessageResponse("Full name is required!"), HttpStatus.BAD_REQUEST);
            }

            // Check if username already exists
            if (accountService.existsByUsername(signupRequest.getUsername())) {
                return new ResponseEntity<>(new MessageResponse("The username already exists! Please try again!"), HttpStatus.BAD_REQUEST);
            }

            // Check if email already exists
            if (accountService.existsByEmail(signupRequest.getEmail())) {
                return new ResponseEntity<>(new MessageResponse("The email already exists! Please try again!"), HttpStatus.BAD_REQUEST);
            }

            // Create Account
            Account account = new Account();
            account.setUserName(signupRequest.getUsername());
            account.setEncryptPassword(passwordEncoder.encode(signupRequest.getPassword()));
            account.setEmail(signupRequest.getEmail());
            account.setIsEnable(true);
            
            // Assign default USER role
            Role role = new Role(1, "ROLE_USER");
            Set<Role> roles = new HashSet<>();
            roles.add(role);
            account.setRoles(roles);
            
            // Save account first
            account = accountService.save(account);

            // Generate customer code
            Customer customerLimit = customerService.customerLimit();
            String customerCode = ConverterMaxCode.generateNextId(customerLimit.getCustomerCode());

            // Create Customer record automatically
            Customer customer = new Customer();
            customer.setCustomerCode(customerCode);
            customer.setCustomerName(signupRequest.getName());
            customer.setCustomerEmail(signupRequest.getEmail());
            customer.setCustomerPhone(signupRequest.getPhone());
            customer.setCustomerGender(signupRequest.getGender());
            customer.setDateOfBirth(signupRequest.getDateOfBirth());
            customer.setCustomerAddress(signupRequest.getAddress());
            customer.setIsEnable(true);
            customer.setAccount(account);
            
            // Set default customer type (assuming ID 1 is the basic/free type)
            List<CustomerType> customerTypes = customerTypeService.findAllCustomerType();
            if (!customerTypes.isEmpty()) {
                CustomerType defaultType = customerTypes.get(0); // Get first customer type as default
                customer.setCustomerType(defaultType);
            }
            
            // Note: idCard is removed, so we don't set it
            
            // Save customer
            customerService.save(customer);

            return new ResponseEntity<>(new MessageResponse("Account registration successful!"), HttpStatus.OK);
            
        } catch (Exception e) {
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
        return new ResponseEntity<>(new ChangePasswordDto(  
                changePasswordDto.getUsername(),
                "", ""), HttpStatus.OK);
    }
}



