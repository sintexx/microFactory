package org.niels.master.model.logic;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=true)
public class DatabaseLogic extends Logic {
    private String database;
    private String method;
}
