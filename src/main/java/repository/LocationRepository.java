package repository;

import domain.railway.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository for accessing and updating locomotive/wagon locations.
 * Works with Locomotive_Current_Location and Wagon_Current_Location tables.
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
            SELECT lcl.locomotive, lcl.endpoint, lcl.status, lcl.arrival,
                   e.name as endpoint_name, e.metafacility
            FROM Locomotive_Current_Location lcl
            JOIN Endpoint e ON lcl.endpoint = e.id
            ORDER BY lcl.locomotive
        """;

        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Endpoint endpoint = new Endpoint(
                        rs.getInt("endpoint"),
                        rs.getString("endpoint_name"),
                        rs.getInt("metafacility")
                );

                LocomotiveLocation loc = new LocomotiveLocation(
                        rs.getInt("locomotive"),
                        endpoint,
                        LocomotiveLocation.Status.valueOf(rs.getString("status")),
                        rs.getTimestamp("arrival").toLocalDateTime()
                );

                locations.add(loc);
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

    /**
     * Updates locomotive location after assignment to train.
     */
    public void updateLocomotiveLocation(int locomotiveId, int endpointId,
                                         LocomotiveLocation.Status status,
                                         LocalDateTime arrival) throws SQLException {
        String query = """
            UPDATE Locomotive_Current_Location
            SET endpoint = ?, status = ?, arrival = ?
            WHERE locomotive = ?
        """;

        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, endpointId);
            stmt.setString(2, status.name());
            stmt.setTimestamp(3, Timestamp.valueOf(arrival));
            stmt.setInt(4, locomotiveId);

            stmt.executeUpdate();
        }
    }

    // ==================== WAGON LOCATIONS ====================

    /**
     * Gets all wagon locations.
     */
    public List<WagonLocation> getAllWagonLocations() throws SQLException {
        List<WagonLocation> locations = new ArrayList<>();

        String query = """
            SELECT wcl.wagon, wcl.endpoint, wcl.status, wcl.arrival,
                   e.name as endpoint_name, e.metafacility
            FROM Wagon_Current_Location wcl
            JOIN Endpoint e ON wcl.endpoint = e.id
            ORDER BY wcl.wagon
        """;

        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Endpoint endpoint = new Endpoint(
                        rs.getInt("endpoint"),
                        rs.getString("endpoint_name"),
                        rs.getInt("metafacility")
                );

                WagonLocation loc = new WagonLocation(
                        rs.getInt("wagon"),
                        endpoint,
                        WagonLocation.Status.valueOf(rs.getString("status")),
                        rs.getTimestamp("arrival").toLocalDateTime()
                );

                locations.add(loc);
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
            FROM Wagon_Current_Location wcl
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
                                    LocalDateTime arrival) throws SQLException {
        String query = """
            UPDATE Wagon_Current_Location
            SET endpoint = ?, status = ?, arrival = ?
            WHERE wagon = ?
        """;

        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, endpointId);
            stmt.setString(2, status.name());
            stmt.setTimestamp(3, Timestamp.valueOf(arrival));
            stmt.setInt(4, wagonId);

            stmt.executeUpdate();
        }
    }
}