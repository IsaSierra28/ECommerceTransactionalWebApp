package com.example.ecommerce.service;

import com.example.ecommerce.dto.InvoiceResponse;
import com.example.ecommerce.exception.ForbiddenOperationException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.model.Customer;
import com.example.ecommerce.model.Invoice;
import com.example.ecommerce.repository.CustomerRepository;
import com.example.ecommerce.repository.InvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final UserService userService;

    public InvoiceService(InvoiceRepository invoiceRepository,
                          CustomerRepository customerRepository,
                          UserService userService) {
        this.invoiceRepository = invoiceRepository;
        this.customerRepository = customerRepository;
        this.userService = userService;
    }

    /**
     * ADMIN sees all invoices, CUSTOMER sees only their own invoices.
     */
    @Transactional(readOnly = true)
    public List<InvoiceResponse> findInvoices(String username) {
        String role = userService.getRole(username);
        if (UserService.ADMIN.equals(role)) {
            return invoiceRepository.findAll().stream().map(InvoiceService::toResponse).toList();
        }
        if (UserService.CUSTOMER.equals(role)) {
            Customer customer = getCustomer(username);
            return invoiceRepository.findByOrderCustomerIdOrderByIssueDateDesc(customer.getId()).stream()
                    .map(InvoiceService::toResponse).toList();
        }
        throw new ForbiddenOperationException("Only admins and customers can see invoices");
    }

    @Transactional(readOnly = true)
    public InvoiceResponse findInvoice(Integer id, String username) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice " + id + " not found"));

        String role = userService.getRole(username);
        if (UserService.ADMIN.equals(role)) {
            return toResponse(invoice);
        }
        if (UserService.CUSTOMER.equals(role)
                && invoice.getOrder().getCustomer().getId().equals(getCustomer(username).getId())) {
            return toResponse(invoice);
        }
        throw new ForbiddenOperationException("You cannot see this invoice");
    }

    private Customer getCustomer(String username) {
        return customerRepository.findByUserUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found"));
    }

    static InvoiceResponse toResponse(Invoice invoice) {
        Customer customer = invoice.getOrder().getCustomer();
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getIssueDate(),
                invoice.getTotal(),
                invoice.getOrder().getId(),
                customer.getFirstName() + " " + customer.getLastName());
    }
}
