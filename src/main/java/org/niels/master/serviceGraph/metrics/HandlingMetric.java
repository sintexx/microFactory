package org.niels.master.serviceGraph.metrics;

public enum HandlingMetric {
    SYNC_HANDLING_DEPENDENCIES("Number of Synchronous Handling Dependencies"),

    ASYNC_HANDLING_DEPENDENCIES("Number of Asynchronous Handling Dependencies"),

    MAX_AFFECTED_SERVICES_CHAIN("Max Length of Affected Service Chain");


    private final String text;

    HandlingMetric(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
