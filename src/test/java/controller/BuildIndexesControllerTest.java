package controller;

import domain.Location;
import domain.Station;
import org.junit.Test;
import repository.StationRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class BuildIndexesControllerTest {

    @Test
    public void buildFromCsvPath() {
        final List<Station>[] loadedHolder = new List[1];
        final int[] loadedCountHolder = new int[1];
        final int[] rejectedCountHolder = new int[1];

        StationRepository repo = new StationRepository() {
            @Override
            public void loadAll(List<Station> stations) {
                loadedHolder[0] = stations;
                loadedCountHolder[0] = (stations == null) ? 0 : stations.size();
                rejectedCountHolder[0] = 0;
            }

            @Override public int getTotalLoadedCount() { return loadedCountHolder[0]; }
            @Override public int getTotalRejectedCount() { return rejectedCountHolder[0]; }

            @Override public List<Station> findByLatitudeWindow(double min, double max) { return null; }
            @Override public List<Station> findByLongitudeWindow(double min, double max) { return null; }
            @Override public List<Station> findByTimeZoneGroup(String tz) { return null; }
            @Override public List<Station> findByTimeZoneAndCountry(String tz, String c) { return null; }
        };

        BuildIndexesController controller = new BuildIndexesController(repo);

        final String[] pathHolder = new String[1];
        final List<Station>[] stationsHolder = new List[1];

        StationCsvLoader okLoader = new StationCsvLoader() {
            @Override
            public List<Station> load(String path) {
                pathHolder[0] = path;
                return stationsHolder[0];
            }
        };

        Station st = new Station("A", "PT", new Location(1, 2),
                "EU", true, false, false);
        stationsHolder[0] = Collections.singletonList(st);

        controller.buildFromCsvPath(okLoader, "ok.csv");

        assertEquals("ok.csv", pathHolder[0]);
        assertNotNull(loadedHolder[0]);
        assertEquals(1, loadedHolder[0].size());
        assertEquals(1, repo.getTotalLoadedCount());
        assertEquals(0, repo.getTotalRejectedCount());

        StationCsvLoader errorLoader = new StationCsvLoader() {
            @Override
            public List<Station> load(String path) throws IOException {
                throw new IOException("Simulated I/O error");
            }
        };

        controller.buildFromCsvPath(errorLoader, "broken.csv");

        assertNotNull(loadedHolder[0]);
        assertTrue(loadedHolder[0].isEmpty());
        assertEquals(0, repo.getTotalLoadedCount());
        assertEquals(0, repo.getTotalRejectedCount());
    }


    @Test
    public void queryByLatitude() {
        final double[] min = new double[1];
        final double[] max = new double[1];

        List<Station> expected = new ArrayList<>();
        expected.add(new Station("S", "PT", new Location(10,10), "EU", false,false,false));

        StationRepository repo = new StationRepository() {
            @Override public void loadAll(List<Station> s) {}
            @Override public int getTotalLoadedCount() { return 0; }
            @Override public int getTotalRejectedCount() { return 0; }

            @Override
            public List<Station> findByLatitudeWindow(double a, double b) {
                min[0] = a;
                max[0] = b;
                return expected;
            }
            @Override public List<Station> findByLongitudeWindow(double a, double b) { return null; }
            @Override public List<Station> findByTimeZoneGroup(String s) { return null; }
            @Override public List<Station> findByTimeZoneAndCountry(String a, String b) { return null; }
        };

        BuildIndexesController ctrl = new BuildIndexesController(repo);

        List<Station> result = ctrl.queryByLatitude(5, 20);

        assertSame(expected, result);
        assertEquals(5, min[0], 0.001);
        assertEquals(20, max[0], 0.001);
    }

    @Test
    public void queryByLongitude() {
        final double[] min = new double[1];
        final double[] max = new double[1];

        List<Station> expected = new ArrayList<>();
        expected.add(new Station("S", "PT", new Location(10,10), "EU", false,false,false));

        StationRepository repo = new StationRepository() {
            @Override public void loadAll(List<Station> s) {}
            @Override public int getTotalLoadedCount() { return 0; }
            @Override public int getTotalRejectedCount() { return 0; }

            @Override public List<Station> findByLatitudeWindow(double a, double b) { return null; }

            @Override
            public List<Station> findByLongitudeWindow(double a, double b) {
                min[0] = a;
                max[0] = b;
                return expected;
            }

            @Override public List<Station> findByTimeZoneGroup(String s) { return null; }
            @Override public List<Station> findByTimeZoneAndCountry(String a, String b) { return null; }
        };

        BuildIndexesController ctrl = new BuildIndexesController(repo);

        List<Station> result = ctrl.queryByLongitude(-5, 5);

        assertSame(expected, result);
        assertEquals(-5, min[0], 0.001);
        assertEquals(5, max[0], 0.001);
    }

    @Test
    public void queryByTimeZoneGroup() {
        final String[] group = new String[1];

        List<Station> expected = new ArrayList<>();
        expected.add(new Station("A","PT",new Location(0,0),"EU",false,false,false));

        StationRepository repo = new StationRepository() {
            @Override public void loadAll(List<Station> s) {}
            @Override public int getTotalLoadedCount() { return 0; }
            @Override public int getTotalRejectedCount() { return 0; }

            @Override public List<Station> findByLatitudeWindow(double a, double b) { return null; }
            @Override public List<Station> findByLongitudeWindow(double a, double b) { return null; }

            @Override
            public List<Station> findByTimeZoneGroup(String tz) {
                group[0] = tz;
                return expected;
            }

            @Override public List<Station> findByTimeZoneAndCountry(String a, String b) { return null; }
        };

        BuildIndexesController ctrl = new BuildIndexesController(repo);

        List<Station> result = ctrl.queryByTimeZoneGroup("Europe/Lisbon");

        assertSame(expected, result);
        assertEquals("Europe/Lisbon", group[0]);
    }

    @Test
    public void queryByTimeZoneAndCountry() {
        final String[] tzHolder = new String[1];
        final String[] cHolder = new String[1];

        List<Station> expected = new ArrayList<>();
        expected.add(new Station("A","PT",new Location(0,0),"EU",false,false,false));

        StationRepository repo = new StationRepository() {
            @Override public void loadAll(List<Station> s) {}
            @Override public int getTotalLoadedCount() { return 0; }
            @Override public int getTotalRejectedCount() { return 0; }

            @Override public List<Station> findByLatitudeWindow(double a, double b) { return null; }
            @Override public List<Station> findByLongitudeWindow(double a, double b) { return null; }
            @Override public List<Station> findByTimeZoneGroup(String s) { return null; }

            @Override
            public List<Station> findByTimeZoneAndCountry(String tz, String country) {
                tzHolder[0] = tz;
                cHolder[0] = country;
                return expected;
            }
        };

        BuildIndexesController ctrl = new BuildIndexesController(repo);

        List<Station> result = ctrl.queryByTimeZoneAndCountry("Europe/Madrid", "ES");

        assertSame(expected, result);
        assertEquals("Europe/Madrid", tzHolder[0]);
        assertEquals("ES", cHolder[0]);
    }
}
