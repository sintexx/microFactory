package org.niels.master.model.logic;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Logic {
    private LogicType type;

    public enum LogicType {
        @SerializedName("databaseAccess")
        DATABASE_ACCESS,
        @SerializedName("serviceCall")
        SERVICE_CALL,
        @SerializedName("insertMock")
        INSERT_MOCK,
    }
}
