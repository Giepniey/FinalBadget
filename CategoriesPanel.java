import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class CategoriesPanel extends JPanel implements Refreshable {
    private List<Transaction> transactions;

    private static final Color BG      = new Color(245, 245, 245);
    private static final Color CARD    = Color.WHITE;
    private static final Color BORDER  = new Color(220, 220, 220);
    private static final Color DIVIDER = new Color(240, 240, 240);
    private static final Color TXT_M   = new Color(30,  30,  30);
    private static final Color TXT_S   = new Color(110, 110, 110);

    public CategoriesPanel(List<Transaction> transactions) {
        this.transactions = transactions;
        setLayout(new GridLayout(1, 2, 20, 0));
        setBackground(BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        updateCharts();
    }

    private void updateCharts() {
        removeAll();
        Map<String, Double> income  = new LinkedHashMap<>();
        Map<String, Double> expense = new LinkedHashMap<>();
        for (Transaction t : transactions) {
            if ("Income".equals(t.getType()))
                income.merge(t.getCategory(), t.getAmount(), Double::sum);
            else
                expense.merge(t.getCategory(), t.getAmount(), Double::sum);
        }
        add(createCard("INCOME",   income));
        add(createCard("EXPENSES", expense));
        revalidate();
        repaint();
    }

    private JPanel createCard(String title, Map<String, Double> data) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD);
        card.setBorder(new LineBorder(BORDER));

        // ── Header ──────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(CARD);
        header.setBorder(new CompoundBorder(
            new MatteBorder(0,0,1,0, BORDER),
            new EmptyBorder(10,14,10,14)
        ));
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLbl.setForeground(TXT_S);
        header.add(titleLbl, BorderLayout.WEST);

        double total = data.values().stream().mapToDouble(Double::doubleValue).sum();
        if (total > 0) {
            JLabel totLbl = new JLabel(String.format("₱%.2f", total));
            totLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
            totLbl.setForeground(TXT_M);
            header.add(totLbl, BorderLayout.EAST);
        }
        card.add(header, BorderLayout.NORTH);

        // ── Chart area (legend + stacked bar) ───────────────────────────────
        JPanel chartArea = new JPanel();
        chartArea.setLayout(new BoxLayout(chartArea, BoxLayout.Y_AXIS));
        chartArea.setBackground(CARD);
        chartArea.setBorder(new EmptyBorder(10, 14, 6, 14));

        if (!data.isEmpty() && total > 0) {
            // Legend
            JPanel legend = buildLegend(data);
            legend.setAlignmentX(Component.LEFT_ALIGNMENT);
            chartArea.add(legend);
            chartArea.add(Box.createVerticalStrut(8));

            // Stacked horizontal bar
            JPanel bar = buildStackedBar(data, total);
            bar.setAlignmentX(Component.LEFT_ALIGNMENT);
            chartArea.add(bar);
            chartArea.add(Box.createVerticalStrut(4));

            // Scale
            JPanel scale = buildScale(total);
            scale.setAlignmentX(Component.LEFT_ALIGNMENT);
            chartArea.add(scale);
        }
        card.add(chartArea, BorderLayout.CENTER);

        // ── Row list ─────────────────────────────────────────────────────────
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(CARD);
        listPanel.setBorder(new EmptyBorder(2, 0, 4, 0));

        if (data.isEmpty()) {
            JLabel empty = new JLabel("  No transactions yet");
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            empty.setForeground(TXT_S);
            empty.setBorder(new EmptyBorder(10, 14, 10, 14));
            listPanel.add(empty);
        } else {
            data.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .forEach(e -> listPanel.add(createRow(e.getKey(), e.getValue(), total)));
        }

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(CARD);
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        // Combine chartArea (top) + scroll list (bottom)
        JPanel middle = new JPanel(new BorderLayout());
        middle.setBackground(CARD);
        middle.add(chartArea, BorderLayout.NORTH);
        middle.add(scroll,    BorderLayout.CENTER);
        card.add(middle, BorderLayout.CENTER);

        return card;
    }

    // ── Legend row: colored dot + label per category ─────────────────────
    private JPanel buildLegend(Map<String, Double> data) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        p.setBackground(CARD);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        for (String cat : data.keySet()) {
            final Color accent = CategoryColors.getColor(cat);
            JPanel dot = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(accent);
                    g2.fillOval(0, (getHeight()-8)/2, 8, 8);
                    g2.dispose();
                }
            };
            dot.setPreferredSize(new Dimension(10, 14));
            dot.setOpaque(false);

            JLabel lbl = new JLabel(cat);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            lbl.setForeground(TXT_S);

            JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
            item.setBackground(CARD);
            item.add(dot);
            item.add(lbl);
            p.add(item);
        }
        return p;
    }

    // ── Stacked horizontal bar ────────────────────────────────────────────
    private JPanel buildStackedBar(Map<String, Double> data, double total) {
        // snapshot for painting
        final Map<String, Double> snap = new LinkedHashMap<>(data);
        final double tot = total;

        JPanel bar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                int bh = h - 2;
                // background
                g2.setColor(new Color(240, 240, 240));
                g2.fillRoundRect(0, 0, w, bh, 6, 6);
                // segments
                int x = 0;
                for (Map.Entry<String, Double> e : snap.entrySet()) {
                    int sw = (int) Math.round(e.getValue() / tot * w);
                    g2.setColor(CategoryColors.getColor(e.getKey()));
                    if (x == 0) {
                        // left-rounded first segment
                        g2.fillRoundRect(x, 0, sw + 6, bh, 6, 6);
                        g2.fillRect(x + 6, 0, sw, bh);
                    } else if (x + sw >= w - 2) {
                        // right-rounded last segment
                        g2.fillRoundRect(x, 0, sw, bh, 6, 6);
                        g2.fillRect(x, 0, sw - 6, bh);
                    } else {
                        g2.fillRect(x, 0, sw, bh);
                    }
                    x += sw;
                }
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(0, 28));
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        return bar;
    }

    // ── Scale ticks below bar ──────────────────────────────────────────────
    private JPanel buildScale(double total) {
        final double tot = total;
        JPanel scale = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                g2.setColor(TXT_S);
                int w = getWidth();
                // pick a nice step
                int steps = 5;
                double step = tot / steps;
                // round step to nice number
                double magnitude = Math.pow(10, Math.floor(Math.log10(step)));
                step = Math.ceil(step / magnitude) * magnitude;
                for (double v = 0; v <= tot + step * 0.5; v += step) {
                    int x = (int) (v / tot * w);
                    if (x > w) x = w;
                    String lbl = v == 0 ? "0" : String.format("%.0f", v);
                    FontMetrics fm = g2.getFontMetrics();
                    int lw = fm.stringWidth(lbl);
                    g2.drawString(lbl, Math.max(0, Math.min(x - lw/2, w - lw)), 10);
                }
                g2.dispose();
            }
        };
        scale.setOpaque(false);
        scale.setPreferredSize(new Dimension(0, 14));
        scale.setMaximumSize(new Dimension(Integer.MAX_VALUE, 14));
        return scale;
    }

    // ── Single category row ────────────────────────────────────────────────
    private JPanel createRow(String category, double amount, double total) {
        Color accent = CategoryColors.getColor(category);
        double pct   = total > 0 ? (amount / total) * 100 : 0;

        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(CARD);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        row.setMinimumSize(new Dimension(0, 38));
        row.setBorder(new CompoundBorder(
            new MatteBorder(0,0,1,0, DIVIDER),
            new EmptyBorder(0,14,0,14)
        ));

        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accent);
                g2.fillOval(0, (getHeight()-9)/2, 9, 9);
                g2.dispose();
            }
        };
        dot.setPreferredSize(new Dimension(12, 12));
        dot.setOpaque(false);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        left.setBackground(CARD);
        left.add(dot);
        JLabel nameLabel = new JLabel(category);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        nameLabel.setForeground(TXT_M);
        left.add(nameLabel);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setBackground(CARD);
        JLabel pctLabel = new JLabel(String.format("%.1f%%", pct));
        pctLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        pctLabel.setForeground(TXT_S);
        JLabel amtLabel = new JLabel(String.format("₱%.2f", amount));
        amtLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        amtLabel.setForeground(TXT_M);
        right.add(pctLabel);
        right.add(amtLabel);

        row.add(left,  BorderLayout.CENTER);
        row.add(right, BorderLayout.EAST);
        return row;
    }

    @Override
    public void refresh(List<Transaction> transactions) {
        this.transactions = transactions;
        updateCharts();
    }
}
