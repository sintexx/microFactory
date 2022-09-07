package org.niels.master.serviceGraph.metrics;

public enum Metric {
    PRODUCED_ENDPOINTS("Number of Produced Endpoints"),
    CONSUMED_ENDPOINTS("Number of Consumed Endpoints"),

    SYNC_SERVICE_DEPENDENCIES("Number of Synchronous Service Dependencies"),

    ASYNC_SERVICE_DEPENDENCIES("Number of Asynchronous Service Dependencies"),

    MAX_AFFECTED_SERVICES_CHAIN_PER_HANDLING("Max Length of Affected Service Chain per Handling"),
    MAX_AFFECTED_SERVICES_PER_HANDLING("Max Affected Services per Handling"),

    DEPENDENCIES_SIMPLE_FAILOVER("Number of Dependencies with Simple Failover"),
    DEPENDENCIES_COMPLEX_FAILOVER("Number of Dependencies with Complex Failover"),

    IS_PART_OF_CYCLE("Part of Communication Cycle"),

    ABSOLUT_IMPORTANCE_OF_THE_SERVICE("Absolute Importance of the Service (AIS)"),

    ABSOLUT_DEPENDENCE_OF_THE_SERVICE("Absolute Dependence of the Service (ADS)"),

    ABSOLUT_CRITICALITY_OF_SERVICE("Absolute Criticality of the Service (ACS)"),

    RELATIVE_IMPORTANCE_OF_THE_SERVICE("Relative Importance of Service (RIS)"),

    RELATIVE_DEPENDENCE_OF_THE_SERVICE("Relative Dependence of the Service (RDS)"),

    RELATIVE_CRITICALITY_OF_SERVICE("RELATIVE Criticality of the Service (RCS)"),

    WORKLOAD("Workload");


    private final String text;

    Metric(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
