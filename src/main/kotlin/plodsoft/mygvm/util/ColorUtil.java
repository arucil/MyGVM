package plodsoft.mygvm.util;

public class ColorUtil {
    /**
     * 解析#rrggbb或#rgb格式的颜色字符串
     * @param color
     * @return 0xrrggbb
     * @throws IllegalArgumentException
     */
    public static int parse(String color) {
        if (!color.matches("^#([a-fA-F0-9]{3}|[a-fA-F0-9]{6})$")) {
            throw new IllegalArgumentException("Invalid color string");
        }

        if (color.length() == 4) {
            int r = Character.digit(color.charAt(1), 16);
            int g = Character.digit(color.charAt(2), 16);
            int b = Character.digit(color.charAt(3), 16);
            return r << 20 | r << 16 | g << 12 | g << 8 | b << 4 | b;
        } else {
            int r = Character.digit(color.charAt(1), 16) << 4 | Character.digit(color.charAt(2), 16);
            int g = Character.digit(color.charAt(3), 16) << 4 | Character.digit(color.charAt(4), 16);
            int b = Character.digit(color.charAt(5), 16) << 4 | Character.digit(color.charAt(6), 16);
            return r << 16 | g << 8 | b;
        }
    }
}
