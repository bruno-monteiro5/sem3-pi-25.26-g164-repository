package controller;

import domain.Location;
import domain.Station;
import org.junit.Before;
import org.junit.Test;
import repository.StationRepository;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class BuildIndexesControllerTest {

    private StationRepository repository;

    @Before
    public void setUp() {
        repository = resetRepositorySingleton();
    }

    @Test
    public void buildFromCsvPath() {
        BuildIndexesController controller = new BuildIndexesController();

        final String[] pathHolder = new String[1];

        StationCsvLoader okLoader = new StationCsvLoader() {
            @Override
            public List<Station> load(String path) {
                pathHolder[0] = path;
                return Collections.singletonList(
                        new Station("A", "PT", new Location(1, 2),
                                "EU", true, false, false));
            }
        };

        controller.buildFromCsvPath(okLoader, "ok.csv");

        assertEquals("ok.csv", pathHolder[0]);
        assertEquals(1, repository.getTotalLoadedCount());
        assertEquals(0, repository.getTotalRejectedCount());

        StationCsvLoader errorLoader = new StationCsvLoader() {
            @Override
            public List<Station> load(String path) throws IOException {
                throw new IOException("Simulated I/O error");
            }
        };

        controller.buildFromCsvPath(errorLoader, "broken.csv");

        assertEquals(0, repository.getTotalLoadedCount());
        assertEquals(0, repository.getTotalRejectedCount());
    }


    @Test
    public void queryByLatitude() {
        BuildIndexesController ctrl = new BuildIndexesController();

        Station low = station("Low", 0, 0, "TZ", "PT");
        Station inside = station("Inside", 10, 5, "TZ", "PT");
        Station high = station("High", 25, 10, "TZ", "PT");

        loadStations(low, inside, high);

        List<Station> result = ctrl.queryByLatitude(5, 20);

        assertEquals(Collections.singletonList(inside), result);
    }

    @Test
    public void queryByLongitude() {
        BuildIndexesController ctrl = new BuildIndexesController();

        Station left = station("Left", 0, -10, "TZ", "PT");
        Station inside = station("Inside", 0, 0, "TZ", "PT");
        Station right = station("Right", 0, 30, "TZ", "PT");

        loadStations(left, inside, right);

        List<Station> result = ctrl.queryByLongitude(-5, 5);

        assertEquals(Collections.singletonList(inside), result);
    }

    @Test
    public void queryByTimeZoneGroup() {
        BuildIndexesController ctrl = new BuildIndexesController();

        Station lisbon = station("Lisbon", 0, 0, "Europe/Lisbon", "PT");
        Station porto = station("Porto", 0, 0, "Europe/Lisbon", "PT");
        Station madrid = station("Madrid", 0, 0, "Europe/Madrid", "ES");

        loadStations(lisbon, porto, madrid);

        List<Station> result = ctrl.queryByTimeZoneGroup("Europe/Lisbon");

        assertEquals(Arrays.asList(lisbon, porto), result);
    }

    @Test
    public void queryByTimeZoneAndCountry() {
        BuildIndexesController ctrl = new BuildIndexesController();

        Station madridA = station("Madrid Atocha", 0, 0, "Europe/Madrid", "ES");
        Station madridB = station("Madrid Chamartin", 0, 0, "Europe/Madrid", "ES");
        Station lisbon = station("Lisbon", 0, 0, "Europe/Lisbon", "PT");

        loadStations(madridA, madridB, lisbon);

        List<Station> result = ctrl.queryByTimeZoneAndCountry("Europe/Madrid", "ES");

        assertEquals(Arrays.asList(madridA, madridB), result);
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

    private void loadStations(Station... stations) {
        repository.loadAll(Arrays.asList(stations));
    }

    private Station station(String name, double latitude, double longitude, String tzGroup, String country) {
        return new Station(name, country, new Location(latitude, longitude), tzGroup, false, false, false);
    }
}
