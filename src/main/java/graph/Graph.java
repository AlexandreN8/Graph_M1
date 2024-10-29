package graph;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Stack;
import java.util.HashSet;
import java.util.Set;


import graph.arc.Arc;
import graph.arc.ArcProba;
import graph.utilities.PathMu;
import graph.utilities.Sommet;

public abstract class Graph {
    
        protected List<Sommet> sommets;
        protected List<Arc> arcs;
        protected Sommet initialSommet;
    
        public Graph() {
            this.sommets = new ArrayList<>();
            this.arcs = new ArrayList<>();
            this.initialSommet = null;

            createGraph();
        }
    
        public List<Sommet> getSommets() {
            return sommets;
        }
    
        public List<Arc> getArcs() {
            return arcs;
        }

        public Sommet getInitialSommet() {
            return initialSommet;
        }

        public void setInitialSommet(Sommet initialSommet) {
            this.initialSommet = initialSommet;
        }
    
        public void setSommets(List<Sommet> sommets) {
            this.sommets = sommets;
        }
    
        public void setArcs(List<Arc> arcs) {
            this.arcs = arcs;
        }
    
        public void addSommet(Sommet sommet) {
            this.sommets.add(sommet);
        }
    
        public void addArc(Arc arc) {
            this.arcs.add(arc);
        }
    
        @Override
        public String toString() {
            return "Graph{" +
                    "sommets=" + sommets +
                    ", arcs=" + arcs +
                    '}';
        }

        // --- Méthodes 
        // -- Abstract method pour initialiser les differents graphes
        protected abstract void createGraph();

        // -- Méthode de manipulation du graphe 
        // Méthode pour obtenir les voisins d'un sommet
        private List<Sommet> obtenirVoisins(Sommet sommet) {
            List<Sommet> voisins = new ArrayList<>();
            for (Arc arc : arcs) {
                if (arc.getSommetDepart().equals(sommet)) {
                    voisins.add(arc.getSommetArrivee());
                }
            }
            return voisins;
        }
    
        // Nouvelle méthode pour supprimer un sommet
        public void removeSommet(Sommet sommet) {
            this.sommets.remove(sommet); // Supprimer le sommet de la liste des sommets
        }

        // Nouvelle méthode pour supprimer un arc
        public void removeArc(Arc arc) {
            this.arcs.remove(arc); // Supprimer l'arc de la liste des arcs
        }

        // Méthode pour trouver un arc entre deux sommets
        public Arc trouverArc(Sommet depart, Sommet arrivee) {
            for (Arc arc : arcs) {
                if (arc.getSommetDepart().equals(depart) && arc.getSommetArrivee().equals(arrivee)) {
                    return arc;
                }
            }
            return null; // Aucun arc trouvé
        }


        // Méthode pour calculer la valeur du chemin en fonction des arcs
        private double calculerValeur(List<Sommet> chemin) {
            double valeur = 1.0; // On part avec 1 unité de la devise de départ
            for (int i = 0; i < chemin.size() - 1; i++) {
                Sommet sommetDepart = chemin.get(i);
                Sommet sommetArrivee = chemin.get(i + 1);
                Arc arc = trouverArc(sommetDepart, sommetArrivee);
                if (arc != null) {
                    valeur *= arc.getChange();
                }
            }
            // Retour au sommet initial (sommet de départ)
            Sommet dernierSommet = chemin.get(chemin.size() - 1);
            Arc arcRetour = trouverArc(dernierSommet, chemin.get(0));
            if (arcRetour != null) {
                valeur *= arcRetour.getChange();
            }
            return valeur;
        }

