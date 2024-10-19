package graph;

import graph.arc.Arc;
import graph.arc.ArcStandard;
import graph.utilities.Sommet;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GraphLarge extends Graph {

    private int sommetIdCounter = 10; // Initialiser à 10, car 10 sommets initiaux

    @Override
    protected void createGraph() {
        Random random = new Random();

        // Créer les 10 sommets initiaux
        for (int i = 1; i <= 10; i++) {
            Sommet sommet = new Sommet(i, "Sommet " + i, random.nextInt(1000), random.nextInt(1000));
            addSommet(sommet);
        }

        // Ajouter des arcs pour assurer la forte connexité sans boucle interne
        for (Sommet sommetDepart : getSommets()) {
            for (Sommet sommetArrivee : getSommets()) {
                if (!sommetDepart.equals(sommetArrivee)) {
                    double tauxChange = 0.5 + (1.5 * random.nextDouble()); // Taux de change aléatoire
                    Arc arc = new ArcStandard(sommetDepart, sommetArrivee, tauxChange, true);
                    addArc(arc);
                }
            }
        }

        setInitialSommet(getSommets().get(0)); // Définir le premier sommet comme sommet initial
    }

    // Méthode pour ajouter un sommet supplémentaire
    public void ajouterSommet() {
        Random random = new Random();
        sommetIdCounter++; // Incrémenter le compteur d'ID des sommets
        Sommet nouveauSommet = new Sommet(sommetIdCounter, "Sommet " + sommetIdCounter, random.nextInt(1000), random.nextInt(1000));
        addSommet(nouveauSommet);

        // Ajouter des arcs pour connecter le nouveau sommet aux autres sommets
        for (Sommet sommet : getSommets()) {
            if (!sommet.equals(nouveauSommet)) {
                double tauxChangeVers = 0.5 + (1.5 * random.nextDouble());
                Arc arcVers = new ArcStandard(nouveauSommet, sommet, tauxChangeVers, true);
                addArc(arcVers);

                double tauxChangeDepuis = 0.5 + (1.5 * random.nextDouble());
                Arc arcDepuis = new ArcStandard(sommet, nouveauSommet, tauxChangeDepuis, true);
                addArc(arcDepuis);
            }
        }
    }

    // Méthode pour supprimer le dernier sommet ajouté
    public void supprimerDernierSommet() {
        if (!getSommets().isEmpty()) {
            Sommet dernierSommet = getSommets().get(getSommets().size() - 1);

            // Supprimer les arcs liés au sommet
            List<Arc> arcsASupprimer = new ArrayList<>();
            for (Arc arc : getArcs()) {
                if (arc.getSommetDepart().equals(dernierSommet) || arc.getSommetArrivee().equals(dernierSommet)) {
                    arcsASupprimer.add(arc);
                }
            }

            // Retirer les arcs du graphe
            for (Arc arc : arcsASupprimer) {
                removeArc(arc);
            }

            // Supprimer le sommet du graphe
            removeSommet(dernierSommet);
        } else {
            System.out.println("Aucun sommet à supprimer.");
        }
    }
}
