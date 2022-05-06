package org.niels.master.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.niels.master.model.interfaces.Interface;

import java.util.List;

@Data
@NoArgsConstructor
public class Service {
    private String name;
    private List<Interface> interfaces;
}
