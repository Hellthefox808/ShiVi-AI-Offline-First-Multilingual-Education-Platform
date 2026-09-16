import React from 'react';
import './globals.css';
import { Plus_Jakarta_Sans, Outfit } from 'next/font/google';
import { Sparkles, BookOpen, Zap, Network } from 'lucide-react';

const jakarta = Plus_Jakarta_Sans({
  subsets: ['latin'],
  variable: '--font-jakarta',
  display: 'swap',
});

const outfit = Outfit({
  subsets: ['latin'],
  variable: '--font-outfit',
  display: 'swap',
});

export const metadata = {
  title: 'BhashaSetu AI (भाषासेतु) — Multilingual MTB-MLE Platform',
  description: 'AI-Powered Mother-Tongue Scaffolding for Jharkhand Tribal Classrooms (Santhali, Ho, Mundari) · SIH26042',
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en" className={`${jakarta.variable} ${outfit.variable} dark`}>
      <body className="bg-slate-950 text-slate-100 min-h-screen flex flex-col font-sans antialiased selection:bg-emerald-500/30 selection:text-emerald-300">
        {/* Top Ambient Glow Gradient */}
        <div className="fixed inset-0 pointer-events-none z-0 overflow-hidden">
          <div className="absolute -top-40 left-1/2 -translate-x-1/2 w-[1000px] h-[400px] bg-gradient-to-b from-emerald-600/15 via-teal-500/5 to-transparent blur-3xl opacity-80" />
          <div className="absolute top-1/3 -left-40 w-96 h-96 bg-amber-500/10 blur-3xl rounded-full" />
          <div className="absolute top-1/2 -right-40 w-96 h-96 bg-emerald-500/10 blur-3xl rounded-full" />
        </div>

        {/* Global Header */}
        <header className="sticky top-0 z-10 border-b border-white/10 bg-slate-900/80 backdrop-blur-xl px-6 py-3.5 shadow-2xl transition-all">
          <div className="max-w-7xl mx-auto flex flex-wrap gap-4 items-center justify-between">
            {/* Logo & Platform Info */}
            <div className="flex items-center space-x-3.5">
              <div className="w-11 h-11 rounded-2xl bg-gradient-to-br from-emerald-500 to-teal-700 flex items-center justify-center shadow-lg shadow-emerald-900/40 border border-emerald-400/30">
                <Sparkles className="w-5 h-5 text-white" />
              </div>
              <div>
                <div className="flex items-center space-x-2">
                  <h1 className="font-extrabold text-lg tracking-tight text-white font-display">
                    भाषासेतु <span className="bg-gradient-to-r from-emerald-400 via-teal-300 to-emerald-200 bg-clip-text text-transparent">BhashaSetu AI</span>
                  </h1>
                  <span className="text-[10px] uppercase font-mono font-bold px-2 py-0.5 rounded-md bg-emerald-500/20 text-emerald-300 border border-emerald-500/30">
                    v3.0-PROD
                  </span>
                </div>
                <p className="text-xs text-slate-400">
                  Mother-Tongue-Based Multilingual Education (MTB-MLE) · SIH 2026 Problem SIH26042
                </p>
              </div>
            </div>

            {/* Badges & System Health */}
            <div className="flex items-center flex-wrap gap-2.5 text-xs font-semibold">
              <div className="flex items-center space-x-1.5 px-3 py-1 rounded-full bg-emerald-950/70 border border-emerald-500/30 text-emerald-300 shadow-sm">
                <span className="w-2 h-2 rounded-full bg-emerald-400 animate-ping" />
                <span>Edge Mesh Active</span>
              </div>
              <div className="flex items-center space-x-1.5 px-3 py-1 rounded-full bg-slate-800/80 border border-slate-700 text-slate-300">
                <BookOpen className="w-3.5 h-3.5 text-emerald-400" />
                <span>JCERT Primary (Grades 1-5)</span>
              </div>
              <div className="flex items-center space-x-1.5 px-3 py-1 rounded-full bg-amber-950/60 border border-amber-500/30 text-amber-300">
                <Zap className="w-3.5 h-3.5 text-amber-400" />
                <span>Sub-3s SLA Compliant</span>
              </div>
              <a
                href="/architecture.html"
                target="_blank"
                rel="noopener noreferrer"
                className="flex items-center space-x-1.5 px-3 py-1 rounded-full bg-indigo-950/70 border border-indigo-500/40 text-indigo-300 hover:bg-indigo-900/80 hover:border-indigo-400/60 transition-all cursor-pointer shadow-sm"
                title="Open Interactive Archify Diagram"
              >
                <Network className="w-3.5 h-3.5 text-indigo-400" />
                <span>Architecture Map</span>
              </a>
            </div>
          </div>
        </header>

        {/* Main Workspace */}
        <main className="relative z-10 flex-1 p-4 md:p-6 lg:p-8 max-w-7xl mx-auto w-full">
          {children}
        </main>

        {/* Footer */}
        <footer className="relative z-10 bg-slate-900/90 border-t border-white/10 text-slate-400 text-xs py-4 px-6 text-center backdrop-blur-md">
          <div className="max-w-7xl mx-auto flex flex-col md:flex-row items-center justify-between gap-2">
            <p>
              BhashaSetu AI (भाषासेतु) · Sarala Birla University (Team SHIVI@808) · SIH 2026 Finalist
            </p>
            <div className="flex items-center space-x-4 text-slate-500">
              <span>Santhali (Ol Chiki)</span>
              <span>•</span>
              <span>Ho (Warang Chiti)</span>
              <span>•</span>
              <span>Mundari (Devanagari)</span>
            </div>
          </div>
        </footer>
      </body>
    </html>
  );
}
