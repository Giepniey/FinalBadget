import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.time.YearMonth;
import java.util.*;
import java.util.List;

public class OverviewPanel extends JPanel implements Refreshable {
    private JLabel    totalIncomeLabel, totalExpenseLabel, balanceLabel;
    private JTextArea historyArea;
    private MonthlyBarChart barChart;
    private List<Transaction> transactions;

    private static final Color BG       = new Color(245, 245, 245);
    private static final Color CARD     = Color.WHITE;
    private static final Color BORDER   = new Color(220, 220, 220);
    private static final Color TXT_MAIN = new Color(30,  30,  30);
    private static final Color TXT_SUB  = new Color(110, 110, 110);
    private static final Color INCOME_C = new Color(40,  140, 80);
    private static final Color EXP_C    = new Color(170, 40,  40);
    private static final Color BAL_POS  = new Color(40,  100, 160);
    private static final Color BAL_NEG  = new Color(160, 40,  40);

    public OverviewPanel(List<Transaction> transactions) {
        this.transactions = transactions;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(BG);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        add(createMonthBar());
        add(Box.createVerticalStrut(12));
        add(createStatsRow());
        add(Box.createVerticalStrut(12));
        add(createMiddleRow());   // chart + history side-by-side
        add(Box.createVerticalStrut(12));
    }

