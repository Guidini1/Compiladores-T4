package compiladores.t4;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.tree.TerminalNode;

import compiladores.t4.AlgumaParser.CmdAtribuicaoContext;
import compiladores.t4.AlgumaParser.CmdRetorneContext;
import compiladores.t4.AlgumaParser.Declaracao_constanteContext;
import compiladores.t4.AlgumaParser.Declaracao_globalContext;
import compiladores.t4.AlgumaParser.Declaracao_tipoContext;
import compiladores.t4.AlgumaParser.Declaracao_variavelContext;
import compiladores.t4.AlgumaParser.IdentificadorContext;
import compiladores.t4.AlgumaParser.ParametroContext;
import compiladores.t4.AlgumaParser.Parcela_unarioContext;
import compiladores.t4.AlgumaParser.ProgramaContext;
import compiladores.t4.AlgumaParser.Tipo_basico_identContext;
import compiladores.t4.AlgumaParser.VariavelContext;
import compiladores.t4.TabelaDeSimbolos.EntradaTabelaDeSimbolos;
import java.util.Iterator;

public class T4Semantico extends AlgumaBaseVisitor {
    
    Escopo escoposAninhados = new Escopo(TabelaDeSimbolos.Tipos.VOID);
    @Override
    public Object visitPrograma(ProgramaContext ctx) {  return super.visitPrograma(ctx);
    }

    //método chamado quando o analisador encontra uma declaração de constante no código fonte constante foi declarada anteriormente, adicionar a constante à tabela 
    //de símbolos gerar um erro semântico caso a constante já tenha sido declarada 
    @Override
    public Object visitDeclaracao_constante(Declaracao_constanteContext ctx) {
        TabelaDeSimbolos atual = escoposAninhados.escopoAtual();
        if (atual.existe(ctx.IDENT().getText())) {
            T4Utils.adicionarErroSemantico(ctx.start, "constante" + ctx.IDENT().getText()+ " ja declarado anteriormente");
        } else {
            TabelaDeSimbolos.Tipos tipo = TabelaDeSimbolos.Tipos.INT;
            TabelaDeSimbolos.Tipos aux = T4Utils.getTipo(ctx.tipo_basico().getText()) ;
            if(aux != null)
                tipo = aux;
            atual.adiciona(ctx.IDENT().getText(), tipo, TabelaDeSimbolos.Structure.CONST);
        }
    return super.visitDeclaracao_constante(ctx);
    }

    //essa função é responsável por verificar se um novo tipo foi declarado anteriormente, adicionar o novo tipo à tabela de símbolos, juntamente com seus identificadores
    @Override
    public Object visitDeclaracao_tipo(Declaracao_tipoContext ctx) {
        TabelaDeSimbolos atual = escoposAninhados.escopoAtual();
        if (atual.existe(ctx.IDENT().getText())) {
             T4Utils.adicionarErroSemantico(ctx.start, "tipo " + ctx.IDENT().getText()+ " declarado duas vezes num mesmo escopo");
        } else {
            TabelaDeSimbolos.Tipos tipo = T4Utils.getTipo(ctx.tipo().getText());
            if(tipo != null)
                atual.adiciona(ctx.IDENT().getText(), tipo, TabelaDeSimbolos.Structure.TIPO);
            else if(ctx.tipo().registro() != null){
                ArrayList<TabelaDeSimbolos.EntradaTabelaDeSimbolos> varReg = new ArrayList<>();
                Iterator<VariavelContext> varIterator = ctx.tipo().registro().variavel().iterator();
                while (varIterator.hasNext()) {
                    VariavelContext va = varIterator.next();
                    TabelaDeSimbolos.Tipos tipoReg = T4Utils.getTipo(va.tipo().getText());
                    
                    Iterator<IdentificadorContext> idIterator = va.identificador().iterator();
                    while (idIterator.hasNext()) {
                        IdentificadorContext id2 = idIterator.next();
                        varReg.add(atual.new EntradaTabelaDeSimbolos(id2.getText(), tipoReg, TabelaDeSimbolos.Structure.TIPO));
                    }
                }
                if (atual.existe(ctx.IDENT().getText())) {
                    T4Utils.adicionarErroSemantico(ctx.start, "identificador " + ctx.IDENT().getText() + " ja declarado anteriormente");
                }
                else{
                    atual.adiciona(ctx.IDENT().getText(), TabelaDeSimbolos.Tipos.REG, TabelaDeSimbolos.Structure.TIPO);
                }

                for(TabelaDeSimbolos.EntradaTabelaDeSimbolos re : varReg){
                    String nameVar = ctx.IDENT().getText() + '.' + re.name;
                    if (atual.existe(nameVar)) {
                        T4Utils.adicionarErroSemantico(ctx.start, "identificador " + nameVar + " ja declarado anteriormente");
                    }
                    else{
                        atual.adiciona(re);
                        atual.adiciona(ctx.IDENT().getText(), re);
                    }
                }
            }
            TabelaDeSimbolos.Tipos t =  T4Utils.getTipo(ctx.tipo().getText());
            atual.adiciona(ctx.IDENT().getText(), t, TabelaDeSimbolos.Structure.TIPO);
        }   return super.visitDeclaracao_tipo(ctx);
    }

