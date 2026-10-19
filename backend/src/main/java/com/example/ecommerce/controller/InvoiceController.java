package com.example.ecommerce.controller;

import com.example.ecommerce.dto.ErrorResponse;
import com.example.ecommerce.dto.InvoiceResponse;
import com.example.ecommerce.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/invoices")
@Tag(name = "Invoices", description = "ADMIN sees all invoices, CUSTOMER sees their own invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping
    @Operation(summary = "List invoices")
    public List<InvoiceResponse> getInvoices(Principal principal) {
        return invoiceService.findInvoices(principal.getName());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one invoice")
    @ApiResponse(responseCode = "403", description = "The invoice belongs to another customer",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public InvoiceResponse getInvoice(@PathVariable Integer id, Principal principal) {
        return invoiceService.findInvoice(id, principal.getName());
    }
}
