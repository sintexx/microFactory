package org.niels.master.generation.clients;

import com.squareup.javapoet.ClassName;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RestClient {
    private ClassName standard;
    private ClassName retry;
    private ClassName failover;
}
