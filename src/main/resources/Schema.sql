CREATE TABLE IF NOT EXISTS users (
   id UUID DEFAULT random_uuid() PRIMARY KEY,
   name VARCHAR(255),
   email VARCHAR(255) UNIQUE,
   password VARCHAR(255),
   created TIMESTAMP DEFAULT NOW(),
   last_login TIMESTAMP DEFAULT NOW(),
   token VARCHAR(2000),
   is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS phones (
   id BIGINT AUTO_INCREMENT PRIMARY KEY,
   number BIGINT,
   citycode INTEGER,
   contrycode VARCHAR(10),
   user_id UUID,
   FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_user_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_phone_user_id ON phones(user_id);