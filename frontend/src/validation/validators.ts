// Frontend validation: checks the user input BEFORE sending it to the API.
// The backend validates everything again, because frontend validation is not enough for security.

export type FormErrors = Record<string, string>;

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const MONEY_REGEX = /^\d+(\.\d{1,2})?$/; // a number with at most 2 decimals

export function hasErrors(errors: FormErrors): boolean {
  return Object.keys(errors).length > 0;
}

// ---------- Login ----------
export function validateLogin(username: string, password: string): FormErrors {
  const errors: FormErrors = {};
  if (!username.trim()) errors.username = "Username is required";
  if (!password) errors.password = "Password is required";
  return errors;
}

// ---------- Product ----------
// The form keeps every value as text, so we can show a clear message if it is not a number
export interface ProductForm {
  vendorId: string;
  categoryId: string;
  sku: string;
  name: string;
  description: string;
  price: string;
  quantity: string;
}

export function validateProduct(form: ProductForm, isAdmin: boolean): FormErrors {
  const errors: FormErrors = {};

  if (isAdmin && !form.vendorId) errors.vendorId = "Vendor is required";
  if (!form.categoryId) errors.categoryId = "Category is required";

  if (!form.sku.trim()) errors.sku = "SKU is required";
  else if (form.sku.length > 30) errors.sku = "SKU must have at most 30 characters";

  if (!form.name.trim()) errors.name = "Product name is required";
  else if (form.name.length > 100) errors.name = "Product name must have at most 100 characters";

  if (form.description.length > 500) errors.description = "Description must have at most 500 characters";

  if (!form.price.trim()) errors.price = "Price is required";
  else if (!MONEY_REGEX.test(form.price)) errors.price = "Price must be a valid number with at most 2 decimals";
  else if (Number(form.price) <= 0) errors.price = "Price must be greater than 0";

  if (!form.quantity.trim()) errors.quantity = "Quantity is required";
  else if (!/^\d+$/.test(form.quantity)) errors.quantity = "Quantity must be a whole number, 0 or more";

  return errors;
}

// ---------- Quantity to buy ----------
export function validateBuyQuantity(value: string): string | null {
  if (!/^\d+$/.test(value) || Number(value) <= 0) return "Quantity must be a whole number greater than 0";
  return null;
}

// ---------- Payment ----------
export interface PaymentForm {
  method: "CREDIT_CARD" | "BANK_TRANSFER";
  amount: string;
  cardHolder: string;
  cardNumber: string;
  expiryDate: string;
  cvv: string;
  accountHolder: string;
  institutionNumber: string;
  transitNumber: string;
  accountNumber: string;
}

export function validatePayment(form: PaymentForm): FormErrors {
  const errors: FormErrors = {};

  if (!form.amount.trim()) errors.amount = "Payment amount is required";
  else if (!MONEY_REGEX.test(form.amount)) errors.amount = "Payment amount must be a valid number with at most 2 decimals";
  else if (Number(form.amount) <= 0) errors.amount = "Payment amount must be greater than 0";

  if (form.method === "CREDIT_CARD") {
    if (!form.cardHolder.trim()) errors.cardHolder = "Card holder is required";
    if (!/^\d{16}$/.test(form.cardNumber.replace(/[\s-]/g, ""))) errors.cardNumber = "Card number must have 16 digits";
    if (!/^(0[1-9]|1[0-2])\/\d{2}$/.test(form.expiryDate)) {
      errors.expiryDate = "Expiry date must have the format MM/YY";
    } else {
      const [month, year] = form.expiryDate.split("/").map(Number);
      const lastDay = new Date(2000 + year, month, 0); // last day of the expiry month
      if (lastDay < new Date()) errors.expiryDate = "The card is expired";
    }
    if (!/^\d{3}$/.test(form.cvv)) errors.cvv = "CVV must have 3 digits";
  } else {
    if (!form.accountHolder.trim()) errors.accountHolder = "Account holder is required";
    if (!/^\d{3}$/.test(form.institutionNumber)) errors.institutionNumber = "Institution number must have 3 digits";
    if (!/^\d{5}$/.test(form.transitNumber)) errors.transitNumber = "Transit number must have 5 digits";
    if (!/^\d{7,12}$/.test(form.accountNumber)) errors.accountNumber = "Account number must have 7 to 12 digits";
  }
  return errors;
}

// ---------- Vendor / Customer accounts ----------
export interface AccountForm {
  username: string;
  email: string;
  password: string;
  companyName: string;
  firstName: string;
  lastName: string;
  phone: string;
}

export function validateAccount(form: AccountForm, type: "VENDOR" | "CUSTOMER"): FormErrors {
  const errors: FormErrors = {};

  if (form.username.trim().length < 3 || form.username.length > 50) {
    errors.username = "Username must have between 3 and 50 characters";
  }
  if (!form.email.trim()) errors.email = "Email is required";
  else if (!EMAIL_REGEX.test(form.email)) errors.email = "Email is not valid";
  if (form.password.length < 8) errors.password = "Password must have at least 8 characters";
  if (form.phone.length > 20) errors.phone = "Phone must have at most 20 characters";

  if (type === "VENDOR") {
    if (!form.companyName.trim()) errors.companyName = "Company name is required";
  } else {
    if (!form.firstName.trim()) errors.firstName = "First name is required";
    if (!form.lastName.trim()) errors.lastName = "Last name is required";
  }
  return errors;
}