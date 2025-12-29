import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Form, Button, Alert } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import { MapContainer, TileLayer, Marker, Popup, useMapEvents } from 'react-leaflet';
import L from 'leaflet';
import api from '../api/api';
import { FaUtensils, FaImage, FaMapMarkerAlt, FaUsers, FaClock, FaPhone } from 'react-icons/fa';

// Fix for default Leaflet marker icons
delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
});

const CreateListing: React.FC = () => {
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    servings: 1,
    type: 'Vegetarian',
    photoUrl: '',
    latitude: 0,
    longitude: 0,
    address: '',
    phoneNumber: '',
    claimByTime: ''
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  
  const navigate = useNavigate();

  const handleChange = (e: React.ChangeEvent<any>) => {
    const { name, value } = e.target;
    setFormData({ 
      ...formData, 
      [name]: name === 'servings' ? parseFloat(value) : value 
    });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
        if (formData.latitude === 0 || formData.longitude === 0) {
            alert("Please pin a location on the map.");
            setLoading(false);
            return;
        }

      const submissionData = {
        ...formData,
        servings: Number(formData.servings),
        // Ensure valid ISO string for Instant
        claimByTime: new Date(formData.claimByTime).toISOString(),
        // Simple sanitization to match regex ^[0-9]{10}$ etc.
        phoneNumber: formData.phoneNumber.replace(/[^0-9+]/g, '')
      };

      console.log('Submitting listing:', submissionData);

      await api.post('/listings/create', submissionData);
      alert('Listing created successfully!');
      navigate('/my-donations');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to create listing. Please check all fields.');
    } finally {
      setLoading(false);
    }
  };

  // Component to handle map clicks and location
  function LocationMarker() {
    const [position, setPosition] = useState<L.LatLng | null>(
      formData.latitude !== 0 ? L.latLng(formData.latitude, formData.longitude) : null
    );
    
    useMapEvents({
      click(e) {
        setPosition(e.latlng);
        setFormData(prev => ({ ...prev, latitude: e.latlng.lat, longitude: e.latlng.lng }));
      },
    });

    return position === null ? null : (
      <Marker position={position}>
        <Popup>Pickup Location</Popup>
      </Marker>
    );
  }

  return (
    <Container className="py-5">
      <Row className="justify-content-center">
        <Col lg={8}>
          <Card className="p-4 fade-in">
            <Card.Body>
              <h2 className="fw-bold text-success mb-4">Donate Food</h2>
              <p className="text-muted mb-4">Provide details about the food you're sharing.</p>
              
              {error && <Alert variant="danger">{error}</Alert>}
              
              <Form onSubmit={handleSubmit}>
                <Row className="mb-3">
                  <Col md={6}>
                    <Form.Group className="mb-3">
                      <Form.Label><FaUtensils className="me-2" />Food Name</Form.Label>
                      <Form.Control 
                        name="name"
                        placeholder="e.g. Fresh Pasta Salad" 
                        onChange={handleChange}
                        required 
                      />
                    </Form.Group>
                  </Col>
                  <Col md={6}>
                    <Form.Group className="mb-3">
                      <Form.Label>Food Type</Form.Label>
                      <Form.Select name="type" onChange={handleChange}>
                        <option value="Vegetarian">Vegetarian</option>
                        <option value="Non-Vegetarian">Non-Vegetarian</option>
                        <option value="Vegan">Vegan</option>
                        <option value="Other">Other</option>
                      </Form.Select>
                    </Form.Group>
                  </Col>
                </Row>

                <Form.Group className="mb-3">
                  <Form.Label>Description</Form.Label>
                  <Form.Control 
                    name="description"
                    as="textarea" 
                    rows={3} 
                    placeholder="Describe the food, ingredients, or any allergens..." 
                    onChange={handleChange}
                    required
                  />
                </Form.Group>

                <Row className="mb-3">
                  <Col md={6}>
                    <Form.Group className="mb-3">
                      <Form.Label><FaUsers className="me-2" />Available Servings</Form.Label>
                      <Form.Control 
                        name="servings"
                        type="number" 
                        min="1"
                        step="0.5"
                        value={formData.servings}
                        onChange={handleChange}
                        required 
                      />
                    </Form.Group>
                  </Col>
                  <Col md={6}>
                    <Form.Group className="mb-3">
                      <Form.Label><FaClock className="me-2" />Best Before / Claim By</Form.Label>
                      <Form.Control 
                        name="claimByTime"
                        type="datetime-local" 
                        onChange={handleChange}
                        required 
                      />
                    </Form.Group>
                  </Col>
                </Row>

                <Form.Group className="mb-3">
                  <Form.Label><FaImage className="me-2" />Photo URL</Form.Label>
                  <Form.Control 
                    name="photoUrl"
                    type="url" 
                    placeholder="https://example.com/food.jpg" 
                    onChange={handleChange}
                    required 
                  />
                  <Form.Text className="text-muted">Use a direct link to an image of the food.</Form.Text>
                </Form.Group>

                <Form.Group className="mb-3">
                  <Form.Label><FaMapMarkerAlt className="me-2" />Pin Pickup Location</Form.Label>
                  <div className="border rounded overflow-hidden mb-2" style={{ height: '350px', width: '100%' }}>
                    <MapContainer 
                        center={[20.5937, 78.9629]} // Default center (India)
                        zoom={5} 
                        scrollWheelZoom={true} 
                        style={{ height: '100%', width: '100%' }}
                    >
                        <TileLayer
                            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                        />
                        <LocationMarker />
                    </MapContainer>
                  </div>
                  {formData.latitude !== 0 ? (
                       <div className="bg-light p-2 rounded border-start border-success border-4 d-inline-block">
                           <span className="text-success fw-bold">
                               <FaMapMarkerAlt /> Location set: {formData.latitude.toFixed(6)}, {formData.longitude.toFixed(6)}
                           </span>
                       </div>
                  ) : (
                      <Alert variant="info" className="py-2 px-3 mb-0 d-inline-block small">
                          Please click on the map to set the exact pickup location.
                      </Alert>
                  )}
                </Form.Group>

                <Form.Group className="mb-3">
                  <Form.Label>Pickup Address (Detailed text)</Form.Label>
                  <Form.Control 
                    name="address"
                    placeholder="e.g. Apartment, Street, Landmark" 
                    onChange={handleChange}
                    required 
                  />
                </Form.Group>

                <Form.Group className="mb-4">
                  <Form.Label><FaPhone className="me-2" />Pickup Contact Number</Form.Label>
                  <Form.Control 
                    name="phoneNumber"
                    placeholder="1234567890" 
                    onChange={handleChange}
                    required 
                  />
                </Form.Group>

                <div className="d-flex gap-2">
                  <Button variant="success" type="submit" className="px-5 py-2 fw-bold" disabled={loading}>
                    {loading ? 'Posting...' : 'Post Donation'}
                  </Button>
                  <Button variant="outline-secondary" onClick={() => navigate(-1)} className="px-4">
                    Cancel
                  </Button>
                </div>
              </Form>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </Container>
  );
};

export default CreateListing;