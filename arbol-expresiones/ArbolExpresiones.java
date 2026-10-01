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
            double resultado = resolver(raiz);
            System.out.println("\nArbol resuelto:\n");
            imprimir(raiz, "");
            System.out.println("\nResultado = " + formato(resultado));
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

    // ---------- Resolver ----------

    static double resolver(Nodo n) {
        if (n.izq == null) return Double.parseDouble(n.dato);
        double a = resolver(n.izq);
        double b = resolver(n.der);
        switch (n.dato) {
            case "+": return a + b;
            case "-": return a - b;
            case "*": return a * b;
            case "/":
                if (b == 0) error("Division entre cero");
                return a / b;
            default:  return Math.pow(a, b);
        }
    }

    // ---------- Imprimir el arbol ----------

    static void imprimir(Nodo n, String sangria) {
        if (n.izq == null) {
            System.out.println(n.dato);
        } else {
            System.out.println(n.dato + "  = " + formato(resolver(n)));
            System.out.print(sangria + "|-- ");
            imprimir(n.izq, sangria + "|   ");
            System.out.print(sangria + "|-- ");
            imprimir(n.der, sangria + "    ");
        }
    }

    static String formato(double v) {
        return v == (long) v ? String.valueOf((long) v) : String.valueOf(v);
    }
}
