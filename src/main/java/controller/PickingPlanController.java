package controller;

import domain.*;

import java.util.*;

public class PickingPlanController {


    public PickingPlan generatePlan(List<Allocation> allocations,
                                    PackingMode mode,
                                    double trolleyCapacityKg, AllocationPolicy allocationPolicy) {

        if (allocations == null) {
            throw new IllegalArgumentException("allocations == null");
        }
        if (trolleyCapacityKg <= 0) {
            throw new IllegalArgumentException("trolleyCapacityKg must be > 0");
        }
        if (mode == null) {
            throw new IllegalArgumentException("mode == null");
        }
        if (allocationPolicy == null) {
            throw new IllegalArgumentException("allocationPolicy == null");
        }

        List <PartialAllocationLog> partial = new ArrayList<>();
        List <Allocation> deferred = new ArrayList<>();
        List <Trolley> result;
        switch (mode) {
            case FF:
                result = planFirstFit(allocations, trolleyCapacityKg, allocationPolicy, partial, deferred);
                break;
            case FFD:
                result = planFirstFitDecreasing(allocations, trolleyCapacityKg, allocationPolicy, partial, deferred);
                break;
            case BFD:
                result = planBestFitDecreasing(allocations, trolleyCapacityKg, allocationPolicy, partial, deferred);
                break;
            default:
                throw new IllegalArgumentException("Unsupported mode: " + mode);
        }
        return new PickingPlan(mode, result);
    }


    private List<Trolley> planFirstFit(List<Allocation> allocations, double trolleyCapacity, AllocationPolicy allocationPolicy, List<PartialAllocationLog> partial, List<Allocation> deferred) {
        List<Trolley> trolleys = new ArrayList<>();
        int nextTrolleyId = 1;


        for (Allocation allocation : allocations) {
            double w = allocation.totalWeight();
            boolean placed = false;

            for (int i = 0; i < trolleys.size() && !placed; i++) {
                Trolley trolley = trolleys.get(i);
                if (trolley.canFit(w)) {
                    trolley.addItem(allocation);
                    placed = true;
                }
            }

                if (!placed && (allocationPolicy == AllocationPolicy.SPLIT_LINE || w > trolleyCapacity)) {
                    Trolley targetTrolley = null;
                    if (!trolleys.isEmpty()) {
                        targetTrolley = trolleys.get(trolleys.size() - 1);
                    }
                    double unitWeight = allocation.getUnitWeight();
                    double quantity = allocation.getQuantity();

                    if (targetTrolley != null && unitWeight > 0) {
                        double remaining = targetTrolley.remainingCapacity();
                        double quantityFits = (int)(remaining / unitWeight);
                        if (quantityFits >= 1){
                            double quantityLefts = quantity - quantityFits;

                            Allocation part = new Allocation(
                                allocation.getOrderId(),
                                allocation.getLineNo(),
                                allocation.getSku(),
                                allocation.getBoxId(),
                                allocation.getAisle(),
                                allocation.getBay(),
                                quantityFits,
                                unitWeight
                            );
                            targetTrolley.addItem(part);

                            Allocation lefts = new Allocation(
                                allocation.getOrderId(),
                                allocation.getLineNo(),
                                allocation.getSku(),
                                allocation.getBoxId(),
                                allocation.getAisle(),
                                allocation.getBay(),
                                quantityLefts,
                                unitWeight
                            );
                            partial.add(new PartialAllocationLog(allocation.getOrderId(), allocation.getLineNo(),allocation.getSku(), quantityFits, quantityLefts));

                            Trolley next = new Trolley(nextTrolleyId++, trolleyCapacity);
                            next.addItem(lefts);
                            trolleys.add(next);
                            placed = true;

                    }
                }

            }

            if (!placed) {
                if (!trolleys.isEmpty() && allocationPolicy == AllocationPolicy.DEFER_LINE) {
                    deferred.add(allocation);
                }
                Trolley nextTrolley = new Trolley(nextTrolleyId++, trolleyCapacity);
                nextTrolley.addItem(allocation);
                trolleys.add(nextTrolley);
            }
        }
        return trolleys;
    }

