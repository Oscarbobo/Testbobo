package com.ubox.card.util.device;

import java.util.Arrays;

public class DeviceByteBuffer {

    private byte[] value;

    private int count;

    public DeviceByteBuffer() {
        value = new byte[16];
        count = 0;
    }
    
    public void reset() {
    	count = 0;
    }

    public DeviceByteBuffer append(int b) {
        int newCount = count + 1;
        if(newCount > value.length) {
            expandCapacity(newCount);
        }
        value[count ++] = (byte)b;

        return this;
    }

    public DeviceByteBuffer append(byte[] bytes, int offset, int len) {
        if(bytes == null) {
            throw new IllegalArgumentException("argument is NULL.");
        }

        if(offset < 0 || len < 0) {
            throw new IllegalArgumentException("offset or len illegal.");
        }

        int newCount = count + len;
        if(newCount > value.length) {
            expandCapacity(newCount);
        }
        for(int i = offset; i < len; i++) {
            value[count ++] = bytes[i];
        }
        count = newCount;

        return this;
    }

    public byte[] toByteArray() {
        return Arrays.copyOf(value, count);
    }

    public int length() {
        return count;
    }

    private void expandCapacity(int mininumCapacity) {
        int newCount = (value.length + 1) * 2;
        if(newCount < 0) {
            newCount = Integer.MAX_VALUE;
        } else if(mininumCapacity > newCount) {
            newCount = mininumCapacity;
        }

        value = Arrays.copyOf(value, newCount);
    }

}