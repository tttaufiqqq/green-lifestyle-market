function Field({ label, name, value, onChange, required }) {
  return (
    <div>
      <label className="block text-xs text-zinc-500 mb-0.5">{label}</label>
      <input value={value} onChange={e => onChange(name, e.target.value)}
        required={required} className="input text-sm w-full" />
    </div>
  )
}

/** Per-seller fulfilment method picker + shipping address form. */
export default function FulfilmentSelector({ group, choice, onChange }) {
  const set = (key, val) => onChange(group.sellerId, { ...choice, [key]: val })

  return (
    <div className="space-y-3">
      <p className="text-sm font-semibold text-zinc-700">Delivery from {group.sellerName}</p>

      <div className="flex gap-3">
        {group.allowMeetup && (
          <label className="flex items-center gap-2 text-sm cursor-pointer">
            <input type="radio" name={`method-${group.sellerId}`} value="MEETUP"
              checked={choice.method === 'MEETUP'} onChange={() => set('method', 'MEETUP')} />
            Meetup
          </label>
        )}
        {group.allowShipping && (
          <label className="flex items-center gap-2 text-sm cursor-pointer">
            <input type="radio" name={`method-${group.sellerId}`} value="SHIPPING"
              checked={choice.method === 'SHIPPING'} onChange={() => set('method', 'SHIPPING')} />
            Shipping
          </label>
        )}
      </div>

      {choice.method === 'SHIPPING' && (
        <div className="grid grid-cols-2 gap-2 pl-1 border-l-2 border-emerald-100">
          <div className="col-span-2">
            <Field label="Full name *" name="shipName" value={choice.shipName} onChange={set} required />
          </div>
          <Field label="Phone *" name="shipPhone" value={choice.shipPhone} onChange={set} required />
          <div className="col-span-2">
            <Field label="Address line 1 *" name="shipAddress1" value={choice.shipAddress1} onChange={set} required />
          </div>
          <Field label="Address line 2" name="shipAddress2" value={choice.shipAddress2} onChange={set} />
          <Field label="Postcode *" name="shipPostcode" value={choice.shipPostcode} onChange={set} required />
          <Field label="City *" name="shipCity" value={choice.shipCity} onChange={set} required />
          <Field label="State *" name="shipState" value={choice.shipState} onChange={set} required />
        </div>
      )}
    </div>
  )
}
