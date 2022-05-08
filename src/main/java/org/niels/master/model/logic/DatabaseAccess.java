package org.niels.master.model.logic;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=true)
public class DatabaseAccess extends Logic {
    private String database;
    private DatabaseMethod method;

    public enum DatabaseMethod {
        @SerializedName("getSingle")
        GET_SINGLE,
        @SerializedName("getList")
        GET_LIST,
        @SerializedName("saveSingle")
        SAVE_SINGLE,
    }
}
