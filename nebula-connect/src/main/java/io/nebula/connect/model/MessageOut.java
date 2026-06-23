package io.nebula.connect.model;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageOut {
    private String code;
    private String subCode;
    private Integer sequence;
    private JSONObject response;
}
