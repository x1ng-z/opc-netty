package hs.opcnetty.bean.vo;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IotWriteDataFialedVo{
    private String node;
    private String measurePoint;
    private String message;
}