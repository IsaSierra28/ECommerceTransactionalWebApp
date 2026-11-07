import { useEffect, useState } from "react";
import { Alert, Button, Container, Table } from "react-bootstrap";
import { deleteCustomer, getCustomers } from "../services/api";
import type { Customer } from "../types";
import AccountFormModal from "../components/AccountFormModal";

// ADMIN: list, create and delete customers
function CustomersPage() {
  const [customers, setCustomers] = useState<Customer[]>([]);
  // Form: undefined = closed, null = new customer, Customer = edit
  const [editing, setEditing] = useState<Customer | null | undefined>(undefined);
  const [message, setMessage] = useState<{ type: "success" | "danger"; text: string } | null>(null);

  const loadCustomers = () => {
    getCustomers().then(setCustomers).catch((e: Error) => setMessage({ type: "danger", text: e.message }));
  };

  useEffect(() => {
    loadCustomers();
  }, []);

  const handleDelete = async (customer: Customer) => {
    if (!window.confirm(`Delete ${customer.firstName} ${customer.lastName}?`)) return;
    try {
      await deleteCustomer(customer.id);
      setMessage({ type: "success", text: `${customer.firstName} ${customer.lastName} was deleted` });
      loadCustomers();
    } catch (e) {
      setMessage({ type: "danger", text: (e as Error).message });
    }
  };

  return (
    <Container>
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h1>Customers</h1>
        <Button onClick={() => setEditing(null)}>+ New customer</Button>
      </div>

      {message && <Alert variant={message.type} dismissible onClose={() => setMessage(null)}>{message.text}</Alert>}

      <Table striped bordered hover responsive className="bg-white">
        <thead>
          <tr><th>Name</th><th>Username</th><th>Email</th><th>Phone</th><th>Actions</th></tr>
        </thead>
        <tbody>
          {customers.map((c) => (
            <tr key={c.id}>
              <td>{c.firstName} {c.lastName}</td>
              <td>{c.username}</td>
              <td>{c.email}</td>
              <td>{c.phone ?? "-"}</td>
              <td className="d-flex gap-2">
                <Button size="sm" variant="outline-primary" onClick={() => setEditing(c)}>Edit</Button>
                <Button size="sm" variant="outline-danger" onClick={() => handleDelete(c)}>Delete</Button>
              </td>
            </tr>
          ))}
        </tbody>
      </Table>

      {editing !== undefined && (
        <AccountFormModal
          type="CUSTOMER"
          account={editing}
          onClose={() => setEditing(undefined)}
          onSaved={() => {
            setMessage({ type: "success", text: `The customer was ${editing ? "updated" : "created"}` });
            setEditing(undefined);
            loadCustomers();
          }}
        />
      )}
    </Container>
  );
}

export default CustomersPage;