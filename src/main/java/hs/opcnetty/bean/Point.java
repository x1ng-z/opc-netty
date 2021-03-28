package hs.opcnetty.bean;

import lombok.Builder;
import lombok.Data;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/1/4 14:40
 */
@Data
@Builder
public class Point {
    private int pointid;//忽略
    private String tag;//dcs位号
    private String notion;//位号注释
    private String type;//忽略，数据类型opc程序自动判断
    private int writeable;//标志dcs位号是否可进行反写
    private int opcserveid;//所属opcserveid
    private String resouce;//忽略
    private String standard;//忽略
    private float value;//反写时候用的值
    private int rate;

}
