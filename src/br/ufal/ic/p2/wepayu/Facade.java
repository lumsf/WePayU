package br.ufal.ic.p2.wepayu;

import br.ufal.ic.p2.wepayu.Exception.*;
import br.ufal.ic.p2.wepayu.models.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Fachada (Facade) do sistema WePayU: ponto único de entrada para
 * todas as operações do sistema de folha de pagamento (cadastro e
 * alteração de empregados, lançamento de cartões de ponto, vendas e
 * taxas de serviço, consultas, geração da folha e comandos de
 * undo/redo).
 *
 * <p>Esta classe apenas valida as entradas mínimas e repassa a
 * execução para {@link BancoDados}, que concentra as regras de
 * negócio. É a classe usada diretamente pelos testes de aceitação
 * (EasyAccept).</p>
 */
public class Facade {
    private static final String ARQUIVO = "wepayu-dados.ser";
    /** Indica se {@link #encerrarSistema()} já foi chamado, bloqueando novos comandos. */
    private boolean encerrado = false;
    /** Base de dados em memória com os empregados e o histórico de undo/redo. */
    BancoDados novoBancoDados = new BancoDados();

    /**
     * Cria um novo empregado horista ou assalariado.
     *
     * @param nome nome do empregado (não pode ser nulo/vazio)
     * @param endereco endereço do empregado (não pode ser nulo/vazio)
     * @param tipo "horista" ou "assalariado"
     * @param salario salário do empregado (por hora ou mensal, conforme o tipo)
     * @return o identificador gerado para o novo empregado
     * @throws NomeNaoPodeSerNuloException se o nome for nulo/vazio
     * @throws EnderecoNaoPodeSerNuloException se o endereço for nulo/vazio
     * @throws TipoInvalidoException se o tipo não for um tipo reconhecido
     * @throws TipoNaoAplicavelException se o tipo for "comissionado" (deve usar a sobrecarga com comissão)
     */
    public String criarEmpregado(String nome, String endereco, String tipo, String salario) {
        if(nome == null || nome.isEmpty()) throw new NomeNaoPodeSerNuloException();
        if(endereco == null || endereco.isEmpty()) throw new EnderecoNaoPodeSerNuloException();
        if(!tipo.equals("horista") && !tipo.equals("assalariado") && !tipo.equals("comissionado")) {
            throw new TipoInvalidoException();
        }
        if(tipo.equals("comissionado")) throw new TipoNaoAplicavelException();

        String salarioFormatado = Validador.formatar(Validador.validarSalario(salario));

        Empregado novo;

        if(tipo.equals("horista")){
            novo = new Horista(nome, endereco, tipo, salarioFormatado);
        }

        else{
            novo = new Assalariado(nome, endereco, tipo, salarioFormatado);
        }

        novoBancoDados.adicionarEmpregado(novo);

        return novo.getId();
    }

    /**
     * Cria um novo empregado comissionado.
     *
     * @param nome nome do empregado (não pode ser nulo/vazio)
     * @param endereco endereço do empregado (não pode ser nulo/vazio)
     * @param tipo deve ser "comissionado"
     * @param salario salário fixo quinzenal do empregado
     * @param comissao taxa de comissão sobre as vendas
     * @return o identificador gerado para o novo empregado
     * @throws NomeNaoPodeSerNuloException se o nome for nulo/vazio
     * @throws EnderecoNaoPodeSerNuloException se o endereço for nulo/vazio
     * @throws TipoInvalidoException se o tipo não for um tipo reconhecido
     * @throws TipoNaoAplicavelException se o tipo não for "comissionado"
     */
    public String criarEmpregado(String nome, String endereco, String tipo, String salario, String comissao) {
        if(nome == null || nome.isEmpty()) throw new NomeNaoPodeSerNuloException();
        if(endereco == null || endereco.isEmpty()) throw new EnderecoNaoPodeSerNuloException();
        if(!tipo.equals("horista") && !tipo.equals("assalariado") && !tipo.equals("comissionado")) {
            throw new TipoInvalidoException();
        }
        if(!tipo.equals("comissionado")) throw new TipoNaoAplicavelException();

        String salarioFormatado = Validador.formatar(Validador.validarSalario(salario));
        String comissaoFormatada = Validador.formatar(Validador.validarComissao(comissao));

        Comissionado novo = new Comissionado(nome, endereco, tipo, salarioFormatado, comissaoFormatada);
        novoBancoDados.adicionarEmpregado(novo);

        return novo.getId();
    }

    /**
     * Remove um empregado do sistema.
     *
     * @param id identificador do empregado a remover
     * @throws EmpregadoNaoExisteException se não existir empregado com esse id
     */
    public void removerEmpregado (String id) {
        novoBancoDados.remover(id);
    }

