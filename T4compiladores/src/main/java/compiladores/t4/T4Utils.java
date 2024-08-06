package compiladores.t4;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.Token;

import compiladores.t4.AlgumaParser.Exp_aritmeticaContext;
import compiladores.t4.AlgumaParser.ExpressaoContext;
import compiladores.t4.AlgumaParser.FatorContext;
import compiladores.t4.AlgumaParser.Fator_logicoContext;
import compiladores.t4.AlgumaParser.ParcelaContext;
import compiladores.t4.AlgumaParser.TermoContext;
import compiladores.t4.AlgumaParser.Termo_logicoContext;

public class T4Utils {
    public static List<String> errosSemanticos = new ArrayList<>(); // Armazena os erros semânticos encontrados

    public static void adicionarErroSemantico(Token token, String mensagem) {
        // Adiciona um erro semântico à lista com a linha onde ocorreu
        errosSemanticos.add(String.format("Linha %d: %s", token.getLine(), mensagem));
    }

    public static TabelaDeSimbolos.Tipos verTipo(Escopo escopos, AlgumaParser.ExpressaoContext ctx) {
        // Determina o tipo de uma expressão lógica analisando cada termo lógico
        TabelaDeSimbolos.Tipos retTipo = null;
        for (Termo_logicoContext tl : ctx.termo_logico()) {
            TabelaDeSimbolos.Tipos aux = verTipo(escopos, tl);
            if (retTipo == null) {
                retTipo = aux;
            } else if (retTipo != aux && aux != TabelaDeSimbolos.Tipos.INVALIDO) {
                retTipo = TabelaDeSimbolos.Tipos.INVALIDO;
            }
        }
        return retTipo;
    }

    public static TabelaDeSimbolos.Tipos verTipo(Escopo escopos, AlgumaParser.Termo_logicoContext ctx) {
        // Avalia o tipo de um termo lógico verificando seus fatores lógicos
        TabelaDeSimbolos.Tipos retTipo = null;
        for (Fator_logicoContext fl : ctx.fator_logico()) {
            TabelaDeSimbolos.Tipos aux = verTipo(escopos, fl);
            if (retTipo == null) {
                retTipo = aux;
            } else if (retTipo != aux && aux != TabelaDeSimbolos.Tipos.INVALIDO) {
                retTipo = TabelaDeSimbolos.Tipos.INVALIDO;
            }
        }
        return retTipo;
    }

    public static TabelaDeSimbolos.Tipos verTipo(Escopo escopos, AlgumaParser.Fator_logicoContext ctx) {
        // Verifica o tipo de um fator lógico, que pode ser uma parcela lógica
        return verTipo(escopos, ctx.parcela_logica());
    }

    public static TabelaDeSimbolos.Tipos verTipo(Escopo escopos, AlgumaParser.Parcela_logicaContext ctx) {
        // Identifica se a parcela lógica é relacional ou simplesmente lógica
        if (ctx.exp_relacional() != null) {
            return verTipo(escopos, ctx.exp_relacional());
        }
        return TabelaDeSimbolos.Tipos.LOGICO;
    }

    public static TabelaDeSimbolos.Tipos verTipo(Escopo escopos, AlgumaParser.Exp_relacionalContext ctx) {
        // Avalia o tipo de uma expressão relacional
        TabelaDeSimbolos.Tipos retTipo = null;
        for (Exp_aritmeticaContext ta : ctx.exp_aritmetica()) {
            TabelaDeSimbolos.Tipos aux = verTipo(escopos, ta);
            boolean auxNumeric = aux == TabelaDeSimbolos.Tipos.REAL || aux == TabelaDeSimbolos.Tipos.INT;
            boolean retNumeric = retTipo == TabelaDeSimbolos.Tipos.REAL || retTipo == TabelaDeSimbolos.Tipos.INT;
            if (retTipo == null) {
                retTipo = aux;
            } else if (!(auxNumeric && retNumeric) && aux != retTipo) {
                retTipo = TabelaDeSimbolos.Tipos.INVALIDO;
            }
        }
        if (retTipo != TabelaDeSimbolos.Tipos.INVALIDO && ctx.op_relacional() != null) {
            retTipo = TabelaDeSimbolos.Tipos.LOGICO;
        }
        return retTipo;
    }

    public static TabelaDeSimbolos.Tipos verTipo(Escopo escopos, AlgumaParser.Exp_aritmeticaContext ctx) {
        // Determina o tipo de uma expressão aritmética
        TabelaDeSimbolos.Tipos retTipo = null;
        for (TermoContext ta : ctx.termo()) {
            TabelaDeSimbolos.Tipos aux = verTipo(escopos, ta);
            if (retTipo == null) {
                retTipo = aux;
            } else if (retTipo != aux && aux != TabelaDeSimbolos.Tipos.INVALIDO) {
                retTipo = TabelaDeSimbolos.Tipos.INVALIDO;
            }
        }
        return retTipo;
    }

