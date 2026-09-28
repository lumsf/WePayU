package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.Exception.EmpregadoNaoExisteException;

/**
 * Empregado do tipo comissionado, que recebe um salário fixo quinzenal
 * somado a uma comissão sobre o total de vendas realizadas no período,
 * pago a cada 14 dias a partir de 14/01/2005.
 */
public class Comissionado extends Empregado{
    /** Taxa de comissão aplicada sobre o valor das vendas (ex.: "0,10" para 10%). */
    private String comissao;

    /** Construtor padrão, usado internamente (ex.: cópias de estado para undo/redo). */
    public Comissionado(){
        super();
    }

    /**
     * Cria um novo empregado comissionado.
     *
     * @param nome nome do empregado
     * @param endereco endereço do empregado
     * @param tipo tipo do empregado ("comissionado")
     * @param salario valor do salário fixo quinzenal
     * @param comissao taxa de comissão sobre as vendas
     * @throws EmpregadoNaoExisteException se nome ou endereço forem inválidos
     */
    public Comissionado(String nome, String endereco, String tipo, String salario, String comissao) throws EmpregadoNaoExisteException {
        super(nome, endereco, tipo, salario);
        this.comissao = comissao;
    }

    /** @return a taxa de comissão do empregado */
    public String getComissao() {
        return comissao;
    }

    /** @param comissao nova taxa de comissão do empregado */
    public void setComissao(String comissao){
        this.comissao = comissao;
    }
}