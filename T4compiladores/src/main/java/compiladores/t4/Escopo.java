package compiladores.t4;

import java.util.LinkedList;
import java.util.List;

public class Escopo {

    // A pilha que contém todas as tabelas de símbolos (escopos)
    private LinkedList<TabelaDeSimbolos> pilha; //empilhando tabelas

    // Construtor que inicializa a pilha e cria o primeiro escopo
    public Escopo(TabelaDeSimbolos.Tipos returnType){
        pilha = new LinkedList<>();
        criarNovoEscopo(returnType);
    }

    // Cria um novo escopo (tabela de símbolos) e adiciona à pilha
    public void criarNovoEscopo(TabelaDeSimbolos.Tipos returnType){
        pilha.push(new TabelaDeSimbolos(returnType));
    }

    // Retorna o escopo atual (o topo da pilha) sem removê-lo
    public TabelaDeSimbolos escopoAtual(){
        return pilha.peek();
    }

    public List<TabelaDeSimbolos> pilhaAtual(){
        return pilha;
    }

    // Remove o escopo atual (o topo da pilha)
    public void removerEscopo(){
        pilha.pop();
    }

    public boolean identExists(String name){
        for(TabelaDeSimbolos escopo : pilha) {
            if(!escopo.existe(name)) {
                return true;
            }
        }
        return false;
    }

}