    public static TabelaDeSimbolos.Tipos verTipo(Escopo escopos, AlgumaParser.TermoContext ctx) {
        // Avalia o tipo de um termo dentro de uma expressão aritmética
        TabelaDeSimbolos.Tipos retTipo = null;
        for (FatorContext fa : ctx.fator()) {
            TabelaDeSimbolos.Tipos aux = verTipo(escopos, fa);
            boolean auxNumeric = aux == TabelaDeSimbolos.Tipos.REAL || aux == TabelaDeSimbolos.Tipos.INT;
            boolean retNumeric = retTipo == TabelaDeSimbolos.Tipos.REAL || retTipo == TabelaDeSimbolos.Tipos.INT;
            if (retTipo == null) {
                retTipo = aux;
            } else if (!(auxNumeric && retNumeric) && aux != retTipo) {
                retTipo = TabelaDeSimbolos.Tipos.INVALIDO;
            }
        }
        return retTipo;
    }

    public static TabelaDeSimbolos.Tipos verTipo(Escopo escopos, AlgumaParser.FatorContext ctx) {
        // Verifica o tipo de um fator, que pode conter uma ou mais parcelas
        TabelaDeSimbolos.Tipos retTipo = null;
        for (ParcelaContext fa : ctx.parcela()) {
            TabelaDeSimbolos.Tipos aux = verTipo(escopos, fa);
            if (retTipo == null) {
                retTipo = aux;
            } else if (retTipo != aux && aux != TabelaDeSimbolos.Tipos.INVALIDO) {
                retTipo = TabelaDeSimbolos.Tipos.INVALIDO;
            }
        }
        return retTipo;
    }

    public static TabelaDeSimbolos.Tipos verTipo(Escopo escopos, AlgumaParser.ParcelaContext ctx) {
        // Identifica o tipo de uma parcela, considerando se é unária ou não
        if (ctx.parcela_nao_unario() != null) {
            return verTipo(escopos, ctx.parcela_nao_unario());
        }
        return verTipo(escopos, ctx.parcela_unario());
    }

    public static TabelaDeSimbolos.Tipos verTipo(Escopo escopos, AlgumaParser.Parcela_nao_unarioContext ctx) {
        // Verifica o tipo de uma parcela não unária, podendo ser identificador ou cadeia
        if (ctx.identificador() != null) {
            return verTipo(escopos, ctx.identificador());
        }
        return TabelaDeSimbolos.Tipos.CADEIA;
    }

    public static TabelaDeSimbolos.Tipos verTipo(Escopo escopos, AlgumaParser.IdentificadorContext ctx) {
        // Determina o tipo de um identificador considerando a hierarquia de escopos
        String var = String.join(".", ctx.IDENT().stream().map(t -> t.getText()).toArray(String[]::new));
        for (TabelaDeSimbolos tabela : escopos.pilhaAtual()) {
            if (tabela.existe(var)) {
                return verTipo(escopos, var);
            }
        }
        return TabelaDeSimbolos.Tipos.INVALIDO;
    }

    public static TabelaDeSimbolos.Tipos verTipo(Escopo escopos, AlgumaParser.Parcela_unarioContext ctx) {
        // Avalia o tipo de uma parcela unária, que pode ser um número, identificador ou expressão
        if (ctx.identificador() != null) {
            return verTipo(escopos, ctx.identificador());
        }
        if (ctx.NUM_REAL() != null) {
            return TabelaDeSimbolos.Tipos.REAL;
        }
        if (ctx.NUM_INT() != null) {
            return TabelaDeSimbolos.Tipos.INT;
        }
        if (ctx.IDENT() != null) {
            return verTipo(escopos, ctx.IDENT().getText());
        }
        TabelaDeSimbolos.Tipos retTipo = null;
        for (ExpressaoContext fa : ctx.expressao()) {
            TabelaDeSimbolos.Tipos aux = verTipo(escopos, fa);
            if (retTipo == null) {
                retTipo = aux;
            } else if (retTipo != aux && aux != TabelaDeSimbolos.Tipos.INVALIDO) {
                retTipo = TabelaDeSimbolos.Tipos.INVALIDO;
            }
        }
        return retTipo;
    }

    public static TabelaDeSimbolos.Tipos verTipo(Escopo escopos, String var) {
        // Verifica o tipo de uma variável específica em todos os escopos disponíveis
        for (TabelaDeSimbolos tabela : escopos.pilhaAtual()) {
            if (tabela.existe(var)) {
                return tabela.verifica(var);
            }
        }
        return TabelaDeSimbolos.Tipos.INVALIDO;
    }

    public static TabelaDeSimbolos.Tipos getTipo(String val) {
        // Converte uma string de tipo para o tipo correspondente na TabelaDeSimbolos
        switch (val) {
            case "real":
                return TabelaDeSimbolos.Tipos.REAL;
            case "inteiro":
                return TabelaDeSimbolos.Tipos.INT;
            case "logico":
                return TabelaDeSimbolos.Tipos.LOGICO;
            case "literal":
                return TabelaDeSimbolos.Tipos.CADEIA;
            default:
                return null;
        }
    }
}
