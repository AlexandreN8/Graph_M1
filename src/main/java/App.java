import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import graph.Graph;
import graph.GraphLarge;
import graph.GraphProject;
import graph.GraphProba;

import graph.arc.Arc;
import graph.utilities.PathMu;
import graph.utilities.Sommet;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import view_components.ZoomableScrollPane;

public class App extends Application {

    private int p = 3; // Variable pour le nombre d'échanges
    private List<PathMu> cheminsComplets; // Stocke tous les chemins mu générés
    private PathMu cheminComplet; // Stocke le chemin mu actuel
    private Pane graphPane = new Pane();  // Pane pour le graphe
    private List<Line> animatedLines = new ArrayList<>();  // Stocker les lignes créées pour chaque animation
    private Map<Arc, Integer> arcPassages = new HashMap<>();  // Stocker le nombre de passages par arc
    private ZoomableScrollPane zoomableScrollPane;
    private ListView<String> cheminResume;

    private Thread bruteForceThread;  // Le thread du brute force
    private AnimationTimer timer;  // Variable globale pour stocker l'AnimationTimer
    private long elapsedTime; // Temps écoulé pour le calcul

    // chart
    private Pane chartPane;
    private LineChart<Number, Number> lineChart;
    private XYChart.Series<Number, Number> bruteForceSeries;
    private XYChart.Series<Number, Number> dijkstraLocalSeries;
    private XYChart.Series<Number, Number> dijkstraGlobalSeries;
    private XYChart.Series<Number, Number> probabilistSeries;
    private RadioButton pOption;
    private RadioButton nOption;
    private ComboBox<String> timeUnitComboBox;
    // Maps distinctes pour chaque algorithme
    private Map<Integer, List<Double>> pToTimeMapBruteForce = new HashMap<>();
    private Map<Integer, List<Double>> pToTimeMapDijkstraLocal = new HashMap<>();
    private Map<Integer, List<Double>> pToTimeMapDijkstraGlobal = new HashMap<>();
    private Map<Integer, List<Double>> pToTimeMapProbabilist = new HashMap<>();

    private Map<Integer, List<Double>> nToTimeMapBruteForce = new HashMap<>();
    private Map<Integer, List<Double>> nToTimeMapDijkstraLocal = new HashMap<>();
    private Map<Integer, List<Double>> nToTimeMapDijkstraGlobal = new HashMap<>();
    private Map<Integer, List<Double>> nToTimeMapProbabilist = new HashMap<>();

    // Map pour stocker les temps originaux pour p (pour conversion d'unités)
    private Map<Integer, List<Double>> originalTimeMapBruteForceP = new HashMap<>();
    private Map<Integer, List<Double>> originalTimeMapDijkstraLocalP = new HashMap<>();
    private Map<Integer, List<Double>> originalTimeMapDijkstraGlobalP = new HashMap<>();
    private Map<Integer, List<Double>> originalTimeMapProbabilistP = new HashMap<>();

    // Map pour stocker les temps originaux pour n
    private Map<Integer, List<Double>> originalTimeMapBruteForceN = new HashMap<>();
    private Map<Integer, List<Double>> originalTimeMapDijkstraLocalN = new HashMap<>();
    private Map<Integer, List<Double>> originalTimeMapDijkstraGlobalN = new HashMap<>();
    private Map<Integer, List<Double>> originalTimeMapProbabilistN = new HashMap<>();


    // Creer et instancier les 3 graphes
    private Graph projectGraph;
    private Graph largeGraph;
    private Graph probabilistGraph;
    private Graph currentGraph = projectGraph;

    private void initializeGraphs() {
        projectGraph = new GraphProject();
        largeGraph = new GraphLarge();
        probabilistGraph = new GraphProba();

        currentGraph = projectGraph;
    }

    @Override
    public void start(Stage primaryStage) {
        // --- INTERFACE GRAPHIQUE ---
        // Créer le layout principal (BorderPane)
        BorderPane root = new BorderPane();

        // --- OVERLAY
        // Créer l'overlay pour le chargement
        StackPane overlay = new StackPane();  // Overlay du chargement
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);");  // Fond semi-transparent

        // VBox pour contenir le chargement, le label et le bouton annuler
        VBox overlayContent = new VBox(10); 
        overlayContent.setPadding(new Insets(20));
        overlayContent.setStyle("-fx-background-color: white; -fx-border-radius: 10; -fx-background-radius: 10;");

        // Indicateur de progression
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(100, 100);

        // Label pour le texte de chargement et le temps écoulé
        Label loadingLabel = new Label("Calcul en cours... Temps écoulé : 0 sec");
        
        // Bouton annuler
        Button cancelButton = new Button("Annuler");

        // Ajouter l'indicateur, le label et le bouton annuler à l'overlayContent
        overlayContent.getChildren().addAll(progressIndicator, loadingLabel, cancelButton);
        overlayContent.setAlignment(Pos.CENTER);  // Centrer le contenu

        // Ajouter l'overlayContent au centre de l'overlay
        overlay.getChildren().add(overlayContent);
        overlay.setAlignment(Pos.CENTER);  // Centrer l'overlay

        // Initialement, masquer l'overlay
        overlay.setVisible(false);

        // Ajouter l'overlay dans le StackPane
        StackPane rootWithOverlay = new StackPane();
        rootWithOverlay.getChildren().addAll(root, overlay);

        // --- PARTIE HAUTE: HBox divisée pour inclure les boutons et le graphe ---
        HBox topLayout = new HBox(10); // Espacement de 10 entre les éléments

        // --- partie gauche: Boutons pour changer de graphe, superposés au graphe ---
        VBox leftButtons = new VBox(10); // Espacement de 10 entre les boutons
        leftButtons.setPadding(new Insets(10)); 
        Button loadProjectGraph = new Button("Graphe Projet");
        Button loadLargeGraph = new Button("Graphe Personalisable");
        Button loadProbabilistGraph = new Button("Graphe Probabiliste");
        
