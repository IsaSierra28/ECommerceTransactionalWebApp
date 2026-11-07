import { useEffect, useState } from "react";
import type { ChangeEvent, FormEvent } from "react";
import { Alert, Button, Card, Col, Container, Form, Row, Table } from "react-bootstrap";
import { Link } from "react-router-dom";
import { useCart } from "../hooks/useCart";
import { ApiException, getMyAddresses, placeOrder } from "../services/api";
import type { Address, Order, PaymentMethod } from "../types";
import { hasErrors, validatePayment } from "../validation/validators";
import type { FormErrors, PaymentForm } from "../validation/validators";
import InvoiceCard from "../components/InvoiceCard";

const emptyPayment: PaymentForm = {
  method: "CREDIT_CARD",
  amount: "",
  cardHolder: "",
  cardNumber: "",
  expiryDate: "",
  cvv: "",
  accountHolder: "",
  institutionNumber: "",
  transitNumber: "",
  accountNumber: "",
};

// The purchase: cart + shipping address + payment -> POST /api/orders
function CheckoutPage() {
  const { items, total, removeFromCart, clearCart } = useCart();

  const [addresses, setAddresses] = useState<Address[]>([]);
  const [addressId, setAddressId] = useState("");
  const [payment, setPayment] = useState<PaymentForm>(emptyPayment);
  const [errors, setErrors] = useState<FormErrors>({});
  const [submitting, setSubmitting] = useState(false);

  // Result of the transaction
  const [order, setOrder] = useState<Order | null>(null);
  const [rollbackMessage, setRollbackMessage] = useState("");

  useEffect(() => {
    getMyAddresses()
      .then((list) => {
        setAddresses(list);
        if (list.length > 0) setAddressId(String(list[0].id));
      })
      .catch(() => setAddresses([]));
  }, []);

  const handleChange = (e: ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    setPayment((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setRollbackMessage("");

    // 1. Frontend validation
    const validationErrors = validatePayment(payment);
    if (!addressId) validationErrors.addressId = "Shipping address is required";
    setErrors(validationErrors);
    if (hasErrors(validationErrors)) return;

    // 2. Send the order. The backend runs the whole purchase in ONE transaction.
    setSubmitting(true);
    try {
      const result = await placeOrder({
        addressId: Number(addressId),
        items: items.map((item) => ({ productId: item.product.id, quantity: item.quantity })),
        payment: {
          method: payment.method as PaymentMethod,
          amount: Number(payment.amount),
          ...(payment.method === "CREDIT_CARD"
            ? {
                cardHolder: payment.cardHolder,
                cardNumber: payment.cardNumber,
                expiryDate: payment.expiryDate,
                cvv: payment.cvv,
              }
            : {
                accountHolder: payment.accountHolder,
                institutionNumber: payment.institutionNumber,
                transitNumber: payment.transitNumber,
                accountNumber: payment.accountNumber,
              }),
        },
      });
      // COMMIT: everything was saved
      setOrder(result);
      clearCart();
      setPayment(emptyPayment);
    } catch (error) {
      // ROLLBACK: nothing was saved
      if (error instanceof ApiException) {
        setRollbackMessage(error.message);
        if (error.fieldErrors) setErrors(error.fieldErrors);
      } else {
        setRollbackMessage("The purchase failed");
      }
    } finally {
      setSubmitting(false);
    }
  };

  // ---------- Successful transaction ----------
  if (order) {
    return (
      <Container>
        <Alert variant="success">
          <Alert.Heading>Purchase completed (COMMIT)</Alert.Heading>
          The order, the order items, the inventory update, the invoice and the payment were saved.
        </Alert>
        <InvoiceCard order={order} />
        <div className="mt-3 d-flex gap-2">
          <Link className="btn btn-primary" to="/products">Continue shopping</Link>
          <Link className="btn btn-outline-primary" to="/orders">See my orders</Link>
        </div>
      </Container>
    );
  }

  // ---------- Empty cart ----------
  if (items.length === 0) {
    return (
      <Container>
        <h1>Cart</h1>
        {rollbackMessage && <RollbackAlert message={rollbackMessage} />}
        <Alert variant="info">Your cart is empty. <Link to="/products">Go to products</Link></Alert>
      </Container>
    );
  }

  const isCard = payment.method === "CREDIT_CARD";

  return (
    <Container>
      <h1 className="mb-3">Cart and payment</h1>

      {rollbackMessage && <RollbackAlert message={rollbackMessage} />}

      <Row className="g-4">
        <Col lg={6}>
          <Card className="shadow-sm">
            <Card.Body>
              <Card.Title>1. Products</Card.Title>
              <Table size="sm" responsive>
                <thead>
                  <tr><th>Product</th><th>Qty</th><th>Price</th><th>Total</th><th></th></tr>
                </thead>
                <tbody>
                  {items.map((item) => (
                    <tr key={item.product.id}>
                      <td>
                        {item.product.name}
                        {item.quantity > item.product.quantity && (
                          <div className="text-warning small">Only {item.product.quantity} in stock</div>
                        )}
                      </td>
                      <td>{item.quantity}</td>
                      <td>${item.product.price.toFixed(2)}</td>
                      <td>${(item.product.price * item.quantity).toFixed(2)}</td>
                      <td>
                        <Button size="sm" variant="link" className="text-danger p-0"
                                onClick={() => removeFromCart(item.product.id)}>Remove</Button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </Table>
              <h4 className="text-end">Order total: ${total.toFixed(2)}</h4>
            </Card.Body>
          </Card>
        </Col>

        <Col lg={6}>
          <Card className="shadow-sm">
            <Card.Body>
              <Form noValidate onSubmit={handleSubmit}>
                <Card.Title>2. Shipping address</Card.Title>
                <Form.Group className="mb-4" controlId="addressId">
                  <Form.Select value={addressId} onChange={(e) => setAddressId(e.target.value)}
                               isInvalid={!!errors.addressId}>
                    <option value="">-- Select an address --</option>
                    {addresses.map((a) => (
                      <option key={a.id} value={a.id}>{a.street}, {a.city}, {a.province} {a.postalCode}</option>
                    ))}
                  </Form.Select>
                  <Form.Control.Feedback type="invalid">{errors.addressId}</Form.Control.Feedback>
                </Form.Group>

                <Card.Title>3. Payment</Card.Title>
                <Form.Group className="mb-3">
                  <Form.Check inline type="radio" id="credit" name="method" value="CREDIT_CARD"
                              label="Credit card" checked={isCard} onChange={handleChange} />
                  <Form.Check inline type="radio" id="bank" name="method" value="BANK_TRANSFER"
                              label="Bank transfer" checked={!isCard} onChange={handleChange} />
                </Form.Group>

                <Form.Group className="mb-3" controlId="amount">
                  <Form.Label>Payment amount ($)</Form.Label>
                  <Form.Control name="amount" value={payment.amount} onChange={handleChange}
                                placeholder={total.toFixed(2)} isInvalid={!!errors.amount} />
                  <Form.Control.Feedback type="invalid">{errors.amount}</Form.Control.Feedback>
                </Form.Group>

                {isCard ? (
                  <>
                    <Field name="cardHolder" label="Card holder" form={payment} errors={errors} onChange={handleChange} />
                    <Field name="cardNumber" label="Card number" form={payment} errors={errors} onChange={handleChange}
                           placeholder="4111 1111 1111 1111" />
                    <Row>
                      <Col><Field name="expiryDate" label="Expiry (MM/YY)" form={payment} errors={errors}
                                  onChange={handleChange} placeholder="12/30" /></Col>
                      <Col><Field name="cvv" label="CVV" form={payment} errors={errors}
                                  onChange={handleChange} placeholder="123" /></Col>
                    </Row>
                  </>
                ) : (
                  <>
                    <Field name="accountHolder" label="Account holder" form={payment} errors={errors} onChange={handleChange} />
                    <Row>
                      <Col><Field name="institutionNumber" label="Institution" form={payment} errors={errors}
                                  onChange={handleChange} placeholder="001" /></Col>
                      <Col><Field name="transitNumber" label="Transit" form={payment} errors={errors}
                                  onChange={handleChange} placeholder="12345" /></Col>
                    </Row>
                    <Field name="accountNumber" label="Account number" form={payment} errors={errors}
                           onChange={handleChange} placeholder="1234567" />
                  </>
                )}

                <Button type="submit" className="w-100" disabled={submitting}>
                  {submitting ? "Processing..." : `Place order ($${total.toFixed(2)})`}
                </Button>
              </Form>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </Container>
  );
}

// Shown when the backend rejects the purchase and makes a ROLLBACK
function RollbackAlert({ message }: { message: string }) {
  return (
    <Alert variant="danger">
      <Alert.Heading>Purchase rejected (ROLLBACK)</Alert.Heading>
      <p className="mb-1">{message}</p>
      <small>Nothing was saved: no order, no invoice, no payment and the inventory did not change.</small>
    </Alert>
  );
}

// Small reusable input with its error message
interface FieldProps {
  name: keyof PaymentForm;
  label: string;
  form: PaymentForm;
  errors: FormErrors;
  placeholder?: string;
  onChange: (e: ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => void;
}

function Field({ name, label, form, errors, placeholder, onChange }: FieldProps) {
  return (
    <Form.Group className="mb-3" controlId={name}>
      <Form.Label>{label}</Form.Label>
      <Form.Control name={name} value={form[name]} onChange={onChange}
                    placeholder={placeholder} isInvalid={!!errors[name]} />
      <Form.Control.Feedback type="invalid">{errors[name]}</Form.Control.Feedback>
    </Form.Group>
  );
}

export default CheckoutPage;