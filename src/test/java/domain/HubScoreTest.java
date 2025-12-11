package domain;

import org.junit.Test;
import static org.junit.Assert.*;

public class HubScoreTest {

    /**
     * Creates a properly initialized Station for testing.
     */
    private Station createTestStation(int id, String name) {
        Location loc = new Location(41.1579, -8.6291); // Porto coordinates
        return new Station(id, name, loc, 0.0, 0.0);
    }

    @Test
    public void testGetters() {
        Station s = createTestStation(1, "Porto Campanhã");

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
        Station a = createTestStation(1, "A");
        Station b = createTestStation(2, "B");

        HubScore hs1 = new HubScore(a, 1, 10, 0.1, 0.2, 0.30);
        HubScore hs2 = new HubScore(b, 2, 20, 0.2, 0.3, 0.80);

        // hs2 has higher hubScore (0.80 > 0.30), so it should come first
        assertTrue("hs1 should be greater than hs2 (lower priority)", hs1.compareTo(hs2) > 0);
        assertTrue("hs2 should be less than hs1 (higher priority)", hs2.compareTo(hs1) < 0);
        assertEquals("Same object should have compareTo = 0", 0, hs1.compareTo(hs1));
    }

    @Test
    public void testCompareToWithEqualScores() {
        Station a = createTestStation(1, "A");
        Station b = createTestStation(2, "B");

        HubScore hs1 = new HubScore(a, 1, 10, 0.1, 0.2, 0.50);
        HubScore hs2 = new HubScore(b, 2, 20, 0.2, 0.3, 0.50);

        assertEquals("Stations with equal hubScores should have compareTo = 0",
                0, hs1.compareTo(hs2));
    }

    @Test
    public void testCompareToDescendingOrder() {
        Station s1 = createTestStation(1, "Station1");
        Station s2 = createTestStation(2, "Station2");
        Station s3 = createTestStation(3, "Station3");

        HubScore hs1 = new HubScore(s1, 1, 10, 0.1, 0.2, 0.90);
        HubScore hs2 = new HubScore(s2, 2, 20, 0.2, 0.3, 0.50);
        HubScore hs3 = new HubScore(s3, 3, 30, 0.3, 0.4, 0.10);

        assertTrue("hs1 (0.90) should come before hs2 (0.50)",
                hs1.compareTo(hs2) < 0);
        assertTrue("hs2 (0.50) should come before hs3 (0.10)",
                hs2.compareTo(hs3) < 0);
        assertTrue("hs1 (0.90) should come before hs3 (0.10)",
                hs1.compareTo(hs3) < 0);
    }
}