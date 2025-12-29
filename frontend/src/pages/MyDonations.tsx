import React, { useEffect, useState } from 'react';
import { Container, Row, Col, Card, Badge, Button, Tabs, Tab, Spinner, ListGroup } from 'react-bootstrap';
import api from '../api/api';
import type { Listing, Claim } from '../types';
import { FaCheckCircle, FaExclamationCircle } from 'react-icons/fa';

const MyDonations: React.FC = () => {
  const [listings, setListings] = useState<Listing[]>([]);
  const [claims, setClaims] = useState<Claim[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      const [listingsRes, claimsRes] = await Promise.all([
        api.get('/listings/my-donations'),
        api.get('/claims/donated-claims')
      ]);
      setListings(listingsRes.data);
      setClaims(claimsRes.data);
    } catch (err) {
      console.error('Failed to load donor data');
    } finally {
      setLoading(false);
    }
  };

  const handleFulfill = async (claimId: number) => {
    try {
      await api.put(`/claims/fulfill/${claimId}`);
      fetchData();
    } catch (err) {
      alert('Failed to fulfill claim');
    }
  };

  if (loading) return <div className="text-center py-5"><Spinner animation="border" variant="success" /></div>;

  return (
    <Container className="py-4 fade-in">
      <h2 className="fw-bold mb-4">Donor Dashboard</h2>
      
      <Tabs defaultActiveKey="listings" className="mb-4 custom-tabs">
        <Tab eventKey="listings" title={`My Listings (${listings.length})`}>
          <Row className="g-4">
            {listings.map((listing) => (
              <Col key={listing.id} md={6}>
                <Card className="h-100 flex-row overflow-hidden border-0 shadow-sm">
                  <div style={{ width: '150px' }}>
                    <img 
                      src={listing.photoUrl} 
                      alt={listing.name} 
                      className="w-100 h-100 object-fit-cover"
                    />
                  </div>
                  <Card.Body className="p-3">
                    <div className="d-flex justify-content-between align-items-start">
                      <h5 className="fw-bold mb-1">{listing.name}</h5>
                      <Badge bg={listing.status === 'AVAILABLE' ? 'success' : 'secondary'}>
                        {listing.status}
                      </Badge>
                    </div>
                    <p className="text-muted small mb-2">{listing.address}</p>
                    <div className="small">
                      <strong>{listing.servings}</strong> servings available
                    </div>
                  </Card.Body>
                </Card>
              </Col>
            ))}
          </Row>
        </Tab>
        
        <Tab eventKey="claims" title={`Pickup Requests (${claims.filter(c => c.status === 'PENDING_PICKUP').length})`}>
          {claims.length === 0 ? (
            <p className="text-center text-muted py-5">No claims for your food yet.</p>
          ) : (
            <ListGroup variant="flush" className="shadow-sm rounded">
              {claims.map((claim) => (
                <ListGroup.Item key={claim.id} className="p-4">
                  <div className="d-flex justify-content-between align-items-center">
                    <div>
                      <h6 className="fw-bold mb-1">Claim for Listing #{claim.listingId}</h6>
                      <p className="text-muted small mb-0">Requested on: {new Date(claim.claimTime).toLocaleString()}</p>
                      <Badge bg={claim.status === 'FULFILLED' ? 'success' : 'warning'} className="mt-2">
                        {claim.status}
                      </Badge>
                    </div>
                    {claim.status === 'PENDING_PICKUP' && (
                      <Button variant="success" onClick={() => handleFulfill(claim.id)}>
                        <FaCheckCircle className="me-2" /> Mark as Picked Up
                      </Button>
                    )}
                  </div>
                </ListGroup.Item>
              ))}
            </ListGroup>
          )}
        </Tab>
      </Tabs>
    </Container>
  );
};

export default MyDonations;
