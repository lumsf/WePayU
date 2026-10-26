package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.Exception.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.time.temporal.ChronoUnit;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Camada de persistência e regras de negócio do sistema WePayU: mantém
 * a lista de empregados e as agendas de pagamento disponíveis em memória,
 * gera os identificadores, valida e registra lançamentos (cartões de
 * ponto, vendas, taxas de serviço), calcula a folha de pagamento e
 * controla o histórico de undo/redo.
 *
 * <p>Esta classe é usada pela {@link br.ufal.ic.p2.wepayu.Facade}, que
 * expõe uma API mais simples para o restante do sistema (e para os
 * testes de aceitação).</p>
 */
public class BancoDados {
    /** Lista de todos os empregados cadastrados no sistema. */
    private List<Empregado> listaEmpregados = new ArrayList<>();
    /** Contador usado para gerar o próximo identificador de empregado ("id1", "id2", ...). */
    private int contador = 0;
    /** Agendas de pagamento que os empregados podem escolher (as padrão mais as criadas). */
    private List<AgendaPagamento> agendasDisponiveis = new ArrayList<>();

    /** Pilha de estados anteriores, usada pelo comando {@link #undo()}. */
    private Stack<EstadoBancoDados> pilhaUndo = new Stack<>();
    /** Pilha de estados desfeitos, usada pelo comando {@link #redo()}. */
    private Stack<EstadoBancoDados> pilhaRedo = new Stack<>();

    /** Cria um banco de dados vazio, com apenas as três agendas de pagamento padrão disponíveis. */
    public BancoDados() {
        restaurarAgendasPadrao();
    }

    /** Deixa como agendas disponíveis apenas as três padrão do sistema. */
    private void restaurarAgendasPadrao() {
        agendasDisponiveis.clear();
        agendasDisponiveis.add(AgendaPagamento.criar("semanal 5"));
        agendasDisponiveis.add(AgendaPagamento.criar("mensal $"));
        agendasDisponiveis.add(AgendaPagamento.criar("semanal 2 5"));
    }

    /**
     * Cria uma nova agenda de pagamento e a disponibiliza para os empregados.
     *
     * @param descricao descrição da agenda, ex.: "semanal 2 5" ou "mensal 10"
     * @throws DescricaoDeAgendaInvalidaException se a descrição não seguir um formato válido
     * @throws AgendaDePagamentosJaExisteException se a agenda já existir
     */
    public void criarAgendaDePagamentos(String descricao) {
        AgendaPagamento nova = AgendaPagamento.criar(descricao);

        if (agendasDisponiveis.contains(nova)) throw new AgendaDePagamentosJaExisteException();

        agendasDisponiveis.add(nova);
    }

    /**
     * Empilha uma cópia (snapshot) do estado atual na pilha de undo e
     * limpa a pilha de redo. Deve ser chamado antes de qualquer
     * alteração de estado, para que ela possa ser desfeita depois.
     */
    private void salvarEstado() {

        EstadoBancoDados estado =
                new EstadoBancoDados(listaEmpregados, contador);

        pilhaUndo.push(estado);
        pilhaRedo.clear();
    }

    /**
     * Adiciona um novo empregado ao sistema, gerando e atribuindo a
     * ele um novo identificador único.
     *
     * @param novoEmpregado empregado já validado, a ser cadastrado
     */
    public void adicionarEmpregado(Empregado novoEmpregado) {
        salvarEstado();

        contador++;

        String id = "id" + contador;
        novoEmpregado.setId(id);

        listaEmpregados.add(novoEmpregado);
    }

    /**
     * Remove o empregado com o identificador informado.
     *
     * @param id identificador do empregado a ser removido
     * @throws IdentificacaoDoEmpregadoNaoPodeSerNulaException se o id for nulo/vazio
     * @throws EmpregadoNaoExisteException se não existir empregado com esse id
     */
    public void remover(String id){
        if(id == null || id.isEmpty()) throw new IdentificacaoDoEmpregadoNaoPodeSerNulaException();
        for (int j = 0; j < listaEmpregados.size(); j++) {
            if (listaEmpregados.get(j).getId().equals(id)) {
                salvarEstado();
                listaEmpregados.remove(j);
                return;
            }
        }
        throw new EmpregadoNaoExisteException();
    }

    /**
     * Busca um empregado pelo seu identificador.
     *
     * @param id identificador do empregado
     * @return o empregado encontrado
     * @throws EmpregadoNaoExisteException se não existir empregado com esse id
     */
    public Empregado buscar(String id) {
        for (Empregado empregado : listaEmpregados) {
            if (empregado.getId().equals(id)) {
                return empregado;
            }
        }
        throw new EmpregadoNaoExisteException();
    }

    /**
     * Busca o identificador do empregado com o nome informado,
     * considerando a ordem de cadastro (o {@code indice}-ésimo
     * empregado, a partir de 1, com esse nome).
     *
     * @param nome nome do empregado procurado
     * @param indice posição (1-based) entre os empregados com esse nome
     * @return o identificador do empregado encontrado
     * @throws NaoHaEmpregadoComEsseNomeException se não houver empregado
     *         com esse nome nessa posição
     */
    public String buscarPorNome(String nome, int indice){
        int contador = 0;
        for(Empregado empregado : listaEmpregados){
            if(empregado.getNome().equals(nome)){
                contador++;

                if(contador == indice){
                    return empregado.getId();
                }
            }
        }
        throw new NaoHaEmpregadoComEsseNomeException();
    }

