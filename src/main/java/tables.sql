create table users(
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    elo int,
    name VARCHAR,
    bio TEXT,
    image TEXT
);

create table stats(
    id SERIAL PRIMARY KEY,
    duration int,
    count int,
    user_id int,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
)