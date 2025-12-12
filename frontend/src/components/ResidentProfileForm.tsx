import React, { useEffect, useMemo, useState } from 'react';
import { apiService } from '../services/api';

type Profile = {
  fullName?: string;
  email?: string;
  phone?: string;
  addressLine1?: string;
  addressLine2?: string;
  city?: string;
  state?: string;
  postalCode?: string;
  country?: string;
  dateOfBirth?: string;
  gender?: string;
  preferredLanguage?: string;
  emergencyContactName?: string;
  emergencyContactPhone?: string;
  householdSize?: number;
  specialNeeds?: string;
  latitude?: number;
  longitude?: number;
  consentToContact?: boolean;
  consentToShare?: boolean;
};

interface Props {
  onSaved?: (profile: Profile) => void;
}

export const ResidentProfileForm: React.FC<Props> = ({ onSaved }) => {
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [profile, setProfile] = useState<Profile>({});

  useEffect(() => {
    let mounted = true;
    (async () => {
      try {
        setLoading(true);
        const data = await apiService.getMyProfile();
        if (mounted) {
          setProfile(data);
        }
      } catch (e: any) {
        if (mounted) setError(e?.message || 'Failed to load profile');
      } finally {
        if (mounted) setLoading(false);
      }
    })();
    return () => {
      mounted = false;
    };
  }, []);

  const onChange = (field: keyof Profile) => (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const value = e.target.type === 'number' ? Number(e.target.value) : e.target.type === 'checkbox' ? (e.target as HTMLInputElement).checked : e.target.value;
    setProfile(prev => ({ ...prev, [field]: value }));
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setSuccess(null);
    try {
      setSaving(true);
      const payload: Profile = { ...profile };
      const updated = await apiService.updateMyProfile(payload as any);
      setProfile(updated);
      setSuccess('Profile saved');
      onSaved?.(updated);
    } catch (e: any) {
      setError(e?.message || 'Failed to save profile');
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return <div className="p-4">Loading profile...</div>;
  }

  return (
    <form onSubmit={handleSave} className="max-w-3xl mx-auto p-4 space-y-6">
      {error && <div className="text-red-600 text-sm">{error}</div>}
      {success && <div className="text-green-600 text-sm">{success}</div>}

      <section className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium">Full name</label>
          <input className="mt-1 w-full border rounded p-2" value={profile.fullName || ''} onChange={onChange('fullName')} />
        </div>
        <div>
          <label className="block text-sm font-medium">Email</label>
          <input className="mt-1 w-full border rounded p-2" value={profile.email || ''} onChange={onChange('email')} disabled />
        </div>
        <div>
          <label className="block text-sm font-medium">Phone</label>
          <input className="mt-1 w-full border rounded p-2" value={profile.phone || ''} onChange={onChange('phone')} />
        </div>
        <div>
          <label className="block text-sm font-medium">Preferred language</label>
          <input className="mt-1 w-full border rounded p-2" value={profile.preferredLanguage || ''} onChange={onChange('preferredLanguage')} />
        </div>
      </section>

      <section className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium">Address line 1</label>
          <input className="mt-1 w-full border rounded p-2" value={profile.addressLine1 || ''} onChange={onChange('addressLine1')} />
        </div>
        <div>
          <label className="block text-sm font-medium">Address line 2</label>
          <input className="mt-1 w-full border rounded p-2" value={profile.addressLine2 || ''} onChange={onChange('addressLine2')} />
        </div>
        <div>
          <label className="block text-sm font-medium">City</label>
          <input className="mt-1 w-full border rounded p-2" value={profile.city || ''} onChange={onChange('city')} />
        </div>
        <div>
          <label className="block text-sm font-medium">State/Province</label>
          <input className="mt-1 w-full border rounded p-2" value={profile.state || ''} onChange={onChange('state')} />
        </div>
        <div>
          <label className="block text-sm font-medium">Postal code</label>
          <input className="mt-1 w-full border rounded p-2" value={profile.postalCode || ''} onChange={onChange('postalCode')} />
        </div>
        <div>
          <label className="block text-sm font-medium">Country</label>
          <input className="mt-1 w-full border rounded p-2" value={profile.country || ''} onChange={onChange('country')} />
        </div>
      </section>

      <section className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium">Date of birth</label>
          <input type="date" className="mt-1 w-full border rounded p-2" value={profile.dateOfBirth || ''} onChange={onChange('dateOfBirth')} />
        </div>
        <div>
          <label className="block text-sm font-medium">Gender</label>
          <select className="mt-1 w-full border rounded p-2" value={profile.gender || ''} onChange={onChange('gender')}>
            <option value="">Select</option>
            <option value="female">Female</option>
            <option value="male">Male</option>
            <option value="non-binary">Non-binary</option>
            <option value="prefer-not-to-say">Prefer not to say</option>
          </select>
        </div>
        <div>
          <label className="block text-sm font-medium">Household size</label>
          <input type="number" className="mt-1 w-full border rounded p-2" value={profile.householdSize || 0} onChange={onChange('householdSize')} />
        </div>
        <div>
          <label className="block text-sm font-medium">Special needs</label>
          <textarea className="mt-1 w-full border rounded p-2" value={profile.specialNeeds || ''} onChange={onChange('specialNeeds')} rows={3} />
        </div>
      </section>

      <section className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label className="inline-flex items-center space-x-2">
            <input type="checkbox" checked={!!profile.consentToContact} onChange={onChange('consentToContact')} />
            <span className="text-sm">I consent to be contacted for assistance</span>
          </label>
        </div>
        <div>
          <label className="inline-flex items-center space-x-2">
            <input type="checkbox" checked={!!profile.consentToShare} onChange={onChange('consentToShare')} />
            <span className="text-sm">I consent to share my data with responders</span>
          </label>
        </div>
      </section>

      <div className="flex items-center gap-3">
        <button type="submit" className="bg-blue-600 text-white px-4 py-2 rounded disabled:opacity-50" disabled={saving}>
          {saving ? 'Saving...' : 'Save profile'}
        </button>
        <span className="text-sm text-gray-500">Ensure your contact information is accurate for responders.</span>
      </div>
    </form>
  );
};

export default ResidentProfileForm;





