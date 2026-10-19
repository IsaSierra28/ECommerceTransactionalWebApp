package com.example.ecommerce.service;

import com.example.ecommerce.dto.PaymentRequest;
import com.example.ecommerce.exception.InsufficientPaymentException;
import com.example.ecommerce.exception.PaymentFailedException;
import com.example.ecommerce.model.CustomerOrder;
import com.example.ecommerce.model.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Implementation 2 of PaymentService: payment by BANK TRANSFER (Canadian bank account).
 *
 * Business rules (different from the credit card):
 *  - The account holder is required.
 *  - Institution number: 3 digits (identifies the bank).
 *  - Transit number: 5 digits (identifies the branch).
 *  - Account number: between 7 and 12 digits.
 *  - There is no maximum amount, so big orders can be paid with a bank transfer.
 *  - The amount must cover the order total.
 */
@Service("bankTransferPaymentService")
public class BankTransferPaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(BankTransferPaymentServiceImpl.class);

    @Override
    public Payment processPayment(CustomerOrder order, BigDecimal amountDue, PaymentRequest request) {
        log.info("Processing BANK TRANSFER payment of {} for an order of {}", request.amount(), amountDue);

        if (request.accountHolder() == null || request.accountHolder().isBlank()) {
            throw new PaymentFailedException("The account holder name is required");
        }
        if (request.institutionNumber() == null || !request.institutionNumber().matches("\\d{3}")) {
            throw new PaymentFailedException("The institution number must have 3 digits");
        }
        if (request.transitNumber() == null || !request.transitNumber().matches("\\d{5}")) {
            throw new PaymentFailedException("The transit number must have 5 digits");
        }
        if (request.accountNumber() == null || !request.accountNumber().matches("\\d{7,12}")) {
            throw new PaymentFailedException("The account number must have between 7 and 12 digits");
        }

        if (request.amount().compareTo(amountDue) < 0) {
            throw new InsufficientPaymentException("Insufficient payment: the order total is " + amountDue
                    + " but the bank transfer is " + request.amount());
        }

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod("BANK_TRANSFER");
        payment.setAmount(amountDue);
        String account = request.accountNumber();
        log.info("Bank transfer approved (account ending in {})", account.substring(account.length() - 3));
        return payment;
    }
}