    //verifica se os identificadores de tipos personalizados (identificadores de tipo) utilizados no código foram previamente declarados. 
    //Se um identificador de tipo não tiver sido declarado, um erro semântico é gerado
    @Override
    public Object visitTipo_basico_ident(Tipo_basico_identContext ctx) {
        if(ctx.IDENT() != null){
            boolean existe = false;
            for(TabelaDeSimbolos escopo : escoposAninhados.pilhaAtual()) {
                if(escopo.existe(ctx.IDENT().getText())) {
                    existe = true;
                }
            }
            if(!existe){
                T4Utils.adicionarErroSemantico(ctx.start, "tipo " + ctx.IDENT().getText() + " nao declarado");
            }
        }   return super.visitTipo_basico_ident(ctx);
    }

    //verificar e adicionar as declarações globais (funções ou procedimentos) à tabela de símbolos, 
    //tratando corretamente os parâmetros e gerando erros semânticos quando necessário
    @Override
    public Object visitDeclaracao_global(Declaracao_globalContext ctx) {
        TabelaDeSimbolos atual = escoposAninhados.escopoAtual();
        Object ret;
        if (atual.existe(ctx.IDENT().getText())) {
            T4Utils.adicionarErroSemantico(ctx.start, ctx.IDENT().getText() + " ja declarado anteriormente");
            ret = super.visitDeclaracao_global(ctx);
        } else {
            TabelaDeSimbolos.Tipos returnTypeFunc = TabelaDeSimbolos.Tipos.VOID;
            if(ctx.getText().startsWith("funcao")){
                returnTypeFunc = T4Utils.getTipo(ctx.tipo_estendido().getText());
                atual.adiciona(ctx.IDENT().getText(), returnTypeFunc, TabelaDeSimbolos.Structure.FUNC);
            }
            else{
                returnTypeFunc = TabelaDeSimbolos.Tipos.VOID;
                atual.adiciona(ctx.IDENT().getText(), returnTypeFunc, TabelaDeSimbolos.Structure.PROC);
            }
            escoposAninhados.criarNovoEscopo(returnTypeFunc);
            TabelaDeSimbolos escopoAntigo = atual;
            atual = escoposAninhados.escopoAtual();
            if(ctx.parametros() != null){
                for(ParametroContext p : ctx.parametros().parametro()){
                    for (IdentificadorContext id : p.identificador()) {
                        String nomeId = "";
                        int i = 0;
                        for(TerminalNode ident : id.IDENT()){
                            if(i++ > 0)
                                nomeId += ".";
                            nomeId += ident.getText();
                        }
                        if (atual.existe(nomeId)) {
                            T4Utils.adicionarErroSemantico(id.start, "identificador " + nomeId + " ja declarado anteriormente");
                        } else {
                            TabelaDeSimbolos.Tipos tipo = T4Utils.getTipo(p.tipo_estendido().getText());
                            if(tipo != null){
                                EntradaTabelaDeSimbolos in = atual.new EntradaTabelaDeSimbolos(nomeId, tipo, TabelaDeSimbolos.Structure.VAR);
                                atual.adiciona(in);
                                escopoAntigo.adiciona(ctx.IDENT().getText(), in);
                            }
                            else{
                                TerminalNode identTipo =    p.tipo_estendido().tipo_basico_ident() != null  
                                                            && p.tipo_estendido().tipo_basico_ident().IDENT() != null 
                                                            ? p.tipo_estendido().tipo_basico_ident().IDENT() : null;
                                if(identTipo != null){
                                    ArrayList<TabelaDeSimbolos.EntradaTabelaDeSimbolos> regVars = null;
                                    boolean found = false;
                                    for(TabelaDeSimbolos t: escoposAninhados.pilhaAtual()){
                                        if(!found){
                                            if(t.existe(identTipo.getText())){
                                                regVars = t.getTypeProperties(identTipo.getText());
                                                found = true;
                                            }
                                        }
                                    }
                                    if(atual.existe(nomeId)){
                                        T4Utils.adicionarErroSemantico(id.start, "identificador " + nomeId + " ja declarado anteriormente");
                                    } else{
                                        EntradaTabelaDeSimbolos in = atual.new EntradaTabelaDeSimbolos(nomeId, TabelaDeSimbolos.Tipos.REG, TabelaDeSimbolos.Structure.VAR);
                                        atual.adiciona(in);
                                        escopoAntigo.adiciona(ctx.IDENT().getText(), in);
                                        for(TabelaDeSimbolos.EntradaTabelaDeSimbolos s: regVars){
                                            atual.adiciona(nomeId + "." + s.name, s.tipo, TabelaDeSimbolos.Structure.VAR);
                                        }   
                                    }
                                }
                            }
                        }
                    }
                }
            }
            ret = super.visitDeclaracao_global(ctx);
            escoposAninhados.removerEscopo();
        }   
        return ret;
    }

