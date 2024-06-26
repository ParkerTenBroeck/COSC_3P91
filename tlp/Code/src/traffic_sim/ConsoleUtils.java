package traffic_sim;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;

@SuppressWarnings("unused")
public class ConsoleUtils {


    /**
     * Puts the terminal in raw mode and setup a shutdown hook to exit raw mode
     */
    public static void enterRawMode() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            showCursor();
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

    /**
     * @return if the console has new data we can read
     */
    public static boolean hasNext() throws IOException {
        return in.available() > 0;
    }


    /**
     * Represents a single key press alongside modifier keys
     */
    public static class Key{
        public boolean ctrl = false;
        public boolean shift = false;
        public boolean alt = false;
        /**
         * Java key code for the event
         */
        public int code;
        /**
         * A char that represents the key. 0 if no char exists for it.
         */
        public char key;
        public Key(int code){
            this.code = code;
        }
        @Override
        public String toString(){

            return KeyEvent.getKeyText(this.code) + "("+this.code+")"
                    + (this.key != 0?(String.format("'%s'", this.key)): " ")
                    + (this.ctrl?" Ctlr ":"")
                    + (this.alt?" Alt ":"")
                    + (this.shift?" Shift; ":"");
        }
    }

    /**
     * @return The next code point of the input stream. It is just a single byte
     */
    public static int nextCode() throws IOException{
        var read = in.read();
        if(read == 3 || read == -1)System.exit(1);
        return read;
    }

    /**
     * @param key Modifies the key modifiers such that they match the expected modifiers from a sequence starting in 59
     */
    private static void read59Seq(Key key) throws IOException{
        var kind = nextCode();
        kind %= 10;
        key.ctrl = kind >= 3;
        key.alt = kind == 1 || kind == 2 || kind == 5 || kind == 6;
        key.shift = kind == 0 || kind == 2 || kind == 4 || kind == 6;
    }

    /**
     * Checks if a key is a regular key. and if it adjust the key, code, and modifiers
     * depending on what it is.
     *
     * @param key   The key we want to check
     */
    private static void checkRegularChar(Key key){
        if(key.code >= 1  && key.code <= 26){
            // control keys. tab, newline
            if(key.code == 9){
                key.key = '\t';
            }else if(key.code == 8){
                key.ctrl = true;
            }else if(key.code == 13){
                key.key = '\n';
                key.code = key.key;
            }else{
                key.ctrl = true;
                key.key = (char) ('A' - 1 + key.code);
                key.code = key.key;
            }
        }else if(key.code == 127){
            // backspace
            key.code = 8;
        }else if(key.code >= 'a' && key.code <= 'z'){
            // lowercase alphabet
            key.shift = false;
            key.key = (char) key.code;
            key.code = key.code - 'a' + 'A';
        }else if(key.code >= 'A' && key.code <= 'Z'){
            // uppercase alphabet
            key.shift = true;
            key.key = (char) key.code;
        }else{
            // symbols and numbers.
            switch(key.code){
                case '0', ')' -> sCase('0', ')', key);
                case '1', '!' -> sCase('1', '!', key);
                case '2', '@' -> sCase('2', '@', key);
                case '3', '#' -> sCase('3', '#', key);
                case '4', '$' -> sCase('4', '$', key);
                case '5', '%' -> sCase('5', '%', key);
                case '6', '^' -> sCase('6', '^', key);
                case '7', '&' -> sCase('7', '&', key);
                case '8', '*' -> sCase('8', '*', key);
                case '9', '(' -> sCase('9', '(', key);

                case ';', ':' -> sCase(';', ':', key);

                case '-', '_' -> sCase('-', '_', key);
                case '=', '+' -> sCase('=', '+', key);
                case '[', '{' -> sCase('[', '{', key);
                case ']', '}' -> sCase(']', '}', key);
                case '\\', '|' -> sCase( '\\', '|', key);
                case '\'', '"' -> sCase( '\'', '"', key);
                case ',', '<' -> sCase(',', '<', key);
                case '.', '>' -> sCase(';', ':', key);
                case '/', '?' -> sCase('/', '?', key);

                case '`', '~' -> cCase('`', '~', key, 192);
            }
        }
    }

    /**
     * Checks if this key is a upper or lower case with a simple mapping between the char and code
     *
     * @param lower The lower case (non shift) char for this key
     * @param upper The upper case (with shift) char for this key
     * @param key   The key pressed
     */
    private static void sCase(char lower, char upper, Key key) {
        key.shift = key.code == upper;
        key.key = (char) key.code;
        key.code = lower;
    }
    /**
     * Checks if this key is a upper or lower case with a provided mapping between the char and code
     *
     * @param lower The lower case (non shift) char for this key
     * @param upper The upper case (with shift) char for this key
     * @param key   The key pressed
     */
    private static void cCase(char lower, char upper, Key key, int code) {
        key.shift = key.code == upper;
        key.key = (char) key.code;
        key.code = code;
    }