    private List<Trolley> planFirstFitDecreasing(List<Allocation> allocations, double trolleyCapacity, AllocationPolicy allocationPolicy, List<PartialAllocationLog> partial, List<Allocation> deferred) {
        List<Allocation> sorted = new ArrayList<>(allocations);
        Collections.sort(sorted);
        return planFirstFit(sorted, trolleyCapacity,allocationPolicy, partial, deferred);
    }

    private List<Trolley> planBestFitDecreasing(List<Allocation> allocations, double trolleyCapacity, AllocationPolicy allocationPolicy, List<PartialAllocationLog> partial, List<Allocation> deferred) {
        List<Allocation> sorted = new ArrayList<>(allocations);
        Collections.sort(sorted);

        List<Trolley> trolleys = new ArrayList<>();
        int nextTrolleyId = 1;

        for (Allocation allocation : sorted) {
            double w = allocation.totalWeight();
            boolean placed = false;
            Trolley best = null;
            double bestRemaining = trolleyCapacity; //trolley capacity para ser um valor alto

            for (Trolley trolley : trolleys) {
                if (trolley.canFit(w)) {
                    double remainingAfter = trolley.remainingCapacity() - w;
                    if (remainingAfter >= 0 && remainingAfter < bestRemaining) {
                        bestRemaining = remainingAfter;
                        best = trolley;
                    }
                }
            }

            if (best != null) {
                best.addItem(allocation);
                placed = true;
            } else {
                if(allocationPolicy == AllocationPolicy.SPLIT_LINE || w > trolleyCapacity) {
                    Trolley targetTrolley = null;
                    double maxRemaining = -1.0;
                    for (Trolley trolley : trolleys) {
                        double remaining = trolley.remainingCapacity();
                        if (remaining > maxRemaining) {
                            maxRemaining = remaining;
                            targetTrolley = trolley;
                        }
                    }
                    if (targetTrolley == null) {
                        targetTrolley = new Trolley(nextTrolleyId++, trolleyCapacity);
                        trolleys.add(targetTrolley);

                    }
                    double unitWeight = allocation.getUnitWeight();
                    if (unitWeight > 0) {
                        double quantityFits = (int)(targetTrolley.remainingCapacity() / unitWeight);
                        if (quantityFits > allocation.getQuantity()) {
                            quantityFits = allocation.getQuantity();
                        }
                        if (quantityFits >= 1) {
                            double quantityLefts = allocation.getQuantity() - quantityFits;

                            Allocation part = new Allocation(
                                allocation.getOrderId(),
                                allocation.getLineNo(),
                                allocation.getSku(),
                                allocation.getBoxId(),
                                allocation.getAisle(),
                                allocation.getBay(),
                                quantityFits,
                                unitWeight
                            );
                            targetTrolley.addItem(part);

                            Allocation lefts = new Allocation(
                                allocation.getOrderId(),
                                allocation.getLineNo(),
                                allocation.getSku(),
                                allocation.getBoxId(),
                                allocation.getAisle(),
                                allocation.getBay(),
                                quantityLefts,
                                unitWeight
                            );
                            partial.add(new PartialAllocationLog(allocation.getOrderId(), allocation.getLineNo(),allocation.getSku(), quantityFits, quantityLefts));

                            Trolley next = new Trolley(nextTrolleyId++, trolleyCapacity);
                            next.addItem(lefts);
                            trolleys.add(next);
                            placed = true;
                        }
                    }
                    if (!placed) {
                        Trolley nextTrolley = new Trolley(nextTrolleyId++, trolleyCapacity);
                        nextTrolley.addItem(allocation);
                        trolleys.add(nextTrolley);
                        placed = true;
                    }
                } else if (allocationPolicy == AllocationPolicy.DEFER_LINE) {
                    if (!trolleys.isEmpty()) {
                        deferred.add(allocation);
                        Trolley nextTrolley = new Trolley(nextTrolleyId++, trolleyCapacity);
                        nextTrolley.addItem(allocation);
                        trolleys.add(nextTrolley);
                        placed = true;
                    }
                }
                if (!placed) {
                    Trolley nt = new Trolley(nextTrolleyId++, trolleyCapacity);
                    nt.addItem(allocation);
                    trolleys.add(nt);
                }
            }
        }
        return trolleys;
    }
}
