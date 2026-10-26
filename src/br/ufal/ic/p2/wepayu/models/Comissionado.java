package br.ufal.ic.p2.wepayu.models;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Empregado do tipo comissionado, que recebe uma parte fixa do salário
 * mensal somada a uma comissão sobre o total de vendas realizadas no
 * período. Por padrão é pago a cada 2 semanas, às sextas-feiras
 * ("semanal 2 5"), mas pode escolher outra {@link AgendaPagamento}.
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
     * @param salario valor do salário mensal
     * @param comissao taxa de comissão sobre as vendas
     * @throws br.ufal.ic.p2.wepayu.Exception.NomeNaoPodeSerNuloException se o nome for nulo/vazio
     * @throws br.ufal.ic.p2.wepayu.Exception.EnderecoNaoPodeSerNuloException se o endereço for nulo/vazio
     */
    public Comissionado(String nome, String endereco, String tipo, String salario, String comissao) {
        super(nome, endereco, tipo, salario);
        this.comissao = comissao;
    }

    /**
     * Parte fixa do pagamento: a fatia do salário mensal que cabe na
     * agenda do empregado.
     *
     * @return o salário fixo de um pagamento
     */
    public BigDecimal salarioFixo() {
        BigDecimal salarioMensal = new BigDecimal(getSalario().replace(",", "."));

        return getAgenda().salarioDoPeriodo(salarioMensal);
    }

    /**
     * Soma as vendas dentro do período que termina em {@code dataPagamento}.
     *
     * @param dataPagamento data do pagamento
     * @return total vendido no período
     */
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

    /**
     * Calcula a comissão sobre as vendas do período, truncada em 2 casas.
     *
     * @param dataPagamento data do pagamento
     * @return o valor da comissão
     */
    public BigDecimal valorComissao(LocalDate dataPagamento) {
        BigDecimal percentual = new BigDecimal(comissao.replace(",", "."));

        return vendasNoPeriodo(dataPagamento).multiply(percentual).setScale(2, RoundingMode.DOWN);
    }

    /**
     * Calcula o pagamento bruto: parte fixa somada à comissão do período.
     *
     * @param dataPagamento data do pagamento
     * @return o valor bruto a pagar
     */
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