    /**
     * Altera um atributo simples de um empregado (nome, endereço,
     * salário, comissão, sindicalização, agenda de pagamento ou método
     * de pagamento sem dados adicionais).
     *
     * @param id identificador do empregado
     * @param atributo nome do atributo a alterar
     * @param valor novo valor do atributo
     * @throws AgendaDePagamentoNaoDisponivelException se {@code atributo} for
     *         "agendaPagamento" e a agenda informada não estiver disponível
     */
    public void alteraEmpregado(String id, String atributo, String valor) {
        if(id == null || id.isEmpty()) throw new IdentificacaoDoEmpregadoNaoPodeSerNulaException();

        Empregado e = buscar(id);

        if(atributo.equals("nome")){
            if(valor == null || valor.isEmpty()) throw new NomeNaoPodeSerNuloException();
            salvarEstado();
            e.setNome(valor);
        }
        else if(atributo.equals("endereco")){
            if(valor == null || valor.isEmpty()) throw new EnderecoNaoPodeSerNuloException();
            salvarEstado();
            e.setEndereco(valor);
        }
        else if(atributo.equals("salario")){
            String salarioFormatado = Validador.formatar(Validador.validarSalario(valor));
            salvarEstado();
            e.setSalario(salarioFormatado);
        }
        else if(atributo.equals("comissao")){
            if(!e.getTipo().equals("comissionado")) throw new EmpregadoNaoEhComissionadoException();
            String comissaoFormatada = Validador.formatar(Validador.validarComissao(valor));
            salvarEstado();
            ((Comissionado) e).setComissao(comissaoFormatada);
        }
        else if(atributo.equals("sindicalizado")){
            if (!valor.equals("true") && !valor.equals("false")) throw new ValorDeveSerTrueOuFalseException();

            salvarEstado();
            e.setSindicalizado(Boolean.parseBoolean(valor));

            if (valor.equals("false")) {
                e.setIdSindicato(null);
                e.setTaxaSindical(null);
                return;
            }
        }
        else if(atributo.equals("agendaPagamento")){
            AgendaPagamento escolhida = null;

            for (AgendaPagamento agenda : agendasDisponiveis) {
                if (agenda.getDescricao().equals(valor)) escolhida = agenda;
            }
            if (escolhida == null) throw new AgendaDePagamentoNaoDisponivelException();

            salvarEstado();
            e.setAgenda(escolhida);
        }
        else if(atributo.equals("metodoPagamento")){
            if(!valor.equals("emMaos") && !valor.equals("banco") && !valor.equals("correios")){
                throw new MetodoDePagamentoInvalidoException();
            }
            if(valor.equals("banco")) throw new BancoNaoPodeSerNuloException();

            salvarEstado();
            e.setMetodoPagamento(valor);

            if(valor.equals("emMaos") || valor.equals("correios")){
                e.setBanco(null);
                e.setAgencia(null);
                e.setContaCorrente(null);
            }
        }
        else{
            throw new AtributoNaoExisteException();
        }
    }

    /**
     * Altera a sindicalização de um empregado, definindo (ou removendo)
     * o sindicato e a taxa sindical diária associados.
     *
     * @param id identificador do empregado
     * @param atributo deve ser "sindicalizado"
     * @param valor "true" para sindicalizar, "false" para remover a sindicalização
     * @param idSindicato identificação do sindicato (obrigatória se valor="true")
     * @param taxaSindical taxa sindical diária (obrigatória se valor="true")
     */
    public void alteraEmpregado(String id, String atributo, String valor, String idSindicato,
                                String taxaSindical){
        Empregado empregado = buscar(id);

        if (!atributo.equals("sindicalizado")) throw new AtributoNaoExisteException();
        if (!valor.equals("true") && !valor.equals("false")) throw new ValorDeveSerTrueOuFalseException();

        if (valor.equals("false")) {
            salvarEstado();
            empregado.setSindicalizado(false);
            empregado.setIdSindicato(null);
            empregado.setTaxaSindical(null);
            return;
        }

        if (idSindicato == null || idSindicato.isEmpty()){
            throw new IdentificacaoDoSindicatoNaoPodeSerNulaException();
        }
        if (taxaSindical == null || taxaSindical.isEmpty()) throw new TaxaSindicalNulaException();

        String taxaFormatada = Validador.formatar(Validador.validarTaxaSindical(taxaSindical));

        for (int j = 0; j < listaEmpregados.size(); j++) {
            Empregado outro = listaEmpregados.get(j);

            if (!outro.getId().equals(id) && outro.isSindicalizado()
                    && outro.getIdSindicato() != null
                    && outro.getIdSindicato().equals(idSindicato))
                throw new HaOutroEmpregadoComEstaIdentificacaoDeSindicatoException();
        }

        salvarEstado();

        empregado.setSindicalizado(true);
        empregado.setIdSindicato(idSindicato);
        empregado.setTaxaSindical(taxaFormatada);
    }

    /**
     * Altera o método de pagamento de um empregado para pagamento em
     * conta bancária, definindo banco, agência e conta corrente.
     *
     * @param id identificador do empregado
     * @param atributo deve ser "metodoPagamento"
     * @param valor "banco" ou "emMaos"
     * @param banco nome do banco (obrigatório se valor="banco")
     * @param agencia agência bancária (obrigatório se valor="banco")
     * @param contaCorrente número da conta corrente (obrigatório se valor="banco")
     */
    public void alteraEmpregado(String id, String atributo, String valor, String banco, String agencia,
                                String contaCorrente){
        Empregado empregado = buscar(id);

        if(!atributo.equals("metodoPagamento")) throw new AtributoNaoExisteException();
        if(!valor.equals("banco") && !valor.equals("emMaos")) throw new MetodoDePagamentoInvalidoException();

        if(valor.equals("emMaos")){
            salvarEstado();
            empregado.setMetodoPagamento("emMaos");
            empregado.setBanco(null);
            empregado.setAgencia(null);
            empregado.setContaCorrente(null);
            return;
        }

        if(banco == null || banco.isEmpty()) throw new BancoNaoPodeSerNuloException();
        if(agencia == null || agencia.isEmpty()) throw new AgenciaNaoPodeSerNuloException();
        if(contaCorrente == null || contaCorrente.isEmpty()) throw new ContaCorrenteNaoPodeSerNuloException();

        salvarEstado();
        empregado.setMetodoPagamento("banco");
        empregado.setBanco(banco);
        empregado.setAgencia(agencia);
        empregado.setContaCorrente(contaCorrente);
    }

