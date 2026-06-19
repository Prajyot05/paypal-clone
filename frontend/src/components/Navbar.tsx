"use client";

import Link from "next/link";
import { useAuth } from "@/context/AuthContext";
import { useTheme } from "next-themes";
import { Sun, Moon, LogOut, Wallet } from "lucide-react";
import { useEffect, useState } from "react";

export function Navbar() {
  const { user, logout } = useAuth();
  const { theme, setTheme } = useTheme();
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);

  return (
    <nav className="sticky top-0 z-50 w-full border-b border-border/40 bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div className="container mx-auto flex h-16 items-center justify-between px-4">
        <Link href="/" className="flex items-center space-x-2">
          <Wallet className="h-6 w-6 text-blue-800 dark:text-blue-400" />
          <span className="font-extrabold tracking-tight text-xl">AtlasPay</span>
        </Link>
        
        <div className="flex items-center space-x-6">
          {user ? (
            <>
              <Link href="/dashboard" className="text-sm font-medium hover:text-blue-600 transition-colors">Dashboard</Link>
              <Link href="/send" className="text-sm font-medium hover:text-blue-600 transition-colors">Send Money</Link>
              <Link href="/ledger" className="text-sm font-medium hover:text-blue-600 transition-colors">Ledger</Link>
              <div className="flex items-center space-x-4 ml-4 border-l pl-4 border-border/40">
                <span className="text-sm text-muted-foreground hidden md:inline-block">Hi, {user.name}</span>
                <button onClick={logout} className="text-sm text-red-500 hover:text-red-600 flex items-center transition-colors">
                  <LogOut className="h-4 w-4 mr-1" /> Logout
                </button>
              </div>
            </>
          ) : (
            <Link href="/auth" className="text-sm font-semibold bg-blue-800 dark:bg-blue-600 text-white px-5 py-2 rounded hover:bg-blue-900 transition-colors">
              Log In
            </Link>
          )}

          {mounted && (
            <button
              onClick={() => setTheme(theme === "dark" ? "light" : "dark")}
              className="p-2 rounded hover:bg-secondary/80 transition-colors"
              aria-label="Toggle theme"
            >
              {theme === "dark" ? <Sun className="h-5 w-5" /> : <Moon className="h-5 w-5" />}
            </button>
          )}
        </div>
      </div>
    </nav>
  );
}
