import { useState } from "react";
import type { ChangeEvent, FormEvent } from "react";
import { Alert, Button, Col, Form, Modal, Row } from "react-bootstrap";
import type { Category, Product, Vendor } from "../types";
import { ApiException, createProduct, updateProduct } from "../services/api";
import { hasErrors, validateProduct } from "../validation/validators";
import type { FormErrors, ProductForm } from "../validation/validators";

interface ProductFormModalProps {
  product: Product | null;      // null = create a new product
  categories: Category[];
  vendors: Vendor[];            // only used by the ADMIN
  isAdmin: boolean;
  onClose: () => void;
  onSaved: () => void;
}

function toForm(product: Product | null): ProductForm {
  return {
    vendorId: product ? String(product.vendorId) : "",
    categoryId: product ? String(product.categoryId) : "",
    sku: product?.sku ?? "",
    name: product?.name ?? "",
    description: product?.description ?? "",
    price: product ? String(product.price) : "",
    quantity: product ? String(product.quantity) : "",
  };
}

// Form to create or edit a product, with frontend validation
function ProductFormModal({ product, categories, vendors, isAdmin, onClose, onSaved }: ProductFormModalProps) {
  const [form, setForm] = useState<ProductForm>(toForm(product));
  const [errors, setErrors] = useState<FormErrors>({});
  const [serverError, setServerError] = useState("");
  const [saving, setSaving] = useState(false);

  // One function for every input: it uses the "name" of the input
  const handleChange = (e: ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    setForm((prevForm) => ({ ...prevForm, [e.target.name]: e.target.value }));
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    // 1. Frontend validation
    const validationErrors = validateProduct(form, isAdmin);
    setErrors(validationErrors);
    setServerError("");
    if (hasErrors(validationErrors)) return;

    // 2. Send to the API (the backend validates again)
    const request = {
      vendorId: isAdmin ? Number(form.vendorId) : null,
      categoryId: Number(form.categoryId),
      sku: form.sku.trim(),
      name: form.name.trim(),
      description: form.description.trim(),
      price: Number(form.price),
      quantity: Number(form.quantity),
    };

    setSaving(true);
    try {
      if (product) {
        await updateProduct(product.id, request);
      } else {
        await createProduct(request);
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

  return (
    <Modal show onHide={onClose} size="lg">
      <Form noValidate onSubmit={handleSubmit}>
        <Modal.Header closeButton>
          <Modal.Title>{product ? "Edit product" : "New product"}</Modal.Title>
        </Modal.Header>

        <Modal.Body>
          {serverError && <Alert variant="danger">{serverError}</Alert>}

          <Row>
            {isAdmin && (
              <Form.Group as={Col} md={6} className="mb-3" controlId="vendorId">
                <Form.Label>Vendor</Form.Label>
                <Form.Select name="vendorId" value={form.vendorId} onChange={handleChange} isInvalid={!!errors.vendorId}>
                  <option value="">-- Select a vendor --</option>
                  {vendors.map((v) => <option key={v.id} value={v.id}>{v.companyName}</option>)}
                </Form.Select>
                <Form.Control.Feedback type="invalid">{errors.vendorId}</Form.Control.Feedback>
              </Form.Group>
            )}

            <Form.Group as={Col} md={6} className="mb-3" controlId="categoryId">
              <Form.Label>Category</Form.Label>
              <Form.Select name="categoryId" value={form.categoryId} onChange={handleChange} isInvalid={!!errors.categoryId}>
                <option value="">-- Select a category --</option>
                {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
              </Form.Select>
              <Form.Control.Feedback type="invalid">{errors.categoryId}</Form.Control.Feedback>
            </Form.Group>
          </Row>

          <Row>
            <Form.Group as={Col} md={4} className="mb-3" controlId="sku">
              <Form.Label>SKU</Form.Label>
              <Form.Control name="sku" value={form.sku} onChange={handleChange} isInvalid={!!errors.sku} />
              <Form.Control.Feedback type="invalid">{errors.sku}</Form.Control.Feedback>
            </Form.Group>

            <Form.Group as={Col} md={8} className="mb-3" controlId="name">
              <Form.Label>Product name</Form.Label>
              <Form.Control name="name" value={form.name} onChange={handleChange} isInvalid={!!errors.name} />
              <Form.Control.Feedback type="invalid">{errors.name}</Form.Control.Feedback>
            </Form.Group>
          </Row>

          <Form.Group className="mb-3" controlId="description">
            <Form.Label>Description</Form.Label>
            <Form.Control as="textarea" rows={2} name="description" value={form.description}
                          onChange={handleChange} isInvalid={!!errors.description} />
            <Form.Control.Feedback type="invalid">{errors.description}</Form.Control.Feedback>
          </Form.Group>

          <Row>
            <Form.Group as={Col} md={6} className="mb-3" controlId="price">
              <Form.Label>Price ($)</Form.Label>
              <Form.Control name="price" value={form.price} onChange={handleChange} isInvalid={!!errors.price} />
              <Form.Control.Feedback type="invalid">{errors.price}</Form.Control.Feedback>
            </Form.Group>

            <Form.Group as={Col} md={6} className="mb-3" controlId="quantity">
              <Form.Label>Quantity in stock</Form.Label>
              <Form.Control name="quantity" value={form.quantity} onChange={handleChange} isInvalid={!!errors.quantity} />
              <Form.Control.Feedback type="invalid">{errors.quantity}</Form.Control.Feedback>
            </Form.Group>
          </Row>
        </Modal.Body>

        <Modal.Footer>
          <Button variant="secondary" onClick={onClose}>Cancel</Button>
          <Button type="submit" disabled={saving}>{saving ? "Saving..." : "Save"}</Button>
        </Modal.Footer>
      </Form>
    </Modal>
  );
}

export default ProductFormModal;