    /**
     * Muda o tipo de um empregado para "horista" ou "assalariado",
     * criando um novo objeto do tipo correspondente e copiando para
     * ele os dados cadastrais comuns do empregado antigo. A agenda de
     * pagamento volta a ser a padrão do novo tipo.
     *
     * @param id identificador do empregado
     * @param novoTipo "horista" ou "assalariado"
     * @param salario novo salário (se nulo, reaproveita o salário atual)
     */
    public void mudaTipoEmpregado(String id, String novoTipo, String salario){
        Empregado antigo = buscar(id);

        if(!novoTipo.equals("horista") && !novoTipo.equals("assalariado")) throw new TipoInvalidoException();

        String salarioFormatado;

        if(salario == null){
            salarioFormatado = Validador.formatar(Validador.validarSalario(antigo.getSalario()));
        }
        else{
            salarioFormatado = Validador.formatar(Validador.validarSalario(salario));
        }

        Empregado novo;

        if(novoTipo.equals("horista")){
            novo = new Horista(antigo.getNome(), antigo.getEndereco(), novoTipo, salarioFormatado);
        }
        else if(novoTipo.equals("assalariado")){
            novo = new Assalariado(antigo.getNome(), antigo.getEndereco(), novoTipo, salarioFormatado);
        }
        else{
            throw new TipoInvalidoException();
        }

        copiarDadosComuns(antigo, novo);

        salvarEstado();

        int idx = listaEmpregados.indexOf(antigo);
        listaEmpregados.set(idx, novo);
    }

    /**
     * Muda o tipo de um empregado para "comissionado", criando um
     * novo {@link Comissionado} e copiando para ele os dados
     * cadastrais comuns do empregado antigo. A agenda de pagamento
     * volta a ser a padrão dos comissionados.
     *
     * @param id identificador do empregado
     * @param novoTipo deve ser "comissionado"
     * @param salario ignorado (mantém-se o salário atual do empregado)
     * @param comissao taxa de comissão do novo empregado comissionado
     */
    public void mudaTipoEmpregado(String id, String novoTipo, String salario, String comissao){
        Empregado antigo = buscar(id);

        if(!novoTipo.equals("comissionado")) throw new TipoInvalidoException();

        String salarioFormatado = Validador.formatar(Validador.validarSalario(antigo.getSalario()));

        String comissaoFormatada = Validador.formatar(Validador.validarComissao(comissao));

        Comissionado novo = new Comissionado(antigo.getNome(), antigo.getEndereco(), novoTipo,
                salarioFormatado, comissaoFormatada);

        copiarDadosComuns(antigo, novo);

        salvarEstado();

        int idx = listaEmpregados.indexOf(antigo);
        listaEmpregados.set(idx, novo);
    }

    /**
     * Copia para {@code novo} os dados comuns de {@code antigo} (id,
     * método e dados de pagamento, sindicalização, data do último
     * pagamento, descontos pendentes e lançamentos), usado ao trocar o
     * tipo de um empregado. A agenda de pagamento não é copiada: o novo
     * empregado fica com a agenda padrão do seu tipo.
     *
     * @param antigo empregado original
     * @param novo empregado recém-criado, que receberá os dados
     */
    private void copiarDadosComuns(Empregado antigo, Empregado novo){
        novo.setId(antigo.getId());
        novo.setMetodoPagamento(antigo.getMetodoPagamento());
        novo.setBanco(antigo.getBanco());
        novo.setAgencia(antigo.getAgencia());
        novo.setContaCorrente(antigo.getContaCorrente());
        novo.setSindicalizado(antigo.isSindicalizado());
        novo.setIdSindicato(antigo.getIdSindicato());
        novo.setTaxaSindical(antigo.getTaxaSindical());
        novo.setDataUltimoPagamento(antigo.getDataUltimoPagamento());
        novo.copiarLancamentosDe(antigo);
        novo.setDescontosPendentes(antigo.getDescontosPendentes());
    }

    /**
     * Remove todos os empregados cadastrados, reinicia o contador de
     * identificadores e deixa disponíveis apenas as agendas de pagamento
     * padrão, usado para reiniciar o sistema entre execuções de testes.
     */
    public void zerar() {
        salvarEstado();
        listaEmpregados.clear();
        contador = 0;
        restaurarAgendasPadrao();
    }

    /**
     * Lança um cartão de ponto para um empregado horista.
     *
     * @param id identificador do empregado
     * @param data data do lançamento, no formato "dd/mm/aaaa"
     * @param horas quantidade de horas trabalhadas (deve ser positiva)
     * @throws EmpregadoNaoEhHoristaException se o empregado não for horista
     * @throws DataInvalidaException se a data for inválida
     * @throws HorasDevemSerPositivasException se as horas não forem positivas
     */
    public void lancaCartao(String id, String data, String horas){
        if(id == null || id.isEmpty()) throw new IdentificacaoDoEmpregadoNaoPodeSerNulaException();

        Empregado empregado = buscar(id);

        if(!(empregado instanceof Horista)) throw new EmpregadoNaoEhHoristaException();

        Utilitarios.validarData(data);

        BigDecimal quantidadeHoras;

        try{
            quantidadeHoras = new BigDecimal(horas.replace(",", "."));
        }
        catch (Exception e) {
            throw new HorasDevemSerPositivasException();
        }

        if(quantidadeHoras.compareTo(BigDecimal.ZERO) <= 0) throw new HorasDevemSerPositivasException();

        salvarEstado();

        CartaoDePonto cartao = new CartaoDePonto(data, horas);
        empregado.adicionarCartao(cartao);
    }

