import Link from "next/link";
import { ArrowRight, ShieldCheck, Zap, Globe } from "lucide-react";

export default function Home() {
  return (
    <div className="flex flex-col items-center min-h-[calc(100vh-4rem)] bg-background">
      
      {/* Hero Section */}
      <div className="w-full max-w-6xl px-4 py-24 md:py-32 flex flex-col items-start border-b border-border/40">
        <div className="inline-flex items-center border border-border bg-secondary/20 px-3 py-1 text-xs font-mono text-muted-foreground mb-6 rounded-sm">
          <span className="flex h-2 w-2 bg-blue-600 mr-2"></span>
          System Status: Operational
        </div>
        
        <h1 className="text-4xl md:text-6xl font-black tracking-tight text-foreground mb-6 max-w-3xl leading-tight">
          Enterprise Payment Infrastructure.
        </h1>
        
        <p className="text-lg md:text-xl text-muted-foreground max-w-2xl leading-relaxed mb-10">
          A production-ready distributed system showcasing eventual consistency, idempotency, and choreographic saga patterns. Built for speed and high availability.
        </p>
        
        <div className="flex flex-col sm:flex-row items-center gap-4 w-full sm:w-auto">
          <Link href="/auth" className="flex items-center justify-center w-full sm:w-auto px-6 py-3 rounded-sm bg-blue-800 text-white font-semibold hover:bg-blue-900 transition-colors">
            Get Started <ArrowRight className="ml-2 h-4 w-4" />
          </Link>
          <a href="https://github.com/Prajyot05/atlas-pay" target="_blank" rel="noreferrer" className="flex items-center justify-center w-full sm:w-auto px-6 py-3 rounded-sm bg-secondary text-secondary-foreground font-semibold hover:bg-secondary/80 transition-colors border border-border">
            View Documentation
          </a>
        </div>
      </div>

      {/* Feature Grid */}
      <div className="w-full bg-secondary/5">
        <div className="grid grid-cols-1 md:grid-cols-3 divide-y md:divide-y-0 md:divide-x divide-border/40 max-w-6xl mx-auto">
          <div className="p-10 flex flex-col items-start">
            <ShieldCheck className="h-8 w-8 text-blue-800 mb-6" />
            <h3 className="text-lg font-bold mb-3 tracking-tight">Absolute Idempotency</h3>
            <p className="text-muted-foreground text-sm leading-relaxed">
              Redis-backed distributed locks guarantee exactly-once processing. Network retries will never result in duplicate charges.
            </p>
          </div>
          
          <div className="p-10 flex flex-col items-start">
            <Zap className="h-8 w-8 text-blue-800 mb-6" />
            <h3 className="text-lg font-bold mb-3 tracking-tight">Saga Orchestration</h3>
            <p className="text-muted-foreground text-sm leading-relaxed">
              Transactional Outbox pattern coupled with Kafka enables true distributed eventual consistency, bypassing 2PC locking overhead.
            </p>
          </div>

          <div className="p-10 flex flex-col items-start">
            <Globe className="h-8 w-8 text-blue-800 mb-6" />
            <h3 className="text-lg font-bold mb-3 tracking-tight">gRPC Validation</h3>
            <p className="text-muted-foreground text-sm leading-relaxed">
              Internal microservices communicate via high-throughput binary Protocol Buffers, wrapped in Resilience4j circuit breakers.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
