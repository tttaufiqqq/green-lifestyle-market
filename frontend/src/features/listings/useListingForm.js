import { useState, useEffect } from 'react'
import { listingsApi } from './listings.api'

export function useListingForm(initial = null) {
  const [form, setForm] = useState({
    title: '', description: '', categoryId: '', itemCondition: 'NEW',
    price: '', quantity: 1, allowMeetup: true, allowShipping: false,
    shippingFee: '', meetupLocation: '', sustainabilityNote: '', status: 'DRAFT',
    ...(initial ?? {}),
  })
  const [categories, setCategories] = useState([])
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    listingsApi.getCategories().then(setCategories).catch(console.error)
  }, [])

  const handle = (e) => {
    const { name, value, type, checked } = e.target
    setForm(f => ({ ...f, [name]: type === 'checkbox' ? checked : value }))
  }

  const submit = async (onSuccess) => {
    setError(null)
    setLoading(true)
    try {
      const body = {
        ...form,
        categoryId: Number(form.categoryId),
        price: parseFloat(form.price),
        quantity: Number(form.quantity),
        shippingFee: form.allowShipping && form.shippingFee !== '' ? parseFloat(form.shippingFee) : null,
        meetupLocation: form.allowMeetup ? form.meetupLocation : null,
      }
      const res = initial?.id
        ? await listingsApi.updateListing(initial.id, body)
        : await listingsApi.createListing(body)
      onSuccess(res)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return { form, handle, submit, categories, error, loading }
}
