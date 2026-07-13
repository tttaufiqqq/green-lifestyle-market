import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useListingForm } from './useListingForm'
import { listingsApi } from './listings.api'
import { toast } from '../../stores/toast'

export default function ListingFormPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [initial, setInitial] = useState(id ? undefined : null)
  const [images, setImages] = useState([])

  useEffect(() => {
    if (!id) return
    listingsApi.getMyListings().then(list => {
      const found = list.find(l => String(l.id) === id) ?? null
      setInitial(found)
      setImages(found?.images ?? [])
    }).catch(() => setInitial(null))
  }, [id])

  if (initial === undefined) return <p className="p-6">Loading…</p>
  if (id && initial === null) return <p className="p-6 text-red-600">Listing not found.</p>

  return <ListingForm initial={initial} id={id} images={images} setImages={setImages} navigate={navigate} />
}

function ListingForm({ initial, id, images, setImages, navigate }) {
  const { form, handle, submit, categories, error, loading } = useListingForm(initial)
  const [uploading, setUploading] = useState(false)

  const handleSubmit = (e) => {
    e.preventDefault()
    submit(res => navigate(`/listings/${res.id}/edit`))
  }

  const handleImageUpload = async (e) => {
    const file = e.target.files[0]
    if (!file || !id) return
    setUploading(true)
    try {
      const res = await listingsApi.uploadImage(id, file)
      setImages(res.images ?? [])
      toast.success('Image uploaded')
    } catch (err) {
      toast.error(err.message)
    } finally {
      setUploading(false)
      e.target.value = ''
    }
  }

  const handleDeleteImage = async (imgId) => {
    try {
      await listingsApi.deleteImage(id, imgId)
      setImages(prev => prev.filter(i => i.id !== imgId))
      toast.success('Image removed')
    } catch (err) {
      toast.error(err.message)
    }
  }

  const flatCategories = (categories || []).flatMap(c => [
    { id: c.id, name: c.name },
    ...(c.children || []).map(ch => ({ id: ch.id, name: `\u00a0\u00a0${ch.name}` })),
  ])

  return (
    <main className="max-w-2xl mx-auto p-6 space-y-6">
      <h1 className="text-2xl font-bold">{id ? 'Edit Listing' : 'Create Listing'}</h1>
      {error && <p className="text-red-600 text-sm">{error}</p>}
      <form onSubmit={handleSubmit} className="space-y-4">
        <input name="title" placeholder="Title" value={form.title} onChange={handle}
          className="input w-full" required maxLength={120} />
        <textarea name="description" placeholder="Description" value={form.description}
          onChange={handle} className="input w-full h-28" required />
        <select name="categoryId" value={form.categoryId} onChange={handle}
          className="input w-full" required>
          <option value="">Select category…</option>
          {flatCategories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
        </select>
        <select name="itemCondition" value={form.itemCondition} onChange={handle} className="input w-full">
          {['NEW', 'LIKE_NEW', 'GOOD', 'FAIR'].map(v =>
            <option key={v} value={v}>{v.replace('_', ' ')}</option>)}
        </select>
        <div className="grid grid-cols-2 gap-4">
          <input name="price" type="number" step="0.01" min="0.01" placeholder="Price (RM)"
            value={form.price} onChange={handle} className="input" required />
          <input name="quantity" type="number" min="1" placeholder="Qty"
            value={form.quantity} onChange={handle} className="input" required />
        </div>
        <div className="space-y-2 border rounded p-3">
          <label className="flex items-center gap-2 cursor-pointer">
            <input type="checkbox" name="allowMeetup" checked={form.allowMeetup} onChange={handle} />
            Meetup
          </label>
          {form.allowMeetup && (
            <input name="meetupLocation" placeholder="Meetup location" value={form.meetupLocation}
              onChange={handle} className="input w-full" />
          )}
          <label className="flex items-center gap-2 cursor-pointer">
            <input type="checkbox" name="allowShipping" checked={form.allowShipping} onChange={handle} />
            Shipping
          </label>
          {form.allowShipping && (
            <input name="shippingFee" type="number" step="0.01" min="0" placeholder="Shipping fee (RM)"
              value={form.shippingFee} onChange={handle} className="input w-full" />
          )}
        </div>
        <input name="sustainabilityNote" placeholder="Sustainability note (optional)"
          value={form.sustainabilityNote} onChange={handle} className="input w-full" maxLength={255} />
        <select name="status" value={form.status} onChange={handle} className="input w-full">
          <option value="DRAFT">Draft</option>
          <option value="ACTIVE">Active</option>
        </select>
        <button type="submit" disabled={loading} className="btn-primary w-full">
          {loading ? 'Saving…' : id ? 'Update Listing' : 'Create Listing'}
        </button>
      </form>
      {id && (
        <section className="space-y-3">
          <h2 className="font-semibold">Images ({images.length}/5)</h2>
          <div className="flex gap-2 flex-wrap">
            {images.map(img => (
              <div key={img.id} className="relative w-24 h-24">
                <img src={`/uploads/${img.path}`} alt="" className="w-full h-full object-cover rounded" />
                <button onClick={() => handleDeleteImage(img.id)}
                  className="absolute -top-1 -right-1 bg-red-600 text-white text-xs w-5 h-5 rounded-full">
                  &times;
                </button>
              </div>
            ))}
          </div>
          {images.length < 5 && (
            <label className="cursor-pointer text-green-700 underline text-sm">
              {uploading ? 'Uploading…' : '+ Add image'}
              <input type="file" accept="image/jpeg,image/png,image/webp"
                onChange={handleImageUpload} className="hidden" disabled={uploading} />
            </label>
          )}
        </section>
      )}
    </main>
  )
}
