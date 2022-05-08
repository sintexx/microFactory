package org.niels.master.model.logic;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Logic {
    private String type;

    public enum LogicType {
        @SerializedName("databaseAccess")
        DATABASE_ACCESS,
        @SerializedName("serviceCall")
        SERVICE_CALL,
    }
}