        /*********** ALGO ************/  
        public List<PathMu> bruteForceNaive(Sommet sommetDepart, int p, int maxChemins) {
            List<PathMu> cheminsMu = new ArrayList<>();  // Stocker les meilleurs chemins qui reviennent à l'Euro
            Set<String> cheminsUniques = new HashSet<>();  // Set pour éviter les doublons
        
            // Pile pour stocker les chemins partiels
            Stack<List<Sommet>> pile = new Stack<>();
            List<Sommet> cheminInitial = new ArrayList<>();
            cheminInitial.add(sommetDepart);
            pile.push(cheminInitial);
        
            // Boucle principale pour explorer tous les chemins possibles
            while (!pile.isEmpty()) {
                List<Sommet> cheminCourant = pile.pop();
                Sommet dernierSommet = cheminCourant.get(cheminCourant.size() - 1);
        
                // Continuer à explorer les voisins si on n'a pas encore atteint p étapes
                if (cheminCourant.size() < p) {
                    List<Sommet> voisins = obtenirVoisins(dernierSommet);
        
                    // Explorer chaque voisin et prolonger le chemin
                    for (Sommet voisin : voisins) {
                        List<Sommet> nouveauChemin = new ArrayList<>(cheminCourant);
                        nouveauChemin.add(voisin);
                        pile.push(nouveauChemin);
                    }
                }
        
                // Si on atteint p, vérifier si le chemin revient à Euro (sommet de départ)
                if (cheminCourant.size() == p) { // note : le nombre d'echange = p - 1
                    if (dernierSommet.equals(sommetDepart)) {
                        // Si le chemin finit déjà à "Euro", on l'ajoute sans modifier
                        String cheminStr = cheminCourant.toString();
        
                        if (!cheminsUniques.contains(cheminStr)) {
                            double valeurRetour = calculerValeur(cheminCourant);
        
                            // Ajouter le chemin si on ne depasse pas encore la limite maxChemins
                            if (cheminsMu.size() < maxChemins) {
                                cheminsMu.add(new PathMu(cheminCourant, valeurRetour));  // Stocker le chemin
                            } else {
                                // Sinon remplacer le chemin avec la plus petite valeur pour éviter lee stackOverflow
                                PathMu cheminMin = cheminsMu.stream()
                                    .min(Comparator.comparingDouble(PathMu::getValeur))
                                    .orElse(null);
        
                                if (cheminMin != null && cheminMin.getValeur() < valeurRetour) {
                                    cheminsMu.remove(cheminMin);  // Retirer le plus petit chemin
                                    cheminsMu.add(new PathMu(cheminCourant, valeurRetour));  // Ajouter le nouveau chemin
                                }
                            }
                            // Ajouter le chemin à la liste des chemins
                            cheminsUniques.add(cheminStr);
                        }
                    } else {
                        // Sinon, ajouter le retour à "Euro" pour compléter le chemin
                        List<Sommet> cheminRetourEuro = new ArrayList<>(cheminCourant);
                        cheminRetourEuro.add(sommetDepart);  // Retour à l'Euro
        
                        String cheminStr = cheminRetourEuro.toString();
                        if (!cheminsUniques.contains(cheminStr)) {
                            double valeurRetour = calculerValeur(cheminRetourEuro);
        
                            // Mêmes verifications que précédemment pour éviter le stack overflow
                            if (cheminsMu.size() < maxChemins) {
                                cheminsMu.add(new PathMu(cheminRetourEuro, valeurRetour)); 
                            } else {
                                PathMu cheminMin = cheminsMu.stream()
                                    .min(Comparator.comparingDouble(PathMu::getValeur))
                                    .orElse(null);
        
                                if (cheminMin != null && cheminMin.getValeur() < valeurRetour) {
                                    cheminsMu.remove(cheminMin); 
                                    cheminsMu.add(new PathMu(cheminRetourEuro, valeurRetour));
                                }
                            }
                            cheminsUniques.add(cheminStr);
                        }
                    }
                }
            }
            // Retourner la liste des chemins trouvés
            return cheminsMu;
        }
        
