package domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KDNodeTest {

    private Station station1;
    private Station station2;
    private Station station3;
    private List<Station> stationList;

    @BeforeEach
    void setUp() {
        station1 = new Station(
                "Lisboa Santa Apolonia",
                "PT",
                new Location(38.71387, -9.122271),
                "WET",
                true,
                true,
                false
        );

        station2 = new Station(
                "Lisboa Oriente",
                "PT",
                new Location(38.71387, -9.122271), // Same coordinates
                "WET",
                false,
                true,
                false
        );

        station3 = new Station(
                "Porto Campanha",
                "PT",
                new Location(41.14961, -8.58519),
                "WET",
                true,
                true,
                false
        );

        stationList = new ArrayList<>();
    }

    @Test
    void testConstructorWithSingleStation() {
        stationList.add(station1);
        KDNode node = new KDNode(0, stationList);

        assertEquals(0, node.getAxis());
        assertEquals(1, node.getStations().size());
        assertEquals("Lisboa Santa Apolonia", node.getStations().get(0).getName());
        assertNull(node.getLeft());
        assertNull(node.getRight());
    }

    @Test
    void testConstructorWithMultipleStations() {
        stationList.add(station2);
        stationList.add(station1);
        KDNode node = new KDNode(1, stationList);

        assertEquals(1, node.getAxis());
        assertEquals(2, node.getStations().size());
        // Should be sorted by name
        assertEquals("Lisboa Oriente", node.getStations().get(0).getName());
        assertEquals("Lisboa Santa Apolonia", node.getStations().get(1).getName());
    }

    @Test
    void testGetCoordinateLatitude() {
        stationList.add(station1);
        KDNode node = new KDNode(0, stationList);

        assertEquals(38.71387, node.getCoordinate(), 0.00001);
    }

    @Test
    void testGetCoordinateLongitude() {
        stationList.add(station1);
        KDNode node = new KDNode(1, stationList);

        assertEquals(-9.122271, node.getCoordinate(), 0.00001);
    }

    @Test
    void testGetCoordinateEmptyList() {
        KDNode node = new KDNode(0, new ArrayList<>());
        assertEquals(0.0, node.getCoordinate(), 0.00001);
    }

    @Test
    void testSetLeft() {
        stationList.add(station1);
        KDNode node = new KDNode(0, stationList);

        List<Station> leftList = new ArrayList<>();
        leftList.add(station3);
        KDNode leftNode = new KDNode(1, leftList);

        node.setLeft(leftNode);
        assertNotNull(node.getLeft());
        assertEquals(leftNode, node.getLeft());
    }

    @Test
    void testSetRight() {
        stationList.add(station1);
        KDNode node = new KDNode(0, stationList);

        List<Station> rightList = new ArrayList<>();
        rightList.add(station3);
        KDNode rightNode = new KDNode(1, rightList);

        node.setRight(rightNode);
        assertNotNull(node.getRight());
        assertEquals(rightNode, node.getRight());
    }

    @Test
    void testAxisLatitude() {
        stationList.add(station1);
        KDNode node = new KDNode(0, stationList);
        assertEquals(0, node.getAxis());
    }

    @Test
    void testAxisLongitude() {
        stationList.add(station1);
        KDNode node = new KDNode(1, stationList);
        assertEquals(1, node.getAxis());
    }

    @Test
    void testStationsSortedAlphabetically() {
        // Add in reverse order
        stationList.add(station3);
        stationList.add(station2);
        stationList.add(station1);

        KDNode node = new KDNode(0, stationList);

        List<Station> sorted = node.getStations();
        assertEquals("Lisboa Oriente", sorted.get(0).getName());
        assertEquals("Lisboa Santa Apolonia", sorted.get(1).getName());
        assertEquals("Porto Campanha", sorted.get(2).getName());
    }

    @Test
    void testGetStationsIsImmutable() {
        stationList.add(station1);
        KDNode node = new KDNode(0, stationList);

        List<Station> retrieved = node.getStations();
        int originalSize = retrieved.size();

        // Modify the original list
        stationList.add(station2);

        // Retrieved list should not change
        assertEquals(originalSize, retrieved.size());
    }

    @Test
    void testSetLeftAndRight() {
        stationList.add(station1);
        KDNode root = new KDNode(0, stationList);

        List<Station> leftList = new ArrayList<>();
        leftList.add(station2);
        KDNode left = new KDNode(1, leftList);

        List<Station> rightList = new ArrayList<>();
        rightList.add(station3);
        KDNode right = new KDNode(1, rightList);

        root.setLeft(left);
        root.setRight(right);

        assertEquals(left, root.getLeft());
        assertEquals(right, root.getRight());
    }

    @Test
    void testNullChildren() {
        stationList.add(station1);
        KDNode node = new KDNode(0, stationList);

        assertNull(node.getLeft());
        assertNull(node.getRight());
    }
}