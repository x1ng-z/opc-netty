package hs.opcnetty.opc.event;


import hs.opcnetty.bean.Point;

/**
 * @author zzx
 * @version 1.0
 * @date 2020/12/1 8:28
 */
public class WriteEvent implements Event {
    private Point point;
    private float value;

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    @Override
    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }
}
