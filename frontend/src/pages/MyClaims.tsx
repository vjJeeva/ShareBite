import React, { useEffect, useState } from 'react';
import { Container, Table, Badge, Button, Spinner, Alert, Card } from 'react-bootstrap';
import api from '../api/api';
import type { Claim } from '../types';
import { FaHistory, FaTimesCircle } from 'react-icons/fa';

const MyClaims: React.FC = () => {
  const [claims, setClaims] = useState<Claim[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchClaims();
  }, []);

  const fetchClaims = async () => {
    try {
      const response = await api.get('/claims/my-claims');
      setClaims(response.data);
    } catch (err) {
      setError('Failed to load claims.');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = async (claimId: number) => {
    if (window.confirm('Are you sure you want to cancel this claim?')) {
      try {
        await api.put(`/claims/cancel/${claimId}`);
        fetchClaims();
      } catch (err: any) {
        alert(err.response?.data || 'Failed to cancel claim.');
      }
    }
  };

  if (loading) return <div className="text-center py-5"><Spinner animation="border" variant="success" /></div>;

  return (
    <Container className="py-4 fade-in">
      <h2 className="fw-bold mb-4">My Claims</h2>
      
      {error && <Alert variant="danger">{error}</Alert>}
      
      {claims.length === 0 ? (
        <Card className="text-center p-5">
          <Card.Body>
            <FaHistory size={40} className="text-muted mb-3" />
            <p className="text-muted">You haven't claimed any food yet.</p>
            <Button variant="success" href="/available">Browse Available Food</Button>
          </Card.Body>
        </Card>
      ) : (
        <Card className="border-0 shadow-sm">
          <Table responsive hover className="mb-0">
            <thead className="bg-light">
              <tr>
                <th className="px-4 py-3">Listing ID</th>
                <th className="py-3">Status</th>
                <th className="py-3">Claim Date</th>
                <th className="px-4 py-3 text-end">Actions</th>
              </tr>
            </thead>
            <tbody>
              {claims.map((claim) => (
                <tr key={claim.id} className="align-middle">
                  <td className="px-4">#{claim.listingId}</td>
                  <td>
                    <Badge bg={
                      claim.status === 'FULFILLED' ? 'success' : 
                      claim.status === 'CANCELLED' ? 'danger' : 'primary'
                    }>
                      {claim.status.replace('_', ' ')}
                    </Badge>
                  </td>
                  <td>{new Date(claim.claimTime).toLocaleString()}</td>
                  <td className="px-4 text-end">
                    {claim.status === 'PENDING_PICKUP' && (
                      <Button 
                        variant="outline-danger" 
                        size="sm" 
                        onClick={() => handleCancel(claim.id)}
                      >
                        <FaTimesCircle className="me-1" /> Cancel
                      </Button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        </Card>
      )}
    </Container>
  );
};

export default MyClaims;
