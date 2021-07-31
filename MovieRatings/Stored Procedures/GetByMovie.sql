DELIMITER //

DROP PROCEDURE IF EXISTS GetByMovie;
CREATE PROCEDURE GetByMovie(
		IN movieName VARCHAR(255)
	)
BEGIN
    SET movieName = CONCAT('%',LOWER(movieName),'%');
	Select DISTINCT 
		m.TITLE 
        ,m.`YEAR`
        ,d.DIR_NAME 
        ,m.CRITIC_RATE
        ,m.MOV_ID
        ,m.AUD_RATE
        ,m.DIR_ID
        ,m.AUD_COUNT
    FROM Movies m, Directors d
    Where
		m.Title LIKE movieName
        AND
        m.Dir_id = d.Dir_id
	ORDER BY 
		m.Critic_rate DESC;
END //

DELIMITER ;

