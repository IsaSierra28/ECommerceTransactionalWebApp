import { useState } from "react";
import type { ChangeEvent, FormEvent } from "react";
import { Alert, Button, Form, Modal } from "react-bootstrap";
import { ApiException, createCustomer, createVendor } from "../services/api";
import { hasErrors, validateAccount } from "../validation/validators";
import type { AccountForm, FormErrors } from "../validation/validators";

interface AccountFormModalProps {
  type: "VENDOR" | "CUSTOMER";
  onClose: () => void;
  onSaved: () => void;
}

const emptyForm: AccountForm = {
  username: "", email: "", password: "", companyName: "", firstName: "", lastName: "", phone: "",
};

// Form used by the ADMIN to create a vendor or a customer (with their user account)
function AccountFormModal({ type, onClose, onSaved }: AccountFormModalProps) {
  const [form, setForm] = useState<AccountForm>(emptyForm);
  const [errors, setErrors] = useState<FormErrors>({});
  const [serverError, setServerError] = useState("");
  const [saving, setSaving] = useState(false);

  const handleChange = (e: ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const validationErrors = validateAccount(form, type);
    setErrors(validationErrors);
    setServerError("");
    if (hasErrors(validationErrors)) return;

    const common = {
      username: form.username.trim(),
      email: form.email.trim(),
      password: form.password,
      phone: form.phone.trim() || null,
    };

    setSaving(true);
    try {
      if (type === "VENDOR") {
        await createVendor({ ...common, companyName: form.companyName.trim() });
      } else {
        await createCustomer({ ...common, firstName: form.firstName.trim(), lastName: form.lastName.trim() });
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

  return (
    <Modal show onHide={onClose}>
      <Form noValidate onSubmit={handleSubmit}>
        <Modal.Header closeButton>
          <Modal.Title>{type === "VENDOR" ? "New vendor" : "New customer"}</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {serverError && <Alert variant="danger">{serverError}</Alert>}
          {type === "VENDOR"
            ? input("companyName", "Company name")
            : <>{input("firstName", "First name")}{input("lastName", "Last name")}</>}
          {input("username", "Username")}
          {input("email", "Email", "email")}
          {input("password", "Password", "password")}
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