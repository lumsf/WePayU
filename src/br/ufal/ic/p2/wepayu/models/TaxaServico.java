package br.ufal.ic.p2.wepayu.models;

/**
 * Representa uma taxa de serviço lançada para um membro sindicalizado,
 * descontada do salário líquido do empregado no dia do pagamento.
 */
public class TaxaServico {
    /** Data em que a taxa foi lançada, no formato "dd/mm/aaaa". */
    private String data;
    /** Valor da taxa de serviço (aceita vírgula como separador decimal). */
    private String valor;

    /**
     * Cria uma nova taxa de serviço.
     *
     * @param data data em que a taxa foi lançada
     * @param valor valor da taxa
     */
    public TaxaServico(String data, String valor){
        this.data = data;
        this.valor = valor;
    }

    /** @return a data da taxa de serviço */
    public String getData() {
        return data;
    }

    /** @return o valor da taxa de serviço */
    public String getValor() {
        return valor;
    }
}