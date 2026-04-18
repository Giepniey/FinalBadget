import java.awt.Color;

public class CategoryColors {
    // Category accent colors (used as small highlights only)
    public static Color getColor(String category) {
        if (category == null) return new Color(130, 130, 130);
        switch (category.trim()) {
            case "Food":          return new Color(210, 45,  45);   // Red
            case "Transport":     return new Color(41,  128, 210);  // Blue
            case "Shopping":      return new Color(210, 105, 20);   // Orange
            case "Entertainment": return new Color(210, 50,  130);  // Pink
            case "Utilities":     return new Color(115, 0,   0);    // Maroon
            case "Education":     return new Color(175, 135, 0);    // Dark Yellow
            case "Health":        return new Color(100, 55,  20);   // Brown
            case "Rent":          return new Color(100, 55,  20);   // Brown
            // Income
            case "Salary":        return new Color(30,  140, 90);
            case "Freelance":     return new Color(40,  120, 160);
            case "Investment":    return new Color(80,  100, 170);
            case "Bonus":         return new Color(130, 70,  170);
            case "Gift":          return new Color(170, 60,  120);
            default:              return new Color(130, 130, 130);  // Neutral grey
        }
    }
}
