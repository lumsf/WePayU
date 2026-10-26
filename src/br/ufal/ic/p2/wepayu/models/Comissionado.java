package br.ufal.ic.p2.wepayu.models;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Empregado do tipo comissionado, que recebe um salário fixo quinzenal
 * somado a uma comissão sobre o total de vendas realizadas no período,
 * pago a cada 14 dias a partir de 14/01/2005.
 */
public class Comissionado extends Empregado{
    /** Taxa de comissão aplicada sobre o valor das vendas (ex.: "0,10" para 10%). */
    private String comissao;

    /**
     * Cria um novo empregado comissionado.
     *
     * @param nome nome do empregado
     * @param endereco endereço do empregado
     * @param tipo tipo do empregado ("comissionado")
     * @param salario valor do salário fixo quinzenal
     * @param comissao taxa de comissão sobre as vendas
     * @throws br.ufal.ic.p2.wepayu.Exception.NomeNaoPodeSerNuloException se o nome for nulo/vazio
     * @throws br.ufal.ic.p2.wepayu.Exception.EnderecoNaoPodeSerNuloException se o endereço for nulo/vazio
     */
    public Comissionado(String nome, String endereco, String tipo, String salario, String comissao) {
        super(nome, endereco, tipo, salario);
        this.comissao = comissao;
    }

    /** Parte fixa do pagamento: a fatia do salário mensal que cabe na agenda do empregado. */
    public BigDecimal salarioFixo() {
        BigDecimal salarioMensal = new BigDecimal(getSalario().replace(",", "."));

        return getAgenda().salarioDoPeriodo(salarioMensal);
    }

    /** Total vendido no período que termina em {@code dataPagamento}. */
    public BigDecimal vendasNoPeriodo(LocalDate dataPagamento) {
        BigDecimal total = BigDecimal.ZERO;
        LocalDate inicio = inicioPeriodo(dataPagamento);

        for (Venda venda : getListaVendas()) {
            LocalDate dataVenda = Utilitarios.validarData(venda.getData());

            if (!dataVenda.isBefore(inicio) && !dataVenda.isAfter(dataPagamento)) {
                total = total.add(new BigDecimal(venda.getValor().replace(",", ".")));
            }
        }
        return total;
    }

    /** Comissão sobre as vendas do período, truncada em 2 casas. */
    public BigDecimal valorComissao(LocalDate dataPagamento) {
        BigDecimal percentual = new BigDecimal(comissao.replace(",", "."));

        return vendasNoPeriodo(dataPagamento).multiply(percentual).setScale(2, RoundingMode.DOWN);
    }

    /** Parte fixa somada à comissão do período. */
    @Override
    public BigDecimal calcularPagamento(LocalDate dataPagamento) {
        return salarioFixo().add(valorComissao(dataPagamento));
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