package org.niels.master.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.niels.master.model.interfaces.Interface;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode
public class Service {
    private String name;

    private String dbms;
    private String database;


    private List<Interface> interfaces = new ArrayList<>();

    public Interface getInterfaceByName(String name) {
        return interfaces.stream().filter(i -> i.getName().equals(name)).findFirst().get();
    }

    public boolean isPartOfHandling(String handling) {
        return this.interfaces.stream().filter(i -> i.getPartOfHandling().contains(handling)).count() > 0;
    }
}
