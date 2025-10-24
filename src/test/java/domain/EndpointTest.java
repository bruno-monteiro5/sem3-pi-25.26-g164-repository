package domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class EndpointTest {


    private static class TestEndpoint extends Endpoint {
        public TestEndpoint(String name, String location) {
            super(name, location);
        }
    }


    @Test
    void constructor_initializesFields() {
        Endpoint endpoint = new TestEndpoint("Central Station", "Lisbon");

        assertEquals("Central Station", endpoint.getName());
        assertEquals("Lisbon", endpoint.getLocation());
    }


    @Test
    void getName_returnsCurrentValue() {
        Endpoint endpoint = new TestEndpoint("North Terminal", "Porto");
        assertEquals("North Terminal", endpoint.getName());
    }

    @Test
    void getLocation_returnsCurrentValue() {
        Endpoint endpoint = new TestEndpoint("East Yard", "Braga");
        assertEquals("Braga", endpoint.getLocation());
    }
}
