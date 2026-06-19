"use client";

import { useEffect, useState } from "react";
import { useAuth } from "@/context/AuthContext";
import { api } from "@/lib/api";
import { useRouter } from "next/navigation";
import { BookOpen, Search } from "lucide-react";

export default function LedgerPage() {
  const { user, token, loading: authLoading } = useAuth();
  const router = useRouter();

  const [ledger, setLedger] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!authLoading && !user) router.push("/auth");
  }, [user, authLoading, router]);

  useEffect(() => {
    if (token && user) {
      fetchLedger();
    }
  }, [token, user]);

  const fetchLedger = async () => {
    try {
      const data = await api.get(`/api/wallets/${user!.id}/ledger`, token!);
      setLedger(data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  if (authLoading || loading) return <div className="flex items-center justify-center min-h-[50vh]"><div className="animate-spin rounded-sm h-8 w-8 border-b-2 border-blue-600"></div></div>;

  return (
    <div className="container mx-auto max-w-5xl px-4 py-8 space-y-6">
      <div className="flex flex-col md:flex-row md:items-end justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight flex items-center">
            <BookOpen className="mr-3 h-8 w-8 text-blue-600" /> Wallet Ledger
          </h1>
          <p className="text-muted-foreground mt-1">Immutable double-entry bookkeeping records.</p>
        </div>
        <div className="relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <input 
            type="text" 
            placeholder="Search reference IDs..." 
            className="w-full md:w-64 pl-9 pr-4 py-2 rounded-sm bg-card border border-input focus:ring-2 focus:ring-blue-500 text-sm"
          />
        </div>
      </div>

      <div className="bg-card border border-border rounded-sm shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm text-left">
            <thead className="bg-secondary/50 text-muted-foreground uppercase text-xs">
              <tr>
                <th className="px-6 py-4 font-medium tracking-wider">Date & Time</th>
                <th className="px-6 py-4 font-medium tracking-wider">Type</th>
                <th className="px-6 py-4 font-medium tracking-wider">Amount</th>
                <th className="px-6 py-4 font-medium tracking-wider">Balance After</th>
                <th className="px-6 py-4 font-medium tracking-wider">Reference / Description</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
              {ledger.length === 0 ? (
                <tr>
                  <td colSpan={5} className="px-6 py-8 text-center text-muted-foreground">
                    No ledger entries found.
                  </td>
                </tr>
              ) : (
                ledger.map((entry) => (
                  <tr key={entry.id} className="hover:bg-muted/50 transition-colors">
                    <td className="px-6 py-4 whitespace-nowrap text-muted-foreground">
                      {new Date(entry.timestamp).toLocaleString()}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`px-2.5 py-1 rounded-sm text-xs font-bold tracking-wider ${
                        entry.type === 'CREDIT' ? 'bg-green-500/10 text-green-500' : 'bg-red-500/10 text-red-500'
                      }`}>
                        {entry.type}
                      </span>
                    </td>
                    <td className={`px-6 py-4 whitespace-nowrap font-medium ${entry.type === 'CREDIT' ? 'text-green-500' : ''}`}>
                      {entry.type === 'CREDIT' ? '+' : '-'}${entry.amount.toFixed(2)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap font-mono">
                      ${entry.balanceAfter.toFixed(2)}
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex flex-col">
                        <span className="font-medium text-foreground">{entry.description}</span>
                        <span className="text-xs text-muted-foreground font-mono mt-0.5">Ref: {entry.referenceId}</span>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
