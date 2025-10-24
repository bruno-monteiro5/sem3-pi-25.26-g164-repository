package controller;

import domain.*;
import controller.PickPathController.PickPathResult;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for {@link PickPathController}.
 * <p>
 * Each test method targets exactly one public member of {@code PickPathController}
 * and documents the scenario, preconditions and expected outcome.
 */
class PickPathControllerTest {

    /**
     * Verifies distance computation with the warehouse metric D.
     *
     * Expected:
     *  - If same aisle, returns absolute bay difference;
     *  - Else, returns descent + horizontal + ascent as per formula.
     *
     * @see PickPathController#distance(int, int, int, int)
     */
    @Test
    void distance_computesCorrectly() {
        PickPathController ctrl = new PickPathController();
        assertEquals(5, ctrl.distance(2, 7, 2, 2));
        assertEquals(13, ctrl.distance(1, 8, 2, 2));
        assertEquals(9, ctrl.distance(2, 2, 3, 4));
        assertEquals(11, ctrl.distance(0, 0, 1, 8));
        assertEquals(0, ctrl.distance(2, 4, 2, 4));
    }

    /**
     * Verifies Strategy A produces ordered path and total matching the spec.
     *
     * Expected:
     *  - Sorted by aisle, then bay;
     *  - Total distance is correctly accumulated.
     *
     * @see PickPathController#sequenceStrategyA(List)
     */
    @Test
    void sequenceStrategyA_matchesExpectedPathAndTotal() {
        PickPathController ctrl = new PickPathController();
        List<PickStop> input = new ArrayList<>();
        input.add(new PickStop(1, 8, 1));
        input.add(new PickStop(2, 2, 1));
        input.add(new PickStop(3, 4, 1));

        PickPathResult result = ctrl.sequenceStrategyA(input);

        assertEquals(33, result.getTotalDistance());
        List<PickStop> path = result.getPath();
        assertEquals(3, path.size());
        assertEquals(1, path.get(0).getAisle());
        assertEquals(2, path.get(1).getAisle());
        assertEquals(3, path.get(2).getAisle());
    }

    /**
     * Verifies Strategy A uses bay as tiebreaker when aisle is equal.
     *
     * Scenario:
     *  - Two stops with same aisle but different bay.
     *
     * Expected:
     *  - Stop with smaller bay comes first.
     *
     * @see PickPathController#sequenceStrategyA(List)
     */
    @Test
    void sequenceStrategyA_ordersByBayIfAisleEqual() {
        PickPathController ctrl = new PickPathController();
        List<PickStop> input = List.of(
                new PickStop(2, 9, 1),
                new PickStop(2, 3, 1),
                new PickStop(1, 4, 1)
        );

        PickPathResult result = ctrl.sequenceStrategyA(input);
        List<PickStop> path = result.getPath();

        assertEquals(1, path.get(0).getAisle());
        assertEquals(2, path.get(1).getAisle());
        assertEquals(3, path.get(1).getBay());
        assertEquals(2, path.get(2).getAisle());
        assertEquals(9, path.get(2).getBay());
    }

    /**
     * Verifies Strategy A with empty list yields 0 distance.
     *
     * @see PickPathController#sequenceStrategyA(List)
     */
    @Test
    void sequenceStrategyA_emptyListReturnsZero() {
        PickPathController ctrl = new PickPathController();
        PickPathResult result = ctrl.sequenceStrategyA(new ArrayList<>());
        assertEquals(0, result.getTotalDistance());
        assertTrue(result.getPath().isEmpty());
    }

    /**
     * Verifies Strategy B picks stops by nearest distance from (0,0).
     *
     * Expected:
     *  - Path is built greedy from closest to furthest.
     *  - Total distance matches example.
     *
     * @see PickPathController#sequenceStrategyB(List)
     */
    @Test
    void sequenceStrategyB_matchesExpectedPathAndTotal() {
        PickPathController ctrl = new PickPathController();
        List<PickStop> input = new ArrayList<>();
        input.add(new PickStop(1, 8, 1));
        input.add(new PickStop(2, 2, 1));
        input.add(new PickStop(3, 4, 1));

        PickPathResult result = ctrl.sequenceStrategyB(input);

        assertEquals(35, result.getTotalDistance());
        List<PickStop> path = result.getPath();
        assertEquals(3, path.size());
        assertEquals(2, path.get(0).getAisle());
        assertEquals(3, path.get(1).getAisle());
        assertEquals(1, path.get(2).getAisle());
    }