    /**
     * Lança um cartão de ponto para um empregado horista.
     *
     * @param id identificador do empregado
     * @param data data do lançamento, "dd/mm/aaaa"
     * @param horas quantidade de horas trabalhadas
     * @throws IdentificacaoDoEmpregadoNaoPodeSerNulaException se o id for nulo/vazio
     */
    public void lancaCartao(String id, String data, String horas){
        if(id == null || id.isEmpty()) throw new IdentificacaoDoEmpregadoNaoPodeSerNulaException();
        novoBancoDados.lancaCartao(id, data, horas);
    }

    /**
     * Lança uma venda para um empregado comissionado.
     *
     * @param id identificador do empregado
     * @param data data da venda, "dd/mm/aaaa"
     * @param valor valor da venda
     */
    public void lancaVenda(String id, String data, String valor){
        novoBancoDados.lancaVenda(id, data, valor);
    }

    /**
     * Lança uma taxa de serviço para o empregado sindicalizado
     * identificado pelo id de sindicato informado.
     *
     * @param id identificação do sindicato do empregado alvo
     * @param data data do lançamento, "dd/mm/aaaa"
     * @param valor valor da taxa
     */
    public void lancaTaxaServico(String id, String data, String valor){
        novoBancoDados.lancaTaxaServico(id, data, valor);
    }

    /**
     * Altera um atributo simples de um empregado: "nome", "endereco",
     * "salario", "comissao", "sindicalizado" ou "metodoPagamento"
     * (para valores que não exigem dados adicionais, como "emMaos").
     *
     * @param id identificador do empregado
     * @param atributo nome do atributo a alterar
     * @param valor novo valor do atributo
     */
    public void alteraEmpregado(String id, String atributo, String valor) {
        if(atributo.equals("tipo")){
            novoBancoDados.mudaTipoEmpregado(id, valor, null);
        }
        else if(atributo.equals("metodoPagamento")){
            novoBancoDados.alteraEmpregado(id, atributo, valor);
        }
        else{
            novoBancoDados.alteraEmpregado(id, atributo, valor);
        }
    }

    /**
     * Altera um atributo de um empregado que exige um dado extra:
     * troca de tipo para "horista"/"assalariado" (extra = novo
     * salário) ou para "comissionado" (extra = comissão), ou troca do
     * método de pagamento para "banco" (extra = nome do banco).
     *
     * @param id identificador do empregado
     * @param atributo "tipo" ou "metodoPagamento"
     * @param valor novo valor do atributo
     * @param extra dado adicional (salário, comissão ou banco, conforme o caso)
     * @throws AtributoNaoExisteException se o atributo não for reconhecido
     */
    public void alteraEmpregado(String id, String atributo, String valor, String extra) {
        if(atributo.equals("tipo")){
            if(valor.equals("comissionado")){
                novoBancoDados.mudaTipoEmpregado(id, valor, null, extra);
            }

            else{
                novoBancoDados.mudaTipoEmpregado(id, valor, extra);
            }
        }

        else if(atributo.equals("metodoPagamento")){
            novoBancoDados.alteraEmpregado(id, atributo, valor, extra, null, null);
        }

        else{
            throw new AtributoNaoExisteException();
        }
    }

    /**
     * Altera um atributo de um empregado que exige dois dados extras:
     * troca de tipo para "comissionado" com salário e comissão
     * explícitos, ou sindicalização com id de sindicato e taxa
     * sindical.
     *
     * @param id identificador do empregado
     * @param atributo "tipo" (com valor "comissionado") ou "sindicalizado"
     * @param valor novo valor do atributo
     * @param extra1 primeiro dado adicional (salário ou id de sindicato)
     * @param extra2 segundo dado adicional (comissão ou taxa sindical)
     * @throws AtributoNaoExisteException se a combinação de atributo/valor não for reconhecida
     */
    public void alteraEmpregado(String id, String atributo, String valor, String extra1, String extra2) {
        if(atributo.equals("tipo") && valor.equals("comissionado")){
            novoBancoDados.mudaTipoEmpregado(id, valor, extra1, extra2);
        }
        else if(atributo.equals("sindicalizado")){
            novoBancoDados.alteraEmpregado(id, atributo, valor, extra1, extra2);
        }
        else{
            throw new AtributoNaoExisteException();
        }
    }

