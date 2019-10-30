package dwang.meiousaveloader.view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;

public class SwingUtils {
    public static void setLocationToScreenCenter(Frame frame) {
        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    public static void setMinimumSizeRelativeToScreen(Component frame, int factor) {
        GraphicsEnvironment curGraphicsEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Dimension defaultSize = new Dimension(curGraphicsEnv.getMaximumWindowBounds().width/factor,
                curGraphicsEnv.getMaximumWindowBounds().height/factor);
        frame.setMinimumSize(defaultSize);
    }

    public static int getHeightRelativeToScreen(int factor) {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().height/factor;
    }

    public static int getWidthRelativeToScreen(int factor) {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().width/factor;
    }

}
