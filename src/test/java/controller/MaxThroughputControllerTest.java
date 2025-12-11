package controller;

import domain.Station;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.*;

class MaxThroughputControllerTest {

    private MaxThroughputController controller;
    private final String stationsCsv = "test_stations.csv";
    private final String linesCsv = "test_lines.csv";

    /**
     * We build a small known flow network in order to make a more grounded and simplified (yet as efficient) test:
     *
     *      (6) ----10----> (8)
     *       |               |
     *       5               7
     *       |               |
     *      (9) ----15----> (10)
     *
     * Maximum flow from 6 → 10 = 7 + 5 = 12
     */
    @BeforeEach
    void setup() throws Exception {
        controller = new MaxThroughputController();

        // Create test stations CSV
        try (PrintWriter out = new PrintWriter(new FileWriter(stationsCsv))) {
            out.println("Station id,Station,Lat,Lon,CoordX,CoordY");
            out.println("6,AALST,0,0,0,0");
            out.println("8,AALTER,0,0,0,0");
            out.println("9,AARSCHOT,0,0,0,0");
            out.println("10,AARSELE,0,0,0,0");
        }

        // Create test lines CSV
        try (PrintWriter out = new PrintWriter(new FileWriter(linesCsv))) {
            out.println("departure_stid,arrival_stid,dist,capacity,cost");
            out.println("6,8,1,10,0");
            out.println("6,9,1,5,0");
            out.println("8,10,1,7,0");
            out.println("9,10,1,15,0");
        }
    }

    @Test
    void buildGraphFromCsv() {
        assertDoesNotThrow(() -> controller.buildGraphFromCsv(stationsCsv, linesCsv));
        assertTrue(controller.isGraphBuilt());
    }

    @Test
    void computeMaxFlow() {
        assertDoesNotThrow(() -> controller.buildGraphFromCsv(stationsCsv, linesCsv));

        Station s = controller.getStationById("AALST");
        Station t = controller.getStationById("AARSELE");

        assertNotNull(s);
        assertNotNull(t);

        int flow = controller.computeMaxFlow(s, t);
        assertEquals(12, flow);
    }

    @Test
    void isGraphBuilt() {
        assertFalse(controller.isGraphBuilt());

        assertDoesNotThrow(() -> controller.buildGraphFromCsv(stationsCsv, linesCsv));
        assertTrue(controller.isGraphBuilt());
    }

    @Test
    void getStationCount() {
        assertDoesNotThrow(() -> controller.buildGraphFromCsv(stationsCsv, linesCsv));
        assertEquals(4, controller.getStationCount());
    }

    @Test
    void getLineCount() {
        assertDoesNotThrow(() -> controller.buildGraphFromCsv(stationsCsv, linesCsv));
        assertEquals(4, controller.getLineCount());
    }

    @Test
    void getStationById() {
        assertDoesNotThrow(() -> controller.buildGraphFromCsv(stationsCsv, linesCsv));

        Station s8 = controller.getStationById("AALTER");
        assertNotNull(s8);
        assertEquals(8, s8.getId());

        Station missing = controller.getStationById("WSIGTWBUIGNEWBB");
        assertNull(missing);
    }
}
