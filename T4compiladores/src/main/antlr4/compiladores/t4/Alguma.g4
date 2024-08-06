grammar Alguma;

// Definição de palavras-chave
PALAVRAS_CHAVES: 
    'algoritmo' | 'declare' | 'literal' | 'inteiro' | 'leia' | 'escreva' | 'tipo' | 'funcao' | 'fim_funcao' | 'retorne' | 'fim_algoritmo' 
    | 'real' | 'logico' | 'var' | 'constante' | 'falso' | 'verdadeiro' | 'caso' | 'seja' | 'fim_caso' | 'se' | 'entao' | 'senao' | 'fim_se' 
    | 'para' | 'ate' | 'faca' | 'fim_para' | 'enquanto' | 'fim_enquanto' | 'registro' | 'fim_registro' | 'procedimento' | 'fim_procedimento';

// Definição de operadores lógicos
OPERADORES_LOGICOS: 'e' | 'ou' | 'nao';

// Definição de operadores aritméticos
OPERADORES_ARITMETICOS: '/' | '+' | '*' | '%' | '-';

// Definição de operadores relacionais
OPERADORES_RELACIONAIS: '<' | '=' | '<>' | '<=' | '>=' | '>';

// Definição de números inteiros
NUM_INT: ('+')? ('0'..'9')+;

// Definição de números reais
NUM_REAL: ('+'|'-')? ('0'..'9')+ '.' ('0'..'9')+;

// Definição de identificadores
IDENT: [a-zA-Z][a-zA-Z0-9_]*;

// Definição de delimitador (:)
DELIM: ':';

// Definição de abre parênteses
ABRE_PAR: '(';

// Definição de fecha parênteses
FECHA_PAR: ')';

// Definição de abre colchetes
ABRE_COL: '[';

// Definição de fecha colchetes
FECHA_COL: ']';

// Definição de vírgula
VIRG: ',';

// Definição de operador de atribuição (<-)
ATRIBUICAO: '<-';

// Definição de ponteiro (^)
PONTEIRO: '^';

// Definição de endereço (&)
ENDERECO: '&';

// Definição de ponto (.)
DOT: '.';

// Definição de intervalo (..)
ATE: '..';

// Definição de espaço em branco (ignorado)
WS: ( ' ' | '\t' | '\r' | '\n' ) -> skip;

// Definição de cadeia entre aspas duplas
CADEIA: '"' (~('\n' | '\r' | '"'))* '"';

// Definição de cadeia sem fim
CADEIA_SEM_FIM : '"' (~('\n' | '\r' | '"'))* ('\n' | '\r');

// Definição de comentário entre chaves
COMENTARIO: '{' (~('\n'|'\r'|'}'))* '}' -> skip;

// Definição de comentário sem fim
COMENTARIO_SEM_FIM : '{' (~('\n'|'\r'|'}'))* ('\n'|'\r');

// Definição de caracter inválido
CARACTER_INVALIDO : .;

// Definição de programa principal
programa: declaracoes 'algoritmo' corpo 'fim_algoritmo' EOF;

// Declarações locais e globais
declaracoes: decl_local_global*;

// Declaração de variável local
decl_local_global: declaracao_local | declaracao_global;

// Declaração de variável local
declaracao_local: declaracao_variavel | declaracao_constante | declaracao_tipo; 

declaracao_variavel: 'declare' variavel;

declaracao_constante: 'constante' IDENT ':' tipo_basico '=' valor_constante;

declaracao_tipo: 'tipo' IDENT ':' tipo; 

// Declaração de variável
variavel: identificador (',' identificador)* ':' tipo;

// Identificador de variável
identificador: IDENT ('.' IDENT)* dimensao;

// Definição de dimensão de array
dimensao: ('[' exp_aritmetica ']')*;

// Definição de tipo de dado
tipo: registro | tipo_estendido;

// Definição de tipo básico
tipo_basico: 'literal' | 'inteiro' | 'real' | 'logico';

// Definição de tipo básico ou identificador
tipo_basico_ident: tipo_basico | IDENT;

