-- ===========================
-- Funções de relacionamento
-- ===========================

CREATE OR REPLACE FUNCTION follow_user(p_follower BIGINT, p_followed BIGINT)
RETURNS VOID AS $$
BEGIN
    IF p_follower = p_followed THEN
        RAISE EXCEPTION 'Um usuário não pode seguir ele mesmo.';
END IF;

IF EXISTS (
        SELECT 1 FROM follows
        WHERE follower_id = p_follower
        AND followed_id = p_followed
    ) THEN
        RETURN;
END IF;

INSERT INTO follows (follower_id, followed_id)
VALUES (p_follower, p_followed);
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION unfollow_user(p_follower BIGINT, p_followed BIGINT)
RETURNS VOID AS $$
BEGIN
DELETE FROM follows
WHERE follower_id = p_follower
  AND followed_id = p_followed;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION is_following(p_follower BIGINT, p_followed BIGINT)
RETURNS BOOLEAN AS $$
DECLARE result BOOLEAN;
BEGIN
SELECT EXISTS (
    SELECT 1 FROM follows
    WHERE follower_id = p_follower
      AND followed_id = p_followed
) INTO result;

RETURN result;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION count_followers(p_user BIGINT)
RETURNS BIGINT AS $$
DECLARE total BIGINT;
BEGIN
SELECT COUNT(*)
    INTO total
FROM follows
WHERE followed_id = p_user;

RETURN total;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION count_following(p_user BIGINT)
RETURNS BIGINT AS $$
DECLARE total BIGINT;
BEGIN
SELECT COUNT(*)
    INTO total
FROM follows
WHERE follower_id = p_user;

RETURN total;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION get_following_ids(p_user BIGINT)
RETURNS TABLE(id BIGINT) AS $$
BEGIN
    RETURN QUERY
SELECT followed_id
FROM follows
WHERE follower_id = p_user;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION get_follower_ids(p_user BIGINT)
RETURNS TABLE(id BIGINT) AS $$
BEGIN
    RETURN QUERY
SELECT follower_id
FROM follows
WHERE followed_id = p_user;
END;
$$ LANGUAGE plpgsql;
