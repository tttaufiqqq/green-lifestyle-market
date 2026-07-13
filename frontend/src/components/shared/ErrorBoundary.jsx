import { Component } from 'react'

export default class ErrorBoundary extends Component {
  constructor(props) {
    super(props)
    this.state = { hasError: false }
  }

  static getDerivedStateFromError() {
    return { hasError: true }
  }

  componentDidCatch(error, info) {
    console.error('[ErrorBoundary]', error, info)
  }

  render() {
    if (this.state.hasError) {
      return (
        <main className="min-h-screen flex flex-col items-center justify-center p-8 text-center gap-4">
          <h1 className="text-2xl font-bold text-zinc-900">Something went wrong</h1>
          <p className="text-zinc-600">An unexpected error occurred. Try reloading the page.</p>
          <button onClick={() => window.location.reload()} className="btn-primary">
            Reload
          </button>
        </main>
      )
    }
    return this.props.children
  }
}
