CREATE DATABASE IF NOT EXISTS vietravel_booking
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE vietravel_booking;
-- Destination ↔ Tour Category (nhiều-nhiều)
CREATE TABLE destination_category_map(
  destination_id BIGINT NOT NULL,
  category_id BIGINT NOT NULL,
  PRIMARY KEY(destination_id, category_id),

  CONSTRAINT fk_dcm_destination FOREIGN KEY(destination_id) REFERENCES destination(id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_dcm_category FOREIGN KEY(category_id) REFERENCES tour_category(id)
    ON DELETE CASCADE ON UPDATE CASCADE,

  INDEX idx_dcm_category(category_id)
) ENGINE=InnoDB;

-- =========================
CREATE TABLE destination(
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(150) NOT NULL,
  type ENUM('COUNTRY','PROVINCE','CITY','ATTRACTION') NOT NULL,
  parent_id BIGINT NULL,
  slug VARCHAR(200) NOT NULL UNIQUE,
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  CONSTRAINT fk_destination_parent FOREIGN KEY(parent_id) REFERENCES destination(id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  INDEX idx_destination_type(type),
  INDEX idx_destination_parent(parent_id),
  INDEX idx_destination_active(is_active)
) ENGINE=InnoDB;

CREATE TABLE email_verification_codes(
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  email VARCHAR(255) NOT NULL,
  code_hash VARCHAR(255) NOT NULL,
  expires_at DATETIME NOT NULL,
  used_at DATETIME NULL,
  attempt_count INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_evc_email(email),
  INDEX idx_evc_expires(expires_at)
) ENGINE=InnoDB;

-- =========================
-- 0) MASTER DATA (Admin CRUD)
-- =========================

-- Dòng tour (Admin quản lý)
CREATE TABLE tour_lines(
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(30) NOT NULL UNIQUE,      -- CAO_CAP / TIEU_CHUAN / TIET_KIEM
  name VARCHAR(100) NOT NULL,            -- Tên hiển thị
  min_price DECIMAL(12,2) NOT NULL,      -- Giá tối thiểu
  max_price DECIMAL(12,2) NOT NULL,      -- Giá tối đa
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  sort_order INT NOT NULL DEFAULT 0,
  CHECK (min_price <= max_price),
  INDEX idx_tour_lines_active(is_active),
  INDEX idx_tour_lines_price(min_price, max_price),
  INDEX idx_tour_lines_sort(sort_order)
) ENGINE=InnoDB;


-- Phương tiện (Admin quản lý)
CREATE TABLE transport_modes(
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(30) NOT NULL UNIQUE,   -- VD: XE/MAY_BAY/TAU_THUY
  name VARCHAR(100) NOT NULL,         -- Tên hiển thị tiếng Việt
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  sort_order INT NOT NULL DEFAULT 0,
  INDEX idx_transport_active(is_active),
  INDEX idx_transport_sort(sort_order)
) ENGINE=InnoDB;

-- =========================
-- 1) USERS & PROFILE
-- =========================
CREATE TABLE users(
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  email VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role ENUM('CUSTOMER','STAFF','ADMIN') NOT NULL,
  status ENUM('PENDING','ACTIVE','LOCKED') NOT NULL DEFAULT 'ACTIVE',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_users_role(role),
  INDEX idx_users_status(status)
) ENGINE=InnoDB;

