package org.niels.master.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class DatabaseServer {
    private String name;
    private List<String> databases;

    public static String getKubernetesServiceName(String dbmsName) {
        return dbmsName + "postgres";
    }
}
