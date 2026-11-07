import { useState } from "react";
import type { ChangeEvent, FormEvent } from "react";
import { Alert, Button, Form, Modal } from "react-bootstrap";
import { ApiException, createCustomer, createVendor, updateCustomer, updateVendor } from "../services/api";
import type { Customer, Vendor } from "../types";
import { hasErrors, validateAccount } from "../validation/validators";
import type { AccountForm, FormErrors } from "../validation/validators";

interface AccountFormModalProps {
  type: "VENDOR" | "CUSTOMER";
  account?: Vendor | Customer | null;   // null or missing = create, otherwise = edit
  onClose: () => void;
  onSaved: () => void;
}

function toForm(account?: Vendor | Customer | null): AccountForm {
  return {
    username: account?.username ?? "",
    email: account?.email ?? "",
    password: "",
    companyName: account && "companyName" in account ? account.companyName : "",
    firstName: account && "firstName" in account ? account.firstName : "",
    lastName: account && "lastName" in account ? account.lastName : "",
    phone: account?.phone ?? "",
  };
}

// Form used by the ADMIN to create or edit a vendor or a customer.
// Create: also creates the user account (username + password).
// Edit: only the email, the name/company and the phone can be changed.
function AccountFormModal({ type, account, onClose, onSaved }: AccountFormModalProps) {
  const isEdit = !!account;
  const [form, setForm] = useState<AccountForm>(toForm(account));
  const [errors, setErrors] = useState<FormErrors>({});
  const [serverError, setServerError] = useState("");
  const [saving, setSaving] = useState(false);

  const handleChange = (e: ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const validationErrors = validateAccount(form, type, isEdit);
    setErrors(validationErrors);
    setServerError("");
    if (hasErrors(validationErrors)) return;

    const details = type === "VENDOR"
      ? { companyName: form.companyName.trim() }
      : { firstName: form.firstName.trim(), lastName: form.lastName.trim() };
    const data = { ...details, email: form.email.trim(), phone: form.phone.trim() || null };

    setSaving(true);
    try {
      if (isEdit && account) {
        if (type === "VENDOR") await updateVendor(account.id, data);
        else await updateCustomer(account.id, data);
      } else {
        const newAccount = { ...data, username: form.username.trim(), password: form.password };
        if (type === "VENDOR") await createVendor(newAccount);
        else await createCustomer(newAccount);
      }
      onSaved();
    } catch (error) {
      if (error instanceof ApiException) {
        setServerError(error.message);
        if (error.fieldErrors) setErrors(error.fieldErrors);
      }
    } finally {
      setSaving(false);
    }
  };

  const input = (name: keyof AccountForm, label: string, inputType = "text") => (
    <Form.Group className="mb-3" controlId={name}>
      <Form.Label>{label}</Form.Label>
      <Form.Control type={inputType} name={name} value={form[name]} onChange={handleChange}
                    isInvalid={!!errors[name]} />
      <Form.Control.Feedback type="invalid">{errors[name]}</Form.Control.Feedback>
    </Form.Group>
  );

  const title = `${isEdit ? "Edit" : "New"} ${type === "VENDOR" ? "vendor" : "customer"}`;

  return (
    <Modal show onHide={onClose}>
      <Form noValidate onSubmit={handleSubmit}>
        <Modal.Header closeButton>
          <Modal.Title>{title}</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {serverError && <Alert variant="danger">{serverError}</Alert>}
          {type === "VENDOR"
            ? input("companyName", "Company name")
            : <>{input("firstName", "First name")}{input("lastName", "Last name")}</>}
          {isEdit
            ? <p className="text-muted small">Username: <strong>{account?.username}</strong></p>
            : input("username", "Username")}
          {input("email", "Email", "email")}
          {!isEdit && input("password", "Password", "password")}
          {input("phone", "Phone (optional)")}
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={onClose}>Cancel</Button>
          <Button type="submit" disabled={saving}>{saving ? "Saving..." : "Save"}</Button>
        </Modal.Footer>
      </Form>
    </Modal>
  );
}

export default AccountFormModal;