    /**
     * Lança uma venda para um empregado comissionado.
     *
     * @param id identificador do empregado
     * @param data data da venda, no formato "dd/mm/aaaa"
     * @param valor valor da venda (deve ser positivo)
     * @throws EmpregadoNaoEhComissionadoException se o empregado não for comissionado
     * @throws DataInvalidaException se a data for inválida
     * @throws ValorDeveSerPositivoException se o valor não for positivo
     */
    public void lancaVenda(String id, String data, String valor){
        if(id == null || id.isEmpty()) throw new IdentificacaoDoEmpregadoNaoPodeSerNulaException();

        Empregado empregado = buscar(id);

        if(!(empregado instanceof Comissionado)) throw new EmpregadoNaoEhComissionadoException();

        Utilitarios.validarData(data);

        BigDecimal valorVenda;

        try{
            valorVenda = new BigDecimal(valor.replace(",", "."));
        }
        catch (Exception e) {
            throw new ValorDeveSerPositivoException();
        }

        if(valorVenda.compareTo(BigDecimal.ZERO) <= 0) throw new ValorDeveSerPositivoException();

        salvarEstado();

        Venda venda = new Venda(data, valor);
        empregado.adicionarVenda(venda);
    }

    /**
     * Soma o valor das vendas de um empregado comissionado dentro de
     * um intervalo de datas (data final exclusiva).
     *
     * @param id identificador do empregado
     * @param dataInicial início do intervalo (inclusive), "dd/mm/aaaa"
     * @param dataFinal fim do intervalo (exclusivo), "dd/mm/aaaa"
     * @return o total vendido no período, formatado com vírgula
     * @throws EmpregadoNaoEhComissionadoException se o empregado não for comissionado
     * @throws DataInicialInvalidaException se a data inicial for inválida
     * @throws DataFinalInvalidaException se a data final for inválida
     * @throws DataInicialNaoPodeSerPosteriorAaDataFinalException se início &gt; fim
     */
    public String getVendasRealizadas(String id, String dataInicial, String dataFinal){
        if(id == null || id.isEmpty()) throw new IdentificacaoDoEmpregadoNaoPodeSerNulaException();

        Empregado empregado = buscar(id);

        if(!(empregado instanceof Comissionado)) throw new EmpregadoNaoEhComissionadoException();

        LocalDate inicio;
        try{
            inicio = Utilitarios.validarData(dataInicial);
        }
        catch (DataInvalidaException e){
            throw new DataInicialInvalidaException();
        }

        LocalDate fim;
        try{
            fim = Utilitarios.validarData(dataFinal);
        }
        catch (DataInvalidaException e){
            throw new DataFinalInvalidaException();
        }

        if(inicio.isAfter(fim)) throw new DataInicialNaoPodeSerPosteriorAaDataFinalException();

        BigDecimal total = BigDecimal.ZERO;

        for(int j = 0; j < empregado.getListaVendas().size(); j++){
            Venda venda = empregado.getListaVendas().get(j);

            LocalDate dataVenda = Utilitarios.validarData(venda.getData());

            if(!dataVenda.isBefore(inicio) && dataVenda.isBefore(fim)){
                BigDecimal valor = new BigDecimal(venda.getValor().replace(",", "."));

                total = total.add(valor);
            }
        }
        return Utilitarios.formatarDinheiro(total);
    }

    /**
     * Lança uma taxa de serviço para o empregado sindicalizado cuja
     * identificação de sindicato seja igual a {@code membro}.
     *
     * @param membro identificação do sindicato do empregado alvo
     * @param data data do lançamento, no formato "dd/mm/aaaa"
     * @param valor valor da taxa (deve ser positivo)
     * @throws MembroNaoExisteException se nenhum empregado tiver essa identificação de sindicato
     * @throws DataInvalidaException se a data for inválida
     * @throws ValorDeveSerPositivoException se o valor não for positivo
     */
    public void lancaTaxaServico(String membro, String data, String valor) {
        if(membro == null || membro.isEmpty()) throw new IdentificacaoDoMembroNaoPodeSerNulaException();

        Empregado empregado = null;

        for(int j = 0; j < listaEmpregados.size(); j++){
            Empregado atual = listaEmpregados.get(j);

            if(atual.getIdSindicato() != null && atual.getIdSindicato().equals(membro)){
                empregado = atual;
                break;
            }
        }

        if(empregado == null) throw new MembroNaoExisteException();

        Utilitarios.validarData(data);

        BigDecimal valorTaxa;

        try{
            valorTaxa = new BigDecimal(valor.replace(",", "."));
        }
        catch (Exception e){
            throw new ValorDeveSerPositivoException();
        }

        if(valorTaxa.compareTo(BigDecimal.ZERO) <= 0) throw new ValorDeveSerPositivoException();

        salvarEstado();

        TaxaServico taxaServico = new TaxaServico(data, valor);

        empregado.adicionarTaxaServico(taxaServico);
    }

    /**
     * Soma o valor das taxas de serviço de um empregado sindicalizado
     * dentro de um intervalo de datas (data final exclusiva).
     *
     * @param id identificador do empregado
     * @param dataInicial início do intervalo (inclusive), "dd/mm/aaaa"
     * @param dataFinal fim do intervalo (exclusivo), "dd/mm/aaaa"
     * @return o total de taxas no período, formatado com vírgula
     * @throws EmpregadoNaoEhSindicalizadoException se o empregado não for sindicalizado
     * @throws DataInicialInvalidaException se a data inicial for inválida
     * @throws DataFinalInvalidaException se a data final for inválida
     * @throws DataInicialNaoPodeSerPosteriorAaDataFinalException se início &gt; fim
     */
    public String getTaxasServico(String id, String dataInicial, String dataFinal){
        if(id == null || id.isEmpty()) throw new IdentificacaoDoEmpregadoNaoPodeSerNulaException();

        Empregado empregado = buscar(id);

        if(!empregado.isSindicalizado()) throw new EmpregadoNaoEhSindicalizadoException();

        LocalDate inicio;
        try{
            inicio = Utilitarios.validarData(dataInicial);
        }
        catch (DataInvalidaException e){
            throw new DataInicialInvalidaException();
        }

        LocalDate fim;
        try{
            fim = Utilitarios.validarData(dataFinal);
        }
        catch (DataInvalidaException e){
            throw new DataFinalInvalidaException();
        }

        if(inicio.isAfter(fim)) throw new DataInicialNaoPodeSerPosteriorAaDataFinalException();

        BigDecimal total = BigDecimal.ZERO;

        for(int j = 0; j < empregado.getListaTaxaServico().size(); j++){
            TaxaServico taxaServico = empregado.getListaTaxaServico().get(j);

            LocalDate dataTaxaServico = Utilitarios.validarData(taxaServico.getData());

            if(!dataTaxaServico.isBefore(inicio) && dataTaxaServico.isBefore(fim)){
                BigDecimal valorTaxa = new BigDecimal(taxaServico.getValor().replace(",", "."));

                total = total.add(valorTaxa);
            }
        }
        return Utilitarios.formatarDinheiro(total);
    }

