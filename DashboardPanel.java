import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

public class DashboardPanel extends JPanel {
    private JTabbedPane    tabbedPane;
    private List<Transaction> transactions;
    private DataManager    dataManager;

    private static final Color BG      = new Color(245, 245, 245);
    private static final Color HEADER  = Color.WHITE;
    private static final Color BORDER  = new Color(220, 220, 220);
    private static final Color TXT     = new Color(30,  30,  30);
    private static final Color BTN_BG  = new Color(40,  40,  40);

    public DashboardPanel() {
        this.dataManager  = new DataManager();
        this.transactions = dataManager.loadTransactions();

        setLayout(new BorderLayout());
        setBackground(BG);

        add(createHeader(), BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(HEADER);
        tabbedPane.setForeground(TXT);
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabbedPane.setBorder(new EmptyBorder(0, 0, 0, 0));

        tabbedPane.addTab("Dashboard",       new OverviewPanel(transactions));
        tabbedPane.addTab("Add Transaction", new TransactionFormPanel(this));
        tabbedPane.addTab("Reports",         new ReportsPanel(transactions));
        tabbedPane.addTab("Categories",      new CategoriesPanel(transactions));

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER);
        header.setBorder(new MatteBorder(0, 0, 1, 0, BORDER));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 14));
        left.setBackground(HEADER);
        JLabel title = new JLabel("Badget");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(TXT);
        left.add(title);

        JLabel sub = new JLabel("Budget & Expense Tracker");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(130, 130, 130));
        left.add(sub);
        header.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 12));
        right.setBackground(HEADER);
        JButton refreshBtn = new JButton("↻  Refresh");
        refreshBtn.setBackground(BTN_BG);
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshBtn.setBorder(new EmptyBorder(7, 16, 7, 16));
        refreshBtn.setFocusPainted(false);
        refreshBtn.setOpaque(true);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> refreshData());
        right.add(refreshBtn);
        header.add(right, BorderLayout.EAST);

        return header;
    }

    public void refreshData() {
        transactions = dataManager.loadTransactions();
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            Component comp = tabbedPane.getComponentAt(i);
            if (comp instanceof Refreshable)
                ((Refreshable) comp).refresh(transactions);
        }
    }

    public void addTransaction(Transaction t) {
        transactions.add(t);
        dataManager.saveTransactions(transactions);
        refreshData();
    }

    public List<Transaction> getTransactions() { return transactions; }
}
