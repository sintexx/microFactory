package org.niels.master.serviceGraph.metrics;

public enum HandlingMetric {
    SYNC_HANDLING_DEPENDENCIES("Number of Synchronous Handling Dependencies"),

    ASYNC_HANDLING_DEPENDENCIES("Number of Asynchronous Handling Dependencies"),

    MAX_AFFECTED_SERVICES_CHAIN("Max Length of Affected Service Chain"),

    DISTRIBUTION_OF_WORKLOAD("Distribution of workload (mean deviation)"),

    NUMBER_OF_AFFECTED_SERVICES("Number of affected Services");


    private final String text;

    HandlingMetric(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
