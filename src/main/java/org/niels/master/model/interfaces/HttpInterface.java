package org.niels.master.model.interfaces;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=true)
public class HttpInterface extends Interface {
    private HttpMethod method;

    public enum HttpMethod {
        @SerializedName("GET")
        GET,
        @SerializedName("POST")
        POST
    }

    public String getClientMethodName() {
        return this.getMethod().toString().toLowerCase() + this.getName();
    }
}
