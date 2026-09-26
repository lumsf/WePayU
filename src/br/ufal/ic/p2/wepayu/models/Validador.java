package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.Exception.*;

public class Validador {
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

    public static String formatar(double d){
        return String.format("%.2f", d).replace(".", ",");
    }

}
