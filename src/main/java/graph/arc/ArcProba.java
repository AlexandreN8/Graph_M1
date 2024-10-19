package graph.arc;

import javafx.scene.text.Text;
import graph.utilities.Sommet;
import javafx.scene.shape.Line;

public class ArcProba extends Arc {

    private double riskFactor;  // Facteur de risque

    public ArcProba(Sommet sommetDepart, Sommet sommetArrivee, double change, double riskFactor, boolean etiquetteHaut) {
        super(sommetDepart, sommetArrivee, change, etiquetteHaut);
        this.riskFactor = riskFactor;
    }

    public double getRiskFactor() {
        return riskFactor;
    }

    public void setRiskFactor(double riskFactor) {
        this.riskFactor = riskFactor;
    }

    // Méthode pour calculer la valeur ajustée en fonction du risque
    public double getAdjustedValue() {
        return change * (1 - riskFactor); // Réduire la valeur en fonction du risque
    }

    @Override
    public Text createLabel(Line line) {
        Text label = new Text();
        label.setText(String.format("%.2f (risque: %.2f)", change, riskFactor)); // Afficher taux de change et risque

        // Reprendre la logique de placement du label
        double labelX = line.getStartX() + 0.7 * (line.getEndX() - line.getStartX());
        double labelY = line.getStartY() + 0.7 * (line.getEndY() - line.getStartY());
        label.setX(labelX);
        label.setY(labelY);

        return label;
    }
}
