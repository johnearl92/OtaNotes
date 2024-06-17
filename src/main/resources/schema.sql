CREATE TABLE IF NOT EXISTS note (
                                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      title VARCHAR(255) NOT NULL,
                                      body VARCHAR(5000) NOT NULL
);