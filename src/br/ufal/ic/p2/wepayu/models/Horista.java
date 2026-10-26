package br.ufal.ic.p2.wepayu.models;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Empregado do tipo horista, remunerado de acordo com as horas
 * registradas em seus cartões de ponto, com pagamento de horas
 * extras (acima de 8h/dia) a 1,5x o valor da hora normal.
 */
public class Horista extends Empregado{
    /**
     * Cria um novo empregado horista.
     *
     * @param nome nome do empregado
     * @param endereco endereço do empregado
     * @param tipo tipo do empregado ("horista")
     * @param salario valor do salário por hora
     */
    public Horista(String nome, String endereco, String tipo, String salario) {
        super(nome, endereco, tipo, salario);
    }

    /**
     * Soma as horas normais (até 8h por dia) dos cartões dentro do período
     * que termina em {@code dataPagamento}.
     *
     * @param dataPagamento data do pagamento
     * @return total de horas normais no período
     */
    public BigDecimal horasNormaisNoPeriodo(LocalDate dataPagamento) {
        BigDecimal total = BigDecimal.ZERO;
        LocalDate inicio = inicioPeriodo(dataPagamento);
        for (CartaoDePonto cartao : getListaCartoes()) {
            LocalDate dataCartao = Utilitarios.validarData(cartao.getData());
            if (!dataCartao.isBefore(inicio) && !dataCartao.isAfter(dataPagamento)) {
                BigDecimal horas = new BigDecimal(cartao.getHoras().replace(",", "."));
                total = total.add(horas.min(new BigDecimal("8")));   // o que passa de 8h não é normal
            }
        }
        return total;
    }

    /**
     * Soma as horas extras (acima de 8h por dia) dos cartões dentro do período
     * que termina em {@code dataPagamento}.
     *
     * @param dataPagamento data do pagamento
     * @return total de horas extras no período
     */
    public BigDecimal horasExtrasNoPeriodo(LocalDate dataPagamento) {
        BigDecimal total = BigDecimal.ZERO;
        LocalDate inicio = inicioPeriodo(dataPagamento);
        for (CartaoDePonto cartao : getListaCartoes()) {
            LocalDate dataCartao = Utilitarios.validarData(cartao.getData());
            if (!dataCartao.isBefore(inicio) && !dataCartao.isAfter(dataPagamento)) {
                BigDecimal horas = new BigDecimal(cartao.getHoras().replace(",", "."));
                total = total.add(horas.subtract(new BigDecimal("8")).max(BigDecimal.ZERO));
            }
        }
        return total;
    }

    /**
     * Calcula o pagamento bruto: horas normais ao valor da hora, mais
     * horas extras a 1,5x o valor da hora, no período da agenda.
     *
     * @param dataPagamento data do pagamento
     * @return o valor bruto a pagar
     */
    @Override
    public BigDecimal calcularPagamento(LocalDate dataPagamento) {
        BigDecimal salarioHora = new BigDecimal(getSalario().replace(",", "."));

        return horasNormaisNoPeriodo(dataPagamento).multiply(salarioHora)
                .add(horasExtrasNoPeriodo(dataPagamento).multiply(salarioHora).multiply(new BigDecimal("1.5")));
    }
}