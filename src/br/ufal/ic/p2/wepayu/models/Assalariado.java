package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.Exception.EmpregadoNaoExisteException;

/**
 * Empregado do tipo assalariado, que recebe um salário mensal fixo,
 * pago sempre no último dia útil do mês.
 */
public class Assalariado extends Empregado{
    /** Construtor padrão, usado internamente (ex.: cópias de estado para undo/redo). */
    public Assalariado(){
        super();
    }

    /**
     * Cria um novo empregado assalariado.
     *
     * @param nome nome do empregado
     * @param endereco endereço do empregado
     * @param tipo tipo do empregado ("assalariado")
     * @param salario valor do salário mensal fixo
     * @throws EmpregadoNaoExisteException se nome ou endereço forem inválidos
     */
    public Assalariado(String nome, String endereco, String tipo, String salario) throws EmpregadoNaoExisteException {
        super(nome, endereco, tipo, salario);
    }
}