// Definição de tipo estendido
tipo_estendido: '^'? tipo_basico_ident;

// Valor constante
valor_constante: CADEIA | NUM_INT | NUM_REAL | 'verdadeiro' | 'falso';

// Definição de registro
registro: 'registro' variavel* 'fim_registro';

// Declaração de procedimento global
declaracao_global: 'procedimento' IDENT '(' (parametros)? ')' (declaracao_local)* (cmd)* 'fim_procedimento' | 
                   'funcao' IDENT '(' (parametros)? ')' ':' tipo_estendido (declaracao_local)* (cmd)* 'fim_funcao'; 

// Parâmetro de procedimento ou função
parametro: 'var'? identificador (',' identificador)* ':' tipo_estendido;

// Lista de parâmetros
parametros: parametro (',' parametro)*;

// Corpo do programa
corpo: declaracao_local* cmd*;

// Comando
cmd: cmdLeia 
   | cmdEscreva 
   | cmdSe 
   | cmdCaso 
   | cmdPara 
   | cmdEnquanto 
   | cmdFaca 
   | cmdAtribuicao 
   | cmdChamada 
   | cmdRetorne;

// Comando de leitura
cmdLeia: 'leia' '(' '^'? identificador (',' '^'? identificador)* ')';

// Comando de escrita
cmdEscreva: 'escreva' '(' expressao (',' expressao)* ')';

// Comando condicional SE
cmdSe: 'se' expressao 'entao' cmd* ('senao' cmd*)? 'fim_se';

// Comando condicional CASO
cmdCaso: 'caso' exp_aritmetica 'seja' selecao ('senao' cmd*)? 'fim_caso';

// Comando de repetição PARA
cmdPara: 'para' IDENT '<-' exp_aritmetica 'ate' exp_aritmetica 'faca' cmd* 'fim_para';

// Comando de repetição ENQUANTO
cmdEnquanto: 'enquanto' expressao 'faca' cmd* 'fim_enquanto';

// Comando de repetição FAÇA
cmdFaca: 'faca' cmd* 'ate' expressao;

// Comando de atribuição
cmdAtribuicao: '^'? identificador '<-' expressao;

// Comando de chamada de função
cmdChamada: IDENT '(' expressao (',' expressao)* ')';

// Comando de retorno
cmdRetorne: 'retorne' expressao;

// Seleção de casos
selecao: item_selecao*;

// Item de seleção
item_selecao: constantes ':' cmd*;

// Constantes de seleção
constantes: numero_intervalo (',' numero_intervalo)*;

// Intervalo de números
numero_intervalo: op_unario? NUM_INT ('..' op_unario? NUM_INT)?;

// Operador unário
op_unario: '-';

// Expressão aritmética
exp_aritmetica: termo (op1 termo)*;

// Termo da expressão aritmética
termo: fator (op2 fator)*;

// Fator da expressão aritmética
fator: parcela (op3 parcela)*;

// Operadores aritméticos
op1: '+' | '-';
op2: '*' | '/';
op3: '%';

// Parcela da expressão aritmética
parcela: op_unario? parcela_unario | parcela_nao_unario;

// Parcela unária
parcela_unario: '^'? identificador | IDENT '(' expressao (',' expressao)* ')' | NUM_INT | NUM_REAL | '(' expressao ')';

// Parcela não unária
parcela_nao_unario: '&' identificador | CADEIA;

// Expressão relacional
exp_relacional: exp_aritmetica (op_relacional exp_aritmetica)?;

// Operadores relacionais
op_relacional: '=' | '<>' | '>=' | '<=' | '>' | '<';

// Expressão
expressao: termo_logico (op_logico_1 termo_logico)*;

// Definição de termo lógico
termo_logico: fator_logico (op_logico_2 fator_logico)*;

// Definição de fator lógico
fator_logico: 'nao'? parcela_logica;

// Definição de parcela lógica
parcela_logica: ('verdadeiro' | 'falso') | exp_relacional;

// Definição de operador lógico OR
op_logico_1: 'ou';

// Definição de operador lógico AND
op_logico_2: 'e';