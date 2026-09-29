# Ficha da App Store — Natação Criativa Treinos

Tudo o que o App Store Connect pede para publicar a versão 1.0, pronto para
copiar e colar. Os limites de caracteres são os da Apple.

## App

| Campo | Valor |
|---|---|
| Nome (até 30) | Natação Criativa Treinos |
| Subtítulo (até 30) | Seu treino de natação diário |
| Bundle ID | com.natacaocriativa.treinos |
| SKU | natacao-criativa-treinos |
| Idioma principal | Português (Brasil) |
| Categoria principal | Saúde e fitness |
| Categoria secundária | Esportes |
| Preço | Grátis |
| Classificação etária | 4+ (nenhum conteúdo sensível) |
| Direitos autorais | 2026 Natação Criativa |
| URL de suporte | https://natacaocriativa.com.br |
| URL de marketing | https://natacaocriativa.com.br |
| Política de privacidade | https://treinos.natacaocriativa.com.br/privacidade.html |

## Texto promocional (até 170)

Treino de natação pronto todo dia, do seu nível, com cronômetro por série.
Siga um plano de semanas, acompanhe o progresso e compartilhe o treino feito.

## Descrição (até 4000)

O Natação Criativa Treinos traz o treino do dia pronto para você cair na
água, montado pelo Método Natação Criativa.

TREINO DO DIA
• Um treino novo a cada dia, em ciclos de 28 dias.
• Três níveis: Pré-condicionamento, Condicionamento e Aperfeiçoamento.
• Programa de piscina e programa de preparação para águas abertas (mar,
  lago e travessias), a partir do Condicionamento.
• Distância total, tempo estimado, objetivo e esforço de cada treino.

NADE COM O APP
• Cronômetro do treino e série por série: aquecimento, parte principal e
  volta à calma.
• Toque a cada repetição e acompanhe o que falta.
• Ao terminar, registre o esforço (0 a 10) e uma observação.

PLANO DE TREINO
• Monte um plano de várias semanas com o número de treinos por semana que
  cabe na sua rotina.
• Veja o próximo treino e troque qualquer dia por outro do método ou por
  um treino seu.

MEUS TREINOS
• Crie e edite os seus treinos.
• Salve a sugestão do dia para nadar depois.
• Receba treinos de outras pessoas por link ou código.

PROGRESSO
• Sua semana, metros e tempo nadados, histórico e conquistas.
• Pontos por metro e por treino completo.
• Ranking opcional: só aparece quem aceita, com o nome que escolher.

COMPARTILHE
• Cartão bonito do treino feito, para o Instagram, o WhatsApp e as redes.

CUIDADO COM A SAÚDE
• Questionário PAR-Q antes de começar a treinar.
• Lembrete no dia do treino.

Sua conta pode ser excluída a qualquer momento, dentro do próprio app, em
Perfil.

## Palavras-chave (até 100, separadas por vírgula, sem espaço)

natação,nadar,piscina,treino,águas abertas,triathlon,plano de treino,cronômetro,metros,fitness

## Novidades desta versão

Primeira versão para iPhone.

## Capturas de tela

iPhone 6,9" (1320 × 2868), na pasta `iphone-loja\capturas-6.9` do computador,
nesta ordem:

1. Tela inicial com o calendário e o plano
2. Treino do dia na tela inicial
3. Plano de treino
4. Treino do dia com distância, tempo e objetivo
5. Estrutura do treino
6. PAR-Q antes do primeiro treino

A Apple aceita só o tamanho de 6,9" e reaproveita para os outros iPhones.

## Privacidade do app (questionário do App Store Connect)

"Você ou seus parceiros coletam dados deste app?" → **Sim**.

| Tipo de dado | Coletado | Ligado à pessoa | Rastreamento | Finalidade |
|---|---|---|---|---|
| Informações de contato → Endereço de e-mail | Sim | Sim | Não | Funcionalidade do app |
| Informações de contato → Nome | Sim | Sim | Não | Funcionalidade do app |
| Saúde e condicionamento → Saúde (PAR-Q, esforço) | Sim | Sim | Não | Funcionalidade do app |
| Saúde e condicionamento → Condicionamento físico (treinos) | Sim | Sim | Não | Funcionalidade do app |
| Identificadores → ID do usuário | Sim | Sim | Não | Funcionalidade do app |

Nada de publicidade, análise de terceiros ou rastreamento. As respostas
batem com `iosApp/Privacidade/PrivacyInfo.xcprivacy`.

## Informações para a revisão da Apple

- **Conta de teste:** e-mail e senha de uma conta só de teste (a Nice
  preenche no App Store Connect; não vai para o repositório).
- **Observações para o revisor:**

  > O app mostra treinos de natação. Para entrar, use a conta de teste acima.
  > Antes do primeiro treino aparece o questionário PAR-Q (saúde), que pode
  > ser fechado. A exclusão de conta fica em Perfil > Excluir minha conta.
  > O ranking é opcional e só mostra quem aceitou participar.

- **Criptografia:** o app só usa HTTPS; já declarado no Info.plist
  (`ITSAppUsesNonExemptEncryption = false`), então a Apple não pergunta.

## Checklist do que a Apple exige

- [x] Exclusão de conta dentro do app
- [x] Política de privacidade pública
- [x] Manifesto de privacidade (PrivacyInfo.xcprivacy)
- [x] Ícone 1024 × 1024 sem transparência
- [x] Login só por e-mail (sem login social, não precisa de "Entrar com a Apple")
- [ ] Conta Apple Developer (US$ 99/ano)
- [ ] App criado no App Store Connect
- [ ] Chave de API e segredos no GitHub
- [ ] Primeiro envio pelo GitHub e teste no TestFlight
- [ ] Ficha, capturas e privacidade preenchidas
- [ ] Enviar para revisão
