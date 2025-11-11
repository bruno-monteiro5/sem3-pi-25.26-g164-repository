package controller;

import repository.StationRepository;
import domain.Station;
import java.util.List;

public class BuildIndexesController {

    private final StationRepository repo;

    public BuildIndexesController(StationRepository repo) {
        this.repo = repo;
    }


}
