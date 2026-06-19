const API_BASE_URL = "http://localhost:8080";

export const api = {
  get: async (endpoint: string, token?: string) => {
    const headers: Record<string, string> = { "Content-Type": "application/json" };
    if (token) headers["Authorization"] = `Bearer ${token}`;

    const res = await fetch(`${API_BASE_URL}${endpoint}`, { headers });
    if (!res.ok) throw new Error(await res.text() || res.statusText);
    return res.json();
  },
  post: async (endpoint: string, body: any, token?: string, idempotencyKey?: string) => {
    const headers: Record<string, string> = { "Content-Type": "application/json" };
    if (token) headers["Authorization"] = `Bearer ${token}`;
    if (idempotencyKey) headers["Idempotency-Key"] = idempotencyKey;

    const res = await fetch(`${API_BASE_URL}${endpoint}`, {
      method: "POST",
      headers,
      body: JSON.stringify(body),
    });
    
    // For /auth/signup, it returns plain text, so we handle it
    const text = await res.text();
    if (!res.ok) throw new Error(text || res.statusText);
    
    try {
      return JSON.parse(text);
    } catch {
      return text;
    }
  }
};
