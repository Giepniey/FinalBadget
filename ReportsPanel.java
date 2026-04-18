import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.time.YearMonth;
import java.util.*;
import java.util.List;

public class ReportsPanel extends JPanel implements Refreshable {
    private List<Transaction> transactions;
    private JSpinner  monthSpinner;
    private JTextArea reportArea;
    private BarChartPanel chartPanel;

    private static final Color BG      = new Color(245, 245, 245);
    private static final Color CARD    = Color.WHITE;
    private static final Color BORDER  = new Color(220, 220, 220);
    private static final Color TXT     = new Color(30,  30,  30);
    private static final Color TXT_SUB = new Color(110, 110, 110);

    // ── Max allowed YearMonth = current month ──────────────────────────────
    private static final YearMonth MAX_MONTH = YearMonth.now();

    public ReportsPanel(List<Transaction> transactions) {
        this.transactions = transactions;
        setLayout(new BorderLayout());
        setBackground(BG);

        add(createControls(), BorderLayout.NORTH);

        // ── Centre: report text (left) + bar chart (right) ──────────────────
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setDividerLocation(340);
        split.setDividerSize(1);
        split.setBorder(null);
        split.setResizeWeight(0.35);

        reportArea = new JTextArea();
        reportArea.setBackground(new Color(252, 252, 252));
        reportArea.setForeground(TXT);
        reportArea.setEditable(false);
        reportArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        reportArea.setMargin(new Insets(16, 16, 16, 16));
        JScrollPane reportScroll = new JScrollPane(reportArea);
        reportScroll.setBorder(new EmptyBorder(16, 16, 16, 8));
        reportScroll.getViewport().setBackground(BG);

        chartPanel = new BarChartPanel();
        JPanel chartWrapper = new JPanel(new BorderLayout());
        chartWrapper.setBackground(BG);
        chartWrapper.setBorder(new EmptyBorder(16, 8, 16, 16));
        chartWrapper.add(chartPanel, BorderLayout.CENTER);

        split.setLeftComponent(reportScroll);
        split.setRightComponent(chartWrapper);
        add(split, BorderLayout.CENTER);

        generateReport();
    }

