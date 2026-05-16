package ska.ui;

import ska.db.DatabaseManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Main application window for the SK&A Database System.
 * Uses a JTabbedPane to house all functional panels.
 */
public class MainFrame extends JFrame {

    private JTabbedPane tabbedPane;
    private JLabel statusLabel;
    private EmployeePanel employeePanel;
    private ProjectPanel projectPanel;
    private ManHoursPanel manHoursPanel;
    private ProfitabilityPanel profitabilityPanel;
    private PrincipalLicensePanel principalLicensePanel;
    private ReportsPanel reportsPanel;

    public MainFrame() {
        setTitle("SK&A Structural Engineers — Project Database");
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                int confirm = JOptionPane.showConfirmDialog(
                    MainFrame.this,
                    "Exit the SK&A Database System?",
                    "Confirm Exit",
                    JOptionPane.YES_NO_OPTION
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    DatabaseManager.closeConnection();
                    System.exit(0);
                }
            }
        });

        buildUI();
        checkDatabaseConnection();
    }

    private void buildUI() {
        // ── Header banner ──────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30, 58, 95));
        header.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JLabel title = new JLabel("SK&A Structural Engineers, PLLC");
        title.setFont(new Font("Georgia", Font.BOLD, 20));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Project & Employee Database System");
        subtitle.setFont(new Font("Georgia", Font.ITALIC, 13));
        subtitle.setForeground(new Color(180, 200, 220));

        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setOpaque(false);
        titlePanel.add(title);
        titlePanel.add(subtitle);
        header.add(titlePanel, BorderLayout.WEST);

        // ── Tabbed pane ────────────────────────────────────────────────
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.PLAIN, 13));

        employeePanel = new EmployeePanel(this);
        projectPanel = new ProjectPanel(this);
        manHoursPanel = new ManHoursPanel(this);
        profitabilityPanel = new ProfitabilityPanel(this);
        principalLicensePanel = new PrincipalLicensePanel(this);
        reportsPanel = new ReportsPanel(this);
        
        tabbedPane.addTab("Employees",    employeePanel);
        tabbedPane.addTab("Projects",     projectPanel);
        tabbedPane.addTab("Man-Hours",    manHoursPanel);
        tabbedPane.addTab("Profitability", profitabilityPanel);
        tabbedPane.addTab("Principal Licenses", principalLicensePanel);
        tabbedPane.addTab("Reports",      reportsPanel);

        // ── Status bar ─────────────────────────────────────────────────
        statusLabel = new JLabel("  Ready");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        statusLabel.setForeground(new Color(80, 80, 80));

        Container cp = getContentPane();
        cp.setLayout(new BorderLayout());
        cp.add(header,      BorderLayout.NORTH);
        cp.add(tabbedPane,  BorderLayout.CENTER);
        cp.add(statusLabel, BorderLayout.SOUTH);
    }

    /** Updates the status bar message. Call from any panel. */
    public void setStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText("  " + message);
        }
    }

    /** Refreshes dropdowns across all panels to sync data changes. */
    public void refreshAllPanelDropdowns() {
        if (projectPanel != null) {
            projectPanel.refreshEmployeeDropdowns();
        }
        if (manHoursPanel != null) {
            manHoursPanel.refreshProjectAndEmployeeDropdowns();
        }
        if (profitabilityPanel != null) {
            profitabilityPanel.refreshProjectDropdown();
        }
        if (principalLicensePanel != null) {
            principalLicensePanel.refreshPrincipalDropdown();
        }
        if (reportsPanel != null) {
            reportsPanel.refreshReportDropdowns();
        }
    }

    /** Refresh the Man-Hours table in its panel. */
    public void refreshManHoursPanel() {
        try {
            java.lang.reflect.Method m = manHoursPanel.getClass().getMethod("refreshManHoursList");
            m.invoke(manHoursPanel);
        } catch (Exception ignored) {}
    }

    private void checkDatabaseConnection() {
        if (DatabaseManager.testConnection()) {
            setStatus("Database connected.");
        } else {
            setStatus("Database NOT connected — check DatabaseManager settings.");
            JOptionPane.showMessageDialog(this,
                "Could not connect to the database.\n" +
                "Please edit the URL, USER, and PASSWORD in DatabaseManager.java\n" +
                "and ensure your MySQL server is running.",
                "Connection Error",
                JOptionPane.WARNING_MESSAGE
            );
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}