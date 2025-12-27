package domain.railway;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Test suite for {@link RoutePlan}.
 * <p>
 * Each test method targets exactly one public member of {@code RoutePlan}
 * and documents the scenario, preconditions and expected outcome.
 */
public class RoutePlanTest {

    private Endpoint endpoint1;
    private Endpoint endpoint2;
    private Endpoint endpoint3;
    private Freight freight1;
    private Freight freight2;

    @Before
    public void setUp() {
        endpoint1 = new Endpoint(1, "Lisbon", 100);
        endpoint2 = new Endpoint(2, "Coimbra", 200);
        endpoint3 = new Endpoint(3, "Porto", 300);

        freight1 = new Freight(1, java.time.LocalDate.now(), endpoint1, endpoint2);
        freight2 = new Freight(2, java.time.LocalDate.now(), endpoint2, endpoint3);
    }

    /**
     * Verifies that the constructor initializes an empty operation list.
     *
     * Preconditions:
     *  - none.
     *
     * Expected:
     *  - {@link RoutePlan#isEmpty()} returns true;
     *  - {@link RoutePlan#getOperationCount()} returns 0.
     *
     * @see RoutePlan#RoutePlan()
     */
    @Test
    public void constructor_initializesEmptyPlan() {
        RoutePlan plan = new RoutePlan();
        assertTrue(plan.isEmpty());
        assertEquals(0, plan.getOperationCount());
    }

    /**
     * Tests addOperation with null input.
     *
     * Scenario:
     *  - Attempt to add a null operation.
     *
     * Expected:
     *  - throws IllegalArgumentException.
     *
     * @see RoutePlan#addOperation(RouteOperation)
     */
    @Test(expected = IllegalArgumentException.class)
    public void addOperation_nullOperation_throwsException() {
        RoutePlan plan = new RoutePlan();
        plan.addOperation(null);
    }

    /**
     * Tests addOperation and list immutability.
     *
     * Scenario:
     *  - Add one operation and retrieve operations list.
     *  - Attempt to modify the returned list.
     *
     * Expected:
     *  - plan is not empty;
     *  - operation count is 1;
     *  - returned list is unmodifiable.
     *
     * @see RoutePlan#addOperation(RouteOperation)
     * @see RoutePlan#getOperations()
     */
    @Test(expected = UnsupportedOperationException.class)
    public void getOperations_returnsUnmodifiableList() {
        RoutePlan plan = new RoutePlan();
        plan.addOperation(new RouteOperation(endpoint1, freight1, RouteOperation.Type.LOAD));

        assertFalse(plan.isEmpty());
        assertEquals(1, plan.getOperationCount());

        List<RouteOperation> ops = plan.getOperations();
        ops.add(new RouteOperation(endpoint2, freight1, RouteOperation.Type.UNLOAD));
    }

    /**
     * Tests that getFirstStation and getLastStation return null on empty plan.
     *
     * Scenario:
     *  - Create an empty route plan and query first/last station.
     *
     * Expected:
     *  - both return null.
     *
     * @see RoutePlan#getFirstStation()
     * @see RoutePlan#getLastStation()
     */
    @Test
    public void getFirstAndLastStation_emptyPlan_returnsNull() {
        RoutePlan plan = new RoutePlan();
        assertNull(plan.getFirstStation());
        assertNull(plan.getLastStation());
    }

    /**
     * Tests getFirstStation and getLastStation with multiple operations.
     *
     * Scenario:
     *  - Add operations spanning Lisbon → Coimbra → Porto.
     *
     * Expected:
     *  - first station is Lisbon;
     *  - last station is Porto.
     *
     * @see RoutePlan#getFirstStation()
     * @see RoutePlan#getLastStation()
     */
    @Test
    public void getFirstAndLastStation_withOperations_returnsCorrectStations() {
        RoutePlan plan = new RoutePlan();

        plan.addOperation(new RouteOperation(endpoint1, freight1, RouteOperation.Type.LOAD));
        plan.addOperation(new RouteOperation(endpoint2, freight1, RouteOperation.Type.UNLOAD));
        plan.addOperation(new RouteOperation(endpoint2, freight2, RouteOperation.Type.LOAD));
        plan.addOperation(new RouteOperation(endpoint3, freight2, RouteOperation.Type.UNLOAD));

        assertEquals(endpoint1, plan.getFirstStation());
        assertEquals(endpoint3, plan.getLastStation());
    }

    /**
     * Tests toString for empty plan.
     *
     * Scenario:
     *  - Create empty route plan and call toString.
     *
     * Expected:
     *  - returns "RoutePlan: empty".
     *
     * @see RoutePlan#toString()
     */
    @Test
    public void toString_emptyPlan_returnsCorrectFormat() {
        RoutePlan plan = new RoutePlan();
        assertEquals("RoutePlan: empty", plan.toString());
    }

    /**
     * Tests toString for non-empty plan.
     *
     * Scenario:
     *  - Add operations spanning Lisbon → Porto and call toString.
     *
     * Expected:
     *  - includes the number of operations;
     *  - includes the first and last station names.
     *
     * @see RoutePlan#toString()
     */
    @Test
    public void toString_withOperations_includesCountAndEndpoints() {
        RoutePlan plan = new RoutePlan();

        plan.addOperation(new RouteOperation(endpoint1, freight1, RouteOperation.Type.LOAD));
        plan.addOperation(new RouteOperation(endpoint2, freight1, RouteOperation.Type.UNLOAD));
        plan.addOperation(new RouteOperation(endpoint2, freight2, RouteOperation.Type.LOAD));
        plan.addOperation(new RouteOperation(endpoint3, freight2, RouteOperation.Type.UNLOAD));

        String result = plan.toString();

        assertTrue(result.contains("4 operations"));
        assertTrue(result.contains("Lisbon"));
        assertTrue(result.contains("Porto"));
    }
}