    /**
     * Altera o método de pagamento de um empregado para pagamento em
     * conta bancária.
     *
     * @param id identificador do empregado
     * @param atributo deve ser "metodoPagamento"
     * @param valor deve ser "banco"
     * @param banco nome do banco
     * @param agencia agência bancária
     * @param contaCorrente número da conta corrente
     */
    public void alteraEmpregado(String id, String atributo, String valor, String banco, String agencia,
                                String contaCorrente){
        novoBancoDados.alteraEmpregado(id, atributo, valor, banco, agencia, contaCorrente);
    }

    /**
     * Busca o identificador do {@code indice}-ésimo empregado
     * cadastrado com o nome informado.
     *
     * @param nome nome do empregado procurado
     * @param indice posição (1-based) entre os empregados com esse nome
     * @return o identificador do empregado encontrado
     * @throws NaoHaEmpregadoComEsseNomeException se não houver empregado com esse nome nessa posição
     */
    public String getEmpregadoPorNome(String nome, int indice){
        return novoBancoDados.buscarPorNome(nome, indice);
    }

    /**
     * Consulta o total de horas normais trabalhadas por um empregado
     * horista em um intervalo de datas.
     *
     * @param id identificador do empregado
     * @param dataInicial início do intervalo (inclusive), "dd/mm/aaaa"
     * @param dataFinal fim do intervalo (exclusivo), "dd/mm/aaaa"
     * @return total de horas normais no período, formatado com vírgula
     */
    public String getHorasNormaisTrabalhadas(String id, String dataInicial, String dataFinal){
        return novoBancoDados.getHorasNormaisTrabalhadas(id, dataInicial, dataFinal);
    }

    /**
     * Consulta o total de horas extras trabalhadas por um empregado
     * horista em um intervalo de datas.
     *
     * @param id identificador do empregado
     * @param dataInicial início do intervalo (inclusive), "dd/mm/aaaa"
     * @param dataFinal fim do intervalo (exclusivo), "dd/mm/aaaa"
     * @return total de horas extras no período, formatado com vírgula
     */
    public String getHorasExtrasTrabalhadas(String id, String dataInicial, String dataFinal){
        return novoBancoDados.getHorasExtrasTrabalhadas(id, dataInicial, dataFinal);
    }

    /**
     * Consulta o total vendido por um empregado comissionado em um
     * intervalo de datas.
     *
     * @param id identificador do empregado
     * @param dataInicial início do intervalo (inclusive), "dd/mm/aaaa"
     * @param dataFinal fim do intervalo (exclusivo), "dd/mm/aaaa"
     * @return total vendido no período, formatado com vírgula
     */
    public String getVendasRealizadas(String id, String dataInicial, String dataFinal) {
        return novoBancoDados.getVendasRealizadas(id, dataInicial, dataFinal);
    }

    /**
     * Consulta o total de taxas de serviço lançadas para um
     * empregado sindicalizado em um intervalo de datas.
     *
     * @param id identificador do empregado
     * @param dataInicial início do intervalo (inclusive), "dd/mm/aaaa"
     * @param dataFinal fim do intervalo (exclusivo), "dd/mm/aaaa"
     * @return total de taxas no período, formatado com vírgula
     */
    public String getTaxasServico(String id, String dataInicial, String dataFinal) {
        return novoBancoDados.getTaxasServico(id, dataInicial, dataFinal);
    }

    /**
     * Gera o relatório da folha de pagamento de uma data em um
     * arquivo de saída.
     *
     * @param data data de referência da folha, "dd/mm/aaaa"
     * @param saida caminho do arquivo a ser gerado
     * @throws IOException se ocorrer erro de escrita no arquivo
     */
    public void rodaFolha(String data, String saida) throws IOException {
        novoBancoDados.rodaFolha(data, saida);
    }

    /**
     * Consulta o valor total bruto da folha de pagamento de uma data.
     *
     * @param data data de referência da folha, "dd/mm/aaaa"
     * @return o total da folha, formatado com vírgula
     */
    public String totalFolha(String data) {
        return novoBancoDados.totalFolha(data);
    }

    /**
     * Cria uma nova agenda de pagamento e a disponibiliza para os empregados.
     *
     * @param descricao descrição da agenda, ex.: "semanal 2 5" ou "mensal 10"
     */
    public void criarAgendaDePagamentos(String descricao) {
        novoBancoDados.criarAgendaDePagamentos(descricao);
    }

