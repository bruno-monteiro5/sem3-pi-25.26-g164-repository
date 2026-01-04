package repository;

import domain.Location;
import domain.railway.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository for Railway entities.
 * Updated for USLP09/10 with new Locomotive_Model structure.
 *
 * @author G164
 * @version 2.0
 */
public class RailwayRepository {

    // ==================== ENDPOINTS ====================

    public List<Endpoint> getAllEndpoints() throws SQLException {
        List<Endpoint> endpoints = new ArrayList<>();
        String call = "{ ? = call RAILWAY_API.f_getAllEndpoints() }";

        try (Connection conn = DatabaseConnectionPool.getConnection();
             CallableStatement stmt = conn.prepareCall(call)) {

            stmt.registerOutParameter(1, Types.REF_CURSOR);
            stmt.execute();

            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                while (rs.next()) {
                    endpoints.add(new Endpoint(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getInt("metafacility")
                    ));
                }
            }
        }
        return endpoints;
    }

    public Endpoint getEndpointById(int id) throws SQLException {
        String call = "{ ? = call RAILWAY_API.f_getEndpointById(?) }";

        try (Connection conn = DatabaseConnectionPool.getConnection();
             CallableStatement stmt = conn.prepareCall(call)) {

            stmt.registerOutParameter(1, Types.REF_CURSOR);
            stmt.setInt(2, id);
            stmt.execute();

            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                if (rs.next()) {
                    return new Endpoint(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getInt("metafacility")
                    );
                }
            }
        }
        return null;
    }

    // ==================== LINES ====================

    public List<Line> getAllLines() throws SQLException {
        List<Line> lines = new ArrayList<>();
        String call = "{ ? = call RAILWAY_API.f_getAllLines() }";

        try (Connection conn = DatabaseConnectionPool.getConnection();

             CallableStatement stmt = conn.prepareCall(call)) {

            stmt.registerOutParameter(1, Types.REF_CURSOR);
            stmt.execute();

            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                while (rs.next()) {
                    lines.add(new Line(
                                rs.getInt("ID"),
                                rs.getString("NAME"),
                                rs.getString("OWNER"),
                                rs.getInt("ENDPOINT_A"),
                                rs.getInt("ENDPOINT_B"),
                                rs.getInt("GAUGE"),
                                rs.getString("ENDPOINT_A_NAME"),
                                rs.getString("ENDPOINT_B_NAME")
                        ));
                }
            }
        }
        return lines;
    }

    // ==================== LINE SEGMENTS ====================

    public List<LineSegment> getLineSegments(int lineId) throws SQLException {
        List<LineSegment> segments = new ArrayList<>();
        String call = "{ ? = call RAILWAY_API.f_getLineSegments(?) }";

        try (Connection conn = DatabaseConnectionPool.getConnection();
             CallableStatement stmt = conn.prepareCall(call)) {

            stmt.registerOutParameter(1, Types.REF_CURSOR);
            stmt.setInt(2, lineId);
            stmt.execute();

            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
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

    public TrackType getTrackType(int segmentId) throws SQLException {
        String callMultiple = "{ ? = call RAILWAY_API.f_getMultipleTracks(?) }";

        try (Connection conn = DatabaseConnectionPool.getConnection();
             CallableStatement stmt = conn.prepareCall(callMultiple)) {

            stmt.registerOutParameter(1, Types.REF_CURSOR);
            stmt.setInt(2, segmentId);
            stmt.execute();

            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                if (rs.next()) {
                    return new MultipleTrack(segmentId, rs.getInt("number_tracks"));
                }
            }
        }

        String callSingle = "{ ? = call RAILWAY_API.f_getSingleTrack(?) }";
        try (Connection conn = DatabaseConnectionPool.getConnection();
             CallableStatement stmt = conn.prepareCall(callSingle)) {

            stmt.registerOutParameter(1, Types.REF_CURSOR);
            stmt.setInt(2, segmentId);
            stmt.execute();

            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                if (rs.next()) {
                    return getSidingInfo(segmentId);
                }
            }
        }

        return null;
    }

    private SingleTrack getSidingInfo(int segmentId) throws SQLException {
        String call = "{ ? = call RAILWAY_API.f_getSidingInfo(?) }";

        try (Connection conn = DatabaseConnectionPool.getConnection();
             CallableStatement stmt = conn.prepareCall(call)) {

            stmt.registerOutParameter(1, Types.REF_CURSOR);
            stmt.setInt(2, segmentId);
            stmt.execute();

            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                if (rs.next()) {
                    return new SingleTrack(
                            segmentId,
                            rs.getInt("position"),
                            rs.getInt("length")
                    );
                }
            }
        }

        return new SingleTrack(segmentId);
    }

    // ==================== LOCOMOTIVES ====================

    /**
     * Gets all locomotives with their model information.
     * FIXED: Now uses Locomotive_Model table structure.
     */
    public List<Locomotive> getAllLocomotives() throws SQLException {
        List<Locomotive> locomotives = new ArrayList<>();

        String call = "{ ? = call RAILWAY_API.f_getAllLocomotives() }";

        try (Connection conn = DatabaseConnectionPool.getConnection();
             CallableStatement stmt = conn.prepareCall(call)) {

            stmt.registerOutParameter(1, Types.REF_CURSOR);
            stmt.execute();

            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                while (rs.next()) {
                    locomotives.add(new Locomotive(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("make"),
                            rs.getString("model_name"),
                            rs.getInt("power"),
                            rs.getInt("max_speed"),
                            rs.getInt("op_speed"),
                            rs.getString("traction_type"),
                            rs.getInt("total_weight"),
                            rs.getInt("gaugeid"),
                            rs.getInt("length")
                    ));
                }
            }
        }
        return locomotives;
    }

    /**
     * Gets a single locomotive by ID.
     */
    public Locomotive getLocomotiveById(int locomotiveId) throws SQLException {
        String call = "{ ? = call RAILWAY_API.f_getLocomotiveById(?) }";

        try (Connection conn = DatabaseConnectionPool.getConnection();
             CallableStatement stmt = conn.prepareCall(call)) {

            stmt.registerOutParameter(1, Types.REF_CURSOR);
            stmt.setInt(2, locomotiveId);
            stmt.execute();

            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                if (rs.next()) {
                    return new Locomotive(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("make"),
                            rs.getString("model_name"),
                            rs.getInt("power"),
                            rs.getInt("max_speed"),
                            rs.getInt("op_speed"),
                            rs.getString("traction_type"),
                            rs.getInt("total_weight"),
                            rs.getInt("gaugeid"),
                            rs.getInt("length")
                    );
                }
            }
        }
        return null;
    }

    // ==================== PATHS ====================

    public List<Path> getAllPaths() throws SQLException {
        List<Path> paths = new ArrayList<>();
        String call = "{ ? = call RAILWAY_API.f_getAllPaths() }";

        try (Connection conn = DatabaseConnectionPool.getConnection();
             CallableStatement stmt = conn.prepareCall(call)) {

            stmt.registerOutParameter(1, Types.REF_CURSOR);
            stmt.execute();

            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                while (rs.next()) {
                    Path path = new Path(
                            rs.getInt("id"),
                            rs.getString("name")
                    );
                    paths.add(path);
                }
            }
        }

        for (Path path : paths) {
            loadPathEndpoints(path);
        }

        return paths;
    }

    public Path getPathById(int pathId) throws SQLException {
        String call = "{ ? = call RAILWAY_API.f_getPathById(?) }";

        try (Connection conn = DatabaseConnectionPool.getConnection();
             CallableStatement stmt = conn.prepareCall(call)) {

            stmt.registerOutParameter(1, Types.REF_CURSOR);
            stmt.setInt(2, pathId);
            stmt.execute();

            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                if (rs.next()) {
                    Path path = new Path(
                            rs.getInt("id"),
                            rs.getString("name")
                    );
                    loadPathEndpoints(path);
                    return path;
                }
            }
        }
        return null;
    }

    private void loadPathEndpoints(Path path) throws SQLException {
        String call = "{ ? = call RAILWAY_API.f_loadPathEndpoints(?) }";

        try (Connection conn = DatabaseConnectionPool.getConnection();
             CallableStatement stmt = conn.prepareCall(call)) {

            stmt.registerOutParameter(1, Types.REF_CURSOR);
            stmt.setInt(2, path.getId());
            stmt.execute();

            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                while (rs.next()) {
                    Endpoint endpoint = new Endpoint(
                            rs.getInt("endpoint"),
                            rs.getString("name"),
                            rs.getInt("metafacility")
                    );

                    Path.PathEndpoint pathEndpoint = new Path.PathEndpoint(
                            rs.getInt("seq"),
                            endpoint,
                            "Y".equals(rs.getString("must_stop"))
                    );

                    path.addEndpoint(pathEndpoint);
                }
            }
        }
    }

    // ==================== TRAINS ====================

    private record TrainDto(int id, String operator, LocalTime time, LocalDate date,
                            int pathId, int maxLength) {}

    public List<Train> getAllTrains() throws SQLException {
        List<TrainDto> dtos = new ArrayList<>();
        String call = "{ ? = call RAILWAY_API.f_getAllTrains() }";

        try (Connection conn = DatabaseConnectionPool.getConnection();
             CallableStatement stmt = conn.prepareCall(call)) {

            stmt.registerOutParameter(1, Types.REF_CURSOR);
            stmt.execute();

            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                while (rs.next()) {
                    dtos.add(mapToTrainDto(rs));
                }
            }
        }

        List<Train> trains = new ArrayList<>();
        for (TrainDto dto : dtos) {
            Train train = hydrateTrain(dto);
            trains.add(train);
        }

        return trains;
    }

    public Train getTrainById(int trainId) throws SQLException {
        String call = "{ ? = call RAILWAY_API.f_getTrainById(?) }";

        TrainDto dto = null;

        try (Connection conn = DatabaseConnectionPool.getConnection();
             CallableStatement stmt = conn.prepareCall(call)) {

            stmt.registerOutParameter(1, Types.REF_CURSOR);
            stmt.setInt(2, trainId);
            stmt.execute();

            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                if (rs.next()) {
                    dto = mapToTrainDto(rs);
                }
            }
        }

        if (dto != null) {
            Train train = hydrateTrain(dto);
            loadTrainLocomotives(train);
            loadTrainFreights(train);
            return train;
        }
        return null;
    }

    private TrainDto mapToTrainDto(ResultSet rs) throws SQLException {
        return new TrainDto(
                rs.getInt(1),                     // id
                rs.getString(2),                  // operator
                rs.getTimestamp(3).toLocalDateTime().toLocalTime(),  // time
                rs.getDate(4).toLocalDate(),      // date
                rs.getInt(5),                     // path
                rs.getInt(6)                      // max_length
        );
    }

    private Train hydrateTrain(TrainDto dto) throws SQLException {
        Path path = getPathById(dto.pathId());

        // Start and End come from the Path!
        Endpoint start = null;
        Endpoint end = null;

        if (path != null && !path.getOrderedEndpoints().isEmpty()) {
            List<Path.PathEndpoint> ordered = path.getOrderedEndpoints();
            start = ordered.get(0).getEndpoint();
            end = ordered.get(ordered.size() - 1).getEndpoint();
        }

        return new Train(dto.id(), dto.operator(), dto.time(), dto.date(), path, start, end);
    }

    private void loadTrainLocomotives(Train train) throws SQLException {
        String call = "{ ? = call RAILWAY_API.f_loadTrainLocomotives(?) }";

        try (Connection conn = DatabaseConnectionPool.getConnection();
             CallableStatement stmt = conn.prepareCall(call)) {

            stmt.registerOutParameter(1, Types.REF_CURSOR);
            stmt.setInt(2, train.getId());
            stmt.execute();

            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                while (rs.next()) {
                    Locomotive loco = new Locomotive(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("make"),
                            rs.getString("model_name"),
                            rs.getInt("power"),
                            rs.getInt("max_speed"),
                            rs.getInt("op_speed"),
                            rs.getString("traction_type"),
                            rs.getInt("total_weight"),
                            rs.getInt("gaugeid"),
                            rs.getInt("length")
                    );
                    train.addLocomotive(loco);
                }
            }
        }
    }

    private void loadTrainFreights(Train train) throws SQLException {
        record FreightDto(int id, LocalDate date, int originId, int destinationId) {}
        List<FreightDto> dtos = new ArrayList<>();

        String call = "{ ? = call RAILWAY_API.f_loadTrainFreights(?) }";

        try (Connection conn = DatabaseConnectionPool.getConnection();
             CallableStatement stmt = conn.prepareCall(call)) {

            stmt.registerOutParameter(1, Types.REF_CURSOR);
            stmt.setInt(2, train.getId());
            stmt.execute();

            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                while (rs.next()) {
                    dtos.add(new FreightDto(
                            rs.getInt("id"),
                            rs.getDate("date").toLocalDate(),
                            rs.getInt("origin"),
                            rs.getInt("destination")
                    ));
                }
            }
        }

        for (FreightDto dto : dtos) {
            Freight freight = new Freight(
                    dto.id(),
                    dto.date(),
                    getEndpointById(dto.originId()),
                    getEndpointById(dto.destinationId())
            );
            loadFreightWagons(freight);
            train.addFreight(freight);
        }
    }

    private void loadFreightWagons(Freight freight) throws SQLException {
        record WagonRawData(int id, int serviceYear, String operator,
                            int modelId, String model, String maker, int bogies, int length,
                            int width, int height, double weight, int maxSpeed,
                            double payload, double volume, String type) {}

        List<WagonRawData> rawDataList = new ArrayList<>();

        String call = "{ ? = call RAILWAY_API.f_loadFreightWagons(?) }";

        try (Connection conn = DatabaseConnectionPool.getConnection();
             CallableStatement stmt = conn.prepareCall(call)) {

            stmt.registerOutParameter(1, Types.REF_CURSOR);
            stmt.setInt(2, freight.getId());
            stmt.execute();

            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                while (rs.next()) {
                    rawDataList.add(new WagonRawData(
                            rs.getInt("id"),
                            rs.getDate("service_start").toLocalDate().getYear(),
                            rs.getString("operator"),
                            rs.getInt("model_id"),
                            rs.getString("model"),
                            rs.getString("maker"),
                            rs.getInt("number_bogies"),
                            rs.getInt("length"),
                            rs.getInt("width"),
                            rs.getInt("height"),
                            rs.getDouble("weight"),
                            rs.getInt("max_speed"),
                            rs.getDouble("payload"),
                            rs.getDouble("volume"),
                            rs.getString("type")
                    ));
                }
            }
        }

        for (WagonRawData data : rawDataList) {
            int gauge = getWagonGauge(data.modelId());

            WagonModel model = new WagonModel(
                    data.modelId(),
                    data.model(),
                    data.maker(),
                    data.bogies(),
                    data.length(),
                    data.width(),
                    data.height(),
                    data.weight(),
                    data.maxSpeed(),
                    data.payload(),
                    data.volume(),
                    data.type(),
                    gauge
            );

            Wagon wagon = new Wagon(
                    data.id(),
                    data.serviceYear(),
                    model,
                    data.operator()
            );
            freight.addWagon(wagon);
        }
    }

    private int getWagonGauge(int wagonModelId) throws SQLException {
        String call = "{ ? = call RAILWAY_API.f_getWagonGauge(?) }";

        try (Connection conn = DatabaseConnectionPool.getConnection();
             CallableStatement stmt = conn.prepareCall(call)) {

            stmt.registerOutParameter(1, Types.REF_CURSOR);
            stmt.setInt(2, wagonModelId);
            stmt.execute();

            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                if (rs.next()) {
                    return rs.getInt("Gaugeid");
                }
            }
        }
        return 1668;
    }

    // ==================== WAGONS (NEW FOR USLP09) ====================

    /**
     * Gets a wagon by ID with full details.
     */
    public Wagon getWagonById(int wagonId) throws SQLException {
        String call = "{ ? = call RAILWAY_API.f_getWagonById(?) }";

        try (Connection conn = DatabaseConnectionPool.getConnection();
             CallableStatement stmt = conn.prepareCall(call)) {

            stmt.registerOutParameter(1, Types.REF_CURSOR);
            stmt.setInt(2, wagonId);
            stmt.execute();

            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                if (rs.next()) {
                    int gauge = getWagonGauge(rs.getInt("wagon_model"));

                    WagonModel model = new WagonModel(
                            rs.getInt("wagon_model"),
                            rs.getString("model"),
                            rs.getString("maker"),
                            rs.getInt("number_bogies"),
                            rs.getInt("length"),
                            rs.getInt("width"),
                            rs.getInt("height"),
                            rs.getDouble("weight"),
                            rs.getInt("max_speed"),
                            rs.getDouble("payload"),
                            rs.getDouble("volume"),
                            rs.getString("type"),
                            gauge
                    );

                    return new Wagon(
                            rs.getInt("id"),
                            rs.getDate("service_start").toLocalDate().getYear(),
                            model,
                            rs.getString("operator")
                    );
                }
            }
        }
        return null;
    }

    // ==================== PATH MANAGEMENT ====================

    public Path createPath(String name) throws SQLException {
        int newId;

        String call = "{ ? = call RAILWAY_API.f_createPath(?) }";

        try (Connection conn = DatabaseConnectionPool.getConnection();
             CallableStatement stmt = conn.prepareCall(call)) {

            stmt.registerOutParameter(1, Types.INTEGER);
            stmt.setString(2, name);
            stmt.execute();

            newId = stmt.getInt(1);
        }

        return new Path(newId, name);
    }

    public void addEndpointToPath(int pathId, int seq, int endpointId, boolean mustStop) throws SQLException {
        String call = "{ call RAILWAY_API.p_addEndpointToPath(?, ?, ?, ?) }";

        try (Connection conn = DatabaseConnectionPool.getConnection();
             CallableStatement stmt = conn.prepareCall(call)) {

            stmt.setInt(1, pathId);
            stmt.setInt(2, seq);
            stmt.setInt(3, endpointId);
            stmt.setString(4, mustStop ? "Y" : "N");
            stmt.execute();
        }
    }

    public void updateTrainRoute(int trainId, int pathId) throws SQLException {
        String call = "{ call RAILWAY_API.p_updateTrainRoute(?, ?) }";

        try (Connection conn = DatabaseConnectionPool.getConnection();
             CallableStatement stmt = conn.prepareCall(call)) {

            stmt.setInt(1, pathId);
            stmt.setInt(2, trainId);
            stmt.execute();
        }
    }

    // ==================== FREIGHT MANAGEMENT (NEW FOR USLP08/09) ====================

    public List<Freight> getPendingFreights() throws SQLException {
        List<Freight> freights = new ArrayList<>();

        String call = "{ ? = call RAILWAY_API.f_getPendingFreights() }";

        try (Connection conn = DatabaseConnectionPool.getConnection();
             CallableStatement stmt = conn.prepareCall(call)) {

            stmt.registerOutParameter(1, Types.REF_CURSOR);
            stmt.execute();

            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                while (rs.next()) {
                    Freight freight = new Freight(
                            rs.getInt("id"),
                            rs.getDate("date").toLocalDate(),
                            getEndpointById(rs.getInt("origin")),
                            getEndpointById(rs.getInt("destination"))
                    );

                    loadFreightWagons(freight);
                    freights.add(freight);
                }
            }
        }

        return freights;
    }

    public Freight getFreightById(int freightId) throws SQLException {
        String call = "{ ? = call RAILWAY_API.f_getFreightById(?) }";

        try (Connection conn = DatabaseConnectionPool.getConnection();
             CallableStatement stmt = conn.prepareCall(call)) {

            stmt.registerOutParameter(1, Types.REF_CURSOR);
            stmt.setInt(2, freightId);
            stmt.execute();

            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                if (rs.next()) {
                    Freight freight = new Freight(
                            rs.getInt("id"),
                            rs.getDate("date").toLocalDate(),
                            getEndpointById(rs.getInt("origin")),
                            getEndpointById(rs.getInt("destination"))
                    );

                    loadFreightWagons(freight);
                    return freight;
                }
            }
        }

        return null;
    }

    public void assignFreightToTrain(int freightId, int trainId) throws SQLException {
        String call = "{ call RAILWAY_API.p_assignFreightToTrain(?, ?) }";

        try (Connection conn = DatabaseConnectionPool.getConnection();
             CallableStatement stmt = conn.prepareCall(call)) {

            stmt.setInt(1, freightId);
            stmt.setInt(2, trainId);
            stmt.execute();
        }
    }

    public void removeFreightFromTrain(int freightId, int trainId) throws SQLException {
        String call = "{ call RAILWAY_API.p_removeFreightFromTrain(?, ?) }";

        try (Connection conn = DatabaseConnectionPool.getConnection();
             CallableStatement stmt = conn.prepareCall(call)) {

            stmt.setInt(1, freightId);
            stmt.setInt(2, trainId);
            stmt.execute();
        }
    }

    public int createFreight(LocalDate date, int originEndpointId, int destinationEndpointId) throws SQLException {
        String call = "{ ? = call RAILWAY_API.f_createFreight(?, ?, ?) }";

        try (Connection conn = DatabaseConnectionPool.getConnection()) {
            conn.setAutoCommit(false);
            try (CallableStatement stmt = conn.prepareCall(call)) {

                stmt.registerOutParameter(1, Types.INTEGER);
                stmt.setDate(2, Date.valueOf(date));
                stmt.setInt(3, originEndpointId);
                stmt.setInt(4, destinationEndpointId);

                stmt.execute();
                int newId = stmt.getInt(1);

                conn.commit();
                return newId;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }



    // ==================== LOCOMOTIVE-TRAIN ASSIGNMENTS (NEW FOR USLP09) ====================

    public void assignLocomotiveToTrain(int locomotiveId, int trainId, int position) throws SQLException {
        String call = "{ call RAILWAY_API.p_assignLocomotiveToTrain(?, ?, ?) }";

        try (Connection conn = DatabaseConnectionPool.getConnection();
             CallableStatement stmt = conn.prepareCall(call)) {

            stmt.setInt(1, locomotiveId);
            stmt.setInt(2, trainId);
            stmt.setInt(3, position);
            stmt.execute();
        }
    }

    public void removeLocomotiveFromTrain(int locomotiveId, int trainId) throws SQLException {
        String call = "{ call RAILWAY_API.p_removeLocomotiveFromTrain(?, ?) }";

        try (Connection conn = DatabaseConnectionPool.getConnection();
             CallableStatement stmt = conn.prepareCall(call)) {

            stmt.setInt(1, locomotiveId);
            stmt.setInt(2, trainId);
            stmt.execute();
        }
    }

    public void removeAllLocomotivesFromTrain(int trainId) throws SQLException {
        String call = "{ call RAILWAY_API.p_removeAllLocomotivesFromTrain(?) }";

        try (Connection conn = DatabaseConnectionPool.getConnection();
             CallableStatement stmt = conn.prepareCall(call)) {

            stmt.setInt(1, trainId);
            stmt.execute();
        }
    }

    public int getLocomotiveCountForTrain(int trainId) throws SQLException {
        String call = "{ ? = call RAILWAY_API.f_getLocomotiveCountForTrain(?) }";

        try (Connection conn = DatabaseConnectionPool.getConnection();
             CallableStatement stmt = conn.prepareCall(call)) {

            stmt.registerOutParameter(1, Types.REF_CURSOR);
            stmt.setInt(2, trainId);
            stmt.execute();

            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                if (rs.next()) {
                    return rs.getInt("cnt");
                }
            }
        }
        return 0;
    }

    // ==================== TRAIN CREATION (NEW FOR USLP09) ====================

    public int createTrain(String operatorVat, LocalTime time, LocalDate date,
                           int pathId, int startEndpointId, int endEndpointId,
                           int maxLength) throws SQLException {

        String call = "{ ? = call RAILWAY_API.f_createTrain(?, ?, ?, ?, ?, ?, ?) }";

        try (Connection conn = DatabaseConnectionPool.getConnection();
             CallableStatement stmt = conn.prepareCall(call)) {

            stmt.registerOutParameter(1, Types.INTEGER);
            stmt.setString(2, operatorVat);
            stmt.setTime(3, Time.valueOf(time));
            stmt.setDate(4, Date.valueOf(date));
            stmt.setInt(5, pathId);
            stmt.setInt(6, startEndpointId);
            stmt.setInt(7, endEndpointId);
            stmt.setInt(8, maxLength);

            stmt.execute();
            return stmt.getInt(1);
        }
    }


    /**
     * Ensures a placeholder Path with name 'UNASSIGNED' exists and returns its id.
     *
     * Train.path is NOT NULL and references Path(id). Since USLP09 may create a train
     * before USLP08 generates the real path, we use Path('UNASSIGNED') as placeholder.
     */
    public int ensureUnassignedPath() throws SQLException {
        String call = "{ ? = call RAILWAY_API.f_ensureUnassignedPath() }";

        try (Connection conn = DatabaseConnectionPool.getConnection();
             CallableStatement stmt = conn.prepareCall(call)) {

            stmt.registerOutParameter(1, Types.INTEGER);
            stmt.execute();
            return stmt.getInt(1);
        }
    }

    /** Returns true if a Train with the given id already exists. */
    public boolean trainExists(int trainId) throws SQLException {
        String call = "{ ? = call RAILWAY_API.f_trainExists(?) }";

        try (Connection conn = DatabaseConnectionPool.getConnection();
             CallableStatement stmt = conn.prepareCall(call)) {

            stmt.registerOutParameter(1, Types.REF_CURSOR);
            stmt.setInt(2, trainId);
            stmt.execute();

            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                return rs.next();
            }
        }
    }

    /**
     * Creates a Train with an explicit id (chosen by the user).
     *
     * Operator is a VAT (FK to Operator.vat).
     * Time is stored as TIMESTAMP(0) using only time-of-day (matches your insert script).
     */
    public void createTrainWithId(int id,
                                  String operatorVat,
                                  LocalDate date,
                                  LocalTime time,
                                  int pathId,
                                  int maxLength) throws SQLException {

        String call = "{ call RAILWAY_API.p_createTrainWithId(?, ?, ?, ?, ?, ?) }";

        try (Connection conn = DatabaseConnectionPool.getConnection();
             CallableStatement ps = conn.prepareCall(call)) {

            ps.setInt(1, id);
            ps.setString(2, operatorVat);
            ps.setString(3, time.toString());
            ps.setInt(4, pathId);
            ps.setDate(5, Date.valueOf(date));
            ps.setInt(6, maxLength);

            ps.execute();
        }
    }


    public void updateTrainDeparture(int trainId, LocalDate newDate, LocalTime newTime)
            throws SQLException {

        String call = "{ call RAILWAY_API.p_updateTrainDeparture(?, ?, ?) }";

        try (Connection conn = DatabaseConnectionPool.getConnection();
             CallableStatement stmt = conn.prepareCall(call)) {

            stmt.setDate(1, Date.valueOf(newDate));
            stmt.setTime(2, Time.valueOf(newTime));
            stmt.setInt(3, trainId);
            stmt.execute();
        }
    }

    public void assignWagonToFreight(int wagonId, int freightId) throws SQLException {
        String call = "{ call RAILWAY_API.p_assignWagonToFreight(?, ?) }";
        try (Connection conn = DatabaseConnectionPool.getConnection();
             CallableStatement stmt = conn.prepareCall(call)) {
            stmt.setInt(1, wagonId);
            stmt.setInt(2, freightId);
            stmt.execute();
        }
    }

    public boolean isWagonAssignedToAnyFreight(int wagonId) throws SQLException {
        String call = "{ ? = call RAILWAY_API.f_isWagonAssignedToAnyFreight(?) }";

        try (Connection conn = DatabaseConnectionPool.getConnection();
             CallableStatement stmt = conn.prepareCall(call)) {

            stmt.registerOutParameter(1, Types.REF_CURSOR);
            stmt.setInt(2, wagonId);
            stmt.execute();

            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                return rs.next();
            }
        }
    }


    public List<Train> getTrainsWithUnassignedPath() throws SQLException {
        String call = "{ ? = call RAILWAY_API.f_getTrainsWithUnassignedPath() }";

        List<Train> res = new ArrayList<>();

        try (Connection conn = DatabaseConnectionPool.getConnection();
             CallableStatement ps = conn.prepareCall(call)) {

            ps.registerOutParameter(1, Types.REF_CURSOR);
            ps.execute();

            try (ResultSet rs = (ResultSet) ps.getObject(1)) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String operatorVat = rs.getString("operator");

                    LocalTime departureTime = rs.getTimestamp("time").toLocalDateTime().toLocalTime();
                    LocalDate date = rs.getDate("date").toLocalDate();

                    // Criar Path mínimo (ajusta se o teu Path tiver outro construtor)
                    Path path = new Path(rs.getInt("path_id"), rs.getString("path_name"));

                    Train tr = new Train(
                            id,
                            operatorVat,
                            departureTime,
                            date,
                            path,
                            null,
                            null
                    );

                    res.add(tr);
                }
            }
        }

        return res;
    }


    public List<Freight> getFreightsByTrain(int trainId) throws SQLException {
        String call = "{ ? = call RAILWAY_API.f_getFreightsByTrain(?) }";

        List<Freight> res = new ArrayList<>();

        try (Connection conn = DatabaseConnectionPool.getConnection();
             CallableStatement ps = conn.prepareCall(call)) {

            ps.registerOutParameter(1, Types.REF_CURSOR);
            ps.setInt(2, trainId);
            ps.execute();

            try (ResultSet rs = (ResultSet) ps.getObject(1)) {
                while (rs.next()) {
                    int freightId = rs.getInt("id");
                    LocalDate date = rs.getDate("date").toLocalDate();

                    int originId = rs.getInt("origin");
                    int destId = rs.getInt("destination");

                    Endpoint origin = getEndpointById(originId);
                    Endpoint dest = getEndpointById(destId);

                    Freight f = new Freight(freightId, date, origin, dest);
                    res.add(f);
                }
            }
        }

        return res;
    }


    public void updateTrainPath(int trainId, int pathId) throws SQLException {
        String call = "{ ? = call RAILWAY_API.f_updateTrainPath(?, ?) }";

        try (Connection conn = DatabaseConnectionPool.getConnection();
             CallableStatement ps = conn.prepareCall(call)) {

            ps.registerOutParameter(1, Types.INTEGER);
            ps.setInt(2, pathId);
            ps.setInt(3, trainId);

            ps.execute();
            int n = ps.getInt(1);

            if (n != 1) {
                throw new SQLException("Train not found or not updated: id=" + trainId);
            }

            conn.commit();
        }
    }





}