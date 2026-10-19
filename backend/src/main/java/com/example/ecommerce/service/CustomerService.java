package com.example.ecommerce.service;

import com.example.ecommerce.dto.AddressResponse;
import com.example.ecommerce.dto.CustomerRequest;
import com.example.ecommerce.dto.CustomerResponse;
import com.example.ecommerce.dto.CustomerUpdateRequest;
import com.example.ecommerce.exception.ConflictException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.model.AppUser;
import com.example.ecommerce.model.Customer;
import com.example.ecommerce.model.Role;
import com.example.ecommerce.repository.AddressRepository;
import com.example.ecommerce.repository.AppUserRepository;
import com.example.ecommerce.repository.CustomerOrderRepository;
import com.example.ecommerce.repository.CustomerRepository;
import com.example.ecommerce.repository.RoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final AddressRepository addressRepository;
    private final CustomerOrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerService(CustomerRepository customerRepository,
                           AppUserRepository appUserRepository,
                           RoleRepository roleRepository,
                           AddressRepository addressRepository,
                           CustomerOrderRepository orderRepository,
                           PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.appUserRepository = appUserRepository;
        this.roleRepository = roleRepository;
        this.addressRepository = addressRepository;
        this.orderRepository = orderRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> findAll() {
        return customerRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CustomerResponse findById(Integer id) {
        return toResponse(getCustomer(id));
    }

    @Transactional(readOnly = true)
    public CustomerResponse findMyProfile(String username) {
        return toResponse(getByUsername(username));
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> findMyAddresses(String username) {
        Customer customer = getByUsername(username);
        return addressRepository.findByCustomerId(customer.getId()).stream()
                .map(a -> new AddressResponse(a.getId(), a.getStreet(), a.getCity(), a.getProvince(),
                        a.getPostalCode(), a.getCountry()))
                .toList();
    }

    /**
     * Creates the user account (role CUSTOMER) and the customer profile in the same transaction.
     */
    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {
        if (appUserRepository.existsByUsername(request.username())) {
            throw new ConflictException("The username '" + request.username() + "' is already used");
        }
        if (appUserRepository.existsByEmail(request.email())) {
            throw new ConflictException("The email '" + request.email() + "' is already used");
        }

        Role customerRole = roleRepository.findByName(UserService.CUSTOMER)
                .orElseThrow(() -> new ResourceNotFoundException("Role CUSTOMER not found"));

        AppUser user = new AppUser();
        user.setRole(customerRole);
        user.setUsername(request.username().trim());
        user.setEmail(request.email().trim());
        // The password is never saved as plain text
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        appUserRepository.save(user);

        Customer customer = new Customer();
        customer.setUser(user);
        customer.setFirstName(request.firstName().trim());
        customer.setLastName(request.lastName().trim());
        customer.setPhone(request.phone());
        return toResponse(customerRepository.save(customer));
    }

    @Transactional
    public CustomerResponse updateCustomer(Integer id, CustomerUpdateRequest request) {
        Customer customer = getCustomer(id);
        AppUser user = customer.getUser();

        if (!user.getEmail().equalsIgnoreCase(request.email()) && appUserRepository.existsByEmail(request.email())) {
            throw new ConflictException("The email '" + request.email() + "' is already used");
        }

        user.setEmail(request.email().trim());
        customer.setFirstName(request.firstName().trim());
        customer.setLastName(request.lastName().trim());
        customer.setPhone(request.phone());
        return toResponse(customerRepository.save(customer));
    }

    @Transactional
    public void deleteCustomer(Integer id) {
        Customer customer = getCustomer(id);
        // Orders and invoices must be kept, so a customer with orders cannot be deleted
        if (orderRepository.existsByCustomerId(id)) {
            throw new ConflictException("The customer has orders and cannot be deleted");
        }
        AppUser user = customer.getUser();
        addressRepository.deleteByCustomerId(id);
        customerRepository.delete(customer);
        appUserRepository.delete(user);
    }

    private Customer getCustomer(Integer id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer " + id + " not found"));
    }

    private Customer getByUsername(String username) {
        return customerRepository.findByUserUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found for user " + username));
    }

    private CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getUser().getUsername(),
                customer.getUser().getEmail(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getPhone());
    }
}
