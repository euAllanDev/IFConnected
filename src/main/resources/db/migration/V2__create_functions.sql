CREATE OR REPLACE FUNCTION follow_user(p_follower BIGINT, p_followed BIGINT)
RETURNS VOID AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM follows WHERE follower_id = p_follower AND followed_id = p_followed
    ) THEN
        INSERT INTO follows (follower_id, followed_id)
        VALUES (p_follower, p_followed);
    END IF;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION create_user(
    p_username VARCHAR,
    p_email VARCHAR,
    p_bio TEXT,
    p_image VARCHAR
)
RETURNS BIGINT AS $$
DECLARE new_id BIGINT;
BEGIN
    INSERT INTO users (username, email, bio, profile_image_url)
    VALUES (p_username, p_email, p_bio, p_image)
    RETURNING id INTO new_id;

    RETURN new_id;
END;
$$ LANGUAGE plpgsql;