    /**
     * Consulta o valor de um atributo de um empregado (nome,
     * endereço, tipo, salário, dados de pagamento, sindicalização ou
     * comissão, conforme aplicável ao tipo do empregado).
     *
     * @param id identificador do empregado
     * @param atributo nome do atributo consultado
     * @return o valor do atributo, como texto
     * @throws IdentificacaoDoEmpregadoNaoPodeSerNulaException se o id for nulo/vazio
     * @throws EmpregadoNaoEhSindicalizadoException se consultar dado de sindicato de não sindicalizado
     * @throws EmpregadoNaoRecebeEmBancoException se consultar dado bancário de quem não recebe em banco
     * @throws EmpregadoNaoEhComissionadoException se consultar comissão de quem não é comissionado
     * @throws AtributoNaoExisteException se o atributo não for reconhecido
     * @throws EmpregadoNaoExisteException se o empregado não existir
     */
    public String getAtributoEmpregado(String id,String atributo) {
        if(id == null || id.isEmpty()) throw new IdentificacaoDoEmpregadoNaoPodeSerNulaException();

        Empregado empregado = novoBancoDados.buscar(id);

        if(atributo.equals("nome")){
            return empregado.getNome();
        }
        else if(atributo.equals("endereco")){
            return empregado.getEndereco();
        }
        else if(atributo.equals("tipo")){
            return empregado.getTipo();
        }
        else if(atributo.equals("salario")){
            return empregado.getSalario();
        }
        else if(atributo.equals("metodoPagamento")){
            return empregado.getMetodoPagamento();
        }
        else if(atributo.equals("sindicalizado")){
            return String.valueOf(empregado.isSindicalizado());
        }
        else if(atributo.equals("idSindicato")){
            if(!empregado.isSindicalizado()) throw new EmpregadoNaoEhSindicalizadoException();
            return empregado.getIdSindicato();
        }
        else if(atributo.equals("taxaSindical")){
            if(!empregado.isSindicalizado()) throw new EmpregadoNaoEhSindicalizadoException();
            return empregado.getTaxaSindical();
        }
        else if(atributo.equals("banco")){
            if(!empregado.getMetodoPagamento().equals("banco")) throw new EmpregadoNaoRecebeEmBancoException();
            return empregado.getBanco();
        }
        else if(atributo.equals("agencia")){
            if(!empregado.getMetodoPagamento().equals("banco")) throw new EmpregadoNaoRecebeEmBancoException();
            return empregado.getAgencia();
        }
        else if(atributo.equals("contaCorrente")){
            if(!empregado.getMetodoPagamento().equals("banco")) throw new EmpregadoNaoRecebeEmBancoException();
            return empregado.getContaCorrente();
        }
        else if(atributo.equals("comissao")){
            if(!(empregado instanceof Comissionado)) throw new EmpregadoNaoEhComissionadoException();
            return ((Comissionado) empregado).getComissao();
        }
        else if(atributo.equals("agendaPagamento")){
            return empregado.getAgenda().getDescricao();
        }
        throw new AtributoNaoExisteException();
    }

    /**
     * Desfaz o último comando que alterou o estado do sistema.
     *
     * @throws SistemaEncerradoException se o sistema já tiver sido encerrado
     * @throws NaoHaComandoADesfazerException se não houver comando a desfazer
     */
    public void undo() {
        verificarEncerrado();
        novoBancoDados.undo();
    }

    /**
     * Refaz o último comando desfeito por {@link #undo()}.
     *
     * @throws SistemaEncerradoException se o sistema já tiver sido encerrado
     * @throws NaoHaComandoARefazerException se não houver comando a refazer
     */
    public void redo() {
        verificarEncerrado();
        novoBancoDados.redo();
    }

    /**
     * Consulta o número de empregados atualmente cadastrados.
     *
     * @return o número de empregados, como texto
     * @throws SistemaEncerradoException se o sistema já tiver sido encerrado
     */
    public String getNumeroDeEmpregados() {
        verificarEncerrado();
        return String.valueOf(novoBancoDados.getNumeroDeEmpregados());
    }

    /**
     * Verifica se o sistema já foi encerrado, lançando exceção caso
     * positivo. Usado antes de comandos que não fazem sentido após o
     * encerramento (undo, redo e consulta de número de empregados).
     *
     */
    private void verificarEncerrado() {
        if (encerrado) throw new SistemaEncerradoException();
    }

    /** Lê os dados gravados por encerrarSistema, se o arquivo existir. */
    public Facade() {
        try {
            if (new File(ARQUIVO).exists()) {
                novoBancoDados.carregar(ARQUIVO);
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException("Nao foi possivel ler os dados", e);
        }
    }
    /** Grava os dados em arquivo e bloqueia novos comandos.*/
    public void encerrarSistema() throws IOException{
        novoBancoDados.gravar(ARQUIVO);
        encerrado = true;
    }

    /** Apaga todos os empregados e o arquivo de dados, e reabre o sistema. */
    public void zerarSistema() throws IOException{
        encerrado = false;

        novoBancoDados.zerar();

        Files.deleteIfExists(Paths.get(ARQUIVO));
    }


}