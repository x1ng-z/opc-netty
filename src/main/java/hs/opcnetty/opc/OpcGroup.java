package hs.opcnetty.opc;

import lombok.Data;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/1/14 22:20
 */
@Data
public class OpcGroup {
    private OpcExecute readopcexecute;
    private OpcExecute writeopcexecute;
    private long iotid;
}
