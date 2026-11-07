import type {
  ApiError, Address, Category, Customer, Invoice, Order, OrderRequest,
  Product, ProductRequest, User, Vendor,
} from "../types";

const API_URL = "http://localhost:8080/api";
const AUTH_KEY = "auth";

// Error thrown when the API answers with 400, 401, 403, 404, 409 or 500
export class ApiException extends Error {
  status: number;
  fieldErrors: Record<string, string> | null;

  constructor(error: ApiError) {
    super(error.message);
    this.status = error.status;
    this.fieldErrors = error.fieldErrors;
  }
}

// HTTP Basic: the username and password are sent in every request (encoded in Base64).
// They are kept in sessionStorage, so they are deleted when the browser tab is closed.
function saveCredentials(username: string, password: string) {
  sessionStorage.setItem(AUTH_KEY, btoa(`${username}:${password}`));
}

export function clearCredentials() {
  sessionStorage.removeItem(AUTH_KEY);
}

export function hasCredentials(): boolean {
  return sessionStorage.getItem(AUTH_KEY) !== null;
}

// All the requests go through this function
async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const headers: Record<string, string> = { "Content-Type": "application/json" };
  const credentials = sessionStorage.getItem(AUTH_KEY);
  if (credentials) {
    headers["Authorization"] = `Basic ${credentials}`;
  }

  let response: Response;
  try {
    response = await fetch(API_URL + path, { ...options, headers });
  } catch {
    throw new ApiException({
      status: 0,
      error: "Network error",
      message: "Cannot connect to the server. Is the backend running on port 8080?",
      fieldErrors: null,
    });
  }

  if (response.status === 204) {
    return undefined as T; // 204 No Content (delete)
  }

  const data = await response.json().catch(() => null);
  if (!response.ok) {
    throw new ApiException(data ?? {
      status: response.status,
      error: response.statusText,
      message: "The request failed",
      fieldErrors: null,
    });
  }
  return data as T;
}

const post = (body: unknown): RequestInit => ({ method: "POST", body: JSON.stringify(body) });
const put = (body: unknown): RequestInit => ({ method: "PUT", body: JSON.stringify(body) });

// ---------- Authentication ----------
export async function login(username: string, password: string): Promise<User> {
  saveCredentials(username, password);
  try {
    return await request<User>("/auth/me");
  } catch (error) {
    clearCredentials();
    throw error;
  }
}
export const getCurrentUser = () => request<User>("/auth/me");

// ---------- Products and categories ----------
export const getProducts = () => request<Product[]>("/products");
export const getCategories = () => request<Category[]>("/categories");
export const createProduct = (product: ProductRequest) => request<Product>("/products", post(product));
export const updateProduct = (id: number, product: ProductRequest) => request<Product>(`/products/${id}`, put(product));
export const deleteProduct = (id: number) => request<void>(`/products/${id}`, { method: "DELETE" });

// ---------- Vendors (ADMIN) ----------
export const getVendors = () => request<Vendor[]>("/vendors");
export const createVendor = (vendor: object) => request<Vendor>("/vendors", post(vendor));
export const updateVendor = (id: number, vendor: object) => request<Vendor>(`/vendors/${id}`, put(vendor));
export const activateVendor = (id: number) => request<Vendor>(`/vendors/${id}/activate`, { method: "PATCH" });
export const deactivateVendor = (id: number) => request<Vendor>(`/vendors/${id}/deactivate`, { method: "PATCH" });
export const deleteVendor = (id: number) => request<void>(`/vendors/${id}`, { method: "DELETE" });

// ---------- Customers ----------
export const getCustomers = () => request<Customer[]>("/customers");
export const createCustomer = (customer: object) => request<Customer>("/customers", post(customer));
export const updateCustomer = (id: number, customer: object) => request<Customer>(`/customers/${id}`, put(customer));
export const deleteCustomer = (id: number) => request<void>(`/customers/${id}`, { method: "DELETE" });
export const getMyAddresses = () => request<Address[]>("/customers/me/addresses");

// ---------- Orders and invoices ----------
export const placeOrder = (order: OrderRequest) => request<Order>("/orders", post(order));
export const getOrders = () => request<Order[]>("/orders");
export const getInvoices = () => request<Invoice[]>("/invoices");