package domain;

import java.util.ArrayList;
import java.util.List;

public final class KDTreeUtil {

    private KDTreeUtil() {} // Não instanciar

    /**
     * Coleta todas as estações da árvore
     */
    public static List<Station> collectAllStations(KDTree2D tree) {
        List<Station> result = new ArrayList<>();
        if (tree != null && tree.getRoot() != null) {
            collectAllStations(tree.getRoot(), result);
        }
        return result;
    }

    /**
     * Travessia recursiva
     */
    public static void collectAllStations(KDNode node, List<Station> accumulator) {
        if (node == null) return;

        List<Station> bucket = node.getStations();
        for (int i = 0; i < bucket.size(); i++) {
            accumulator.add(bucket.get(i));
        }

        collectAllStations(node.getLeft(), accumulator);
        collectAllStations(node.getRight(), accumulator);
    }

    /**
     * Valida coordenadas geográficas
     */
    public static void validateCoordinates(double latitude, double longitude) {
        if (latitude < -90.0 || latitude > 90.0) {
            throw new IllegalArgumentException(
                    "Latitude must be between -90 and 90 degrees, got: " + latitude);
        }
        if (longitude < -180.0 || longitude > 180.0) {
            throw new IllegalArgumentException(
                    "Longitude must be between -180 and 180 degrees, got: " + longitude);
        }
    }
}