DELIMITER //

DROP PROCEDURE IF EXISTS GetByDirector;
CREATE PROCEDURE GetByDirector(
		IN directorName VARCHAR(255)
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
    FROM Movies m, Directors d
    Where
		d.DIR_NAME like CONCAT('%',directorName,'%')
        AND
		d.DIR_ID = m.DIR_ID
	ORDER BY
        m.CRITIC_RATE DESC;
END //

DELIMITER ;