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
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Implementation 1 of PaymentService: payment by CREDIT CARD.
 *
 * Business rules:
 *  - The card holder is required.
 *  - The card number must have 16 digits and pass the Luhn check (the algorithm used by real cards).
 *  - The expiry date (MM/YY) must not be in the past.
 *  - The CVV must have 3 digits.
 *  - A credit card payment cannot be more than 5,000.00 (card limit).
 *  - The amount must cover the order total.
 */
@Service("creditCardPaymentService")
public class CreditCardPaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(CreditCardPaymentServiceImpl.class);
    private static final BigDecimal CARD_LIMIT = new BigDecimal("5000.00");

    @Override
    public Payment processPayment(CustomerOrder order, BigDecimal amountDue, PaymentRequest request) {
        log.info("Processing CREDIT CARD payment of {} for an order of {}", request.amount(), amountDue);

        if (isBlank(request.cardHolder())) {
            throw new PaymentFailedException("The card holder name is required");
        }

        String cardNumber = request.cardNumber() == null ? "" : request.cardNumber().replaceAll("[\\s-]", "");
        if (!cardNumber.matches("\\d{16}") || !passesLuhnCheck(cardNumber)) {
            throw new PaymentFailedException("The credit card number is not valid");
        }

        if (isExpired(request.expiryDate())) {
            throw new PaymentFailedException("The credit card is expired or the expiry date is not valid (use MM/YY)");
        }

        if (request.cvv() == null || !request.cvv().matches("\\d{3}")) {
            throw new PaymentFailedException("The CVV must have 3 digits");
        }

        if (amountDue.compareTo(CARD_LIMIT) > 0) {
            throw new PaymentFailedException("Credit card payments cannot be more than " + CARD_LIMIT
                    + ". Please use a bank transfer.");
        }

        if (request.amount().compareTo(amountDue) < 0) {
            throw new InsufficientPaymentException("Insufficient payment: the order total is " + amountDue
                    + " but the payment is " + request.amount());
        }

        // The card is charged exactly the order total
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod("CREDIT_CARD");
        payment.setAmount(amountDue);
        log.info("Credit card payment approved (card ending in {})", cardNumber.substring(12));
        return payment;
    }

    /**
     * Luhn algorithm: checks that the card number is well formed.
     */
    private boolean passesLuhnCheck(String number) {
        int sum = 0;
        boolean doubleDigit = false;
        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = number.charAt(i) - '0';
            if (doubleDigit) {
                digit = digit * 2;
                if (digit > 9) {
                    digit = digit - 9;
                }
            }
            sum += digit;
            doubleDigit = !doubleDigit;
        }
        return sum % 10 == 0;
    }

    private boolean isExpired(String expiryDate) {
        if (expiryDate == null) {
            return true;
        }
        try {
            YearMonth expiry = YearMonth.parse(expiryDate.trim(), DateTimeFormatter.ofPattern("MM/yy"));
            return expiry.isBefore(YearMonth.now());
        } catch (DateTimeParseException e) {
            return true;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
