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
     * Gets the latest location for each locomotive.
     */
    public List<LocomotiveLocation> getAllLocomotiveLocations() throws SQLException {
        List<LocomotiveLocation> locations = new ArrayList<>();

        String call = "{ ? = call LOCATION_API.f_getAllLocomotiveLocations() }";

        try (Connection conn = DatabaseConnectionPool.getConnection();
             CallableStatement stmt = conn.prepareCall(call)) {

            stmt.registerOutParameter(1, Types.REF_CURSOR);
            stmt.execute();

            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
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
        }

        return locations;
    }

    /**
     * Gets free locomotives at a specific endpoint, sorted by arrival time.
     */
    public List<LocomotiveLocation> getFreeLocomotivesAt(int endpointId) throws SQLException {
        List<LocomotiveLocation> locations = new ArrayList<>();

        String call = "{ ? = call LOCATION_API.f_getFreeLocomotivesAt(?) }";

        try (Connection conn = DatabaseConnectionPool.getConnection();
             CallableStatement stmt = conn.prepareCall(call)) {

            stmt.registerOutParameter(1, Types.REF_CURSOR);
            stmt.setInt(2, endpointId);
            stmt.execute();

            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                while (rs.next()) {
                    int locoId = rs.getInt("locomotive");

                    Endpoint endpoint = new Endpoint(
                            rs.getInt("endpoint"),
                            rs.getString("endpoint_name"),
                            rs.getInt("metafacility")
                    );

                    LocomotiveLocation loc = new LocomotiveLocation(
                            locoId,
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

        String call = "{ call LOCATION_API.p_insertLocomotiveLocation(?, ?, ?, ?, ?) }";

        String eventType = (status == LocomotiveLocation.Status.T) ? "D" : "A"; // D=Departure, A=Arrival

        try (Connection conn = DatabaseConnectionPool.getConnection();
             CallableStatement stmt = conn.prepareCall(call)) {

            stmt.setInt(1, locomotiveId);
            stmt.setInt(2, endpointId);
            stmt.setString(3, status.name());
            stmt.setTimestamp(4, Timestamp.valueOf(time));
            stmt.setString(5, eventType);
            stmt.execute();
        }
    }


    // ==================== WAGON LOCATIONS ====================

    /**
     * Gets the latest location for each wagon.
     */
    public List<WagonLocation> getAllWagonLocations() throws SQLException {
        List<WagonLocation> locations = new ArrayList<>();

        String call = "{ ? = call LOCATION_API.f_getAllWagonLocations() }";

        try (Connection conn = DatabaseConnectionPool.getConnection();
             CallableStatement stmt = conn.prepareCall(call)) {

            stmt.registerOutParameter(1, Types.REF_CURSOR);
            stmt.execute();

            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
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
        }

        return locations;
    }

    /**
     * Gets free wagons at a specific endpoint, sorted by arrival time.
     */
    public List<WagonLocation> getFreeWagonsAt(int endpointId) throws SQLException {
        List<WagonLocation> locations = new ArrayList<>();

        String call = "{ ? = call LOCATION_API.f_getFreeWagonsAt(?) }";

        try (Connection conn = DatabaseConnectionPool.getConnection();
             CallableStatement stmt = conn.prepareCall(call)) {

            stmt.registerOutParameter(1, Types.REF_CURSOR);
            stmt.setInt(2, endpointId);
            stmt.execute();

            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                while (rs.next()) {
                    int wagonId = rs.getInt("wagon");

                    Endpoint endpoint = new Endpoint(
                            rs.getInt("endpoint"),
                            rs.getString("endpoint_name"),
                            rs.getInt("metafacility")
                    );

                    WagonLocation loc = new WagonLocation(
                            wagonId,
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

        String call = "{ call LOCATION_API.p_insertWagonLocation(?, ?, ?, ?, ?) }";

        String eventType = (status == WagonLocation.Status.T) ? "D" : "A";

        try (Connection conn = DatabaseConnectionPool.getConnection();
             CallableStatement stmt = conn.prepareCall(call)) {

            stmt.setInt(1, wagonId);
            stmt.setInt(2, endpointId);
            stmt.setString(3, status.name());
            stmt.setTimestamp(4, Timestamp.valueOf(time));
            stmt.setString(5, eventType);
            stmt.execute();
        }
    }

}