    /**
     * Soma as horas normais (até 8h por dia) trabalhadas por um
     * empregado horista dentro de um intervalo de datas (data final
     * exclusiva).
     *
     * @param id identificador do empregado
     * @param dataInicial início do intervalo (inclusive), "dd/mm/aaaa"
     * @param dataFinal fim do intervalo (exclusivo), "dd/mm/aaaa"
     * @return total de horas normais no período, formatado com vírgula
     * @throws EmpregadoNaoEhHoristaException se o empregado não for horista
     * @throws DataInicialInvalidaException se a data inicial for inválida
     * @throws DataFinalInvalidaException se a data final for inválida
     * @throws DataInicialNaoPodeSerPosteriorAaDataFinalException se início &gt; fim
     */
    public String getHorasNormaisTrabalhadas(String id, String dataInicial, String dataFinal){
        if(id == null || id.isEmpty()) throw new IdentificacaoDoEmpregadoNaoPodeSerNulaException();

        Empregado empregado = buscar(id);

        if(!(empregado instanceof Horista)) throw new EmpregadoNaoEhHoristaException();

        LocalDate inicio;
        try{
            inicio = Utilitarios.validarData(dataInicial);
        }
        catch (DataInvalidaException e){
            throw new DataInicialInvalidaException();
        }

        LocalDate fim;
        try{
            fim = Utilitarios.validarData(dataFinal);
        }
        catch (DataInvalidaException e){
            throw new DataFinalInvalidaException();
        }

        if(inicio.isAfter(fim)) throw new DataInicialNaoPodeSerPosteriorAaDataFinalException();

        BigDecimal total = BigDecimal.ZERO;

        for(int j = 0; j < empregado.getListaCartoes().size(); j++){
            CartaoDePonto cartaoDePonto = empregado.getListaCartoes().get(j);

            LocalDate dataCartao = Utilitarios.validarData(cartaoDePonto.getData());

            if(!dataCartao.isBefore(inicio) && dataCartao.isBefore(fim)){
                BigDecimal horas = new BigDecimal(cartaoDePonto.getHoras().replace(",", "."));

                if(horas.compareTo(new BigDecimal("8")) <= 0) total = total.add(horas);
                else{
                    total = total.add(new BigDecimal("8"));
                }
            }
        }
        return Utilitarios.formatarHoras(total);
    }

    /**
     * Soma as horas extras (acima de 8h por dia) trabalhadas por um
     * empregado horista dentro de um intervalo de datas (data final
     * exclusiva).
     *
     * @param id identificador do empregado
     * @param dataInicial início do intervalo (inclusive), "dd/mm/aaaa"
     * @param dataFinal fim do intervalo (exclusivo), "dd/mm/aaaa"
     * @return total de horas extras no período, formatado com vírgula
     * @throws EmpregadoNaoEhHoristaException se o empregado não for horista
     * @throws DataInicialInvalidaException se a data inicial for inválida
     * @throws DataFinalInvalidaException se a data final for inválida
     * @throws DataInicialNaoPodeSerPosteriorAaDataFinalException se início &gt; fim
     */
    public String getHorasExtrasTrabalhadas(String id, String dataInicial, String dataFinal){
        if(id == null || id.isEmpty()) throw new IdentificacaoDoEmpregadoNaoPodeSerNulaException();

        Empregado empregado = buscar(id);

        if(!(empregado instanceof Horista)) throw new EmpregadoNaoEhHoristaException();

        LocalDate inicio;
        try{
            inicio = Utilitarios.validarData(dataInicial);
        }
        catch (DataInvalidaException e){
            throw new DataInicialInvalidaException();
        }

        LocalDate fim;
        try{
            fim = Utilitarios.validarData(dataFinal);
        }
        catch (DataInvalidaException e){
            throw new DataFinalInvalidaException();
        }

        if(inicio.isAfter(fim)) throw new DataInicialNaoPodeSerPosteriorAaDataFinalException();

        BigDecimal total = BigDecimal.ZERO;

        for(int j = 0; j < empregado.getListaCartoes().size(); j++){
            CartaoDePonto cartaoDePonto = empregado.getListaCartoes().get(j);

            LocalDate dataCartao = Utilitarios.validarData(cartaoDePonto.getData());

            if(!dataCartao.isBefore(inicio) && dataCartao.isBefore(fim)){
                BigDecimal horas = new BigDecimal(cartaoDePonto.getHoras().replace(",", "."));

                if(horas.compareTo(new BigDecimal("8")) > 0) {
                    BigDecimal horasExtras = horas.subtract(new BigDecimal("8"));
                    total = total.add(horasExtras);
                }
            }
        }
        return Utilitarios.formatarHoras(total);
    }

