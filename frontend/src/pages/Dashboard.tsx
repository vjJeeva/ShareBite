import React from 'react';
import { Container, Row, Col, Card } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { FaUtensils, FaHandsHelping, FaList, FaHistory } from 'react-icons/fa';

const Dashboard: React.FC = () => {
  const { user } = useAuth();

  return (
    <Container className="py-5 fade-in">
      <div className="mb-5">
        <h1 className="fw-bold text-success">Welcome, {user?.name}!</h1>
        <p className="text-muted lead">What would you like to do today?</p>
      </div>

      <Row className="g-4">
        {user?.role === 'DONOR' ? (
          <>
            <Col md={6}>
              <Link to="/create-listing" className="text-decoration-none">
                <Card className="h-100 p-4 text-center shadow-sm hover-shadow border-0">
                  <Card.Body>
                    <div className="mb-3 text-success">
                      <FaUtensils size={50} />
                    </div>
                    <Card.Title className="fw-bold fs-3 text-dark">Donate Food</Card.Title>
                    <Card.Text className="text-muted">
                      List your surplus food to help those in need. 
                      Quick and easy sharing.
                    </Card.Text>
                  </Card.Body>
                </Card>
              </Link>
            </Col>
            <Col md={6}>
              <Link to="/my-donations" className="text-decoration-none">
                <Card className="h-100 p-4 text-center shadow-sm hover-shadow border-0">
                  <Card.Body>
                    <div className="mb-3 text-primary">
                      <FaList size={50} />
                    </div>
                    <Card.Title className="fw-bold fs-3 text-dark">My Donations</Card.Title>
                    <Card.Text className="text-muted">
                      View and manage your active and past food listings.
                    </Card.Text>
                  </Card.Body>
                </Card>
              </Link>
            </Col>
          </>
        ) : (
          <>
            <Col md={6}>
              <Link to="/available" className="text-decoration-none">
                <Card className="h-100 p-4 text-center shadow-sm hover-shadow border-0">
                  <Card.Body>
                    <div className="mb-3 text-success">
                      <FaUtensils size={50} />
                    </div>
                    <Card.Title className="fw-bold fs-3 text-dark">Browse Food</Card.Title>
                    <Card.Text className="text-muted">
                      See what's available near you and claim food today.
                    </Card.Text>
                  </Card.Body>
                </Card>
              </Link>
            </Col>
            <Col md={6}>
              <Link to="/my-claims" className="text-decoration-none">
                <Card className="h-100 p-4 text-center shadow-sm hover-shadow border-0">
                  <Card.Body>
                    <div className="mb-3 text-primary">
                      <FaHistory size={50} />
                    </div>
                    <Card.Title className="fw-bold fs-3 text-dark">My Claims</Card.Title>
                    <Card.Text className="text-muted">
                      Track your active claims and view your history.
                    </Card.Text>
                  </Card.Body>
                </Card>
              </Link>
            </Col>
          </>
        )}
      </Row>
    </Container>
  );
};

export default Dashboard;
