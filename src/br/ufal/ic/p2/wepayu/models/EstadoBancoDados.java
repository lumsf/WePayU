package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.Exception.EmpregadoNaoExisteException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class EstadoBancoDados {

    private List<Empregado> empregados;
    private int contador;

    public EstadoBancoDados(List<Empregado> listaEmpregados, int contador) throws Exception {
        this.empregados = new ArrayList<>();
        this.contador = contador;

        for (int i = 0; i < listaEmpregados.size(); i++) {
            Empregado empregado = listaEmpregados.get(i);

            Empregado copia;

            if (empregado.getTipo().equals("horista")) {
                copia = new Horista(empregado.getNome(), empregado.getEndereco(), empregado.getTipo(), empregado.getSalario());
            }
            else if (empregado.getTipo().equals("assalariado")) {
                copia = new Assalariado(empregado.getNome(), empregado.getEndereco(), empregado.getTipo(), empregado.getSalario());
            }
            else {
                Comissionado original = (Comissionado) empregado;

                copia = new Comissionado(empregado.getNome(), empregado.getEndereco(), empregado.getTipo(), empregado.getSalario(), original.getComissao());
            }

            copia.setId(empregado.getId());
            copia.setMetodoPagamento(empregado.getMetodoPagamento());
            copia.setSindicalizado(empregado.getSindicalizado());
            copia.setIdSindicato(empregado.getIdSindicato());
            copia.setTaxaSindical(empregado.getTaxaSindical());
            copia.setBanco(empregado.getBanco());
            copia.setAgencia(empregado.getAgencia());
            copia.setContaCorrente(empregado.getContaCorrente());
            copia.setDataUltimoPagamento(empregado.getDataUltimoPagamento());
            copia.setDescontosPendentes(empregado.getDescontosPendentes());

            for (int j = 0; j < empregado.getListaCartoes().size(); j++) {
                CartaoDePonto cartao = empregado.getListaCartoes().get(j);
                CartaoDePonto copiaCartao = new CartaoDePonto(cartao.getData(), cartao.getHoras());

                copia.adicionarCartao(copiaCartao);
            }

            for (int j = 0; j < empregado.getListaVendas().size(); j++) {
                Venda venda = empregado.getListaVendas().get(j);
                Venda copiaVenda = new Venda(venda.getData(), venda.getValor());

                copia.adicionarVenda(copiaVenda);
            }

            for (int j = 0; j < empregado.getListaTaxaServico().size(); j++) {
                TaxaServico taxa = empregado.getListaTaxaServico().get(j);
                TaxaServico copiaTaxa = new TaxaServico(taxa.getData(), taxa.getValor());

                copia.adicionarTaxaServico(copiaTaxa);
            }

            empregados.add(copia);
        }
    }

    public List<Empregado> getEmpregados() {
        return empregados;
    }

    public int getContador() {
        return contador;
    }
}