        // -- 2eme Mandatory : Algorithme de Dijkstra simplifié Local
        /*
         * V1 : Cette version de l'algorithme ne considère que les chemin complet (retour en euro) qui font partis de l'optimum
         * Autrement dit on ne mémorise pas les chemin complet que l'on emprunte pas
         * 
         * La V2 quand à elle mémorise les chemins complet que l'on emprunte pas ce qui est une optimisation en temps constant
         * Note : C'est la V2 qu'on utilise dans le projet
         * 
         */
        public List<PathMu> maximizeDijkstraSimplifieLocalV1(Sommet monnaie, int p) {
            // Liste pour stocker tous les chemins complets
            List<PathMu> cheminsComplets = new ArrayList<>();
        
            // Variables locales : cheminsActuels stocke le meilleur chemin vers chaque sommet
            Map<Sommet, PathMu> cheminsActuels = new HashMap<>();
            // Initialisation : Chemin partant de la monnaie initiale avec une valeur de 1
            cheminsActuels.put(monnaie, new PathMu(new ArrayList<>(List.of(monnaie)), 1.0));
        
            // Itérations (k = 1 jusqu'à p) : explorer les chemins possibles
            for (int k = 1; k <= p; k++) {
                Map<Sommet, PathMu> nouveauxChemins = new HashMap<>();
        
                // Parcours des chemins actuels
                for (Map.Entry<Sommet, PathMu> entry : cheminsActuels.entrySet()) {
                    Sommet sommetCourant = entry.getKey();
                    PathMu cheminActuel = entry.getValue();
        
                    if (k < p) {
                        // Pour k < p : prolonger les chemins vers les autres sommets
                        for (Arc arc : arcs) {
                            if (arc.getSommetDepart().equals(sommetCourant)) {
                                Sommet sommetArrivee = arc.getSommetArrivee();
        
                                // Construire un nouveau chemin
                                List<Sommet> nouveauChemin = new ArrayList<>(cheminActuel.getChemin());
                                nouveauChemin.add(sommetArrivee);
                                double nouvelleValeur = cheminActuel.getValeur() * arc.getChange();
        
                                PathMu nouveauPathMu = new PathMu(nouveauChemin, nouvelleValeur);
        
                                // Si le sommet n'a pas encore été atteint OU si on a trouvé un meilleur chemin
                                if (!nouveauxChemins.containsKey(sommetArrivee) || 
                                    nouveauxChemins.get(sommetArrivee).getValeur() < nouvelleValeur) {
                                    nouveauxChemins.put(sommetArrivee, nouveauPathMu);
                                }
                            }
                        }
                    } else {
                        // k = p - 1 : à l'itération p, on force le retour à la devise de départ
                        Arc arcRetour = trouverArc(sommetCourant, monnaie);
                        if (arcRetour != null) {
                            // Construire le chemin de retour
                            List<Sommet> cheminComplet = new ArrayList<>(cheminActuel.getChemin());
                            cheminComplet.add(monnaie);
        
                            // Calculer la valeur finale du chemin
                            double valeurFinale = cheminActuel.getValeur() * arcRetour.getChange();
                            PathMu cheminFinal = new PathMu(cheminComplet, valeurFinale);
        
                            // Ajouter le chemin complet à la liste des chemins complets
                            cheminsComplets.add(cheminFinal);
                        }
                    }
                }
                // Mettre à jour cheminsActuels avec les nouveaux chemins trouvés (on garde les meilleurs)
                cheminsActuels = nouveauxChemins;
            }
        
            // Retourner tous les chemins complets avec leurs valeurs
            return cheminsComplets;
        }

        // V2 : mémorisation des chemins
        public List<PathMu> maximizeDijkstraSimplifieLocal(Sommet monnaie, int p) {
            List<PathMu> cheminsComplets = new ArrayList<>(); // Liste pour stocker les chemins complets
        
            // Chemins actuels pour chaque sommet
            Map<Sommet, PathMu> cheminsActuels = new HashMap<>();
            cheminsActuels.put(monnaie, new PathMu(new ArrayList<>(List.of(monnaie)), 1.0));
        
            // Itérations pour explorer les chemins possibles jusqu'à p étapes
            for (int k = 1; k <= p; k++) {
                Map<Sommet, PathMu> nouveauxChemins = new HashMap<>();
        
                // Parcourir les chemins actuels
                for (Map.Entry<Sommet, PathMu> entry : cheminsActuels.entrySet()) {
                    Sommet sommetCourant = entry.getKey();
                    PathMu cheminActuel = entry.getValue();
        
                    if (k < p) {
                        // Pour k < p : prolonger les chemins vers les autres sommets
                        for (Arc arc : arcs) {
                            if (arc.getSommetDepart().equals(sommetCourant)) {
                                Sommet sommetArrivee = arc.getSommetArrivee();
                                List<Sommet> nouveauChemin = new ArrayList<>(cheminActuel.getChemin());
                                nouveauChemin.add(sommetArrivee);
                                double nouvelleValeur = cheminActuel.getValeur() * arc.getChange();
        
                                PathMu nouveauPathMu = new PathMu(nouveauChemin, nouvelleValeur);
        
                                // Comparer avec les autres chemins
                                if (!nouveauxChemins.containsKey(sommetArrivee) || 
                                    nouveauxChemins.get(sommetArrivee).getValeur() < nouvelleValeur) {
                                    nouveauxChemins.put(sommetArrivee, nouveauPathMu);
                                }
                            }
                        }
                    }
        
                    // Mémorisation
                    // À chaque itération, vérifier si un chemin court est déjà complet et meilleur
                    Arc arcRetour = trouverArc(sommetCourant, monnaie);
                    if (arcRetour != null) {
                        List<Sommet> cheminComplet = new ArrayList<>(cheminActuel.getChemin());
                        cheminComplet.add(monnaie);
                        double valeurFinale = cheminActuel.getValeur() * arcRetour.getChange();
                        
                        PathMu cheminFinal = new PathMu(cheminComplet, valeurFinale);
        
                        boolean estMeilleurChemin = true;
        
                        // Vérifier si ce chemin complet est meilleur que ceux existants
                        for (PathMu chemin : cheminsComplets) {
                            if (chemin.getChemin().equals(cheminComplet) && chemin.getValeur() >= valeurFinale) {
                                estMeilleurChemin = false;
                                break;
                            }
                        }
        
                        // Si c'est un meilleur chemin ou qu'il n'existe pas, on l'ajoute
                        if (estMeilleurChemin) {
                            cheminsComplets.add(cheminFinal);
                        }
                    }
                }
        
                // Mettre à jour cheminsActuels avec les nouveaux chemins trouvés (on garde les meilleurs)
                cheminsActuels = nouveauxChemins;
            }
        
            return cheminsComplets;
        }        

