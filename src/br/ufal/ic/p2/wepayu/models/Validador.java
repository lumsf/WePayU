package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.Exception.*;

/**
 * Classe utilitária com métodos estáticos para validar e formatar
 * valores numéricos informados como {@code String} (salário, comissão
 * e taxa sindical), aceitando tanto ponto quanto vírgula como
 * separador decimal na entrada.
 */
public class Validador {
    /**
     * Valida um valor de salário informado como texto.
     *
     * @param valor texto do salário (aceita vírgula ou ponto decimal)
     * @return o valor numérico do salário
     * @throws SalarioNuloException se o valor for nulo ou vazio
     * @throws SalarioNaoNumericoException se o valor não for numérico
     * @throws SalarioNegativoException se o valor for negativo
     */
    public static double validarSalario(String valor) throws EmpregadoNaoExisteException {
        if(valor == null || valor.isEmpty()) throw new SalarioNuloException();
        double d;

        try{
            d = Double.parseDouble(valor.replace(",", "."));
        }
        catch (NumberFormatException ex){
            throw new SalarioNaoNumericoException();
        }
        if(d < 0) throw new SalarioNegativoException();
        return d;
    }

    /**
     * Valida um valor de comissão informado como texto.
     *
     * @param valor texto da comissão (aceita vírgula ou ponto decimal)
     * @return o valor numérico da comissão
     * @throws ComissaoNulaException se o valor for nulo ou vazio
     * @throws ComissaoNaoNumericaException se o valor não for numérico
     * @throws ComissaoNegativaException se o valor for negativo
     */
    public static double validarComissao(String valor) throws EmpregadoNaoExisteException{
        if(valor == null || valor.isEmpty()) throw new ComissaoNulaException();
        double d;

        try{
            d = Double.parseDouble(valor.replace(",", "."));
        }
        catch (NumberFormatException ex){
            throw new ComissaoNaoNumericaException();
        }
        if(d < 0) throw new ComissaoNegativaException();
        return d;
    }

    /**
     * Valida um valor de taxa sindical informado como texto.
     *
     * @param valor texto da taxa sindical (aceita vírgula ou ponto decimal)
     * @return o valor numérico da taxa sindical
     * @throws TaxaSindicalNulaException se o valor for nulo ou vazio
     * @throws TaxaSindicalNaoNumericaException se o valor não for numérico
     * @throws TaxaSindicalNegativaException se o valor for negativo
     */
    public static double validarTaxaSindical(String valor) throws EmpregadoNaoExisteException{
        if(valor == null || valor.isEmpty()) throw new TaxaSindicalNulaException();
        double d;

        try{
            d = Double.parseDouble(valor.replace(",", "."));
        }
        catch (NumberFormatException ex){
            throw new TaxaSindicalNaoNumericaException();
        }
        if(d < 0) throw new TaxaSindicalNegativaException();
        return d;
    }

    /**
     * Formata um valor numérico com duas casas decimais, usando
     * vírgula como separador decimal (padrão usado em todo o sistema).
     *
     * @param d valor numérico a formatar
     * @return o valor formatado, ex.: "1234,56"
     */
    public static String formatar(double d){
        return String.format("%.2f", d).replace(".", ",");
    }

}