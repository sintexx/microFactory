package org.niels.master.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ServiceModelConfig {
    private List<Service> services;
    private List<DatabaseServer> databaseServers;
}
