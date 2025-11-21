package repository;

import domain.railway.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RailwayRepository {

    // ==================== ENDPOINTS ====================

    public List<Endpoint> getAllEndpoints() throws SQLException {
        List<Endpoint> endpoints = new ArrayList<>();
        String query = """
            SELECT id, name, metafacility
            FROM Endpoint
            ORDER BY name
        """;

        try (Connection conn = DatabaseConnection.getConnection();
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

        try (Connection conn = DatabaseConnection.getConnection();
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
     * Obtém o tipo de via de um segmento (Single/Multiple Track)
     */
    public TrackType getTrackType(int segmentId) throws SQLException {
        // Primeiro verifica se é Multiple Track
        String queryMultiple = """
            SELECT number_tracks 
            FROM Multiple_Tracks 
            WHERE segment = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(queryMultiple)) {

            stmt.setInt(1, segmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new MultipleTrack(segmentId, rs.getInt("number_tracks"));
                }
            }
        }

        // Se não for Multiple, verifica Single Track
        String querySingle = "SELECT segment FROM Single_Track WHERE segment = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(querySingle)) {

            stmt.setInt(1, segmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Verifica se tem Siding
                    return getSidingInfo(segmentId);
                }
            }
        }

        return null; // Não encontrado
    }

    private SingleTrack getSidingInfo(int segmentId) throws SQLException {
        String query = """
            SELECT position, length 
            FROM Siding 
            WHERE segment = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
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

        // Single Track sem siding
        return new SingleTrack(segmentId);
    }

    // ==================== LOCOMOTIVES ====================

    public List<Locomotive> getAllLocomotives() throws SQLException {
        List<Locomotive> locomotives = new ArrayList<>();
        String query = """
            SELECT l.id, l.name, l.make, l.model, l.power, l.max_speed, l.op_speed, 
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
                        rs.getInt("power"),
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

    // ==================== PATHS ====================

    public List<Path> getAllPaths() throws SQLException {
        List<Path> paths = new ArrayList<>();
        String queryPaths = "SELECT id, name FROM Path ORDER BY name";

        // Passo 1: Carregar os objetos básicos (ID e Nome)
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(queryPaths);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Path path = new Path(
                        rs.getInt("id"),
                        rs.getString("name")
                );
                // Removido: loadPathEndpoints(path); - Causa conflito de conexão dentro do loop
                paths.add(path);
            }
        }

        // Passo 2: Carregar os detalhes (Endpoints) fora do contexto do ResultSet anterior
        // Isto permite que o método loadPathEndpoints gerencie a sua própria conexão sem fechar a do loop principal
        for (Path path : paths) {
            loadPathEndpoints(path);
        }

        return paths;
    }

    public Path getPathById(int pathId) throws SQLException {
        String query = "SELECT id, name FROM Path WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
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

        try (Connection conn = DatabaseConnection.getConnection();
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

    // Record auxiliar para armazenar dados temporários e libertar a conexão rapidamente
    private record TrainDto(int id, String operator, LocalTime time, LocalDate date, int pathId, int startId, int endId) {}

    public List<Train> getAllTrains() throws SQLException {
        List<TrainDto> dtos = new ArrayList<>();
        String query = """
            SELECT t.id, t.operator, t.time, t."date", t.path, t."start", t."end"
                                   FROM Train t
                                   ORDER BY t."date", t.time
        """;

        // PASSO 1: Extrair apenas os dados básicos e IDs
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                dtos.add(mapToTrainDto(rs));
            }
        } // A conexão é fechada aqui, o que é seguro.

        // PASSO 2: Hidratar os objetos (carregar dependências)
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
            SELECT t.id, t.operator, t.time, t.date, t.path, t.start, t.end
            FROM Train t
            WHERE t.id = ?
        """;

        TrainDto dto = null;

        // PASSO 1: Extrair dados básicos
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, trainId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    dto = mapToTrainDto(rs);
                }
            }
        }

        // PASSO 2: Hidratar se encontrado
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
                rs.getInt("id"),
                rs.getString("operator"),
                rs.getTime("time").toLocalTime(),
                rs.getDate("date").toLocalDate(),
                rs.getInt("path"),
                rs.getInt("start"),
                rs.getInt("end")
        );
    }

    private Train hydrateTrain(TrainDto dto) throws SQLException {
        // Agora é seguro chamar estes métodos, pois eles abrem/fecham as suas próprias conexões
        Path path = getPathById(dto.pathId());
        Endpoint start = getEndpointById(dto.startId());
        Endpoint end = getEndpointById(dto.endId());

        return new Train(dto.id(), dto.operator(), dto.time(), dto.date(), path, start, end);
    }

    private void loadTrainLocomotives(Train train) throws SQLException {
        String query = """
            SELECT l.id, l.name, l.make, l.model, l.power, l.max_speed, l.op_speed,
                   l.total_weight, lg.gauge,
                   CASE WHEN el.locomotive IS NOT NULL THEN 'ELECTRIC'
                        WHEN dl.locomotive IS NOT NULL THEN 'DIESEL'
                        ELSE 'UNKNOWN' END as traction_type
            FROM Locomotive_Train lt
            JOIN Locomotive l ON lt.locomotive = l.id
            LEFT JOIN Electric_Locomotive el ON l.id = el.locomotive
            LEFT JOIN Diesel_Locomotive dl ON l.id = dl.locomotive
            JOIN Locomotive_Gauge lg ON l.id = lg.locomotive
            WHERE lt.train = ?
            ORDER BY lt.position
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, train.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Locomotive loco = new Locomotive(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("make"),
                            rs.getString("model"),
                            rs.getInt("power"),
                            rs.getInt("max_speed"),
                            rs.getInt("op_speed"),
                            rs.getString("traction_type"),
                            rs.getInt("total_weight"),
                            rs.getInt("gauge")
                    );
                    train.addLocomotive(loco);
                }
            }
        }
    }

    private void loadTrainFreights(Train train) throws SQLException {
        // Record temporário para evitar conflito de conexão aninhada (Connection Closed error)
        record FreightDto(int id, LocalDate date, int originId, int destinationId) {}
        List<FreightDto> dtos = new ArrayList<>();

        String query = """
            SELECT f.id, f."date", f.origin, f.destination
            FROM Freight f
            WHERE f.train = ?
        """;

        // PASSO 1: Extrair apenas os dados básicos
        try (Connection conn = DatabaseConnection.getConnection();
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
        } // A conexão fecha aqui com segurança

        // PASSO 2: Hidratar os objetos (cada método abrirá sua própria conexão sem conflito)
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
        // Record temporário para armazenar dados brutos e libertar a conexão antes de processar dependências
        record WagonRawData(int id, int serviceYear, String operator,
                            String model, String maker, int bogies, int length,
                            int width, int height, double weight, int maxSpeed,
                            double payload, double volume, String type) {}

        List<WagonRawData> rawDataList = new ArrayList<>();

        String query = """
            SELECT w.id, w.service_year, w.operator, wm.*
            FROM Wagon_Freight wf
            JOIN Wagon w ON wf.wagon = w.id
            JOIN Wagon_Model wm ON w.wagon_model = wm.id
            WHERE wf.freight = ?
        """;

        // PASSO 1: Extrair dados brutos (Connection aberta apenas aqui)
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, freight.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // Armazenamos apenas os dados primitivos
                    rawDataList.add(new WagonRawData(
                            rs.getInt("id"),
                            rs.getInt("service_year"),
                            rs.getString("operator"),
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
        } // Conexão fecha aqui com segurança

        // PASSO 2: Hidratar objetos (agora é seguro chamar métodos que abrem novas conexões)
        for (WagonRawData data : rawDataList) {
            // getWagonGauge agora pode abrir/fechar sua própria conexão sem afetar o loop anterior
            Object gaugeInfo = getWagonGauge(data.id());

            WagonModel model = new WagonModel(
                    data.id(),
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
                    (Integer) gaugeInfo
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

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, wagonModelId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("Gaugeid");
                }
            }
        }
        return 1668; // default gauge
    }







    /**
     * Cria um novo Path com um nome dado.
     * Gera o ID como MAX(id)+1.
     */
    public Path createPath(String name) throws SQLException {
        int newId;

        String getMaxId = "SELECT NVL(MAX(id), 0) AS max_id FROM Path";
        String insertPath = "INSERT INTO Path (id, name) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {

            // 1) Calcular novo ID
            try (PreparedStatement ps = conn.prepareStatement(getMaxId);
                 ResultSet rs = ps.executeQuery()) {
                rs.next();
                newId = rs.getInt("max_id") + 1;
            }

            // 2) Inserir path
            try (PreparedStatement ps = conn.prepareStatement(insertPath)) {
                ps.setInt(1, newId);
                ps.setString(2, name);
                ps.executeUpdate();
            }
        }

        return new Path(newId, name);
    }

    /**
     * Adiciona um endpoint à tabela Endpoint_Path para um path.
     */
    public void addEndpointToPath(int pathId, int seq, int endpointId, boolean mustStop) throws SQLException {
        String sql = "INSERT INTO Endpoint_Path (path, seq, endpoint, must_stop) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, pathId);
            ps.setInt(2, seq);
            ps.setInt(3, endpointId);
            ps.setString(4, mustStop ? "Y" : "N");
            ps.executeUpdate();
        }
    }

    /**
     * Atualiza o path e os endpoints de início/fim de um train.
     */
    public void updateTrainRoute(int trainId, int pathId, int startEndpointId, int endEndpointId) throws SQLException {
        String sql = """
            UPDATE Train
            SET path = ?, "start" = ?, "end" = ?
            WHERE id = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, pathId);
            ps.setInt(2, startEndpointId);
            ps.setInt(3, endEndpointId);
            ps.setInt(4, trainId);

            ps.executeUpdate();
        }
    }



}