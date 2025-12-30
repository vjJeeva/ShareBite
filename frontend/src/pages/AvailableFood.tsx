import React, { useEffect, useState } from 'react';
import { Container, Row, Col, Card, Button, Badge, Spinner, Alert, Modal } from 'react-bootstrap';
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import api from '../api/api';
import type { Listing } from '../types';
import { useAuth } from '../context/AuthContext';
import { FaMapMarkerAlt, FaUtensils, FaClock, FaPhone, FaEnvelope, FaInfoCircle } from 'react-icons/fa';

const AvailableFood: React.FC = () => {
  const [listings, setListings] = useState<Listing[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [claimingId, setClaimingId] = useState<number | null>(null);
  
  // Modal State for "Amazon-style" details
  const [showModal, setShowModal] = useState(false);
  const [selectedListing, setSelectedListing] = useState<Listing | null>(null);

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

  const handleOpenDetails = (listing: Listing) => {
    setSelectedListing(listing);
    setShowModal(true);
  };

  const handleCloseModal = () => {
    setShowModal(false);
    setSelectedListing(null);
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
      setShowModal(false); 
      fetchListings(); 
    } catch (err: any) {
      alert(err.response?.data || 'Failed to claim food.');
    } finally {
      setClaimingId(null);
    }
  };

  if (loading) return (
    <div className="text-center py-5">
      <Spinner animation="border" variant="success" />
      <p className="mt-2">Fetching fresh food...</p>
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
              <Card 
                className="h-100 shadow-sm border-0 hover-card" 
                style={{ cursor: 'pointer', transition: '0.3s' }}
                onClick={() => handleOpenDetails(listing)}
              >
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
                    <span className="text-success fw-bold small">{listing.servings} Servings</span>
                  </div>
                  <Card.Text className="text-muted small mb-3 text-truncate">
                    {listing.description}
                  </Card.Text>
                  
                  <div className="mb-3">
                    <div className="d-flex align-items-center text-muted small mb-1">
                      <FaMapMarkerAlt className="me-2 text-danger" /> {listing.address}
                    </div>
                  </div>

                  <Button 
                    variant="success" 
                    className="w-100 fw-bold rounded-pill"
                    onClick={(e) => {
                        e.stopPropagation(); // Prevents the modal from opening when clicking claim
                        handleClaim(listing.id);
                    }}
                    disabled={claimingId === listing.id}
                  >
                    {claimingId === listing.id ? 'Claiming...' : 'Claim Now'}
                  </Button>
                </Card.Body>
              </Card>
            </Col>
          ))}
        </Row>
      )}

      {/* DETAIL MODAL */}
      <Modal show={showModal} onHide={handleCloseModal} size="lg" centered>
        <Modal.Header closeButton className="border-0 pb-0">
          <Modal.Title className="fw-bold text-success">Food Details</Modal.Title>
        </Modal.Header>
        <Modal.Body className="p-4">
          {selectedListing && (
            <Row>
              <Col md={6} className="mb-3 mb-md-0">
                <div className="rounded overflow-hidden shadow-sm mb-3" style={{ height: '250px' }}>
                  <img 
                    src={selectedListing.photoUrl || 'https://via.placeholder.com/400x300?text=Food'} 
                    alt={selectedListing.name}
                    className="w-100 h-100 object-fit-cover"
                  />
                </div>
                {/* INTERACTIVE MAP */}
                <div className="rounded overflow-hidden shadow-sm border" style={{ height: '200px' }}>
                  <MapContainer 
                    center={[selectedListing.latitude, selectedListing.longitude]} 
                    zoom={15} 
                    style={{ height: '100%', width: '100%' }}
                  >
                    <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
                    <Marker position={[selectedListing.latitude, selectedListing.longitude]}>
                      <Popup>{selectedListing.name}</Popup>
                    </Marker>
                  </MapContainer>
                </div>
              </Col>
              
              <Col md={6}>
                <Badge bg="success" className="mb-2">{selectedListing.type}</Badge>
                <h3 className="fw-bold">{selectedListing.name}</h3>
                <p className="text-muted">{selectedListing.description}</p>
                <hr />
                
                <div className="mb-3">
                  <p className="mb-2"><FaUtensils className="me-2 text-success" /> <strong>Servings:</strong> {selectedListing.servings}</p>
                  <p className="mb-2"><FaMapMarkerAlt className="me-2 text-danger" /> <strong>Address:</strong> {selectedListing.address}</p>
                  <p className="mb-2"><FaClock className="me-2 text-primary" /> <strong>Expiry:</strong> {new Date(selectedListing.claimByTime).toLocaleString()}</p>
                </div>

                <div className="p-3 bg-light rounded border mb-3">
                   <h6 className="fw-bold mb-2 small text-uppercase text-muted">Donor Information</h6>
                   <p className="mb-1 small"><strong>Name:</strong> {selectedListing.donorName || 'Verified Donor'}</p>
                   {isAuthenticated && (
                     <>
                        <p className="mb-1 small"><strong>Email:</strong> {selectedListing.donorEmail || 'N/A'}</p>
                        <p className="mb-0 small"><strong>Phone:</strong> {selectedListing.phoneNumber}</p>
                     </>
                   )}
                </div>

                {user?.role === 'RECIPIENT' && (
                  <Button 
                    variant="success" 
                    className="w-100 py-2 fw-bold"
                    onClick={() => handleClaim(selectedListing.id)}
                    disabled={claimingId === selectedListing.id}
                  >
                    {claimingId === selectedListing.id ? <Spinner size="sm" /> : 'Confirm Claim'}
                  </Button>
                )}
              </Col>
            </Row>
          )}
        </Modal.Body>
      </Modal>
    </Container>
  );
};

export default AvailableFood;