import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import javax.imageio.ImageIO;
class Screen {
    private StringBuilder buffer;
    private int W;
    private int H;
    public Screen() {
        W = 80;
        H = 60;
        buffer = new StringBuilder().repeat("\033[38;2;000;000;000m\033[48;2;000;000;000m\u2580\033[m".repeat(W)+'\n', H/2);
    }
    public Screen(int width, int height) {
        W = width;
        H = height;
        buffer = new StringBuilder().repeat("\033[38;2;000;000;000m\033[48;2;000;000;000m\u2580\033[m".repeat(W)+'\n', H/2);
    }
    public void set(int rgb, int i, int j) throws IndexOutOfBoundsException {
        final int[] R = {7,8,9};
        final int[] G = {11,12,13};
        final int[] B = {15,16,17};
        if (i>=H) throw new IndexOutOfBoundsException(W);
        if (j>=W) throw new IndexOutOfBoundsException(H);
        int index = (42*W+1)*(i/2) + 42*j + ((i%2==1)? 19 : 0);
        int r = (rgb>>16)&0xff;
        int g = (rgb>>8)&0xff;
        int b = rgb&0xff;
        buffer.setCharAt(index+R[0],(char)(48+(r/100)%10));
        buffer.setCharAt(index+R[1],(char)(48+(r/10)%10));
        buffer.setCharAt(index+R[2],(char)(48+r%10));
        buffer.setCharAt(index+G[0],(char)(48+(g/100)%10));
        buffer.setCharAt(index+G[1],(char)(48+(g/10)%10));
        buffer.setCharAt(index+G[2],(char)(48+g%10));
        buffer.setCharAt(index+B[0],(char)(48+(b/100)%10));
        buffer.setCharAt(index+B[1],(char)(48+(b/10)%10));
        buffer.setCharAt(index+B[2],(char)(48+b%10));
    }
    public void display() {
        System.out.println(buffer.toString());
    }
}
public class Renderer extends Thread {
    private static int W = 80;
    private static int H = 60;
    private static Screen screen;
    public static void render(BufferedImage img) {
        Instant start = Instant.now();
        for (int i=0; i<img.getHeight(); ++i)
            for (int j=0; j<img.getWidth(); ++j)
                screen.set(img.getRGB(j, i), i, j);
        Instant end = Instant.now();
        screen.display();
        System.out.println("Rendered in: " + Duration.between(start,end).toMillis() + "ms");
    }
    public static void main(String[] args) throws IOException {
        if (ImageIO.read(new File(args[0]))!=null) {
            BufferedImage img = ImageIO.read(new File(args[0]));
            Dimension dim = new Dimension(img.getWidth(),img.getHeight());
            if (img.getWidth()>W) {
                dim.width = W;
                dim.height = (dim.width * img.getHeight()) / img.getWidth();
            }
            if (img.getHeight()>H) {
                dim.height = H;
                dim.width = (dim.height * img.getWidth()) / img.getHeight();
            } 
            screen = new Screen(dim.width, dim.height);
            Image tmp = img.getScaledInstance(dim.width,dim.height,BufferedImage.SCALE_SMOOTH);
            if (tmp instanceof BufferedImage)
                img = (BufferedImage) tmp;
            else {
                img = new BufferedImage(dim.width,dim.height,BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = img.createGraphics();
                g.drawImage(tmp, 0, 0, dim.width, dim.height, null);
                g.dispose();
            }
            render(img);
        }
    }
}
