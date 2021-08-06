DELIMITER //

DROP PROCEDURE IF EXISTS GetByGenre;
CREATE PROCEDURE GetByGenre(
		IN favortiteGenre VARCHAR(255)
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
		MOVIES m
        ,MOVIE_GENRES mg 
        ,DIRECTORS d
    Where
		mg.GENRE LIKE CONCAT('%',LOWER(favortiteGenre),'%')
        AND
        mg.MOV_ID = m.MOV_ID
        AND
        m.DIR_ID = d.DIR_ID
	ORDER BY 
		m.CRITIC_RATE DESC;
END //

DELIMITER ;

