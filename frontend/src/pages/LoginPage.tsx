import { useState } from "react";
import type { FormEvent } from "react";
import { Alert, Button, Card, Container, Form } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";
import { ApiException } from "../services/api";
import { hasErrors, validateLogin } from "../validation/validators";
import type { FormErrors } from "../validation/validators";

function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();

  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [errors, setErrors] = useState<FormErrors>({});
  const [serverError, setServerError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault(); // do not reload the page

    // 1. Frontend validation
    const validationErrors = validateLogin(username, password);
    setErrors(validationErrors);
    setServerError("");
    if (hasErrors(validationErrors)) return;

    // 2. Ask the API if the username and password are correct
    setSubmitting(true);
    try {
      await login(username, password);
      navigate("/products");
    } catch (error) {
      if (error instanceof ApiException && error.status === 401) {
        setServerError("Wrong username or password");
      } else {
        setServerError(error instanceof Error ? error.message : "Login failed");
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Container className="d-flex justify-content-center mt-5">
      <Card style={{ width: "24rem" }} className="shadow-sm">
        <Card.Body>
          <Card.Title as="h2" className="mb-4 text-center">Login</Card.Title>

          {serverError && <Alert variant="danger">{serverError}</Alert>}

          <Form noValidate onSubmit={handleSubmit}>
            <Form.Group className="mb-3" controlId="username">
              <Form.Label>Username</Form.Label>
              <Form.Control
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                isInvalid={!!errors.username}
              />
              <Form.Control.Feedback type="invalid">{errors.username}</Form.Control.Feedback>
            </Form.Group>

            <Form.Group className="mb-4" controlId="password">
              <Form.Label>Password</Form.Label>
              <Form.Control
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                isInvalid={!!errors.password}
              />
              <Form.Control.Feedback type="invalid">{errors.password}</Form.Control.Feedback>
            </Form.Group>

            <Button type="submit" className="w-100" disabled={submitting}>
              {submitting ? "Logging in..." : "Login"}
            </Button>
          </Form>

          <p className="text-muted small mt-4 mb-0">
            Test users (password <code>Password123!</code>): admin, techstore, maria
          </p>
        </Card.Body>
      </Card>
    </Container>
  );
}

export default LoginPage;