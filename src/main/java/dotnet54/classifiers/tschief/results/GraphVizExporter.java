package dotnet54.classifiers.tschief.results;

import dotnet54.classifiers.tschief.TSCheifTree;
import dotnet54.classifiers.tschief.TSChiefNode;
import dotnet54.classifiers.tschief.splitters.boss.BossSplitterV1;
import dotnet54.classifiers.tschief.splitters.ee.ElasticDistanceSplitter;
import dotnet54.classifiers.tschief.splitters.ee.MultivariateElasticDistanceSplitter;
import dotnet54.classifiers.tschief.splitters.rise.RISESplitterV1;
import dotnet54.classifiers.tschief.splitters.rise.RISESplitterV2;
import dotnet54.util.Util;
import guru.nidi.graphviz.attribute.*;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import org.apache.commons.text.WordUtils;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import static guru.nidi.graphviz.model.Factory.*;

public class GraphVizExporter {

    private static DecimalFormat df = new DecimalFormat("#.####");

    public GraphVizExporter(){

    }

    public static void exportTree(TSCheifTree tree) throws IOException {
//        Graph g1 = graph("example1").directed()
//                .graphAttr().with(Rank.dir(Rank.RankDir.LEFT_TO_RIGHT))
////                .nodeAttr().with(Font.name("arial"))
//                .linkAttr().with("class", "link-class")
//                .with(
//                        node("a").with(Color.RED).link(node("b")),
//                        node("b").link(
//                                to(node("c")).with(Attributes.attr("weight", 5), Style.DASHED)
//                        )
//                );
//        Graphviz.fromGraph(g1).height(100).render(Format.PNG).toFile(new File("out/local/cif/ex1.png"));

        String outputPath =  tree.tsChiefOptions.currentOutputPath + "/" +
                tree.tsChiefOptions.datasetName + "/trees/";

        File fileObj = new File(outputPath);
        fileObj.mkdirs();

        MutableGraph g = mutGraph("tree-" + tree.getTreeID()).setDirected(true)
//                .graphAttrs().add(Rank.dir(Rank.RankDir.LEFT_TO_RIGHT))
//                .nodeAttrs().add(Font.name("arial"))
                .linkAttrs().add("class", "link-class");

        TSChiefNode tsChiefRoot = tree.getRootNode();
        MutableNode rootNode = createNode(tsChiefRoot);
        buildTree(g, tsChiefRoot, rootNode);
        g.add(rootNode);

        // imperative api

        // add legend
        g.use((gr, ctx) -> {
            mutNode("EE").add(Shape.RECTANGLE, Style.FILLED, Color.hsv(.9, .3, 1.0));
            mutNode("BOSS").add(Shape.RECTANGLE, Style.FILLED, Color.hsv(.3, .3, 1.0));
            mutNode("RISE").add(Shape.RECTANGLE, Style.FILLED, Color.hsv(.6, .3, 1.0));
        });

        Graphviz.fromGraph(g).height(800).render(Format.PNG)
        .toFile(new File(outputPath + tree.getTreeID()+ ".png"));

//        String dotFile = Graphviz.fromGraph(g).render(Format.DOT).toString();;
//        try (PrintStream out = new PrintStream(new FileOutputStream(outputPath + tree.getTreeID()+ ".dot"))) {
//            out.print(dotFile);
//        }

    }

    private static void buildTree(MutableGraph g, TSChiefNode tsChiefNode, MutableNode parentNode){
        if (tsChiefNode.isLeaf()) {
            return;
        }
        MutableNode childNode = null;
        for (int key : tsChiefNode.getChildren().keys()) {
            TSChiefNode child = tsChiefNode.getChildren().get(key);
            childNode = createNode(child);
            parentNode.addLink(childNode);
            g.add(childNode);
            buildTree(g, child, childNode);
        }
    }

