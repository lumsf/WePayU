package br.ufal.ic.p2.wepayu.models;

/**
 * Representa um cartão de ponto lançado para um empregado horista,
 * registrando a quantidade de horas trabalhadas em uma determinada data.
 */
public class CartaoDePonto {
    /** Data do lançamento, no formato "dd/mm/aaaa". */
    private String data;
    /** Quantidade de horas trabalhadas nessa data (aceita vírgula como separador decimal). */
    private String horas;

    /**
     * Cria um novo cartão de ponto.
     *
     * @param data data do lançamento, no formato "dd/mm/aaaa"
     * @param horas quantidade de horas trabalhadas
     */
    public CartaoDePonto(String data, String horas){
        this.data = data;
        this.horas = horas;
    }

    /** @return a data do lançamento */
    public String getData(){
        return data;
    }

    /** @return a quantidade de horas trabalhadas */
    public String getHoras(){
        return horas;
    }
}