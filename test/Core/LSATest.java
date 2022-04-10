package Core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LSATest {
    static LSA lsa = new LSA();
    static String sample =
            """
                    A: B:5 C:3 D:5
                    B: A:5 C:4 E:3 F:2
                    C: A:3 B:4 D:1 E:6
                    D: A:5 C:1 E:3
                    E: B:3 C:6 D:3 F:5
                    F: B:2 E:5
                    """;

    @BeforeEach
    void init() {
        lsa.loadFromStr(sample);
    }

    @Test
    void parse() {
        lsa.parse();
        assertEquals(lsa.Nodes.get("A").getEdge("B"), 5);
        assertEquals(lsa.Nodes.get("B").getEdge("A"), 5);
    }

    @Test
    void parseLine() {
        lsa.parseLine("X: A:1");
        assertEquals(lsa.Nodes.get("X").getEdge("A"), 1);
        assertEquals(lsa.Nodes.get("A").getEdge("X"), 1);
    }

    @Test
    void setSource() {
        String src = "B";
        lsa.setSource(src);
        assertEquals(lsa.source, src);
    }

    @Test
    void singleStep() {
        lsa.parse();
        lsa.setSource("B");
        String arr[] = {
                "F", "E", "C", "A", "D"
        };
        for (int count = 0; count < arr.length; count++) {
            String step = lsa.SingleStep();
            if (step.isEmpty()) break;
            System.out.println(lsa);
            assertEquals(arr[count], step);
        }
    }

    @Test
    void run() {
        lsa.parse();
        lsa.setSource("B");
        String arr[] = {
                "F", "E", "C", "A", "D"
        };
        int distance[] = {
                2, 3, 4, 5, 5
        };
        lsa.Run();
        for (int count = 0; count < distance.length; count++) {
            assertEquals(lsa.Distances.get(arr[count]), distance[count]);
        }
    }
}