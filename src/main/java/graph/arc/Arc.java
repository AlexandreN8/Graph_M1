package graph.arc;

import graph.utilities.Sommet;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;

public abstract class Arc {

    protected Sommet sommetDepart; 
    protected Sommet sommetArrivee;
    protected double change; // Taux de change commun pour tous les arcs
    protected boolean etiquetteHaut; // Pour indiquer si la courbe doit être inversée (javafx)
    protected Line line; // Ligne de l'arc (javafx)
    protected Polygon arrowHead; // Flèche de l'arc (javafx)

    private static final double ESPACEMENT = 5; // Décalage pour espacer les arcs adjacents (javafx)
    private static final double RAYON_SOMMET = 20; // Rayon du cercle du sommet (javafx)

    public Arc(Sommet sommetDepart, Sommet sommetArrivee, double change, boolean etiquetteHaut) {
        this.sommetDepart = sommetDepart;
        this.sommetArrivee = sommetArrivee;
        this.change = change;
        this.etiquetteHaut = etiquetteHaut;
    }

    public Sommet getSommetDepart() {
        return sommetDepart;
    }

    public Sommet getSommetArrivee() {
        return sommetArrivee;
    }

    public double getChange() {
        return change;
    }

    public void setChange(double change) {
        this.change = change;
    }

    public void setSommetDepart(Sommet sommetDepart) {
        this.sommetDepart = sommetDepart;
    }

    public void setSommetArrivee(Sommet sommetArrivee) {
        this.sommetArrivee = sommetArrivee;
    }

    public Line getLine() {
        return line;
    }

    // Méthode commune pour créer une ligne droite représentant l'arc (javafx)
    public Line createLine(boolean decale) {
        this.line = new Line();
        double startX = sommetDepart.getPosX();
        double startY = sommetDepart.getPosY();
        double endX = sommetArrivee.getPosX();
        double endY = sommetArrivee.getPosY();

        // Calculer l'angle de la ligne
        double angle = Math.atan2(endY - startY, endX - startX);

        // Calculer la nouvelle position de fin pour que l'arc s'arrête au bord du cercle
        double distance = Math.hypot(endX - startX, endY - startY);
        double distanceToEdge = distance - RAYON_SOMMET - 10; // Réduire la longueur de la ligne pour s'arrêter avant le cercle et laisser la place à la flèche

        // Recalculer les coordonnées finales
        endX = startX + distanceToEdge * Math.cos(angle);
        endY = startY + distanceToEdge * Math.sin(angle);

        // Appliquer un léger décalage si nécessaire pour éviter les chevauchements
        if (decale) {
            startX += ESPACEMENT * Math.cos(angle + Math.PI / 2);
            startY += ESPACEMENT * Math.sin(angle + Math.PI / 2);
            endX += ESPACEMENT * Math.cos(angle + Math.PI / 2);
            endY += ESPACEMENT * Math.sin(angle + Math.PI / 2);
        }

        line.setStartX(startX);
        line.setStartY(startY);
        line.setEndX(endX);
        line.setEndY(endY);
        line.setStroke(Color.GRAY);
        line.setStrokeWidth(2);

        return line;
    }

    // Méthode pour ajouter une flèche à la fin de l'arc (javafx)
    public Polygon createArrow(Line line) {
        double arrowSize = 10; // Taille de la flèche
        double angle = Math.atan2(line.getEndY() - line.getStartY(), line.getEndX() - line.getStartX());

        // Placer la flèche exactement à la fin de la ligne
        double arrowX = line.getEndX();
        double arrowY = line.getEndY();

        this.arrowHead = new Polygon();
        arrowHead.getPoints().addAll(
            arrowX, arrowY,
            arrowX - arrowSize * Math.cos(angle - Math.PI / 6), arrowY - arrowSize * Math.sin(angle - Math.PI / 6),
            arrowX - arrowSize * Math.cos(angle + Math.PI / 6), arrowY - arrowSize * Math.sin(angle + Math.PI / 6)
        );
        arrowHead.setFill(Color.GRAY);

        return arrowHead;
    }

    // Méthode commune pour réinitialiser le style (javafx)
    public void resetStyle() {
        this.line.setStroke(Color.GRAY);
        this.line.setStrokeWidth(2); // Réinitialiser l'épaisseur à 2
    }

    // Méthode commune pour mettre en surbrillance (javafx)
    public void highlight() {
        this.line.setStroke(Color.GREEN); // Changer la couleur de l'arc en vert pour l'highlight
        this.line.setStrokeWidth(4);    // Augmenter l'épaisseur de l'arc pour l'highlight
        
        this.arrowHead.setFill(Color.GREEN); // Changer la couleur de la flèche en vert pour l'highlight
    }

    // Méthode abstraite à implémenter dans les sous-classes pour les étiquettes spécifiques
    public abstract javafx.scene.text.Text createLabel(Line line);
}
