package graph;

import graph.arc.Arc;
import graph.arc.ArcStandard;
import graph.utilities.Sommet;

public class GraphProject extends Graph {

    @Override
    protected void createGraph() {
        // creer et ajouter les sommet au graphe
        Sommet euro = new Sommet(1, "Euro", 200, 100);
        Sommet dollar = new Sommet(2, "Dollar", 600, 100);
        Sommet livre = new Sommet(3, "Livre Sterling", 200, 400);
        Sommet franc = new Sommet(4, "Franc Suisse", 600, 400);
        addSommet(euro);
        addSommet(dollar);
        addSommet(livre);
        addSommet(franc);

        setInitialSommet(euro);

        // creer et ajouter les arcs au graphe
        Arc euroToDollar = new ArcStandard(euro, dollar, 1.19, false);
        Arc dollarToEuro = new ArcStandard(dollar, euro, 0.84, true);
        Arc euroToLivre = new ArcStandard(euro, livre, 1.33, false);
        Arc livreToEuro = new ArcStandard(livre, euro, 0.75, true);
        Arc euroToFranc = new ArcStandard(euro, franc, 1.62, false);
        Arc francToEuro = new ArcStandard(franc, euro, 0.62, true);
        Arc dollarToLivre = new ArcStandard(dollar, livre, 1.12, false);
        Arc livreToDollar = new ArcStandard(livre, dollar, 0.89, true);
        Arc dollarToFranc = new ArcStandard(dollar, franc, 1.37, false);
        Arc francToDollar = new ArcStandard(franc, dollar, 0.73, true);
        Arc livreToFranc = new ArcStandard(livre, franc, 1.22, false);
        Arc francToLivre = new ArcStandard(franc, livre, 0.82, true);

        Arc[] arcs = {euroToDollar, dollarToEuro, euroToLivre, livreToEuro, euroToFranc, francToEuro, dollarToLivre, livreToDollar, dollarToFranc, francToDollar, livreToFranc, francToLivre};
        for (Arc arc : arcs) {
            addArc(arc);
        }
    }
}