    private JPanel createControls() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 12));
        p.setBackground(CARD);
        p.setBorder(new MatteBorder(0, 0, 1, 0, BORDER));

        JLabel lbl = new JLabel("Month:");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(TXT_SUB);

        // SpinnerDateModel: cap max at last day of current month
        Calendar maxCal = Calendar.getInstance();
        maxCal.set(Calendar.DAY_OF_MONTH, maxCal.getActualMaximum(Calendar.DAY_OF_MONTH));
        maxCal.set(Calendar.HOUR_OF_DAY, 23);
        maxCal.set(Calendar.MINUTE, 59);
        Date maxDate = maxCal.getTime();

        SpinnerDateModel model = new SpinnerDateModel(new Date(), null, maxDate, Calendar.MONTH);
        monthSpinner = new JSpinner(model);
        monthSpinner.setEditor(new JSpinner.DateEditor(monthSpinner, "yyyy-MM"));

        monthSpinner.addChangeListener(e -> {
            // Double-check: reject any future month the user might force via keyboard
            Date selected = (Date) monthSpinner.getValue();
            Calendar selCal = Calendar.getInstance();
            selCal.setTime(selected);
            YearMonth selYM = YearMonth.of(selCal.get(Calendar.YEAR), selCal.get(Calendar.MONTH) + 1);
            if (selYM.isAfter(MAX_MONTH)) {
                JOptionPane.showMessageDialog(
                    this,
                    "Cannot select a future month.\nMaximum allowed: " + MAX_MONTH,
                    "Invalid Date Range",
                    JOptionPane.ERROR_MESSAGE
                );
                monthSpinner.setValue(maxDate);
                return;
            }
            generateReport();
        });

        p.add(lbl);
        p.add(monthSpinner);

        JLabel hint = new JLabel("(Max: " + MAX_MONTH + ")");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        hint.setForeground(TXT_SUB);
        p.add(hint);

        return p;
    }

    private void generateReport() {
        Date sel = (Date) monthSpinner.getValue();
        Calendar selCal = Calendar.getInstance();
        selCal.setTime(sel);
        YearMonth ym = YearMonth.of(selCal.get(Calendar.YEAR), selCal.get(Calendar.MONTH) + 1);

        double inc = 0, exp = 0;
        Map<String, Double> incCat = new LinkedHashMap<>();
        Map<String, Double> expCat = new LinkedHashMap<>();

        for (Transaction t : transactions) {
            if (!YearMonth.from(t.getDate()).equals(ym)) continue;
            if ("Income".equals(t.getType())) {
                inc += t.getAmount();
                incCat.merge(t.getCategory(), t.getAmount(), Double::sum);
            } else {
                exp += t.getAmount();
                expCat.merge(t.getCategory(), t.getAmount(), Double::sum);
            }
        }

        // ── Text report ──────────────────────────────────────────────────────
        String sep = "─".repeat(44);
        StringBuilder r = new StringBuilder();
        r.append("  MONTHLY REPORT  ─  ").append(ym).append("\n")
         .append(sep).append("\n\n")
         .append("  SUMMARY\n").append(sep).append("\n")
         .append(String.format("  Total Income    %16s%n", fmt(inc)))
         .append(String.format("  Total Expenses  %16s%n", fmt(exp)))
         .append(String.format("  Balance         %16s%n", fmt(inc - exp)))
         .append("\n  INCOME BREAKDOWN\n").append(sep).append("\n");

        if (incCat.isEmpty()) r.append("  No income transactions\n");
        else incCat.forEach((c, a) -> r.append(String.format("  %-24s %12s%n", c, fmt(a))));

        r.append("\n  EXPENSE BREAKDOWN\n").append(sep).append("\n");
        if (expCat.isEmpty()) r.append("  No expense transactions\n");
        else expCat.forEach((c, a) -> r.append(String.format("  %-24s %12s%n", c, fmt(a))));

        reportArea.setText(r.toString());
        reportArea.setCaretPosition(0);

        // ── Update bar chart ─────────────────────────────────────────────────
        chartPanel.update(incCat, expCat, inc, exp);
    }

    private String fmt(double v) { return String.format("₱%,.2f", v); }

    @Override
    public void refresh(List<Transaction> txs) {
        this.transactions = txs;
        generateReport();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Inner bar-chart panel
    // ═══════════════════════════════════════════════════════════════════════
    static class BarChartPanel extends JPanel {
        private Map<String, Double> incCat = new LinkedHashMap<>();
        private Map<String, Double> expCat = new LinkedHashMap<>();
        private double totalInc, totalExp;

        private static final Color INC_COLOR = new Color(40, 140, 80);
        private static final Color EXP_COLOR = new Color(170, 40, 40);
        private static final Color AXIS      = new Color(180, 180, 180);
        private static final Color TXT_S     = new Color(110, 110, 110);
        private static final Color TXT_M     = new Color(30,  30,  30);

        BarChartPanel() {
            setBackground(Color.WHITE);
            setBorder(new LineBorder(new Color(220, 220, 220)));
        }

        void update(Map<String, Double> incCat, Map<String, Double> expCat,
                    double totalInc, double totalExp) {
            this.incCat   = new LinkedHashMap<>(incCat);
            this.expCat   = new LinkedHashMap<>(expCat);
            this.totalInc = totalInc;
            this.totalExp = totalExp;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int W = getWidth(), H = getHeight();
            int padL = 55, padR = 20, padT = 20, padB = 50;
            int chartW = W - padL - padR;
            int chartH = H - padT - padB;

            double maxVal = Math.max(totalInc, totalExp);
            if (maxVal <= 0) {
                g2.setFont(new Font("Segoe UI", Font.ITALIC, 11));
                g2.setColor(TXT_S);
                String msg = "No data for this month";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(msg, (W - fm.stringWidth(msg)) / 2, H / 2);
                g2.dispose();
                return;
            }

            // ── Y-axis grid + labels ─────────────────────────────────────────
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            FontMetrics fm = g2.getFontMetrics();
            int ySteps = 5;
            double yStep = niceStep(maxVal / ySteps);
            double yMax  = Math.ceil(maxVal / yStep) * yStep;
            for (double v = 0; v <= yMax + yStep * 0.01; v += yStep) {
                int y = padT + chartH - (int)(v / yMax * chartH);
                g2.setColor(new Color(235, 235, 235));
                g2.drawLine(padL, y, padL + chartW, y);
                g2.setColor(TXT_S);
                String lab = formatAmt(v);
                g2.drawString(lab, padL - fm.stringWidth(lab) - 5, y + fm.getAscent() / 2 - 1);
            }

            // ── Axes ─────────────────────────────────────────────────────────
            g2.setColor(AXIS);
            g2.drawLine(padL, padT, padL, padT + chartH);
            g2.drawLine(padL, padT + chartH, padL + chartW, padT + chartH);

            // ── Bars ─────────────────────────────────────────────────────────
            int numGroups  = Math.max(1, Math.max(incCat.size(), expCat.size()));
            boolean showPerCat = (incCat.size() + expCat.size()) > 0 && numGroups > 0;

            if (showPerCat && (incCat.size() > 1 || expCat.size() > 1)) {
                // Per-category grouped bars
                drawCategoryBars(g2, padL, padT, chartW, chartH, yMax, fm);
            } else {
                // Simple 2-bar: income vs expenses
                drawSummaryBars(g2, padL, padT, chartW, chartH, yMax, fm);
            }

            g2.dispose();
        }

        private void drawSummaryBars(Graphics2D g2, int padL, int padT,
                                     int cW, int cH, double yMax, FontMetrics fm) {
            int barW   = cW / 5;
            int gapBetween = barW / 2;
            int totalBarArea = barW * 2 + gapBetween;
            int startX = padL + (cW - totalBarArea) / 2;

            // Income bar
            if (totalInc > 0) {
                int bh = (int)(totalInc / yMax * cH);
                int bx = startX;
                int by = padT + cH - bh;
                g2.setColor(INC_COLOR);
                g2.fillRoundRect(bx, by, barW, bh, 4, 4);
                g2.fillRect(bx, by + 4, barW, Math.max(1, bh - 4));
            }

            // Expense bar
            if (totalExp > 0) {
                int bh = (int)(totalExp / yMax * cH);
                int bx = startX + barW + gapBetween;
                int by = padT + cH - bh;
                g2.setColor(EXP_COLOR);
                g2.fillRoundRect(bx, by, barW, bh, 4, 4);
                g2.fillRect(bx, by + 4, barW, Math.max(1, bh - 4));
            }

            // X-axis labels
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g2.setColor(new Color(70, 70, 70));
            FontMetrics fm2 = g2.getFontMetrics();
            int labelY = padT + cH + 18;
            String[] labs = {"Income", "Expenses"};
            int[] lx      = {startX, startX + barW + gapBetween};
            for (int i = 0; i < 2; i++) {
                int lw = fm2.stringWidth(labs[i]);
                g2.drawString(labs[i], lx[i] + (barW - lw) / 2, labelY);
            }
        }

        private void drawCategoryBars(Graphics2D g2, int padL, int padT,
                                      int cW, int cH, double yMax, FontMetrics fm) {
            // Merge all categories
            Set<String> allCats = new LinkedHashSet<>();
            allCats.addAll(incCat.keySet());
            allCats.addAll(expCat.keySet());
            int n = allCats.size();
            if (n == 0) return;

            int groupW = cW / n;
            int barW   = Math.max(8, groupW - 8);
            int gx     = padL;

            for (String cat : allCats) {
                double val = incCat.getOrDefault(cat, expCat.getOrDefault(cat, 0.0));
                Color  clr = incCat.containsKey(cat) ? INC_COLOR : EXP_COLOR;

                if (val > 0) {
                    int bh = (int)(val / yMax * cH);
                    int bx = gx + (groupW - barW) / 2;
                    int by = padT + cH - bh;
                    g2.setColor(CategoryColors.getColor(cat));
                    g2.fillRoundRect(bx, by, barW, bh, 4, 4);
                    if (bh > 4) g2.fillRect(bx, by + 4, barW, bh - 4);
                }

                // X label (rotated if needed)
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                g2.setColor(new Color(70, 70, 70));
                FontMetrics fm9 = g2.getFontMetrics();
                int lw = fm9.stringWidth(cat);
                int lx = gx + groupW / 2 - lw / 2;
                g2.drawString(cat, lx, padT + cH + 14);

                gx += groupW;
            }
        }

        private double niceStep(double rawStep) {
            if (rawStep <= 0) return 1;
            double mag = Math.pow(10, Math.floor(Math.log10(rawStep)));
            double norm = rawStep / mag;
            double nice = norm < 1.5 ? 1 : norm < 3 ? 2 : norm < 7 ? 5 : 10;
            return nice * mag;
        }

        private String formatAmt(double v) {
            if (v >= 1_000_000) return String.format("%.1fM", v / 1_000_000);
            if (v >= 1_000)     return String.format("%.0fK", v / 1_000);
            return String.format("%.0f", v);
        }
    }
}