CREATE TABLE user_profile(
  user_id BIGINT PRIMARY KEY,
  full_name VARCHAR(200) NOT NULL,
  phone VARCHAR(30) NULL,
  gender VARCHAR(10) NULL,
  dob DATE NULL,
  address VARCHAR(300) NULL,
  avatar VARCHAR(500) NULL,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_profile_user FOREIGN KEY(user_id) REFERENCES users(id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  INDEX idx_profile_phone(phone)
) ENGINE=InnoDB;

-- =========================
-- 1B) NOTIFICATIONS
-- =========================
CREATE TABLE notifications(
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  title VARCHAR(200) NOT NULL,
  message VARCHAR(800) NOT NULL,
  type ENUM('INFO','SUCCESS','WARNING','ERROR') NOT NULL DEFAULT 'INFO',
  link VARCHAR(500) NULL,
  is_read TINYINT(1) NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  read_at DATETIME NULL,
  CONSTRAINT fk_notifications_user FOREIGN KEY(user_id) REFERENCES users(id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  INDEX idx_notifications_user(user_id),
  INDEX idx_notifications_read(user_id, is_read),
  INDEX idx_notifications_created(created_at)
) ENGINE=InnoDB;

-- =========================
-- 2) CATEGORIES (multi-level)
-- =========================
CREATE TABLE tour_category(
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(150) NOT NULL,
  parent_id BIGINT NULL,
  slug VARCHAR(200) NOT NULL UNIQUE,
  sort_order INT NOT NULL DEFAULT 0,
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  CONSTRAINT fk_category_parent FOREIGN KEY(parent_id) REFERENCES tour_category(id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  INDEX idx_category_parent(parent_id),
  INDEX idx_category_active(is_active)
) ENGINE=InnoDB;

-- =========================
-- 3) DESTINATIONS (deep filter: city/attraction)
-- =========================
CREATE TABLE destination(
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(150) NOT NULL,
  type ENUM('COUNTRY','PROVINCE','CITY','ATTRACTION') NOT NULL,
  parent_id BIGINT NULL,
  slug VARCHAR(200) NOT NULL UNIQUE,
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  CONSTRAINT fk_destination_parent FOREIGN KEY(parent_id) REFERENCES destination(id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  INDEX idx_destination_type(type),
  INDEX idx_destination_parent(parent_id),
  INDEX idx_destination_active(is_active)
) ENGINE=InnoDB;

-- =========================
-- 4) TOURS (chuẩn hoá tour_line_id, transport_mode_id, start_location_id)
-- =========================
CREATE TABLE tours(
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(30) NOT NULL UNIQUE,
  title VARCHAR(300) NOT NULL,
  slug VARCHAR(350) NOT NULL UNIQUE,

  tour_line_id BIGINT NOT NULL,
  transport_mode_id BIGINT NOT NULL,

  duration_days INT NOT NULL,
  duration_nights INT NOT NULL,

  start_location_id BIGINT NULL, -- điểm khởi hành (VD: Hà Nội/TP.HCM) -> destination (CITY)

  base_price DECIMAL(12,2) NULL,

  summary TEXT NULL,
  overview_html MEDIUMTEXT NULL,
  additional_info_html MEDIUMTEXT NULL,
  notes_html MEDIUMTEXT NULL,

  is_active TINYINT(1) NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  CONSTRAINT fk_tours_tour_line FOREIGN KEY(tour_line_id) REFERENCES tour_lines(id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_tours_transport_mode FOREIGN KEY(transport_mode_id) REFERENCES transport_modes(id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_tours_start_location FOREIGN KEY(start_location_id) REFERENCES destination(id)
    ON DELETE SET NULL ON UPDATE CASCADE,

  INDEX idx_tours_line(tour_line_id),
  INDEX idx_tours_transport(transport_mode_id),
  INDEX idx_tours_start_location(start_location_id),
  INDEX idx_tours_active(is_active),
  INDEX idx_tours_duration(duration_days, duration_nights)
) ENGINE=InnoDB;

CREATE TABLE tour_category_map(
  tour_id BIGINT NOT NULL,
  category_id BIGINT NOT NULL,
  PRIMARY KEY(tour_id, category_id),
  CONSTRAINT fk_tcm_tour FOREIGN KEY(tour_id) REFERENCES tours(id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_tcm_category FOREIGN KEY(category_id) REFERENCES tour_category(id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  INDEX idx_tcm_category(category_id)
) ENGINE=InnoDB;

CREATE TABLE tour_destinations(
  tour_id BIGINT NOT NULL,
  destination_id BIGINT NOT NULL,
  PRIMARY KEY(tour_id, destination_id),
  CONSTRAINT fk_td_tour FOREIGN KEY(tour_id) REFERENCES tours(id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_td_destination FOREIGN KEY(destination_id) REFERENCES destination(id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  INDEX idx_td_destination(destination_id)
) ENGINE=InnoDB;

CREATE TABLE tour_images(
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tour_id BIGINT NOT NULL,
  image_url VARCHAR(600) NOT NULL,
  is_thumbnail TINYINT(1) NOT NULL DEFAULT 0,
  sort_order INT NOT NULL DEFAULT 0,
  CONSTRAINT fk_tour_images_tour FOREIGN KEY(tour_id) REFERENCES tours(id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  INDEX idx_tour_images_tour(tour_id),
  INDEX idx_tour_images_thumb(tour_id, is_thumbnail),
  INDEX idx_tour_images_sort(tour_id, sort_order)
) ENGINE=InnoDB;

CREATE TABLE itinerary_days(
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tour_id BIGINT NOT NULL,
  day_no INT NOT NULL,
  title_route VARCHAR(300) NOT NULL,
  meals VARCHAR(30) NULL,          -- VD: SANG,TRUA,TOI
  content_html MEDIUMTEXT NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  CONSTRAINT fk_itinerary_tour FOREIGN KEY(tour_id) REFERENCES tours(id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  UNIQUE KEY uq_itinerary_day(tour_id, day_no),
  INDEX idx_itinerary_sort(tour_id, sort_order)
) ENGINE=InnoDB;

-- =========================
-- 5) DEPARTURES (chuẩn hoá điểm khởi hành bằng destination_id)
-- =========================
CREATE TABLE departures(
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tour_id BIGINT NOT NULL,
  start_date DATE NOT NULL,
  capacity INT NOT NULL,
  available INT NOT NULL,
  price_adult DECIMAL(12,2) NOT NULL,
  price_child DECIMAL(12,2) NOT NULL,

  start_location_id BIGINT NOT NULL, -- Hà Nội/TP.HCM -> destination(CITY)

  status ENUM('OPEN','CLOSED') NOT NULL DEFAULT 'OPEN',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  CONSTRAINT fk_departure_tour FOREIGN KEY(tour_id) REFERENCES tours(id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_departure_start_location FOREIGN KEY(start_location_id) REFERENCES destination(id)
    ON DELETE RESTRICT ON UPDATE CASCADE,

  UNIQUE KEY uq_departure_date(tour_id, start_date, start_location_id),
  INDEX idx_departure_tour(tour_id),
  INDEX idx_departure_date(start_date),
  INDEX idx_departure_status(status),
  INDEX idx_departure_price(price_adult)
) ENGINE=InnoDB;

-- =========================
-- 6) WISHLIST
-- =========================
CREATE TABLE wishlist(
  user_id BIGINT NOT NULL,
  tour_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY(user_id, tour_id),
  CONSTRAINT fk_wishlist_user FOREIGN KEY(user_id) REFERENCES users(id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_wishlist_tour FOREIGN KEY(tour_id) REFERENCES tours(id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  INDEX idx_wishlist_tour(tour_id)
) ENGINE=InnoDB;

-- =========================
-- 7) BOOKINGS / PASSENGERS / PAYMENTS
-- =========================
CREATE TABLE bookings(
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  booking_code VARCHAR(40) NOT NULL UNIQUE,
  user_id BIGINT NULL,
  created_by_staff_id BIGINT NULL,
  departure_id BIGINT NOT NULL,

  contact_name VARCHAR(200) NOT NULL,
  contact_phone VARCHAR(30) NOT NULL,
  contact_email VARCHAR(255) NOT NULL,
  note VARCHAR(500) NULL,

  total_adult INT NOT NULL DEFAULT 0,
  total_child INT NOT NULL DEFAULT 0,
  total_amount DECIMAL(12,2) NOT NULL DEFAULT 0,

  status ENUM('PENDING','PAID','CONFIRMED','CANCEL_REQUESTED','CANCELED','COMPLETED') NOT NULL DEFAULT 'PENDING',

  cancel_requested_at DATETIME NULL,
  canceled_at DATETIME NULL,
  refund_amount DECIMAL(12,2) NULL,

  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  CONSTRAINT fk_booking_user FOREIGN KEY(user_id) REFERENCES users(id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_booking_staff FOREIGN KEY(created_by_staff_id) REFERENCES users(id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_booking_departure FOREIGN KEY(departure_id) REFERENCES departures(id)
    ON DELETE RESTRICT ON UPDATE CASCADE,

  INDEX idx_booking_user(user_id),
  INDEX idx_booking_departure(departure_id),
  INDEX idx_booking_status(status),
  INDEX idx_booking_created_at(created_at)
) ENGINE=InnoDB;

CREATE TABLE booking_passengers(
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  booking_id BIGINT NOT NULL,
  full_name VARCHAR(200) NOT NULL,
  dob DATE NULL,
  passenger_type ENUM('ADULT','CHILD') NOT NULL,
  id_number VARCHAR(50) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_passenger_booking FOREIGN KEY(booking_id) REFERENCES bookings(id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  INDEX idx_passenger_booking(booking_id),
  INDEX idx_passenger_type(passenger_type)
) ENGINE=InnoDB;

CREATE TABLE payments(
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  booking_id BIGINT NOT NULL,
  payment_type ENUM('PAY','REFUND') NOT NULL,
  method ENUM('VNPAY_MOCK','CASH') NOT NULL DEFAULT 'VNPAY_MOCK',
  status ENUM('INIT','SUCCESS','FAILED','REFUNDED') NOT NULL,
  amount DECIMAL(12,2) NOT NULL,
  txn_ref VARCHAR(60) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_payment_booking FOREIGN KEY(booking_id) REFERENCES bookings(id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  INDEX idx_payment_booking(booking_id),
  INDEX idx_payment_status(status),
  INDEX idx_payment_txn(txn_ref)
) ENGINE=InnoDB;

-- =========================
-- 8) NEWS
-- =========================
CREATE TABLE news_category(
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(150) NOT NULL,
  slug VARCHAR(200) NOT NULL UNIQUE,
  is_active TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB;

CREATE TABLE news(
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  category_id BIGINT NOT NULL,
  title VARCHAR(300) NOT NULL,
  slug VARCHAR(350) NOT NULL UNIQUE,
  thumbnail VARCHAR(600) NULL,
  summary TEXT NULL,
  content_html MEDIUMTEXT NOT NULL,
  is_featured TINYINT(1) NOT NULL DEFAULT 0,
  status ENUM('DRAFT','PUBLISHED','HIDDEN') NOT NULL DEFAULT 'PUBLISHED',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_news_category FOREIGN KEY(category_id) REFERENCES news_category(id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  INDEX idx_news_category(category_id),
  INDEX idx_news_featured(is_featured),
  INDEX idx_news_status(status),
  INDEX idx_news_created(created_at)
) ENGINE=InnoDB;

-- =========================
-- 9) FAQ Chatbot
-- =========================
CREATE TABLE faq(
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  question VARCHAR(400) NOT NULL,
  answer MEDIUMTEXT NOT NULL,
  keywords VARCHAR(500) NULL,
  category VARCHAR(100) NULL,
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_faq_active(is_active),
  INDEX idx_faq_sort(sort_order)
) ENGINE=InnoDB;

-- =========================
-- 10) CONTACT
-- =========================
CREATE TABLE contact_inquiries(
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NULL,
  info_type VARCHAR(50) NOT NULL, -- VD: Du lịch, Vé, Dịch vụ...
  full_name VARCHAR(200) NOT NULL,
  email VARCHAR(255) NOT NULL,
  phone VARCHAR(30) NOT NULL,
  company_name VARCHAR(200) NULL,
  guest_count INT NULL DEFAULT 0,
  address VARCHAR(300) NULL,
  subject VARCHAR(300) NOT NULL,
  content TEXT NOT NULL,
  status ENUM('NEW','READ','RESOLVED') NOT NULL DEFAULT 'NEW',
  handled_by BIGINT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_contact_user FOREIGN KEY(user_id) REFERENCES users(id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_contact_handler FOREIGN KEY(handled_by) REFERENCES users(id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  INDEX idx_contact_status(status),
  INDEX idx_contact_created(created_at),
  INDEX idx_contact_user(user_id)
) ENGINE=InnoDB;

-- =========================
-- 11) BRANCHES (display only)
-- =========================
CREATE TABLE branches(
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  region VARCHAR(30) NOT NULL, -- VD: TP_HCM/MIEN_BAC/...
  name VARCHAR(250) NOT NULL,
  address VARCHAR(350) NOT NULL,
  hotline VARCHAR(120) NULL,
  email VARCHAR(255) NULL,
  fax VARCHAR(80) NULL,
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_branch_region(region),
  INDEX idx_branch_active(is_active),
  INDEX idx_branch_sort(sort_order)
) ENGINE=InnoDB;


USE vietravel_booking;
UPDATE user_profile u1
JOIN user_profile u7 ON u7.user_id = 7
SET u1.avatar = u7.avatar
WHERE u1.user_id = 1;

INSERT INTO user_profile (
    user_id,
    full_name,
    phone,
    gender,
    dob,
    address,
    avatar
)
SELECT
    4,
    full_name,
    phone,
    gender,
    dob,
    address,
    avatar
FROM user_profile
WHERE user_id = 5;




-- Add category_id to tours (nullable first for existing data)
ALTER TABLE tours
  ADD COLUMN category_id BIGINT NULL AFTER transport_mode_id;

ALTER TABLE tours
  ADD INDEX idx_tours_category(category_id);

ALTER TABLE tours
  ADD CONSTRAINT fk_tours_category FOREIGN KEY(category_id)
    REFERENCES tour_category(id)
    ON DELETE RESTRICT ON UPDATE CASCADE;

-- If you already have data, update category_id before making it NOT NULL
-- Example (pick a valid category id):
-- UPDATE tours SET category_id = 1 WHERE category_id IS NULL;

-- Then enforce NOT NULL if required
-- ALTER TABLE tours MODIFY COLUMN category_id BIGINT NOT NULL;

ALTER TABLE tours
  DROP FOREIGN KEY fk_tours_start_location,
  DROP COLUMN start_location_id,
  ADD COLUMN start_location ENUM('HN','DN','HCM') NOT NULL AFTER duration_nights;

ALTER TABLE tours
  ADD INDEX idx_tours_start_location(start_location);

ALTER TABLE departures
  DROP FOREIGN KEY fk_departure_start_location,
  DROP COLUMN start_location_id,
  ADD COLUMN start_location ENUM('HN','DN','HCM') NOT NULL AFTER price_child;

ALTER TABLE departures
  DROP INDEX uq_departure_date,
  ADD UNIQUE KEY uq_departure_date(tour_id, start_date, start_location);

ALTER TABLE departures
  ADD INDEX idx_departure_start_location(start_location);
  
ALTER TABLE destination
ADD COLUMN sort_order INT NOT NULL DEFAULT 0 AFTER is_active;

ALTER TABLE destination
  ADD CONSTRAINT fk_destination_category
    FOREIGN KEY(category_id) REFERENCES tour_category(id)
    ON DELETE RESTRICT ON UPDATE CASCADE;


DROP TABLE IF EXISTS destination_category_map;
DROP TABLE IF EXISTS destination;

ALTER TABLE tour_lines
ADD COLUMN min_price DECIMAL(12,2) NOT NULL AFTER name,
ADD COLUMN max_price DECIMAL(12,2) NOT NULL AFTER min_price;
ALTER TABLE tour_lines
ADD CONSTRAINT chk_tour_lines_price
CHECK (min_price <= max_price);
CREATE INDEX idx_tour_lines_price
ON tour_lines(min_price, max_price);

ALTER TABLE users
MODIFY COLUMN status ENUM('PENDING','ACTIVE','LOCKED') NOT NULL DEFAULT 'PENDING';

-- STAFF
INSERT INTO users(
  email,
  password_hash,
  role,
  status,
  created_at,
  updated_at
) VALUES (
  'staff@vietravel.com',
  '$2a$10$22Vhwj2ePNidbNjwzAGGf.AFpbLk.gNVWoTeQoCq3YInX4AqSqqCW',
  'STAFF',
  'ACTIVE',
  NOW(),
  NOW()
);

-- CUSTOMER
INSERT INTO users(
  email,
  password_hash,
  role,
  status,
  created_at,
  updated_at
) VALUES (
  'customer@vietravel.com',
  '$2a$10$22Vhwj2ePNidbNjwzAGGf.AFpbLk.gNVWoTeQoCq3YInX4AqSqqCW',
  'CUSTOMER',
  'ACTIVE',
  NOW(),
  NOW()
);
INSERT INTO users(
  email,
  password_hash,
  role,
  status,
  created_at,
  updated_at
) VALUES (
  'admin@vietravel.com',
  '$2a$10$22Vhwj2ePNidbNjwzAGGf.AFpbLk.gNVWoTeQoCq3YInX4AqSqqCW',
  'ADMIN',
  'ACTIVE',
  NOW(),
  NOW()
);
