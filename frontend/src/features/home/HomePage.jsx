export default function HomePage() {
  return (
    <main className="flex-1 flex flex-col items-center justify-center px-4 md:px-8 py-24 text-center">
      <h1 className="text-4xl font-bold text-zinc-900 tracking-tight">
        Welcome to <span className="text-emerald-600">GreenMarket</span>
      </h1>
      <p className="mt-4 text-lg text-zinc-500 max-w-md">
        Sustainable products for a greener lifestyle. Browse listings, buy and sell
        pre-loved eco-friendly goods.
      </p>
    </main>
  )
}