    /**
     * @return  Gets the next key press from the input stream
     */
    public static Key nextKey() throws IOException {
        // this was one of the worst fucking things to write jesus christ who
        // the fuck made this
        var key = new Key(nextCode());
        if(key.code == 27 && hasNext()){
            key.alt = true;
            key.code = nextCode();
            if(key.code == 91 && hasNext()){
                key.alt = false;
                key.code = nextCode();

                if(key.code == 49 && hasNext()){
                    key.code = nextCode();

                    var next = key.code;
                    switch (key.code){
                        // F5/6/7/8
                        case 53 -> key.code = 116;
                        case 55 -> key.code = 117;
                        case 56 -> key.code = 118;
                        case 57 -> key.code = 119;
                    }
                    switch (next){
                        // F5/6/7/8
                        case 53, 55, 56, 57 -> next = nextCode();
                    }

                    if(next == 59) {
                        read59Seq(key);
                        next = nextCode();
                    }

                    if(next == 126) return key;
                    key.code = next;
                }
                switch(key.code){
                    // arrow keys
                    case 65 -> key.code = 38;
                    case 66 -> key.code = 40;
                    case 67 -> key.code = 39;
                    case 68 -> key.code = 37;

                    //home
                    case 72 -> key.code = 36;
                    //end
                    case 70 -> key.code = 35;

                    case 50, 51, 53, 54 -> {
                        var next = nextCode();
                        if(next == 59) {
                            read59Seq(key);
                            next = nextCode();
                        }
                        if(next == 126){
                            switch(key.code){
                                case 50 -> key.code = 0x9B;
                                case 51 -> key.code = 0x7F;
                                case 53 -> key.code = 33;
                                case 54 -> key.code = 34;
                            }
                        }else{
                            switch(next){
                                // F9/10/11/12
                                case 48 -> key.code = 120;
                                case 49 -> key.code = 121;
                                case 51 -> key.code = 122;
                                case 52 -> key.code = 123;
                            }
                            next = nextCode();
                            if(next == 59) {
                                read59Seq(key);
                                next = nextCode();
                            }
                            // this really should be 126 but who cares right?
                        }
                    }
                    case 90 -> {
                        key.code = 9;
                        key.key = '\t';
                        key.shift = true;
                    }

                    // F1, F2, F3, F4
                    case 80, 81, 82, 83 -> key.code = 112 + key.code - 80;
                }
            }else if(key.code == 79 && hasNext()){
                key.alt = false;
                key.code = nextCode();

                switch(key.code){
                    // F1, F2, F3, F4
                    case 80, 81, 82, 83 -> key.code = 112 + key.code - 80;
                }
            }else{
                checkRegularChar(key);
            }
        }else{
            checkRegularChar(key);
        }
        return key;
    }

    /**
     * Filly clear the console
     */
    public static void fullClear(){
        resetStyle();
        out.write("\033[2J");
    }

    /**
     * Clear the console... sorta
     */
    public static void clear(){
        resetStyle();
        out.write("\033[J");
    }

    /**
     * Print to the console moving the cursor to the start of the next line
     * @param msg   The message to print to the console
     */
    public static void println(String msg) {
        out.write(msg);
        out.write("\033[0E");
    }

    /**
     *
     * @return  the size of the console as a string, represented as x y
     */
    private static String getSize(){
        try{
            var process = new ProcessBuilder("stty","size").redirectInput(ProcessBuilder.Redirect.INHERIT).start();
            var read = process.getInputStream().readAllBytes();
            var str = new String(read).trim();
            return str.isEmpty() ? "24 80" : str;
        }catch (Exception e){throw new RuntimeException(e);}
    }

    /**
     * @return  The width of the console
     */
    public static int getWidth(){
        return Integer.parseInt(getSize().split(" ")[1].trim());
    }

    /**
     * @return  The height of the console
     */
    public static int getHeight(){
        return Integer.parseInt(getSize().split(" ")[0].trim());
    }

    /**
     * Moves the cursor to the next line st the start of the row
     */
    public static void println() {
        out.write("\033[0E");
    }

    /**
     * @param style The style(s) to apply to the console
     */
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

    /**
     * See {@code   ConsoleUtils.println}
     *
     * @param msg the message to print
     * @param style the style(s) to apply to the console
     */
    public static void stylePrintln(String msg, StyleI... style) {
        applyStyle(style);
        out.write(msg);
        out.write("\033[0E");
    }

    /**
     * Move the cursor to (x,y) where 1,1 is the top left point
     *
     * @param x the row we want to move to
     * @param y The column we want to move to
     */
    public static void moveCursor(int x, int y) {
        out.write("\033["+y+";"+x+"H");
        out.write("\033["+y+";"+x+"f");
    }

    /**
     * Resets the current style
     */
    public static void resetStyle(){
        out.write("\033[0m");
    }

    /**
     * Hides the cursor
     */
    public static void hideCursor(){
        out.write("\033[?25l");
    }

    /**
     * Shows the cursor
     */
    public static void showCursor(){
        out.write("\033[?25h");
    }

    /**
     * @param msg The message to print to the console.
     */
    public static void print(String msg) {
        out.write(msg);
    }

    /**
     * Show all the contents printed to the console at once
     */
    public static void show(){
        out.flush();
    }

    /**
     * Represents a style we can apply to the console
     */
    public interface StyleI {
        void writePartial(PrintWriter out);
    }

    /**
     * A color represented by r,g,b values
     */
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

    /**
     * A foreground color represented with RGB
     */
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

    /**
     * A background color represented with RGB
     */
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

    /**
     * A kind of style that can be applied
     */
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
    
    /**
     * A background color represented with basic terminal colors
     */
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

    /**
     * A foreground color represented with basic terminal colors
     */
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
}