        // -- 1er Opt : Algorithme de Dijkstra simplifié Global (version du poly projet)
        public PathMu maximizeDijkstraSimplifieGlobal(Sommet monnaie, int p) {
            // Map pour stocker la meilleure valeur et le chemin correspondant vers chaque sommet
            Map<Sommet, PathMu> cheminsActuels = new HashMap<>();
            // Initialisation : Chemin partant de la monnaie initiale avec une valeur de 1
            cheminsActuels.put(monnaie, new PathMu(new ArrayList<>(List.of(monnaie)), 1.0));

            // Itérations (k = 1 jusqu'à p) : explorer les chemins possibles
            for (int k = 1; k <= p; k++) {
                // Conserver la meilleure valeur globale des itérations précédentes
                Map<Sommet, PathMu> nouveauxChemins = new HashMap<>(cheminsActuels);

                // Parcourir toutes les devises (sommet)
                for (Sommet sommetJ : sommets) {
                    PathMu meilleurChemin = cheminsActuels.get(sommetJ);

                    if (k < p) {
                        // Comparer avec tous les autres sommets pour trouver un meilleur chemin via sommetI
                        for (Arc arc : arcs) {
                            if (arc.getSommetArrivee().equals(sommetJ)) {
                                Sommet sommetI = arc.getSommetDepart();
                                PathMu cheminDepuisI = cheminsActuels.get(sommetI);

                                if (cheminDepuisI != null) {
                                    // Calculer la nouvelle valeur en passant par sommetI vers sommetJ
                                    double nouvelleValeur = cheminDepuisI.getValeur() * arc.getChange();
                                    List<Sommet> nouveauChemin = new ArrayList<>(cheminDepuisI.getChemin());
                                    nouveauChemin.add(sommetJ);

                                    // Si c'est une meilleure valeur globale, on met à jour
                                    if (meilleurChemin == null || nouvelleValeur > meilleurChemin.getValeur()) {
                                        meilleurChemin = new PathMu(nouveauChemin, nouvelleValeur);
                                        nouveauxChemins.put(sommetJ, meilleurChemin);
                                    }
                                }
                            }
                        }
                    } else {
                        // À l'itération k = p, comparer le retour à la devise de départ via tous les chemins possibles
                        for (Arc arcRetour : arcs) {
                            if (arcRetour.getSommetArrivee().equals(monnaie)) {
                                Sommet sommetI = arcRetour.getSommetDepart();
                                PathMu cheminDepuisI = cheminsActuels.get(sommetI);

                                if (cheminDepuisI != null) {
                                    // Construire le chemin de retour et calculer la nouvelle valeur
                                    List<Sommet> cheminComplet = new ArrayList<>(cheminDepuisI.getChemin());
                                    cheminComplet.add(monnaie);
                                    double nouvelleValeur = cheminDepuisI.getValeur() * arcRetour.getChange();

                                    // Comparer avec la valeur précédente et mettre à jour
                                    if (!nouveauxChemins.containsKey(monnaie) || nouvelleValeur > nouveauxChemins.get(monnaie).getValeur()) {
                                        nouveauxChemins.put(monnaie, new PathMu(cheminComplet, nouvelleValeur));
                                    }
                                }
                            }
                        }
                    }
                }
                // Mettre à jour cheminsActuels avec les nouvelles valeurs maximales trouvées
                cheminsActuels = nouveauxChemins;
            }

            // Retourner le meilleur chemin global qui revient à la devise de départ
            return cheminsActuels.get(monnaie);
        }

