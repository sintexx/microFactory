package org.niels.master.model.logic;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=true)
public class HttpServiceCall extends ServiceCall {
    private String endpoint;

    private Fallback fallback = Fallback.NONE;

    public enum Fallback {
        @SerializedName("NONE")
        NONE,
        @SerializedName("RETRY")
        RETRY,
        @SerializedName("COMPLEX")
        COMPLEX,
    }

}
