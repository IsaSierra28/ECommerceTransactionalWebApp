import { useEffect, useState } from "react";
import { Alert, Badge, Button, Card, Col, Container, Form, Row, Spinner } from "react-bootstrap";
import { useAuth } from "../hooks/useAuth";
import { useCart } from "../hooks/useCart";
import { deleteProduct, getCategories, getProducts, getVendors } from "../services/api";
import type { Category, Product, Vendor } from "../types";
import { validateBuyQuantity } from "../validation/validators";
import ProductFormModal from "../components/ProductFormModal";

function ProductsPage() {
  const { user } = useAuth();
  const { addToCart } = useCart();

  const [products, setProducts] = useState<Product[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [vendors, setVendors] = useState<Vendor[]>([]);
  const [categoryFilter, setCategoryFilter] = useState("");
  const [quantities, setQuantities] = useState<Record<number, string>>({});
  const [quantityErrors, setQuantityErrors] = useState<Record<number, string>>({});
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState<{ type: "success" | "danger"; text: string } | null>(null);

  // Product form: undefined = closed, null = new product, Product = edit
  const [editing, setEditing] = useState<Product | null | undefined>(undefined);

  const isAdmin = user?.role === "ADMIN";
  const isVendor = user?.role === "VENDOR";
  const isCustomer = user?.role === "CUSTOMER";

  const loadProducts = () => {
    getProducts()
      .then(setProducts)
      .catch((error: Error) => setMessage({ type: "danger", text: error.message }))
      .finally(() => setLoading(false));
  };

  // Runs when the page opens
  useEffect(() => {
    loadProducts();
    getCategories().then(setCategories).catch(() => setCategories([]));
  }, []);

  // The admin needs the list of vendors in the product form
  useEffect(() => {
    if (isAdmin) getVendors().then(setVendors).catch(() => setVendors([]));
  }, [isAdmin]);

  const handleAddToCart = (product: Product) => {
    const value = quantities[product.id] ?? "1";
    const error = validateBuyQuantity(value);
    setQuantityErrors((prev) => ({ ...prev, [product.id]: error ?? "" }));
    if (error) return;

    addToCart(product, Number(value));
    setMessage({ type: "success", text: `${value} x ${product.name} added to the cart` });
  };

  const handleDelete = async (product: Product) => {
    if (!window.confirm(`Delete "${product.name}"?`)) return;
    try {
      await deleteProduct(product.id);
      setMessage({ type: "success", text: `"${product.name}" was deleted` });
      loadProducts();
    } catch (error) {
      setMessage({ type: "danger", text: (error as Error).message });
    }
  };

  const visibleProducts = categoryFilter
    ? products.filter((p) => p.categoryId === Number(categoryFilter))
    : products;

  if (loading) {
    return <Container className="text-center mt-5"><Spinner animation="border" /></Container>;
  }

  return (
    <Container>
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h1>Products</h1>
        {(isAdmin || isVendor) && (
          <Button onClick={() => setEditing(null)}>+ New product</Button>
        )}
      </div>

      {message && (
        <Alert variant={message.type} dismissible onClose={() => setMessage(null)}>{message.text}</Alert>
      )}

      <Form.Select className="mb-4" style={{ maxWidth: "20rem" }}
                   value={categoryFilter} onChange={(e) => setCategoryFilter(e.target.value)}>
        <option value="">All categories</option>
        {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
      </Form.Select>

      <Row xs={1} md={2} lg={3} className="g-4">
        {visibleProducts.map((product) => (
          <Col key={product.id}>
            <Card className="h-100 shadow-sm">
              <Card.Body>
                <div className="d-flex justify-content-between">
                  <Card.Title>{product.name}</Card.Title>
                  <Badge bg={product.status === "IN_STOCK" ? "success" : "secondary"}>
                    {product.status === "IN_STOCK" ? "In stock" : "Out of stock"}
                  </Badge>
                </div>
                <Card.Subtitle className="mb-2 text-muted">
                  {product.categoryName} · {product.vendorName}
                </Card.Subtitle>
                <Card.Text>{product.description}</Card.Text>
                <h4>${product.price.toFixed(2)}</h4>
                <small className="text-muted">SKU {product.sku} · {product.quantity} available</small>
              </Card.Body>

              <Card.Footer className="bg-white">
                {isCustomer && (
                  <div className="d-flex gap-2 align-items-start">
                    <Form.Group style={{ width: "6rem" }}>
                      <Form.Control
                        size="sm"
                        value={quantities[product.id] ?? "1"}
                        onChange={(e) => setQuantities((prev) => ({ ...prev, [product.id]: e.target.value }))}
                        isInvalid={!!quantityErrors[product.id]}
                        aria-label="Quantity"
                      />
                    </Form.Group>
                    <Button size="sm" disabled={product.quantity === 0} onClick={() => handleAddToCart(product)}>
                      Add to cart
                    </Button>
                  </div>
                )}
                {isCustomer && quantityErrors[product.id] && (
                  <small className="text-danger">{quantityErrors[product.id]}</small>
                )}

                {(isAdmin || isVendor) && (
                  <div className="d-flex gap-2">
                    <Button size="sm" variant="outline-primary" onClick={() => setEditing(product)}>Edit</Button>
                    <Button size="sm" variant="outline-danger" onClick={() => handleDelete(product)}>Delete</Button>
                  </div>
                )}

                {!user && <small className="text-muted">Log in as a customer to buy</small>}
              </Card.Footer>
            </Card>
          </Col>
        ))}
      </Row>

      {editing !== undefined && (
        <ProductFormModal
          product={editing}
          categories={categories}
          vendors={vendors}
          isAdmin={isAdmin}
          onClose={() => setEditing(undefined)}
          onSaved={() => {
            setEditing(undefined);
            setMessage({ type: "success", text: "The product was saved" });
            loadProducts();
          }}
        />
      )}
    </Container>
  );
}

export default ProductsPage;