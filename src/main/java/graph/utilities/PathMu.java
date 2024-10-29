package graph.utilities;
import java.util.List;

/* Représente mu, un chemin parcouru par le graphe */

public class PathMu {
    private List<Sommet> chemin;
    private double valeur;
    private double accProbSucces;

    public PathMu(List<Sommet> chemin, double valeur) {
        this.chemin = chemin;
        this.valeur = valeur;
        this.accProbSucces = 1;
    }

    // Constructeur de la version probabiliste
    public PathMu(List<Sommet> chemin, double valeur, double accProbSucces) {
        this.chemin = chemin;
        this.valeur = valeur;
        this.accProbSucces = accProbSucces;
    }

    public List<Sommet> getChemin() {
        return chemin;
    }

    public double getValeur() {
        return valeur;
    }

    public void setValeur(double valeur) {
        this.valeur = valeur;
    }

    public double getAccProbSucces () {
        return accProbSucces;
    }

    public void setaccProbSucces (double accTaux) {
        this.accProbSucces = accTaux;
    }    

    @Override
    public String toString() {
        StringBuilder cheminStr = new StringBuilder();
        for (Sommet sommet : chemin) {
            cheminStr.append(sommet.getNom()).append(" -> ");
        }
        cheminStr.append("FIN");
        return "Valeur: " + valeur + " | Chemin: " + cheminStr;
    }

    public String toStringProba() {
        // Construire la représentation du chemin
        StringBuilder cheminStr = new StringBuilder();
        for (Sommet sommet : chemin) {
            cheminStr.append(sommet.getNom()).append(" -> ");
        }
        cheminStr.append("FIN");
    
        // Formatter le taux de risque en pourcentage
        String probSucces = String.format("%.2f", accProbSucces * 100);
    
        // Déterminer le label de probabilité de succès en fonction du taux calculé
        String riskLabel;
        if (accProbSucces < 0.7) {
            riskLabel = "Faible";
        } else if (accProbSucces < 0.83) {
            riskLabel = "Modéré";
        } else {
            riskLabel = "Elevé";
        }
    
        // Retourner la chaîne formatée avec les informations de la valeur et du risque
        return String.format("Valeur: %.6f | Taux de succès: %s%% (%s) | Chemin: %s", valeur, probSucces, riskLabel, cheminStr);
    }
    
}
