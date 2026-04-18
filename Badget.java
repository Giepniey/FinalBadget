import javax.swing.*;
import java.io.File;

public class Badget {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            File dataDir = new File("data");
            if (!dataDir.exists()) {
                dataDir.mkdir();
            }
            
            JFrame frame = new JFrame("Badget - Budget & Expense Tracker");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 700);
            frame.setLocationRelativeTo(null);
            frame.setResizable(true);
            
            DashboardPanel dashboard = new DashboardPanel();
            frame.add(dashboard);
            
            frame.setVisible(true);
        });
    }
}
