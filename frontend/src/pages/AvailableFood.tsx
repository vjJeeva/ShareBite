import React, { useEffect, useState } from 'react';
import { Container, Row, Col, Card, Button, Badge, Spinner, Alert } from 'react-bootstrap';
import api from '../api/api';
import type { Listing } from '../types';
import { useAuth } from '../context/AuthContext';
import { FaMapMarkerAlt, FaUtensils, FaClock, FaCheckCircle } from 'react-icons/fa';

const AvailableFood: React.FC = () => {
  const [listings, setListings] = useState<Listing[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [claimingId, setClaimingId] = useState<number | null>(null);
  
  const { user, isAuthenticated } = useAuth();

  useEffect(() => {
    fetchListings();
  }, []);

  const fetchListings = async () => {
    try {
      const response = await api.get('/listings/available');
      setListings(response.data);
    } catch (err) {
      setError('Failed to load listings. Please try again later.');
    } finally {
      setLoading(false);
    }
  };

  const handleClaim = async (listingId: number) => {
    if (!isAuthenticated) {
      alert('Please login to claim food.');
      return;
    }
    
    setClaimingId(listingId);
    try {
      await api.post(`/claims/initiate/${listingId}`);
      alert('Food claimed successfully! Check "My Claims" for details.');
      fetchListings(); // Refresh list
    } catch (err: any) {
      alert(err.response?.data || 'Failed to claim food.');
    } finally {
      setClaimingId(null);
    }
  };

  if (loading) return (
    <div className="text-center py-5">
      <Spinner animation="border" variant="success" />
      <p className="mt-2">Loading fresh food...</p>
    </div>
  );

  return (
    <Container className="py-4 fade-in">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h2 className="fw-bold">Available Food Near You</h2>
        <Badge bg="success" className="p-2 px-3 rounded-pill">{listings.length} items found</Badge>
      </div>

      {error && <Alert variant="danger">{error}</Alert>}

      {listings.length === 0 ? (
        <Card className="text-center p-5 border-0 shadow-sm">
          <Card.Body>
            <FaUtensils size={50} className="text-muted mb-3" />
            <h3>No food available right now</h3>
            <p className="text-muted">Check back later or browse other locations.</p>
          </Card.Body>
        </Card>
      ) : (
        <Row className="g-4">
          {listings.map((listing) => (
            <Col key={listing.id} md={6} lg={4}>
              <Card className="h-100 overflow-hidden">
                <div style={{ height: '200px', overflow: 'hidden', position: 'relative' }}>
                  <img 
                    src={listing.photoUrl || 'https://via.placeholder.com/400x200?text=Food+Image'} 
                    alt={listing.name}
                    className="w-100 h-100 object-fit-cover"
                  />
                  <Badge bg="success" className="position-absolute top-0 end-0 m-3 p-2">
                    {listing.type}
                  </Badge>
                </div>
                <Card.Body className="p-4">
                  <div className="d-flex justify-content-between align-items-start mb-2">
                    <Card.Title className="fw-bold mb-0">{listing.name}</Card.Title>
                    <span className="text-success fw-bold">{listing.servings} Servings</span>
                  </div>
                  <Card.Text className="text-muted small mb-3">
                    {listing.description}
                  </Card.Text>
                  
                  <div className="mb-3">
                    <div className="d-flex align-items-center text-muted small mb-1">
                      <FaMapMarkerAlt className="me-2 text-danger" /> {listing.address}
                    </div>
                    <div className="d-flex align-items-center text-muted small">
                      <FaClock className="me-2 text-primary" /> Claim by: {new Date(listing.claimByTime).toLocaleString()}
                    </div>
                  </div>

                  {user?.role === 'RECIPIENT' && (
                    <Button 
                      variant="success" 
                      className="w-100 fw-bold" 
                      onClick={() => handleClaim(listing.id)}
                      disabled={claimingId === listing.id}
                    >
                      {claimingId === listing.id ? 'Claiming...' : 'Claim This Bite'}
                    </Button>
                  )}
                  
                  {!isAuthenticated && (
                    <Button variant="outline-success" className="w-100 fw-bold" href="/login">
                      Login to Claim
                    </Button>
                  )}
                </Card.Body>
              </Card>
            </Col>
          ))}
        </Row>
      )}
    </Container>
  );
};

export default AvailableFood;