        // Réduire la taille des boutons
        loadProjectGraph.setPrefWidth(100);  
        loadProjectGraph.setPrefHeight(25);  
        loadLargeGraph.setPrefWidth(100);
        loadLargeGraph.setPrefHeight(25);
        loadProbabilistGraph.setPrefWidth(100);
        loadProbabilistGraph.setPrefHeight(25);
        leftButtons.getChildren().addAll(loadProjectGraph, loadLargeGraph, loadProbabilistGraph);

        // --- Partie droite: ZoomableScrollPane avec le graphe ---
        graphPane = new Pane();  // Pane pour le graphe
        zoomableScrollPane = new ZoomableScrollPane(graphPane);
        zoomableScrollPane.setPrefSize(600, 350);  // Largeur et hauteur fixes du ScrollPane

        // Charts propre au Graphe Personalisable
        chartPane = new Pane();  // Pane pour le chart
        chartPane.setPrefSize(600, 350);  // Largeur et hauteur fixes du Pane
        chartPane.setVisible(false);

        // Créer un conteneur qui contiendra le zoomableScrollPane et le chartPane
        StackPane graphOrChartContainer = new StackPane();
        graphOrChartContainer.getChildren().addAll(zoomableScrollPane, chartPane);

        // Ajouter les boutons et le conteneur du graphe/chart dans le HBox (avec 2/3 de la largeur pour le graphe/chart)
        topLayout.getChildren().addAll(leftButtons, graphOrChartContainer);
        HBox.setHgrow(graphOrChartContainer, javafx.scene.layout.Priority.ALWAYS);  // S'assurer que le conteneur prend l'espace restant

        // Ajouter le HBox en haut de la racine
        root.setTop(topLayout);

        // Instanciation des graphes et dessin du currentGraph
        initializeGraphs();
        drawGraph(); 
        
        // --- PARTIE BASSE: Contrôles et Résumé des résultats ---
        VBox bottomLayout = new VBox(10); // VBox pour tout ce qui est en bas (boutons, chemins, réglages)
        bottomLayout.setPadding(new Insets(10)); // Espacement autour du layout inférieur

        // 1. Boutons de contrôle pour les algorithmes (HBox)
        HBox controlButtons = new HBox(10); // Espacement horizontal de 10 pour les boutons
        Button launchBrutForce = new Button("Lancer Brut Force");
        Button launchCommun = new Button("Lancer Simplifié Local");
        Button launchDijkstra = new Button("Lancer Simplifié Global");
        Button launchProbabiliste = new Button("Lancer Local probabiliste");
        controlButtons.getChildren().addAll(launchBrutForce, launchCommun, launchDijkstra, launchProbabiliste);
        launchProbabiliste.setVisible(false); // Masquer le bouton pour l'algorithme probabiliste


        // 2. HBox pour diviser les mu paths et les réglages
        HBox pathsAndSettings = new HBox(10); // HBox pour diviser l'espace

        // --- PARTIE GAUCHE: Résumé des chemins parcourus ---
        VBox pathSummaryBox = new VBox(10); // VBox pour les mu paths
        cheminResume = new ListView<>();
        pathSummaryBox.getChildren().addAll(new Label("Résumé des chemins parcourus :"), cheminResume);

        // --- PARTIE DROITE: Réglages ---
        VBox settingsBox = new VBox(10); // VBox pour les réglages
        settingsBox.setPadding(new Insets(10)); // Espacement dans la box de réglages
        // Label pour afficher le nombre de sommets
        Label vertexCountLabel = new Label("Nb sommets : " + currentGraph.getSommets().size());

        // Boutons pour ajouter/supprimer des sommets
        Button addVertexButton = new Button("+");
        Button removeVertexButton = new Button("-");

        // HBox pour contenir le tout
        HBox vertexControlBox = new HBox(10); // Espacement de 10 entre les composants
        vertexControlBox.setAlignment(Pos.CENTER);
        vertexControlBox.getChildren().addAll(removeVertexButton, vertexCountLabel, addVertexButton);

        // Masquer les boutons en dehors du graphe personnalisable
        addVertexButton.setVisible(false);
        removeVertexButton.setVisible(false);

        // Boutons pour afficher le graphe et le chart
        HBox switchViewButtons = new HBox(10); // HBox pour les boutons de vue
        Button viewGraphButton = new Button("Vue Graphe");
        Button viewChartButton = new Button("Vue Chart");

        // Ajouter les boutons de vue à la HBox
        switchViewButtons.getChildren().addAll(viewGraphButton, viewChartButton);

        // Crer les boutons liés a la chart
        VBox chartButtons = new VBox(10); // HBox pour les boutons de vue
        chartButtons.setVisible(false);

        // Groupe de boutons pour choisir entre p et n
        HBox complexityBox = new HBox(10); // HBox pour les boutons de complexité
        ToggleGroup complexityGroup = new ToggleGroup();
        pOption = new RadioButton("Étudier p");
        nOption = new RadioButton("Étudier n");
        pOption.setToggleGroup(complexityGroup);
        nOption.setToggleGroup(complexityGroup);
        pOption.setSelected(true); // Sélectionner p par défaut

        // Ajouter les boutons de complexité à la HBox
        complexityBox.getChildren().addAll(new Label("Complexité :"), pOption, nOption);

        // ComboBox pour choisir l'unité de temps
        HBox timeUnitBox = new HBox(10); // HBox pour les contrôles de temps
        timeUnitComboBox = new ComboBox<>();
        timeUnitComboBox.getItems().addAll("ns", "ms", "s", "mn", "h");
        timeUnitComboBox.setValue("ns");  // Par défaut, on utilise les ns

        // Ajouter les contrôles de temps à la HBox
        timeUnitBox.getChildren().addAll(new Label("Unité de temps :"), timeUnitComboBox);

        // Ajouter le bouton pour effacer les data de la chart
        Button clearChartButton = new Button("Effacer les données");

        chartButtons.getChildren().addAll(complexityBox, timeUnitBox, clearChartButton);

        // Slider pour modifier p à la volée
        Label pLabel = new Label("Nombre d'échanges (p): " + p);
        Slider pSlider = new Slider(1, 10, p);  // Slider pour p, de 1 à 10
        pSlider.setShowTickLabels(true);
        pSlider.setShowTickMarks(true);
        pSlider.setMajorTickUnit(1);

