use mcmovies;

CREATE TABLE IF NOT EXISTS watched_movies (
    user_id INT NOT NULL,
    movie_id INT NOT NULL,
    rating FLOAT NOT NULL,
    review VARCHAR(200) NOT NULL,
    watched_on DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, movie_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);