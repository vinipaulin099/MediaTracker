# Roteiro para apresentar o MediaTracker

## Demonstração sugerida — aproximadamente 5 minutos

1. **Objetivo:** “O aplicativo organiza os livros e filmes que eu quero acompanhar. Ele salva o progresso, a avaliação e as anotações para eu continuar depois.”
2. **Coleção:** mostre a lista, busque um título e filtre por livro/filme. Explique que os quatro itens iniciais são simulados e só entram na primeira abertura.
3. **Validação:** abra Adicionar e tente salvar vazio. Mostre as mensagens junto aos campos. Cadastre um livro com 100 páginas e deixe autor/ano vazios para demonstrar campos opcionais.
4. **Estado:** durante o cadastro, gire o dispositivo. Os campos continuam preenchidos. Ao salvar, os detalhes da nova obra são abertos com seu ID.
5. **Acompanhamento:** informe 25 páginas, uma nota de 1 a 5 e uma anotação. Salve e volte. A lista exibe 25% de progresso.
6. **Persistência:** feche o aplicativo e abra novamente. O item, progresso, nota e anotações continuam salvos.
7. **Estados vazios:** busque algo inexistente e use Limpar filtros. Para demonstrar uma coleção totalmente vazia, remova as obras com confirmação. Não é necessário apagar todos os dados do app.
8. **Parcial XML:** toque Catálogo, abra obras diferentes, avance o progresso e gire a tela. Explique a Intent com ID, o ViewBinding e o uso exclusivo de mocks nessa parte.
9. **Código e testes:** apresente MediaItem, MediaValidator, MediaRepository/LocalMediaRepository, um ViewModel e MainActivity. Mostre o relatório dos testes unitários e o mapa dos requisitos.

## Perguntas que você precisa saber responder

**Qual é a diferença entre estado de tela e persistência?**
UiState/StateFlow representam o que a tela mostra naquele momento. SavedStateHandle mantém campos e filtros em recriações pelo Android. O DataStore grava os dados da coleção para recuperá-los em uma nova abertura, mesmo depois de encerrar o processo.

**Por que usar data class com val e copy?**
Uma alteração gera um novo valor. Isso deixa as mudanças previsíveis para o StateFlow e evita que duas telas alterem o mesmo objeto silenciosamente. O modelo impede total zero, progresso fora do intervalo e nota inválida.

**O que é fluxo unidirecional?**
A tela recebe UiState e envia ações por callbacks. O ViewModel valida/processa a ação, usa o Repository e publica um novo UiState. A tela não grava dados diretamente.

**Por que Repository?**
Ele concentra a leitura e escrita e esconde o formato de armazenamento. Os ViewModels dependem da interface. Nos testes, é possível trocar o DataStore por um fake que simula sucesso e falha.

**Por que DataStore em vez de Room?**
Esta coleção é pequena e não exige consultas relacionais. Preferences DataStore com JSON mantém a implementação simples e oferece transações e Flow. Se o volume crescer ou surgirem relacionamentos/consultas complexas, Room será uma alternativa adequada.

**Onde está a operação assíncrona?**
Nas operações suspensas de leitura/inicialização/escrita do DataStore, chamadas por coroutines dos ViewModels. Há também coleta reativa da coleção. Não foi criada uma espera artificial só para mostrar um carregamento.

**Como funciona o cancelamento?**
viewModelScope é cancelado quando o ViewModel é liberado. As entradas de Navigation 3 têm seu próprio ViewModelStore. Ao remover a entrada da back stack, as tarefas daquela tela são canceladas. CancellationException é relançada para respeitar esse comportamento.

**O que Navigation 3 faz neste projeto?**
NavKey identifica uma rota. A back stack guarda essas rotas. entryProvider liga cada rota ao conteúdo. NavDisplay apresenta o conteúdo atual. DetailRoute leva o ID do item. rememberNavBackStack e os decoradores preservam a navegação e os ViewModels/estados apropriados.

**Como evitar gravações perdidas?**
Cada operação usa DataStore.edit, que serializa as transações e lê a coleção mais recente dentro da alteração. O teste de concorrência cadastra vários itens simultaneamente e confirma que nenhum foi sobrescrito.

**Como é tratada uma falha?**
Erro de leitura tem uma tela própria com retry. Erro de gravação mantém os campos e dá feedback. O aplicativo não apaga automaticamente uma coleção ilegível. Fakes de repositório verificam os caminhos de falha nos testes.

**Por que as telas XML ainda existem?**
Elas demonstram a etapa parcial solicitada: dois layouts XML, ViewBinding, Intent explícita com dados, componentes XML reutilizáveis e interação com mocks. A coleção persistente e o fluxo final estão em Compose.

## Onde olhar no código

| Assunto | Arquivo/pasta |
| --- | --- |
| Modelo e validação | model/MediaItem.kt |
| Dados simulados | data/MockCatalog.kt |
| Persistência e transações | data/LocalMediaRepository.kt |
| Injeção manual | MediaTrackerApplication.kt |
| Estados e ações | ui/state/ |
| Navigation 3 e coleta lifecycle | MainActivity.kt |
| Telas Compose e lista | ui/screens/MediaScreens.kt |
| ViewBinding e Intent | HomeActivity.kt e BookDetailActivity.kt |
| Layouts XML | res/layout/ |
| Testes | src/test/ e src/androidTest/ |

Revise cada trecho antes de apresentar. O enunciado exige que o estudante consiga explicar as decisões técnicas e o funcionamento do projeto.
