package org.niels.master.serviceGraph.metrics;

import lombok.Data;

@Data
public class HandlingWorkload {
    private String handling;
    private int dbGetSingle = 0;
    private int dbGetList = 0;
    private int dbSaveSingle = 0;
    private int dbSaveList = 0;
    private int calculateIterations = 0;

    public HandlingWorkload(String handling) {
        this.handling = handling;
    }
}
