package com.github.technus.xyzrgbled.model.color;

import javafx.scene.paint.Color;

public class ColorChooserXYZ {
    private final double x,y,z,mult;
    private final Color color;

    public ColorChooserXYZ() {
        this(Color.WHITE,1000);
    }

    public ColorChooserXYZ(double r, double g, double b, double multiplier) {
        this(Color.color(r,g,b),multiplier);
    }

    public ColorChooserXYZ(Color color, double multiplier) {
        this.color=color;
        this.mult=multiplier;
        double[] xyzColorRaw= ColorConverter.RGBtoXYZ2(this.color,this.mult);
        this.x = xyzColorRaw[0];
        this.y = xyzColorRaw[1];
        this.z = xyzColorRaw[2];
    }

    public Color getColor(){
        return color;
    }

    public double getMultiplier() {
        return mult;
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
