package hs.opcnetty.bean.vo;


import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class IotWriteDataVo{
    private int count;
    private List<IotWriteDataFialedVo> fialed;
}
