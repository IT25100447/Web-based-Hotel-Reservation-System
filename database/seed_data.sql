-- Branches
INSERT INTO Branch (name, address, contact_number) VALUES
('SmartStay Colombo City', '100 Galle Road, Colombo 03, Sri Lanka', '+94 11 234 5678'),
('SmartStay Kandy Hilltop', '45 Rajapihilla Mawatha, Kandy, Sri Lanka', '+94 81 223 3445'),
('SmartStay Galle Beach Resort', '12 Light House Street, Galle Fort, Sri Lanka', '+94 91 224 8899');

-- Staff (Matching Proposal Stakeholders)
-- Password is 'Password123!'
INSERT INTO Staff (branch_id, supervisor_id, name, email, phone, password, role, hire_date, status, target_bookings, access_level, approval_level, response_sla_hrs) VALUES
(1, NULL, 'Nuwan Jayasinghe', 'manager@smartstay.com', '+94 77 111 2233', '$2a$10$wN9a56qP1vDqfJ.M2gUuO.qXlIcfD4C4pC70jRkE7jJm19L27/7re', 'MANAGER', '2022-01-15', 'ACTIVE', 100, 'LEVEL_3', 'EXECUTIVE', 12),
(1, 1, 'Kasun Wijeratne', 'it@smartstay.com', '+94 77 222 3344', '$2a$10$wN9a56qP1vDqfJ.M2gUuO.qXlIcfD4C4pC70jRkE7jJm19L27/7re', 'IT', '2022-03-01', 'ACTIVE', NULL, 'ADMIN', 'SYSTEM', 4),
(1, 1, 'Chathuri Perera', 'frontoffice@smartstay.com', '+94 77 333 4455', '$2a$10$wN9a56qP1vDqfJ.M2gUuO.qXlIcfD4C4pC70jRkE7jJm19L27/7re', 'FRONT_OFFICE', '2022-05-10', 'ACTIVE', NULL, 'LEVEL_2', 'OPERATIONAL', 2),
(1, 1, 'Dilshan Fernando', 'reservations@smartstay.com', '+94 77 444 5566', '$2a$10$wN9a56qP1vDqfJ.M2gUuO.qXlIcfD4C4pC70jRkE7jJm19L27/7re', 'RESERVATIONS', '2022-06-20', 'ACTIVE', 50, 'LEVEL_1', 'STANDARD', 6),
(1, 1, 'Ishara Senanayake', 'relations@smartstay.com', '+94 77 555 6677', '$2a$10$wN9a56qP1vDqfJ.M2gUuO.qXlIcfD4C4pC70jRkE7jJm19L27/7re', 'GUEST_RELATIONS', '2022-07-01', 'ACTIVE', NULL, 'LEVEL_2', 'OPERATIONAL', 4),
(1, 1, 'Samantha Alwis', 'finance@smartstay.com', '+94 77 666 7788', '$2a$10$wN9a56qP1vDqfJ.M2gUuO.qXlIcfD4C4pC70jRkE7jJm19L27/7re', 'FINANCE', '2022-04-12', 'ACTIVE', NULL, 'LEVEL_2', 'FINANCIAL', 8);

-- Guests
INSERT INTO Guest (name, email, phone, address, password) VALUES
('Kamal Perera', 'guest@smartstay.com', '+94 71 999 8888', 'No 25 Temple Road, Nugegoda', '$2a$10$wN9a56qP1vDqfJ.M2gUuO.qXlIcfD4C4pC70jRkE7jJm19L27/7re'),
('Sarah Jenkins', 'sarah.j@example.com', '+44 79 1112 3344', '14 Victoria Street, London, UK', '$2a$10$wN9a56qP1vDqfJ.M2gUuO.qXlIcfD4C4pC70jRkE7jJm19L27/7re');

-- Rooms
INSERT INTO Room (branch_id, room_number, type, price, status) VALUES
(1, '101', 'Deluxe Ocean View', 120.00, 'AVAILABLE'),
(1, '102', 'Deluxe Ocean View', 120.00, 'AVAILABLE'),
(1, '201', 'Executive Suite', 250.00, 'AVAILABLE'),
(1, '202', 'Presidential Penthouse', 450.00, 'AVAILABLE'),
(1, '301', 'Standard King', 85.00, 'OCCUPIED'),
(1, '302', 'Standard Twin', 80.00, 'MAINTENANCE'),
(2, 'K-101', 'Mountain Panorama Villa', 160.00, 'AVAILABLE'),
(2, 'K-102', 'Hillside Luxury Suite', 210.00, 'AVAILABLE'),
(3, 'G-101', 'Heritage Colonial Suite', 190.00, 'AVAILABLE'),
(3, 'G-102', 'Beachfront Cabana', 220.00, 'AVAILABLE');

