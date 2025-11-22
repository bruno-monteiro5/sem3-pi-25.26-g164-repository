CREATE OR REPLACE FUNCTION f_usbd29_electric_percentage (
    p_short_name IN Operator.short_name%TYPE
) RETURN SYS_REFCURSOR
AS
    percList SYS_REFCURSOR;
BEGIN
OPEN percList FOR
    SELECT
        Operator.short_name AS operator,
        COUNT(Locomotive.id) AS total_locomotives,
        COUNT(Electric_Locomotive.locomotive) AS electric_locomotives,
        ROUND(COUNT(Electric_Locomotive.locomotive)/ COUNT(Locomotive.id) * 100,3) AS electric_percentage
    FROM Operator
        JOIN Locomotive ON Locomotive.operator = Operator.vat
        LEFT JOIN Electric_Locomotive ON Electric_Locomotive.locomotive = Locomotive.id
    WHERE Operator.short_name = p_short_name
    GROUP BY Operator.short_name;

RETURN percList;
END;
/
