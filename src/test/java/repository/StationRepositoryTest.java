package repository;

import domain.Station;
import domain.StationIndexes;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link StationRepository} using pure JUnit4.
 * <p>
 * No mocking framework is used; instead, a stub implementation of
 * {@link StationIndexes} is injected via reflection, and dummy
 * {@link Station} instances are created with {@link Unsafe} to avoid
 * depending on the real {@code Location} class.
 */
public class StationRepositoryTest {

    private StationRepository repository;
    private StubStationIndexes stubIndexes;

    /**
     * Stub implementation of {@link StationIndexes} that:
     * <ul>
     *     <li>Counts how many times {@link #add(Station)} is called.</li>
     *     <li>Optionally throws a {@link RuntimeException} on add.</li>
     *     <li>Returns simple lists for the query methods.</li>
     * </ul>
     */
    private static class StubStationIndexes extends StationIndexes {
        boolean failOnAdd = false;
        int addCount = 0;

        @Override
        public void add(Station station) {
            addCount++;
            if (failOnAdd) {
                throw new RuntimeException("forced failure");
            }
        }

        @Override
        public List<Station> inLatitudeWindow(double minLatitude, double maxLatitude) {
            return Collections.emptyList();
        }

        @Override
        public List<Station> inLongitudeWindow(double minLongitude, double maxLongitude) {
            return Collections.emptyList();
        }

        @Override
        public List<Station> byTimeZoneGroup(String timeZoneGroup) {
            return Collections.emptyList();
        }

        @Override
        public List<Station> byTimeZoneAndCountry(String timeZoneGroup, String country) {
            return Collections.emptyList();
        }
    }

    /**
     * Creates a dummy {@link Station} instance without invoking its constructor,
     * using {@link Unsafe}. This avoids the need to construct a valid
     * {@code Location} object for tests.
     *
     * @return a non-null Station instance with uninitialized fields
     */
    private static Station newDummyStation() {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            Unsafe unsafe = (Unsafe) f.get(null);
            return (Station) unsafe.allocateInstance(Station.class);
        } catch (Exception e) {
            throw new RuntimeException("Unable to create dummy Station instance for tests", e);
        }
    }

    /**
     * Prepares a clean {@link StationRepository} singleton and injects
     * a {@link StubStationIndexes} instance into its private field via reflection.
     *
     * @throws Exception if reflection fails
     */
    @Before
    public void setUp() throws Exception {
        repository = StationRepository.getInstance();
        repository.clear();

        stubIndexes = new StubStationIndexes();

        Field field = StationRepository.class.getDeclaredField("stationIndexes");
        field.setAccessible(true);
        field.set(repository, stubIndexes);
    }

    /**
     * Ensures the repository is reset after each test.
     */
    @After
    public void tearDown() {
        repository.clear();
    }

    /**
     * Verifies that {@link StationRepository#getInstance()} returns a singleton
     * instance (same object on multiple calls).
     */
    @Test
    public void testGetInstanceSingleton() {
        StationRepository r1 = StationRepository.getInstance();
        StationRepository r2 = StationRepository.getInstance();

        assertSame("getInstance should return the same singleton", r1, r2);
    }

    /**
     * Ensures that {@link StationRepository#loadAll(List)} throws a
     * {@link NullPointerException} when the provided list is null.
     */
    @Test(expected = NullPointerException.class)
    public void testLoadAllNullListThrows() {
        repository.loadAll(null);
    }

    /**
     * Tests {@link StationRepository#loadAll(List)} with a list containing:
     * <ul>
     *     <li>One valid (non-null) station.</li>
     *     <li>One null station.</li>
     * </ul>
     * The non-null station should be counted as loaded; the null as rejected.
     */
    @Test
    public void testLoadAllMixedList() {
        Station s = newDummyStation();

        repository.loadAll(Arrays.asList(s, null));

        assertEquals("One station should be loaded", 1, repository.getTotalLoadedCount());
        assertEquals("One station should be rejected", 1, repository.getTotalRejectedCount());
        assertEquals("add should be called once on StationIndexes", 1, stubIndexes.addCount);
    }

    /**
     * Ensures that when {@link StationIndexes#add(Station)} throws a
     * {@link RuntimeException}, the repository counts that station as rejected.
     */
    @Test
    public void testLoadAllAddThrowsRuntimeException() {
        Station s = newDummyStation();
        stubIndexes.failOnAdd = true;

        repository.loadAll(Collections.singletonList(s));

        assertEquals("No station should be loaded", 0, repository.getTotalLoadedCount());
        assertEquals("One station should be rejected due to exception", 1, repository.getTotalRejectedCount());
    }

    /**
     * Ensures {@link StationRepository#findByLatitudeWindow(double, double)}
     * delegates to {@link StationIndexes#inLatitudeWindow(double, double)} and
     * returns a non-null list.
     */
    @Test
    public void testFindByLatitudeWindow() {
        List<Station> result = repository.findByLatitudeWindow(0.0, 10.0);
        assertNotNull("Result list should not be null", result);
    }

    /**
     * Ensures {@link StationRepository#findByLongitudeWindow(double, double)}
     * delegates to {@link StationIndexes#inLongitudeWindow(double, double)} and
     * returns a non-null list.
     */
    @Test
    public void testFindByLongitudeWindow() {
        List<Station> result = repository.findByLongitudeWindow(-5.0, 5.0);
        assertNotNull("Result list should not be null", result);
    }

    /**
     * Ensures {@link StationRepository#findByTimeZoneGroup(String)}
     * delegates to {@link StationIndexes#byTimeZoneGroup(String)} and
     * returns a non-null list.
     */
    @Test
    public void testFindByTimeZoneGroup() {
        List<Station> result = repository.findByTimeZoneGroup("Europe");
        assertNotNull("Result list should not be null", result);
    }

    /**
     * Ensures {@link StationRepository#findByTimeZoneAndCountry(String, String)}
     * delegates to {@link StationIndexes#byTimeZoneAndCountry(String, String)} and
     * returns a non-null list.
     */
    @Test
    public void testFindByTimeZoneAndCountry() {
        List<Station> result = repository.findByTimeZoneAndCountry("Europe", "PT");
        assertNotNull("Result list should not be null", result);
    }

    /**
     * Ensures {@link StationRepository#getStationIndexes()} returns the same
     * {@link StationIndexes} instance that was injected in the test setup.
     */
    @Test
    public void testGetStationIndexes() {
        StationIndexes indexes = repository.getStationIndexes();
        assertSame("getStationIndexes should return the injected stub", stubIndexes, indexes);
    }

    /**
     * Ensures {@link StationRepository#isLoaded()} reflects whether any station
     * has been successfully loaded.
     */
    @Test
    public void testIsLoaded() {
        repository.clear();
        assertFalse("Repository should not be loaded after clear", repository.isLoaded());

        repository.loadAll(Collections.singletonList(newDummyStation()));
        assertTrue("Repository should be loaded after loading a station", repository.isLoaded());
    }

    /**
     * Ensures {@link StationRepository#clear()} resets the diagnostic counters.
     */
    @Test
    public void testClearResetsCounters() {
        repository.loadAll(Collections.singletonList(newDummyStation()));
        assertTrue(repository.getTotalLoadedCount() > 0);

        repository.clear();
        assertEquals(0, repository.getTotalLoadedCount());
        assertEquals(0, repository.getTotalRejectedCount());
    }
}
