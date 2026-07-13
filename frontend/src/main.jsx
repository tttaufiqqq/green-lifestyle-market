import React from 'react'
import ReactDOM from 'react-dom/client'
import { RouterProvider } from 'react-router-dom'
import { QueryClient, QueryClientProvider, MutationCache } from '@tanstack/react-query'
import router from './routes'
import ErrorBoundary from './components/shared/ErrorBoundary'
import ToastContainer from './components/shared/ToastContainer'
import { toast } from './stores/toast'
import './index.css'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: { retry: 1, staleTime: 30_000 },
  },
  mutationCache: new MutationCache({
    onSuccess: (data, variables, context, mutation) => {
      const msg = mutation.options.meta?.successMessage
      if (typeof msg === 'function') toast.success(msg(data, variables))
      else if (msg) toast.success(msg)
    },
    onError: (error, variables, context, mutation) => {
      if (mutation.options.meta?.suppressErrorToast) return
      toast.error(error.message ?? 'Something went wrong')
    },
  }),
})

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <ErrorBoundary>
      <QueryClientProvider client={queryClient}>
        <RouterProvider router={router} />
        <ToastContainer />
      </QueryClientProvider>
    </ErrorBoundary>
  </React.StrictMode>
)
