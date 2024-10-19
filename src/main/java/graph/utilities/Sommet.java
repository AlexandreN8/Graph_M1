package graph.utilities;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

public class Sommet {

    private int id;
    private String nom;
    private double posX, posY;
    private Circle circle;

    public Sommet(int id, String nom, double posX, double posY) {
        this.id = id;
        this.nom = nom;
        this.posX = posX;
        this.posY = posY;
    }

    public int getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public double getPosX() {
        return posX;
    }

    public double getPosY() {
        return posY;
    }

    public Circle getCircle() {
        return circle;
    }

    @Override
    public String toString() {
        return "Sommet{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                '}';
    }

    // Méthode pour créer le cercle graphique du sommet
    public Circle createCircle() {
        this.circle = new Circle(posX, posY, 20, Color.BLUE);
        return this.circle;
    }

    // Méthode pour créer l'étiquette du nom du sommet
    public Text createNameLabel(boolean enHaut) {
        Text nameLabel = new Text(nom);
        if (enHaut) {
            nameLabel.setX(posX - nameLabel.getLayoutBounds().getWidth() / 2); // Centrer le nom
            nameLabel.setY(posY - 30); // Légèrement au-dessus du sommet
            return nameLabel;
        } else 
            nameLabel.setX(posX - nameLabel.getLayoutBounds().getWidth() / 2); // Centrer le nom
            nameLabel.setY(posY + 35); // Légèrement en-dessous du sommet
            return nameLabel;
    }   
    
    public void resetStyle() {
        this.circle.setFill(Color.LIGHTGRAY); // Remettre la couleur par défaut du sommet
    }
    
    public void highlight() {
        this.circle.setFill(Color.GREEN); // Mettre le sommet en surbrillance 
    }
}
