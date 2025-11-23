package domain.railway;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for {@link Endpoint}.
 * <p>
 * Each test method targets exactly one public member of {@code Endpoint}
 * and documents the scenario, preconditions and expected outcome.
 */
public class EndpointTest {

    /**
     * Verifies that the constructor initializes all fields correctly.
     *
     * Preconditions:
     *  - valid id, name, and metafacilityId supplied.
     *
     * Expected:
     *  - {@link Endpoint#getId()} returns the supplied id;
     *  - {@link Endpoint#getName()} returns the supplied name;
     *  - {@link Endpoint#getMetafacilityId()} returns the supplied metafacilityId.
     *
     * @see Endpoint#Endpoint(int, String, int)
     * @see Endpoint#getId()
     * @see Endpoint#getName()
     * @see Endpoint#getMetafacilityId()
     */
    @Test
    public void constructor_initializesAllFields() {
        Endpoint endpoint = new Endpoint(1, "Lisbon Santa Apolonia", 100);

        assertEquals(1, endpoint.getId());
        assertEquals("Lisbon Santa Apolonia", endpoint.getName());
        assertEquals(100, endpoint.getMetafacilityId());
    }

    /**
     * Tests the getter for the endpoint identifier.
     *
     * Scenario:
     *  - Create an endpoint with a known id and read it back.
     *
     * Expected:
     *  - returned id matches the constructor value.
     *
     * @see Endpoint#getId()
     */
    @Test
    public void getId_returnsCorrectId() {
        Endpoint endpoint = new Endpoint(42, "Porto Campanha", 200);
        assertEquals(42, endpoint.getId());
    }

    /**
     * Tests the getter for the endpoint name.
     *
     * Scenario:
     *  - Create an endpoint with a known name and read it back.
     *
     * Expected:
     *  - returned name matches the constructor value.
     *
     * @see Endpoint#getName()
     */
    @Test
    public void getName_returnsCorrectName() {
        Endpoint endpoint = new Endpoint(1, "Coimbra-B", 300);
        assertEquals("Coimbra-B", endpoint.getName());
    }

    /**
     * Tests the getter for the metafacility identifier.
     *
     * Scenario:
     *  - Create an endpoint with a known metafacilityId and read it back.
     *
     * Expected:
     *  - returned metafacilityId matches the constructor value.
     *
     * @see Endpoint#getMetafacilityId()
     */
    @Test
    public void getMetafacilityId_returnsCorrectId() {
        Endpoint endpoint = new Endpoint(5, "Braga", 500);
        assertEquals(500, endpoint.getMetafacilityId());
    }

    /**
     * Tests the string representation of an endpoint.
     *
     * Scenario:
     *  - Create an endpoint and verify its toString format.
     *
     * Expected:
     *  - exact match to the documented format "Endpoint[id=%d, name=%s]".
     *
     * @see Endpoint#toString()
     */
    @Test
    public void toString_formatsCorrectly() {
        Endpoint endpoint = new Endpoint(10, "Faro", 1000);
        String result = endpoint.toString();

        assertEquals("Endpoint[id=10, name=Faro]", result);
    }

    /**
     * Tests equals method with the same endpoint (reflexive).
     *
     * Scenario:
     *  - Compare an endpoint with itself.
     *
     * Expected:
     *  - returns true.
     *
     * @see Endpoint#equals(Object)
     */
    @Test
    public void equals_reflexive() {
        Endpoint endpoint = new Endpoint(1, "Lisbon", 100);
        assertTrue(endpoint.equals(endpoint));
    }

    /**
     * Tests equals method with two endpoints having the same id.
     *
     * Scenario:
     *  - Create two endpoints with same id but different names.
     *
     * Expected:
     *  - returns true (equality based on id only).
     *
     * @see Endpoint#equals(Object)
     */
    @Test
    public void equals_sameId_returnsTrue() {
        Endpoint endpoint1 = new Endpoint(1, "Lisbon", 100);
        Endpoint endpoint2 = new Endpoint(1, "Lisboa", 200);

        assertTrue(endpoint1.equals(endpoint2));
    }

    /**
     * Tests equals method with two endpoints having different ids.
     *
     * Scenario:
     *  - Create two endpoints with different ids.
     *
     * Expected:
     *  - returns false.
     *
     * @see Endpoint#equals(Object)
     */
    @Test
    public void equals_differentId_returnsFalse() {
        Endpoint endpoint1 = new Endpoint(1, "Lisbon", 100);
        Endpoint endpoint2 = new Endpoint(2, "Porto", 200);

        assertFalse(endpoint1.equals(endpoint2));
    }

    /**
     * Tests equals method with null object.
     *
     * Scenario:
     *  - Compare an endpoint with null.
     *
     * Expected:
     *  - returns false.
     *
     * @see Endpoint#equals(Object)
     */
    @Test
    public void equals_nullObject_returnsFalse() {
        Endpoint endpoint = new Endpoint(1, "Lisbon", 100);
        assertFalse(endpoint.equals(null));
    }

    /**
     * Tests equals method with object of different class.
     *
     * Scenario:
     *  - Compare an endpoint with a String.
     *
     * Expected:
     *  - returns false.
     *
     * @see Endpoint#equals(Object)
     */
    @Test
    public void equals_differentClass_returnsFalse() {
        Endpoint endpoint = new Endpoint(1, "Lisbon", 100);
        assertFalse(endpoint.equals("Not an endpoint"));
    }

    /**
     * Tests hashCode consistency with equals.
     *
     * Scenario:
     *  - Create two endpoints with the same id.
     *
     * Expected:
     *  - both have the same hashCode.
     *
     * @see Endpoint#hashCode()
     */
    @Test
    public void hashCode_consistentWithEquals() {
        Endpoint endpoint1 = new Endpoint(1, "Lisbon", 100);
        Endpoint endpoint2 = new Endpoint(1, "Lisboa", 200);

        assertEquals(endpoint1.hashCode(), endpoint2.hashCode());
    }

    /**
     * Tests hashCode for different endpoints.
     *
     * Scenario:
     *  - Create two endpoints with different ids.
     *
     * Expected:
     *  - hashCodes should be different (not guaranteed but highly probable).
     *
     * @see Endpoint#hashCode()
     */
    @Test
    public void hashCode_differentIds_differentHashCodes() {
        Endpoint endpoint1 = new Endpoint(1, "Lisbon", 100);
        Endpoint endpoint2 = new Endpoint(2, "Porto", 200);

        assertNotEquals(endpoint1.hashCode(), endpoint2.hashCode());
    }
}