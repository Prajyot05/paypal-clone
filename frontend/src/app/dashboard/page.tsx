"use client";

import { useEffect, useState } from "react";
import { useAuth } from "@/context/AuthContext";
import { api } from "@/lib/api";
import { useRouter } from "next/navigation";
import { ArrowRight, ArrowUpRight, ArrowDownLeft, Wallet, Clock, Activity } from "lucide-react";
import Link from "next/link";

export default function DashboardPage() {
  const { user, token, loading: authLoading } = useAuth();
  const router = useRouter();
  
  const [balance, setBalance] = useState<number | null>(null);
  const [recentTxns, setRecentTxns] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!authLoading && !user) {
      router.push("/auth");
    }
  }, [user, authLoading, router]);

  useEffect(() => {
    if (user && token) {
      fetchDashboardData();
    }
  }, [user, token]);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      const balRes = await api.get(`/api/wallets/${user!.id}/balance`, token!);
      setBalance(balRes);
      
      const txnsRes = await api.get(`/api/transactions/user/${user!.id}`, token!);
      setRecentTxns(txnsRes.slice(0, 5)); // Get top 5
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  if (authLoading || loading) {
    return <div className="flex items-center justify-center min-h-[50vh]"><div className="animate-spin rounded-sm h-8 w-8 border-b-2 border-blue-600"></div></div>;
  }

  return (
    <div className="container mx-auto max-w-5xl px-4 py-8 space-y-8">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Dashboard</h1>
        <p className="text-muted-foreground">Welcome back, {user?.name}. Here's what's happening.</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Balance Card */}
        <div className="col-span-1 md:col-span-2 rounded-sm bg-gradient-to-br from-blue-600 to-blue-800 p-8 text-white shadow-xl relative overflow-hidden">
          <div className="absolute top-0 right-0 w-64 h-64 bg-white/10 rounded-sm blur-3xl -mr-20 -mt-20"></div>
          
          <div className="relative z-10 flex flex-col h-full justify-between">
            <div className="flex items-center space-x-2 opacity-80">
              <Wallet className="h-5 w-5" />
              <span className="font-medium">Total Balance</span>
            </div>
            
            <div className="mt-6 mb-8">
              <span className="text-5xl font-bold tracking-tight">${balance?.toFixed(2) || "0.00"}</span>
              <span className="text-blue-200 ml-2">USD</span>
            </div>
            
            <div className="flex space-x-3">
              <Link href="/send" className="bg-white text-blue-900 px-6 py-2.5 rounded-sm font-medium flex items-center hover:bg-blue-50 transition-colors shadow-sm">
                Send Money <ArrowRight className="ml-2 h-4 w-4" />
              </Link>
            </div>
          </div>
        </div>

        {/* Quick Stats Card */}
        <div className="col-span-1 rounded-sm bg-card border border-border p-6 shadow-sm flex flex-col justify-between">
          <div className="flex items-center space-x-2 text-muted-foreground mb-4">
            <Activity className="h-5 w-5" />
            <span className="font-medium">Account Status</span>
          </div>
          
          <div className="space-y-4">
            <div className="flex justify-between items-center py-2 border-b border-border/50">
              <span className="text-sm">User ID</span>
              <span className="font-mono text-sm">{user?.id}</span>
            </div>
            <div className="flex justify-between items-center py-2 border-b border-border/50">
              <span className="text-sm">Account Tier</span>
              <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-green-500/10 text-green-500">Verified</span>
            </div>
          </div>
        </div>
      </div>

      {/* Recent Transactions */}
      <div className="rounded-sm bg-card border border-border shadow-sm overflow-hidden">
        <div className="p-6 border-b border-border flex justify-between items-center">
          <h2 className="text-xl font-bold flex items-center">
            <Clock className="h-5 w-5 mr-2 text-muted-foreground" /> Recent Transactions
          </h2>
          <Link href="/ledger" className="text-sm text-blue-600 hover:underline">View Ledger</Link>
        </div>
        
        <div className="divide-y divide-border">
          {recentTxns.length === 0 ? (
            <div className="p-8 text-center text-muted-foreground">
              No transactions yet.
            </div>
          ) : (
            recentTxns.map((txn: any) => {
              const isSender = txn.senderId === user?.id;
              return (
                <div key={txn.id} className="p-4 sm:p-6 flex items-center justify-between hover:bg-muted/50 transition-colors">
                  <div className="flex items-center space-x-4">
                    <div className={`h-10 w-10 rounded-sm flex items-center justify-center ${isSender ? 'bg-red-500/10 text-red-500' : 'bg-green-500/10 text-green-500'}`}>
                      {isSender ? <ArrowUpRight className="h-5 w-5" /> : <ArrowDownLeft className="h-5 w-5" />}
                    </div>
                    <div>
                      <p className="font-medium">{isSender ? `Sent to User #${txn.receiverId}` : `Received from User #${txn.senderId}`}</p>
                      <div className="flex items-center space-x-2 text-xs text-muted-foreground mt-0.5">
                        <span>{new Date(txn.timestamp).toLocaleDateString()}</span>
                        <span>•</span>
                        <span className={`px-2 py-0.5 rounded-sm text-[10px] uppercase font-bold tracking-wider ${
                          txn.status === 'SUCCESS' ? 'bg-green-500/10 text-green-500' : 
                          txn.status === 'FAILED' ? 'bg-red-500/10 text-red-500' : 
                          'bg-yellow-500/10 text-yellow-500'
                        }`}>
                          {txn.status}
                        </span>
                      </div>
                    </div>
                  </div>
                  <div className={`text-lg font-semibold ${isSender ? '' : 'text-green-500'}`}>
                    {isSender ? '-' : '+'}${txn.amount.toFixed(2)}
                  </div>
                </div>
              );
            })
          )}
        </div>
      </div>
    </div>
  );
}
