import React, { useEffect, useState } from 'react';
import { apiService } from '../services/api';

type Link = {
  id: string;
  entityId: string;
  score?: number;
  reason?: string;
};

export const DedupeReviewPage: React.FC<{ groupId: string }> = ({ groupId }) => {
  const [links, setLinks] = useState<Link[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [canonicalId, setCanonicalId] = useState<string>('');
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    (async () => {
      try {
        setLoading(true);
        const data = await apiService.getDedupeGroupLinks(groupId);
        setLinks(data);
      } catch (e: any) {
        setError(e?.message || 'Failed to load links');
      } finally {
        setLoading(false);
      }
    })();
  }, [groupId]);

  const doMerge = async () => {
    if (!canonicalId) return;
    try {
      setBusy(true);
      await apiService.mergeDedupeGroup(groupId, canonicalId);
      alert('Group merged');
    } catch (e: any) {
      alert(e?.message || 'Merge failed');
    } finally {
      setBusy(false);
    }
  };

  const doDismiss = async () => {
    try {
      setBusy(true);
      await apiService.dismissDedupeGroup(groupId, 'No duplicates');
      alert('Group dismissed');
    } catch (e: any) {
      alert(e?.message || 'Dismiss failed');
    } finally {
      setBusy(false);
    }
  };

  if (loading) return <div className="p-4">Loading…</div>;
  if (error) return <div className="p-4 text-red-600">{error}</div>;

  return (
    <div className="p-4 max-w-3xl mx-auto">
      <h1 className="text-xl font-semibold mb-4">Dedupe Review</h1>
      <div className="mb-4">
        <label className="block text-sm font-medium">Canonical entity ID</label>
        <input value={canonicalId} onChange={(e) => setCanonicalId(e.target.value)} className="mt-1 w-full border rounded p-2" placeholder="UUID" />
      </div>
      <table className="w-full text-sm border">
        <thead className="bg-gray-50">
          <tr>
            <th className="text-left p-2 border">Entity ID</th>
            <th className="text-left p-2 border">Score</th>
            <th className="text-left p-2 border">Reason</th>
          </tr>
        </thead>
        <tbody>
          {links.map((l) => (
            <tr key={l.id} className="border-b">
              <td className="p-2 font-mono text-xs">{l.entityId}</td>
              <td className="p-2">{l.score ?? '-'}</td>
              <td className="p-2">{l.reason ?? '-'}</td>
            </tr>
          ))}
        </tbody>
      </table>
      <div className="mt-4 flex gap-2">
        <button disabled={!canonicalId || busy} onClick={doMerge} className="bg-blue-600 text-white px-4 py-2 rounded disabled:opacity-50">Merge</button>
        <button disabled={busy} onClick={doDismiss} className="bg-gray-100 text-gray-800 px-4 py-2 rounded">Dismiss</button>
      </div>
    </div>
  );
};

export default DedupeReviewPage;





