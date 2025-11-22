CREATE OR REPLACE FUNCTION f_usbd27_grain_wagons (
    p_start_date IN DATE,
    p_end_date   IN DATE
) RETURN SYS_REFCURSOR
AS
    wagonsList SYS_REFCURSOR;
BEGIN
OPEN wagonsList FOR
        WITH

            freights_in_period AS (
                SELECT *
                FROM Freight
                WHERE "date" BETWEEN p_start_date AND p_end_date
            ),

            wagons_grain_wagons AS (
                SELECT Wagon.id
                FROM Wagon
                     JOIN Wagon_Model ON Wagon_Model.id = Wagon.wagon_model
                WHERE LOWER(Wagon_Model.type) LIKE '%cereal%'
            ),

            trains_with_grain_wagons AS (
                SELECT DISTINCT freights_in_period.train
                FROM freights_in_period
                     JOIN Wagon_Freight ON Wagon_Freight.freight = freights_in_period.id
                     JOIN wagons_grain_wagons ON wagons_grain_wagons.id = Wagon_Freight.wagon
            )

SELECT wagons_grain_wagons.id AS grain_wagon
FROM wagons_grain_wagons
         JOIN Wagon_Freight ON Wagon_Freight.wagon = wagons_grain_wagons.id
         JOIN freights_in_period ON freights_in_period.id = Wagon_Freight.freight
GROUP BY wagons_grain_wagons.id
HAVING COUNT(DISTINCT freights_in_period.train) = (
    SELECT COUNT(DISTINCT trains_with_grain_wagons.train)
    FROM trains_with_grain_wagons
);

RETURN wagonsList;
END;
/
