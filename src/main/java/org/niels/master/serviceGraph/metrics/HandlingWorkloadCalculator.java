package org.niels.master.serviceGraph.metrics;

import lombok.AllArgsConstructor;
import org.niels.master.model.Service;
import org.niels.master.model.interfaces.Interface;
import org.niels.master.model.logic.DatabaseAccess;
import org.niels.master.model.logic.Logic;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class HandlingWorkloadCalculator {
    private List<Service> services;

    public HandlingWorkload calculateWorkloadOfHandling(String handling) {

        var workload = new HandlingWorkload(handling);

        for (Service service : services) {
            for (Interface anInterface : service.getInterfaces()) {
                if (!anInterface.getPartOfHandling().contains(handling)) {
                    continue;
                }

                if (anInterface.getWorkload() != null) {
                    workload.setCalculateIterations(workload.getCalculateIterations() + anInterface.getWorkload());
                }

                for (Logic logic : anInterface.getLogic()) {
                    if (logic instanceof DatabaseAccess dbAccess) {
                        switch (dbAccess.getMethod()) {

                            case GET_SINGLE -> {
                                workload.setDbGetSingle(workload.getDbGetSingle() + 1);
                            }
                            case GET_LIST -> {
                                workload.setDbGetList(workload.getDbGetList() + 1);

                            }
                            case SAVE_SINGLE -> {
                                workload.setDbSaveSingle(workload.getDbSaveSingle() + 1);

                            }
                            case SAVE_LIST -> {
                                workload.setDbSaveList(workload.getDbSaveList() + 1);

                            }
                        }
                    }
                }
            }
        }

        return workload;
    }
}
