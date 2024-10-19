package graph.arc;

import javafx.scene.text.Text;
import graph.utilities.Sommet;
import javafx.scene.shape.Line;

public class ArcStandard extends Arc {

    public ArcStandard(Sommet sommetDepart, Sommet sommetArrivee, double change, boolean etiquetteHaut) {
        super(sommetDepart, sommetArrivee, change, etiquetteHaut);
    }

    @Override
    public Text createLabel(Line line) {
        Text label = new Text();
        label.setText(String.valueOf(change)); // afficher le taux de change

        // Reprendre la logique de placement du label
        double labelX = line.getStartX() + 0.7 * (line.getEndX() - line.getStartX());
        double labelY = line.getStartY() + 0.7 * (line.getEndY() - line.getStartY());
        label.setX(labelX);
        label.setY(labelY);

        return label;
    }
}
