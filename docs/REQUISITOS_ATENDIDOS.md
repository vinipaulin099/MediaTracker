# Correspondência com o enunciado

Referência: `docs/Avaliação.md`. O projeto mantém o mesmo tema — organizar livros e filmes — nas duas etapas.

## Parcial: Android Views, XML e Intent

| Exigência | Implementação | Como demonstrar |
| --- | --- | --- |
| Duas telas XML com Views e ViewGroups adequados | `activity_home.xml`, `home_list_item_layout.xml`, `book_detail_layout.xml`; LinearLayout, FrameLayout, ScrollView, RecyclerView, TextView, ImageView e Button | Abra Catálogo e escolha uma obra. |
| Intent explícita e passagem de dados | `HomeActivity` envia `media_id`; `BookDetailActivity` usa esse ID para buscar a obra em `MockCatalog` | Abra duas obras diferentes e compare títulos/detalhes. |
| Views conectadas ao Kotlin e interação | ViewBinding nas duas Activities; botões atualizam progresso e indicador | Toque Avançar 1 e Marcar concluído. |
| Data classes imutáveis e valores opcionais | `MediaItem` com `val`; alterações por `copy`; autor/ano/sinopse opcionais tratados com fallback | Abra Filme independente, que não tem esses metadados. |
| Dados simulados | `MockCatalog` fornece quatro obras sem API ou banco nas telas XML | Explore a tela sem internet. |
| Opcional: apenas ViewBinding | Nenhum `findViewById` nas duas Activities | Confira os arquivos Kotlin. |
| Opcional: componente XML reutilizável | `badge_layout.xml`, inflado nos detalhes e incluído na lista | Confira os rótulos de tipo e quantidade. |

## Etapa 2: Jetpack Compose

| Exigência | Implementação | Como demonstrar |
| --- | --- | --- |
| Telas coerentes e fluxo completo | Minha coleção → cadastro → detalhes → salvar acompanhamento → lista atualizada → reabrir app | Cadastre e acompanhe uma obra própria. |
| Acessibilidade | Tema Material, capas descritas, textos escaláveis, rolagem, títulos com heading, feedback de erro, ações com área de toque mínima | Aumente a fonte e use TalkBack. |
| Navigation 3 | Rotas LibraryRoute, FormRoute e DetailRoute(id); NavDisplay, entryProvider, rememberNavBackStack e decoradores | Navegue, volte e gire a tela nos detalhes. |
| Lista LazyColumn/LazyRow | LazyColumn com chave estável por ID em LibraryScreen | Consulte a coleção e filtre os tipos. |
| Formulário com validação e feedback | MediaValidator/FormViewModel; título e total obrigatórios, limite de caracteres, total inteiro, ano opcional válido; nota/progresso também validados | Tente salvar vazio ou com total zero. |
| ViewModel, UiState, StateFlow e collectAsStateWithLifecycle | ViewModels em ui/state; telas recebem estado e callbacks | Confira MainActivity e os ViewModels. |
| Carregamento, conteúdo, vazio e erro | LibraryUiState e estados visuais distintos; erro de leitura com ação de tentar novamente; falha de gravação mantém rascunho | Busque um título inexistente; confira testes de falha e retry. |
| Preservação de estado | SavedStateHandle para rascunhos/busca/filtro, back stack serializável, ViewModels por entry e DataStore para dados salvos | Gire durante o cadastro; feche/reabra após salvar. |
| Repository | Interface MediaRepository e implementação LocalMediaRepository, injetadas manualmente | Confira data/ e MediaTrackerApplication. |
| Operação assíncrona, coroutines e cancelamento | Inicialização, coleta reativa e gravações suspensas, viewModelScope; CancellationException propagada | Veja testes de operações e cancelamento ao liberar o ViewModel. |
| Persistência local coerente e reativa | DataStore serializa a coleção; Flow atualiza a lista após operações; edit impede perda em gravações simultâneas | Salve progresso/nota e reabra. |
| Sem secrets no GitHub | Nenhuma chave ou senha de serviço; .env/keystores/local.properties ignorados | Consulte manifesto, dependências e .gitignore. |

## Opcionais implementados

Testes relevantes de regras, persistência real em arquivo, concorrência e ViewModels; testes Compose dos fluxos principais e testes das Activities XML com Robolectric; interface de repositório e fakes para testes; injeção manual; validação no modelo/repositório; funcionamento offline; layouts com rolagem e limite de largura para telas maiores. Não foram adicionados API, sincronização, WorkManager ou recursos do celular sem necessidade para este escopo.

## Entrega e execução

O README descreve objetivo, dependências, instalação e comandos. O Gradle Wrapper está incluído. A entrega ao professor deve usar o link do repositório próprio no GitHub. Não há arquivo de segredos para enviar. O registro de validação distingue checks executados de testes que ainda exigem um emulador/celular.
