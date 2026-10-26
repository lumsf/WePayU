package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.Exception.EnderecoNaoPodeSerNuloException;
import br.ufal.ic.p2.wepayu.Exception.NomeNaoPodeSerNuloException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.io.Serializable;

/**
 * Classe base que representa um empregado do sistema WePayU, com os
 * dados cadastrais comuns a todos os tipos (horista, assalariado e
 * comissionado): identificação, dados de sindicalização, forma de
 * pagamento e os lançamentos associados (cartões de ponto, vendas e
 * taxas de serviço).
 *
 * <p>As subclasses {@link Horista}, {@link Assalariado} e
 * {@link Comissionado} especializam a forma como o pagamento é
 * calculado; esta classe concentra apenas os dados e as regras
 * comuns de cadastro.</p>
 */
public abstract class Empregado implements Serializable {
    private static final long serialVersionUID = 1L;
    /** Identificador único do empregado (ex.: "id1", "id2", ...). */
    private String id;
    /** Nome do empregado. */
    private String nome;
    /** Endereço do empregado. */
    private String endereco;
    /** Tipo do empregado: "horista", "assalariado" ou "comissionado". */
    private String tipo;
    /** Salário do empregado (por hora para horistas; mensal para os demais tipos). */
    private String salario;
    /** Agenda de pagamento: define quando o empregado é pago e que período cada pagamento cobre. */
    private AgendaPagamento agenda;
    /** Forma de pagamento: "emMaos", "banco" ou "correios". */
    private String metodoPagamento;
    /** Indica se o empregado é sindicalizado. */
    private boolean sindicalizado;
    /** Identificação do sindicato ao qual o empregado pertence (se sindicalizado). */
    private String idSindicato;
    /** Taxa sindical diária cobrada do empregado (se sindicalizado). */
    private String taxaSindical;
    /** Banco para pagamento via conta bancária. */
    private String banco;
    /** Agência bancária para pagamento via conta bancária. */
    private String agencia;
    /** Número da conta corrente para pagamento via conta bancária. */
    private String contaCorrente;
    /** Data do último pagamento recebido pelo empregado. */
    private String dataUltimoPagamento;
    /** Descontos de taxa sindical/serviço que não couberam no salário líquido de um pagamento anterior. */
    private BigDecimal descontosPendentes;

    /** Cartões de ponto lançados para este empregado (aplicável a horistas). */
    private List<CartaoDePonto> listaCartoes = new ArrayList<>();
    /** Vendas lançadas para este empregado (aplicável a comissionados). */
    private List<Venda> listaVendas = new ArrayList<>();
    /** Taxas de serviço lançadas para este empregado (aplicável a sindicalizados). */
    private List<TaxaServico> listaTaxaServico = new ArrayList<>();

    /**
     * Cria um novo empregado com os dados cadastrais básicos.
     * Por padrão, o empregado é criado não sindicalizado e com
     * pagamento "em mãos".
     *
     * @param nome nome do empregado (não pode ser nulo/vazio)
     * @param endereco endereço do empregado (não pode ser nulo/vazio)
     * @param tipo tipo do empregado ("horista", "assalariado" ou "comissionado")
     * @param salario salário do empregado, já validado e formatado
     */
    public Empregado(String nome, String endereco, String tipo, String salario) {
        if(nome == null || nome.isEmpty()) throw new NomeNaoPodeSerNuloException();
        if(endereco == null || endereco.isEmpty()) throw new EnderecoNaoPodeSerNuloException();

        this.nome = nome;
        this.endereco = endereco;
        this.tipo = tipo;
        this.salario = salario;
        this.metodoPagamento = "emMaos";
        this.agenda = AgendaPagamento.padraoDoTipo(tipo);
    }

    /**
     * Calcula a data em que começa o período de pagamento que termina
     * em {@code dataPagamento}, de acordo com a agenda do empregado.
     *
     * @param dataPagamento data do pagamento
     * @return primeiro dia do período
     */
    public LocalDate inicioPeriodo(LocalDate dataPagamento) {
        return agenda.inicioPeriodo(dataPagamento);
    }

    /**
     * Calcula o pagamento bruto do empregado para a data informada,
     * de acordo com a regra do seu tipo e a agenda de pagamento dele.
     *
     * @param dataPagamento data do pagamento
     * @return o valor bruto a pagar
     */
    public abstract BigDecimal calcularPagamento(LocalDate dataPagamento);

