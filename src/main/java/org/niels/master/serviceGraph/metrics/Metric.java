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

    IS_PART_OF_CYCLE("Part of Communication Cycle");



    private final String text;

    Metric(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
