package org.niels.master.model.logic;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AmqpServiceCall extends ServiceCall {
    private String query;
    private AmqpCallOut out;

    public enum AmqpCallOut {
        @SerializedName("single")
        SINGLE,
        @SerializedName("list")
        LIST
    }
}
