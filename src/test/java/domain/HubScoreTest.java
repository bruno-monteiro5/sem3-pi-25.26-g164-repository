package domain;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;

import static org.junit.Assert.*;

public class HubScoreTest {

    /**
     * Creates a properly initialized Station for testing.
     */
    private Station createTestStation(int id, String name) {
        Location loc = new Location(41.1579, -8.6291);
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
    public void testToStringContainsKeyFields() {
        Station s = createTestStation(1, "Porto Campanhã");
        HubScore hs = new HubScore(s, 5, 12.5, 0.3, 0.7, 0.55);

        String text = hs.toString();

        assertNotNull(text);
        assertTrue(text.contains("Porto Campanhã"));
        assertTrue(text.contains("ID: 1"));
        assertTrue(text.contains("Degree: 5"));
        assertTrue(text.contains("Strength:"));
        assertTrue(text.contains("Betweenness:"));
        assertTrue(text.contains("Harmonic:"));
        assertTrue(text.contains("HubScore:"));
    }

    @Test
    public void testSortingByHubScoreDescending() {
        Station s1 = createTestStation(1, "Station1");
        Station s2 = createTestStation(2, "Station2");
        Station s3 = createTestStation(3, "Station3");

        HubScore hs1 = new HubScore(s1, 1, 10, 0.1, 0.2, 0.90);
        HubScore hs2 = new HubScore(s2, 2, 20, 0.2, 0.3, 0.50);
        HubScore hs3 = new HubScore(s3, 3, 30, 0.3, 0.4, 0.10);

        List<HubScore> list = new ArrayList<HubScore>();
        list.add(hs2);
        list.add(hs3);
        list.add(hs1);

        // Sort descending by hubScore (same criterion as controller's insertion sort)
        Collections.sort(list, new Comparator<HubScore>() {
            @Override
            public int compare(HubScore a, HubScore b) {
                // descending: bigger hubScore first
                if (a.getHubScore() < b.getHubScore()) return 1;
                if (a.getHubScore() > b.getHubScore()) return -1;
                return 0;
            }
        });

        assertEquals(hs1, list.get(0));
        assertEquals(hs2, list.get(1));
        assertEquals(hs3, list.get(2));
    }

    @Test
    public void testSortingWithEqualScoresKeepsAllElements() {
        Station a = createTestStation(1, "A");
        Station b = createTestStation(2, "B");

        HubScore hs1 = new HubScore(a, 1, 10, 0.1, 0.2, 0.50);
        HubScore hs2 = new HubScore(b, 2, 20, 0.2, 0.3, 0.50);

        List<HubScore> list = new ArrayList<HubScore>();
        list.add(hs1);
        list.add(hs2);

        Collections.sort(list, new Comparator<HubScore>() {
            @Override
            public int compare(HubScore x, HubScore y) {
                if (x.getHubScore() < y.getHubScore()) return 1;
                if (x.getHubScore() > y.getHubScore()) return -1;
                return 0;
            }
        });

        // both must still be there
        assertEquals(2, list.size());
        assertTrue(list.contains(hs1));
        assertTrue(list.contains(hs2));
    }
}
