package hs.opcnetty.bean;

import lombok.Data;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/1/4 14:37
 */
@Data
public class OpcServeInfo {
    private static AtomicInteger generid=new AtomicInteger(1);
    /**opc serve id 不同的opc此id不可相同*/
    private final int serveid=generid.addAndGet(1);
    /**opc serve name*/
    private String servename;
    /**opc serve ip*/
    private String serveip;

    private long iotid;
}
