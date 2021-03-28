package hs.opcnetty.bean.dto;

import com.alibaba.csp.sentinel.util.StringUtil;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/3/28 10:35
 */
@Data
public class OpcConnectDto {
    private boolean autoConnect;
    private String updateRate;
    @NotNull
    private String opcIp;
    private String reconnectInterval;
    private String delayTime;
    private String opcPassword;
    @NotNull
    private String opcClsId;
    private String opcUsername;

    public boolean valid(){
        if(StringUtil.isEmpty(opcIp)){
            return false;
        }
        if(StringUtil.isEmpty(opcClsId)){
            return false;
        }
        return true;
    }
}
