package br.ufal.ic.p2.wepayu.models;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Empregado do tipo assalariado, que recebe um salário mensal fixo.
 * Por padrão é pago no último dia útil do mês ("mensal $"), mas pode
 * escolher outra {@link AgendaPagamento}; nesse caso recebe a parte do
 * salário mensal correspondente ao período da agenda.
 */
public class Assalariado extends Empregado{
    /**
     * Cria um novo empregado assalariado.
     *
     * @param nome nome do empregado
     * @param endereco endereço do empregado
     * @param tipo tipo do empregado ("assalariado")
     * @param salario valor do salário mensal fixo
     * @throws br.ufal.ic.p2.wepayu.Exception.NomeNaoPodeSerNuloException se o nome for nulo/vazio
     * @throws br.ufal.ic.p2.wepayu.Exception.EnderecoNaoPodeSerNuloException se o endereço for nulo/vazio
     */
    public Assalariado(String nome, String endereco, String tipo, String salario) {
        super(nome, endereco, tipo, salario);
    }

    /**
     * O pagamento do assalariado é a parte do salário mensal que cabe na
     * agenda dele.
     *
     * @param dataPagamento data do pagamento
     * @return o valor bruto a pagar
     */
    @Override
    public BigDecimal calcularPagamento(LocalDate dataPagamento) {
        BigDecimal salarioMensal = new BigDecimal(getSalario().replace(",", "."));

        return getAgenda().salarioDoPeriodo(salarioMensal);
    }
}