package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.Exception.EmpregadoNaoExisteException;

/**
 * Empregado do tipo horista, remunerado de acordo com as horas
 * registradas em seus cartões de ponto, com pagamento de horas
 * extras (acima de 8h/dia) a 1,5x o valor da hora normal.
 */
public class Horista extends Empregado{
    /** Construtor padrão, usado internamente (ex.: cópias de estado para undo/redo). */
    public Horista(){
        super();
    }

    /**
     * Cria um novo empregado horista.
     *
     * @param nome nome do empregado
     * @param endereco endereço do empregado
     * @param tipo tipo do empregado ("horista")
     * @param salario valor do salário por hora
     * @throws EmpregadoNaoExisteException se nome ou endereço forem inválidos
     */
    public Horista(String nome, String endereco, String tipo, String salario) throws EmpregadoNaoExisteException {
        super(nome, endereco, tipo, salario);

    }
}