-- Room Images (Direct SVG / web safe links for demonstration)
INSERT INTO RoomImage (room_id, image_url) VALUES
(1, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=800&q=80'),
(2, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=800&q=80'),
(3, 'https://images.unsplash.com/photo-1566665797739-1674de7a421a?auto=format&fit=crop&w=800&q=80'),
(4, 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?auto=format&fit=crop&w=800&q=80'),
(7, 'https://images.unsplash.com/photo-1618773928121-c32242e63f39?auto=format&fit=crop&w=800&q=80'),
(9, 'https://images.unsplash.com/photo-1591088398332-8a7791972843?auto=format&fit=crop&w=800&q=80'),
(10, 'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?auto=format&fit=crop&w=800&q=80');

-- Room Facilities
INSERT INTO RoomFacility (room_id, facility_name) VALUES
(1, 'High-Speed Wi-Fi'), (1, 'Ocean Balcony'), (1, 'King Bed'), (1, 'Mini Bar'), (1, 'Air Conditioning'),
(2, 'High-Speed Wi-Fi'), (2, 'Ocean Balcony'), (2, 'Twin Beds'), (2, 'Mini Bar'), (2, 'Air Conditioning'),
(3, 'King Bed'), (3, 'Living Area'), (3, 'Jacuzzi'), (3, 'Work Desk'), (3, 'Complimentary Breakfast'),
(4, 'Private Terrace'), (4, 'Butler Service'), (4, 'Panoramic Ocean View'), (4, 'Luxury Bath'), (4, 'Champagne Lounge'),
(7, 'Mountain View'), (7, 'Fireplace'), (7, 'Private Garden'), (7, 'Heated Bed'),
(9, 'Colonial Decor'), (9, 'Teak Wood Furnishing'), (9, 'Veranda'), (9, 'High Ceiling');

-- Promotions
INSERT INTO Promotion (name, discount_pct, start_date, end_date) VALUES
('Summer Early Bird Discount', 15.00, '2026-05-01', '2026-10-31'),
('Weekend Luxury Getaway', 10.00, '2026-01-01', '2026-12-31');

-- Room Promotion Link
INSERT INTO Room_Promotion (room_id, promo_id) VALUES
(1, 1),
(3, 1),
(3, 2),
(7, 2);

-- Sample Reservation
INSERT INTO Reservation (guest_id, room_id, entered_by_staff_id, booking_date, check_in_date, check_out_date, num_guests, status) VALUES
(1, 1, 4, '2026-09-15 10:30:00', '2026-10-01', '2026-10-04', 2, 'CONFIRMED'),
(2, 7, NULL, '2026-09-10 14:15:00', '2026-10-10', '2026-10-15', 2, 'CONFIRMED');

-- Special Requests
INSERT INTO SpecialRequest (reservation_id, type, description) VALUES
(1, 'BEDDING', 'High floor requested with extra feather pillows.'),
(1, 'CHECK_IN', 'Estimated arrival around 1:00 PM.');

-- Invoices
-- Res 1: 3 nights * $120 = $360. Promo 15% = $306.00
INSERT INTO Invoice (reservation_id, total_amount, outstanding_balance, issue_date) VALUES
(1, 306.00, 106.00, '2026-09-15 10:30:00'),
(2, 800.00, 800.00, '2026-09-10 14:15:00');

-- Payments
INSERT INTO Payment (reservation_id, processed_by_staff_id, method, amount, payment_date, reference_number, status) VALUES
(1, 4, 'CARD', 200.00, '2026-09-15 10:35:00', 'TXN-CARD-992384', 'VERIFIED');

-- Feedback & Complaint
INSERT INTO Feedback (guest_id, reservation_id, handled_by_staff_id, rating, comments, category, status, submitted_date) VALUES
(1, 1, 5, 4, 'Great booking experience! Excited for our upcoming vacation.', 'SERVICE', 'RESOLVED', '2026-09-16 09:00:00');

-- Activity Log
INSERT INTO ActivityLog (staff_id, action, timestamp) VALUES
(1, 'System initialized with initial branch and staff setup', '2026-09-01 08:00:00'),
(4, 'Created reservation #1 for guest Kamal Perera', '2026-09-15 10:30:00'),
(4, 'Recorded card deposit payment of $200.00 for reservation #1', '2026-09-15 10:35:00'),
(5, 'Reviewed guest feedback ticket #1', '2026-09-16 09:15:00');
