package org.niels.master.model.logic;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=true)
public class InsertMock extends Logic {
    private int size;

    private TargetVariable targetVariable;

    public enum TargetVariable {
        @SerializedName("single")
        SINGLE,
        @SerializedName("list")
        LIST
    }
}
