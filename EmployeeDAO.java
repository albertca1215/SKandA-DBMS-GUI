package ska;

import ska.ui.MainFrame;
import javax.swing.*;

/**
 * Main application launcher for the SK&A Database System.
 * Entry point for the entire Swing-based GUI application.
 */
public class SKADatabaseApp {

    public static void main(String[] args) {
        // Schedule GUI creation on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
