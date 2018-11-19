package com.wdcloud.ocs;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ConvertResult {
    private boolean status;
    private String errorMsg;
}