    /**
     * Verifica se um empregado deve ser pago em uma determinada data,
     * de acordo com a agenda de pagamento dele. Horistas só são pagos
     * se tiverem algum cartão de ponto no período.
     *
     * @param empregado empregado a verificar
     * @param data data candidata a pagamento, "dd/mm/aaaa"
     * @return {@code true} se o empregado deve ser pago nessa data
     */
    public boolean deveSerPago(Empregado empregado, String data) {
        LocalDate dataPagamento = Utilitarios.validarData(data);

        if (!empregado.getAgenda().ehDiaDePagamento(dataPagamento)) {
            return false;
        }

        if (empregado instanceof Horista) {
            LocalDate inicio = empregado.inicioPeriodo(dataPagamento);

            for(int j = 0; j < empregado.getListaCartoes().size(); j++){
                LocalDate dataCartao =
                        Utilitarios.validarData(empregado.getListaCartoes().get(j).getData());

                if(!dataCartao.isBefore(inicio) && !dataCartao.isAfter(dataPagamento)){
                    return true;
                }
            }
            return false;
        }

        return true;
    }

    /**
     * Calcula o valor total bruto da folha de pagamento de uma data:
     * a soma dos pagamentos de todos os empregados que devem ser
     * pagos nessa data, de acordo com a agenda de cada um.
     *
     * @param data data de referência da folha, "dd/mm/aaaa"
     * @return o total da folha, formatado com vírgula
     */
    public String totalFolha(String data) {
        LocalDate dataPagamento = Utilitarios.validarData(data);

        BigDecimal totalFolha = BigDecimal.ZERO;

        for(int j = 0; j < listaEmpregados.size(); j++){
            Empregado empregado = listaEmpregados.get(j);

            if(deveSerPago(empregado, data)){
                totalFolha = totalFolha.add(empregado.calcularPagamento(dataPagamento));
            }
        }

        return Utilitarios.formatarDinheiro(totalFolha);
    }

    /**
     * Calcula o total de descontos de um empregado para o período de
     * pagamento da sua agenda: taxa sindical diária (se sindicalizado)
     * mais as taxas de serviço lançadas no período.
     *
     * @param empregado empregado a calcular os descontos
     * @param dataPagamento data do pagamento
     * @return o total de descontos no período
     */
    private BigDecimal calcularDescontos(Empregado empregado, LocalDate dataPagamento){
        BigDecimal descontos = BigDecimal.ZERO;
        LocalDate inicio = empregado.inicioPeriodo(dataPagamento);

        if(empregado.isSindicalizado() && empregado.getTaxaSindical() != null){

            long dias = ChronoUnit.DAYS.between(inicio, dataPagamento) + 1;

            BigDecimal taxa = new BigDecimal(empregado.getTaxaSindical().replace(",", "."));

            descontos = descontos.add(taxa.multiply(BigDecimal.valueOf(dias)));
        }

        for(int j = 0; j < empregado.getListaTaxaServico().size(); j++){
            TaxaServico taxaServico = empregado.getListaTaxaServico().get(j);

            LocalDate dataTaxa = Utilitarios.validarData(taxaServico.getData());

            if(!dataTaxa.isBefore(inicio) && dataTaxa.isBefore(dataPagamento)){
                BigDecimal valor = new BigDecimal(taxaServico.getValor().replace(",", "."));

                descontos = descontos.add(valor);
            }
        }

        return descontos;
    }

    /**
     * Formata o método de pagamento de um empregado para exibição no
     * relatório da folha de pagamento.
     *
     * @param empregado empregado cujo método de pagamento será formatado
     * @return texto descritivo do método de pagamento
     */
    private String formatarMetodoPagamento(Empregado empregado){
        if(empregado.getMetodoPagamento().equals("emMaos")){
            return "Em maos";
        }

        if(empregado.getMetodoPagamento().equals("correios")){
            return "Correios, " + empregado.getEndereco();
        }

        if(empregado.getMetodoPagamento().equals("banco")){
            return empregado.getBanco() + ", Ag. " + empregado.getAgencia() + " CC " + empregado.getContaCorrente();
        }

        return "";
    }

