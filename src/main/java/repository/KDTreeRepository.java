package repository;

import domain.KDTree2D;

/**
 * Singleton repository for storing the 2D-Tree instance.
 * Ensures the same tree is accessible across different controllers and UIs.
 */
public class KDTreeRepository {

    private static KDTreeRepository instance = null;
    private KDTree2D kdTree;

    private KDTreeRepository() {
        this.kdTree = null;
    }

    /**
     * Returns the singleton instance of KDTreeRepository.
     */
    public static synchronized KDTreeRepository getInstance() {
        if (instance == null) {
            instance = new KDTreeRepository();
        }
        return instance;
    }

    /**
     * Stores the 2D-Tree.
     */
    public void setKDTree(KDTree2D kdTree) {
        this.kdTree = kdTree;
    }

    /**
     * Retrieves the 2D-Tree.
     */
    public KDTree2D getKDTree() {
        return kdTree;
    }

    /**
     * Checks if the 2D-Tree has been built.
     */
    public boolean isBuilt() {
        return kdTree != null && kdTree.size() > 0;
    }

    /**
     * Clears the stored 2D-Tree.
     */
    public void clear() {
        this.kdTree = null;
    }
}