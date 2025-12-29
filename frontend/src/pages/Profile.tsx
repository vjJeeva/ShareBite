import React, { useEffect, useState } from 'react';
import { Container, Row, Col, Card, Form, Button, Alert, Spinner } from 'react-bootstrap';
import api from '../api/api';
import type { Profile } from '../types';
import { FaUserCircle, FaEnvelope, FaPhone, FaIdCard } from 'react-icons/fa';
import { useAuth } from '../context/AuthContext';

const ProfilePage: React.FC = () => {
  const [profile, setProfile] = useState<Profile | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const { user } = useAuth();

  useEffect(() => {
    fetchProfile();
  }, []);

  const fetchProfile = async () => {
    try {
      const response = await api.get('/profile/me');
      setProfile(response.data);
    } catch (err) {
      setError('Failed to load profile details.');
    } finally {
      setLoading(false);
    }
  };

  const handleUpdate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!profile) return;
    
    setLoading(true);
    setError('');
    setSuccess('');

    try {
      await api.put('/profile/me', profile);
      setSuccess('Profile updated successfully!');
    } catch (err) {
      setError('Failed to update profile.');
    } finally {
      setLoading(false);
    }
  };

  if (loading && !profile) return <div className="text-center py-5"><Spinner animation="border" variant="success" /></div>;

  return (
    <Container className="py-5 fade-in">
      <Row className="justify-content-center">
        <Col md={8} lg={6}>
          <Card className="border-0 shadow-sm overflow-hidden">
            <div className="bg-success py-5 text-center text-white">
              <FaUserCircle size={80} className="mb-3" />
              <h3 className="fw-bold mb-0">{profile?.name || 'User Profile'}</h3>
              <p className="opacity-75">{user?.role}</p>
            </div>
            <Card.Body className="p-4">
              {error && <Alert variant="danger">{error}</Alert>}
              {success && <Alert variant="success">{success}</Alert>}

              <Form onSubmit={handleUpdate}>
                <Form.Group className="mb-3">
                  <Form.Label className="text-muted small fw-bold"><FaIdCard className="me-2" />Full Name</Form.Label>
                  <Form.Control 
                    value={profile?.name || ''} 
                    onChange={(e) => setProfile(prev => prev ? {...prev, name: e.target.value} : null)}
                    required
                  />
                </Form.Group>

                <Form.Group className="mb-3">
                  <Form.Label className="text-muted small fw-bold"><FaEnvelope className="me-2" />Email Address</Form.Label>
                  <Form.Control value={user?.email || ''} disabled />
                  <Form.Text className="text-muted">Email cannot be changed.</Form.Text>
                </Form.Group>

                <Form.Group className="mb-4">
                  <Form.Label className="text-muted small fw-bold"><FaPhone className="me-2" />Phone Number</Form.Label>
                  <Form.Control 
                    value={profile?.phoneNumber || ''} 
                    onChange={(e) => setProfile(prev => prev ? {...prev, phoneNumber: e.target.value} : null)}
                    required
                  />
                </Form.Group>

                <Button variant="success" type="submit" className="w-100 py-2 fw-bold" disabled={loading}>
                  {loading ? 'Updating...' : 'Save Changes'}
                </Button>
              </Form>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </Container>
  );
};

export default ProfilePage;
