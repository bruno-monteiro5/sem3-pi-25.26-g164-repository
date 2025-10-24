package domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for {@link Option}.
 * <p>
 * Each test method targets exactly one public member of {@code Option}
 * and documents the scenario, preconditions, and expected outcome.
 */
class OptionTest {

    /**
     * Verifies that the constructor correctly assigns description and runnable.
     *
     * Preconditions:
     *  - Valid description and runnable object provided.
     *
     * Expected:
     *  - The string representation matches the description.
     *  - The runnable reference is stored correctly and executed when called.
     *
     * @see Option#Option(String, Runnable)
     * @see Option#toString()
     * @see Option#executeOption()
     */
    @Test
    void constructor_initializesFieldsAndExecutesCorrectly() {
        final boolean[] executed = {false};
        Runnable action = () -> executed[0] = true;

        Option option = new Option("Run process", action);

        assertEquals("Run process", option.toString());

        option.executeOption();
        assertTrue(executed[0]);
    }

    /**
     * Tests {@link Option#toString()} returns only the option description.
     *
     * Scenario:
     *  - Create an option and retrieve its string representation.
     *
     * Expected:
     *  - Returned string equals the description passed at construction.
     *
     * @see Option#toString()
     */
    @Test
    void toString_returnsDescriptionOnly() {
        Option option = new Option("View report", () -> {});
        assertEquals("View report", option.toString());
    }

    /**
     * Tests {@link Option#executeOption()} executes the associated {@link Runnable}.
     *
     * Scenario:
     *  - Create an option with a runnable that changes a flag.
     *  - Execute the option.
     *
     * Expected:
     *  - Runnable’s {@code run()} method is called once.
     *
     * @see Option#executeOption()
     */
    @Test
    void executeOption_runsAssignedRunnable() {
        final boolean[] executed = {false};
        Option option = new Option("Execute test", () -> executed[0] = true);

        option.executeOption();

        assertTrue(executed[0]);
    }
}
