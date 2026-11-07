import { Badge, Button, Container, Nav, Navbar } from "react-bootstrap";
import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";
import { useCart } from "../hooks/useCart";

// The menu changes with the role of the logged user
function AppNavbar() {
  const { user, logout } = useAuth();
  const { items, clearCart } = useCart();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    clearCart();
    navigate("/login");
  };

  return (
    <Navbar bg="dark" data-bs-theme="dark" expand="md" className="mb-4">
      <Container>
        <Navbar.Brand as={NavLink} to="/products">E-Commerce Store</Navbar.Brand>
        <Navbar.Toggle aria-controls="main-nav" />
        <Navbar.Collapse id="main-nav">
          <Nav className="me-auto">
            <Nav.Link as={NavLink} to="/products">Products</Nav.Link>

            {user?.role === "CUSTOMER" && (
              <Nav.Link as={NavLink} to="/checkout">
                Cart <Badge bg="primary">{items.length}</Badge>
              </Nav.Link>
            )}
            {user && <Nav.Link as={NavLink} to="/orders">Orders</Nav.Link>}
            {user?.role === "ADMIN" && (
              <>
                <Nav.Link as={NavLink} to="/vendors">Vendors</Nav.Link>
                <Nav.Link as={NavLink} to="/customers">Customers</Nav.Link>
              </>
            )}
          </Nav>

          {user ? (
            <div className="d-flex align-items-center gap-3">
              <span className="text-light small">
                {user.username} <Badge bg="secondary">{user.role}</Badge>
              </span>
              <Button size="sm" variant="outline-light" onClick={handleLogout}>Logout</Button>
            </div>
          ) : (
            <Nav.Link as={NavLink} to="/login" className="text-light">Login</Nav.Link>
          )}
        </Navbar.Collapse>
      </Container>
    </Navbar>
  );
}

export default AppNavbar;