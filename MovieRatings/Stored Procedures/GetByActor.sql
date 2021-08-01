DELIMITER //

DROP PROCEDURE IF EXISTS GetByActor;
CREATE PROCEDURE GetByActor(
		IN actorName VARCHAR(255)
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
    FROM Actors a, Movies m, Movie_Actors ma, Directors d
    Where
		a.Act_name LIKE concat('%',actorName,'%')
        AND
		a.Act_id = ma.Act_id
        AND
        ma.Mov_id = m.Mov_id
        AND
        m.Dir_id = d.Dir_id
	ORDER BY 
		m.Critic_rate DESC;
END //

DELIMITER ;