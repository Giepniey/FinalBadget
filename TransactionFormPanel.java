import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public class TransactionFormPanel extends JPanel implements Refreshable {

    // ── Form components ─────────────────────────────────────────────────────
    private JComboBox<String>  typeCombo, categoryCombo, monthCombo;
    private JComboBox<Integer> dayCombo, yearCombo;
    private JSpinner           amountSpinner;
    private JTextField         descriptionField;
    private JTable             transactionTable;

    private List<Transaction>  transactions;
    private DataManager        dataManager;
    private DashboardPanel     parentPanel;
    private Transaction        selectedTransaction = null;
    private boolean            updatingDate = false;

    // ── Monochrome palette ──────────────────────────────────────────────────
    private static final Color BG        = new Color(245, 245, 245);
    private static final Color CARD      = Color.WHITE;
    private static final Color BORDER    = new Color(220, 220, 220);
    private static final Color TXT_MAIN  = new Color(30,  30,  30);
    private static final Color TXT_SUB   = new Color(110, 110, 110);
    private static final Color BTN_DEL   = new Color(160, 30,  30);
    private static final Color BTN_UPD   = new Color(60,  60,  60);
    private static final Color BTN_ADD   = new Color(40,  40,  40);

    private static final String[] MONTH_NAMES = {
        "January","February","March","April","May","June",
        "July","August","September","October","November","December"
    };
    private static final String[] EXPENSE_CATEGORIES = {
        "Food","Transport","Shopping","Entertainment","Utilities","Health","Education","Rent","Other"
    };
    private static final String[] INCOME_CATEGORIES = {
        "Salary","Freelance","Investment","Bonus","Gift","Other"
    };

    public TransactionFormPanel(DashboardPanel parentPanel) {
        this.parentPanel = parentPanel;
        this.dataManager  = new DataManager();
        this.transactions = dataManager.loadTransactions();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(BG);

        add(createFormPanel());
        add(Box.createVerticalStrut(16));
        add(createTablePanel());
        add(Box.createVerticalStrut(16));
    }

    // ── Form card ───────────────────────────────────────────────────────────
    private JPanel createFormPanel() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CARD);
        card.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, BORDER),
            new EmptyBorder(20, 24, 20, 24)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(7, 6, 7, 6);
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        // Type
        typeCombo = new JComboBox<>(new String[]{"Expense","Income"});
        styleCombo(typeCombo);
        typeCombo.addActionListener(e -> updateCategories());

        // Category
        categoryCombo = new JComboBox<>(EXPENSE_CATEGORIES);
        styleCombo(categoryCombo);

        // Date – 3 dropdowns
        JPanel datePanel = buildDatePanel();

        // Amount
        amountSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 999999.99, 0.01));
        styleSpinner(amountSpinner);

        // Description
        descriptionField = new JTextField(20);
        styleTextField(descriptionField);

        addRow(card, gbc, 0, "Type",        typeCombo);
        addRow(card, gbc, 1, "Date",        datePanel);
        addRow(card, gbc, 2, "Category",    categoryCombo);
        addRow(card, gbc, 3, "Amount (₱)",  amountSpinner);
        addRow(card, gbc, 4, "Description", descriptionField);

        // Buttons
        gbc.gridy     = 5;
        gbc.gridwidth = 2;
        gbc.gridx     = 0;
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnRow.setBackground(CARD);
        btnRow.add(makeBtn("Add",    BTN_ADD,  e -> addTransaction()));
        btnRow.add(makeBtn("Update", BTN_UPD,  e -> updateTransaction()));
        btnRow.add(makeBtn("Delete", BTN_DEL,  e -> deleteTransaction()));
        card.add(btnRow, gbc);

        return card;
    }

    private JPanel buildDatePanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setBackground(CARD);

        dayCombo   = new JComboBox<>();
        monthCombo = new JComboBox<>(MONTH_NAMES);
        yearCombo  = new JComboBox<>();

        // Populate years 2000–2026
        for (int y = 2000; y <= 2026; y++) yearCombo.addItem(y);

        // Set current date as default
        LocalDate today = LocalDate.now();
        monthCombo.setSelectedIndex(today.getMonthValue() - 1);
        yearCombo.setSelectedItem(today.getYear());
        refreshDays();
        dayCombo.setSelectedItem(today.getDayOfMonth());

        styleCombo(dayCombo);
        styleCombo(monthCombo);
        styleCombo(yearCombo);

        dayCombo.setPreferredSize(new Dimension(60,  28));
        monthCombo.setPreferredSize(new Dimension(110, 28));
        yearCombo.setPreferredSize(new Dimension(75,  28));

        monthCombo.addActionListener(e -> { if (!updatingDate) refreshDays(); });
        yearCombo.addActionListener(e  -> { if (!updatingDate) { refreshMonths(); refreshDays(); } });

        p.add(dayCombo);
        p.add(monthCombo);
        p.add(yearCombo);
        return p;
    }

    /** Rebuild month list, capping at current month when year == this year */
    private void refreshMonths() {
        updatingDate = true;
        int selYear = getSelectedYear();
        LocalDate today = LocalDate.now();
        int maxMonth = (selYear == today.getYear()) ? today.getMonthValue() : 12;

        Object prevMonth = monthCombo.getSelectedItem();
        monthCombo.removeAllItems();
        for (int m = 0; m < maxMonth; m++) monthCombo.addItem(MONTH_NAMES[m]);

        // restore if still valid
        if (prevMonth != null) {
            for (int i = 0; i < monthCombo.getItemCount(); i++) {
                if (monthCombo.getItemAt(i).equals(prevMonth)) {
                    monthCombo.setSelectedIndex(i);
                    break;
                }
            }
        }
        updatingDate = false;
    }

    /** Rebuild day list, capping at today when in current month+year */
    private void refreshDays() {
        updatingDate = true;
        int selYear  = getSelectedYear();
        int selMonth = monthCombo.getSelectedIndex() + 1;
        LocalDate today = LocalDate.now();

        int maxDay;
        if (selYear == today.getYear() && selMonth == today.getMonthValue()) {
            maxDay = today.getDayOfMonth();
        } else {
            maxDay = YearMonth.of(selYear, selMonth).lengthOfMonth();
        }

        Object prevDay = dayCombo.getSelectedItem();
        dayCombo.removeAllItems();
        for (int d = 1; d <= maxDay; d++) dayCombo.addItem(d);

        if (prevDay instanceof Integer && (Integer) prevDay <= maxDay)
            dayCombo.setSelectedItem(prevDay);
        updatingDate = false;
    }

    private int getSelectedYear() {
        Object y = yearCombo.getSelectedItem();
        return (y instanceof Integer) ? (Integer) y : LocalDate.now().getYear();
    }

    private LocalDate getSelectedDate() {
        int day   = dayCombo.getSelectedItem() != null ? (Integer) dayCombo.getSelectedItem() : 1;
        int month = monthCombo.getSelectedIndex() + 1;
        int year  = getSelectedYear();
        return LocalDate.of(year, month, day);
    }

    private void setDateFields(LocalDate d) {
        yearCombo.setSelectedItem(d.getYear());
        refreshMonths();
        monthCombo.setSelectedIndex(d.getMonthValue() - 1);
        refreshDays();
        dayCombo.setSelectedItem(d.getDayOfMonth());
    }

    // ── Table card ──────────────────────────────────────────────────────────
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD);
        panel.setBorder(new EmptyBorder(12, 20, 12, 20));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

        JLabel title = new JLabel("Transactions");
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(TXT_MAIN);
        title.setBorder(new EmptyBorder(0, 0, 10, 0));

        transactionTable = new JTable(new DefaultTableModel(
            new String[]{"Date","Type","Category","Amount","Description"}, 0)) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        transactionTable.setBackground(CARD);
        transactionTable.setForeground(TXT_MAIN);
        transactionTable.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        transactionTable.setRowHeight(26);
        transactionTable.setGridColor(new Color(240, 240, 240));
        transactionTable.getTableHeader().setBackground(new Color(248, 248, 248));
        transactionTable.getTableHeader().setForeground(TXT_SUB);
        transactionTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        transactionTable.setSelectionBackground(new Color(230, 230, 230));
        transactionTable.setSelectionForeground(TXT_MAIN);
        transactionTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && transactionTable.getSelectedRow() >= 0)
                loadToForm();
        });

        JScrollPane scroll = new JScrollPane(transactionTable);
        scroll.setBorder(new LineBorder(BORDER));
        updateTable();

        panel.add(title,  BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void updateTable() {
        DefaultTableModel m = (DefaultTableModel) transactionTable.getModel();
        m.setRowCount(0);
        for (Transaction t : transactions) {
            m.addRow(new Object[]{
                t.getDate(), t.getType(), t.getCategory(),
                String.format("₱%.2f", t.getAmount()), t.getDescription()
            });
        }
        // Color category column
        transactionTable.getColumnModel().getColumn(2).setCellRenderer(
            (table, value, isSelected, hasFocus, row, col) -> {
                JLabel lbl = new JLabel(value != null ? value.toString() : "");
                lbl.setOpaque(true);
                lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                lbl.setBorder(new EmptyBorder(0, 6, 0, 0));
                Color accent = CategoryColors.getColor(value != null ? value.toString() : "");
                lbl.setForeground(isSelected ? TXT_MAIN : accent);
                lbl.setBackground(isSelected ? new Color(230, 230, 230) : CARD);
                return lbl;
            }
        );
    }

    private void loadToForm() {
        int row = transactionTable.getSelectedRow();
        if (row < 0 || row >= transactions.size()) return;
        selectedTransaction = transactions.get(row);
        setDateFields(selectedTransaction.getDate());
        typeCombo.setSelectedItem(selectedTransaction.getType());
        updateCategories();
        categoryCombo.setSelectedItem(selectedTransaction.getCategory());
        amountSpinner.setValue(selectedTransaction.getAmount());
        descriptionField.setText(selectedTransaction.getDescription());
    }

    // ── CRUD actions ─────────────────────────────────────────────────────────
    private void addTransaction() {
        String desc = descriptionField.getText().trim();
        double amt  = (Double) amountSpinner.getValue();
        if (desc.isEmpty() || amt <= 0) {
            showError("Please enter a valid amount and description.");
            return;
        }
        Transaction t = new Transaction(
            getSelectedDate(),
            (String) categoryCombo.getSelectedItem(),
            amt, desc,
            (String) typeCombo.getSelectedItem()
        );
        parentPanel.addTransaction(t);
        JOptionPane.showMessageDialog(this, "Transaction added!", "Success",
            JOptionPane.INFORMATION_MESSAGE);
        resetForm();
    }

    private void updateTransaction() {
        if (selectedTransaction == null) {
            showError("Select a transaction to update.");
            return;
        }
        selectedTransaction.setDate(getSelectedDate());
        selectedTransaction.setCategory((String) categoryCombo.getSelectedItem());
        selectedTransaction.setAmount((Double) amountSpinner.getValue());
        selectedTransaction.setDescription(descriptionField.getText().trim());
        selectedTransaction.setType((String) typeCombo.getSelectedItem());
        dataManager.saveTransactions(transactions);
        parentPanel.refreshData();
        updateTable();
        resetForm();
        JOptionPane.showMessageDialog(this, "Transaction updated!", "Success",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteTransaction() {
        if (selectedTransaction == null) {
            showError("Select a transaction to delete.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete this?",
            "Confirm Delete",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        if (confirm == JOptionPane.OK_OPTION) {
            transactions.remove(selectedTransaction);
            dataManager.saveTransactions(transactions);
            parentPanel.refreshData();
            updateTable();
            resetForm();
        }
        // Cancel: do nothing
    }

    private void resetForm() {
        descriptionField.setText("");
        amountSpinner.setValue(0.0);
        typeCombo.setSelectedIndex(0);
        selectedTransaction = null;
        setDateFields(LocalDate.now());
        updateCategories();
        transactionTable.clearSelection();
    }

    private void updateCategories() {
        String type = (String) typeCombo.getSelectedItem();
        categoryCombo.removeAllItems();
        for (String c : "Income".equals(type) ? INCOME_CATEGORIES : EXPENSE_CATEGORIES)
            categoryCombo.addItem(c);
    }

    // ── Styling helpers ──────────────────────────────────────────────────────
    private void addRow(JPanel p, GridBagConstraints gbc, int row, String lbl, JComponent field) {
        gbc.gridy = row; gbc.gridwidth = 1; gbc.gridx = 0; gbc.weightx = 0.25;
        JLabel label = new JLabel(lbl);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(TXT_SUB);
        p.add(label, gbc);
        gbc.gridx = 1; gbc.weightx = 0.75;
        p.add(field, gbc);
        gbc.gridx = 0;
    }

    private <T> void styleCombo(JComboBox<T> c) {
        c.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        c.setBackground(CARD);
        c.setForeground(TXT_MAIN);
        c.setBorder(new LineBorder(BORDER));
    }

    private void styleSpinner(JSpinner s) {
        s.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        ((JSpinner.DefaultEditor) s.getEditor()).getTextField().setFont(new Font("Segoe UI", Font.PLAIN, 12));
    }

    private void styleTextField(JTextField f) {
        f.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        f.setBorder(new CompoundBorder(new LineBorder(BORDER), new EmptyBorder(4, 6, 4, 6)));
    }

    private JButton makeBtn(String text, Color bg, java.awt.event.ActionListener al) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setBorder(new EmptyBorder(8, 20, 8, 20));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        btn.addActionListener(al);
        return btn;
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void refresh(List<Transaction> transactions) {
        this.transactions = transactions;
        updateTable();
    }
}
