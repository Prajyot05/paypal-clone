"use client";

import { useEffect, useState } from "react";
import { useAuth } from "@/context/AuthContext";
import { api } from "@/lib/api";
import { generateUUID } from "@/lib/utils";
import { useRouter } from "next/navigation";
import { Send, Search, CheckCircle2, XCircle, Loader2 } from "lucide-react";

export default function SendMoneyPage() {
  const { user, token, loading: authLoading } = useAuth();
  const router = useRouter();

  const [users, setUsers] = useState<any[]>([]);
  const [search, setSearch] = useState("");
  const [selectedUser, setSelectedUser] = useState<any | null>(null);
  const [amount, setAmount] = useState("");
  const [note, setNote] = useState("");
  
  const [txState, setTxState] = useState<"IDLE" | "SENDING" | "PENDING" | "SUCCESS" | "FAILED">("IDLE");
  const [txId, setTxId] = useState<number | null>(null);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!authLoading && !user) router.push("/auth");
  }, [user, authLoading, router]);

  useEffect(() => {
    if (token) fetchUsers();
  }, [token]);

  const fetchUsers = async () => {
    try {
      const data = await api.get("/api/users/all", token!);
      setUsers(data.filter((u: any) => u.id !== user?.id));
    } catch (err) {
      console.error(err);
    }
  };

  const filteredUsers = users.filter((u) => 
    u.name.toLowerCase().includes(search.toLowerCase()) || 
    u.email.toLowerCase().includes(search.toLowerCase())
  );

  const handleSend = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedUser || !amount || isNaN(Number(amount)) || Number(amount) <= 0) return;
    
    setError("");
    setTxState("SENDING");
    
    // Generate Idempotency Key - This is the core showcase of the project!
    const idempotencyKey = generateUUID();

    try {
      // 1. Initiate Transaction (Returns PENDING state via Saga orchestrator)
      const txn = await api.post("/api/transactions/create", {
        senderId: user?.id,
        receiverId: selectedUser.id,
        amount: Number(amount)
      }, token!, idempotencyKey);

      setTxId(txn.id);
      setTxState("PENDING");

      // 2. Poll for Status Update (Demonstrates eventual consistency via Kafka)
      pollTransactionStatus(txn.id);
    } catch (err: any) {
      setError(err.message || "Failed to initiate transfer");
      setTxState("IDLE");
    }
  };

  const pollTransactionStatus = async (id: number) => {
    const maxAttempts = 20; // 20 seconds
    let attempts = 0;

    const interval = setInterval(async () => {
      attempts++;
      try {
        const txn = await api.get(`/api/transactions/${id}`, token!);
        if (txn.status === "SUCCESS") {
          setTxState("SUCCESS");
          clearInterval(interval);
        } else if (txn.status === "FAILED") {
          setTxState("FAILED");
          setError(txn.failureReason || "Insufficient balance or network error");
          clearInterval(interval);
        }
        
        if (attempts >= maxAttempts) {
          clearInterval(interval);
          setTxState("FAILED");
          setError("Transaction timed out. Please check your ledger later.");
        }
      } catch (err) {
        console.error("Polling error", err);
      }
    }, 1000); // Poll every 1 second
  };

  if (authLoading) return null;

  return (
    <div className="container mx-auto max-w-3xl px-4 py-8">
      <h1 className="text-3xl font-bold tracking-tight mb-8">Send Money</h1>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
        
        {/* Left Col: Recipient Selection */}
        <div className="space-y-4">
          <h2 className="text-lg font-semibold border-b pb-2">1. Select Recipient</h2>
          <div className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
            <input 
              type="text" 
              placeholder="Search by name or email..." 
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="w-full pl-9 pr-4 py-2 rounded-md bg-card border border-input focus:ring-2 focus:ring-blue-500"
            />
          </div>
          
          <div className="border border-border rounded-lg overflow-hidden bg-card max-h-[400px] overflow-y-auto">
            {filteredUsers.length === 0 ? (
              <div className="p-4 text-center text-sm text-muted-foreground">No users found.</div>
            ) : (
              <ul className="divide-y divide-border">
                {filteredUsers.map((u) => (
                  <li 
                    key={u.id}
                    onClick={() => setSelectedUser(u)}
                    className={`p-3 cursor-pointer hover:bg-secondary/50 transition-colors flex items-center space-x-3 ${selectedUser?.id === u.id ? 'bg-blue-500/10 border-l-4 border-l-blue-500' : 'border-l-4 border-l-transparent'}`}
                  >
                    <div className="h-10 w-10 rounded-full bg-secondary flex items-center justify-center font-bold text-muted-foreground">
                      {u.name.charAt(0).toUpperCase()}
                    </div>
                    <div>
                      <p className="font-medium text-sm">{u.name}</p>
                      <p className="text-xs text-muted-foreground">{u.email}</p>
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </div>

        {/* Right Col: Amount & Status */}
        <div className="space-y-4">
          <h2 className="text-lg font-semibold border-b pb-2">2. Transfer Details</h2>
          
          <div className="bg-card border border-border rounded-xl p-6 shadow-sm">
            {txState === "IDLE" || txState === "SENDING" ? (
              <form onSubmit={handleSend} className="space-y-6">
                {selectedUser ? (
                  <div className="p-3 bg-secondary/50 rounded-lg flex items-center space-x-3">
                    <div className="h-10 w-10 rounded-full bg-blue-500 text-white flex items-center justify-center font-bold">
                      {selectedUser.name.charAt(0).toUpperCase()}
                    </div>
                    <div>
                      <p className="text-xs text-muted-foreground">Sending to</p>
                      <p className="font-medium">{selectedUser.name}</p>
                    </div>
                  </div>
                ) : (
                  <div className="p-3 bg-secondary/30 rounded-lg text-sm text-muted-foreground text-center border border-dashed border-border">
                    Select a recipient first
                  </div>
                )}

                <div className="space-y-2">
                  <label className="text-sm font-medium">Amount (USD)</label>
                  <div className="relative">
                    <span className="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground font-medium">$</span>
                    <input 
                      type="number" 
                      min="0.01" step="0.01"
                      required
                      disabled={!selectedUser || txState === "SENDING"}
                      value={amount}
                      onChange={(e) => setAmount(e.target.value)}
                      className="w-full pl-7 pr-4 py-3 text-lg font-semibold rounded-md bg-background border border-input focus:ring-2 focus:ring-blue-500"
                      placeholder="0.00"
                    />
                  </div>
                </div>

                {error && (
                  <div className="p-3 rounded-md bg-destructive/10 text-destructive text-sm border border-destructive/20">
                    {error}
                  </div>
                )}

                <button 
                  type="submit" 
                  disabled={!selectedUser || !amount || txState === "SENDING"}
                  className="w-full py-3 bg-blue-600 hover:bg-blue-700 disabled:opacity-50 disabled:hover:bg-blue-600 text-white rounded-md font-medium flex items-center justify-center transition-colors"
                >
                  {txState === "SENDING" ? (
                    <><Loader2 className="mr-2 h-5 w-5 animate-spin" /> Initiating...</>
                  ) : (
                    <><Send className="mr-2 h-4 w-4" /> Send Payment</>
                  )}
                </button>
              </form>
            ) : (
              /* Transaction Status View */
              <div className="py-8 flex flex-col items-center justify-center text-center space-y-4">
                {txState === "PENDING" && (
                  <>
                    <div className="relative">
                      <div className="h-16 w-16 rounded-full border-4 border-blue-100 border-t-blue-600 animate-spin"></div>
                      <div className="absolute inset-0 flex items-center justify-center text-blue-600 font-bold">...</div>
                    </div>
                    <h3 className="text-xl font-bold">Processing</h3>
                    <p className="text-sm text-muted-foreground max-w-xs">
                      Waiting for Kafka Saga confirmation from the Wallet Service...
                    </p>
                  </>
                )}
                
                {txState === "SUCCESS" && (
                  <>
                    <CheckCircle2 className="h-16 w-16 text-green-500" />
                    <h3 className="text-xl font-bold text-green-500">Sent Successfully!</h3>
                    <p className="text-sm text-muted-foreground">
                      ${Number(amount).toFixed(2)} has been sent to {selectedUser?.name}.
                    </p>
                    <button 
                      onClick={() => { setTxState("IDLE"); setAmount(""); }}
                      className="mt-4 px-6 py-2 bg-secondary hover:bg-secondary/80 rounded-full text-sm font-medium transition-colors"
                    >
                      Send Another
                    </button>
                  </>
                )}

                {txState === "FAILED" && (
                  <>
                    <XCircle className="h-16 w-16 text-red-500" />
                    <h3 className="text-xl font-bold text-red-500">Transaction Failed</h3>
                    <p className="text-sm text-muted-foreground max-w-xs">
                      {error}
                    </p>
                    <button 
                      onClick={() => setTxState("IDLE")}
                      className="mt-4 px-6 py-2 bg-secondary hover:bg-secondary/80 rounded-full text-sm font-medium transition-colors"
                    >
                      Try Again
                    </button>
                  </>
                )}
              </div>
            )}
            
            {/* Educational Tooltip */}
            <div className="mt-8 pt-4 border-t border-border/50">
              <p className="text-[10px] uppercase font-bold tracking-wider text-muted-foreground mb-1">Architecture Note</p>
              <p className="text-xs text-muted-foreground leading-relaxed">
                This page generates a unique UUID on mount and sends it as an <code>Idempotency-Key</code> header to the Transaction Service. The Transaction Service uses a Redis lock to ensure this exact request is only ever processed once, even if you double-click. It then fires a Kafka event and enters a <code>PENDING</code> state until the Wallet Service completes the transfer and responds.
              </p>
            </div>
          </div>
        </div>

      </div>
    </div>
  );
}
