package repository;

import domain.railway.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RailwayRepository {

    /**
     * Obtém todas as linhas com conexões diretas
     */
    public List<Line> getAllLines() throws SQLException {
        List<Line> lines = new ArrayList<>();
        String query = """
            SELECT l.id, l.name, l.owner, l.endpoint_a, l.endpoint_b, l.gauge,
                   ea.name as endpoint_a_name, eb.name as endpoint_b_name
            FROM Line l
            JOIN Endpoint ea ON l.endpoint_a = ea.id
            JOIN Endpoint eb ON l.endpoint_b = eb.id
            ORDER BY l.name
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lines.add(new Line(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("owner"),
                        rs.getInt("endpoint_a"),
                        rs.getInt("endpoint_b"),
                        rs.getInt("gauge"),
                        rs.getString("endpoint_a_name"),
                        rs.getString("endpoint_b_name")
                ));
            }
        }
        return lines;
    }

    /**
     * Obtém todos os segmentos de uma linha específica
     */
    public List<LineSegment> getLineSegments(int lineId) throws SQLException {
        List<LineSegment> segments = new ArrayList<>();
        String query = """
            SELECT ls.id, ls.is_electrified, ls.speed_limit, ls.max_weight, 
                   ls.length, ls.line
            FROM Line_Segment ls
            WHERE ls.line = ?
            ORDER BY ls.id
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, lineId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    segments.add(new LineSegment(
                            rs.getInt("id"),
                            "Y".equals(rs.getString("is_electrified")),
                            rs.getInt("speed_limit"),
                            rs.getInt("max_weight"),
                            rs.getInt("length"),
                            rs.getInt("line")
                    ));
                }
            }
        }
        return segments;
    }

    /**
     * Obtém todas as locomotivas
     */
    public List<Locomotive> getAllLocomotives() throws SQLException {
        List<Locomotive> locomotives = new ArrayList<>();
        String query = """
            SELECT l.id, l.name, l.make, l.model, l.max_speed, l.op_speed, 
                   l.total_weight, lg.gauge,
                   CASE WHEN el.locomotive IS NOT NULL THEN 'ELECTRIC'
                        WHEN dl.locomotive IS NOT NULL THEN 'DIESEL'
                        ELSE 'UNKNOWN' END as traction_type
            FROM Locomotive l
            LEFT JOIN Electric_Locomotive el ON l.id = el.locomotive
            LEFT JOIN Diesel_Locomotive dl ON l.id = dl.locomotive
            JOIN Locomotive_Gauge lg ON l.id = lg.locomotive
            ORDER BY l.name
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                locomotives.add(new Locomotive(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("make"),
                        rs.getString("model"),
                        rs.getInt("max_speed"),
                        rs.getInt("op_speed"),
                        rs.getString("traction_type"),
                        rs.getInt("total_weight"),
                        rs.getInt("gauge")
                ));
            }
        }
        return locomotives;
    }
}