package repository;

import domain.railway.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository for accessing and updating locomotive/wagon locations.
 *
 */
public class LocationRepository {

    private final RailwayRepository railwayRepo;

    public LocationRepository(RailwayRepository railwayRepo) {
        this.railwayRepo = railwayRepo;
    }

    // ==================== LOCOMOTIVE LOCATIONS ====================

    /**
     * Gets all locomotive locations.
     */
    public List<LocomotiveLocation> getAllLocomotiveLocations() throws SQLException {
        List<LocomotiveLocation> locations = new ArrayList<>();

        String query = """
        SELECT locomotive, endpoint, status, arrival, endpoint_name, metafacility
        FROM (
            SELECT ll.locomotive,
                   ll.endpoint,
                   ll.status,
                   ll.event_time AS arrival,
                   e.name AS endpoint_name,
                   e.metafacility,
                   ROW_NUMBER() OVER (
                       PARTITION BY ll.locomotive
                       ORDER BY ll.event_time DESC, ll.id DESC
                   ) rn
            FROM Locomotive_Location ll
            JOIN Endpoint e ON ll.endpoint = e.id
        )
        WHERE rn = 1
        ORDER BY locomotive
    """;

        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int locoId = rs.getInt("locomotive");

                Endpoint endpoint = new Endpoint(
                        rs.getInt("endpoint"),
                        rs.getString("endpoint_name"),
                        rs.getInt("metafacility")
                );

                LocomotiveLocation.Status status = LocomotiveLocation.Status.valueOf(rs.getString("status"));
                LocalDateTime arrival = rs.getTimestamp("arrival").toLocalDateTime();

                locations.add(new LocomotiveLocation(locoId, endpoint, status, arrival));
            }
        }

        return locations;
    }


    /**
     * Gets free locomotives at a specific endpoint, sorted by arrival time.
     */
    public List<LocomotiveLocation> getFreeLocomotivesAt(int endpointId) throws SQLException {
        List<LocomotiveLocation> locations = new ArrayList<>();

        String query = """
            SELECT lcl.locomotive, lcl.endpoint, lcl.status, lcl.arrival,
                   e.name as endpoint_name, e.metafacility
            FROM Locomotive_Current_Location lcl
            JOIN Endpoint e ON lcl.endpoint = e.id
            WHERE lcl.endpoint = ? AND lcl.status = 'F'
            ORDER BY lcl.arrival
        """;

        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, endpointId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Endpoint endpoint = new Endpoint(
                            rs.getInt("endpoint"),
                            rs.getString("endpoint_name"),
                            rs.getInt("metafacility")
                    );

                    LocomotiveLocation loc = new LocomotiveLocation(
                            rs.getInt("locomotive"),
                            endpoint,
                            LocomotiveLocation.Status.F,
                            rs.getTimestamp("arrival").toLocalDateTime()
                    );

                    locations.add(loc);
                }
            }
        }

        return locations;
    }

    public void updateLocomotiveLocation(int locomotiveId, int endpointId,
                                         LocomotiveLocation.Status status,
                                         LocalDateTime time) throws SQLException {

        String nextIdSql = "SELECT NVL(MAX(id), 0) + 1 AS next_id FROM Locomotive_Location";
        String insertSql = """
        INSERT INTO Locomotive_Location (id, locomotive, endpoint, status, event_time, event_type)
        VALUES (?, ?, ?, ?, ?, ?)
    """;

        String eventType = (status == LocomotiveLocation.Status.T) ? "D" : "A"; // D=Departure, A=Arrival

        try (Connection conn = DatabaseConnectionPool.getConnection()) {
            int nextId;

            try (PreparedStatement ps = conn.prepareStatement(nextIdSql);
                 ResultSet rs = ps.executeQuery()) {
                rs.next();
                nextId = rs.getInt("next_id");
            }

            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setInt(1, nextId);
                stmt.setInt(2, locomotiveId);
                stmt.setInt(3, endpointId);
                stmt.setString(4, status.name());
                stmt.setTimestamp(5, Timestamp.valueOf(time));
                stmt.setString(6, eventType);
                stmt.executeUpdate();
            }
        }
    }


    // ==================== WAGON LOCATIONS ====================

    /**
     * Gets all wagon locations.
     */
    public List<WagonLocation> getAllWagonLocations() throws SQLException {
        List<WagonLocation> locations = new ArrayList<>();

        String query = """
        SELECT wagon, endpoint, status, arrival, endpoint_name, metafacility
        FROM (
            SELECT wcl.wagon,
                   wcl.endpoint,
                   wcl.status,
                   wcl.event_timestamp AS arrival,
                   e.name AS endpoint_name,
                   e.metafacility,
                   ROW_NUMBER() OVER (
                       PARTITION BY wcl.wagon
                       ORDER BY wcl.event_timestamp DESC, wcl.id DESC
                   ) rn
            FROM Wagon_Location wcl
            JOIN Endpoint e ON wcl.endpoint = e.id
        )
        WHERE rn = 1
        ORDER BY wagon
    """;

        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int wagonId = rs.getInt("wagon");

                Endpoint endpoint = new Endpoint(
                        rs.getInt("endpoint"),
                        rs.getString("endpoint_name"),
                        rs.getInt("metafacility")
                );

                WagonLocation.Status status = WagonLocation.Status.valueOf(rs.getString("status"));
                LocalDateTime arrival = rs.getTimestamp("arrival").toLocalDateTime();

                locations.add(new WagonLocation(wagonId, endpoint, status, arrival));
            }
        }

        return locations;
    }


    /**
     * Gets free wagons at a specific endpoint.
     */
    public List<WagonLocation> getFreeWagonsAt(int endpointId) throws SQLException {
        List<WagonLocation> locations = new ArrayList<>();

        String query = """
            SELECT wcl.wagon, wcl.endpoint, wcl.status, wcl.arrival,
                   e.name as endpoint_name, e.metafacility
            FROM Wagon_Location wcl
            JOIN Endpoint e ON wcl.endpoint = e.id
            WHERE wcl.endpoint = ? AND wcl.status = 'F'
            ORDER BY wcl.arrival
        """;

        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, endpointId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Endpoint endpoint = new Endpoint(
                            rs.getInt("endpoint"),
                            rs.getString("endpoint_name"),
                            rs.getInt("metafacility")
                    );

                    WagonLocation loc = new WagonLocation(
                            rs.getInt("wagon"),
                            endpoint,
                            WagonLocation.Status.F,
                            rs.getTimestamp("arrival").toLocalDateTime()
                    );

                    locations.add(loc);
                }
            }
        }

        return locations;
    }

    /**
     * Updates wagon location.
     */
    public void updateWagonLocation(int wagonId, int endpointId,
                                    WagonLocation.Status status,
                                    LocalDateTime time) throws SQLException {

        String nextIdSql = "SELECT NVL(MAX(id), 0) + 1 AS next_id FROM Wagon_Location";
        String insertSql = """
        INSERT INTO Wagon_Location (id, wagon, endpoint, status, event_timestamp, event_type)
        VALUES (?, ?, ?, ?, ?, ?)
    """;

        String eventType = (status == WagonLocation.Status.T) ? "D" : "A";

        try (Connection conn = DatabaseConnectionPool.getConnection()) {
            int nextId;

            try (PreparedStatement ps = conn.prepareStatement(nextIdSql);
                 ResultSet rs = ps.executeQuery()) {
                rs.next();
                nextId = rs.getInt("next_id");
            }

            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setInt(1, nextId);
                stmt.setInt(2, wagonId);
                stmt.setInt(3, endpointId);
                stmt.setString(4, status.name());
                stmt.setTimestamp(5, Timestamp.valueOf(time));
                stmt.setString(6, eventType);
                stmt.executeUpdate();
            }
        }
    }

}