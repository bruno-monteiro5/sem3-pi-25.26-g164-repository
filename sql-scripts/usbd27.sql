WITH

    freights_in_period AS (           --Freights num período específico
        SELECT *
            FROM Freight
            WHERE "date" BETWEEN DATE '2025-10-03' AND DATE '2025-10-06'
    ),

    wagons_grain_wagons AS (          --quais wagons sao grain wagons
        SELECT wagon.id
            FROM Wagon
            JOIN Wagon_Model  ON wagon_Model.id = wagon.wagon_model
            WHERE LOWER(wagon_Model.type) LIKE '%cereal%'
    ),

                                       --USO DISTINCT porque um train pode ter varios freights grain
    trains_with_grain_wagons AS (      --quais trains usaram pelo menos um grain wagon naquele periodo
        SELECT DISTINCT freights_in_period.train
        FROM freights_in_period
            JOIN Wagon_Freight ON Wagon_Freight.freight = freights_in_period.id
            JOIN wagons_grain_wagons ON wagons_grain_wagons.id = Wagon_Freight.wagon
    )

SELECT wagons_grain_wagons.id AS grain_wagon        -- Grain wagons usados em todos os trains com grain wagons
FROM wagons_grain_wagons
    JOIN Wagon_Freight ON Wagon_Freight.wagon = wagons_grain_wagons.id
    JOIN freights_in_period ON freights_in_period.id = Wagon_Freight.freight
GROUP BY wagons_grain_wagons.id
HAVING COUNT(DISTINCT freights_in_period.train) = (
    SELECT COUNT(DISTINCT trains_with_grain_wagons.train)
    FROM trains_with_grain_wagons
)