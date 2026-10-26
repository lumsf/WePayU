package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.Exception.DataInvalidaException;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Utilitarios {
    /**
     * Converte e valida uma data no formato "dd/mm/aaaa".
     *
     * @param data texto da data a validar
     * @return a data convertida para {@link LocalDate}
     * @throws DataInvalidaException se o texto não representar uma data válida
     */
    public static LocalDate validarData(String data){
        try{
            String[] partes = data.split("/");

            if(partes.length != 3) throw new DataInvalidaException();

            int dia = Integer.parseInt(partes[0]);
            int mes = Integer.parseInt(partes[1]);
            int ano = Integer.parseInt(partes[2]);

            return LocalDate.of(ano, mes, dia);
        }
        catch(Exception e){
            if(e instanceof DataInvalidaException) throw (DataInvalidaException) e;
            throw new DataInvalidaException();
        }
    }

    /**
     * Formata um valor monetário com duas casas decimais e vírgula
     * como separador decimal (arredondamento "half up").
     *
     * @param valor valor a formatar
     * @return o valor formatado, ex.: "1234,56"
     */
    public static String formatarDinheiro(BigDecimal valor){
        valor = valor.setScale(2, BigDecimal.ROUND_HALF_UP);

        String resultado = valor.toString();

        resultado = resultado.replace(".", ",");

        return resultado;
    }

    /**
     * Formata uma quantidade de horas removendo zeros à direita
     * desnecessários e usando vírgula como separador decimal.
     *
     * @param valor quantidade de horas a formatar
     * @return o valor formatado, ex.: "8" ou "7,5"
     */
    public static String formatarHoras(BigDecimal valor) {
        valor = valor.stripTrailingZeros();

        return valor.toPlainString().replace(".", ",");
    }
}
