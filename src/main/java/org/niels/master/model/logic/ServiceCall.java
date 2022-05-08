package org.niels.master.model.logic;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=true)
public class ServiceCall extends Logic {
    private String service;
    private String method;

}
