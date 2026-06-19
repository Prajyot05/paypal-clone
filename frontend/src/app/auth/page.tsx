"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/context/AuthContext";
import { api } from "@/lib/api";
import { Wallet, Loader2 } from "lucide-react";

export default function AuthPage() {
  const [isLogin, setIsLogin] = useState(true);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [name, setName] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  
  const router = useRouter();
  const { login } = useAuth();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      if (isLogin) {
        const res = await api.post("/auth/login", { email, password });
        if (res.token) {
          login(res.token);
          router.push("/dashboard");
        } else {
          setError("Failed to get token");
        }
      } else {
        await api.post("/auth/signup", { name, email, password });
        // Automatically login after signup
        const res = await api.post("/auth/login", { email, password });
        if (res.token) {
          login(res.token);
          router.push("/dashboard");
        }
      }
    } catch (err: any) {
      setError(err.message || "An error occurred");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex min-h-[calc(100vh-4rem)] items-center justify-center p-4 relative overflow-hidden bg-background">
      {/* Decorative background elements */}
      <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[600px] bg-blue-600/20 rounded-sm blur-[100px] -z-10 pointer-events-none"></div>
      
      <div className="w-full max-w-md p-8 rounded-sm bg-card border border-border shadow-xl backdrop-blur-sm relative z-10">
        <div className="flex flex-col items-center mb-8">
          <div className="h-12 w-12 bg-blue-600/10 rounded-sm flex items-center justify-center mb-4 border border-blue-600/20">
            <Wallet className="h-6 w-6 text-blue-600" />
          </div>
          <h2 className="text-2xl font-bold">{isLogin ? "Welcome back" : "Create an account"}</h2>
          <p className="text-muted-foreground text-sm mt-1">
            {isLogin ? "Enter your details to access your wallet" : "Start sending money with AtlasPay"}
          </p>
        </div>

        {error && (
          <div className="mb-6 p-3 rounded-sm bg-destructive/10 border border-destructive/20 text-destructive text-sm text-center">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          {!isLogin && (
            <div className="space-y-2">
              <label className="text-sm font-medium">Full Name</label>
              <input
                type="text"
                required
                value={name}
                onChange={(e) => setName(e.target.value)}
                className="w-full px-3 py-2 rounded-sm bg-background border border-input focus:outline-none focus:ring-2 focus:ring-blue-500 transition-shadow"
                placeholder="John Doe"
              />
            </div>
          )}
          <div className="space-y-2">
            <label className="text-sm font-medium">Email Address</label>
            <input
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full px-3 py-2 rounded-sm bg-background border border-input focus:outline-none focus:ring-2 focus:ring-blue-500 transition-shadow"
              placeholder="john@example.com"
            />
          </div>
          <div className="space-y-2">
            <label className="text-sm font-medium">Password</label>
            <input
              type="password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full px-3 py-2 rounded-sm bg-background border border-input focus:outline-none focus:ring-2 focus:ring-blue-500 transition-shadow"
              placeholder="••••••••"
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full py-2.5 px-4 bg-blue-600 hover:bg-blue-700 text-white rounded-sm font-medium transition-colors flex items-center justify-center mt-6 shadow-sm"
          >
            {loading ? <Loader2 className="h-5 w-5 animate-spin" /> : (isLogin ? "Sign In" : "Sign Up")}
          </button>
        </form>

        <div className="mt-6 text-center text-sm">
          <span className="text-muted-foreground">
            {isLogin ? "Don't have an account? " : "Already have an account? "}
          </span>
          <button
            type="button"
            onClick={() => {
              setIsLogin(!isLogin);
              setError("");
            }}
            className="text-blue-600 hover:underline font-medium"
          >
            {isLogin ? "Sign up" : "Log in"}
          </button>
        </div>
      </div>
    </div>
  );
}
