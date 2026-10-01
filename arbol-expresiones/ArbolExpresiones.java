import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Árbol de expresiones aritméticas.
 *
 * El usuario escribe una expresión (por ejemplo ((3+(2*(6-3)))^2*(10/(8-6)))/(5+1)),
 * el programa construye el árbol, lo imprime, muestra sus recorridos,
 * resuelve la expresión paso a paso e imprime el árbol resuelto
 * (cada nodo con el valor que le corresponde).
 *
 * Operadores soportados: +  -  *  /  ^  y paréntesis. También el signo negativo (-5).
 * Precedencia: ^ (asociativo a la derecha)  >  * /  >  + -
 */
public class ArbolExpresiones {

    // ===================== NODO DEL ÁRBOL =====================
    static class Nodo {
        String dato;      // operador o número
        Nodo izq, der;
        double valor;     // valor calculado al resolver

        Nodo(String dato) { this.dato = dato; }

        Nodo(String operador, Nodo izq, Nodo der) {
            this.dato = operador;
            this.izq = izq;
            this.der = der;
        }

        boolean esHoja() { return izq == null && der == null; }
    }

    // ===================== ANALIZADOR (construye el árbol) =====================
    static class Analizador {
        private final String texto;
        private int pos = 0;

        Analizador(String texto) { this.texto = texto.replaceAll("\\s+", ""); }

        Nodo construir() {
            if (texto.isEmpty()) throw new IllegalArgumentException("La expresion esta vacia.");
            Nodo raiz = expresion();
            if (pos < texto.length())
                throw new IllegalArgumentException("Caracter inesperado '" + texto.charAt(pos) + "' en la posicion " + (pos + 1));
            return raiz;
        }

        // expresion := termino (('+' | '-') termino)*
        private Nodo expresion() {
            Nodo nodo = termino();
            while (pos < texto.length() && (actual() == '+' || actual() == '-')) {
                String op = String.valueOf(texto.charAt(pos++));
                nodo = new Nodo(op, nodo, termino());
            }
            return nodo;
        }

        // termino := potencia (('*' | '/') potencia)*
        private Nodo termino() {
            Nodo nodo = potencia();
            while (pos < texto.length() && (actual() == '*' || actual() == '/')) {
                String op = String.valueOf(texto.charAt(pos++));
                nodo = new Nodo(op, nodo, potencia());
            }
            return nodo;
        }

        // potencia := factor ('^' potencia)?    (asociativa a la derecha: 2^3^2 = 2^(3^2))
        private Nodo potencia() {
            Nodo base = factor();
            if (pos < texto.length() && actual() == '^') {
                pos++;
                return new Nodo("^", base, potencia());
            }
            return base;
        }

        // factor := numero | '(' expresion ')' | '-' factor
        private Nodo factor() {
            if (pos >= texto.length())
                throw new IllegalArgumentException("La expresion termina de forma incompleta.");

            char c = actual();
            if (c == '(') {
                pos++;
                Nodo nodo = expresion();
                if (pos >= texto.length() || actual() != ')')
                    throw new IllegalArgumentException("Falta un parentesis de cierre ')'.");
                pos++;
                return nodo;
            }
            if (c == '-') {                       // signo negativo: se representa como 0 - x
                pos++;
                return new Nodo("-", new Nodo("0"), potencia());
            }
            if (Character.isDigit(c) || c == '.') {
                int inicio = pos;
                while (pos < texto.length() && (Character.isDigit(actual()) || actual() == '.')) pos++;
                String numero = texto.substring(inicio, pos);
                try {
                    Double.parseDouble(numero);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Numero invalido: " + numero);
                }
                return new Nodo(numero);
            }
            throw new IllegalArgumentException("Caracter inesperado '" + c + "' en la posicion " + (pos + 1));
        }

        private char actual() { return texto.charAt(pos); }
    }

    // ===================== RESOLVER (postorden) =====================
    static double resolver(Nodo nodo, List<String> pasos) {
        if (nodo.esHoja()) {
            nodo.valor = Double.parseDouble(nodo.dato);
            return nodo.valor;
        }
        double a = resolver(nodo.izq, pasos);
        double b = resolver(nodo.der, pasos);
        switch (nodo.dato) {
            case "+": nodo.valor = a + b; break;
            case "-": nodo.valor = a - b; break;
            case "*": nodo.valor = a * b; break;
            case "/":
                if (b == 0) throw new ArithmeticException("Division entre cero: " + fmt(a) + " / 0");
                nodo.valor = a / b;
                break;
            case "^": nodo.valor = Math.pow(a, b); break;
            default: throw new IllegalStateException("Operador desconocido: " + nodo.dato);
        }
        pasos.add(fmt(a) + " " + nodo.dato + " " + fmt(b) + " = " + fmt(nodo.valor));
        return nodo.valor;
    }

    // ===================== IMPRESIÓN DEL ÁRBOL =====================

    /** Imprime el árbol de forma vertical (raíz arriba). */
    static void imprimirArbol(Nodo raiz, boolean resuelto) {
        List<String> lineas = new ArrayList<>();
        dibujar(raiz, resuelto, lineas);
        for (String l : lineas) System.out.println("   " + l.replaceAll("\\s+$", ""));
    }

