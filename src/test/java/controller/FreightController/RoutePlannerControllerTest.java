package controller.FreightController;

import domain.railway.Endpoint;
import domain.railway.Freight;
import domain.railway.RoutePlan;
import domain.railway.RouteOperation;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test suite for {@link RoutePlannerController}.
 * <p>
 * Each test method targets exactly one public member of {@code RoutePlannerController}
 * and documents the scenario, preconditions and expected outcome.
 */
public class RoutePlannerControllerTest {

    private RoutePlannerController controller;
    private Endpoint endpoint1;
    private Endpoint endpoint2;
    private Endpoint endpoint3;
    private Endpoint endpoint4;
    private LocalDate testDate;

    @Before
    public void setUp() {
        controller = new RoutePlannerController();
        endpoint1 = new Endpoint(1, "Lisbon", 100);
        endpoint2 = new Endpoint(2, "Coimbra", 200);
        endpoint3 = new Endpoint(3, "Porto", 300);
        endpoint4 = new Endpoint(4, "Braga", 400);
        testDate = LocalDate.of(2025, 1, 15);
    }


    private Freight freight(int id, Endpoint origin, Endpoint destination) {
        return new Freight(id, testDate, origin, destination);
    }

    private List<Freight> list(Freight... freights) {
        List<Freight> l = new ArrayList<Freight>();
        for (int i = 0; i < freights.length; i++) {
            l.add(freights[i]);
        }
        return l;
    }

    private void assertThrowsIAE(Runnable r) {
        try {
            r.run();
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
        }
    }


    /**
     * Tests planRoute with null freights list.
     *
     * Scenario:
     *  - Attempt to plan a route with null freights.
     *
     * Expected:
     *  - throws IllegalArgumentException.
     *
     * @see RoutePlannerController#planRoute(List)
     */
    @Test
    public void planRoute_nullFreights_throwsException() {
        assertThrowsIAE(() -> controller.planRoute(null));
    }

    /**
     * Tests planRoute with empty freights list.
     *
     * Scenario:
     *  - Attempt to plan a route with an empty list.
     *
     * Expected:
     *  - throws IllegalArgumentException.
     *
     * @see RoutePlannerController#planRoute(List)
     */
    @Test
    public void planRoute_emptyFreights_throwsException() {
        assertThrowsIAE(() -> controller.planRoute(new ArrayList<Freight>()));
    }

    /**
     * Tests planRoute with single freight (simple route).
     *
     * Scenario:
     *  - Plan a route with one freight from Lisbon to Porto.
     *
     * Expected:
     *  - route plan has 2 operations: load then unload.
     *
     * @see RoutePlannerController#planRoute(List)
     */
    @Test
    public void planRoute_singleFreight_createsTwoOperations() {
        RoutePlan plan = controller.planRoute(list(freight(1, endpoint1, endpoint3)));
        assertEquals(2, plan.getOperationCount());
    }

    /**
     * Tests planRoute with two chained freights.
     *
     * Scenario:
     *  - Plan a route with Lisbon→Coimbra and Coimbra→Porto.
     *
     * Expected:
     *  - route plan has 4 operations.
     *
     * @see RoutePlannerController#planRoute(List)
     */
    @Test
    public void planRoute_twoChainedFreights_createsFourOperations() {
        List<Freight> freights = list(
                freight(1, endpoint1, endpoint2),
                freight(2, endpoint2, endpoint3)
        );

        RoutePlan plan = controller.planRoute(freights);
        assertEquals(4, plan.getOperationCount());
    }

    /**
     * Tests planRoute with non-continuous freights.
     *
     * Scenario:
     *  - Attempt to plan with a broken chain (Lisbon→Coimbra, Porto→Braga).
     *
     * Expected:
     *  - throws IllegalArgumentException.
     *
     * @see RoutePlannerController#planRoute(List)
     */
    @Test
    public void planRoute_nonContinuousFreights_throwsException() {
        List<Freight> freights = list(
                freight(1, endpoint1, endpoint2),
                freight(2, endpoint3, endpoint4)
        );

        assertThrowsIAE(() -> controller.planRoute(freights));
    }

    /**
     * Tests validateContinuousRoute with null freights.
     *
     * Scenario:
     *  - Validate a null freights list.
     *
     * Expected:
     *  - returns false.
     *
     * @see RoutePlannerController#validateContinuousRoute(List)
     */
    @Test
    public void validateContinuousRoute_nullFreights_returnsFalse() {
        assertFalse(controller.validateContinuousRoute(null));
    }

    /**
     * Tests validateContinuousRoute with empty freights.
     *
     * Scenario:
     *  - Validate an empty freights list.
     *
     * Expected:
     *  - returns false.
     *
     * @see RoutePlannerController#validateContinuousRoute(List)
     */
    @Test
    public void validateContinuousRoute_emptyFreights_returnsFalse() {
        assertFalse(controller.validateContinuousRoute(new ArrayList<Freight>()));
    }

    /**
     * Tests validateContinuousRoute with a valid chain.
     *
     * Scenario:
     *  - Validate Lisbon→Coimbra and Coimbra→Porto.
     *
     * Expected:
     *  - returns true.
     *
     * @see RoutePlannerController#validateContinuousRoute(List)
     */
    @Test
    public void validateContinuousRoute_validChain_returnsTrue() {
        List<Freight> freights = list(
                freight(1, endpoint1, endpoint2),
                freight(2, endpoint2, endpoint3)
        );

        assertTrue(controller.validateContinuousRoute(freights));
    }

    /**
     * Tests validateContinuousRoute with a broken chain.
     *
     * Scenario:
     *  - Validate Lisbon→Coimbra and Porto→Braga.
     *
     * Expected:
     *  - returns false.
     *
     * @see RoutePlannerController#validateContinuousRoute(List)
     */
    @Test
    public void validateContinuousRoute_brokenChain_returnsFalse() {
        List<Freight> freights = list(
                freight(1, endpoint1, endpoint2),
                freight(2, endpoint3, endpoint4)
        );

        assertFalse(controller.validateContinuousRoute(freights));
    }

    /**
     * Tests countStationStops with null plan.
     *
     * Scenario:
     *  - Count stations in a null plan.
     *
     * Expected:
     *  - returns 0.
     *
     * @see RoutePlannerController#countStationStops(RoutePlan)
     */
    @Test
    public void countStationStops_nullPlan_returnsZero() {
        assertEquals(0, controller.countStationStops(null));
    }

    /**
     * Tests countStationStops with empty plan.
     *
     * Scenario:
     *  - Count stations in an empty route plan.
     *
     * Expected:
     *  - returns 0.
     *
     * @see RoutePlannerController#countStationStops(RoutePlan)
     */
    @Test
    public void countStationStops_emptyPlan_returnsZero() {
        assertEquals(0, controller.countStationStops(new RoutePlan()));
    }

    /**
     * Tests countStationStops with composite plan.
     *
     * Scenario:
     *  - Plan Lisbon→Coimbra and Coimbra→Porto and count station stops.
     *
     * Expected:
     *  - returns 3 unique stations.
     *
     * @see RoutePlannerController#countStationStops(RoutePlan)
     */
    @Test
    public void countStationStops_compositePlan_returnsThree() {
        List<Freight> freights = list(
                freight(1, endpoint1, endpoint2),
                freight(2, endpoint2, endpoint3)
        );

        RoutePlan plan = controller.planRoute(freights);
        assertEquals(3, controller.countStationStops(plan));
    }


}
