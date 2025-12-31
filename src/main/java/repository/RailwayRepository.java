package repository;

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
        String query = """
            SELECT id, name, metafacility
            FROM Endpoint
            ORDER BY name
        """;

        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                endpoints.add(new Endpoint(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("metafacility")
                ));
            }
        }
        return endpoints;
    }

    public Endpoint getEndpointById(int id) throws SQLException {
        String query = "SELECT id, name, metafacility FROM Endpoint WHERE id = ?";

        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
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
        String query = """
            SELECT l.id, l.name, l.owner, l.endpoint_a, l.endpoint_b, l.gauge,
                   ea.name as endpoint_a_name, eb.name as endpoint_b_name
            FROM Line l
            JOIN Endpoint ea ON l.endpoint_a = ea.id
            JOIN Endpoint eb ON l.endpoint_b = eb.id
            ORDER BY l.name
        """;

        try (Connection conn = DatabaseConnectionPool.getConnection();
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

    // ==================== LINE SEGMENTS ====================

    public List<LineSegment> getLineSegments(int lineId) throws SQLException {
        List<LineSegment> segments = new ArrayList<>();
        String query = """
            SELECT ls.id, ls.is_electrified, ls.speed_limit, ls.max_weight, 
                   ls.length, ls.line
            FROM Line_Segment ls
            WHERE ls.line = ?
            ORDER BY ls.id
        """;

        try (Connection conn = DatabaseConnectionPool.getConnection();
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

    public TrackType getTrackType(int segmentId) throws SQLException {
        String queryMultiple = """
            SELECT number_tracks 
            FROM Multiple_Tracks 
            WHERE segment = ?
        """;

        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(queryMultiple)) {

            stmt.setInt(1, segmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new MultipleTrack(segmentId, rs.getInt("number_tracks"));
                }
            }
        }

        String querySingle = "SELECT segment FROM Single_Track WHERE segment = ?";
        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(querySingle)) {

            stmt.setInt(1, segmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return getSidingInfo(segmentId);
                }
            }
        }

        return null;
    }

    private SingleTrack getSidingInfo(int segmentId) throws SQLException {
        String query = """
            SELECT position, length 
            FROM Siding 
            WHERE segment = ?
        """;

        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, segmentId);
            try (ResultSet rs = stmt.executeQuery()) {
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

        String query = """
            SELECT l.id, l.name, l.service_start, l.total_weight, l.op_speed,
                   lm.id as model_id, lm.name as model_name, lm.make, 
                   lm.power, lm.length, lm.max_speed,
                   lmg.gaugeid,
                   CASE WHEN elm.model IS NOT NULL THEN 'ELECTRIC'
                        WHEN dlm.model IS NOT NULL THEN 'DIESEL'
                        ELSE 'UNKNOWN' END as traction_type
            FROM Locomotive l
            JOIN Locomotive_Model lm ON l.model = lm.id
            LEFT JOIN Electric_Locomotive_Model elm ON lm.id = elm.model
            LEFT JOIN Diesel_Locomotive_Model dlm ON lm.id = dlm.model
            JOIN Locomotive_Model_Gauge lmg ON lm.id = lmg.locomotive_modelid
            ORDER BY l.name
        """;

        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

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
        return locomotives;
    }

    /**
     * Gets a single locomotive by ID.
     */
    public Locomotive getLocomotiveById(int locomotiveId) throws SQLException {
        String query = """
            SELECT l.id, l.name, l.service_start, l.total_weight, l.op_speed,
                   lm.id as model_id, lm.name as model_name, lm.make, 
                   lm.power, lm.length, lm.max_speed,
                   lmg.gaugeid,
                   CASE WHEN elm.model IS NOT NULL THEN 'ELECTRIC'
                        WHEN dlm.model IS NOT NULL THEN 'DIESEL'
                        ELSE 'UNKNOWN' END as traction_type
            FROM Locomotive l
            JOIN Locomotive_Model lm ON l.model = lm.id
            LEFT JOIN Electric_Locomotive_Model elm ON lm.id = elm.model
            LEFT JOIN Diesel_Locomotive_Model dlm ON lm.id = dlm.model
            JOIN Locomotive_Model_Gauge lmg ON lm.id = lmg.locomotive_modelid
            WHERE l.id = ?
        """;

        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, locomotiveId);

            try (ResultSet rs = stmt.executeQuery()) {
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
        String queryPaths = "SELECT id, name FROM Path ORDER BY name";

        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(queryPaths);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Path path = new Path(
                        rs.getInt("id"),
                        rs.getString("name")
                );
                paths.add(path);
            }
        }

        for (Path path : paths) {
            loadPathEndpoints(path);
        }

        return paths;
    }

    public Path getPathById(int pathId) throws SQLException {
        String query = "SELECT id, name FROM Path WHERE id = ?";

        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, pathId);
            try (ResultSet rs = stmt.executeQuery()) {
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
        String query = """
            SELECT ep.seq, ep.endpoint, ep.must_stop, e.name, e.metafacility
            FROM Endpoint_Path ep
            JOIN Endpoint e ON ep.endpoint = e.id
            WHERE ep.path = ?
            ORDER BY ep.seq
        """;

        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, path.getId());
            try (ResultSet rs = stmt.executeQuery()) {
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
        String query = """
            SELECT t.id, t.operator, t.time, t."date", t.path, t.max_length
            FROM Train t
            ORDER BY t."date", t.time
        """;

        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                dtos.add(mapToTrainDto(rs));
            }
        }

        List<Train> trains = new ArrayList<>();
        for (TrainDto dto : dtos) {
            Train train = hydrateTrain(dto);
            loadTrainLocomotives(train);
            loadTrainFreights(train);
            trains.add(train);
        }
        return trains;
    }

    public Train getTrainById(int trainId) throws SQLException {
        String query = """
            SELECT t.id, t.operator, t.time, t."date", t.path, t.max_length
            FROM Train t
            WHERE t.id = ?
        """;

        TrainDto dto = null;

        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, trainId);
            try (ResultSet rs = stmt.executeQuery()) {
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
        String query = """
            SELECT l.id, l.name, l.service_start, l.total_weight, l.op_speed,
                   lm.id as model_id, lm.name as model_name, lm.make, 
                   lm.power, lm.length, lm.max_speed,
                   lmg.gaugeid,
                   CASE WHEN elm.model IS NOT NULL THEN 'ELECTRIC'
                        WHEN dlm.model IS NOT NULL THEN 'DIESEL'
                        ELSE 'UNKNOWN' END as traction_type
            FROM Locomotive_Train lt
            JOIN Locomotive l ON lt.locomotive = l.id
            JOIN Locomotive_Model lm ON l.model = lm.id
            LEFT JOIN Electric_Locomotive_Model elm ON lm.id = elm.model
            LEFT JOIN Diesel_Locomotive_Model dlm ON lm.id = dlm.model
            JOIN Locomotive_Model_Gauge lmg ON lm.id = lmg.locomotive_modelid
            WHERE lt.train = ?
            ORDER BY lt.position
        """;

        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, train.getId());
            try (ResultSet rs = stmt.executeQuery()) {
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

        String query = """
            SELECT f.id, f."date", f.origin, f.destination
            FROM Freight_Train ft
            JOIN Freight f ON ft.freight = f.id
            WHERE ft.train = ?
        """;

        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, train.getId());
            try (ResultSet rs = stmt.executeQuery()) {
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

        String query = """
            SELECT w.id, w.service_start, w.operator, 
                   wm.id as model_id, wm.model, wm.maker, wm.number_bogies, 
                   wm.length, wm.width, wm.height, wm.weight, wm.max_speed,
                   wm.payload, wm.volume, wm.type
            FROM Wagon_Freight wf
            JOIN Wagon w ON wf.wagon = w.id
            JOIN Wagon_Model wm ON w.wagon_model = wm.id
            WHERE wf.freight = ?
        """;

        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, freight.getId());
            try (ResultSet rs = stmt.executeQuery()) {
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
        String query = """
            SELECT Gaugeid 
            FROM Wagon_Model_Gauge 
            WHERE Wagon_Modelid = ?
            FETCH FIRST 1 ROW ONLY
        """;

        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, wagonModelId);
            try (ResultSet rs = stmt.executeQuery()) {
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
        String query = """
            SELECT w.id, w.service_start, w.wagon_model, w.operator,
                   wm.model, wm.maker, wm.number_bogies, wm.length, wm.width,
                   wm.height, wm.weight, wm.max_speed, wm.payload, 
                   wm.volume, wm.type
            FROM Wagon w
            JOIN Wagon_Model wm ON w.wagon_model = wm.id
            WHERE w.id = ?
        """;

        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, wagonId);

            try (ResultSet rs = stmt.executeQuery()) {
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

        String getMaxId = "SELECT NVL(MAX(id), 0) AS max_id FROM Path";
        String insertPath = "INSERT INTO Path (id, name) VALUES (?, ?)";

        try (Connection conn = DatabaseConnectionPool.getConnection()) {

            try (PreparedStatement ps = conn.prepareStatement(getMaxId);
                 ResultSet rs = ps.executeQuery()) {
                rs.next();
                newId = rs.getInt("max_id") + 1;
            }

            try (PreparedStatement ps = conn.prepareStatement(insertPath)) {
                ps.setInt(1, newId);
                ps.setString(2, name);
                ps.executeUpdate();
            }
        }

        return new Path(newId, name);
    }

    public void addEndpointToPath(int pathId, int seq, int endpointId, boolean mustStop) throws SQLException {
        String sql = "INSERT INTO Endpoint_Path (path, seq, endpoint, must_stop) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, pathId);
            ps.setInt(2, seq);
            ps.setInt(3, endpointId);
            ps.setString(4, mustStop ? "Y" : "N");
            ps.executeUpdate();
        }
    }

    public void updateTrainRoute(int trainId, int pathId) throws SQLException {
        String sql = """
            UPDATE Train
            SET path = ?
            WHERE id = ?
        """;

        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, pathId);
            ps.setInt(2, trainId);

            ps.executeUpdate();
        }
    }

    // ==================== FREIGHT MANAGEMENT (NEW FOR USLP08/09) ====================

    public List<Freight> getPendingFreights() throws SQLException {
        List<Freight> freights = new ArrayList<>();

        String query = """
            SELECT f.id, f."date", f.origin, f.destination
            FROM Freight f
            WHERE NOT EXISTS (
                SELECT 1 FROM Freight_Train ft WHERE ft.freight = f.id
            )
            ORDER BY f."date", f.id
        """;

        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

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

        return freights;
    }

    public Freight getFreightById(int freightId) throws SQLException {
        String query = """
            SELECT f.id, f."date", f.origin, f.destination
            FROM Freight f
            WHERE f.id = ?
        """;

        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, freightId);

            try (ResultSet rs = stmt.executeQuery()) {
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
        String sql = "INSERT INTO Freight_Train (freight, train) VALUES (?, ?)";

        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, freightId);
            stmt.setInt(2, trainId);
            stmt.executeUpdate();
        }
    }

    public void removeFreightFromTrain(int freightId, int trainId) throws SQLException {
        String sql = "DELETE FROM Freight_Train WHERE freight = ? AND train = ?";

        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, freightId);
            stmt.setInt(2, trainId);
            stmt.executeUpdate();
        }
    }

    public int createFreight(LocalDate date, int originEndpointId, int destinationEndpointId) throws SQLException {
        String getMax = "SELECT NVL(MAX(id), 0) AS max_id FROM Freight";
        String insert = "INSERT INTO Freight (id, \"date\", origin, destination) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnectionPool.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int newId;
                try (PreparedStatement ps = conn.prepareStatement(getMax);
                     ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    newId = rs.getInt("max_id") + 1;
                }

                try (PreparedStatement ps = conn.prepareStatement(insert)) {
                    ps.setInt(1, newId);
                    ps.setDate(2, Date.valueOf(date));
                    ps.setInt(3, originEndpointId);
                    ps.setInt(4, destinationEndpointId);
                    ps.executeUpdate();
                }

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
        String sql = "INSERT INTO Locomotive_Train (locomotive, train, position) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, locomotiveId);
            stmt.setInt(2, trainId);
            stmt.setInt(3, position);
            stmt.executeUpdate();
        }
    }

    public void removeLocomotiveFromTrain(int locomotiveId, int trainId) throws SQLException {
        String sql = "DELETE FROM Locomotive_Train WHERE locomotive = ? AND train = ?";

        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, locomotiveId);
            stmt.setInt(2, trainId);
            stmt.executeUpdate();
        }
    }

    public void removeAllLocomotivesFromTrain(int trainId) throws SQLException {
        String sql = "DELETE FROM Locomotive_Train WHERE train = ?";

        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, trainId);
            stmt.executeUpdate();
        }
    }

    public int getLocomotiveCountForTrain(int trainId) throws SQLException {
        String sql = "SELECT COUNT(*) as cnt FROM Locomotive_Train WHERE train = ?";

        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, trainId);

            try (ResultSet rs = stmt.executeQuery()) {
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

        int newId;

        String getMaxId = "SELECT NVL(MAX(id), 0) AS max_id FROM Train";
        String insertTrain = """
            INSERT INTO Train (id, operator, time, "date", path, "start", "end", max_length)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConnectionPool.getConnection()) {

            try (PreparedStatement ps = conn.prepareStatement(getMaxId);
                 ResultSet rs = ps.executeQuery()) {
                rs.next();
                newId = rs.getInt("max_id") + 1;
            }

            try (PreparedStatement ps = conn.prepareStatement(insertTrain)) {
                ps.setInt(1, newId);
                ps.setString(2, operatorVat);
                ps.setTime(3, Time.valueOf(time));
                ps.setDate(4, Date.valueOf(date));
                ps.setInt(5, pathId);
                ps.setInt(6, startEndpointId);
                ps.setInt(7, endEndpointId);
                ps.setInt(8, maxLength);
                ps.executeUpdate();
            }
        }

        return newId;
    }


    /**
     * Ensures a placeholder Path with name 'UNASSIGNED' exists and returns its id.
     *
     * Train.path is NOT NULL and references Path(id). Since USLP09 may create a train
     * before USLP08 generates the real path, we use Path('UNASSIGNED') as placeholder.
     */
    public int ensureUnassignedPath() throws SQLException {
        String select = "SELECT id FROM Path WHERE name = 'UNASSIGNED'";
        String getMax = "SELECT NVL(MAX(id), 0) AS max_id FROM Path";
        String insert = "INSERT INTO Path (id, name) VALUES (?, 'UNASSIGNED')";

        try (Connection conn = DatabaseConnectionPool.getConnection()) {

            try (PreparedStatement ps = conn.prepareStatement(select);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }

            int newId;
            try (PreparedStatement ps = conn.prepareStatement(getMax);
                 ResultSet rs = ps.executeQuery()) {
                rs.next();
                newId = rs.getInt("max_id") + 1;
            }

            try (PreparedStatement ps = conn.prepareStatement(insert)) {
                ps.setInt(1, newId);
                ps.executeUpdate();
            }

            return newId;
        }
    }

    /** Returns true if a Train with the given id already exists. */
    public boolean trainExists(int trainId) throws SQLException {
        String sql = "SELECT 1 FROM Train WHERE id = ?";
        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, trainId);
            try (ResultSet rs = ps.executeQuery()) {
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

        String sql = """
        INSERT INTO Train (id, operator, time, path, "date", max_length)
        VALUES (?, ?, TO_TIMESTAMP(?, 'HH24:MI:SS'), ?, ?, ?)
    """;

        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, operatorVat);
            ps.setString(3, time.toString());
            ps.setInt(4, pathId);
            ps.setDate(5, Date.valueOf(date));
            ps.setInt(6, maxLength);
            ps.executeUpdate();
        }
    }


    public void updateTrainDeparture(int trainId, LocalDate newDate, LocalTime newTime)
            throws SQLException {

        String sql = """
            UPDATE Train
            SET "date" = ?, time = ?
            WHERE id = ?
        """;

        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(newDate));
            stmt.setTime(2, Time.valueOf(newTime));
            stmt.setInt(3, trainId);
            stmt.executeUpdate();
        }
    }

    public void assignWagonToFreight(int wagonId, int freightId) throws SQLException {
        String sql = "INSERT INTO Wagon_Freight (wagon, freight) VALUES (?, ?)";
        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, wagonId);
            ps.setInt(2, freightId);
            ps.executeUpdate();
        }
    }

}