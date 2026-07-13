const files = import.meta.glob('./content/*.md', { eager: true, query: '?raw', import: 'default' })

const ORDER = [
  { slug: 'getting-started', label: 'Getting Started', file: './content/getting-started.md' },
  { slug: 'buying', label: 'Buying', file: './content/buying.md' },
  { slug: 'selling', label: 'Selling', file: './content/selling.md' },
  { slug: 'orders-and-delivery', label: 'Orders & Delivery', file: './content/orders-and-delivery.md' },
  { slug: 'payments-and-refunds', label: 'Payments & Refunds', file: './content/payments-and-refunds.md' },
  { slug: 'faq', label: 'FAQ', file: './content/faq.md' },
  { slug: 'contact', label: 'Contact', file: './content/contact.md' },
]

export const helpSections = ORDER.map((s) => ({ ...s, body: files[s.file] }))