        public List<PathMu> maximizeLocalProba(Sommet monnaie, int p) {
            List<PathMu> cheminsComplets = new ArrayList<>();
        
            // CheminsActuels stocke les meilleurs chemins vers chaque sommet et leurs taux de succès
            Map<Sommet, PathMu> cheminsActuels = new HashMap<>();
            cheminsActuels.put(monnaie, new PathMu(new ArrayList<>(List.of(monnaie)), 1.0, 1.0)); // Valeur initiale à 1 et succès à 100%
        
            // Itérations pour explorer les chemins possibles jusqu'à p échanges
            for (int k = 1; k <= p; k++) {
                Map<Sommet, PathMu> nouveauxChemins = new HashMap<>();
        
                // Parcours des chemins actuels
                for (Map.Entry<Sommet, PathMu> entry : cheminsActuels.entrySet()) {
                    Sommet sommetCourant = entry.getKey();
                    PathMu cheminActuel = entry.getValue();
        
                    if (k < p) {
                        // Pour k < p : prolonger les chemins vers les autres sommets
                        for (Arc arc : arcs) {
                            if (arc.getSommetDepart().equals(sommetCourant)) {
                                Sommet sommetArrivee = arc.getSommetArrivee();
        
                                // Prolonger le chemin actuel
                                List<Sommet> nouveauChemin = new ArrayList<>(cheminActuel.getChemin());
                                nouveauChemin.add(sommetArrivee);
                                double nouvelleValeur = cheminActuel.getValeur() * arc.getChange();
        
                                // Calculer le taux de succès accumulé (multiplication des probabilités de succès)
                                double accTauxSucces = cheminActuel.getAccProbSucces(); // Probabilité de succès accumulée
                                if (arc instanceof ArcProba) {
                                    ArcProba arcProba = (ArcProba) arc;
                                    accTauxSucces *= (1 - arcProba.getRiskFactor()); // Multiplier la probabilité de succès
                                }
        
                                PathMu nouveauPathMu = new PathMu(nouveauChemin, nouvelleValeur, accTauxSucces);
        
                                // Critère d'ajustement : Maximiser la valeur tout en minimisant le risque / maximisant le succès
                                if (!nouveauxChemins.containsKey(sommetArrivee) ||
                                    (nouveauPathMu.getValeur() * nouveauPathMu.getAccProbSucces()) > // Valeur pondérée par le succès
                                    (nouveauxChemins.get(sommetArrivee).getValeur() * nouveauxChemins.get(sommetArrivee).getAccProbSucces())) {
                                    nouveauxChemins.put(sommetArrivee, nouveauPathMu);
                                }
                            }
                        }
                    }
                    // Vérifier si un chemin complet est atteint et estMeilleur
                    Arc arcRetour = trouverArc(sommetCourant, monnaie);
                    if (arcRetour != null) {
                        List<Sommet> cheminComplet = new ArrayList<>(cheminActuel.getChemin());
                        cheminComplet.add(monnaie);
                        double valeurFinale = cheminActuel.getValeur() * arcRetour.getChange();
                        double accTauxSucces = cheminActuel.getAccProbSucces();

                        if (arcRetour instanceof ArcProba) {
                            accTauxSucces *= (1 - ((ArcProba) arcRetour).getRiskFactor());
                        }

                        PathMu cheminFinal = new PathMu(cheminComplet, valeurFinale, accTauxSucces);
                        boolean estMeilleurChemin = true;

                        for (PathMu chemin : cheminsComplets) {
                            if (chemin.getChemin().equals(cheminComplet) && chemin.getValeur() >= valeurFinale) {
                                estMeilleurChemin = false;
                                break;
                            }
                        }

                        if (estMeilleurChemin) {
                            cheminsComplets.add(cheminFinal);
                        }
                    }
                }
        
                // Mettre à jour les chemins actuels avec les nouveaux chemins trouvés (garder les meilleurs)
                cheminsActuels = nouveauxChemins;
            }
        
            // Retourner tous les chemins complets trouvés avec leurs valeurs et succès
            return cheminsComplets;
        }       
}