    // ── Current-month label ────────────────────────────────────────────────
    private JPanel createMonthBar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        p.setBackground(CARD);
        p.setBorder(new CompoundBorder(new LineBorder(BORDER), new EmptyBorder(0,6,0,6)));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        JLabel lbl = new JLabel("Current Month:  " + formatMonth(YearMonth.now()));
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(TXT_MAIN);
        p.add(lbl);
        return p;
    }

    private String formatMonth(YearMonth ym) {
        return ym.getMonth().getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH)
               + " " + ym.getYear();
    }

    // ── Stat cards row ─────────────────────────────────────────────────────
    private JPanel createStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 14, 0));
        row.setBackground(BG);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 96));
        totalIncomeLabel  = addStatCard(row, "TOTAL INCOME",   INCOME_C);
        totalExpenseLabel = addStatCard(row, "TOTAL EXPENSES", EXP_C);
        balanceLabel      = addStatCard(row, "BALANCE",        BAL_POS);
        updateStats();
        return row;
    }

    private JLabel addStatCard(JPanel parent, String title, Color accent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD);
        card.setBorder(new CompoundBorder(new LineBorder(BORDER), new EmptyBorder(12,16,12,16)));
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        titleLbl.setForeground(TXT_SUB);
        card.add(titleLbl, BorderLayout.NORTH);
        JLabel valueLbl = new JLabel("₱0.00");
        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLbl.setForeground(accent);
        card.add(valueLbl, BorderLayout.CENTER);
        parent.add(card);
        return valueLbl;
    }

    // ── Middle row: bar chart (left) + transaction history (right) ─────────
    private JPanel createMiddleRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 14, 0));
        row.setBackground(BG);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));

        // Bar chart card
        JPanel chartCard = new JPanel(new BorderLayout());
        chartCard.setBackground(CARD);
        chartCard.setBorder(new LineBorder(BORDER));

        JPanel chartTitle = buildCardTitleBar("Income vs Expenses — This Month");
        chartCard.add(chartTitle, BorderLayout.NORTH);

        barChart = new MonthlyBarChart();
        chartCard.add(barChart, BorderLayout.CENTER);
        updateChart();
        row.add(chartCard);

        // History card
        row.add(createHistoryCard());
        return row;
    }

    private JPanel buildCardTitleBar(String text) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(CARD);
        p.setBorder(new CompoundBorder(
            new MatteBorder(0,0,1,0, BORDER),
            new EmptyBorder(9,14,9,14)
        ));
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TXT_MAIN);
        p.add(lbl);
        return p;
    }

    // ── History text area card ─────────────────────────────────────────────
    private JPanel createHistoryCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD);
        card.setBorder(new LineBorder(BORDER));

        card.add(buildCardTitleBar("Transaction History"), BorderLayout.NORTH);

        historyArea = new JTextArea(10, 40);
        historyArea.setBackground(new Color(252, 252, 252));
        historyArea.setForeground(TXT_MAIN);
        historyArea.setEditable(false);
        historyArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        historyArea.setMargin(new Insets(10, 10, 10, 10));
        updateHistory();

        JScrollPane scroll = new JScrollPane(historyArea);
        scroll.setBorder(null);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    // ── Data update helpers ────────────────────────────────────────────────
    private void updateStats() {
        YearMonth cur = YearMonth.now();
        double inc = 0, exp = 0;
        for (Transaction t : transactions) {
            if (YearMonth.from(t.getDate()).equals(cur)) {
                if ("Income".equals(t.getType())) inc += t.getAmount();
                else                              exp += t.getAmount();
            }
        }
        double bal = inc - exp;
        totalIncomeLabel.setText(String.format("₱%.2f",  inc));
        totalExpenseLabel.setText(String.format("₱%.2f", exp));
        balanceLabel.setText(String.format("₱%.2f",      bal));
        balanceLabel.setForeground(bal >= 0 ? BAL_POS : BAL_NEG);
    }

    private void updateHistory() {
        StringBuilder sb = new StringBuilder();
        transactions.stream()
            .sorted((a, b) -> b.getDate().compareTo(a.getDate()))
            .limit(10)
            .forEach(t -> sb.append(t).append("\n"));
        historyArea.setText(sb.length() == 0 ? "No transactions yet." : sb.toString());
    }

    private void updateChart() {
        YearMonth cur = YearMonth.now();
        Map<String, Double> incCat = new LinkedHashMap<>();
        Map<String, Double> expCat = new LinkedHashMap<>();
        for (Transaction t : transactions) {
            if (YearMonth.from(t.getDate()).equals(cur)) {
                if ("Income".equals(t.getType()))
                    incCat.merge(t.getCategory(), t.getAmount(), Double::sum);
                else
                    expCat.merge(t.getCategory(), t.getAmount(), Double::sum);
            }
        }
        barChart.update(incCat, expCat);
    }

    @Override
    public void refresh(List<Transaction> txs) {
        this.transactions = txs;
        updateStats();
        updateHistory();
        updateChart();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Vertical grouped bar chart (per-category or summary)
    // ═══════════════════════════════════════════════════════════════════════
    static class MonthlyBarChart extends JPanel {
        private Map<String, Double> incCat = new LinkedHashMap<>();
        private Map<String, Double> expCat = new LinkedHashMap<>();

        private static final Color INC = new Color(40,  140, 80);
        private static final Color EXP = new Color(170, 40,  40);
        private static final Color AX  = new Color(180, 180, 180);
        private static final Color TXS = new Color(110, 110, 110);

        MonthlyBarChart() { setBackground(Color.WHITE); }

        void update(Map<String, Double> inc, Map<String, Double> exp) {
            this.incCat = new LinkedHashMap<>(inc);
            this.expCat = new LinkedHashMap<>(exp);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int W = getWidth(), H = getHeight();
            int padL = 52, padR = 14, padT = 16, padB = 44;
            int cW = W - padL - padR, cH = H - padT - padB;

            double totalInc = incCat.values().stream().mapToDouble(Double::doubleValue).sum();
            double totalExp = expCat.values().stream().mapToDouble(Double::doubleValue).sum();
            double maxVal   = Math.max(totalInc, totalExp);

            // No data
            if (maxVal <= 0) {
                g2.setFont(new Font("Segoe UI", Font.ITALIC, 11));
                g2.setColor(TXS);
                String msg = "No data this month";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(msg, (W - fm.stringWidth(msg)) / 2, H / 2);
                g2.dispose(); return;
            }

            // Decide: show per-category or summary
            boolean perCat = (incCat.size() + expCat.size()) > 2;

            if (perCat) {
                drawCategoryBars(g2, padL, padT, cW, cH, maxVal);
            } else {
                drawSummaryBars(g2, padL, padT, cW, cH, maxVal, totalInc, totalExp);
            }

            // Y-axis grid + labels
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            FontMetrics fm = g2.getFontMetrics();
            double step = niceStep(maxVal / 5.0);
            double yMax = Math.ceil(maxVal / step) * step;
            for (double v = 0; v <= yMax + step * 0.01; v += step) {
                int y = padT + cH - (int)(v / yMax * cH);
                g2.setColor(new Color(235, 235, 235));
                g2.drawLine(padL, y, padL + cW, y);
                g2.setColor(TXS);
                String lab = niceLabel(v);
                g2.drawString(lab, padL - fm.stringWidth(lab) - 4, y + fm.getAscent() / 2 - 1);
            }

            // Axes
            g2.setColor(AX);
            g2.drawLine(padL, padT, padL, padT + cH);
            g2.drawLine(padL, padT + cH, padL + cW, padT + cH);

            // Re-draw bars ON TOP of grid
            if (perCat) {
                drawCategoryBars(g2, padL, padT, cW, cH, Math.ceil(maxVal / step) * step);
            } else {
                drawSummaryBars(g2, padL, padT, cW, cH, Math.ceil(maxVal / step) * step, totalInc, totalExp);
            }

            g2.dispose();
        }

        private void drawSummaryBars(Graphics2D g2, int pL, int pT, int cW, int cH,
                                     double yMax, double inc, double exp) {
            int barW  = Math.min(60, cW / 4);
            int gap   = barW / 2;
            int total = 2 * barW + gap;
            int sx    = pL + (cW - total) / 2;

            drawBar(g2, sx,           pT, cH, barW, inc, yMax, INC, "Income");
            drawBar(g2, sx + barW + gap, pT, cH, barW, exp, yMax, EXP, "Expenses");
        }

        private void drawCategoryBars(Graphics2D g2, int pL, int pT, int cW, int cH, double yMax) {
            Set<String> cats = new LinkedHashSet<>();
            cats.addAll(incCat.keySet());
            cats.addAll(expCat.keySet());
            if (cats.isEmpty()) return;

            int n     = cats.size();
            int grpW  = cW / n;
            int barW  = Math.max(8, grpW - 10);
            int gx    = pL;
            for (String cat : cats) {
                double val = incCat.getOrDefault(cat, expCat.getOrDefault(cat, 0.0));
                Color  clr = CategoryColors.getColor(cat);
                drawBar(g2, gx + (grpW - barW) / 2, pT, cH, barW, val, yMax, clr, cat);
                gx += grpW;
            }
        }

        private void drawBar(Graphics2D g2, int bx, int pT, int cH,
                             int barW, double val, double yMax,
                             Color clr, String label) {
            if (val > 0 && yMax > 0) {
                int bh = (int)(val / yMax * cH);
                int by = pT + cH - bh;
                g2.setColor(clr);
                g2.fillRoundRect(bx, by, barW, bh, 4, 4);
                if (bh > 6) g2.fillRect(bx, by + 4, barW, bh - 4);
            }
            // X label
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2.setColor(new Color(70, 70, 70));
            FontMetrics fm = g2.getFontMetrics();
            // Truncate if too wide
            String lbl = label.length() > 8 ? label.substring(0, 7) + "." : label;
            int lw = fm.stringWidth(lbl);
            g2.drawString(lbl, bx + (barW - lw) / 2, pT + cH + 16);
        }

        private double niceStep(double raw) {
            if (raw <= 0) return 1;
            double mag  = Math.pow(10, Math.floor(Math.log10(raw)));
            double norm = raw / mag;
            double nice = norm < 1.5 ? 1 : norm < 3 ? 2 : norm < 7 ? 5 : 10;
            return nice * mag;
        }

        private String niceLabel(double v) {
            if (v >= 1_000_000) return String.format("%.1fM", v / 1_000_000);
            if (v >= 1_000)     return String.format("%.0fK", v / 1_000);
            return String.format("%.0f", v);
        }
    }
}
