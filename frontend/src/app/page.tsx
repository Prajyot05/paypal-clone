import Link from "next/link";
import { ArrowRight, ShieldCheck, Zap, Globe } from "lucide-react";

export default function Home() {
  return (
    <div className="flex flex-col items-center justify-center min-h-[calc(100vh-4rem)] bg-[radial-gradient(ellipse_at_top,_var(--tw-gradient-stops))] from-blue-900/20 via-background to-background">
      
      <div className="text-center space-y-6 max-w-4xl px-4 py-20 relative z-10">
        <div className="inline-flex items-center rounded-full border border-blue-500/30 bg-blue-500/10 px-3 py-1 text-sm text-blue-500 mb-4 backdrop-blur-sm">
          <span className="flex h-2 w-2 rounded-full bg-blue-500 mr-2 animate-pulse"></span>
          Java 21 • Spring Boot • Microservices
        </div>
        
        <h1 className="text-5xl md:text-7xl font-extrabold tracking-tight bg-clip-text text-transparent bg-gradient-to-r from-blue-500 via-indigo-500 to-purple-500">
          The Future of Payments.
        </h1>
        
        <p className="text-xl text-muted-foreground max-w-2xl mx-auto leading-relaxed">
          A production-ready distributed system showcasing eventual consistency, idempotency, and saga patterns. Built for high availability.
        </p>
        
        <div className="flex flex-col sm:flex-row items-center justify-center gap-4 pt-8">
          <Link href="/auth" className="flex items-center justify-center w-full sm:w-auto px-8 py-3 rounded-full bg-blue-600 text-white font-medium hover:bg-blue-700 transition-all shadow-lg shadow-blue-500/25 hover:scale-105">
            Get Started <ArrowRight className="ml-2 h-4 w-4" />
          </Link>
          <a href="https://github.com/Prajyot05/paypal-clone" target="_blank" rel="noreferrer" className="flex items-center justify-center w-full sm:w-auto px-8 py-3 rounded-full bg-secondary text-secondary-foreground font-medium hover:bg-secondary/80 transition-all hover:scale-105">
            View Source
          </a>
        </div>
      </div>

      {/* Feature Grid */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 max-w-6xl w-full px-4 mt-12 pb-20">
        <div className="p-6 rounded-2xl bg-card border border-border/50 shadow-sm backdrop-blur-sm hover:border-blue-500/50 transition-colors">
          <ShieldCheck className="h-10 w-10 text-blue-500 mb-4" />
          <h3 className="text-xl font-bold mb-2">Absolute Idempotency</h3>
          <p className="text-muted-foreground text-sm">Redis-backed idempotency keys guarantee no double-charges, even if you spam the send button.</p>
        </div>
        
        <div className="p-6 rounded-2xl bg-card border border-border/50 shadow-sm backdrop-blur-sm hover:border-purple-500/50 transition-colors">
          <Zap className="h-10 w-10 text-purple-500 mb-4" />
          <h3 className="text-xl font-bold mb-2">Saga Orchestration</h3>
          <p className="text-muted-foreground text-sm">Distributed transactions handled gracefully via Kafka. True eventual consistency without 2PC deadlocks.</p>
        </div>

        <div className="p-6 rounded-2xl bg-card border border-border/50 shadow-sm backdrop-blur-sm hover:border-indigo-500/50 transition-colors">
          <Globe className="h-10 w-10 text-indigo-500 mb-4" />
          <h3 className="text-xl font-bold mb-2">Microservice Scale</h3>
          <p className="text-muted-foreground text-sm">Independent Postgres databases per service, Spring Cloud Gateway, and Java 21 Virtual Threads.</p>
        </div>
      </div>
    </div>
  );
}
