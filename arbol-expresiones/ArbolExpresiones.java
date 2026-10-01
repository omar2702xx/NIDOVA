import java.util.ArrayList;
import java.util.Scanner;
import java.util.Stack;

public class ArbolExpresiones {

    // Nodo del arbol: guarda un numero o un operador
    static class Nodo {
        String dato;
        Nodo izq, der;

        Nodo(String dato) {
            this.dato = dato;
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Escribe la expresion: ");
        String expresion = sc.nextLine().replace(" ", "");

        try {
            ArrayList<String> postfija = convertirAPostfija(expresion);
            Nodo raiz = construirArbol(postfija);

            ArrayList<String> lineas = new ArrayList<>();
            dibujar(raiz, lineas);
            System.out.println("\nArbol de expresion:\n");
            for (int i = 0; i < lineas.size(); i++) {
                System.out.println(lineas.get(i));
            }
        } catch (RuntimeException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    // ---------- Paso 1: infija a postfija con una pila ----------

    static ArrayList<String> convertirAPostfija(String expresion) {
        ArrayList<String> salida = new ArrayList<>();
        Stack<Character> pila = new Stack<>();
        int i = 0;

        while (i < expresion.length()) {
            char c = expresion.charAt(i);

            if (Character.isDigit(c)) {
                // leer el numero completo (puede tener varios digitos)
                String numero = "";
                while (i < expresion.length() && Character.isDigit(expresion.charAt(i))) {
                    numero = numero + expresion.charAt(i);
                    i++;
                }
                salida.add(numero);
                continue;
            } else if (c == '(') {
                pila.push(c);
            } else if (c == ')') {
                // sacar operadores hasta encontrar el '('
                while (!pila.isEmpty() && pila.peek() != '(') {
                    salida.add("" + pila.pop());
                }
                if (pila.isEmpty()) {
                    throw new RuntimeException("Sobra un parentesis ')'");
                }
                pila.pop(); // quitar el '('
            } else if (esOperador(c)) {
                // sacar los operadores que tengan mayor prioridad
                while (!pila.isEmpty() && pila.peek() != '(' && sacarAntes(pila.peek(), c)) {
                    salida.add("" + pila.pop());
                }
                pila.push(c);
            } else {
                throw new RuntimeException("Signo no valido '" + c + "'");
            }
            i++;
        }

        // pasar lo que quedo en la pila
        while (!pila.isEmpty()) {
            char c = pila.pop();
            if (c == '(') {
                throw new RuntimeException("Falta un parentesis ')'");
            }
            salida.add("" + c);
        }
        return salida;
    }

    static boolean esOperador(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '^';
    }

    static int prioridad(char c) {
        if (c == '^') return 3;
        if (c == '*' || c == '/') return 2;
        return 1; // + y -
    }

    // dice si el operador de la pila se saca antes de meter el nuevo
    static boolean sacarAntes(char enPila, char nuevo) {
        if (nuevo == '^') {
            return prioridad(enPila) > prioridad(nuevo); // ^ se agrupa de derecha a izquierda
        }
        return prioridad(enPila) >= prioridad(nuevo);
    }

    // ---------- Paso 2: postfija a arbol con una pila ----------

    static Nodo construirArbol(ArrayList<String> postfija) {
        Stack<Nodo> pila = new Stack<>();

        for (int i = 0; i < postfija.size(); i++) {
            String dato = postfija.get(i);
            Nodo nodo = new Nodo(dato);

            if (esOperador(dato.charAt(0))) {
                if (pila.size() < 2) {
                    throw new RuntimeException("Falta un numero cerca de '" + dato + "'");
                }
                nodo.der = pila.pop();
                nodo.izq = pila.pop();
            }
            pila.push(nodo);
        }

        if (pila.isEmpty()) {
            throw new RuntimeException("La expresion esta vacia");
        }
        if (pila.size() > 1) {
            throw new RuntimeException("Falta un operador entre los numeros");
        }
        return pila.pop();
    }

    // ---------- Paso 3: dibujar el arbol ----------

    // Dibuja el arbol en una lista de lineas y regresa la columna donde quedo la raiz
    static int dibujar(Nodo n, ArrayList<String> lineas) {
        if (n.izq == null) {
            lineas.add(n.dato);
            return n.dato.length() / 2;
        }

        ArrayList<String> izq = new ArrayList<>();
        ArrayList<String> der = new ArrayList<>();
        int colIzq = dibujar(n.izq, izq);
        int colDer = dibujar(n.der, der);

        // el lado derecho empieza 3 espacios despues de lo mas ancho del lado izquierdo
        int anchoIzq = 0;
        for (int i = 0; i < izq.size(); i++) {
            anchoIzq = Math.max(anchoIzq, izq.get(i).length());
        }
        int inicioDer = anchoIzq + 3;
        colDer = colDer + inicioDer;
        int centro = (colIzq + colDer) / 2;

        lineas.add(repetir(" ", centro) + n.dato);
        lineas.add(repetir(" ", colIzq + 1) + repetir("_", centro - colIzq - 1) + "|" + repetir("_", colDer - centro - 1));
        lineas.add(repetir(" ", colIzq) + "|" + repetir(" ", colDer - colIzq - 1) + "|");

        // juntar los dos lados renglon por renglon
        int renglones = Math.max(izq.size(), der.size());
        for (int i = 0; i < renglones; i++) {
            String linea = "";
            if (i < izq.size()) linea = izq.get(i);
            if (i < der.size()) linea = linea + repetir(" ", inicioDer - linea.length()) + der.get(i);
            lineas.add(linea);
        }
        return centro;
    }

    static String repetir(String s, int veces) {
        String r = "";
        for (int i = 0; i < veces; i++) {
            r = r + s;
        }
        return r;
    }
}
