CREATE DATABASE pureskin;
use pureskin;
CREATE TABLE quotes (
    ID_QUOTE INT NOT NULL AUTO_INCREMENT,
    QUOTE VARCHAR(100) NOT NULL,
    AUTOR VARCHAR(30) NOT NULL,
    PRIMARY KEY(ID_QUOTE)
);

-- Sample data for quotes
INSERT INTO quotes (QUOTE, AUTOR) 
VALUES ("Self-care is giving the world the best of you, instead of what's left of you.", "Katie Reed");
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL,
    username varchar(255),
    password_hash TEXT NOT NULL,
    reset_token VARCHAR(256),
    token_expires DATETIME
);


select * from quotes;
CREATE TABLE sessions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    session_token VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP DEFAULT (CURRENT_TIMESTAMP + INTERVAL 1 MONTH),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Table: daily_quote
CREATE TABLE daily_quote (
    ID_DAILY_QUOTE INT NOT NULL AUTO_INCREMENT,
    ID_QUOTE INT NOT NULL,
    QUOTE_DATE DATE NOT NULL,
    PRIMARY KEY (ID_DAILY_QUOTE),
    FOREIGN KEY (ID_QUOTE) REFERENCES quotes(ID_QUOTE)
);
select * from user_details;
select * from users;
CREATE TABLE user_details (
    id_user INT NOT NULL,
    varsta INT,
    sex ENUM('Male', 'Female', 'Other'),
    skin_type SET('Normal Skin', 'Karma Skin', 'Dry Skin', 'Oily Skin'),
    skin_sensitivity SET('Not sensitive at all', 'Somewhat sensitive', 'Very sensitive'), 
    skin_phototype SET('Pale white skin', 'White skin', 'Light brown skin', 'Moderate brown skin', 'Dark brown skin', 'Deep brown skin'),
    concerns SET(
        'Acne & Blemishes', 'Anti-aging', 'Black Heads', 'Dark Circles',
        'Dark Spots', 'Dryness', 'Dullness', 'Fine Lines & Wrinkles',
        'Loss of Firmness', 'Oiliness', 'Puffiness', 'Redness',
        'Uneven Texture', 'Visible Pores'
    ),
    poza_profil BLOB,
    PRIMARY KEY (id_user),
    FOREIGN KEY (id_user) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE routines (
    id INT AUTO_INCREMENT PRIMARY KEY,
    routine_type ENUM('morning', 'evening', 'exfoliation', 'face mask', 'eye mask', 'lip mask') NOT NULL,
    user_id INT NOT NULL,
    notification_time TIME NOT NULL,
    notification_days SET('monday', 'tuesday', 'wednesday', 'thursday', 'friday', 'saturday', 'sunday') NOT NULL
);

-- Table: steps
CREATE TABLE steps (
    id INT AUTO_INCREMENT PRIMARY KEY,
    routine_id INT NOT NULL,
    step_order INT NOT NULL,
    step_name VARCHAR(100) NOT NULL,
    description TEXT,
    product_id INT,
    FOREIGN KEY (routine_id) REFERENCES routines(id) ON DELETE CASCADE
);

-- Table: spf_routines
CREATE TABLE spf_routines (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    interval_minutes INT NOT NULL,
    active_days SET('monday', 'tuesday', 'wednesday', 'thursday', 'friday', 'saturday', 'sunday') NOT NULL,
    product_id INT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Table: recommended_routines
CREATE TABLE recommended_routines (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    routine_type ENUM('morning', 'evening', 'exfoliation', 'face mask', 'eye mask', 'lip mask') NOT NULL,
    step_name VARCHAR(100) NOT NULL,
    product_id INT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Table: daily_logs
CREATE TABLE daily_logs (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user (user_id)
);

-- Table: daily_log_content
CREATE TABLE daily_log_content (
    id INT PRIMARY KEY AUTO_INCREMENT,
    daily_log_id INT NOT NULL, 
    skin_feeling_score INT, 
    skin_condition ENUM('normal', 'oily', 'dehydrated', 'itchy') NULL,
    notes TEXT,
    weather ENUM('sunny', 'cloudy', 'precipitations') NULL, 
    stress_level INT,
    log_date DATE NOT NULL DEFAULT (CURRENT_DATE),  
    FOREIGN KEY (daily_log_id) REFERENCES daily_logs(id) ON DELETE CASCADE
);

-- Table: routine_completions
CREATE TABLE routine_completions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    completion_date DATE NOT NULL DEFAULT (CURRENT_DATE),
    user_id INT NOT NULL,
    routine_id INT NULL, 
    steps TEXT NULL,
    spf_id INT NULL,
    FOREIGN KEY (routine_id) REFERENCES routines(id) ON DELETE CASCADE,
    FOREIGN KEY (spf_id) REFERENCES spf_routines(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE messages (
    message_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    sender ENUM('bot', 'user') NOT NULL,
    text TEXT NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

select * from users;

CREATE TABLE recommended_routines (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    routine_type ENUM('morning', 'evening', 'exfoliation', 'face mask', 'eye mask', 'lip mask') NOT NULL,
    step_name VARCHAR(100) NOT NULL,
    product_id INT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

select * from users;
select * from user_details;
select * from sessions;

select * from routine_completions;

CREATE TABLE routine_completions_details (
    id INT PRIMARY KEY AUTO_INCREMENT,
    completion_date DATE NOT NULL DEFAULT (CURRENT_DATE),
    user_id INT NOT NULL,
    routine_type ENUM('morning', 'evening', 'exfoliation', 'face mask', 'eye mask', 'lip mask','spf'), 
    steps TEXT NULL,
    max_steps INT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
use pureskin;
desc routine_completions_details;
select * from routine_completions_details;
select * from daily_log_content;


select * from routine_completions;

select * from routine_completions;




