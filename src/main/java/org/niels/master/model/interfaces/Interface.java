package org.niels.master.model.interfaces;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.niels.master.model.logic.Logic;

import java.util.List;

@Data
@NoArgsConstructor
public class Interface {
    protected String name;
    protected Type type;
    protected InputType in;
    protected OutputType out;

    protected List<Logic> logic;

    public enum Type {
        @SerializedName("http")
        HTTP,
        @SerializedName("amqp")
        AMQP
    }

    public enum OutputType {
        @SerializedName("none")
        NONE,
        @SerializedName("single")
        SINGLE,
        @SerializedName("list")
        LIST
    }

    public enum InputType {
        @SerializedName("none")
        NONE,
        @SerializedName("single")
        SINGLE,
        @SerializedName("list")
        LIST
    }
}
