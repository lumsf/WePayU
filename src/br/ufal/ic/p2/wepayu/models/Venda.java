package br.ufal.ic.p2.wepayu.models;

/**
 * Representa uma venda lançada para um empregado comissionado,
 * usada no cálculo da comissão paga sobre o valor vendido.
 */
public class Venda {
    /** Data em que a venda foi realizada, no formato "dd/mm/aaaa". */
    private String data;
    /** Valor da venda (aceita vírgula como separador decimal). */
    private String valor;

    /**
     * Cria uma nova venda.
     *
     * @param data data em que a venda foi realizada
     * @param valor valor da venda
     */
    public Venda(String data, String valor){
        this.data = data;
        this.valor = valor;
    }

    /** @return a data da venda */
    public String getData() {
        return data;
    }

    /** @return o valor da venda */
    public String getValor() {
        return valor;
    }
}