    //por verifica se um identificado é válido, ou seja, se ele foi previamente declarado em algum dos escopos presentes na pilha de escopos. 
    //Se um identificador não tiver sido declarado, um erro semântico é gerado
    @Override
    public Object visitIdentificador(IdentificadorContext ctx) {
        String Var = "";
        int i = 0;
        for(TerminalNode id : ctx.IDENT()){
            if(i++ > 0)
                Var += ".";
            Var += id.getText();
        }
        boolean erro = true;
        for(TabelaDeSimbolos escopo : escoposAninhados.pilhaAtual()) {
            if(escopo.existe(Var)) {
                erro = false;
            }
        }
        if(erro)
            T4Utils.adicionarErroSemantico(ctx.start, "identificador " + Var + " nao declarado");    return super.visitIdentificador(ctx);
    }

    //verifica e adiciona as declarações de variáveis à tabela de símbolos, tratando corretamente os diferentes tipos de variáveis, 
    //incluindo variáveis com membros de tipos registro e também adiciona erros semânticos quando identificadores duplicados são encontrados
    @Override
    public Object visitDeclaracao_variavel(Declaracao_variavelContext ctx) {
        TabelaDeSimbolos atual = escoposAninhados.escopoAtual();
        for (IdentificadorContext id : ctx.variavel().identificador()) {
            String nomeId = "";
            int i = 0;
            for(TerminalNode ident : id.IDENT()){
                if(i++ > 0)
                    nomeId += ".";
                nomeId += ident.getText();
            }
            if (atual.existe(nomeId)) {
                T4Utils.adicionarErroSemantico(id.start, "identificador " + nomeId + " ja declarado anteriormente");
            } else {
                TabelaDeSimbolos.Tipos tipo = T4Utils.getTipo(ctx.variavel().tipo().getText());
                if(tipo != null)
                    atual.adiciona(nomeId, tipo, TabelaDeSimbolos.Structure.VAR);
                else{
                    TerminalNode identTipo =    ctx.variavel().tipo() != null
                                                && ctx.variavel().tipo().tipo_estendido() != null 
                                                && ctx.variavel().tipo().tipo_estendido().tipo_basico_ident() != null  
                                                && ctx.variavel().tipo().tipo_estendido().tipo_basico_ident().IDENT() != null 
                                                ? ctx.variavel().tipo().tipo_estendido().tipo_basico_ident().IDENT() : null;
                    if(identTipo != null){
                        ArrayList<TabelaDeSimbolos.EntradaTabelaDeSimbolos> regVars = null;
                        boolean found = false;
                        for(TabelaDeSimbolos t: escoposAninhados.pilhaAtual()){
                            if(!found){
                                if(t.existe(identTipo.getText())){
                                    regVars = t.getTypeProperties(identTipo.getText());
                                    found = true;
                                }
                            }
                        }
                        if(atual.existe(nomeId)){
                            T4Utils.adicionarErroSemantico(id.start, "identificador " + nomeId
                                        + " ja declarado anteriormente");
                        } else{
                            atual.adiciona(nomeId, TabelaDeSimbolos.Tipos.REG, TabelaDeSimbolos.Structure.VAR);
                            for(TabelaDeSimbolos.EntradaTabelaDeSimbolos s: regVars){
                                atual.adiciona(nomeId + "." + s.name, s.tipo, TabelaDeSimbolos.Structure.VAR);
                            }   
                        }
                    }
                    else if(ctx.variavel().tipo().registro() != null){
                        ArrayList<TabelaDeSimbolos.EntradaTabelaDeSimbolos> varReg = new ArrayList<>();
                        for(VariavelContext va : ctx.variavel().tipo().registro().variavel()){
                            TabelaDeSimbolos.Tipos tipoReg =  T4Utils.getTipo(va.tipo().getText());
                            for(IdentificadorContext id2 : va.identificador()){
                                varReg.add(atual.new EntradaTabelaDeSimbolos(id2.getText(), tipoReg, TabelaDeSimbolos.Structure.VAR));
                            }
                        }  
                        atual.adiciona(nomeId, TabelaDeSimbolos.Tipos.REG, TabelaDeSimbolos.Structure.VAR);

                        for(TabelaDeSimbolos.EntradaTabelaDeSimbolos re : varReg){
                            String nameVar = nomeId + '.' + re.name;
                            if (atual.existe(nameVar)) {
                                T4Utils.adicionarErroSemantico(id.start, "identificador " + nameVar + " ja declarado anteriormente");
                            }
                            else{
                                atual.adiciona(re);
                                atual.adiciona(nameVar, re.tipo, TabelaDeSimbolos.Structure.VAR);
                            }
                        }
                    }
                    else{
                        atual.adiciona(id.getText(), TabelaDeSimbolos.Tipos.INT, TabelaDeSimbolos.Structure.VAR);
                    }
                }
            }
        }   
        return super.visitDeclaracao_variavel(ctx);
    }

