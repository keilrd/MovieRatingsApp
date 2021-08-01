DELIMITER //
DROP PROCEDURE IF EXISTS GetByFavorites;
CREATE PROCEDURE GetByFavorites(
		IN userID BIGINT
	)
BEGIN
	Select DISTINCT 
		m.TITLE 
        ,m.`YEAR`
        ,d.DIR_NAME 
        ,m.CRITIC_RATE
        ,m.MOV_ID
        ,m.AUD_RATE
        ,m.DIR_ID
        ,m.AUD_COUNT
    FROM 
		USERS u
        ,MOVIES m
        ,ACTORS a
        ,DIRECTORS d
        ,MOVIE_GENRES mg
        ,MOVIE_ACTORS ma
    Where
		u.USER_ID = userID
	AND
		a.ACT_ID = u.FAV_ACT
	AND
		a.ACT_ID = ma.ACT_ID 
	AND
		ma.MOV_ID = m.MOV_ID
	AND
		u.FAV_DIR = d.DIR_ID
    AND 
		m.DIR_ID = d.DIR_ID
	AND
		u.FAV_GENRE = mg.GENRE
	AND
		m.MOV_ID = mg.MOV_ID
	ORDER BY 
		m.Critic_rate DESC;
END //

DELIMITER ;



