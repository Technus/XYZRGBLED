package com.github.technus.xyzrgbled.model.color;

import javafx.scene.paint.Color;

public class ColorLedXYZ {
    private final double x,y,z;
    private final Color color;

    public ColorLedXYZ() {
        this(Color.WHITE);
    }

    public ColorLedXYZ(double r, double g, double b) {
        this(Color.color(r,g,b));
    }

    public ColorLedXYZ(Color color) {
        this.color=color;
        double[] xyzColorRaw= ColorConverter.RGBtoXYZ(this.color);
        this.x = xyzColorRaw[0];
        this.y = xyzColorRaw[1];
        this.z = xyzColorRaw[2];
    }

    public Color getColor(){
        return color;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }
}
