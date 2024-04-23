CREATE TYPE tournament_status AS ENUM ('active', 'completed', 'cancelled', 'pending');
CREATE TYPE exercise_type AS ENUM ('pushups', 'situps');


create table users(
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    elo int,
    name VARCHAR,
    bio TEXT,
    image TEXT
);

create table tournaments(
    id SERIAL PRIMARY KEY,
    start_time TIMESTAMP,
    status tournament_status NOT NULL DEFAULT 'pending'
);

create table tournament_participants(
    tournament_id int not null,
    user_id int not null,
    placement int,
    score int,
    FOREIGN KEY (tournament_id) REFERENCES tournaments(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    PRIMARY KEY (tournament_id, user_id)
);

create table stats(
    id SERIAL PRIMARY KEY,
    type exercise_type NOT NULL DEFAULT 'pushups',
    duration int,
    count int,
    user_id int,
    tournament_id int,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (tournament_id) REFERENCES  tournaments(id) ON DELETE SET NULL
);
