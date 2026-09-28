package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.Exception.*;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Camada de persistência e regras de negócio do sistema WePayU: mantém
 * a lista de empregados em memória, gera os identificadores, valida e
 * registra lançamentos (cartões de ponto, vendas, taxas de serviço),
 * calcula a folha de pagamento e controla o histórico de undo/redo.
 *
 * <p>Esta classe é usada pela {@link br.ufal.ic.p2.wepayu.Facade}, que
 * expõe uma API mais simples para o restante do sistema (e para os
 * testes de aceitação).</p>
 */
public class BancoDados {
    /** Lista de todos os empregados cadastrados no sistema. */
    public List<Empregado> listaEmpregados = new ArrayList<>();
    /** Contador usado para gerar o próximo identificador de empregado ("id1", "id2", ...). */
    private int contador = 0;

    /** Pilha de estados anteriores, usada pelo comando {@link #undo()}. */
    private Stack<EstadoBancoDados> pilhaUndo = new Stack<>();
    /** Pilha de estados desfeitos, usada pelo comando {@link #redo()}. */
    private Stack<EstadoBancoDados> pilhaRedo = new Stack<>();

    /** @return a lista de empregados cadastrados no sistema */
    public List<Empregado> getListaEmpregados(){ return listaEmpregados;}

    /**
     * Empilha uma cópia (snapshot) do estado atual na pilha de undo e
     * limpa a pilha de redo. Deve ser chamado antes de qualquer
     * alteração de estado, para que ela possa ser desfeita depois.
     *
     * @throws Exception se ocorrer erro ao criar o snapshot
     */
    private void salvarEstado() throws Exception {

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
     * @throws Exception se ocorrer erro ao salvar o estado anterior
     */
    public void adicionarEmpregado(Empregado novoEmpregado) throws Exception {
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
     * @throws Exception se ocorrer erro ao salvar o estado anterior
     */
    public void remover(String id) throws Exception{
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
    public Empregado buscar(String id) throws Exception{
        for(Empregado empregado : listaEmpregados){
            if(empregado.getId().equals(id)){
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
     * salário, comissão, sindicalização ou método de pagamento sem
     * dados adicionais).
     *
     * @param id identificador do empregado
     * @param atributo nome do atributo a alterar
     * @param valor novo valor do atributo
     * @throws Exception se o id/atributo/valor forem inválidos para o caso
     */
    public void alteraEmpregado(String id, String atributo, String valor) throws Exception {
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
            salvarEstado();
            e.setSalario(Validador.formatar(Validador.validarSalario(valor)));
        }
        else if(atributo.equals("comissao")){
            if(!e.getTipo().equals("comissionado")) throw new EmpregadoNaoComissionadoException();
            salvarEstado();
            ((Comissionado) e).setComissao(Validador.formatar(Validador.validarComissao(valor)));
        }
        else if(atributo.equals("sindicalizado")){
            if (!valor.equals("true") && !valor.equals("false")) throw new ValorDeveSerTrueOuFalseException();

            salvarEstado();
            e.setSindicalizado(valor);

            if (valor.equals("false")) {
                e.setIdSindicato(null);
                e.setTaxaSindical(null);
                return;
            }
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
     * @throws Exception se os dados forem inválidos ou o sindicato já
     *         estiver em uso por outro empregado
     */
    public void alteraEmpregado(String id, String atributo, String valor, String idSindicato,
                                String taxaSindical) throws Exception {
        Empregado empregado = buscar(id);

        if (!atributo.equals("sindicalizado")) throw new AtributoNaoExisteException();
        if (!valor.equals("true") && !valor.equals("false")) throw new ValorDeveSerTrueOuFalseException();

        if (valor.equals("false")) {
            salvarEstado();
            empregado.setSindicalizado("false");
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

            if (!outro.getId().equals(id) && outro.getSindicalizado().equals("true")
                    && outro.getIdSindicato() != null
                    && outro.getIdSindicato().equals(idSindicato))
                throw new HaOutroEmpregadoComEstaIdentificacaoDeSindicatoException();
        }

        salvarEstado();

        empregado.setSindicalizado("true");
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
     * @throws Exception se os dados forem inválidos
     */
    public void alteraEmpregado(String id, String atributo, String valor, String banco, String agencia,
                                String contaCorrente) throws Exception{
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
     * ele os dados cadastrais comuns do empregado antigo.
     *
     * @param id identificador do empregado
     * @param novoTipo "horista" ou "assalariado"
     * @param salario novo salário (se nulo, reaproveita o salário atual)
     * @throws Exception se o tipo for inválido ou o empregado não existir
     */
    public void mudaTipoEmpregado(String id, String novoTipo, String salario) throws Exception{
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
     * cadastrais comuns do empregado antigo.
     *
     * @param id identificador do empregado
     * @param novoTipo deve ser "comissionado"
     * @param salario ignorado (mantém-se o salário atual do empregado)
     * @param comissao taxa de comissão do novo empregado comissionado
     * @throws Exception se o tipo for inválido ou o empregado não existir
     */
    public void mudaTipoEmpregado(String id, String novoTipo, String salario, String comissao) throws Exception{
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
     * Copia para {@code novo} os dados cadastrais comuns de
     * {@code antigo} (id, forma de pagamento, sindicalização e data
     * do último pagamento), usado ao trocar o tipo de um empregado.
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
        novo.setSindicalizado(antigo.getSindicalizado());
        novo.setIdSindicato(antigo.getIdSindicato());
        novo.setTaxaSindical(antigo.getTaxaSindical());
        novo.setDataUltimoPagamento(antigo.getDataUltimoPagamento());
    }

    /**
     * Remove todos os empregados cadastrados e reinicia o contador de
     * identificadores, usado para reiniciar o sistema entre execuções
     * de testes.
     *
     * @throws Exception se ocorrer erro ao salvar o estado anterior
     */
    public void zerar() throws Exception {
        salvarEstado();
        listaEmpregados.clear();
        contador = 0;
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
    public void lancaCartao(String id, String data, String horas) throws Exception{
        if(id == null || id.isEmpty()) throw new IdentificacaoDoEmpregadoNaoPodeSerNulaException();

        Empregado empregado = buscar(id);

        if(!(empregado instanceof Horista)) throw new EmpregadoNaoEhHoristaException();

        validarData(data);

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
     * Converte e valida uma data no formato "dd/mm/aaaa".
     *
     * @param data texto da data a validar
     * @return a data convertida para {@link LocalDate}
     * @throws DataInvalidaException se o texto não representar uma data válida
     */
    private LocalDate validarData(String data){
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
     * Lança uma venda para um empregado comissionado.
     *
     * @param id identificador do empregado
     * @param data data da venda, no formato "dd/mm/aaaa"
     * @param valor valor da venda (deve ser positivo)
     * @throws EmpregadoNaoComissionadoException se o empregado não for comissionado
     * @throws DataInvalidaException se a data for inválida
     * @throws ValorDeveSerPositivoException se o valor não for positivo
     */
    public void lancaVenda(String id, String data, String valor) throws Exception{
        if(id == null || id.isEmpty()) throw new IdentificacaoDoEmpregadoNaoPodeSerNulaException();

        Empregado empregado = buscar(id);

        if(!(empregado instanceof Comissionado)) throw new EmpregadoNaoComissionadoException();

        validarData(data);

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
    public String getVendasRealizadas(String id, String dataInicial, String dataFinal) throws Exception{
        if(id == null || id.isEmpty()) throw new IdentificacaoDoEmpregadoNaoPodeSerNulaException();

        Empregado empregado = buscar(id);

        if(!(empregado instanceof Comissionado)) throw new EmpregadoNaoEhComissionadoException();

        LocalDate inicio;
        try{
            inicio = validarData(dataInicial);
        }
        catch (DataInvalidaException e){
            throw new DataInicialInvalidaException();
        }

        LocalDate fim;
        try{
            fim = validarData(dataFinal);
        }
        catch (DataInvalidaException e){
            throw new DataFinalInvalidaException();
        }

        if(inicio.isAfter(fim)) throw new DataInicialNaoPodeSerPosteriorAaDataFinalException();

        BigDecimal total = BigDecimal.ZERO;

        for(int j = 0; j < empregado.getListaVendas().size(); j++){
            Venda venda = empregado.getListaVendas().get(j);

            LocalDate dataVenda = validarData(venda.getData());

            if(!dataVenda.isBefore(inicio) && dataVenda.isBefore(fim)){
                BigDecimal valor = new BigDecimal(venda.getValor().replace(",", "."));

                total = total.add(valor);
            }
        }
        return formatarDinheiro(total);
    }

    /**
     * Formata um valor monetário com duas casas decimais e vírgula
     * como separador decimal (arredondamento "half up").
     *
     * @param valor valor a formatar
     * @return o valor formatado, ex.: "1234,56"
     */
    private String formatarDinheiro(BigDecimal valor){
        valor = valor.setScale(2, BigDecimal.ROUND_HALF_UP);

        String resultado = valor.toString();

        resultado = resultado.replace(".", ",");

        return resultado;
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
    public void lancaTaxaServico(String membro, String data, String valor) throws Exception {
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

        validarData(data);

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
    public String getTaxasServico(String id, String dataInicial, String dataFinal) throws Exception{
        if(id == null || id.isEmpty()) throw new IdentificacaoDoEmpregadoNaoPodeSerNulaException();

        Empregado empregado = buscar(id);

        if(!(empregado.getSindicalizado().equals("true"))) throw new EmpregadoNaoEhSindicalizadoException();

        LocalDate inicio;
        try{
            inicio = validarData(dataInicial);
        }
        catch (DataInvalidaException e){
            throw new DataInicialInvalidaException();
        }

        LocalDate fim;
        try{
            fim = validarData(dataFinal);
        }
        catch (DataInvalidaException e){
            throw new DataFinalInvalidaException();
        }

        if(inicio.isAfter(fim)) throw new DataInicialNaoPodeSerPosteriorAaDataFinalException();

        BigDecimal total = BigDecimal.ZERO;

        for(int j = 0; j < empregado.getListaTaxaServico().size(); j++){
            TaxaServico taxaServico = empregado.getListaTaxaServico().get(j);

            LocalDate dataTaxaServico = validarData(taxaServico.getData());

            if(!dataTaxaServico.isBefore(inicio) && dataTaxaServico.isBefore(fim)){
                BigDecimal valorTaxa = new BigDecimal(taxaServico.getValor().replace(",", "."));

                total = total.add(valorTaxa);
            }
        }
        return formatarDinheiro(total);
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
    public String getHorasNormaisTrabalhadas(String id, String dataInicial, String dataFinal) throws Exception{
        if(id == null || id.isEmpty()) throw new IdentificacaoDoEmpregadoNaoPodeSerNulaException();

        Empregado empregado = buscar(id);

        if(!(empregado instanceof Horista)) throw new EmpregadoNaoEhHoristaException();

        LocalDate inicio;
        try{
            inicio = validarData(dataInicial);
        }
        catch (DataInvalidaException e){
            throw new DataInicialInvalidaException();
        }

        LocalDate fim;
        try{
            fim = validarData(dataFinal);
        }
        catch (DataInvalidaException e){
            throw new DataFinalInvalidaException();
        }

        if(inicio.isAfter(fim)) throw new DataInicialNaoPodeSerPosteriorAaDataFinalException();

        BigDecimal total = BigDecimal.ZERO;

        for(int j = 0; j < empregado.getListaCartoes().size(); j++){
            CartaoDePonto cartaoDePonto = empregado.getListaCartoes().get(j);

            LocalDate dataCartao = validarData(cartaoDePonto.getData());

            if(!dataCartao.isBefore(inicio) && dataCartao.isBefore(fim)){
                BigDecimal horas = new BigDecimal(cartaoDePonto.getHoras().replace(",", "."));

                if(horas.compareTo(new BigDecimal("8")) <= 0) total = total.add(horas);
                else{
                    total = total.add(new BigDecimal("8"));
                }
            }
        }
        return formatarHoras(total);
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
    public String getHorasExtrasTrabalhadas(String id, String dataInicial, String dataFinal) throws Exception{
        if(id == null || id.isEmpty()) throw new IdentificacaoDoEmpregadoNaoPodeSerNulaException();

        Empregado empregado = buscar(id);

        if(!(empregado instanceof Horista)) throw new EmpregadoNaoEhHoristaException();

        LocalDate inicio;
        try{
            inicio = validarData(dataInicial);
        }
        catch (DataInvalidaException e){
            throw new DataInicialInvalidaException();
        }

        LocalDate fim;
        try{
            fim = validarData(dataFinal);
        }
        catch (DataInvalidaException e){
            throw new DataFinalInvalidaException();
        }

        if(inicio.isAfter(fim)) throw new DataInicialNaoPodeSerPosteriorAaDataFinalException();

        BigDecimal total = BigDecimal.ZERO;

        for(int j = 0; j < empregado.getListaCartoes().size(); j++){
            CartaoDePonto cartaoDePonto = empregado.getListaCartoes().get(j);

            LocalDate dataCartao = validarData(cartaoDePonto.getData());

            if(!dataCartao.isBefore(inicio) && dataCartao.isBefore(fim)){
                BigDecimal horas = new BigDecimal(cartaoDePonto.getHoras().replace(",", "."));

                if(horas.compareTo(new BigDecimal("8")) > 0) {
                    BigDecimal horasExtras = horas.subtract(new BigDecimal("8"));
                    total = total.add(horasExtras);
                }
            }
        }
        return formatarHoras(total);
    }

    /**
     * Formata uma quantidade de horas removendo zeros à direita
     * desnecessários e usando vírgula como separador decimal.
     *
     * @param valor quantidade de horas a formatar
     * @return o valor formatado, ex.: "8" ou "7,5"
     */
    private String formatarHoras(BigDecimal valor) {
        valor = valor.stripTrailingZeros();

        return valor.toPlainString().replace(".", ",");
    }

    /**
     * Verifica se um empregado deve ser pago em uma determinada data,
     * de acordo com a regra de pagamento do seu tipo: horistas são
     * pagos toda sexta-feira (se tiverem cartões no período),
     * assalariados no último dia útil do mês, e comissionados a cada
     * 14 dias a partir de 14/01/2005.
     *
     * @param empregado empregado a verificar
     * @param data data candidata a pagamento, "dd/mm/aaaa"
     * @return {@code true} se o empregado deve ser pago nessa data
     */
    public boolean deveSerPago(Empregado empregado, String data) {
        LocalDate dataPagamento = validarData(data);

        if (empregado instanceof Horista) {
            if(dataPagamento.getDayOfWeek().getValue() != 5){
                return false;
            }

            LocalDate inicio = dataPagamento.minusDays(6);

            for(int j = 0; j < empregado.getListaCartoes().size(); j++){
                LocalDate dataCartao =
                        validarData(empregado.getListaCartoes().get(j).getData());

                if(!dataCartao.isBefore(inicio) && !dataCartao.isAfter(dataPagamento)){
                    return true;
                }
            }
            return false;
        }

        if (empregado instanceof Comissionado) {
            return devePagarComissionado(dataPagamento);
        }

        if (empregado instanceof Assalariado) {
            return devePagarAssalariado(dataPagamento);
        }
        return false;
    }

    /**
     * Verifica se {@code data} é o dia de pagamento de assalariados
     * naquele mês: o último dia do mês, antecipado para sexta-feira
     * caso caia num fim de semana.
     *
     * @param data data candidata a pagamento
     * @return {@code true} se for o dia de pagamento dos assalariados
     */
    public boolean devePagarAssalariado(LocalDate data) {
        LocalDate ultimoDia = data.withDayOfMonth(data.lengthOfMonth());

        int diaDaSemana = ultimoDia.getDayOfWeek().getValue();

        if (diaDaSemana == 6) {
            ultimoDia = ultimoDia.minusDays(1);
        }
        else if (diaDaSemana == 7) {
            ultimoDia = ultimoDia.minusDays(2);
        }

        return data.equals(ultimoDia);
    }

    /**
     * Verifica se {@code data} é um dia de pagamento de comissionados:
     * um múltiplo de 14 dias a partir do primeiro pagamento, em
     * 14/01/2005.
     *
     * @param data data candidata a pagamento
     * @return {@code true} se for um dia de pagamento dos comissionados
     */
    private boolean devePagarComissionado(LocalDate data) {
        LocalDate primeiroPagamento = LocalDate.of(2005, 1, 14);

        if(data.isBefore(primeiroPagamento)){
            return false;
        }

        long dias = ChronoUnit.DAYS.between(primeiroPagamento, data);

        return dias % 14 == 0;
    }

    /**
     * Calcula o valor total bruto da folha de pagamento de uma data:
     * a soma dos pagamentos de todos os empregados que devem ser
     * pagos nessa data, de acordo com a regra de cada tipo.
     *
     * @param data data de referência da folha, "dd/mm/aaaa"
     * @return o total da folha, formatado com vírgula
     * @throws Exception se a data for inválida
     */
    public String totalFolha(String data) throws Exception{
        LocalDate dataPagamento = validarData(data);

        BigDecimal totalFolha = BigDecimal.ZERO;

        for(int j = 0; j < listaEmpregados.size(); j++){
            Empregado empregado = listaEmpregados.get(j);

            if(deveSerPago(empregado, data)){
                BigDecimal pagamento;

                if(empregado instanceof Horista){
                    pagamento = calcularPagamentoHorista(empregado, dataPagamento);
                }
                else if(empregado instanceof Comissionado){
                    pagamento = calcularPagamentoComissionado(empregado, dataPagamento);
                }
                else{
                    pagamento = calcularPagamentoAssalariado(empregado);
                }

                totalFolha = totalFolha.add(pagamento);
            }
        }

        return formatarDinheiro(totalFolha);
    }

    /**
     * Calcula o pagamento bruto de um empregado horista para os 7
     * dias que terminam em {@code dataPagamento}: horas normais ao
     * valor da hora, mais horas extras a 1,5x o valor da hora.
     *
     * @param empregado empregado horista
     * @param dataPagamento data do pagamento
     * @return o valor bruto a pagar
     */
    private BigDecimal calcularPagamentoHorista(Empregado empregado, LocalDate dataPagamento){
        BigDecimal salarioHora = new BigDecimal(empregado.getSalario().replace(",", "."));
        BigDecimal horasNormais = BigDecimal.ZERO;
        BigDecimal horasExtras = BigDecimal.ZERO;

        LocalDate inicio = dataPagamento.minusDays(6);

        for(int j = 0; j < empregado.getListaCartoes().size(); j++){
            CartaoDePonto cartao = empregado.getListaCartoes().get(j);
            LocalDate dataCartao = validarData(cartao.getData());

            if(!dataCartao.isBefore(inicio) && !dataCartao.isAfter(dataPagamento)){
                BigDecimal horas = new BigDecimal(cartao.getHoras().replace(",", "."));
                if(horas.compareTo(new BigDecimal("8")) <= 0){
                    horasNormais = horasNormais.add(horas);
                }
                else{
                    horasNormais = horasNormais.add(new BigDecimal("8"));

                    BigDecimal extras = horas.subtract(new BigDecimal("8"));

                    horasExtras = horasExtras.add(extras);
                }
            }
        }

        BigDecimal pagamentoNormal = horasNormais.multiply(salarioHora);
        BigDecimal pagamentoExtra = horasExtras.multiply(salarioHora).multiply(new BigDecimal("1.5"));

        return pagamentoNormal.add(pagamentoExtra);
    }

    /**
     * Calcula o pagamento bruto de um empregado assalariado: o seu
     * salário mensal fixo, sem cálculos adicionais.
     *
     * @param empregado empregado assalariado
     * @return o valor bruto a pagar
     */
    private BigDecimal calcularPagamentoAssalariado(Empregado empregado){
        return new BigDecimal(empregado.getSalario().replace(",", "."));
    }

    /**
     * Calcula o pagamento bruto de um empregado comissionado para os
     * 14 dias que terminam em {@code dataPagamento}: o salário fixo
     * quinzenal (salário mensal * 12 / 26) somado à comissão sobre o
     * total vendido no período.
     *
     * @param empregado empregado comissionado
     * @param dataPagamento data do pagamento
     * @return o valor bruto a pagar
     */
    private BigDecimal calcularPagamentoComissionado(Empregado empregado, LocalDate dataPagamento){
        Comissionado comissionado = (Comissionado) empregado;

        BigDecimal salario = new BigDecimal(comissionado.getSalario().replace(",", "."));
        BigDecimal percentualComissao = new BigDecimal(comissionado.getComissao().replace(",", "."));
        BigDecimal salarioDuasSemanas = salario.multiply(new BigDecimal("12")).divide(new BigDecimal("26"), 10, BigDecimal.ROUND_DOWN);

        salarioDuasSemanas = salarioDuasSemanas.setScale(2, BigDecimal.ROUND_DOWN);

        LocalDate inicio = dataPagamento.minusDays(13);

        BigDecimal totalVendas = BigDecimal.ZERO;

        for(int j = 0; j < comissionado.getListaVendas().size(); j++){
            Venda venda = comissionado.getListaVendas().get(j);

            LocalDate dataVenda = validarData(venda.getData());

            if(!dataVenda.isBefore(inicio) && !dataVenda.isAfter(dataPagamento)){
                BigDecimal valorVenda = new BigDecimal(venda.getValor().replace(",", "."));

                totalVendas = totalVendas.add(valorVenda);
            }
        }

        BigDecimal comissao = totalVendas.multiply(percentualComissao).setScale(2, BigDecimal.ROUND_DOWN);

        return salarioDuasSemanas.add(comissao);
    }

    /**
     * Aplica o desconto de taxa sindical diária ao pagamento bruto de
     * um empregado sindicalizado, proporcional aos dias do período de
     * pagamento correspondente ao seu tipo.
     *
     * @param empregado empregado a descontar
     * @param pagamento valor bruto antes do desconto
     * @param dataPagamento data do pagamento
     * @return o valor com o desconto de taxa sindical aplicado
     */
    private BigDecimal aplicarDescontosSindicais(Empregado empregado, BigDecimal pagamento,
                                                 LocalDate dataPagamento){
        if(!empregado.getSindicalizado().equals("true")){
            return pagamento;
        }

        LocalDate inicio;

        if(empregado instanceof Horista){
            inicio = dataPagamento.minusDays(6);
        }
        else if(empregado instanceof Comissionado){
            inicio = dataPagamento.minusDays(13);
        }
        else{
            inicio = dataPagamento.withDayOfMonth(1);
        }

        if(empregado.getTaxaSindical() != null){
            BigDecimal taxaDiaria = new BigDecimal(empregado.getTaxaSindical().replace(",", "."));

            long quantidadeDias = ChronoUnit.DAYS.between(inicio, dataPagamento) + 1;

            BigDecimal taxaSindical = taxaDiaria.multiply(BigDecimal.valueOf(quantidadeDias));

            pagamento = pagamento.subtract(taxaSindical);
        }
        return pagamento;
    }

    /**
     * Calcula o total de descontos de um empregado para o período de
     * pagamento correspondente ao seu tipo: taxa sindical diária (se
     * sindicalizado) mais as taxas de serviço lançadas no período.
     *
     * @param empregado empregado a calcular os descontos
     * @param dataPagamento data do pagamento
     * @return o total de descontos no período
     */
    private BigDecimal calcularDescontos(Empregado empregado, LocalDate dataPagamento){
        BigDecimal descontos = BigDecimal.ZERO;

        if(empregado.getSindicalizado().equals("true") && empregado.getTaxaSindical() != null){

            LocalDate inicio;

            if(empregado instanceof Horista){
                inicio = dataPagamento.minusDays(6);
            }
            else if(empregado instanceof Comissionado){
                inicio = dataPagamento.minusDays(13);
            }
            else{
                inicio = dataPagamento.withDayOfMonth(1);
            }

            long dias = ChronoUnit.DAYS.between(inicio, dataPagamento) + 1;

            BigDecimal taxa = new BigDecimal(empregado.getTaxaSindical().replace(",", "."));

            descontos = descontos.add(taxa.multiply(BigDecimal.valueOf(dias)));
        }

        LocalDate inicioPeriodo;

        if(empregado instanceof Horista){
            inicioPeriodo = dataPagamento.minusDays(6);
        }
        else if(empregado instanceof Comissionado){
            inicioPeriodo = dataPagamento.minusDays(13);
        }
        else{
            inicioPeriodo = dataPagamento.withDayOfMonth(1);
        }

        for(int j = 0; j < empregado.getListaTaxaServico().size(); j++){
            TaxaServico taxaServico = empregado.getListaTaxaServico().get(j);

            LocalDate dataTaxa = validarData(taxaServico.getData());

            if(!dataTaxa.isBefore(inicioPeriodo) && dataTaxa.isBefore(dataPagamento)){
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
            return "Banco do Brasil, Ag. " + empregado.getAgencia() + " CC " + empregado.getContaCorrente();
        }

        return "";
    }

    /**
     * Gera o relatório da folha de pagamento de uma data em um
     * arquivo de texto, com uma seção para cada tipo de empregado
     * (horistas, assalariados e comissionados), listados em ordem
     * alfabética, seguida do total geral da folha.
     *
     * @param data data de referência da folha, "dd/mm/aaaa"
     * @param saida caminho do arquivo de saída a ser gerado
     * @throws Exception se a data for inválida ou ocorrer erro de escrita
     */
    public void rodaFolha(String data, String saida) throws Exception  {
        LocalDate dataPagamento = validarData(data);

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

            int totalHorasNormais = 0;
            int totalHorasExtras = 0;
            BigDecimal totalBrutoHoristas = BigDecimal.ZERO;
            BigDecimal totalDescontosHoristas = BigDecimal.ZERO;
            BigDecimal totalLiquidoHoristas = BigDecimal.ZERO;

            if (dataPagamento.getDayOfWeek().getValue() == 5) {
                for (int j = 0; j < ordenados.size(); j++) {
                    Empregado empregado = ordenados.get(j);

                    if (empregado instanceof Horista) {
                        int horasNormais = 0;
                        int horasExtras = 0;

                        LocalDate inicio = dataPagamento.minusDays(6);
                        BigDecimal salarioHora = new BigDecimal(empregado.getSalario().replace(",", "."));

                        for (int k = 0; k < empregado.getListaCartoes().size(); k++) {
                            CartaoDePonto cartao = empregado.getListaCartoes().get(k);
                            LocalDate dataCartao = validarData(cartao.getData());

                            if (!dataCartao.isBefore(inicio) && !dataCartao.isAfter(dataPagamento)) {
                                BigDecimal horas = new BigDecimal(cartao.getHoras().replace(",",
                                        "."));
                                if (horas.compareTo(new BigDecimal("8")) <= 0) {
                                    horasNormais += horas.intValue();
                                } else {
                                    horasNormais += 8;
                                    horasExtras += horas.subtract(new BigDecimal("8")).intValue();
                                }
                            }
                        }

                        BigDecimal pagamentoNormal = salarioHora.multiply(BigDecimal.valueOf(horasNormais));
                        BigDecimal pagamentoExtra = salarioHora.multiply(BigDecimal.valueOf(horasExtras))
                                .multiply(new BigDecimal("1.5"));
                        BigDecimal bruto = pagamentoNormal.add(pagamentoExtra);

                        BigDecimal taxasServicoPeriodo = BigDecimal.ZERO;
                        for (int k = 0; k < empregado.getListaTaxaServico().size(); k++) {
                            TaxaServico ts = empregado.getListaTaxaServico().get(k);
                            LocalDate dataTaxa = validarData(ts.getData());
                            if (!dataTaxa.isBefore(inicio) && dataTaxa.isBefore(dataPagamento)) {
                                taxasServicoPeriodo = taxasServicoPeriodo.add(new BigDecimal(ts.getValor()
                                        .replace(",", ".")));
                            }
                        }

                        BigDecimal devidoSemana = BigDecimal.ZERO;
                        if (empregado.getSindicalizado().equals("true") && empregado.getTaxaSindical() != null) {
                            BigDecimal taxaDiaria = new BigDecimal(empregado.getTaxaSindical().replace(","
                                    , "."));
                            devidoSemana = taxaDiaria.multiply(BigDecimal.valueOf(7));
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

                        totalHorasNormais += horasNormais;
                        totalHorasExtras += horasExtras;
                        totalBrutoHoristas = totalBrutoHoristas.add(bruto);
                        totalDescontosHoristas = totalDescontosHoristas.add(descontos);
                        totalLiquidoHoristas = totalLiquidoHoristas.add(liquido);

                        String metodo = formatarMetodoPagamento(empregado);

                        writer.write(String.format(Locale.US, "%-36s %5d %5d %13s %9s %15s %-38s",
                                empregado.getNome(), horasNormais, horasExtras,
                                formatarDinheiro(bruto), formatarDinheiro(descontos), formatarDinheiro(liquido),
                                metodo));
                        writer.newLine();
                    }
                }
            }

            writer.newLine();

            writer.write(String.format(Locale.US, "TOTAL HORISTAS %27d %5d %13s %9s %15s", totalHorasNormais
                    , totalHorasExtras, formatarDinheiro(totalBrutoHoristas), formatarDinheiro(totalDescontosHoristas), formatarDinheiro(totalLiquidoHoristas)));
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
                    BigDecimal bruto = calcularPagamentoAssalariado(empregado);
                    BigDecimal descontos = calcularDescontos(empregado, dataPagamento);
                    BigDecimal liquido = bruto.subtract(descontos);

                    totalBrutoAssalariados = totalBrutoAssalariados.add(bruto);
                    totalDescontosAssalariados = totalDescontosAssalariados.add(descontos);
                    totalLiquidoAssalariados = totalLiquidoAssalariados.add(liquido);

                    writer.write(String.format(Locale.US, "%-48s %13s %9s %15s %-38s", empregado.getNome(),
                            formatarDinheiro(bruto), formatarDinheiro(descontos), formatarDinheiro(liquido), formatarMetodoPagamento(empregado)));

                    writer.newLine();
                }
            }

            writer.newLine();

            writer.write(String.format(Locale.US, "TOTAL ASSALARIADOS %43s %9s %15s",
                    formatarDinheiro(totalBrutoAssalariados), formatarDinheiro(totalDescontosAssalariados),
                    formatarDinheiro(totalLiquidoAssalariados)));            writer.newLine();
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

                    BigDecimal fixo = new BigDecimal(empregado.getSalario().replace(",", "."))
                            .multiply(new BigDecimal("12")).divide(new BigDecimal("26"), 10,
                                    BigDecimal.ROUND_DOWN);

                    fixo = fixo.setScale(2, BigDecimal.ROUND_DOWN);

                    LocalDate inicio = dataPagamento.minusDays(13);

                    BigDecimal vendas = BigDecimal.ZERO;

                    for(int k = 0; k < empregado.getListaVendas().size(); k++){
                        Venda venda = empregado.getListaVendas().get(k);
                        LocalDate dataVenda = validarData(venda.getData());

                        if(!dataVenda.isBefore(inicio) && !dataVenda.isAfter(dataPagamento)){
                            vendas = vendas.add(new BigDecimal(venda.getValor().replace(",", ".")));
                        }
                    }

                    BigDecimal percentual = new BigDecimal(comissionado.getComissao().replace(",",
                            "."));
                    BigDecimal comissao = vendas.multiply(percentual).setScale(2, BigDecimal.ROUND_DOWN);
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
                            empregado.getNome(), formatarDinheiro(fixo), formatarDinheiro(vendas),
                            formatarDinheiro(comissao), formatarDinheiro(bruto), formatarDinheiro(descontos),
                            formatarDinheiro(liquido), formatarMetodoPagamento(empregado)));

                    writer.newLine();
                }
            }

            writer.newLine();

            writer.write(String.format(Locale.US, "TOTAL COMISSIONADOS %10s %8s %8s %13s %9s %15s",
                    formatarDinheiro(totalFixo), formatarDinheiro(totalVendas), formatarDinheiro(totalComissao),
                    formatarDinheiro(totalBrutoComissionados), formatarDinheiro(totalDescontosComissionados),
                    formatarDinheiro(totalLiquidoComissionados)));

            writer.newLine();
            writer.newLine();

            BigDecimal totalFolha = totalBrutoHoristas.add(totalBrutoAssalariados).add(totalBrutoComissionados);

            writer.write("TOTAL FOLHA: " + formatarDinheiro(totalFolha));

            writer.newLine();
        }
    }

    /**
     * Desfaz o último comando que alterou o estado do sistema,
     * restaurando o estado anterior e permitindo refazê-lo com
     * {@link #redo()}.
     *
     * @throws NaoHaComandoADesfazerException se não houver comando a desfazer
     * @throws Exception se ocorrer erro ao restaurar o estado
     */
    public void undo() throws Exception {
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
     * @throws Exception se ocorrer erro ao restaurar o estado
     */
    public void redo() throws Exception {
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
}