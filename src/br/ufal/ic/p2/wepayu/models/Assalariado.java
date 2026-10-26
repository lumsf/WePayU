package br.ufal.ic.p2.wepayu.models;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Empregado do tipo assalariado, que recebe um salário mensal fixo,
 * pago sempre no último dia útil do mês.
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

    /** O pagamento do assalariado é a parte do salário mensal que cabe na agenda dele. */
    @Override
    public BigDecimal calcularPagamento(LocalDate dataPagamento) {
        BigDecimal salarioMensal = new BigDecimal(getSalario().replace(",", "."));

        return getAgenda().salarioDoPeriodo(salarioMensal);
    }
}