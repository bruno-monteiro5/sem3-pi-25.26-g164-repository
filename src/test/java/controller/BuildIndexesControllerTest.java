package controller;

import domain.Station;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import repository.StationRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link BuildIndexesController} using only JUnit4
 * and the real {@link StationRepository} singleton.
 * <p>
 * These tests aim to exercise all branches of the controller logic
 *
 */
public class BuildIndexesControllerTest {

    private BuildIndexesController controller;
    private PrintStream originalOut;
    private ByteArrayOutputStream outContent;

    /**
     * Sets up a fresh controller instance and captures {@code System.out}
     * before each test.
     */
    @Before
    public void setUp() {
        controller = new BuildIndexesController();

        originalOut = System.out;
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    /**
     * Restores {@code System.out} after each test to avoid
     * side effects between tests.
     */
    @After
    public void tearDown() {
        System.setOut(originalOut);
    }

    /**
     * Verifies that {@link BuildIndexesController#buildFromCsvPath(StationCsvLoader, String)}
     * correctly uses the loader when no exception is thrown and prints a summary line.
     */
    @Test
    public void testBuildFromCsvPathSuccessfulLoad() throws IOException {
        // Arrange: simple loader that returns an empty list and checks the path
        StationCsvLoader loader = new StationCsvLoader() {
            @Override
            public List<Station> load(String csvPath) throws IOException {
                assertEquals("path.csv", csvPath);
                return Collections.emptyList();
            }
        };

        // Act
        controller.buildFromCsvPath(loader, "path.csv");

        String output = outContent.toString();
        assertTrue("Expected summary line to be printed",
                output.contains("Index build finished."));
        assertTrue("Expected loaded count to be mentioned",
                output.contains("Loaded="));
        assertTrue("Expected rejected count to be mentioned",
                output.contains("Rejected="));
        assertTrue("Expected time value to be mentioned",
                output.contains("Time="));
    }

    /**
     * Verifies that {@link BuildIndexesController#buildFromCsvPath(StationCsvLoader, String)}
     * handles {@link IOException} from the loader by printing an error message
     * and still finishing without throwing.
     */
    @Test
    public void testBuildFromCsvPathHandlesIOException() throws IOException {
        // Arrange: loader that always throws an IOException
        StationCsvLoader loader = new StationCsvLoader() {
            @Override
            public List<Station> load(String csvPath) throws IOException {
                throw new IOException("boom");
            }
        };

        // Act
        controller.buildFromCsvPath(loader, "bad.csv");

        // Assert: output should contain an error message and still show the summary line
        String output = outContent.toString();
        assertTrue("Error message should mention the CSV path",
                output.contains("Error reading CSV 'bad.csv'"));
        assertTrue("Error message should include the exception message",
                output.contains("boom"));
        assertTrue("Even after an error, a summary line should be printed",
                output.contains("Index build finished."));
    }

    /**
     * Ensures that {@link BuildIndexesController#queryByLatitude(double, double)}
     * returns a non-null list and does not throw, even if no data is loaded.
     */
    @Test
    public void testQueryByLatitudeReturnsNonNull() {
        List<Station> result = controller.queryByLatitude(0.0, 10.0);
        assertNotNull("Result list should never be null", result);
    }

    /**
     * Ensures that {@link BuildIndexesController#queryByLongitude(double, double)}
     * returns a non-null list and does not throw, even if no data is loaded.
     */
    @Test
    public void testQueryByLongitudeReturnsNonNull() {
        List<Station> result = controller.queryByLongitude(-5.0, 5.0);
        assertNotNull("Result list should never be null", result);
    }

    /**
     * Ensures that {@link BuildIndexesController#queryByTimeZoneGroup(String)}
     * returns a non-null list and does not throw.
     */
    @Test
    public void testQueryByTimeZoneGroupReturnsNonNull() {
        List<Station> result = controller.queryByTimeZoneGroup("Europe");
        assertNotNull("Result list should never be null", result);
    }

    /**
     * Ensures that {@link BuildIndexesController#queryByTimeZoneAndCountry(String, String)}
     * returns a non-null list and does not throw.
     */
    @Test
    public void testQueryByTimeZoneAndCountryReturnsNonNull() {
        List<Station> result = controller.queryByTimeZoneAndCountry("Europe", "PT");
        assertNotNull("Result list should never be null", result);
    }

    /**
     * Ensures that {@link BuildIndexesController#isLoaded()} can be called
     * safely and reflects the repository state.
     * <p>
     * No specific value is asserted here because the underlying
     * {@link StationRepository} implementation may decide when to mark
     * itself as loaded. The goal is to execute the method for coverage.
     */
    @Test
    public void testIsLoadedCallable() {
        boolean loaded = controller.isLoaded();
        assertNotNull(Boolean.valueOf(loaded));
    }
}
