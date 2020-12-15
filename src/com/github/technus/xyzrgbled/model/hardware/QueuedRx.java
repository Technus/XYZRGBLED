package com.github.technus.xyzrgbled.model.hardware;

public class QueuedRx{
    public final byte[] data;
    public final int delayMs;

    public QueuedRx(byte[] data, int delayMs) {
        this.data = data;
        this.delayMs = delayMs;
    }
}
