package traffic_sim.networking;

import java.io.OutputStream;

/**
 * AN output stream that writes directly to a byte buffer
 */
public class BufferedWriter extends OutputStream {
    private int index = 0;
    private final byte[] data;

    /**
     * @return  Gets ths size (number of bytes) written to the internal buffer
     */

    public int getSize(){
        return this.index;
    }

    /**
     * @return  Gets a byte array of everything. including unused data.
     */
    public byte[] getAllData(){
        return this.data;
    }

    /**
     * Restarts the size of the buffer.
     */
    public void clear() {
        this.index = 0;
    }

    /**
     * Construct a buffer with a default size of 64k
     */
    public BufferedWriter(){
        this.data = new byte[1 << 16];
    }

    /**
     * @param size  How large(in bytes) the buffer should be
     */
    public BufferedWriter(int size){
        this.data = new byte[size];
    }

    /**
     * Construct a buffer with a existing byte buffer
     *
     * @param data  The existing buffer
     */
    public BufferedWriter(byte[] data){
        this.data = data;
    }

    /**
     * Construct a buffer with a existing byte buffer starting at index index
     *
     * @param data  The existing buffer
     * @param index The index we want to start writing to
     */
    public BufferedWriter(byte[] data, int index){
        this.data = data;
        this.index = index;
    }

    /**
     * @param v The byte to write to the buffer
     */
    public void writeByte(byte v) {
        this.data[this.index++] = v;
    }

    /**
     * @param v The short to write to the buffer
     */
    public void writeShort(short v) {
        this.data[this.index++] = (byte) (v >> 8);
        this.data[this.index++] = (byte) v;
    }

    /**
     * @param v The int to write to the buffer
     */
    public void writeInt(int v) {
        this.data[this.index++] = (byte) (v >> 24);
        this.data[this.index++] = (byte) (v >> 16);
        this.data[this.index++] = (byte) (v >> 8);
        this.data[this.index++] = (byte) v;
    }

    /**
     * @param v The float to write to the buffer
     */
    public void writeFloat(float v) {
        this.writeInt(Float.floatToRawIntBits(v));
    }

    /**
     * @param v The long to write to the buffer
     */
    public void writeLong(long v) {
        this.data[this.index++] = (byte) (v >> 56);
        this.data[this.index++] = (byte) (v >> 48);
        this.data[this.index++] = (byte) (v >> 40);
        this.data[this.index++] = (byte) (v >> 32);
        this.data[this.index++] = (byte) (v >> 24);
        this.data[this.index++] = (byte) (v >> 16);
        this.data[this.index++] = (byte) (v >> 8);
        this.data[this.index++] = (byte) v;
    }

    /**
     * @param v The double to write to the buffer
     */
    public void writeDouble(double v) {
        this.writeLong(Double.doubleToRawLongBits(v));
    }

    /**
     * @param s The string to write to the buffer
     */
    public void writeString(String s) {
        var bytes = s.getBytes();
        this.writeShort((short)bytes.length);
        System.arraycopy(bytes, 0, this.data, this.index, bytes.length);
        this.index += bytes.length;
    }

    @Override
    public void write(int b) {
        this.data[this.index++] = (byte)b;
    }
}
