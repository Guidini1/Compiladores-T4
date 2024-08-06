package compiladores.t4;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

public class TabelaDeSimbolos {
    public TabelaDeSimbolos.Tipos returnType;

        // Enumeração dos tipos possíveis usados na tabela de símbolos
    public enum Tipos{
        INT, REAL, CADEIA, LOGICO, INVALIDO, REG, VOID
    }

    // Enumeração para definir as estruturas de dados usadas na tabela
    public enum Structure{
        VAR, CONST, PROC, FUNC, TIPO
    }
    // Classe interna para representar uma entrada na tabela de símbolos

    class EntradaTabelaDeSimbolos{
        String name; // Nome do símbolo
        Tipos tipo; // Tipo do símbolo
        Structure structure;

        // Construtor privado para criar uma nova entrada na tabela
        public EntradaTabelaDeSimbolos(String name, Tipos tipo, Structure structure){
            this.name = name;
            this.tipo = tipo;
            this.structure = structure;

        }

    }
    private HashMap<String, EntradaTabelaDeSimbolos> Mtabela;// Tabela de símbolos armazenada em um HashMap
    private HashMap<String, ArrayList<EntradaTabelaDeSimbolos>> Ttabela;

    // Construtor da classe Tabela, inicializa o HashMap
    public TabelaDeSimbolos(TabelaDeSimbolos.Tipos returnType){
        Mtabela = new HashMap<>();
        Ttabela = new HashMap<>();
        this.returnType = returnType;
    }

    // Retorna o tipo associado a um símbolo
    public Tipos verifica(String name){
        return Mtabela.get(name).tipo;
    }

    // Verifica se um símbolo está presente na tabela
    public boolean existe(String name){
        return Mtabela.containsKey(name); 
    }

    // Adiciona um novo símbolo com seu tipo na tabela
    public void adiciona(String name, Tipos tipo, Structure structure){
        EntradaTabelaDeSimbolos input = new EntradaTabelaDeSimbolos(name, tipo, structure);
        Mtabela.put(name, input);
    }

    public void adiciona(EntradaTabelaDeSimbolos input){
        Mtabela.put(input.name, input);

    }

    public void adiciona(String tipoName, EntradaTabelaDeSimbolos input){
        if(Ttabela.containsKey(tipoName)){
            Ttabela.get(tipoName).add(input);
        }else{
            ArrayList<EntradaTabelaDeSimbolos> list = new ArrayList<>();
            list.add(input);
            Ttabela.put(tipoName, list);
        }
    }

    public ArrayList<EntradaTabelaDeSimbolos> getTypeProperties(String name){
        return Ttabela.get(name);
    }
}