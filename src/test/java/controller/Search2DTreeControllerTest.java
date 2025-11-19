package controller;

import domain.KDTree2D;
import domain.Location;
import domain.Station;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import repository.KDTreeRepository;
import repository.StationRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class Search2DTreeControllerTest {

    private Search2DTreeController controller;
    private StationRepository repository;
    private Station station1, station2, station3, station4;
    private Build2DTreeController buildController;

    @BeforeEach
    void setUp() {
        // Get singleton instance
        repository = StationRepository.getInstance();
        controller = new Search2DTreeController();
        buildController = new Build2DTreeController();

        station1 = new Station(
                "Lisboa Santa Apolonia",
                "PT",
                new Location(38.71387, -9.122271),
                "WET",
                true, true, false
        );

        station2 = new Station(
                "Porto Campanha",
                "PT",
                new Location(41.14961, -8.58519),
                "WET",
                true, true, false
        );

        station3 = new Station(
                "Coimbra-B",
                "PT",
                new Location(40.20564, -8.41955),
                "WET",
                true, true, false
        );

        station4 = new Station(
                "Celorico de bastos",
                "PT",
                new Location(41, -10),
                "WET",
                false, true, false
        );
    }

    @Test
    void testSearchInArea() {
        // Load specific data
        List<Station> stations = new ArrayList<>();
        stations.add(station1);
        stations.add(station2);
        stations.add(station3);
        stations.add(station4);

        repository.loadAll(stations);
        buildController.build2DTree();
        String result = controller.searchInArea(39, 42, -11, -8);

        assertFalse(result.isBlank());

        assertFalse(result.contains("Lisboa Santa Apolonia"));
        assertFalse(result.contains("Porto Campanha"));
        assertTrue(result.contains("Coimbra-B"));
    }

    @Test
    void testSearchInArea1() {
        // Load specific data
        List<Station> stations = new ArrayList<>();
        stations.add(station1);
        stations.add(station2);
        stations.add(station3);
        stations.add(station4);

        KDTreeRepository repo = KDTreeRepository.getInstance();

        repository.loadAll(stations);
        buildController.build2DTree();
        String result = controller.searchInArea(-89, -88, -10, -8, repo.getKDTree().getRoot(), "");

        assertTrue(result.isBlank());
    }

    @Test
    void testSearchInArea2() {
        // Load specific data
        List<Station> stations = new ArrayList<>();
        stations.add(station1);
        stations.add(station2);
        stations.add(station3);
        stations.add(station4);

        repository.loadAll(stations);
        buildController.build2DTree();
        String result = controller.searchInArea(39, 42, -10, -8, buildController.getKDTree().getRoot(), "");

        assertFalse(result.isBlank());

    }

    @Test
    void testSearchInArea3() {
        // Load specific data
        List<Station> stations = new ArrayList<>();
        stations.add(station1);
        stations.add(station2);
        stations.add(station3);
        stations.add(station4);

        repository.loadAll(stations);
        buildController.build2DTree();
        String result = controller.searchInArea(39, 42, -11, -8, 1, 0, "pt");

        assertFalse(result.isBlank());

        assertFalse(result.contains("Lisboa Santa Apolonia"));
        assertFalse(result.contains("Porto Campanha"));
        assertFalse(result.contains("Coimbra-B"));
        assertTrue(result.contains("Celorico de bastos"));
    }
}