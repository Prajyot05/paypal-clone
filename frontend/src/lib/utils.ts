import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

// Generate random UUID for Idempotency Key
export function generateUUID() {
  return crypto.randomUUID();
}
