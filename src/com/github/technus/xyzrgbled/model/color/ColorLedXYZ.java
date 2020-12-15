package com.github.technus.xyzrgbled.model.color;

import javafx.scene.paint.Color;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColorLedXYZ that = (ColorLedXYZ) o;
        return Double.compare(that.x, x) == 0 &&
                Double.compare(that.y, y) == 0 &&
                Double.compare(that.z, z) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }
}
