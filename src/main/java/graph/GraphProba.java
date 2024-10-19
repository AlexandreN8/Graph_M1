package graph;

import graph.arc.ArcProba;
import graph.utilities.Sommet;

public class GraphProba extends Graph {

    @Override
    protected void createGraph() {
        Sommet euro = new Sommet(1, "Euro", 200, 100);
        Sommet dollar = new Sommet(2, "Dollar", 600, 100);
        Sommet livre = new Sommet(3, "Livre Sterling", 200, 400);
        Sommet franc = new Sommet(4, "Franc Suisse", 600, 400);

        addSommet(euro);
        addSommet(dollar);
        addSommet(livre);
        addSommet(franc);

        setInitialSommet(euro);

        // Créer les arcs doublement valués
        ArcProba euroToDollar = new ArcProba(euro, dollar, 1.19, 0.05, false);
        ArcProba dollarToEuro = new ArcProba(dollar, euro, 0.84, 0.05,  true);
        ArcProba euroToLivre = new ArcProba(euro, livre, 1.33, 0.08, false);
        ArcProba livreToEuro = new ArcProba(livre, euro, 0.75, 0.08, true);
        ArcProba euroToFranc = new ArcProba(euro, franc, 1.62, 0.07, false);
        ArcProba francToEuro = new ArcProba(franc, euro, 0.62, 0.07, true);
        ArcProba dollarToLivre = new ArcProba(dollar, livre, 1.12, 0.06, false);
        ArcProba livreToDollar = new ArcProba(livre, dollar, 0.89, 0.06, true);
        ArcProba dollarToFranc = new ArcProba(dollar, franc, 1.37, 0.04, false);
        ArcProba francToDollar = new ArcProba(franc, dollar, 0.73, 0.04, true);
        ArcProba livreToFranc = new ArcProba(livre, franc, 1.22, 0.09, false);
        ArcProba francToLivre = new ArcProba(franc, livre, 0.82, 0.09, true);

        addArc(euroToDollar);
        addArc(dollarToEuro);
        addArc(euroToLivre);
        addArc(livreToEuro);
        addArc(euroToFranc);
        addArc(francToEuro);
        addArc(dollarToLivre);
        addArc(livreToDollar);
        addArc(dollarToFranc);
        addArc(francToDollar);
        addArc(livreToFranc);
        addArc(francToLivre);
    }
}
