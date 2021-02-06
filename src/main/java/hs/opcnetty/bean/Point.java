package hs.opcnetty.bean;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/1/4 14:40
 */
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


    public int getPointid() {
        return pointid;
    }

    public void setPointid(int pointid) {
        this.pointid = pointid;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getNotion() {
        return notion;
    }

    public void setNotion(String notion) {
        this.notion = notion;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getWriteable() {
        return writeable;
    }

    public void setWriteable(int writeable) {
        this.writeable = writeable;
    }

    public int getOpcserveid() {
        return opcserveid;
    }

    public void setOpcserveid(int opcserveid) {
        this.opcserveid = opcserveid;
    }

    public String getResouce() {
        return resouce;
    }

    public void setResouce(String resouce) {
        this.resouce = resouce;
    }

    public String getStandard() {
        return standard;
    }

    public void setStandard(String standard) {
        this.standard = standard;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }
}
