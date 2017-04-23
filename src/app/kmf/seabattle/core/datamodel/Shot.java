package app.kmf.seabattle.core.datamodel;

import app.kmf.seabattle.enums.ShotResult;

/**
 * Created by Roman Rybalkin
 * 13.03.17
 */
public class Shot {
    private int x, y;
    private ShotResult prev;
    private ShotResult current;

    public Shot(int x, int y, ShotResult prev, ShotResult current) {
        this.x = x;
        this.y = y;
        this.prev = prev;
        this.current = current;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public ShotResult getPrev() {
        return prev;
    }

    public ShotResult getCurrent() {
        return current;
    }

    public void setCurrent(ShotResult current) {
        this.current = current;
    }

    public static Shot ofPlayer(int x, int y) {
        return new Shot(x, y, null, null);
    }

    public static Shot ofAndroid(ShotResult prev) {
        return new Shot(-1, -1, prev, null);
    }
}
