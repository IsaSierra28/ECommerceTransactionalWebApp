import { useEffect, useState } from "react";
import { Alert, Container, Spinner } from "react-bootstrap";
import { useAuth } from "../hooks/useAuth";
import { getOrders } from "../services/api";
import type { Order } from "../types";
import InvoiceCard from "../components/InvoiceCard";

// ADMIN sees all orders, VENDOR the orders with their products, CUSTOMER their own orders.
// The backend decides which orders to return.
function OrdersPage() {
  const { user } = useAuth();
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    getOrders()
      .then(setOrders)
      .catch((e: Error) => setError(e.message))
      .finally(() => setLoading(false));
  }, []);

  const title = user?.role === "CUSTOMER" ? "My orders and invoices" : "Orders";

  return (
    <Container>
      <h1 className="mb-3">{title}</h1>
      {loading && <Spinner animation="border" />}
      {error && <Alert variant="danger">{error}</Alert>}
      {!loading && !error && orders.length === 0 && <Alert variant="info">There are no orders yet.</Alert>}

      <div className="d-flex flex-column gap-3">
        {orders.map((order) => <InvoiceCard key={order.id} order={order} />)}
      </div>
    </Container>
  );
}

export default OrdersPage;