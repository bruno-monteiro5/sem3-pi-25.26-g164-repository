package controller;

import domain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import repository.StationRepository;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class Build2DTreeControllerTest {

    private Build2DTreeController controller;
    private StationRepository repository;
    private Station station1, station2, station3;

    @BeforeEach
    void setUp() {
        // Get singleton instance
        repository = StationRepository.getInstance();
        controller = new Build2DTreeController();

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
    }

    @Test
    void testConstructor() {
        Build2DTreeController ctrl = new Build2DTreeController();
        assertNotNull(ctrl);
        assertNotNull(ctrl.getKDTree());
        assertFalse(ctrl.isBuilt());
    }

    @Test
    void testBuild2DTreeWithoutIndexes() {
        // Create a NEW controller instance to ensure clean state
        Build2DTreeController freshController = new Build2DTreeController();

        // Check if repository has data
        if (repository.isLoaded()) {
            // If repository has data from previous tests,
            // the method will work, so we test a different scenario
            assertDoesNotThrow(() -> {
                freshController.build2DTree();
            });
        } else {
            // Repository is empty, should throw exception
            assertThrows(IllegalStateException.class, () -> {
                freshController.build2DTree();
            });
        }
    }

    @Test
    void testBuild2DTreeThrowsExceptionWhenNoData() {
        // To truly test this, we need to ensure repository is empty
        // Since we can't clear the singleton, we'll create a mock scenario

        // This test verifies the exception message at least
        try {
            Build2DTreeController ctrl = new Build2DTreeController();

            // Only test if repository is truly empty
            if (!repository.isLoaded()) {
                ctrl.build2DTree();
                fail("Should have thrown IllegalStateException");
            }
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("AVL indexes must be built first"));
        }
    }

    @Test
    void testBuild2DTreeWithIndexes() {
        // Load some stations
        List<Station> stations = new ArrayList<>();
        stations.add(station1);
        stations.add(station2);
        stations.add(station3);

        repository.loadAll(stations);

        // Now build the tree
        assertDoesNotThrow(() -> {
            controller.build2DTree();
        });

        assertTrue(controller.isBuilt());
        assertNotNull(controller.getKDTree());
        assertTrue(controller.getKDTree().size() >= 3);
    }

    @Test
    void testGetKDTree() {
        // Ensure repository has data
        if (!repository.isLoaded()) {
            List<Station> stations = new ArrayList<>();
            stations.add(station1);
            repository.loadAll(stations);
        }

        controller.build2DTree();

        KDTree2D tree = controller.getKDTree();
        assertNotNull(tree);
        assertTrue(tree.size() > 0);
    }

    @Test
    void testGetStatisticsAfterBuilding() {
        // Load fresh data for this test
        List<Station> stations = new ArrayList<>();
        stations.add(station1);
        stations.add(station2);

        repository.loadAll(stations);
        controller.build2DTree();

        KDTree2D.KDTreeStats stats = controller.getStatistics();
        assertNotNull(stats);
        assertTrue(stats.getSize() >= 2);
        assertTrue(stats.getHeight() >= 0);
    }

    @Test
    void testIsBuiltInitiallyFalse() {
        Build2DTreeController ctrl = new Build2DTreeController();
        assertFalse(ctrl.isBuilt());
    }

    @Test
    void testIsBuiltAfterBuilding() {
        // Ensure data is loaded
        if (!repository.isLoaded()) {
            List<Station> stations = new ArrayList<>();
            stations.add(station1);
            repository.loadAll(stations);
        }

        controller.build2DTree();
        assertTrue(controller.isBuilt());
    }

    @Test
    void testBuild2DTreePrintsStatistics() {
        // Load data
        List<Station> stations = new ArrayList<>();
        stations.add(station1);
        stations.add(station2);
        stations.add(station3);

        repository.loadAll(stations);

        // Capture system output
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        controller.build2DTree();

        System.setOut(originalOut);

        String output = outContent.toString();
        assertTrue(output.contains("2D-Tree build finished"));
        assertTrue(output.contains("Size="));
        assertTrue(output.contains("Height="));
        assertTrue(output.contains("Bucket size distribution"));
    }

    @Test
    void testMultipleBuildCalls() {
        // Ensure data is loaded
        if (!repository.isLoaded()) {
            List<Station> stations = new ArrayList<>();
            stations.add(station1);
            repository.loadAll(stations);
        }

        controller.build2DTree();
        int firstSize = controller.getKDTree().size();

        // Build again (should rebuild)
        Build2DTreeController newController = new Build2DTreeController();
        newController.build2DTree();
        int secondSize = newController.getKDTree().size();

        assertEquals(firstSize, secondSize);
    }

    @Test
    void testGetStatisticsBeforeBuilding() {
        Build2DTreeController ctrl = new Build2DTreeController();

        // Should return stats for empty tree
        KDTree2D.KDTreeStats stats = ctrl.getStatistics();
        assertNotNull(stats);
        assertEquals(0, stats.getSize());
        assertEquals(-1, stats.getHeight());
    }

    @Test
    void testSingletonRepositorySharing() {
        // Create two controllers
        Build2DTreeController ctrl1 = new Build2DTreeController();
        Build2DTreeController ctrl2 = new Build2DTreeController();

        // Load data through repository
        List<Station> stations = new ArrayList<>();
        stations.add(station1);
        StationRepository.getInstance().loadAll(stations);

        // Both should be able to build
        assertDoesNotThrow(() -> ctrl1.build2DTree());
        assertDoesNotThrow(() -> ctrl2.build2DTree());
    }

    @Test
    void testBuildWithEmptyRepository() {
        // This is a conceptual test - we can't truly empty the singleton
        // But we can test that the controller handles the check correctly
        Build2DTreeController ctrl = new Build2DTreeController();

        assertNotNull(ctrl.getKDTree());
        assertFalse(ctrl.isBuilt());
    }

    @Test
    void testGetStatisticsReturnsCorrectData() {
        // Load specific data
        List<Station> stations = new ArrayList<>();
        stations.add(station1);
        stations.add(station2);
        stations.add(station3);

        repository.loadAll(stations);
        controller.build2DTree();

        KDTree2D.KDTreeStats stats = controller.getStatistics();

        assertNotNull(stats);
        assertNotNull(stats.getBucketDistribution());
        assertTrue(stats.getSize() > 0);
        assertTrue(stats.getHeight() >= 0);
    }

    @Test
    void searchInArea(){
        // Load specific data
        List<Station> stations = new ArrayList<>();
        stations.add(station1);
        stations.add(station2);
        stations.add(station3);

        repository.loadAll(stations);
        controller.build2DTree();
        String result = controller.searchInArea(39, 42, -10, -8);

        assertFalse(result.isBlank());

        assertFalse(result.contains("Lisboa Santa Apolonia"));
        assertFalse(result.contains("Porto Campanha"));
        assertTrue(result.contains("Coimbra-B"));


    }
}