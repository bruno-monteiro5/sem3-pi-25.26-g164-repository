package domain;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link ProximitySearchResult}.
 * <p>
 * These tests aim at full branch coverage, including the StationDistance
 * inner class and both branches of the comparator used in sortResults().
 */
public class ProximitySearchResultTest {

    /**
     * Helper method to create a Station with a given name.
     * <p>
     * Adjust the Location/Station constructor arguments if your project
     * uses a slightly different signature.
     */
    private Station createStation(String name) {
        Location location = new Location(0.0, 0.0);
        return new Station(
                name,
                "PT",
                location,
                "WET",
                false,
                false,
                false
        );
    }

    @Test
    public void addResultShouldIncreaseResultCountAndStoreStation() {
        ProximitySearchResult result =
                new ProximitySearchResult(10.0, 20.0, 3, null);

        Station station = createStation("Lisboa");
        result.addResult(station, 12.5);

        assertEquals(1, result.getResultCount());
        List<ProximitySearchResult.StationDistance> results = result.getResults();
        assertEquals(1, results.size());
        assertSame(station, results.get(0).getStation());
        assertEquals(12.5, results.get(0).getDistanceKm(), 0.0001);
    }

    @Test
    public void sortResultsShouldOrderByDistanceAscending() {
        ProximitySearchResult result =
                new ProximitySearchResult(10.0, 20.0, 3, null);

        Station s1 = createStation("A");
        Station s2 = createStation("B");
        Station s3 = createStation("C");

        // Distances intentionally out of order
        result.addResult(s1, 30.0);
        result.addResult(s2, 10.0);
        result.addResult(s3, 20.0);

        result.sortResults();
        List<ProximitySearchResult.StationDistance> sorted = result.getResults();

        assertEquals(3, sorted.size());
        assertSame(s2, sorted.get(0).getStation()); // 10.0 km
        assertSame(s3, sorted.get(1).getStation()); // 20.0 km
        assertSame(s1, sorted.get(2).getStation()); // 30.0 km
    }

    @Test
    public void sortResultsShouldUseStationNameAsTieBreakerWhenDistancesEqual() {
        ProximitySearchResult result =
                new ProximitySearchResult(10.0, 20.0, 3, null);

        Station sA = createStation("Alpha");
        Station sB = createStation("Beta");
        Station sC = createStation("Gamma");

        // Two same distance to force the comparator branch where distCompare == 0
        result.addResult(sB, 15.0);
        result.addResult(sA, 15.0);
        result.addResult(sC, 5.0);

        result.sortResults();
        List<ProximitySearchResult.StationDistance> sorted = result.getResults();

        assertEquals(3, sorted.size());
        // First by smallest distance
        assertSame(sC, sorted.get(0).getStation());
        // For equal distance (15.0), "Alpha" should come before "Beta"
        assertSame(sA, sorted.get(1).getStation());
        assertSame(sB, sorted.get(2).getStation());
    }

    @Test
    public void getTargetLatitudeReturnsValuePassedToConstructor() {
        ProximitySearchResult result =
                new ProximitySearchResult(41.15, -8.61, 5, null);

        assertEquals(41.15, result.getTargetLatitude(), 0.000001);
    }

    @Test
    public void getTargetLongitudeReturnsValuePassedToConstructor() {
        ProximitySearchResult result =
                new ProximitySearchResult(41.15, -8.61, 5, null);

        assertEquals(-8.61, result.getTargetLongitude(), 0.000001);
    }

    @Test
    public void getRequestedCountReturnsValuePassedToConstructor() {
        ProximitySearchResult result =
                new ProximitySearchResult(41.15, -8.61, 7, null);

        assertEquals(7, result.getRequestedCount());
    }

    @Test
    public void getFilterCanReturnNullWhenNoFilterProvided() {
        ProximitySearchResult result =
                new ProximitySearchResult(41.15, -8.61, 7, null);

        assertNull(result.getFilter());
    }

    @Test
    public void getResultsReturnsUnmodifiableList() {
        ProximitySearchResult result =
                new ProximitySearchResult(10.0, 20.0, 2, null);

        result.addResult(createStation("Porto"), 5.0);
        List<ProximitySearchResult.StationDistance> list = result.getResults();

        assertEquals(1, list.size());

        try {
            list.add(new ProximitySearchResult.StationDistance(
                    createStation("Lisboa"), 10.0));
            fail("Expected UnsupportedOperationException when modifying results list");
        } catch (UnsupportedOperationException expected) {
            // expected behaviour
        }
    }

    @Test
    public void getResultCountReflectsNumberOfAddedResults() {
        ProximitySearchResult result =
                new ProximitySearchResult(10.0, 20.0, 2, null);

        assertEquals(0, result.getResultCount());

        result.addResult(createStation("A"), 1.0);
        assertEquals(1, result.getResultCount());

        result.addResult(createStation("B"), 2.0);
        assertEquals(2, result.getResultCount());
    }

    @Test
    public void stationDistanceToStringFormatsDistanceAndName() {
        Station station = createStation("Test Station");
        ProximitySearchResult.StationDistance sd =
                new ProximitySearchResult.StationDistance(station, 12.3456);

        String str = sd.toString();
        assertEquals("12,346 km - Test Station", str);
    }

    @Test
    public void stationDistanceGettersReturnConstructorValues() {
        Station station = createStation("Another Station");
        double distance = 42.0;

        ProximitySearchResult.StationDistance sd =
                new ProximitySearchResult.StationDistance(station, distance);

        assertSame(station, sd.getStation());
        assertEquals(distance, sd.getDistanceKm(), 0.000001);
    }
}
