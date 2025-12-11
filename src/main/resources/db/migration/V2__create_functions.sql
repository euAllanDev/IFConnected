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