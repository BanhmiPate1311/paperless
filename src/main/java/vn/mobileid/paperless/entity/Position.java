package vn.mobileid.paperless.entity;

public class Position {
    private String pos;
    private int x1;
    private int x2;
    private int y1;
    private int y2;
    private int height = 50;
    private int width = 180;
    private int distance = 7;

    public Position(int count, int pageHeight) {
        this.x1 = (width + distance) * (count % 4) + distance;
        this.y1 = (pageHeight - distance - height) - (height + distance) * (count / 4);

    }

    public String getPos() {
        return pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }

    public int getX1() {
        return x1;
    }

    public void setX1(int x1) {
        this.x1 = x1;
    }

    public int getX2() {
        return x2;
    }

    public void setX2(int x2) {
        this.x2 = x2;
    }

    public int getY1() {
        return y1;
    }

    public void setY1(int y1) {
        this.y1 = y1;
    }

    public int getY2() {
        return y2;
    }

    public void setY2(int y2) {
        this.y2 = y2;
    }
}
