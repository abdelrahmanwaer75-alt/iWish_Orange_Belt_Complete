CREATE DATABASE IF NOT EXISTS iwish CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE iwish;

CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(64) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS friendships (
    id INT PRIMARY KEY AUTO_INCREMENT,
    sender_id INT NOT NULL,
    receiver_id INT NOT NULL,
    status ENUM('PENDING','ACCEPTED','DECLINED') NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_friend_pair (sender_id, receiver_id),
    CONSTRAINT fk_fs FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_fr FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS items (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    default_price DECIMAL(12,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS wishlist_items (
    id INT PRIMARY KEY AUTO_INCREMENT,
    owner_id INT NOT NULL,
    item_id INT NULL,
    custom_name VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    price DECIMAL(12,2) NOT NULL,
    collected_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    status ENUM('OPEN','COMPLETED') NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_wl_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_wl_item FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS contributions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    wishlist_item_id INT NOT NULL,
    buyer_id INT NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_c_item FOREIGN KEY (wishlist_item_id) REFERENCES wishlist_items(id) ON DELETE CASCADE,
    CONSTRAINT fk_c_buyer FOREIGN KEY (buyer_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS notifications (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    message VARCHAR(500) NOT NULL,
    type VARCHAR(40) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_n_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

INSERT INTO items(name, description, default_price) VALUES
('PlayStation 5', 'Gaming console', 30000),
('Apple AirPods Pro', 'Wireless earbuds', 12000),
('Smart Watch', 'Modern smartwatch', 7000),
('Laptop', 'Portable computer', 45000),
('Perfume', 'Premium perfume', 3500)
ON DUPLICATE KEY UPDATE name=name;