        // Label pour afficher la durée de l'algorithme
        Label algoDurationLabel = new Label("Durée de l'algorithme : ");

        // Ajouter le slider et le label à la box des réglages
        settingsBox.getChildren().addAll(pLabel, pSlider, vertexControlBox, switchViewButtons, chartButtons);
        
        // Ajouter les mu paths à gauche et les réglages à droite dans la HBox
        pathsAndSettings.getChildren().addAll(pathSummaryBox, settingsBox);

        // Ajouter les boutons en haut du layout inférieur
        bottomLayout.getChildren().addAll(controlButtons, pathsAndSettings, algoDurationLabel);

        // Ajouter le layout inférieur complet à la racine
        root.setBottom(bottomLayout);

        // Instanciation de la chart
        initializeChart();

        // --- AJUSTEMENTS DE TAILLE ---
        // Faire en sorte que le contenu s'adapte à la taille de la fenêtre
        graphPane.prefWidthProperty().bind(root.widthProperty().multiply(0.7));
        graphPane.prefHeightProperty().bind(root.heightProperty().multiply(0.6));      
        chartPane.prefWidthProperty().bind(graphOrChartContainer.widthProperty());
        chartPane.prefHeightProperty().bind(graphOrChartContainer.heightProperty());
        // Ajuster la taille hoizontale du linechart pour prendre tout lespace disponible
        lineChart.prefWidthProperty().bind(chartPane.widthProperty());  

  

        // Ajuster les tailles pour la partie inférieure
        pathSummaryBox.prefWidthProperty().bind(pathsAndSettings.widthProperty().multiply(0.7));
        settingsBox.prefWidthProperty().bind(pathsAndSettings.widthProperty().multiply(0.3));
        cheminResume.prefHeightProperty().bind(pathsAndSettings.heightProperty());
        bottomLayout.maxHeightProperty().bind(root.heightProperty().multiply(0.42)); // 42% de la hauteur de la fenêtre
        bottomLayout.minHeightProperty().bind(root.heightProperty().multiply(0.42)); // 42% de la hauteur de la fenêtre
        // -- FIN INTERFACE GRAPHIQUE --
        
        // -- EVENTS --
        // ACTIONS BOUTON "Annuler le brute force"
        cancelButton.setOnAction(e -> {
            if (bruteForceThread != null && bruteForceThread.isAlive()) {
                bruteForceThread.interrupt();  // Interrompre le thread
            }
            if (timer != null) {
                timer.stop();  // Stopper le timer si le calcul est annulé
            }
            overlay.setVisible(false);  // Masquer l'overlay quand on annule
            launchBrutForce.setDisable(false);  // Réactiver le bouton pour permettre un nouveau lancement
            algoDurationLabel.setText("Durée de l'algorithme : Annulé");  // Afficher "Annulé" dans le label
        });        

        // ACTIONS BOUTON "Lancer Brut Force"        
        launchBrutForce.setOnAction(event -> {
            // Reset chemin, style et timer
            cheminResume.getItems().clear();
            resetGraphStyle(currentGraph.getArcs(), currentGraph);
            if (timer != null) {
                timer.stop();
            }
        
            loadingLabel.setText("Calcul en cours... Temps écoulé : 0 ms");
            final long startTime = System.nanoTime(); // Démarrer le timer

            // Afficher l'overlay
            overlay.setVisible(true);
            launchBrutForce.setDisable(true);  // Désactiver le bouton pour éviter plusieurs clics
        
            // Utilisation de l'AnimationTimer pour capturer les nano secondes écoulées
            AnimationTimer timer = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    elapsedTime = System.nanoTime() - startTime;
                    loadingLabel.setText("Calcul en cours... Temps écoulé : " + formatElapsedTime(elapsedTime));
                }
            };
            timer.start();
        
