package repository;

import domain.railway.*;
import domain.scheduling.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository for recording train movements in the database.
 * Works with Train_Movement, Stop, and Passage tables.
 *
 */
public class MovementRepository {

    /**
     * Records all movements from a TrainSchedule in the database.
     *
     * @param schedule The complete train schedule
     * @throws SQLException if database operation fails
     */
    public void recordScheduleMovements(TrainSchedule schedule) throws SQLException {
        List<TrainMovementRecord> records = convertScheduleToMovements(schedule);

        try (Connection conn = DatabaseConnectionPool.getConnection()) {
            conn.setAutoCommit(false);

            try {
                for (TrainMovementRecord record : records) {
                    insertMovement(conn, record);
                }

                conn.commit();

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /**
     * Converts a TrainSchedule into a list of movement records.
     */
    private List<TrainMovementRecord> convertScheduleToMovements(TrainSchedule schedule) {
        List<TrainMovementRecord> records = new ArrayList<>();
        int trainId = schedule.getTrain().getId();

        for (SegmentPassage passage : schedule.getPassages()) {
            // Each passage goes through 2 endpoints: departure and arrival

            // 1. Departure endpoint (always a passage unless it's the first)
            // (skip first departure - it's the train's starting point)

            // 2. Arrival endpoint (stop or passage)
            if (passage.requiresStop()) {
                // STOP: arrival time + 5 min stop = departure time
                LocalDateTime arrivalTime = passage.getArrivalTime();
                LocalDateTime departureTime = arrivalTime.plusMinutes(5);

                records.add(new TrainMovementRecord(
                        trainId,
                        passage.getArrivalEndpoint(),
                        arrivalTime,
                        departureTime
                ));
            } else {
                // PASSAGE: just passes through
                records.add(new TrainMovementRecord(
                        trainId,
                        passage.getArrivalEndpoint(),
                        passage.getArrivalTime()
                ));
            }
        }

        return records;
    }

    /**
     * Inserts a single movement record into the database.
     */
    private void insertMovement(Connection conn, TrainMovementRecord record) throws SQLException {
        // Step 1: Insert into Train_Movement
        String insertMovement = """
            INSERT INTO Train_Movement (train, endpoint)
            VALUES (?, ?)
        """;

        int movementId;

        try (PreparedStatement stmt = conn.prepareStatement(insertMovement,
                new String[]{"id"})) {

            stmt.setInt(1, record.getTrainId());
            stmt.setInt(2, record.getEndpoint().getId());
            stmt.executeUpdate();

            // Get generated ID
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    movementId = rs.getInt(1);
                    record.setId(movementId);
                } else {
                    throw new SQLException("Failed to get generated movement ID");
                }
            }
        }

        // Step 2: Insert into Stop or Passage
        if (record.isStop()) {
            String insertStop = """
                INSERT INTO Stop (arrival, departure, movement)
                VALUES (?, ?, ?)
            """;

            try (PreparedStatement stmt = conn.prepareStatement(insertStop)) {
                stmt.setTimestamp(1, Timestamp.valueOf(record.getArrivalTime()));
                stmt.setTimestamp(2, Timestamp.valueOf(record.getDepartureTime()));
                stmt.setInt(3, movementId);
                stmt.executeUpdate();
            }

        } else {
            String insertPassage = """
                INSERT INTO Passage (passage, movement)
                VALUES (?, ?)
            """;

            try (PreparedStatement stmt = conn.prepareStatement(insertPassage)) {
                stmt.setTimestamp(1, Timestamp.valueOf(record.getPassageTime()));
                stmt.setInt(2, movementId);
                stmt.executeUpdate();
            }
        }
    }

    /**
     * Gets all movements for a specific train.
     */
    public List<TrainMovementRecord> getMovementsByTrain(int trainId) throws SQLException {
        List<TrainMovementRecord> records = new ArrayList<>();

        String query = """
            SELECT tm.id, tm.train, tm.endpoint,
                   e.name as endpoint_name, e.metafacility,
                   s.arrival, s.departure,
                   p.passage
            FROM Train_Movement tm
            JOIN Endpoint e ON tm.endpoint = e.id
            LEFT JOIN Stop s ON tm.id = s.movement
            LEFT JOIN Passage p ON tm.id = p.movement
            WHERE tm.train = ?
            ORDER BY COALESCE(s.arrival, p.passage)
        """;

        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, trainId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Endpoint endpoint = new Endpoint(
                            rs.getInt("endpoint"),
                            rs.getString("endpoint_name"),
                            rs.getInt("metafacility")
                    );

                    TrainMovementRecord record;

                    Timestamp arrival = rs.getTimestamp("arrival");
                    if (arrival != null) {
                        // It's a STOP
                        record = new TrainMovementRecord(
                                trainId,
                                endpoint,
                                arrival.toLocalDateTime(),
                                rs.getTimestamp("departure").toLocalDateTime()
                        );
                    } else {
                        // It's a PASSAGE
                        record = new TrainMovementRecord(
                                trainId,
                                endpoint,
                                rs.getTimestamp("passage").toLocalDateTime()
                        );
                    }

                    record.setId(rs.getInt("id"));
                    records.add(record);
                }
            }
        }

        return records;
    }

    /**
     * Deletes all movements for a train (for rescheduling).
     */
    public void deleteMovementsByTrain(int trainId) throws SQLException {
        // Cascade delete will handle Stop and Passage tables
        String query = "DELETE FROM Train_Movement WHERE train = ?";

        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, trainId);
            stmt.executeUpdate();
        }
    }
}