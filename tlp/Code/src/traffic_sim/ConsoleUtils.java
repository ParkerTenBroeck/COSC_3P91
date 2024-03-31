package traffic_sim;

import java.awt.*;
import java.io.*;

public class ConsoleUtils {



    public static void enterRawMode() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            show();
            String[] cmd = {"/bin/sh", "-c", "stty -raw </dev/tty"};
            try {
                Runtime.getRuntime().exec(cmd).waitFor();
            } catch (Exception ignore) {}
         }));
        String[] cmd = {"/bin/sh", "-c", "stty raw -echo </dev/tty"};
        try{
            Runtime.getRuntime().exec(cmd).waitFor();
            out = new PrintWriter(System.out, false);
        }catch (Exception e){throw new RuntimeException(e);}
    }

    private static InputStream in = System.in;
    private static PrintWriter out = new PrintWriter(System.out, true);//new PrintWriter(System.out, false);

    public static boolean hasNext() throws IOException {
        return in.available() > 0;
    }


    public static class Key{
        boolean ctrl = false;
        boolean shift = false;
        boolean alt = false;
        int code;
        char key;
        public Key(){

        }
    }

    public static int nextCode() throws IOException{
        var read = in.read();
        if(read == 3 || read == -1)System.exit(1);
        return read;
    }
    public static int nextChar() throws IOException {
        return nextCode();
    }

    public static void fullClear(){
        resetStyle();
        out.write("\033[2J");
    }

    public static void clear(){
        resetStyle();
        out.write("\033[J");
    }
    public static void println(String msg) {
        out.write(msg);
        out.write("\033[0E");
    }

    private static String getSize(){
        try{
            var process = new ProcessBuilder("stty","size").redirectInput(ProcessBuilder.Redirect.INHERIT).start();
            var read = process.getInputStream().readAllBytes();
            var str = new String(read).trim();
            return str.isEmpty() ? "24 80" : str;
        }catch (Exception e){throw new RuntimeException(e);}
    }

    public static int getWidth(){
        return Integer.parseInt(getSize().split(" ")[1].trim());
    }
    public static int getHeight(){
        return Integer.parseInt(getSize().split(" ")[0].trim());
    }

    public static void println() {
        out.write("\033[0E");
    }

    public static void applyStyle(StyleI... style){
        out.write("\033[");
        for(int i = 0; i < style.length; i ++){
            style[i].writePartial(out);
            if(i + 1 == style.length){
                out.write('m');
            }else{
                out.write(';');
            }
        }
    }

    public static void stylePrintln(String msg, StyleI... style) {
        applyStyle(style);
        out.write(msg);
        out.write("\033[0E");
    }

    public static void moveCursor(int x, int y) {
        out.write("\033["+y+";"+x+"H");
        out.write("\033["+y+";"+x+"f");
    }

    public static void resetStyle(){
        out.write("\033[0m");
    }

    public static void setBold(){
        out.write("\033[1m");
    }

    public static void setUnderline(){
        out.write("\033[4m");
    }

    public static void setBlinking(){
        out.write("\033[5m");
    }

    public static void hideCursor(){
        out.write("\033[?25l");
    }

    public static void print(String s) {
        out.write(s);
    }

    public interface StyleI {
        void writePartial(PrintWriter out);
    }

    private static class RGB implements StyleI{
        final byte r;
        final byte g;
        final byte b;
        public RGB(Color color){
            this.r = (byte)color.getRed();
            this.g = (byte)color.getGreen();
            this.b = (byte)color.getBlue();
        }
        public RGB(int r, int g, int b){
            this.r = (byte)r;
            this.g = (byte)g;
            this.b = (byte)b;
        }

        @Override
        public void writePartial(PrintWriter out) {
            out.print(((int)r) & 0xff);
            out.write(";");
            out.print(((int)g) & 0xff);
            out.write(";");
            out.print(((int)b) & 0xff);
        }
    }

    private final static class RGBF extends RGB implements StyleI{

        public RGBF(Color color) {
            super(color);
        }

        public RGBF(int r, int g, int b) {
            super(r, g, b);
        }

        @Override
        public void writePartial(PrintWriter out) {
            out.write("38;" );
            super.writePartial(out);
        }
    }

    private final static class RGBB extends RGB implements StyleI{

        public RGBB(Color color) {
            super(color);
        }

        public RGBB(int r, int g, int b) {
            super(r, g, b);
        }

        @Override
        public void writePartial(PrintWriter out) {
            out.write("48;" );
            super.writePartial(out);
        }
    }

    public enum Style implements StyleI{
        Bold(1),
        UnBold(22),
        Italic(3),
        UnItalic(23),
        Underline(4),
        UnUnderline(24),
        Blinking(5),
        UnBlinking(25);
        public final int code;
        Style(int code){
            this.code = code;
        }

        @Override
        public void writePartial(PrintWriter out) {
            out.print(code);
        }
    }

    public enum BasicForeground implements StyleI {
        Black(30),
        Red(31),
        Green(32),
        Yellow(33),
        Blue(34),
        Magenta(35),
        Cyan(36),
        White(37),
        Default(39),
        Reset(0);

        public final int code;
        BasicForeground(int code){
            this.code = code;
        }

        @Override
        public void writePartial(PrintWriter out) {
            out.print(this.code);
        }
    }

    public enum BasicBackground implements StyleI {
        Black(40),
        Red(41),
        Green(42),
        Yellow(43),
        Blue(44),
        Magenta(45),
        Cyan(46),
        White(47),
        Default(49),
        Reset(0);

        public final int code;
        BasicBackground(int code){
            this.code = code;
        }

        @Override
        public void writePartial(PrintWriter out) {
            out.print(this.code);
        }
    }

    public static void showCursor(){
        out.write("\033[?25h");
    }

    public static void show(){
        out.flush();
    }
}