            // Lancer l'algorithme dans un thread
            bruteForceThread = new Thread(() -> {
                List<PathMu> resultatsChemins = currentGraph.bruteForceNaive(currentGraph.getInitialSommet(), p, 500000);
        
                // S'assurer que cheminsComplets est correctement mis à jour
                Platform.runLater(() -> {
                    cheminsComplets = resultatsChemins; //Assurer que cheminsComplets est mis à jour en dehors du thread
                    long finalElapsedTime = System.nanoTime() - startTime; // Calculer la durée
                    timer.stop();  // Arrêter le timer
                    overlay.setVisible(false);  // Masquer l'overlay
                    launchBrutForce.setDisable(false);  // Réactiver le bouton
        
                    // Trier les chemins par valeur décroissante
                    cheminsComplets.sort(Comparator.comparingDouble(PathMu::getValeur).reversed());
                    PathMu maxChemin = cheminsComplets.stream()
                        .max(Comparator.comparingDouble(PathMu::getValeur))
                        .orElse(null);
        
                    // Afficher les résultats dans le ListView
                    int index = 1;
                    for (PathMu chemin : cheminsComplets) {
                        String cheminString = "μ" + index + " = " + chemin.toString();
                        cheminResume.getItems().add(cheminString);
                        index++;
                    }

                    // Mettre en surbrillance le chemin avec la valeur maximale
                    highlightPath(maxChemin);
        
                    // Mettre en surbrillance bleu le chemin selectionné (maximum par defaut)
                    setupCheminListView(cheminResume, maxChemin);
        
                    // Utiliser le formatElapsedTime pour afficher la durée correcte dans le label
                    algoDurationLabel.setText("Durée de l'algorithme : " + formatElapsedTime(finalElapsedTime));
                    originalTimeMapBruteForceP.computeIfAbsent(p, k -> new ArrayList<>()).add((double) finalElapsedTime);
                    pToTimeMapBruteForce.computeIfAbsent(p, k -> new ArrayList<>()).add((double) finalElapsedTime);
                    
                    originalTimeMapBruteForceN.computeIfAbsent(currentGraph.getSommets().size(), k -> new ArrayList<>()).add((double) finalElapsedTime);
                    nToTimeMapBruteForce.computeIfAbsent(currentGraph.getSommets().size(), k -> new ArrayList<>()).add((double) finalElapsedTime);

                    // Mettre a jour la chart 
                    updateChartData();
                });
            });
            bruteForceThread.start();  // Lancer le thread
        });
        
        //  ACTIONS BOUTON "Lancer Dijkstra Simplifié Local (version du TD)" 
        launchCommun.setOnAction(event -> {
            cheminResume.getItems().clear(); // Effacer les anciens résultats
            resetGraphStyle(currentGraph.getArcs(), currentGraph); // Réinitialiser les styles des arcs et sommets

            // Début du timer
            algoDurationLabel.setText("Calcul en cours...");
            final long startTime = System.nanoTime(); // Démarrer le timer

            // Lancer l'algorithme
            cheminsComplets = currentGraph.maximizeDijkstraSimplifieLocal(currentGraph.getInitialSommet(), p); // Lancer l'algorithme

            // Fin du timer
            long finalElapsedTime = System.nanoTime() - startTime; // Calculer la durée
            algoDurationLabel.setText("Durée de l'algorithme : " + formatElapsedTime(finalElapsedTime)); // afficher la durée dans le lbl
            
            // Ajouter a la chart
            // Ajouter la durée moyenne au graphique
            pToTimeMapDijkstraLocal.computeIfAbsent(p, k -> new ArrayList<>()).add((double) finalElapsedTime);
            nToTimeMapDijkstraLocal.computeIfAbsent(currentGraph.getSommets().size(), k -> new ArrayList<>()).add((double) finalElapsedTime);

            originalTimeMapDijkstraLocalN.computeIfAbsent(currentGraph.getSommets().size(), k -> new ArrayList<>()).add((double) finalElapsedTime);
            originalTimeMapDijkstraLocalP.computeIfAbsent(p, k -> new ArrayList<>()).add((double) finalElapsedTime);

            // Mettre a jour la chart 
            updateChartData();

            // Trier les mu paths par valeur décroissante
            cheminsComplets.sort(Comparator.comparingDouble(PathMu::getValeur).reversed());

            // Trouver le chemin mu avec la valeur maximale
            PathMu maxChemin = cheminsComplets.stream()
                .max((chemin1, chemin2) -> Double.compare(chemin1.getValeur(), chemin2.getValeur()))
                .orElse(null);

            // Ajouter les chemins mu au résumé
            int index = 1;
            for (PathMu chemin : cheminsComplets) {
                String cheminString = "μ" + index + " = " + chemin.toString();
                cheminResume.getItems().add(cheminString);
                index++;
            }

            // Mettre en surbrillance le chemin avec la valeur maximale
            highlightPath(maxChemin);

            // Mettre en surbrillance bleu le chemin selectionné (maximum par defaut)
            setupCheminListView(cheminResume, maxChemin);
        });

        // ACTIONS BOUTON "Lancer Dijkstra Simplifié Global (version du projet papier)"
        launchDijkstra.setOnAction(event -> {
            cheminResume.getItems().clear(); // Effacer les anciens résultats
            resetGraphStyle(currentGraph.getArcs(), currentGraph); // Réinitialiser les styles des arcs et sommets

            // Début du timer
            algoDurationLabel.setText("Calcul en cours...");
            final long startTime = System.nanoTime(); // Démarrer le timer

            // Lancer l'algorithme
            cheminComplet = currentGraph.maximizeDijkstraSimplifieGlobal(currentGraph.getInitialSommet(), p); // Lancer l'algorithme

            // Fin du timer
            long finalElapsedTime = System.nanoTime() - startTime; // Calculer la durée
            algoDurationLabel.setText("Durée de l'algorithme : " + formatElapsedTime(finalElapsedTime)); // afficher la durée dans le lbl

            // Ajouter a la chart
            pToTimeMapDijkstraGlobal.computeIfAbsent(p, k -> new ArrayList<>()).add((double) finalElapsedTime);
            nToTimeMapDijkstraGlobal.computeIfAbsent(currentGraph.getSommets().size(), k -> new ArrayList<>()).add((double) finalElapsedTime);
        
            originalTimeMapDijkstraGlobalN.computeIfAbsent(currentGraph.getSommets().size(), k -> new ArrayList<>()).add((double) finalElapsedTime);
            originalTimeMapDijkstraGlobalP.computeIfAbsent(p, k -> new ArrayList<>()).add((double) finalElapsedTime);

            // Mettre a jour la chart
            updateChartData();

            // Mettre à jour l'affichage des chemins
            cheminResume.getItems().add("μ = " + cheminComplet.toString());

            // Mettre en surbrillance le chemin avec la valeur maximale
            highlightPath(cheminComplet);

            // Mettre en surbrillance bleu le chemin selectionné (maximum par defaut)
            setupCheminListView(cheminResume, cheminComplet);

            // Ajouter le chemin unique aux cheminsComplets pour l'event de tracage
            cheminsComplets = new ArrayList<>();
            cheminsComplets.add(cheminComplet);
        });

        // -- ACTIONS BOUTON "Lancer Dijkstra Simplifié local Probabiliste"
        launchProbabiliste.setOnAction(event -> {
            cheminResume.getItems().clear(); // Effacer les anciens résultats
            resetGraphStyle(currentGraph.getArcs(), currentGraph); // Réinitialiser les styles des arcs et sommets

            // Début du timer
            algoDurationLabel.setText("Calcul en cours...");
            final long startTime = System.nanoTime(); // Démarrer le timer

            // Lancer l'algorithme
            cheminsComplets = currentGraph.maximizeLocalProba(currentGraph.getInitialSommet(), p); // Lancer l'algorithme

            // Fin du timer
            long finalElapsedTime = System.nanoTime() - startTime; // Calculer la durée
            algoDurationLabel.setText("Durée de l'algorithme : " + formatElapsedTime(finalElapsedTime)); // afficher la durée dans le lbl

            // Ajouter a la chart
            // Ajouter la valeur de `p` et le temps à la Map, sans modifier les anciens points
            pToTimeMapProbabilist.computeIfAbsent(p, k -> new ArrayList<>()).add((double) finalElapsedTime);
            nToTimeMapProbabilist.computeIfAbsent(currentGraph.getSommets().size(), k -> new ArrayList<>()).add((double) finalElapsedTime);

            originalTimeMapProbabilistN.computeIfAbsent(currentGraph.getSommets().size(), k -> new ArrayList<>()).add((double) finalElapsedTime);
            originalTimeMapProbabilistP.computeIfAbsent(p, k -> new ArrayList<>()).add((double) finalElapsedTime);
            // Mettre a jour la chart
            updateChartData();

            // Trier les chemins par rapport à la valeur divisé par le taux de risque
            cheminsComplets.sort(Comparator.comparingDouble((PathMu chemin) -> chemin.getValeur() / chemin.getAccProbSucces()).reversed());

            // Ajouter les chemins mu au résumé avec un formatage clair
            for (PathMu chemin : cheminsComplets) {
                // Ajouter le chemin formaté au ListView
                String cheminString = chemin.toStringProba();
                cheminResume.getItems().add(cheminString);
            }

            // Mettre en surbrillance le chemin avec la valeur maximale
            PathMu maxChemin = cheminsComplets.stream()
                .max((chemin1, chemin2) -> Double.compare(chemin1.getValeur(), chemin2.getValeur()))
                .orElse(null);

            if (maxChemin != null) {
                for (int i = 0; i < maxChemin.getChemin().size() - 1; i++) {
                    Sommet start = maxChemin.getChemin().get(i);
                    Sommet end = maxChemin.getChemin().get(i + 1);
                    Arc arcToHighlight = currentGraph.trouverArc(start, end);
                    if (arcToHighlight != null) {
                        arcToHighlight.highlight();
                    }
                    start.highlight();
                    end.highlight();
                }
            }

            // Mettre à jour l'affichage des chemins avec couleur par niveau de risque
            cheminResume.setCellFactory(lv -> new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle(""); // Reset style
                    } else {
                        setText(item);
                        if (maxChemin != null && item.contains(maxChemin.toString())) {
                            setStyle("-fx-background-color: lightgreen;"); // Colorer en vert le chemin max
                        } else {
                            // Utiliser des couleurs basées sur le taux de risque
                            if (item.contains("Elevé")) {
                                setStyle("-fx-background-color: lightblue;");
                            } else if (item.contains("Modéré")) {
                                setStyle("-fx-background-color: lightyellow;");
                            } else {
                                setStyle("-fx-background-color: lightcoral;");
                            }
                        }
                    }
                }
            });
        });

        // ANIMATION DE TRAÇAGE DES MU PATHS
        cheminResume.setOnMouseClicked(event -> handlePathSelection(event, currentGraph.getArcs(), cheminsComplets, currentGraph));

        // --- ACTIONS DES BOUTONS POUR CHANGER DE GRAPHE ---
        // Charger le graphe du projet
        loadProjectGraph.setOnAction(event -> {
            launchProbabiliste.setVisible(false);
            addVertexButton.setVisible(false);
            removeVertexButton.setVisible(false);
            currentGraph = projectGraph;
            vertexCountLabel.setText("Nb sommets : " + currentGraph.getSommets().size()); // Mise à jour du label
            drawGraph();
        });

        // Charger le grand graphe
        loadLargeGraph.setOnAction(event -> {
            launchProbabiliste.setVisible(false);
            addVertexButton.setVisible(true);
            removeVertexButton.setVisible(true);
            currentGraph = largeGraph;
            vertexCountLabel.setText("Nb sommets : " + currentGraph.getSommets().size()); // Mise à jour du label
            drawGraph();
        });

        // Charger le graphe probabiliste
        loadProbabilistGraph.setOnAction(event -> {
            launchProbabiliste.setVisible(true);
            addVertexButton.setVisible(false);
            removeVertexButton.setVisible(false);
            currentGraph = probabilistGraph;
            vertexCountLabel.setText("Nb sommets : " + currentGraph.getSommets().size()); // Mise à jour du label
            drawGraph();
        });

        viewGraphButton.setOnAction(event -> {
            graphPane.setVisible(true);
            chartPane.setVisible(false);
            chartButtons.setVisible(false);
            drawGraph();
        });
        
        viewChartButton.setOnAction(event -> {
            graphPane.setVisible(false);
            chartPane.setVisible(true);
            chartButtons.setVisible(true);
            drawChart();  // Fonction pour dessiner le chart
        });        

        // Appel de la mise à jour de l'axe X lorsque le slider p change ou que le nombre de sommets change
        pSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            p = newVal.intValue();  // Mettre à jour la valeur de p
            pLabel.setText("Nombre d'échanges (p): " + p);  // Mettre à jour le label
            updateXAxis();  // Mettre à jour l'axe X pour refléter la nouvelle valeur de p
            updateChartData();
        });

        // Appel de la mise à jour de l'axe X lors de l'ajout ou la suppression de sommets
        addVertexButton.setOnAction(event -> {
            ((GraphLarge) currentGraph).ajouterSommet();  // Ajouter un sommet
            vertexCountLabel.setText("Nb sommets : " + currentGraph.getSommets().size());  // Mettre à jour le label
            updateXAxis();  // Mettre à jour l'axe X avec le nouveau nombre de sommets
            updateChartData();
            drawGraph();  // Redessiner le graphe après modification
        });

        removeVertexButton.setOnAction(event -> {
            ((GraphLarge) currentGraph).supprimerDernierSommet();  // Supprimer le dernier sommet
            vertexCountLabel.setText("Nb sommets : " + currentGraph.getSommets().size());  // Mettre à jour le label
            updateXAxis();  // Mettre à jour l'axe X avec le nouveau nombre de sommets
            drawGraph();  // Redessiner le graphe après modification
        });

        // Mise à jour de l'axe X et des données quand l'utilisateur bascule entre p et n
        pOption.setOnAction(event -> {
            updateXAxis();  // Appeler la mise à jour de l'axe X lorsque p est sélectionné
            updateChartData();  // Mettre à jour les données du chart si besoin
        });

        nOption.setOnAction(event -> {
            updateXAxis();  // Appeler la mise à jour de l'axe X lorsque n est sélectionné
            updateChartData();  // Mettre à jour les données du chart si besoin
        });
        
        // Event pour changer l'unité de temps
        timeUnitComboBox.setOnAction(event -> {
            updateChartAxes();  // Convertir les temps et redessiner le chart
            updateChartData();
        });
        
        clearChartButton.setOnAction(event -> {
            // Supprimer les séries du chart
            lineChart.getData().clear();
        
            // Créer de nouvelles instances des séries pour éviter les erreurs
            bruteForceSeries = new XYChart.Series<>();
            bruteForceSeries.setName("Brute Force");
        
            dijkstraLocalSeries = new XYChart.Series<>();
            dijkstraLocalSeries.setName("Dijkstra Simplifié Local");
        
            dijkstraGlobalSeries = new XYChart.Series<>();
            dijkstraGlobalSeries.setName("Dijkstra Simplifié Global");
        
            probabilistSeries = new XYChart.Series<>();
            probabilistSeries.setName("Dijkstra Probabiliste");
        
            // Réajouter les séries vides au chart
            lineChart.getData().add(bruteForceSeries);
            lineChart.getData().add(dijkstraLocalSeries);
            lineChart.getData().add(dijkstraGlobalSeries);
            lineChart.getData().add(probabilistSeries);
        
            // Vider les maps pour chaque algorithme
            pToTimeMapBruteForce.clear();
            pToTimeMapDijkstraLocal.clear();
            pToTimeMapDijkstraGlobal.clear();
            pToTimeMapProbabilist.clear();
            
            nToTimeMapBruteForce.clear();
            nToTimeMapDijkstraLocal.clear();
            nToTimeMapDijkstraGlobal.clear();
            nToTimeMapProbabilist.clear();
            
            originalTimeMapBruteForceP.clear();
            originalTimeMapDijkstraLocalP.clear();
            originalTimeMapDijkstraGlobalP.clear();
            originalTimeMapProbabilistP.clear();
        
            originalTimeMapBruteForceN.clear();
            originalTimeMapDijkstraLocalN.clear();
            originalTimeMapDijkstraGlobalN.clear();
            originalTimeMapProbabilistN.clear();
        
            updateChartData();
        });
        
        
        // --- SCENE ---
        Scene scene = new Scene(rootWithOverlay, 1000, 800); // Taille initiale de la fenêtre
        primaryStage.setScene(scene);
        primaryStage.setTitle("Graphes et Algorithmes");
        primaryStage.setMaximized(true);  // Activer le mode plein écran, trop d'element verticaux
        primaryStage.show();        
    }

    // Animer le chemin emprunter par le mu path selectionné
    private void handlePathSelection(MouseEvent event, List<Arc> arcs, List<PathMu> cheminsComplets, Graph graph) {
        @SuppressWarnings("unchecked")
        ListView<String> cheminResume = (ListView<String>) event.getSource();
        int selectedIndex = cheminResume.getSelectionModel().getSelectedIndex();
    
        if (selectedIndex >= 0 && selectedIndex < cheminsComplets.size()) {
            PathMu selectedPath = cheminsComplets.get(selectedIndex);
            List<Sommet> chemin = selectedPath.getChemin();
    
            // Réinitialiser les styles des arcs et sommets à leurs couleurs par défaut
            resetGraphStyle(graph.getArcs(), graph);
    
            // Créer un SequentialTransition pour une animation séquentielle
            SequentialTransition sequentialTransition = new SequentialTransition();
    
            for (int i = 0; i < chemin.size() - 1; i++) {
                Sommet start = chemin.get(i);
                Sommet end = chemin.get(i + 1);
                Arc arcToAnimate = graph.trouverArc(start, end);
                
    
                if (arcToAnimate != null) {
                    // Incrémenter le compteur de passages pour cet arc
                    arcPassages.put(arcToAnimate, arcPassages.getOrDefault(arcToAnimate, 0) + 1);
                    int passageCount = arcPassages.get(arcToAnimate);
    
                    // Calculer la nouvelle nuance de vert basée sur le nombre de passages
                    Color newColor = getColorForPassage(passageCount);
    
                    // Réinitialiser l'arc 
                    Line originalLine = arcToAnimate.getLine();  // Ligne existante représentant l'arc
                    originalLine.setStrokeWidth(3);  //  ajuster l'épaisseur
    
                    // Créer une nouvelle ligne pour l'animation avec départ aux coordonnées du sommet source
                    Line animatedLine = new Line(originalLine.getStartX(), originalLine.getStartY(),
                                                 originalLine.getStartX(), originalLine.getStartY());
    
                    animatedLine.setStroke(newColor);  // Appliquer la couleur en fonction du nombre de passages
                    animatedLine.setStrokeWidth(3);  // Épaisseur de la ligne animée
    
                    // Ajouter la nouvelle ligne au graphe pour l'animer
                    graphPane.getChildren().add(animatedLine);
                    animatedLines.add(animatedLine);  // Stocker cette ligne pour suppression ultérieure
    
                    // Créer une Transition pour animer la ligne
                    Timeline animateArc = new Timeline(
                        new KeyFrame(Duration.seconds(1), 
                            new KeyValue(animatedLine.endXProperty(), originalLine.getEndX()),
                            new KeyValue(animatedLine.endYProperty(), originalLine.getEndY())
                        )
                    );
    
                    // Une fois que l'animation de l'arc est terminée, garder la ligne avec la nouvelle couleur
                    animateArc.setOnFinished(e -> {
                        end.highlight();  // Marquer le sommet destination
                        animatedLine.setStroke(newColor);  // Garder l'arc avec la nouvelle couleur
                    });
    
                    // Ajouter une KeyFrame pour marquer le sommet de départ avant de commencer à animer l'arc
                    Timeline highlightStart = new Timeline(
                        new KeyFrame(Duration.seconds(0), e -> start.highlight())
                    );
                    
    
                    // Ajouter ces animations dans le `SequentialTransition` pour qu'elles s'exécutent successivement
                    sequentialTransition.getChildren().addAll(highlightStart, animateArc);
                }
            }
            // Démarrer l'animation séquentielle
            sequentialTransition.play();
        }
    }    
    
    // Méthode pour réinitialiser les styles des arcs et sommets
    private void resetGraphStyle(List<Arc> arcs, Graph graph) {
        for (Line line : animatedLines) {
            graphPane.getChildren().remove(line);
        }
        animatedLines.clear();  // Vider la liste des lignes animées après suppression
    
        // Réinitialiser les styles des arcs
        for (Arc arc : arcs) {
            arc.resetStyle();  // Remet la couleur d'origine pour chaque arc
        }
        // Réinitialiser les styles des sommets
        for (Sommet sommet : graph.getSommets()) {
            sommet.resetStyle();  // Remet la couleur d'origine pour chaque sommet
        }
    }

    // Fonction pour déterminer une nuance de vert en fonction du nombre de passages
    private Color getColorForPassage(int passageCount) {
        // Nuances progressives : plus il y a de passages, plus le vert devient foncé
        double intensity = Math.max(0, 1 - (passageCount * 0.2));  // Diminuer l'intensité de vert à chaque passage
        return new Color(0, intensity, 0, 1); 
    }

    private void drawGraph() {
        // effacer le graphe actuel
        graphPane.getChildren().clear();
        // effacer les mu paths de la listview
        if (cheminResume != null) cheminResume.getItems().clear();

        // dessiner les arcs
        for (Arc arc : currentGraph.getArcs()) {
            Line line = arc.createLine(true);
            graphPane.getChildren().add(line);
            Text label = arc.createLabel(line);
            graphPane.getChildren().add(label);
            Polygon arrowHead = arc.createArrow(line);
            graphPane.getChildren().add(arrowHead);
        }

        // dessiner les sommets
        for (Sommet sommet : currentGraph.getSommets()) {
            Circle circle = sommet.createCircle();
            graphPane.getChildren().add(circle);
            Text nameLabel = sommet.createNameLabel(sommet.getId() <= 2);
            graphPane.getChildren().add(nameLabel);
        }
    }

    // Fonction pour formater le temps écoulé à partir des nanosecondes
    private String formatElapsedTime(long elapsedTimeInNanos) {
        long hours = elapsedTimeInNanos / 3_600_000_000_000L;  // 1h = 3,600,000,000,000 ns
        long minutes = (elapsedTimeInNanos % 3_600_000_000_000L) / 60_000_000_000L;  // 1min = 60,000,000,000 ns
        long seconds = (elapsedTimeInNanos % 60_000_000_000L) / 1_000_000_000L;  // 1sec = 1,000,000,000 ns
        long milliseconds = (elapsedTimeInNanos % 1_000_000_000L) / 1_000_000L;  // 1ms = 1,000,000 ns
        long nanoseconds = elapsedTimeInNanos % 1_000_000L;  // Les nanosecondes restantes

        if (hours > 0) {
            return String.format("%d h %d min %d sec", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%d min %d sec", minutes, seconds);
        } else if (seconds > 0) {
            return String.format("%d sec %03d ms", seconds, milliseconds); // Afficher en sec et ms si >= 1s
        } else if (milliseconds > 0) {
            return String.format("%d ms %03d ns", milliseconds, nanoseconds); // Afficher en ms et ns si < 1s
        } else {
            return String.format("%d ns", nanoseconds); // Très rapide, afficher uniquement les nanosecondes
        }
    }

    // Fonction pour mettre en surbrillance les sommets et les arcs d'un chemin
    private void highlightPath(PathMu maxChemin) {
        if (maxChemin != null) {
            for (int i = 0; i < maxChemin.getChemin().size() - 1; i++) {
                Sommet start = maxChemin.getChemin().get(i);
                Sommet end = maxChemin.getChemin().get(i + 1);
                Arc arcToHighlight = currentGraph.trouverArc(start, end);
                if (arcToHighlight != null) {
                    arcToHighlight.highlight();
                }
                start.highlight();
                end.highlight();
            }
        }
    }

    // Fonction pour mettre highlight un chemin mu selectionné
    private void setupCheminListView(ListView<String> cheminResume, PathMu maxChemin) {
        cheminResume.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle(""); // Reset style
                } else {
                    setText(item);
                    // Si c'est le chemin maximal
                    if (maxChemin != null && item.contains(maxChemin.toString())) {
                        setStyle("-fx-background-color: lightblue;"); // Colorer en bleu le chemin max
                    } else {
                        setStyle(""); // Reset style pour les autres
                    }
                }
            }
        });
    }

    @SuppressWarnings("unchecked") // todo : clean
    private void initializeChart() {
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();

        // Utiliser une condition dynamique pour l'axe X basé sur l'option sélectionnée
        if (pOption.isSelected()) {
            xAxis.setLabel("p (nombre d'échanges)");
        } else if (nOption.isSelected()) {
            xAxis.setLabel("n (nombre de sommets)");
        }
    
        // Définir l'axe Y en fonction de l'unité de temps sélectionnée
        switch (timeUnitComboBox.getValue()) {
            case "ns":
                yAxis.setLabel("Temps d'exécution (ns)");
                break;
            case "ms":
                yAxis.setLabel("Temps d'exécution (ms)");
                break;
            case "s":
                yAxis.setLabel("Temps d'exécution (s)");
                break;
            case "mn":
                yAxis.setLabel("Temps d'exécution (mn)");
                break;
            case "h":
                yAxis.setLabel("Temps d'exécution (h)");
                break;
        }
    
        lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Performance des Algorithmes");
    
        // Initialiser les séries pour chaque algorithme
        bruteForceSeries = new XYChart.Series<>();
        bruteForceSeries.setName("Brute Force");
    
        dijkstraLocalSeries = new XYChart.Series<>();
        dijkstraLocalSeries.setName("Dijkstra Simplifié Local");
    
        dijkstraGlobalSeries = new XYChart.Series<>();
        dijkstraGlobalSeries.setName("Dijkstra Simplifié Global");
    
        probabilistSeries = new XYChart.Series<>();
        probabilistSeries.setName("Dijkstra Probabiliste");

        // adapter a p par defaut
        updateXAxis();
    
        // Ajouter les séries au chart
        lineChart.getData().addAll(bruteForceSeries, dijkstraLocalSeries, dijkstraGlobalSeries, probabilistSeries);
    
        chartPane.getChildren().add(lineChart);  // Ajouter le chart au Pane
    }

    // Mise à jour de l'axe X en fonction de p ou n
    private void updateXAxis() {
        NumberAxis xAxis = (NumberAxis) lineChart.getXAxis();  // Récupérer l'axe X du chart
        
        // Si pOption est sélectionné, on met à jour l'axe X pour afficher p (valeur du slider)
        if (pOption.isSelected()) {
            xAxis.setLabel("p (nombre d'échanges)");
            for (XYChart.Series<Number, Number> series : lineChart.getData()) {
                for (XYChart.Data<Number, Number> dataPoint : series.getData()) {
                    dataPoint.setXValue(p);  // Met à jour les valeurs X avec la valeur actuelle du slider p
                }
            }
        } 
        // Si nOption est sélectionné, on met à jour l'axe X pour afficher n (nombre de sommets)
        else if (nOption.isSelected()) {
            int numberOfVertices = currentGraph.getSommets().size();  // Récupérer le nombre de sommets du graphe
            xAxis.setLabel("n (nombre de sommets)");
            for (XYChart.Series<Number, Number> series : lineChart.getData()) {
                for (XYChart.Data<Number, Number> dataPoint : series.getData()) {
                    dataPoint.setXValue(numberOfVertices);  // Met à jour les valeurs X avec le nombre de sommets
                }
            }
        }
    }
    
    // Fonction pour convertir les temps en unités de temps sélectionnées
    private void updateChartAxes() {
        String selectedTimeUnit = timeUnitComboBox.getValue();
    
        if (pOption.isSelected()) {
            convertTimeUnits(originalTimeMapBruteForceP, pToTimeMapBruteForce, selectedTimeUnit);
            convertTimeUnits(originalTimeMapDijkstraLocalP, pToTimeMapDijkstraLocal, selectedTimeUnit);
            convertTimeUnits(originalTimeMapDijkstraGlobalP, pToTimeMapDijkstraGlobal, selectedTimeUnit);
            convertTimeUnits(originalTimeMapProbabilistP, pToTimeMapProbabilist, selectedTimeUnit);
        } else if (nOption.isSelected()) {
            convertTimeUnits(originalTimeMapBruteForceN, nToTimeMapBruteForce, selectedTimeUnit);
            convertTimeUnits(originalTimeMapDijkstraLocalN, nToTimeMapDijkstraLocal, selectedTimeUnit);
            convertTimeUnits(originalTimeMapDijkstraGlobalN, nToTimeMapDijkstraGlobal, selectedTimeUnit);
            convertTimeUnits(originalTimeMapProbabilistN, nToTimeMapProbabilist, selectedTimeUnit);
        }
    
        updateChartData();  // Redessiner les données du chart
    }    
    
    private void updateChartData() {
        if (pOption.isSelected()) {
            // Mettre à jour chaque algorithme avec sa propre Map (p)
            updateSeriesWithAverage(bruteForceSeries, pToTimeMapBruteForce);
            updateSeriesWithAverage(dijkstraLocalSeries, pToTimeMapDijkstraLocal);
            updateSeriesWithAverage(dijkstraGlobalSeries, pToTimeMapDijkstraGlobal);
            updateSeriesWithAverage(probabilistSeries, pToTimeMapProbabilist);
        } else if (nOption.isSelected()) {
            // nOption est sélectionné, mettre à jour chaque algorithme avec sa propre Map
            updateSeriesWithAverage(bruteForceSeries, nToTimeMapBruteForce);
            updateSeriesWithAverage(dijkstraLocalSeries, nToTimeMapDijkstraLocal);
            updateSeriesWithAverage(dijkstraGlobalSeries, nToTimeMapDijkstraGlobal);
            updateSeriesWithAverage(probabilistSeries, nToTimeMapProbabilist);
        }
    }   
    
    
    // Fonction pour calculer la moyenne et mettre à jour la série
    private void updateSeriesWithAverage(XYChart.Series<Number, Number> series, Map<Integer, List<Double>> valueToTimeMap) {
        // Vider la série actuelle
        series.getData().clear();

        // Parcourir la Map et calculer la moyenne pour chaque `p` ou `n`
        for (Map.Entry<Integer, List<Double>> entry : valueToTimeMap.entrySet()) {
            int keyValue = entry.getKey();
            List<Double> times = entry.getValue();

            // Calculer la moyenne des temps
            double averageTime = times.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

            // Ajouter la moyenne à la série
            series.getData().add(new XYChart.Data<>(keyValue, averageTime));
        }
    }

    private void convertTimeUnits(Map<Integer, List<Double>> originalTimeMap, Map<Integer, List<Double>> valueToTimeMap, String newTimeUnit) {
        for (Map.Entry<Integer, List<Double>> entry : originalTimeMap.entrySet()) {
            List<Double> convertedTimes = new ArrayList<>();
            for (Double timeValue : entry.getValue()) {
                convertedTimes.add(convertTimeUnit(timeValue, newTimeUnit));
            }
            valueToTimeMap.put(entry.getKey(), convertedTimes);
        }
    }
    
    private double convertTimeUnit(double timeInNanos, String newTimeUnit) {
        switch (newTimeUnit) {
            case "ns":
                return timeInNanos;  // Aucune conversion nécessaire
            case "ms":
                return timeInNanos / 1_000_000;  // Conversion des nanosecondes en millisecondes
            case "s":
                return timeInNanos / 1_000_000_000;  // Conversion des nanosecondes en secondes
            case "mn":
                return timeInNanos / (60 * 1_000_000_000);  // Conversion des nanosecondes en minutes
            case "h":
                return timeInNanos / (60 * 60 * 1_000_000_000);  // Conversion des nanosecondes en heures
            default:
                return timeInNanos;  // Valeur par défaut en nanosecondes
        }
    }
    
    
    private void drawChart() {
        // Logique pour afficher le chart
        // Les données sont mises à jour chaque fois qu'un algo est lancé
        chartPane.setVisible(true);
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