    private static MutableNode createNode(TSChiefNode tsChiefNode){
        MutableNode graphNode;
        String nodeName = tsChiefNode.getNodeID() + "";
        StringBuilder nodeLabel = new StringBuilder();

        graphNode = mutNode(nodeName);

        final int MAX_CHARS = 100;
        final int COL_WITH = 20;

        String trainDist = tsChiefNode.trainDistribution.toString();
        if (trainDist.length() > MAX_CHARS){
            trainDist = trainDist.substring(0, MAX_CHARS) + "...";
        }
        trainDist = WordUtils.wrap(trainDist, COL_WITH, "<br />", true);

        String testDist = tsChiefNode.testDistribution.toString();
        if (testDist.length() > MAX_CHARS){
            testDist = testDist.substring(0, MAX_CHARS) + "...";
        }
        testDist = WordUtils.wrap(testDist, COL_WITH, "<br />", true);

        nodeLabel.append("<b>")
                .append("NodeID: ")
                .append(tsChiefNode.getNodeID())
                .append("</b><br/>")
                .append("Depth: ")
                .append(tsChiefNode.getNodeDepth())
                .append("<br/>trainDist: ")
                .append(trainDist)
                .append("<br/>")
                .append("testDist: ")
                .append(testDist)
                .append("<br/>");

        if (tsChiefNode.isLeaf()) {
            graphNode.add(Shape.ELLIPSE);
            nodeLabel.append("Label: ")
                    .append(tsChiefNode.label())
                    .append("<br/>");
        }else{
            graphNode.add(Shape.RECTANGLE);
            nodeLabel.append("Gini: ")
                    .append(df.format(Util.gini(tsChiefNode.trainDistribution)))
                    .append("<br/>");
        }


        // TODO if label string is too long truncare (eg. if too many classes)

        if (tsChiefNode.bestSplitter instanceof ElasticDistanceSplitter){
            graphNode.add(Style.FILLED, Color.hsv(.9, .3, 1.0));
            ElasticDistanceSplitter ee = (ElasticDistanceSplitter) tsChiefNode.bestSplitter;
            nodeLabel.append("Splitter: ")
                    .append(WordUtils.wrap(tsChiefNode.bestSplitter.toString(), COL_WITH, "<br />", true))
                    .append("<br/>");
            nodeLabel.append("DM: ")
                    .append(WordUtils.wrap(ee.getSimilarityMeasure().measureName.toString(), COL_WITH, "<br />", true))
                    .append("<br/>");

        }else if (tsChiefNode.bestSplitter instanceof MultivariateElasticDistanceSplitter){
            graphNode.add(Style.FILLED, Color.hsv(.9, .3, 1.0));
            MultivariateElasticDistanceSplitter mee = (MultivariateElasticDistanceSplitter) tsChiefNode.bestSplitter;
            nodeLabel.append("Splitter: ")
                    .append(WordUtils.wrap(tsChiefNode.bestSplitter.toString(), COL_WITH, "<br />", true))
                    .append("<br/>");
            nodeLabel.append("DM: ")
                    .append(mee.getMeasure().toString())
                    .append("<br/>");

        }else if (tsChiefNode.bestSplitter instanceof BossSplitterV1){
            graphNode.add(Style.FILLED, Color.hsv(.3, .3, 1.0));
            nodeLabel.append("Splitter: ")
                    .append(WordUtils.wrap(tsChiefNode.bestSplitter.toString(), COL_WITH, "<br />", true))
                    .append("<br/>");
        }else if (tsChiefNode.bestSplitter instanceof RISESplitterV1 ||
                tsChiefNode.bestSplitter instanceof RISESplitterV2){
            graphNode.add(Style.FILLED, Color.hsv(.6, .3, 1.0));
            nodeLabel.append("Splitter: ")
                    .append(WordUtils.wrap(tsChiefNode.bestSplitter.toString(), COL_WITH,"<br />", true))
                    .append("<br/>");
        }

        graphNode.add(Attributes.attr("label", Label.html(nodeLabel.toString())));

        return graphNode;
    }

}
