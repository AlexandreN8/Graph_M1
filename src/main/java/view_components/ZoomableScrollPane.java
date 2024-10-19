package view_components;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

/* Composant ScrollPane modifier pour conserver permettre de scale la node target */
public class ZoomableScrollPane extends ScrollPane {
    private double scaleValue = 0.7;
    private double initialScaleValue = 0.7; // pour reset la position
    private double zoomIntensity = 0.02;
    private Node target;
    private Node zoomNode;

    public ZoomableScrollPane(Node target) {
        super();
        this.target = target;
        this.zoomNode = new Group(target);
        setContent(outerNode(zoomNode));

        setPannable(true);
        setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        setFitToHeight(true); //center
        setFitToWidth(true); //center

        updateScale();
    }

    private Node outerNode(Node node) {
        Node outerNode = centeredNode(node);
        outerNode.setOnScroll(e -> {
            e.consume();
            onScroll(e.getTextDeltaY(), new Point2D(e.getX(), e.getY()));
        });
        return outerNode;
    }

    private Node centeredNode(Node node) {
        VBox vBox = new VBox(node);
        vBox.setAlignment(Pos.CENTER);
        return vBox;
    }

    private void updateScale() {
        target.setScaleX(scaleValue);
        target.setScaleY(scaleValue);
    }

    public void resetZoom() {
        scaleValue = initialScaleValue;  // Remettre le zoom à sa valeur initiale
        updateScale();  // Mettre à jour le zoom du contenu
        layout(); // raffraichir la position du contenu avant de recentrer
        recenter();  // Recentrer la vue
    }
    
    private void recenter() {
        setVvalue(0.5);
        setHvalue(0.5);
    }
    

    public void onScroll(double wheelDelta, Point2D mousePoint) {
        double zoomFactor = Math.exp(wheelDelta * zoomIntensity);

        Bounds innerBounds = zoomNode.getLayoutBounds();
        Bounds viewportBounds = getViewportBounds();

        // Calculer l'offset de la position de la cible (pixels) entre [0, 1] 
        double valX = this.getHvalue() * (innerBounds.getWidth() - viewportBounds.getWidth());
        double valY = this.getVvalue() * (innerBounds.getHeight() - viewportBounds.getHeight());

        scaleValue = scaleValue * zoomFactor;
        updateScale();
        this.layout(); // rafraichir la position du contenu 

        // Convertir la position de la souris en coordonnées locales
        Point2D posInZoomTarget = target.parentToLocal(zoomNode.parentToLocal(mousePoint));

        // Calculer l'ajustement de la position de la cible
        Point2D adjustment = target.getLocalToParentTransform().deltaTransform(posInZoomTarget.multiply(zoomFactor - 1));

        // convertir l'ajustement en coordonée de scrollPane entre [0, 1]
        Bounds updatedInnerBounds = zoomNode.getBoundsInLocal();
        this.setHvalue((valX + adjustment.getX()) / (updatedInnerBounds.getWidth() - viewportBounds.getWidth()));
        this.setVvalue((valY + adjustment.getY()) / (updatedInnerBounds.getHeight() - viewportBounds.getHeight()));
    }

}