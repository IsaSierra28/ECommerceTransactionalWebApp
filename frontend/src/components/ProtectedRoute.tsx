import type { ReactNode } from "react";
import { Alert, Container, Spinner } from "react-bootstrap";
import { Navigate } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";
import type { Role } from "../types";

interface ProtectedRouteProps {
  roles?: Role[];
  children: ReactNode;
}

// Shows the page only if the user is logged in and has one of the roles.
// This only hides pages in the browser; the real security is in the backend (Spring Security).
function ProtectedRoute({ roles, children }: ProtectedRouteProps) {
  const { user, loading } = useAuth();

  if (loading) {
    return <Container className="text-center mt-5"><Spinner animation="border" /></Container>;
  }
  if (!user) {
    return <Navigate to="/login" replace />;
  }
  if (roles && !roles.includes(user.role)) {
    return (
      <Container>
        <Alert variant="danger">403 - Forbidden: your role ({user.role}) cannot open this page.</Alert>
      </Container>
    );
  }
  return <>{children}</>;
}

export default ProtectedRoute;