    // verifica se o comando retorne é permitido no escopo atual. Se o escopo for um procedimento 
    //(com tipo de retorno VOID), o comando retorne não é permitido e um erro semântico é gerado
    @Override
    public Object visitCmdRetorne(CmdRetorneContext ctx) {
        if(escoposAninhados.escopoAtual().returnType == TabelaDeSimbolos.Tipos.VOID){
            T4Utils.adicionarErroSemantico(ctx.start, "comando retorne nao permitido nesse escopo");
        }   return super.visitCmdRetorne(ctx);
    }

    //verifica a compatibilidade da atribuição entre a expressão do lado direito e a variável do lado esquerdo. 
    //Se a atribuição não for compatível (por exemplo, tentando atribuir um valor real a uma variável inteira), um erro semântico é gerado. 
    @Override
    public Object visitCmdAtribuicao(CmdAtribuicaoContext ctx) {
        TabelaDeSimbolos.Tipos tipoExpressao = T4Utils.verTipo(escoposAninhados, ctx.expressao());
        boolean error = false;
        String pointerChar = ctx.getText().charAt(0) == '^' ? "^" : "";
        String Var = "";
        int i = 0;
        for(TerminalNode id : ctx.identificador().IDENT()){
            if(i++ > 0)
                Var += ".";
            Var += id.getText();
        }
        if (tipoExpressao != TabelaDeSimbolos.Tipos.INVALIDO) {
            boolean found = false;
            for(TabelaDeSimbolos escopo : escoposAninhados.pilhaAtual()){
                if (escopo.existe(Var) && !found)  {
                    found = true;
                    TabelaDeSimbolos.Tipos tipoVariavel = T4Utils.verTipo(escoposAninhados, Var);
                    Boolean varNumeric = tipoVariavel == TabelaDeSimbolos.Tipos.REAL || tipoVariavel == TabelaDeSimbolos.Tipos.INT;
                    Boolean expNumeric = tipoExpressao == TabelaDeSimbolos.Tipos.REAL || tipoExpressao == TabelaDeSimbolos.Tipos.INT;
                    if  (!(varNumeric && expNumeric) && tipoVariavel != tipoExpressao && tipoExpressao != TabelaDeSimbolos.Tipos.INVALIDO) {
                        error = true;
                    }
                } 
            }
        } else{
            error = true;
        }

        if(error){
            Var = ctx.identificador().getText();
            T4Utils.adicionarErroSemantico(ctx.identificador().start, "atribuicao nao compativel para " + pointerChar + Var );
        }
        return super.visitCmdAtribuicao(ctx);
    }

    //responsável por verificar se a chamada de função ou procedimento possui os parâmetros corretos 
    //e compatíveis com os declarados na declaração da função ou procedimento. Se houver incompatibilidade nos parâmetros, um erro semântico é gerado
    @Override
    public Object visitParcela_unario(Parcela_unarioContext ctx) {
        TabelaDeSimbolos atual = escoposAninhados.escopoAtual();
        if(ctx.IDENT() != null){
            String name = ctx.IDENT().getText();
            if(atual.existe(ctx.IDENT().getText())){
                List<EntradaTabelaDeSimbolos> params = atual.getTypeProperties(name);
                boolean error = false;
                if(params.size() != ctx.expressao().size()){
                    error = true;
                } else {
                    for(int i = 0; i < params.size(); i++){
                        if(params.get(i).tipo != T4Utils.verTipo(escoposAninhados, ctx.expressao().get(i))){
                            error = true;
                        }
                    }
                }
                if(error){
                    T4Utils.adicionarErroSemantico(ctx.start, "incompatibilidade de parametros na chamada de " + name);
                }
            }
        }
        return super.visitParcela_unario(ctx);
    }
}