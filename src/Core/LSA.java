package Core;

import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.attribute.Shape;
import guru.nidi.graphviz.engine.*;
import guru.nidi.graphviz.model.MutableGraph;

import static guru.nidi.graphviz.model.Factory.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class LSA {

    static class Dist implements Comparable<Dist> {
        String name;
        int distance;
        String prev;

        public Dist(String name, int distance, String prev) {
            this.name = name;
            this.distance = distance;
            this.prev = prev;
        }

        @Override
        public int compareTo(Dist o) {
            return Integer.compare(this.distance, o.distance);
        }
    }


    public HashMap<String,Node> Nodes = new HashMap<>();
    public String source;

    public HashMap<String, Integer> Distances = new HashMap<>() {
        @Override
        public String toString() {
            return super.toString()
                    .replaceAll("\\s*[^\\s]*=2147483647,?", "")
                    .replaceAll("^\\{", "")
                    .replaceAll(",?}$", "");
        }
    };
    public PriorityQueue<Dist> Q = new PriorityQueue<>();
    public HashSet<String> visited = new HashSet<>() {
        @Override
        public String toString() {
            return super.toString()
                    .replaceAll("^\\[", "")
                    .replaceAll("]$", "");
        }
    };
    public HashMap<String, String> Predecessor = new HashMap<>() {
        @Override
        public String toString() {
            return super.toString()
                    .replaceAll("\\s*[^\\s]*=null,?", "")
                    .replaceAll("^\\{", "")
                    .replaceAll(",?}$", "")
                    .replaceAll("=", "←");
        }
    };

    public ArrayList<String> text = new ArrayList<>();

    public static String[] sample = {
        "A: B:5 C:3 D:5 ",
        "B: A:5 C:4 E:3 F:2",
        "C: A:3 B:4 D:1 E:6",
        "D: A:5 C:1 E:3 ",
        "E: B:3 C:6 D:3 F:5 ",
        "F: B:2 E:5"
    };

    // reset source but not nodes
    public void Reset() {
        Distances.clear();
        Predecessor.clear();
        Q.clear();
        visited.clear();
        source = null;
    }

    // reset all
    public void safeReset() {
        Reset();
        text.clear();
        Nodes.clear();
    }

    public void loadFromFile(File file) throws Exception {
        safeReset();
        Scanner sc = new Scanner(file);
        while (sc.hasNextLine()) {
            text.add(sc.nextLine());
        }
        sc.close();
    }

    public void loadFromStr(String text) {
        safeReset();
        this.text = new ArrayList<>(Arrays.asList(text.split("\n")));
    }

    /**
     * Parse `this.text` and store to `this.Nodes`.
     *
     * @exception RuntimeException if invalid format detected
     */
    public void parse() throws IllegalArgumentException {
        // clearing cache
        Nodes.clear();

        for (String s : text){
            parseLine(s);
        }
    }

    public void parseLine(String s) throws IllegalArgumentException {
        if (!s.matches("([^:]*:)?(\\ [^:]*:[\\d]*)*\\s*"))
            throw new IllegalArgumentException("Invalid Format: Should be in format 'X: Y:1 Z:2'.");

        // Split with the first ':', ignoring following colons
        String[] parts = s.split(":", 2);

        // does not contain ':', err
        if (parts.length < 2)
            throw new IllegalArgumentException("Invalid Format: Missing ':', '<node>:<len>'.");

        String src = parts[0];
        String[] neighbors = parts[1].split(" ");

        for (String n : neighbors) {
            if (n.isEmpty()) continue;
            // Check if the same edges from different direction are consistent
            // nParts[0] is neighbor name; nParts[1] is distance
            String[] nParts = n.split(":");
            if (nParts.length != 2)
                throw new IllegalArgumentException("Invalid Format: Missing ':', '<node>:<len>'.");
            if (Nodes.containsKey(n)) {
                String EdgeFromN = String.valueOf(Nodes.get(n).getEdge(src));
                // edge n-src is not empty && not the consistent with src-n
                if (!EdgeFromN.isEmpty() && !nParts[1].equals(EdgeFromN))
                    throw new IllegalArgumentException("Invalid Format: Inconsistent edges.");
            }
            String neighbor = nParts[0];
            int distance;
            try {
                distance = Integer.parseInt(nParts[1]);
            } catch (Exception ex) {
                throw new IllegalArgumentException("Invalid Format: Invalid length, '<node>:<len>'.");
            }
            AddNode(src);
            AddNode(neighbor);
            AddEdge(src, neighbor, distance);
        }
    }

    public int setSource(String source) {
        Reset();
        this.source = source;
        for (String s : Nodes.keySet()) {
            Distances.put(s, Integer.MAX_VALUE);
            Predecessor.put(s, null);
        }
        //Set the distance of the source to 0
        Distances.put(source, 0);
        visited.add(source);

        //Add neighbors of the source to the queue
        Node src = Nodes.get(source);
        if (src == null)
            return 1;
        for(String s : src.Neighbors.keySet()){
            Q.add(new Dist(s, Nodes.get(source).Neighbors.get(s), source));
            Predecessor.put(s, source);
        }
        return 0;
    }

    public String SingleStep() {
        if(source == null)
            throw new RuntimeException("Source not set");
        if (Q.isEmpty())
            return "";
        // Single step of Dijkstra's algorithm
        Dist d = Q.poll();
        assert d != null;
        if (visited.contains(d.name)) return SingleStep();
        visited.add(d.name);
        Distances.put(d.name, d.distance);
        Predecessor.put(d.name, d.prev);
        for(String s : Nodes.get(d.name).Neighbors.keySet()){
            if(!visited.contains(s)){
                int newDist = Distances.get(d.name) + Nodes.get(d.name).Neighbors.get(s);
                    Q.add(new Dist(s, newDist, d.name));
            }
        }
        // printGraph(draw(d.name));
        return d.name;
    }

    public void Run(){
        while(!Q.isEmpty())
            SingleStep();
    }

    public int AddNode(String name){
        assert source == null; //There should not be selected or in-progress algorithm before editing the graph
        //Return -1 if the node already exists
        if(Nodes.containsKey(name))
            return -1;
        Nodes.put(name, new Node(name));
        return 0;
    }

    public int RemoveNode(String name){
        assert source == null; //There should not be selected or in-progress algorithm before editing the graph
        //Return -1 if the node does not exist
        if(!Nodes.containsKey(name))
            return -1;
        Nodes.remove(name);
        //remove all edges connected to this node
        for(String s : Nodes.keySet()){
            Nodes.get(s).Neighbors.remove(name);
        }
        return 0;
    }
    
    public int AddEdge(String from, String to, int distance){
        assert source == null; //There should not be selected or in-progress algorithm before editing the graph
        //Return -1 if the node does not exist
        if(!Nodes.containsKey(from) || !Nodes.containsKey(to))
            return -1;
        //Return -2 if the edge already exists
        if(Nodes.get(from).Neighbors.containsKey(to))
            return -2;
        Nodes.get(from).Neighbors.put(to, distance);
        Nodes.get(to).Neighbors.put(from, distance);
        return 0;
    }

    public int RemoveEdge(String from, String to){
        assert source == null; //There should not be selected or in-progress algorithm before editing the graph
        //Return -1 if the node does not exist
        if(!Nodes.containsKey(from) || !Nodes.containsKey(to))
            return -1;
        Nodes.get(from).Neighbors.remove(to);
        Nodes.get(to).Neighbors.remove(from);
        return 0;
    }

    public MutableGraph draw(String dest){//highlight the path from source to dest
        Graphviz.useEngine(new GraphvizJdkEngine());
        var set = new HashSet<String>();
        if(dest != null&&Predecessor.get(dest)!=null){
            do {
                set.add(dest);
                dest = Predecessor.get(dest);
            }while (dest != null);
        }
        MutableGraph g = mutGraph("LSA");
        g.nodeAttrs().add(Shape.CIRCLE);
        //avoid repetition of edges
        var edges = new HashMap<String,HashSet<String>>();
        for (String s : Nodes.keySet()) {
            edges.put(s, new HashSet<String>());
        }
        for(String s : Nodes.keySet()){
            var n = mutNode(s).add(Color.ORANGE);
            if(set.contains(s)){
                n.add(Color.RED);
            }else if(Distances.get(s) == Integer.MAX_VALUE){
                n.add(Color.GRAY);
            }
            g.add(n);
            int i = 0;
            for (String t : Nodes.get(s).Neighbors.keySet()) {
                if(edges.get(t).contains(s))continue;
                var e = mutNode(t);
                var lbl = Label.of(Nodes.get(s).Neighbors.get(t).toString());
                n.addLink(t);
                edges.get(s).add(t);
                n.links().get(i).add(Label.of(Nodes.get(s).Neighbors.get(t).toString()));
                if(set.contains(t)&&set.contains(s)&&(Objects.equals(Predecessor.get(t), s) || Objects.equals(Predecessor.get(s), t))){
                    n.links().get(i).add(Color.RED);
                }
                i++;
            }
        }
        return g;
    }

    public int printGraph(MutableGraph g)  {
        try {
            Graphviz.fromGraph(g).width(300).engine(Engine.NEATO).render(Format.PNG).toFile(new File("graph.png"));
        }
        catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
            return -1;
        }
        return 0;
    }


    @Override
    public String toString() {
        return "Known Distances: " + Distances +
                "\nVisited Routers: " + visited +
                "\nEstablished Link: " + Predecessor;
    }
}
