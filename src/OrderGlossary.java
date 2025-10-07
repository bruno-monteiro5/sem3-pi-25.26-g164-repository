import java.nio.file.*;
import java.util.*;
import java.io.IOException;

public class OrderGlossary {

    public static void main(String[] args) throws IOException {
        String entryFile = "doc/global-artifacts/glossary.md";
        String outFile   = "doc/global-artifacts/glossary.md";

        List<String> lines = Files.readAllLines(Path.of(entryFile));

        List<String> preface = new ArrayList<>();
        String headerRow = null;
        String separatorRow = null;
        List<String> dataRows = new ArrayList<>();

        boolean seenTable = false;

        for (String line : lines) {
            String t = line.trim();

            if (t.startsWith("|:") || t.matches("^\\|\\s*-+\\s*\\|\\s*-+\\s*\\|\\s*$")) {
                separatorRow = line;
                seenTable = true;
            } else if (t.startsWith("|")) {
                if (t.matches("(?i)^\\|\\s*TEA\\s*(\\(EN\\))?\\s*\\|\\s*Description\\s*\\(EN\\)\\s*\\|\\s*$")) {
                    headerRow = "| TEA (EN) | Description (EN) |";
                    seenTable = true;
                    continue;
                }
                dataRows.add(line);
                seenTable = true;
            } else {
                if (!seenTable) preface.add(line);
            }
        }

        if (headerRow == null) headerRow = "| TEA (EN) | Description (EN) |";
        if (separatorRow == null) separatorRow = "|:---------|:------------------|";

        dataRows.sort(Comparator.comparing(l -> {
            String[] parts = l.split("\\|");
            return parts.length > 1 ? parts[1].trim().toLowerCase() : "";
        }));

        List<String> out = new ArrayList<>();
        out.addAll(preface);
        if (!out.isEmpty() && !out.get(out.size()-1).isBlank()) out.add("");
        out.add(headerRow);
        out.add(separatorRow);
        out.addAll(dataRows);

        Files.write(Path.of(outFile), out);
    }
}
