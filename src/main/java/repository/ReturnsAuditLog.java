package repository;

import domain.ReturnDecision;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Appends return inspection decisions to an audit log file.
 */
public class ReturnsAuditLog {

    private final String logFilePath;

    public ReturnsAuditLog(String logFilePath) {
        this.logFilePath = logFilePath;
    }

    /**
     * Appends a ReturnDecision to the log file.
     *
     * @param decision the decision to be logged
     * @throws IOException if file writing fails
     */
    public void append(ReturnDecision decision) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFilePath, true))) {
            writer.write(decision.toString());
            writer.newLine();
        }
    }
}
