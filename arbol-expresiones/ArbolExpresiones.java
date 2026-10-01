import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ArbolExpresiones {

    // Nodo del arbol: guarda un numero o un operador
    static class Nodo {
        String dato;
        Nodo izq, der;

        Nodo(String dato, Nodo izq, Nodo der) {
            this.dato = dato;
            this.izq = izq;
            this.der = der;
        }
    }

    static String texto; // expresion sin espacios
    static int pos;      // posicion actual al leer la expresion

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Escribe la expresion: ");
        texto = sc.nextLine().replace(" ", "");
        pos = 0;

        try {
            Nodo raiz = suma();
            if (pos < texto.length()) {
                if (texto.charAt(pos) == ')') error("Sobra un parentesis ')'");
                error("Signo no valido '" + texto.charAt(pos) + "'");
            }
            List<String> lineas = new ArrayList<>();
            dibujar(raiz, lineas);
            System.out.println("\nArbol de expresion:\n");
            for (String linea : lineas) System.out.println(linea);
        } catch (RuntimeException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    // ---------- Construir el arbol ----------

    // suma -> multiplicacion ( (+|-) multiplicacion )*
    static Nodo suma() {
        Nodo nodo = multiplicacion();
        while (pos < texto.length() && (texto.charAt(pos) == '+' || texto.charAt(pos) == '-')) {
            String op = "" + texto.charAt(pos++);
            nodo = new Nodo(op, nodo, multiplicacion());
        }
        return nodo;
    }

    // multiplicacion -> potencia ( (*|/) potencia )*
    static Nodo multiplicacion() {
        Nodo nodo = potencia();
        while (pos < texto.length() && (texto.charAt(pos) == '*' || texto.charAt(pos) == '/')) {
            String op = "" + texto.charAt(pos++);
            nodo = new Nodo(op, nodo, potencia());
        }
        return nodo;
    }

    // potencia -> factor ( ^ potencia )?
    static Nodo potencia() {
        Nodo nodo = factor();
        if (pos < texto.length() && texto.charAt(pos) == '^') {
            pos++;
            nodo = new Nodo("^", nodo, potencia());
        }
        return nodo;
    }

    // factor -> numero | ( suma )
    static Nodo factor() {
        if (pos >= texto.length()) error("La expresion esta incompleta");
        char c = texto.charAt(pos);

        if (c == '(') {
            pos++;
            Nodo nodo = suma();
            if (pos >= texto.length() || texto.charAt(pos) != ')') error("Falta un parentesis ')'");
            pos++;
            return nodo;
        }
        if (Character.isDigit(c)) {
            int inicio = pos;
            while (pos < texto.length() && Character.isDigit(texto.charAt(pos))) pos++;
            return new Nodo(texto.substring(inicio, pos), null, null);
        }
        if (c == ')') error("Sobra un parentesis ')' o falta un numero");
        if ("+-*/^".indexOf(c) >= 0) error("Falta un numero antes de '" + c + "'");
        error("Signo no valido '" + c + "'");
        return null;
    }

    static void error(String mensaje) {
        throw new RuntimeException(mensaje);
    }

    // ---------- Imprimir el arbol ----------

    // Dibuja el arbol en una lista de lineas y devuelve la columna donde quedo su raiz
    static int dibujar(Nodo n, List<String> lineas) {
        if (n.izq == null) {
            lineas.add(n.dato);
            return n.dato.length() / 2;
        }

        List<String> izq = new ArrayList<>();
        List<String> der = new ArrayList<>();
        int colIzq = dibujar(n.izq, izq);
        int colDer = dibujar(n.der, der);

        int inicioDer = ancho(izq) + 3;   // el subarbol derecho va 3 espacios despues del izquierdo
        colDer += inicioDer;
        int centro = (colIzq + colDer) / 2;

        lineas.add(espacios(centro) + n.dato);
        lineas.add(espacios(colIzq + 1) + "_".repeat(centro - colIzq - 1) + "|" + "_".repeat(colDer - centro - 1));
        lineas.add(espacios(colIzq) + "|" + espacios(colDer - colIzq - 1) + "|");

        for (int i = 0; i < Math.max(izq.size(), der.size()); i++) {
            String linea = i < izq.size() ? izq.get(i) : "";
            if (i < der.size()) linea += espacios(inicioDer - linea.length()) + der.get(i);
            lineas.add(linea);
        }
        return centro;
    }

    static int ancho(List<String> lineas) {
        int max = 0;
        for (String l : lineas) max = Math.max(max, l.length());
        return max;
    }

    static String espacios(int n) {
        return " ".repeat(Math.max(0, n));
    }
}
