package com.github.technus.xyzrgbled.model.color;

import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.paint.Color;

public class ColorReadingXYZ {
    private final double x,y,z;
    private final boolean tooHigh,tooLow;
    private final SimpleDoubleProperty multiplier=new SimpleDoubleProperty(1D);
    private final ReadOnlyObjectWrapper<Color> color=new ReadOnlyObjectWrapper<>();

    public ColorReadingXYZ(){
        x=y=z=0;
        tooHigh=tooLow=false;
    }

    public ColorReadingXYZ(double x, double y, double z, double mult,boolean tooHigh,boolean tooLow) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.tooHigh = tooHigh;
        this.tooLow = tooLow;
        color.bind(new ObjectBinding<Color>() {
            {
                bind(multiplier);
            }
            @Override
            protected Color computeValue() {
                return XYZtoRGB2(x,y,z,multiplier.get());
            }
        });
        this.multiplier.set(mult);
    }

    private static javafx.scene.paint.Color XYZtoRGB2(double x, double y, double z, double mult) {

        x *= mult/100D;
        y *= mult/100D;
        z *= mult/100D;

        double r = 3.240479f * x - 1.53715f * y - 0.498535f * z;
        double g = -0.969256f * x + 1.875991f * y + 0.041556f * z;
        double b = 0.055648f * x - 0.204043f * y + 1.057311f * z;

        if ( r > 0.0031308 )
            r = 1.055f * ( (float)Math.pow(r, 0.4166f) ) - 0.055f;
        else
            r = 12.92f * r;

        if ( g > 0.0031308 )
            g = 1.055f * ( (float)Math.pow(g, 0.4166f) ) - 0.055f;
        else
            g = 12.92f * g;

        if ( b > 0.0031308 )
            b = 1.055f * ( (float)Math.pow(b, 0.4166f) ) - 0.055f;
        else
            b = 12.92f * b;

        return javafx.scene.paint.Color.color(clamp(r),clamp(g),clamp(b));
    }

    private static double clamp(double d){
        return Math.max(0,Math.min(1,d));
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

    public boolean isTooHigh() {
        return tooHigh;
    }

    public boolean isTooLow() {
        return tooLow;
    }

    public double getMultiplier() {
        return multiplier.get();
    }

    public SimpleDoubleProperty multiplierProperty() {
        return multiplier;
    }

    public void setMultiplier(double multiplier) {
        this.multiplier.set(multiplier);
    }

    public Color getColor() {
        return color.get();
    }

    public ReadOnlyObjectProperty<Color> colorProperty() {
        return color.getReadOnlyProperty();
    }
}
