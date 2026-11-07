// Types that match the DTOs of the Spring Boot REST API

export type Role = "ADMIN" | "VENDOR" | "CUSTOMER";

export interface User {
  userId: number;
  username: string;
  email: string;
  role: Role;
}

export interface Category {
  id: number;
  name: string;
  description: string | null;
}

export interface Product {
  id: number;
  sku: string;
  name: string;
  description: string | null;
  price: number;
  quantity: number;
  status: "IN_STOCK" | "OUT_OF_STOCK";
  categoryId: number;
  categoryName: string;
  vendorId: number;
  vendorName: string;
}

export interface ProductRequest {
  vendorId?: number | null;
  categoryId: number;
  sku: string;
  name: string;
  description: string;
  price: number;
  quantity: number;
}

export interface Vendor {
  id: number;
  username: string;
  email: string;
  companyName: string;
  phone: string | null;
  active: boolean;
}

export interface Customer {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  phone: string | null;
}

export interface Address {
  id: number;
  street: string;
  city: string;
  province: string;
  postalCode: string;
  country: string;
}

export type PaymentMethod = "CREDIT_CARD" | "BANK_TRANSFER";

export interface PaymentRequest {
  method: PaymentMethod;
  amount: number;
  cardHolder?: string;
  cardNumber?: string;
  expiryDate?: string;
  cvv?: string;
  accountHolder?: string;
  institutionNumber?: string;
  transitNumber?: string;
  accountNumber?: string;
}

export interface OrderRequest {
  addressId: number;
  items: { productId: number; quantity: number }[];
  payment: PaymentRequest;
}

export interface Invoice {
  id: number;
  invoiceNumber: string;
  issueDate: string;
  total: number;
  orderId: number;
  customerName: string;
}

export interface Order {
  id: number;
  orderDate: string;
  status: string;
  total: number;
  customerId: number;
  customerName: string;
  shippingAddress: string;
  items: { productId: number; productName: string; quantity: number; unitPrice: number; lineTotal: number }[];
  invoice: Invoice | null;
  payments: { id: number; paymentMethod: string; amount: number; paymentDate: string }[];
}

// Error JSON returned by GlobalExceptionHandler in the backend
export interface ApiError {
  status: number;
  error: string;
  message: string;
  fieldErrors: Record<string, string> | null;
}

// One line of the shopping cart (only in the frontend)
export interface CartItem {
  product: Product;
  quantity: number;
}