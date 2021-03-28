package hs.opcnetty.bean.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/3/28 12:09
 */




@Data
@Builder
public class IotWriteVo {
    private int status;
    private String message;
    private IotWriteDataVo data;
}