    /**
     * Verifies Strategy B with one stop computes correct distance.
     *
     * @see PickPathController#sequenceStrategyB(List)
     */
    @Test
    void sequenceStrategyB_singleStop() {
        PickPathController ctrl = new PickPathController();
        List<PickStop> input = List.of(new PickStop(2, 3, 1));
        PickPathResult result = ctrl.sequenceStrategyB(input);
        assertEquals(ctrl.distance(0, 0, 2, 3), result.getTotalDistance());
        assertEquals(1, result.getPath().size());
    }

    /**
     * Verifies Strategy B handles empty input.
     *
     * @see PickPathController#sequenceStrategyB(List)
     */
    @Test
    void sequenceStrategyB_emptyListReturnsZero() {
        PickPathController ctrl = new PickPathController();
        PickPathResult result = ctrl.sequenceStrategyB(new ArrayList<>());
        assertEquals(0, result.getTotalDistance());
        assertTrue(result.getPath().isEmpty());
    }

    /**
     * Verifies Strategy B tie-breaking logic for aisle and bay.
     *
     * @see PickPathController#sequenceStrategyB(List)
     */
    @Test
    void sequenceStrategyB_fullTieBreakerCoverage() {
        PickPathController ctrl = new PickPathController();
        List<PickStop> input = new ArrayList<>();
        input.add(new PickStop(2, 5, 1));
        input.add(new PickStop(2, 6, 1));
        input.add(new PickStop(3, 2, 1));

        PickPathResult result = ctrl.sequenceStrategyB(input);

        PickStop first = result.getPath().get(0);
        assertEquals(2, first.getAisle());
        assertEquals(5, first.getBay());
    }

    /**
     * Verifies merging allocations with same bay.
     *
     * @see PickPathController#mergeStops(List)
     */
    @Test
    void mergeStops_aggregatesByAisleBay() {
        PickPathController ctrl = new PickPathController();
        List<Allocation> allocations = new ArrayList<>();

        Items item = new Items("SKU1", "Test Product", "TestCat", "kg", 1, 2);
        Order order = new Order("ORD01", 1, "2025-10-31");

        Aisle aisle1 = new Aisle(1);
        Bay bay8 = new Bay(8, 100);
        Box box1 = new Box(item, 2);
        OrderLine line1 = new OrderLine(1, "SKU1", 2);
        OrderLine line2 = new OrderLine(2, "SKU1", 3);

        allocations.add(new Allocation(order, line1, "SKU1", box1, aisle1, bay8, 2.0, 1.0));
        allocations.add(new Allocation(order, line2, "SKU1", box1, aisle1, bay8, 3.0, 1.0));

        Aisle aisle2 = new Aisle(2);
        Bay bay2 = new Bay(2, 100);
        Box box2 = new Box(item, 1);
        OrderLine line3 = new OrderLine(3, "SKU1", 1);

        allocations.add(new Allocation(order, line3, "SKU1", box2, aisle2, bay2, 1.0, 1.0));

        List<PickStop> merged = ctrl.mergeStops(allocations);

        assertEquals(2, merged.size());
        for (PickStop s : merged) {
            if (s.getAisle() == 1 && s.getBay() == 8) {
                assertEquals(5.0, s.getTotalQty());
            } else if (s.getAisle() == 2 && s.getBay() == 2) {
                assertEquals(1.0, s.getTotalQty());
            } else {
                fail("Unexpected pick stop location");
            }
        }
    }

    /**
     * Verifies mergeStops creates stop when no duplicates exist.
     *
     * @see PickPathController#mergeStops(List)
     */
    @Test
    void mergeStops_createsSingleStopIfNoDuplicates() {
        PickPathController ctrl = new PickPathController();
        List<Allocation> allocations = new ArrayList<>();

        Items item = new Items("SKU9", "Test", "Cat", "unit", 1, 1);
        Order order = new Order("TEST", 1, "2025-12-31");
        Aisle aisle = new Aisle(1);
        Bay bay = new Bay(5, 100);
        Box box = new Box(item, 1);
        OrderLine line = new OrderLine(1, "SKU9", 1);

        allocations.add(new Allocation(order, line, "SKU9", box, aisle, bay, 1.0, 1.0));

        List<PickStop> result = ctrl.mergeStops(allocations);
        assertEquals(1, result.size());
        PickStop stop = result.get(0);
        assertEquals(1, stop.getAisle());
        assertEquals(5, stop.getBay());
        assertEquals(1.0, stop.getTotalQty());
    }
}
