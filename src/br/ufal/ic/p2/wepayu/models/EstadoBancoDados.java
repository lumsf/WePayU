package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.Exception.EmpregadoNaoExisteException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa uma "fotografia" (snapshot) do estado do {@link BancoDados}
 * em um dado momento — a lista de empregados (copiada em profundidade,
 * com todos os seus cartões de ponto, vendas e taxas de serviço) e o
 * contador usado para gerar novos identificadores.
 *
 * <p>É usada para implementar as operações de undo/redo: antes de cada
 * comando que altera o estado do sistema, um {@code EstadoBancoDados} é
 * empilhado, permitindo restaurar o sistema para esse ponto depois.</p>
 */
public class EstadoBancoDados {

    /** Cópia independente da lista de empregados no momento do snapshot. */
    private List<Empregado> empregados;
    /** Valor do contador de identificadores no momento do snapshot. */
    private int contador;

    /**
     * Cria um snapshot do estado atual, copiando profundamente cada
     * empregado da lista informada (incluindo seus cartões de ponto,
     * vendas e taxas de serviço), de forma que alterações futuras na
     * lista original não afetem este snapshot.
     *
     * @param listaEmpregados lista de empregados a ser copiada
     * @param contador valor atual do contador de identificadores
     * @throws Exception se ocorrer erro ao recriar algum empregado
     */
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

    /** @return a lista de empregados armazenada neste snapshot */
    public List<Empregado> getEmpregados() {
        return empregados;
    }

    /** @return o valor do contador de identificadores armazenado neste snapshot */
    public int getContador() {
        return contador;
    }
}