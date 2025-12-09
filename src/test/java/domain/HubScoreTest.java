package domain;

import org.junit.Test;
import static org.junit.Assert.*;

public class HubScoreTest {

    /**
     * Simple fake Station object for testing purposes.
     * We only implement the methods used by HubScore (getId and getName).
     */
    private static class FakeStation extends Station {
        private final int id;
        private final String name;

        public FakeStation(int id, String name) {
            super(0, "", null, 0, 0); // bypass original constructor
            this.id = id;
            this.name = name;
        }

        @Override
        public int getId() { return id; }

        @Override
        public String getName() { return name; }
    }

    @Test
    public void testGetters() {
        Station s = new FakeStation(1, "Porto Campanhã");

        HubScore hs = new HubScore(s, 5, 12.5, 0.3, 0.7, 0.55);

        assertEquals(s, hs.getStation());
        assertEquals(5, hs.getDegree());
        assertEquals(12.5, hs.getStrength(), 0.0001);
        assertEquals(0.3, hs.getBetweenness(), 0.0001);
        assertEquals(0.7, hs.getHarmonicCloseness(), 0.0001);
        assertEquals(0.55, hs.getHubScore(), 0.0001);
    }

    @Test
    public void testCompareTo() {
        Station a = new FakeStation(1, "A");
        Station b = new FakeStation(2, "B");

        HubScore hs1 = new HubScore(a, 1, 10, 0.1, 0.2, 0.30);
        HubScore hs2 = new HubScore(b, 2, 20, 0.2, 0.3, 0.80);

        assertTrue(hs1.compareTo(hs2) > 0);
        assertTrue(hs2.compareTo(hs1) < 0);
        assertEquals(0, hs1.compareTo(hs1));
    }

    @Test
    public void testToString() {
        Station s = new FakeStation(10, "Gaia");

        HubScore hs = new HubScore(s, 3, 12.5, 0.1234, 0.5678, 0.9999);

        String str = hs.toString();

        assertTrue(str.contains("Gaia"));
        assertTrue(str.contains("ID: 10"));
        assertTrue(str.contains("Degree: 3"));
        assertTrue(str.contains("Strength: 12.50"));
        assertTrue(str.contains("Betweenness: 0.1234"));
        assertTrue(str.contains("Harmonic: 0.5678"));
        assertTrue(str.contains("HubScore: 0.9999"));
    }
}
