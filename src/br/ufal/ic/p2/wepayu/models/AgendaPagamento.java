package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.Exception.DescricaoDeAgendaInvalidaException;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Agenda de pagamento de um empregado: define em quais datas ele é pago
 * e qual período (e quanto do salário) cada pagamento cobre.
 *
 * <p>Formatos aceitos:</p>
 * <ul>
 *   <li>{@code "semanal D"}: toda semana, no dia D (1 = segunda ... 7 = domingo);</li>
 *   <li>{@code "semanal N D"}: a cada N semanas (1 a 52), no dia D;</li>
 *   <li>{@code "mensal D"}: todo mês, no dia D (1 a 28);</li>
 *   <li>{@code "mensal $"}: no último dia útil do mês.</li>
 * </ul>
 *
 * <p>Objeto imutável: para "mudar" a agenda de um empregado, troca-se o objeto.</p>
 */
public class AgendaPagamento implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Primeiro dia do calendário do sistema; as semanas são contadas a partir dele. */
    private static final LocalDate INICIO_CALENDARIO = LocalDate.of(2005, 1, 1);

    private final String descricao;
    private final boolean mensal;
    /** Mensal: dia do mês (1 a 28), ou 0 para "último dia útil". */
    private final int diaDoMes;
    /** Semanal: de quantas em quantas semanas. */
    private final int intervaloSemanas;
    /** Semanal: dia da semana (1 = segunda ... 7 = domingo). */
    private final int diaDaSemana;

    private AgendaPagamento(String descricao, boolean mensal, int diaDoMes,
                            int intervaloSemanas, int diaDaSemana) {
        this.descricao = descricao;
        this.mensal = mensal;
        this.diaDoMes = diaDoMes;
        this.intervaloSemanas = intervaloSemanas;
        this.diaDaSemana = diaDaSemana;
    }

    /**
     * Cria uma agenda a partir da sua descrição.
     *
     * @param descricao texto da agenda, ex.: "semanal 2 5" ou "mensal $"
     * @return a agenda correspondente
     * @throws DescricaoDeAgendaInvalidaException se o texto não seguir um dos formatos
     */
    public static AgendaPagamento criar(String descricao) {
        if (descricao == null) throw new DescricaoDeAgendaInvalidaException();

        String[] partes = descricao.split(" ");

        if (partes.length == 2 && partes[0].equals("mensal")) {
            if (partes[1].equals("$")) {
                return new AgendaPagamento(descricao, true, 0, 0, 0);
            }
            int dia = lerInteiro(partes[1]);
            if (dia < 1 || dia > 28) throw new DescricaoDeAgendaInvalidaException();
            return new AgendaPagamento(descricao, true, dia, 0, 0);
        }

        if (partes.length == 2 && partes[0].equals("semanal")) {
            int dia = lerInteiro(partes[1]);
            if (dia < 1 || dia > 7) throw new DescricaoDeAgendaInvalidaException();
            return new AgendaPagamento(descricao, false, 0, 1, dia);
        }

        if (partes.length == 3 && partes[0].equals("semanal")) {
            int intervalo = lerInteiro(partes[1]);
            int dia = lerInteiro(partes[2]);
            if (intervalo < 1 || intervalo > 52) throw new DescricaoDeAgendaInvalidaException();
            if (dia < 1 || dia > 7) throw new DescricaoDeAgendaInvalidaException();
            return new AgendaPagamento(descricao, false, 0, intervalo, dia);
        }

        throw new DescricaoDeAgendaInvalidaException();
    }

    /** Agenda padrão de cada tipo de empregado. */
    public static AgendaPagamento padraoDoTipo(String tipo) {
        if (tipo.equals("horista")) return criar("semanal 5");
        if (tipo.equals("comissionado")) return criar("semanal 2 5");
        return criar("mensal $");
    }

    private static int lerInteiro(String texto) {
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            throw new DescricaoDeAgendaInvalidaException();
        }
    }

    /** @return a descrição original da agenda, ex.: "semanal 2 5" */
    public String getDescricao() {
        return descricao;
    }

    /** @return {@code true} se o empregado deve ser pago em {@code data} */
    public boolean ehDiaDePagamento(LocalDate data) {
        if (mensal) {
            if (diaDoMes == 0) return data.equals(ultimoDiaUtil(data));
            return data.getDayOfMonth() == diaDoMes;
        }

        if (data.isBefore(INICIO_CALENDARIO)) return false;
        if (data.getDayOfWeek().getValue() != diaDaSemana) return false;

        long semana = ChronoUnit.DAYS.between(INICIO_CALENDARIO, data) / 7 + 1;
        return semana % intervaloSemanas == 0;
    }

    /** @return o primeiro dia do período que termina no pagamento de {@code dataPagamento} */
    public LocalDate inicioPeriodo(LocalDate dataPagamento) {
        if (!mensal) return dataPagamento.minusDays(7L * intervaloSemanas - 1);
        if (diaDoMes == 0) return dataPagamento.withDayOfMonth(1);
        return dataPagamento.minusMonths(1).plusDays(1);
    }

    /**
     * Parte do salário mensal que cabe em um pagamento desta agenda:
     * o salário inteiro (mensal) ou {@code salário * 12 * N / 52} (semanal),
     * truncado em duas casas.
     */
    public BigDecimal salarioDoPeriodo(BigDecimal salarioMensal) {
        if (mensal) return salarioMensal;

        return salarioMensal
                .multiply(BigDecimal.valueOf(12L * intervaloSemanas))
                .divide(BigDecimal.valueOf(52), 10, RoundingMode.DOWN)
                .setScale(2, RoundingMode.DOWN);
    }

    private static LocalDate ultimoDiaUtil(LocalDate data) {
        LocalDate ultimo = data.withDayOfMonth(data.lengthOfMonth());
        int dia = ultimo.getDayOfWeek().getValue();

        if (dia == 6) return ultimo.minusDays(1);
        if (dia == 7) return ultimo.minusDays(2);
        return ultimo;
    }

    @Override
    public boolean equals(Object outro) {
        return outro instanceof AgendaPagamento
                && descricao.equals(((AgendaPagamento) outro).descricao);
    }

    @Override
    public int hashCode() {
        return descricao.hashCode();
    }

    @Override
    public String toString() {
        return descricao;
    }
}