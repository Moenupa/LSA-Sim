package Core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NodeTest {

    static String src;
    static String dest;
    static int len;
    static Node a;
    static Node b;

    @BeforeEach
    void init() {
        src = "a";
        dest = "b";
        len = 2;
        a = new Node(src);
        b = new Node(dest);
    }

    @Test
    void addEdge() {
        a.addEdge(dest, len);
        assertEquals(a.getEdge(dest), len);
    }

    @Test
    void removeEdge() {
        a.addEdge(dest, len);
        assertTrue(a.hasEdge(dest));
        a.removeEdge(dest);
        assertFalse(a.hasEdge(dest));
    }

    @Test
    void getName() {
        assertEquals(a.getName(), src);
    }
}