    // ---- Getters e setters dos dados cadastrais ----

    /** @return o identificador único do empregado */
    public String getId() {
        return id;
    }
    /** @param id novo identificador único do empregado */
    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }
    public void setNome(String nome){ this.nome = nome; }

    public String getEndereco() {
        return endereco;
    }
    public void setEndereco(String endereco){ this.endereco = endereco; }

    public String getTipo() {
        return tipo;
    }
    public void setTipo(String tipo){ this.tipo = tipo; }

    public String getSalario() {
        return salario;
    }
    public void setSalario(String salario){ this.salario = salario; }

    /** @return a agenda de pagamento do empregado */
    public AgendaPagamento getAgenda(){ return agenda; }
    /** @param agenda nova agenda de pagamento do empregado */
    public void setAgenda(AgendaPagamento agenda){ this.agenda = agenda; }

    public String getMetodoPagamento(){ return metodoPagamento; }
    public void setMetodoPagamento(String metodoPagamento){ this.metodoPagamento = metodoPagamento; }

    public boolean isSindicalizado() { return sindicalizado; }
    public void setSindicalizado(boolean sindicalizado) { this.sindicalizado = sindicalizado; }

    public String getIdSindicato(){ return idSindicato; }
    public void setIdSindicato(String idSindicato){ this.idSindicato = idSindicato; }

    public String getTaxaSindical(){ return taxaSindical; }
    public void setTaxaSindical(String taxaSindical){ this.taxaSindical = taxaSindical; }

    public String getBanco(){ return banco; }
    public void setBanco(String banco){ this.banco = banco; }

    public String getAgencia(){ return agencia; }
    public void setAgencia(String agencia){ this.agencia = agencia; }

    public String getContaCorrente(){ return contaCorrente; }
    public void setContaCorrente(String contaCorrente){ this.contaCorrente = contaCorrente; }

    public String getDataUltimoPagamento(){ return dataUltimoPagamento; }
    public void setDataUltimoPagamento(String dataUltimoPagamento) { this.dataUltimoPagamento = dataUltimoPagamento; }

    public BigDecimal getDescontosPendentes(){ return descontosPendentes;}
    public void setDescontosPendentes(BigDecimal descontosPendentes){ this.descontosPendentes = descontosPendentes; }

    // ---- Cartões de ponto (horistas) ----

    /** @return a lista de cartões de ponto lançados para este empregado */
    public List<CartaoDePonto> getListaCartoes() {
        return Collections.unmodifiableList(listaCartoes);
    }

    /** Adiciona um novo cartão de ponto à lista deste empregado.
     * @param novoCartaoDePonto cartão de ponto a ser lançado */
    public void adicionarCartao(CartaoDePonto novoCartaoDePonto){
        listaCartoes.add(novoCartaoDePonto);
    }

    // ---- Vendas (comissionados) ----

    /** @return a lista de vendas lançadas para este empregado */
    public List<Venda> getListaVendas() {
        return Collections.unmodifiableList(listaVendas);
    }

    /** Adiciona uma nova venda à lista deste empregado.
     * @param novaVenda venda a ser lançada */
    public void adicionarVenda(Venda novaVenda){
        listaVendas.add(novaVenda);
    }

    // ---- Taxas de serviço (sindicalizados) ----

    /** @return a lista de taxas de serviço lançadas para este empregado */
    public List<TaxaServico> getListaTaxaServico() {
        return Collections.unmodifiableList(listaTaxaServico);
    }
    /** Adiciona uma nova taxa de serviço à lista deste empregado.
     * @param novaTaxaServico taxa de serviço a ser lançada */
    public void adicionarTaxaServico(TaxaServico novaTaxaServico){
        listaTaxaServico.add(novaTaxaServico);
    }

    /**
     * Copia para este empregado os cartões de ponto, vendas e taxas de
     * serviço de {@code outro} (em listas novas, sem compartilhá-las),
     * usado ao trocar o tipo de um empregado.
     *
     * @param outro empregado de onde os lançamentos serão copiados
     */
    public void copiarLancamentosDe(Empregado outro) {
        this.listaCartoes = new ArrayList<>(outro.listaCartoes);
        this.listaVendas = new ArrayList<>(outro.listaVendas);
        this.listaTaxaServico = new ArrayList<>(outro.listaTaxaServico);
    }
}