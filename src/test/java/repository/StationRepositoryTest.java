package repository;

import domain.Location;
import domain.Station;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for the {@link StationRepository} class.
 * <p>
 * These tests cover:
 * <ul>
 *     <li>Loading valid and invalid stations</li>
 *     <li>Correct updating of load/reject counters</li>
 *     <li>Delegation of query methods to the underlying {@link domain.StationIndexes}</li>
 *     <li>Validation of exceptional cases</li>
 * </ul>
 * <p>
 * Both normal and exceptional flows are exercised to ensure full coverage.
 */
public class StationRepositoryTest {

    private StationRepository repository;

    @Before
    public void setUp() {
        repository = resetRepositorySingleton();
    }

    /**
     * Tests that loadAll correctly loads valid stations,
     * rejects null stations, and handles exceptions thrown
     * by StationIndexes.add.
     */
    @Test
    public void loadAll() {
        List<Station> list = new ArrayList<>();

        Station valid1 = new Station(
                "A",
                "PT",
                new Location(10, 20),
                "Europe/Lisbon",
                false,
                false,
                false
        );

        Station valid2 = new Station(
                "B",
                "PT",
                new Location(15, 25),
                "Europe/Lisbon",
                true,
                false,
                false
        );

        list.add(valid1);
        list.add(null);
        list.add(valid2);

        repository.loadAll(list);

        assertEquals(2, repository.getTotalLoadedCount());
        assertEquals(1, repository.getTotalRejectedCount());

        try {
            repository.loadAll(null);
            fail("Expected NullPointerException for null station list");
        } catch (NullPointerException expected) {
        }
    }

    /**
     * Tests that the getter returns the current number
     * of successfully loaded stations.
     */
    @Test
    public void getTotalLoadedCount() {
        List<Station> stations = new ArrayList<>();

        stations.add(new Station("X", "PT", new Location(1, 1), "EU", false, false, false));
        repository.loadAll(stations);

        assertEquals(1, repository.getTotalLoadedCount());
    }

    /**
     * Tests that the getter returns the current number
     * of rejected stations.
     */
    @Test
    public void getTotalRejectedCount() {
        List<Station> stations = new ArrayList<>();

        stations.add(null);
        stations.add(null);

        repository.loadAll(stations);

        assertEquals(0, repository.getTotalLoadedCount());
        assertEquals(2, repository.getTotalRejectedCount());
    }

    /**
     * Tests that latitude window queries delegate correctly
     * to {@link domain.StationIndexes}.
     */
    @Test
    public void findByLatitudeWindow() {
        Station s1 = new Station(
                "A", "PT",
                new Location(10, 0),
                "EU", false, false, false
        );
        Station s2 = new Station(
                "B", "PT",
                new Location(20, 0),
                "EU", false, false, false
        );
        Station s3 = new Station(
                "C", "PT",
                new Location(30, 0),
                "EU", false, false, false
        );

        List<Station> stations = new ArrayList<>();
        stations.add(s1);
        stations.add(s2);
        stations.add(s3);

        repository.loadAll(stations);

        List<Station> result = repository.findByLatitudeWindow(15, 25);

        assertEquals(1, result.size());
        assertSame(s2, result.get(0));
    }

    /**
     * Tests that longitude window queries delegate correctly
     * to {@link domain.StationIndexes}.
     */
    @Test
    public void findByLongitudeWindow() {
        Station s1 = new Station(
                "A", "PT",
                new Location(0, -10),
                "EU", false, false, false
        );
        Station s2 = new Station(
                "B", "PT",
                new Location(0, 0),
                "EU", false, false, false
        );
        Station s3 = new Station(
                "C", "PT",
                new Location(0, 10),
                "EU", false, false, false
        );

        List<Station> stations = new ArrayList<>();
        stations.add(s1);
        stations.add(s2);
        stations.add(s3);

        repository.loadAll(stations);

        List<Station> result = repository.findByLongitudeWindow(-5, 5);

        assertEquals(1, result.size());
        assertSame(s2, result.get(0));
    }

    /**
     * Tests querying by time zone group.
     */
    @Test
    public void findByTimeZoneGroup() {
        Station s1 = new Station(
                "A", "PT",
                new Location(0, 0),
                "Europe/Lisbon",
                false, false, false
        );
        Station s2 = new Station(
                "B", "ES",
                new Location(0, 0),
                "Europe/Lisbon",
                false, false, false
        );
        Station s3 = new Station(
                "C", "PT",
                new Location(0, 0),
                "Europe/Madrid",
                false, false, false
        );

        List<Station> list = new ArrayList<>();
        list.add(s1);
        list.add(s2);
        list.add(s3);

        repository.loadAll(list);

        List<Station> result = repository.findByTimeZoneGroup("Europe/Lisbon");

        assertEquals(2, result.size());
        assertTrue(result.contains(s1));
        assertTrue(result.contains(s2));
        assertFalse(result.contains(s3));
    }

    /**
     * Tests querying by time zone group and country.
     */
    @Test
    public void findByTimeZoneAndCountry() {
        Station s1 = new Station(
                "A", "PT",
                new Location(0, 0),
                "Europe/Lisbon",
                false, false, false
        );
        Station s2 = new Station(
                "B", "PT",
                new Location(0, 0),
                "Europe/Lisbon",
                false, false, false
        );
        Station s3 = new Station(
                "C", "ES",
                new Location(0, 0),
                "Europe/Lisbon",
                false, false, false
        );
        Station s4 = new Station(
                "D", "PT",
                new Location(0, 0),
                "Europe/Madrid",
                false, false, false
        );

        List<Station> list = new ArrayList<>();
        list.add(s1);
        list.add(s2);
        list.add(s3);
        list.add(s4);

        repository.loadAll(list);

        List<Station> result = repository.findByTimeZoneAndCountry("Europe/Lisbon", "PT");

        assertEquals(2, result.size());
        assertTrue(result.contains(s1));
        assertTrue(result.contains(s2));
        assertFalse(result.contains(s3));
        assertFalse(result.contains(s4));
    }
    private StationRepository resetRepositorySingleton() {
        try {
            Field instanceField = StationRepository.class.getDeclaredField("instance");
            instanceField.setAccessible(true);
            instanceField.set(null, null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to reset StationRepository singleton", e);
        }
        return StationRepository.getInstance();
    }
}
