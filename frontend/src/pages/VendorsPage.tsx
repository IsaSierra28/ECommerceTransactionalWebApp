import { useEffect, useState } from "react";
import { Alert, Badge, Button, Container, Table } from "react-bootstrap";
import { activateVendor, deactivateVendor, deleteVendor, getVendors } from "../services/api";
import type { Vendor } from "../types";
import AccountFormModal from "../components/AccountFormModal";

// ADMIN: list, create, activate/deactivate and delete vendors
function VendorsPage() {
  const [vendors, setVendors] = useState<Vendor[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [message, setMessage] = useState<{ type: "success" | "danger"; text: string } | null>(null);

  const loadVendors = () => {
    getVendors().then(setVendors).catch((e: Error) => setMessage({ type: "danger", text: e.message }));
  };

  useEffect(() => {
    loadVendors();
  }, []);

  const toggleActive = async (vendor: Vendor) => {
    try {
      if (vendor.active) await deactivateVendor(vendor.id);
      else await activateVendor(vendor.id);
      setMessage({ type: "success", text: `${vendor.companyName} was ${vendor.active ? "deactivated" : "activated"}` });
      loadVendors();
    } catch (e) {
      setMessage({ type: "danger", text: (e as Error).message });
    }
  };

  const handleDelete = async (vendor: Vendor) => {
    if (!window.confirm(`Delete ${vendor.companyName}?`)) return;
    try {
      await deleteVendor(vendor.id);
      setMessage({ type: "success", text: `${vendor.companyName} was deleted` });
      loadVendors();
    } catch (e) {
      setMessage({ type: "danger", text: (e as Error).message });
    }
  };

  return (
    <Container>
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h1>Vendors</h1>
        <Button onClick={() => setShowForm(true)}>+ New vendor</Button>
      </div>

      {message && <Alert variant={message.type} dismissible onClose={() => setMessage(null)}>{message.text}</Alert>}

      <Table striped bordered hover responsive className="bg-white">
        <thead>
          <tr><th>Company</th><th>Username</th><th>Email</th><th>Phone</th><th>Status</th><th>Actions</th></tr>
        </thead>
        <tbody>
          {vendors.map((v) => (
            <tr key={v.id}>
              <td>{v.companyName}</td>
              <td>{v.username}</td>
              <td>{v.email}</td>
              <td>{v.phone ?? "-"}</td>
              <td><Badge bg={v.active ? "success" : "secondary"}>{v.active ? "Active" : "Inactive"}</Badge></td>
              <td className="d-flex gap-2">
                <Button size="sm" variant={v.active ? "outline-warning" : "outline-success"} onClick={() => toggleActive(v)}>
                  {v.active ? "Deactivate" : "Activate"}
                </Button>
                <Button size="sm" variant="outline-danger" onClick={() => handleDelete(v)}>Delete</Button>
              </td>
            </tr>
          ))}
        </tbody>
      </Table>

      {showForm && (
        <AccountFormModal
          type="VENDOR"
          onClose={() => setShowForm(false)}
          onSaved={() => {
            setShowForm(false);
            setMessage({ type: "success", text: "The vendor was created" });
            loadVendors();
          }}
        />
      )}
    </Container>
  );
}

export default VendorsPage;