    /**
     * Dibuja el subárbol como un bloque de texto y devuelve la columna
     * donde está centrada la etiqueta de su raíz.
     */
    private static int dibujar(Nodo nodo, boolean resuelto, List<String> salida) {
        String etiqueta = etiqueta(nodo, resuelto);
        if (nodo.esHoja()) {
            salida.add(etiqueta);
            return etiqueta.length() / 2;
        }

        List<String> izq = new ArrayList<>();
        List<String> der = new ArrayList<>();
        int centroIzq = dibujar(nodo.izq, resuelto, izq);
        int centroDer = dibujar(nodo.der, resuelto, der);

        int anchoIzq = ancho(izq);
        int separacion = 3;
        int inicioDer = anchoIzq + separacion;
        int colDer = inicioDer + centroDer;
        int centro = (centroIzq + colDer) / 2;

        // la etiqueta de la raíz no debe salirse por la izquierda
        int inicioEtiqueta = Math.max(0, centro - etiqueta.length() / 2);
        centro = inicioEtiqueta + etiqueta.length() / 2;

        // línea de la etiqueta y línea horizontal que conecta a los hijos
        StringBuilder l1 = new StringBuilder();
        StringBuilder l2 = new StringBuilder();
        StringBuilder l3 = new StringBuilder();
        rellenar(l1, inicioEtiqueta).append(etiqueta);
        rellenar(l2, centroIzq + 1);
        while (l2.length() < colDer) l2.append(l2.length() == centro ? '|' : '_');
        rellenar(l3, centroIzq).append('|');
        rellenar(l3, colDer).append('|');

        salida.add(l1.toString());
        salida.add(l2.toString());
        salida.add(l3.toString());

        int filas = Math.max(izq.size(), der.size());
        for (int i = 0; i < filas; i++) {
            StringBuilder fila = new StringBuilder();
            if (i < izq.size()) fila.append(izq.get(i));
            rellenar(fila, inicioDer);
            if (i < der.size()) fila.append(der.get(i));
            salida.add(fila.toString());
        }
        return centro;
    }

    private static String etiqueta(Nodo nodo, boolean resuelto) {
        if (!resuelto || nodo.esHoja()) return nodo.esHoja() ? fmt(Double.parseDouble(nodo.dato)) : nodo.dato;
        return "[" + nodo.dato + "]=" + fmt(nodo.valor);
    }

    private static int ancho(List<String> lineas) {
        int max = 0;
        for (String l : lineas) max = Math.max(max, l.length());
        return max;
    }

    private static StringBuilder rellenar(StringBuilder sb, int hasta) {
        while (sb.length() < hasta) sb.append(' ');
        return sb;
    }

    // ===================== RECORRIDOS =====================
    static void preorden(Nodo n, StringBuilder sb) {
        if (n == null) return;
        sb.append(n.dato).append(' ');
        preorden(n.izq, sb);
        preorden(n.der, sb);
    }

    static void inorden(Nodo n, StringBuilder sb) {
        if (n == null) return;
        if (!n.esHoja()) sb.append("( ");
        inorden(n.izq, sb);
        sb.append(n.dato).append(' ');
        inorden(n.der, sb);
        if (!n.esHoja()) sb.append(") ");
    }

    static void postorden(Nodo n, StringBuilder sb) {
        if (n == null) return;
        postorden(n.izq, sb);
        postorden(n.der, sb);
        sb.append(n.dato).append(' ');
    }

    // ===================== UTILIDADES =====================

    /** Muestra enteros sin ".0" y decimales con hasta 4 cifras. */
    static String fmt(double v) {
        if (v == Math.rint(v) && !Double.isInfinite(v) && Math.abs(v) < 1e15) return String.valueOf((long) v);
        String s = String.format(java.util.Locale.US, "%.4f", v);
        return s.replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    // ===================== PROGRAMA PRINCIPAL =====================
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("==============================================");
        System.out.println("            ARBOL DE EXPRESIONES");
        System.out.println("==============================================");
        System.out.println("Operadores: +  -  *  /  ^   y parentesis ( )");
        System.out.println("Ejemplo: ((3+(2*(6-3)))^2*(10/(8-6)))/(5+1)");
        System.out.println("Escribe 'salir' para terminar.");

        while (true) {
            System.out.print("\nEscribe la expresion: ");
            if (!sc.hasNextLine()) break;
            String entrada = sc.nextLine().trim();
            if (entrada.equalsIgnoreCase("salir")) break;
            if (entrada.isEmpty()) continue;

            try {
                Nodo raiz = new Analizador(entrada).construir();

                System.out.println("\n--- ARBOL DE LA EXPRESION ---\n");
                imprimirArbol(raiz, false);

                StringBuilder pre = new StringBuilder(), in = new StringBuilder(), post = new StringBuilder();
                preorden(raiz, pre);
                inorden(raiz, in);
                postorden(raiz, post);
                System.out.println("\n--- RECORRIDOS ---");
                System.out.println("Preorden  (prefija) : " + pre.toString().trim());
                System.out.println("Inorden   (infija)  : " + in.toString().trim());
                System.out.println("Postorden (postfija): " + post.toString().trim());

                List<String> pasos = new ArrayList<>();
                double resultado = resolver(raiz, pasos);

                System.out.println("\n--- RESOLUCION PASO A PASO ---");
                for (int i = 0; i < pasos.size(); i++)
                    System.out.println("Paso " + (i + 1) + ": " + pasos.get(i));

                System.out.println("\n--- ARBOL RESUELTO ([operador]=valor) ---\n");
                imprimirArbol(raiz, true);

                System.out.println("\nRESULTADO: " + entrada + " = " + fmt(resultado));
            } catch (IllegalArgumentException | ArithmeticException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        System.out.println("Hasta luego!");
    }
}