    /**
     * Gera o relatório da folha de pagamento de uma data em um
     * arquivo de texto, com uma seção para cada tipo de empregado
     * (horistas, assalariados e comissionados), listados em ordem
     * alfabética, seguida do total geral da folha. Só entram os
     * empregados cuja agenda de pagamento cai nessa data.
     *
     * @param data data de referência da folha, "dd/mm/aaaa"
     * @param saida caminho do arquivo de saída a ser gerado
     * @throws IOException se não for possível escrever o arquivo de saída
     */
    public void rodaFolha(String data, String saida) throws IOException  {
        LocalDate dataPagamento = Utilitarios.validarData(data);

        salvarEstado();

        List<Empregado> ordenados = new ArrayList<>(listaEmpregados);
        ordenados.sort((a, b) -> a.getNome().compareTo(b.getNome()));

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(saida))) {
            writer.write("FOLHA DE PAGAMENTO DO DIA " + dataPagamento);
            writer.newLine();

            writer.write("====================================");
            writer.newLine();
            writer.newLine();

            writer.write("===============================================================================================================================");
            writer.newLine();
            writer.write("===================== HORISTAS ================================================================================================");
            writer.newLine();
            writer.write("===============================================================================================================================");
            writer.newLine();

            writer.write("Nome                                 Horas Extra Salario Bruto Descontos Salario Liquido Metodo");
            writer.newLine();

            writer.write("==================================== ===== ===== ============= ========= =============== ======================================");
            writer.newLine();

            BigDecimal totalHorasNormais = BigDecimal.ZERO;
            BigDecimal totalHorasExtras = BigDecimal.ZERO;
            BigDecimal totalBrutoHoristas = BigDecimal.ZERO;
            BigDecimal totalDescontosHoristas = BigDecimal.ZERO;
            BigDecimal totalLiquidoHoristas = BigDecimal.ZERO;

            for (int j = 0; j < ordenados.size(); j++) {
                Empregado empregado = ordenados.get(j);

                if (empregado instanceof Horista && empregado.getAgenda().ehDiaDePagamento(dataPagamento)) {
                    LocalDate inicio = empregado.inicioPeriodo(dataPagamento);

                    Horista horista = (Horista) empregado;

                    BigDecimal horasNormais = horista.horasNormaisNoPeriodo(dataPagamento);
                    BigDecimal horasExtras  = horista.horasExtrasNoPeriodo(dataPagamento);
                    BigDecimal bruto = horista.calcularPagamento(dataPagamento);

                    BigDecimal taxasServicoPeriodo = BigDecimal.ZERO;
                    for (int k = 0; k < empregado.getListaTaxaServico().size(); k++) {
                        TaxaServico ts = empregado.getListaTaxaServico().get(k);
                        LocalDate dataTaxa = Utilitarios.validarData(ts.getData());
                        if (!dataTaxa.isBefore(inicio) && dataTaxa.isBefore(dataPagamento)) {
                            taxasServicoPeriodo = taxasServicoPeriodo.add(new BigDecimal(ts.getValor()
                                    .replace(",", ".")));
                        }
                    }

                    BigDecimal devidoSemana = BigDecimal.ZERO;
                    if (empregado.isSindicalizado() && empregado.getTaxaSindical() != null) {
                        BigDecimal taxaDiaria = new BigDecimal(empregado.getTaxaSindical().replace(","
                                , "."));
                        long diasNoPeriodo = ChronoUnit.DAYS.between(inicio, dataPagamento) + 1;
                        devidoSemana = taxaDiaria.multiply(BigDecimal.valueOf(diasNoPeriodo));
                    }

                    BigDecimal dividaAnterior = empregado.getDescontosPendentes() == null ? BigDecimal.ZERO :
                            empregado.getDescontosPendentes();
                    BigDecimal totalDevido = devidoSemana.add(dividaAnterior).add(taxasServicoPeriodo);

                    BigDecimal descontos;
                    BigDecimal liquido;

                    if (bruto.compareTo(totalDevido) >= 0) {
                        descontos = totalDevido;
                        liquido = bruto.subtract(descontos);
                        empregado.setDescontosPendentes(BigDecimal.ZERO);
                    } else {
                        descontos = bruto;
                        liquido = BigDecimal.ZERO;
                        empregado.setDescontosPendentes(totalDevido.subtract(bruto));
                    }

                    totalHorasNormais = totalHorasNormais.add(horasNormais);
                    totalHorasExtras = totalHorasExtras.add(horasExtras);
                    totalBrutoHoristas = totalBrutoHoristas.add(bruto);
                    totalDescontosHoristas = totalDescontosHoristas.add(descontos);
                    totalLiquidoHoristas = totalLiquidoHoristas.add(liquido);

                    String metodo = formatarMetodoPagamento(empregado);

                    writer.write(String.format(Locale.US, "%-36s %5s %5s %13s %9s %15s %-38s",
                            empregado.getNome(), Utilitarios.formatarHoras(horasNormais)
                            , Utilitarios.formatarHoras(horasExtras),
                            Utilitarios.formatarDinheiro(bruto), Utilitarios.formatarDinheiro(descontos), Utilitarios.formatarDinheiro(liquido),
                            metodo));
                    writer.newLine();
                }
            }

            writer.newLine();

            writer.write(String.format(Locale.US, "TOTAL HORISTAS %27s %5s %13s %9s %15s", Utilitarios.formatarHoras(totalHorasNormais)
                    , Utilitarios.formatarHoras(totalHorasExtras), Utilitarios.formatarDinheiro(totalBrutoHoristas), Utilitarios.formatarDinheiro(totalDescontosHoristas), Utilitarios.formatarDinheiro(totalLiquidoHoristas)));
            writer.newLine();
            writer.newLine();

            writer.write("===============================================================================================================================");
            writer.newLine();
            writer.write("===================== ASSALARIADOS ============================================================================================");
            writer.newLine();
            writer.write("===============================================================================================================================");
            writer.newLine();

            writer.write("Nome                                             Salario Bruto Descontos Salario Liquido Metodo");
            writer.newLine();

            writer.write("================================================ ============= ========= =============== ======================================");
            writer.newLine();

            BigDecimal totalBrutoAssalariados = BigDecimal.ZERO;
            BigDecimal totalDescontosAssalariados = BigDecimal.ZERO;
            BigDecimal totalLiquidoAssalariados = BigDecimal.ZERO;

            for(int j = 0; j < ordenados.size(); j++){
                Empregado empregado = ordenados.get(j);

                if(empregado instanceof Assalariado && deveSerPago(empregado, data)){
                    BigDecimal bruto = empregado.calcularPagamento(dataPagamento);
                    BigDecimal descontos = calcularDescontos(empregado, dataPagamento);
                    BigDecimal liquido = bruto.subtract(descontos);

                    totalBrutoAssalariados = totalBrutoAssalariados.add(bruto);
                    totalDescontosAssalariados = totalDescontosAssalariados.add(descontos);
                    totalLiquidoAssalariados = totalLiquidoAssalariados.add(liquido);

                    writer.write(String.format(Locale.US, "%-48s %13s %9s %15s %-38s", empregado.getNome(),
                            Utilitarios.formatarDinheiro(bruto), Utilitarios.formatarDinheiro(descontos), Utilitarios.formatarDinheiro(liquido), formatarMetodoPagamento(empregado)));

                    writer.newLine();
                }
            }

            writer.newLine();

            writer.write(String.format(Locale.US, "TOTAL ASSALARIADOS %43s %9s %15s",
                    Utilitarios.formatarDinheiro(totalBrutoAssalariados), Utilitarios.formatarDinheiro(totalDescontosAssalariados),
                    Utilitarios.formatarDinheiro(totalLiquidoAssalariados)));            writer.newLine();
            writer.newLine();

            writer.write("===============================================================================================================================");
            writer.newLine();
            writer.write("===================== COMISSIONADOS ===========================================================================================");
            writer.newLine();
            writer.write("===============================================================================================================================");
            writer.newLine();

            writer.write("Nome                  Fixo     Vendas   Comissao Salario Bruto Descontos Salario Liquido Metodo");
            writer.newLine();

            writer.write("===================== ======== ======== ======== ============= ========= =============== ======================================");
            writer.newLine();

            BigDecimal totalFixo = BigDecimal.ZERO;
            BigDecimal totalVendas = BigDecimal.ZERO;
            BigDecimal totalComissao = BigDecimal.ZERO;
            BigDecimal totalBrutoComissionados = BigDecimal.ZERO;
            BigDecimal totalDescontosComissionados = BigDecimal.ZERO;
            BigDecimal totalLiquidoComissionados = BigDecimal.ZERO;

            for(int j = 0; j < ordenados.size(); j++){
                Empregado empregado = ordenados.get(j);

                if(empregado instanceof Comissionado && deveSerPago(empregado, data)){
                    Comissionado comissionado = (Comissionado) empregado;

                    BigDecimal fixo = comissionado.salarioFixo();
                    BigDecimal vendas = comissionado.vendasNoPeriodo(dataPagamento);
                    BigDecimal comissao = comissionado.valorComissao(dataPagamento);
                    BigDecimal bruto = fixo.add(comissao);
                    BigDecimal descontos = calcularDescontos(empregado, dataPagamento);
                    BigDecimal liquido = bruto.subtract(descontos);

                    totalFixo = totalFixo.add(fixo);
                    totalVendas = totalVendas.add(vendas);
                    totalComissao = totalComissao.add(comissao);
                    totalBrutoComissionados = totalBrutoComissionados.add(bruto);
                    totalDescontosComissionados = totalDescontosComissionados.add(descontos);
                    totalLiquidoComissionados = totalLiquidoComissionados.add(liquido);

                    writer.write(String.format(Locale.US, "%-21s %8s %8s %8s %13s %9s %15s %-38s",
                            empregado.getNome(), Utilitarios.formatarDinheiro(fixo), Utilitarios.formatarDinheiro(vendas),
                            Utilitarios.formatarDinheiro(comissao), Utilitarios.formatarDinheiro(bruto), Utilitarios.formatarDinheiro(descontos),
                            Utilitarios.formatarDinheiro(liquido), formatarMetodoPagamento(empregado)));

                    writer.newLine();
                }
            }

            writer.newLine();

            writer.write(String.format(Locale.US, "TOTAL COMISSIONADOS %10s %8s %8s %13s %9s %15s",
                    Utilitarios.formatarDinheiro(totalFixo), Utilitarios.formatarDinheiro(totalVendas), Utilitarios.formatarDinheiro(totalComissao),
                    Utilitarios.formatarDinheiro(totalBrutoComissionados), Utilitarios.formatarDinheiro(totalDescontosComissionados),
                    Utilitarios.formatarDinheiro(totalLiquidoComissionados)));

            writer.newLine();
            writer.newLine();

            BigDecimal totalFolha = totalBrutoHoristas.add(totalBrutoAssalariados).add(totalBrutoComissionados);

            writer.write("TOTAL FOLHA: " + Utilitarios.formatarDinheiro(totalFolha));

            writer.newLine();
        }
    }

    /**
     * Desfaz o último comando que alterou o estado do sistema,
     * restaurando o estado anterior e permitindo refazê-lo com
     * {@link #redo()}.
     *
     * @throws NaoHaComandoADesfazerException se não houver comando a desfazer
     */
    public void undo() {
        if (pilhaUndo.empty()) throw new NaoHaComandoADesfazerException();

        EstadoBancoDados estadoAtual = new EstadoBancoDados(listaEmpregados, contador);

        pilhaRedo.push(estadoAtual);

        EstadoBancoDados estadoAnterior = pilhaUndo.pop();

        listaEmpregados.clear();
        listaEmpregados.addAll(estadoAnterior.getEmpregados());

        contador = estadoAnterior.getContador();
    }

    /**
     * Refaz o último comando desfeito por {@link #undo()}, reaplicando
     * o estado que havia sido descartado.
     *
     * @throws NaoHaComandoARefazerException se não houver comando a refazer
     */
    public void redo() {
        if (pilhaRedo.empty()) throw new NaoHaComandoARefazerException();

        EstadoBancoDados estadoAtual = new EstadoBancoDados(listaEmpregados, contador);

        pilhaUndo.push(estadoAtual);

        EstadoBancoDados proximoEstado = pilhaRedo.pop();

        listaEmpregados.clear();
        listaEmpregados.addAll(proximoEstado.getEmpregados());

        contador = proximoEstado.getContador();
    }

    /** @return o número de empregados atualmente cadastrados no sistema */
    public int getNumeroDeEmpregados(){
        return listaEmpregados.size();
    }

    /**
     * Grava em arquivo o contador de identificadores, os empregados e as
     * agendas de pagamento disponíveis.
     *
     * @param arquivo caminho do arquivo a ser gravado
     * @throws IOException se não for possível escrever o arquivo
     */
    public void gravar(String arquivo) throws IOException {
        try (ObjectOutputStream saida = new ObjectOutputStream(new FileOutputStream(arquivo))) {
            saida.writeInt(contador);
            saida.writeObject(new ArrayList<>(listaEmpregados));
            saida.writeObject(new ArrayList<>(agendasDisponiveis));
        }
    }

    /**
     * Lê de um arquivo o que foi gravado por {@link #gravar(String)}:
     * contador de identificadores, empregados e agendas de pagamento.
     *
     * @param arquivo caminho do arquivo a ser lido
     * @throws IOException se não for possível ler o arquivo
     * @throws ClassNotFoundException se o arquivo tiver classes desconhecidas
     */
    @SuppressWarnings("unchecked")
    public void carregar(String arquivo) throws IOException, ClassNotFoundException {
        try (ObjectInputStream entrada = new ObjectInputStream(new FileInputStream(arquivo))) {
            contador = entrada.readInt();
            listaEmpregados.addAll((List<Empregado>) entrada.readObject());
            agendasDisponiveis.clear();
            agendasDisponiveis.addAll((List<AgendaPagamento>) entrada.readObject());
        }
    }
}