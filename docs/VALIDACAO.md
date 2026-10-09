# Validação da entrega

Verificação realizada em 7 de outubro de 2026, em Linux, JDK 17, Gradle 8.13 e Android SDK 36.

| Verificação | Resultado |
| --- | --- |
| Gradle Wrapper incluído no projeto | `./gradlew --version` executado; distribuição 8.13 baixada e checksum conferido pelo wrapper. |
| Compilação do aplicativo | `assembleDebug` gerou o APK de debug. |
| Testes de regras de negócio | 6 aprovados. |
| Testes da persistência real em arquivo | 4 aprovados: reabertura do DataStore, coleção vazia, concorrência e dados inválidos. |
| Testes de ViewModels | 12 aprovados: estados, filtros, rascunhos, validação, retry, gravação e cancelamento. |
| Testes Compose com Robolectric | 2 aprovados: validação/estado vazio e cadastro → detalhes → concluir/salvar → voltar à lista atualizada. |
| Testes XML com Robolectric | 3 aprovados: Intent com ID correto, campos opcionais/progresso restaurado e ID inexistente. |
| Total de testes locais | **27 executados, zero falhas, zero erros, zero ignorados.** |
| Android Lint | `lintDebug` concluído, **zero erros e 17 avisos**. |
| Compilação dos testes instrumentados | `assembleDebugAndroidTest` concluído. |
| Integridade do APK | Entradas ZIP/CRC verificadas; metadados confirmam minSdk 26 e targetSdk 36. |
| Revisão do diff | `git diff --check` sem problemas. |

Os avisos de lint são informativos: versões mais recentes disponíveis (as versões usadas foram fixadas e testadas), recursos não usados da base das aulas, localização das duas imagens da base, marcação de títulos XML suportada a partir da API 28 e o corretor inglês interpretando “Autor” como erro. Eles não impediram a compilação.

Comandos/tarefas utilizados: `assembleDebug`, `testDebugUnitTest`, `lintDebug` e `assembleDebugAndroidTest`, com um worker no ambiente de validação. Os testes de interface locais foram executados por Robolectric com Android API 35. O conjunto instrumentado foi compilado, mas **não foi executado em um celular físico ou emulador Android** neste ambiente.

Para conferir no seu dispositivo, siga o README e rode `./gradlew connectedDebugAndroidTest` com um emulador/celular conectado. O roteiro de apresentação inclui uma conferência manual da rotação e da persistência após reabrir o app.
