import { createContext, useContext, useEffect, useState } from "react";
import type { ReactNode } from "react";
import type { User } from "../types";
import * as api from "../services/api";

interface AuthContextValue {
  user: User | null;
  loading: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

// Wraps the whole app, so every component can know who is logged in
export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(api.hasCredentials());

  // If the page is reloaded, we ask the API again who is logged in
  useEffect(() => {
    if (api.hasCredentials()) {
      api.getCurrentUser()
        .then(setUser)
        .catch(() => api.clearCredentials())
        .finally(() => setLoading(false));
    }
  }, []);

  const login = async (username: string, password: string) => {
    const loggedUser = await api.login(username, password);
    setUser(loggedUser);
  };

  const logout = () => {
    api.clearCredentials();
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

// Custom hook: const { user, login, logout } = useAuth();
export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used inside AuthProvider");
  }
  return context;
}