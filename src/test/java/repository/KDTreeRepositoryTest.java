package repository;

import domain.KDTree2D;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link KDTreeRepository}.
 */
public class KDTreeRepositoryTest {

    /**
     * Resets the singleton before each test to avoid interference.
     */
    @Before
    public void setUp() {
        KDTreeRepository.resetForTests();
    }

    /**
     * Resets the singleton after each test to keep tests isolated.
     */
    @After
    public void tearDown() {
        KDTreeRepository.resetForTests();
    }

    /**
     * Ensures that {@code getInstance} returns the same singleton instance.
     */
    @Test
    public void testSingletonReturnsSameInstance() {
        KDTreeRepository repo1 = KDTreeRepository.getInstance();
        KDTreeRepository repo2 = KDTreeRepository.getInstance();

        assertNotNull(repo1);
        assertSame(repo1, repo2);
    }

    /**
     * Verifies that {@code setKDTree} and {@code getKDTree} store and return the same instance.
     */
    @Test
    public void testSetAndGetKDTree() {
        KDTreeRepository repo = KDTreeRepository.getInstance();
        KDTree2D tree = createKDTreeWithSize(3);

        repo.setKDTree(tree);

        assertSame(tree, repo.getKDTree());
    }

    /**
     * Verifies that {@code isBuilt} returns {@code false} when no tree is stored.
     */
    @Test
    public void testIsBuiltReturnsFalseWhenTreeIsNull() {
        KDTreeRepository repo = KDTreeRepository.getInstance();

        assertFalse(repo.isBuilt());
    }

    /**
     * Verifies that {@code isBuilt} returns {@code false} when the stored tree is empty.
     */
    @Test
    public void testIsBuiltReturnsFalseWhenTreeIsEmpty() {
        KDTreeRepository repo = KDTreeRepository.getInstance();
        KDTree2D emptyTree = createKDTreeWithSize(0);

        repo.setKDTree(emptyTree);

        assertFalse(repo.isBuilt());
    }

    /**
     * Verifies that {@code isBuilt} returns {@code true} when the stored tree has elements.
     */
    @Test
    public void testIsBuiltReturnsTrueWhenTreeHasElements() {
        KDTreeRepository repo = KDTreeRepository.getInstance();
        KDTree2D nonEmptyTree = createKDTreeWithSize(5);

        repo.setKDTree(nonEmptyTree);

        assertTrue(repo.isBuilt());
    }

    /**
     * Verifies that {@code clear} removes the stored tree.
     */
    @Test
    public void testClearRemovesStoredTree() {
        KDTreeRepository repo = KDTreeRepository.getInstance();
        KDTree2D tree = createKDTreeWithSize(2);

        repo.setKDTree(tree);
        assertNotNull(repo.getKDTree());

        repo.clear();

        assertNull(repo.getKDTree());
        assertFalse(repo.isBuilt());
    }

    /**
     * Verifies that {@code resetForTests} discards the current singleton instance.
     */
    @Test
    public void testResetForTestsCreatesNewInstance() {
        KDTreeRepository first = KDTreeRepository.getInstance();
        KDTree2D tree = createKDTreeWithSize(4);
        first.setKDTree(tree);

        KDTreeRepository.resetForTests();

        KDTreeRepository second = KDTreeRepository.getInstance();

        assertNotSame(first, second);
        assertNull(second.getKDTree());
        assertFalse(second.isBuilt());
    }

    /**
     * Creates a {@link KDTree2D} with a given {@code size()} using an anonymous subclass.
     *
     * @param size value to be returned by {@code size()}
     * @return a {@link KDTree2D} instance with the requested size
     */
    private KDTree2D createKDTreeWithSize(final int size) {
        return new KDTree2D() {
            @Override
            public int size() {
                return size;
            }
        };
    }
}
