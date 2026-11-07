import { Card, Table } from "react-bootstrap";
import type { Order } from "../types";

// Shows an order with its invoice and payment
function InvoiceCard({ order }: { order: Order }) {
  return (
    <Card className="shadow-sm">
      <Card.Header className="d-flex justify-content-between">
        <strong>Invoice {order.invoice?.invoiceNumber ?? "-"}</strong>
        <span>Order #{order.id} · {new Date(order.orderDate).toLocaleString()}</span>
      </Card.Header>
      <Card.Body>
        <p className="mb-1"><strong>Customer:</strong> {order.customerName}</p>
        <p><strong>Ship to:</strong> {order.shippingAddress}</p>

        <Table size="sm" responsive>
          <thead>
            <tr><th>Product</th><th>Quantity</th><th>Unit price</th><th>Line total</th></tr>
          </thead>
          <tbody>
            {order.items.map((item) => (
              <tr key={item.productId}>
                <td>{item.productName}</td>
                <td>{item.quantity}</td>
                <td>${item.unitPrice.toFixed(2)}</td>
                <td>${item.lineTotal.toFixed(2)}</td>
              </tr>
            ))}
          </tbody>
        </Table>

        <div className="d-flex justify-content-between">
          <span>
            {order.payments.map((p) => (
              <span key={p.id}>Paid by {p.paymentMethod.replace("_", " ").toLowerCase()}: ${p.amount.toFixed(2)}</span>
            ))}
          </span>
          <h5>Total: ${order.total.toFixed(2)}</h5>
        </div>
      </Card.Body>
    </Card>
  );
}

export default InvoiceCard;