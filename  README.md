# MOD – Gestão Musical

Sistema de Organização e Acompanhamento Musical desenvolvido para apoiar a **Secretaria Musical do Setor Paranoá**, em Brasília-DF.

> Projeto acadêmico desenvolvido no contexto do curso de **Análise e Desenvolvimento de Sistemas – UNINTER**, com aplicação prática em um cenário real de gestão musical.

---

## Sumário

- [1. Apresentação](#1-apresentação)
- [2. Problema identificado](#2-problema-identificado)
- [3. Objetivos](#3-objetivos)
- [4. Contexto de utilização](#4-contexto-de-utilização)
- [5. ODS relacionados](#5-ods-relacionados)
- [6. Visão geral da solução](#6-visão-geral-da-solução)
- [7. Arquitetura](#7-arquitetura)
- [8. Tecnologias utilizadas](#8-tecnologias-utilizadas)
- [9. Estrutura do projeto](#9-estrutura-do-projeto)
- [10. Perfis de usuário](#10-perfis-de-usuário)
- [11. Gestão de alunos](#11-gestão-de-alunos)
- [12. Evolução musical](#12-evolução-musical)
- [13. Histórico](#13-histórico)
- [14. Auditoria](#14-auditoria)
- [15. Notificações e alertas](#15-notificações-e-alertas)
- [16. Arquivamento e restauração](#16-arquivamento-e-restauração)
- [17. Compartilhamento entre Comuns](#17-compartilhamento-entre-comuns)
- [18. Relatórios](#18-relatórios)
- [19. Dashboard](#19-dashboard)
- [20. Segurança e autenticação](#20-segurança-e-autenticação)
- [21. Banco de dados](#21-banco-de-dados)
- [22. Swagger / OpenAPI](#22-swagger--openapi)
- [23. Como executar](#23-como-executar)
- [24. Fluxo de utilização](#24-fluxo-de-utilização)
- [25. Diagramas e evidências](#25-diagramas-e-evidências)
- [26. Status atual do desenvolvimento](#26-status-atual-do-desenvolvimento)
- [27. Testes realizados](#27-testes-realizados)
- [28. Limitações conhecidas](#28-limitações-conhecidas)
- [29. Evoluções futuras](#29-evoluções-futuras)
- [30. Contexto acadêmico](#30-contexto-acadêmico)

---

# 1. Apresentação

O **MOD – Gestão Musical** é um sistema desenvolvido para auxiliar a organização e o acompanhamento das atividades da Secretaria Musical do Setor Paranoá.

A proposta central é concentrar em um único sistema informações relacionadas a alunos, evolução musical, histórico, notificações, auditoria, arquivamentos, compartilhamentos entre Comuns e relatórios operacionais.

O projeto foi pensado para oferecer **rastreabilidade**, **organização**, **segurança**, **padronização** e **apoio à tomada de decisão**, reduzindo a dependência de registros dispersos e facilitando o acompanhamento das atividades musicais.

---

# 2. Problema identificado

O acompanhamento musical envolve diversas informações que precisam ser atualizadas ao longo do tempo, entre elas:

- cadastro de alunos;
- Comum de origem;
- nível musical;
- cargo ministerial;
- instrumentos;
- evolução em MTS;
- evolução em MSA;
- métodos musicais;
- hinário;
- escalas;
- alterações cadastrais;
- arquivamentos;
- restaurações;
- compartilhamentos;
- notificações;
- histórico das ações realizadas.

Sem uma estrutura centralizada, torna-se mais difícil identificar:

- quem realizou determinada operação;
- quando uma informação foi modificada;
- qual era o estado anterior de um registro;
- quais alunos estão sem movimentação;
- quais ações ocorreram em determinado período;
- quais usuários realizaram lançamentos;
- quais registros exigem atenção da Secretaria.

O MOD foi criado para tratar esses pontos por meio de uma aplicação com persistência em banco de dados, autenticação, auditoria e histórico.

---

# 3. Objetivos

## 3.1 Objetivo geral

Desenvolver um sistema de gestão capaz de apoiar a Secretaria Musical no cadastro, organização, acompanhamento e rastreamento das informações relacionadas aos alunos e à evolução musical.

## 3.2 Objetivos específicos

- centralizar os dados dos alunos;
- organizar informações de Comum, nível, cargo e instrumento;
- registrar evolução musical;
- manter histórico das principais operações;
- identificar o usuário responsável por cada ação;
- gerar notificações automáticas;
- detectar alunos sem movimentação por período prolongado;
- permitir arquivamento lógico e restauração;
- permitir compartilhamento controlado entre Comuns;
- fornecer relatórios operacionais;
- documentar a API;
- melhorar a rastreabilidade e a segurança das operações.

---

# 4. Contexto de utilização

O sistema foi modelado considerando a realidade da **Secretaria Musical do Setor Paranoá**.

Entre as Comuns consideradas no domínio do projeto estão:

- Sobradinho dos Melos;
- Paranoá – Quadra 30 (Central);
- Altiplano Leste;
- Itapoã I;
- Del Lago II;
- Setor Mandala;
- Paranoá Parque;
- Vila Margarida – Rota do Cavalo.

O sistema não pretende substituir automaticamente sistemas institucionais externos. Sua função é apoiar a organização, o acompanhamento e a rastreabilidade das informações tratadas pela Secretaria Musical dentro do escopo definido para o projeto.

---

# 5. ODS relacionados

O projeto está relacionado aos seguintes Objetivos de Desenvolvimento Sustentável:

- **ODS 04 – Educação de Qualidade**
- **ODS 09 – Indústria, Inovação e Infraestrutura**
- **ODS 11 – Cidades e Comunidades Sustentáveis**
- **ODS 16 – Paz, Justiça e Instituições Eficazes**

A relação ocorre principalmente pela aplicação de tecnologia na organização de processos, apoio à formação musical, melhoria da gestão da informação e fortalecimento da rastreabilidade.

---

# 6. Visão geral da solução

A aplicação é organizada em módulos que se comunicam com uma API REST construída em Spring Boot.

Principais capacidades implementadas:

- autenticação de usuários cadastrados no banco;
- login e logout com sessão;
- identificação do usuário autenticado;
- controle de uma sessão ativa por usuário;
- gestão de alunos;
- Comuns, níveis e cargos ministeriais;
- instrumentos e tonalidades;
- registro de MTS;
- registro de MSA;
- registro de Método;
- registro de Hinário;
- registro de Escala;
- histórico;
- auditoria;
- notificações;
- arquivamento e restauração;
- compartilhamento entre Comuns;
- alterações restritas com fluxo PENDENTE/APROVADA/REJEITADA;
- identificação de alunos sem movimentação;
- alertas automáticos;
- relatório mensal;
- Dashboard da Secretaria;
- usuários e permissões;
- interface web em Angular + TypeScript;
- documentação da API com Swagger/OpenAPI.

Funcionalidades posteriores à entrega são identificadas neste documento.

---

# 7. Arquitetura

O sistema utiliza uma arquitetura em camadas.

```text
Cliente / Postman / Swagger / Interface
                 |
                 v
            Controller
                 |
                 v
              Service
                 |
                 v
            Repository
                 |
                 v
         JPA / Hibernate
                 |
                 v
            PostgreSQL
```

Responsabilidades principais:

### Controller

Responsável por receber as requisições HTTP, validar a rota e encaminhar a operação para a camada de serviço.

### Service

Concentra as regras de negócio, validações, transações e integração entre diferentes entidades do sistema.

### Repository

Responsável pelo acesso aos dados utilizando Spring Data JPA.

### Entity

Representa as entidades persistidas no PostgreSQL.

### DTO

Define os objetos utilizados na entrada e saída de dados da API, evitando exposição desnecessária das entidades.

### Security

Responsável pela autenticação, identificação do usuário autenticado e configuração do Spring Security.

---

# 8. Tecnologias utilizadas

| Tecnologia | Utilização |
|---|---|
| Java 21 | Linguagem principal |
| Spring Boot | Framework da aplicação |
| Spring Web | API REST |
| Spring Data JPA | Persistência |
| Hibernate | ORM |
| PostgreSQL 17 | Banco de dados |
| Spring Security | Autenticação, autorização e sessão |
| BCrypt | Hash de senhas |
| Springdoc OpenAPI | Documentação Swagger |
| Maven | Gerenciamento de dependências |
| Angular | Interface web |
| TypeScript | Front-end |
| Node.js / npm | Ambiente do front-end |
| Postman | Testes da API |
| Git | Controle de versão |
| GitHub | Repositórios remotos |
| IntelliJ IDEA | Desenvolvimento back-end |
| pgAdmin | Administração do PostgreSQL |

---

# 9. Estrutura do projeto

Estrutura simplificada:

```text
mod-gestao-musical/
│
├── database/
│   ├── schema.sql
│   └── dados-iniciais.sql
│
├── docs/
│   └── imagens/
│
├── src/
│   └── main/
│       ├── java/
│       │   └── br/com/mod/gestaomusical/
│       │       ├── config/
│       │       ├── controller/
│       │       ├── dto/
│       │       ├── entity/
│       │       ├── repository/
│       │       ├── security/
│       │       └── service/
│       │
│       └── resources/
│
├── pom.xml
└── README.md
```

---

# 10. Perfis de usuário

O domínio prevê os seguintes perfis principais:

- Secretaria;
- Encarregado Regional;
- Encarregado Local;
- Instrutor.

A estrutura de perfis e permissões já existe no banco de dados.

Entre as permissões cadastradas estão:

- consultar aluno;
- cadastrar aluno;
- editar aluno;
- arquivar aluno;
- alterar nível;
- inserir lição;
- consultar histórico musical;
- consultar auditoria;
- gerenciar usuário;
- gerenciar permissões.

## 10.1 Regra de autoria e autorização musical

A autoria de qualquer operação é sempre vinculada ao **usuário efetivamente autenticado** no sistema.

Nos lançamentos musicais:

- Instrutor(a) e Encarregado Local usam obrigatoriamente o próprio usuário autenticado como autorizador;
- Secretaria e Encarregado Regional podem autorizar em nome próprio ou indicar outro usuário;
- o usuário indicado deve existir, estar ativo e pertencer ao mesmo setor do usuário autenticado;
- o usuário indicado não poderá estar com sessão ativa no MOD naquele momento;
- a autoria da operação permanece sempre vinculada ao usuário efetivamente autenticado.

```text
Instrutor(a) / Encarregado Local
ATOR = usuário autenticado
AUTORIZADOR = usuário autenticado
```

```text
Secretaria / Encarregado Regional
ATOR = usuário autenticado
AUTORIZADOR = próprio usuário autenticado
               OU outro usuário ativo do mesmo setor
                  sem sessão ativa no MOD
```

A validação deve considerar o vínculo de **setor**, não apenas a Comum individual.

> **Status atual:** autenticação, autoria real, restrições por perfil, validação de autorizador por setor e bloqueio de autorizador com sessão ativa estão implementados e validados.

---

# 11. Gestão de alunos

O módulo de alunos permite:

- listar alunos;
- consultar aluno por ID;
- cadastrar;
- atualizar;
- listar arquivados;
- arquivar;
- restaurar.

Principais informações tratadas:

- nome;
- Comum;
- nível;
- cargo ministerial;
- indicação de instrumento;
- data de batismo;
- data de início no GEM;
- situação.

Situações utilizadas:

```text
ATIVO
ARQUIVADO
```

O sistema utiliza **arquivamento lógico**, preservando o histórico do aluno.

---

# 12. Evolução musical

A evolução musical é tratada em módulos independentes.

## 12.1 MTS

Permite registrar:

- data;
- módulo;
- lição;
- página inicial;
- página final;
- observações.

O usuário responsável pelo lançamento é obtido automaticamente pela autenticação.

## 12.2 MSA

Permite registrar:

- data;
- fase;
- páginas;
- lições;
- clave;
- usuário que autorizou;
- observações.

Há uma distinção entre **autoria da operação** e **autorização musical**.

Por padrão, o usuário autenticado também é o autorizador do lançamento. A Secretaria e o Encarregado Regional podem indicar outro usuário como autorizador, desde que esse usuário exista, esteja ativo, pertença ao mesmo setor e não esteja com sessão ativa no MOD naquele momento.

```text
AUTOR DA OPERAÇÃO = usuário autenticado

AUTORIZADOR MUSICAL =
    usuário autenticado
    ou, para Secretaria / Encarregado Regional,
    outro usuário ativo do mesmo setor
    sem sessão ativa no MOD
```

Mesmo quando a Secretaria ou o Encarregado Regional informa outro autorizador, a autoria da operação continua registrada para quem efetivamente estava autenticado.

## 12.3 Método

Permite registrar:

- nome do método;
- data;
- páginas;
- lições;
- clave;
- autorizado por;
- observações.

## 12.4 Hinário

Permite registrar:

- número do hino;
- voz;
- clave;
- autorizado por;
- observações.

Vozes previstas:

```text
PRINCIPAL
ALTERNATIVA
```

## 12.5 Escala

Permite registrar:

- nome da escala;
- tonalidade;
- clave;
- autorizado por;
- observações.

Tonalidades atualmente cadastradas no ambiente de desenvolvimento incluem:

- Dó;
- Fá;
- Fá/Sib;
- Láb;
- Mib;
- Sib.

---

# 13. Histórico

O sistema mantém registros de histórico para diferentes operações.

Exemplos:

```text
REGISTRO_MTS
REGISTRO_MSA
REGISTRO_METODO
REGISTRO_HINARIO
REGISTRO_ESCALA
ATUALIZACAO_ALUNO
EXCLUSAO_ALUNO
RESTAURACAO_ALUNO
COMPARTILHAMENTO_ALUNO
```

O histórico registra informações como:

- aluno;
- usuário responsável;
- tipo do evento;
- descrição;
- valor anterior;
- valor novo.

Isso permite acompanhar a evolução do aluno e as principais alterações realizadas no sistema.

---

# 14. Auditoria

A auditoria é uma das funcionalidades centrais do MOD.

Cada operação auditada pode registrar:

- usuário autenticado;
- ação;
- tabela afetada;
- ID do registro;
- descrição;
- dados anteriores;
- dados novos;
- data e hora.

Exemplo conceitual:

```text
Ação: ATUALIZACAO_ALUNO
Usuário: Maria Secretaria
Registro: aluno 14

Antes:
nome = Aluno Teste

Depois:
nome = Aluno Teste Atualizado
```

A autoria das operações não depende mais de um `usuarioId` enviado pelo cliente.

O usuário responsável é obtido diretamente da autenticação do Spring Security.

Essa decisão reduz a possibilidade de falsificação da autoria das operações.

---

# 15. Notificações e alertas

O sistema gera notificações associadas a eventos relevantes.

Exemplos:

- registro de MTS;
- registro de MSA;
- registro de Método;
- registro de Hinário;
- registro de Escala;
- atualização de aluno;
- arquivamento;
- restauração;
- compartilhamento.

Também existe uma funcionalidade para detectar alunos sem movimentação.

## Aluno sem movimentação por 60 dias

A regra considera:

1. última movimentação registrada em MTS, MSA, Método, Hinário ou Escala;
2. se não houver movimentação, utiliza `dataInicioGem`;
3. se também não existir, utiliza a data de criação do aluno;
4. considera apenas alunos ativos.

Um alerta pode ser gerado para a Secretaria quando um aluno atingir o período configurado sem movimentação.

O sistema também evita gerar alertas duplicados para o mesmo período de inatividade.

---

# 16. Arquivamento e restauração

O sistema não realiza exclusão física do aluno como fluxo normal.

O processo utilizado é:

```text
ATIVO
  |
  v
ARQUIVADO
```

Para arquivar um aluno é necessário informar um motivo.

Durante o arquivamento são registrados:

- registro de exclusão;
- usuário autenticado;
- motivo;
- histórico;
- notificação;
- auditoria.

A restauração realiza o fluxo inverso:

```text
ARQUIVADO
   |
   v
 ATIVO
```

A restauração utiliza somente o usuário autenticado e não exige `usuarioId` no corpo da requisição.

---

# 17. Compartilhamento entre Comuns

O MOD permite compartilhar um aluno com outra Comum sem alterar sua Comum de origem.

Regras principais:

- aluno arquivado não pode ser compartilhado;
- aluno não pode ser compartilhado com a própria Comum;
- somente um compartilhamento ativo é permitido;
- a Comum original é preservada;
- o usuário responsável é obtido da autenticação.

Exemplo:

```text
Comum de origem:
Paranoá - Quadra 30

Comum de destino:
Itapoã I
```

O compartilhamento gera:

- registro próprio;
- histórico;
- notificação;
- auditoria.

---

# 18. Relatórios

## 18.1 Relatório mensal

O relatório mensal já está implementado.

Ele consolida informações de um determinado mês, incluindo:

- total de alunos ativos;
- total de alunos arquivados;
- quantidade de registros MTS;
- quantidade de registros MSA;
- quantidade de Métodos;
- quantidade de registros de Hinário;
- quantidade de Escalas;
- alunos sem movimentação por 60 dias;
- compartilhamentos;
- exclusões/arquivamentos;
- restaurações;
- notificações;
- eventos de auditoria;
- data de geração.

Rotas disponíveis:

```text
POST /relatorios/mensais/{ano}/{mes}
GET  /relatorios/mensais/{ano}/{mes}
```

## 18.2 Relatório semanal

> **Status: em desenvolvimento**

A proposta do relatório semanal é apresentar uma visão operacional das atividades realizadas pelos usuários.

Exemplos de informações previstas:

- usuário que cadastrou alunos;
- quantidade de alunos cadastrados;
- quantidade de alunos arquivados;
- quantidade de registros musicais;
- atividades realizadas por instrutores;
- usuários sem movimentação na semana.

A implementação desse relatório depende da trilha de autoria real das operações, que já foi estruturada no sistema.

---

# 19. Dashboard

> **Status: concluído**

O Dashboard da Secretaria foi implementado no front-end Angular e consome dados reais do back-end.

Entre os indicadores apresentados estão:

- total de alunos;
- alunos ativos;
- alunos arquivados;
- alunos por Comum;
- atividades recentes;
- alterações pendentes;
- notificações.

A contagem de alunos por Comum considera apenas alunos ativos, mantendo os indicadores coerentes após arquivamento e restauração.

O Dashboard é restrito à Secretaria. Usuários sem permissão são direcionados para áreas compatíveis com seu perfil.

---

# 20. Segurança e autenticação

A aplicação utiliza Spring Security.

## 20.1 Usuários no banco

A autenticação utiliza os usuários armazenados na tabela `usuario`.

O login é realizado pelo e-mail.

## 20.2 Senhas

As senhas são armazenadas utilizando BCrypt.

Senhas em texto puro não devem ser versionadas.

## 20.3 Autoria real das operações

O MOD possui um serviço responsável por identificar o usuário autenticado.

Com isso, o cliente não escolhe mais quem será registrado como autor.

Exemplo:

```text
Cliente envia operação
         |
         v
Spring Security
         |
         v
Usuário autenticado
         |
         v
Service
         |
         v
Histórico / Notificação / Auditoria
```

Essa lógica já foi aplicada e testada em:

- MTS;
- MSA;
- Método;
- Hinário;
- Escala;
- cadastro de aluno;
- atualização de aluno;
- arquivamento;
- restauração;
- compartilhamento.

## 20.4 Autorização musical, perfil, setor e sessão ativa

A autorização musical segue regras adicionais de segurança e de negócio.

### Instrutor(a) e Encarregado Local

Esses perfis não podem indicar outra pessoa como autorizadora.

```text
ATOR = usuário autenticado
AUTORIZADOR = usuário autenticado
```

### Secretaria e Encarregado Regional

Esses perfis podem:

- autorizar em nome próprio;
- ou indicar outro usuário como autorizador.

Quando indicarem outra pessoa, o sistema deverá validar:

1. o usuário informado existe;
2. o usuário está ativo;
3. o usuário pertence ao mesmo setor do usuário autenticado;
4. o usuário indicado não está com sessão ativa no MOD naquele momento.

A autoria da operação nunca é transferida para o autorizador indicado.

Exemplo:

```text
João Regional autenticado
Setor Paranoá
        |
        +--> autoriza em nome próprio         -> permitido
        |
        +--> usuário ativo do mesmo setor
        |    sem sessão ativa                 -> permitido
        |
        +--> usuário de outro setor           -> bloqueado
        |
        +--> usuário com sessão ativa         -> bloqueado
```

Mensagens de negócio previstas:

```text
O usuário informado como autorizador não pertence ao mesmo setor do usuário autenticado.
```

```text
O usuário informado como autorizador está ativo no sistema.
O lançamento deve ser realizado pelo próprio usuário.
```

> **Status atual:** autenticação, autoria real, validação por perfil/setor e controle de sessão ativa estão implementados e testados.

## 20.5 Controle de sessão e usuários online

O sistema utiliza autenticação por sessão com Spring Security.

Endpoints principais:

```text
POST /auth/login
POST /auth/logout
GET  /auth/sessao
```

A sessão utiliza cookie `JSESSIONID`.

O endpoint `/auth/sessao` informa o usuário autenticado, incluindo:

- ID;
- nome;
- e-mail;
- perfil;
- status online.

O controle de sessão ativa é utilizado também para impedir que Secretaria ou Encarregado Regional indiquem como autorizador um usuário que já esteja autenticado no MOD.

O front-end exibe dinamicamente o nome e o perfil reais do usuário autenticado e restringe a exibição do Dashboard aos usuários autorizados.


---

# 21. Banco de dados

O projeto utiliza PostgreSQL.

O banco contém tabelas relacionadas a:

- alunos;
- Comuns;
- níveis;
- cargos;
- instrumentos;
- tonalidades;
- usuários;
- perfis;
- permissões;
- MTS;
- MSA;
- métodos;
- hinário;
- escalas;
- histórico;
- auditoria;
- notificações;
- exclusões;
- compartilhamentos;
- relatórios.

## 21.1 Scripts versionados

O projeto possui:

```text
database/schema.sql
database/dados-iniciais.sql
```

### `schema.sql`

Contém a estrutura do banco.

### `dados-iniciais.sql`

Contém dados estruturais necessários para iniciar o ambiente, sem incluir dados sensíveis de usuários ou registros operacionais.

Entre os dados estruturais estão:

- setor;
- perfis;
- permissões;
- relação perfil/permissão;
- Comuns;
- níveis;
- cargos ministeriais;
- instrumentos;
- tonalidades.

O processo de restauração do banco já foi validado em um banco temporário de testes.

---

# 22. Swagger / OpenAPI

A API está documentada utilizando Springdoc OpenAPI.

Rotas públicas da documentação:

```text
/swagger-ui/**
/swagger-ui.html
/v3/api-docs/**
```

A interface Swagger permite:

- consultar endpoints;
- visualizar modelos;
- verificar parâmetros;
- testar requisições;
- visualizar respostas;
- testar endpoints protegidos após autenticação por sessão.

O Swagger descreve a API.

O README descreve o sistema, o contexto e as regras de negócio.

---

# 23. Como executar

## 23.1 Pré-requisitos

- Java 21;
- PostgreSQL;
- Maven;
- Git;
- IDE compatível com Java/Spring Boot.

## 23.2 Clonar o projeto

```bash
git clone https://github.com/MagnoDevbraul/mod-gestao-musical.git
```

Entrar na pasta:

```bash
cd mod-gestao-musical
```

## 23.3 Criar o banco

Criar um banco PostgreSQL para o projeto.

Depois executar:

```text
database/schema.sql
```

e:

```text
database/dados-iniciais.sql
```

## 23.4 Configurar propriedades locais

O arquivo com credenciais reais não deve ser versionado.

Utilize o arquivo de exemplo do projeto como referência para configurar:

```text
spring.datasource.url
spring.datasource.username
spring.datasource.password
```

## 23.5 Executar o back-end

Pela IDE:

```text
Executar a classe principal do Spring Boot
```

Ou pelo Maven Wrapper:

```powershell
.\mvnw spring-boot:run
```

O back-end utiliza, por padrão:

```text
http://localhost:8080
```

## 23.6 Executar o front-end

No projeto Angular:

```powershell
npm start
```

O front-end utiliza, por padrão:

```text
http://localhost:4200
```

Durante o desenvolvimento, o Angular utiliza proxy para comunicação com o back-end.

## 23.7 Abrir Swagger

Com a aplicação em execução:

```text
http://localhost:8080/swagger-ui/index.html
```

> A porta pode variar caso seja alterada na configuração local.

---

# 24. Fluxo de utilização

Um fluxo simplificado de utilização do sistema pode ser:

```text
1. Usuário autentica
        |
        v
2. Consulta ou cadastra aluno
        |
        v
3. Registra evolução musical
        |
        v
4. Sistema grava histórico
        |
        v
5. Sistema gera notificação
        |
        v
6. Sistema registra auditoria
        |
        v
7. Secretaria acompanha informações
        |
        v
8. Relatórios consolidam atividades
```

---

# 25. Diagramas e evidências

A documentação visual deverá ser armazenada em:

```text
docs/imagens/
```

Sugestão de arquivos:

```text
docs/imagens/der.png
docs/imagens/caso-de-uso.png
docs/imagens/arquitetura.png
docs/imagens/swagger.png
docs/imagens/dashboard.png
```

Exemplo de inclusão no README:

```markdown
![DER do MOD](docs/imagens/der.png)
```

## DER

> Adicionar imagem final do DER.

## Caso de uso

> Adicionar diagrama final de caso de uso.

## Swagger

> Adicionar screenshot da documentação Swagger.

## Dashboard

> Adicionar screenshot após a implementação do Dashboard.

---

# 26. Status atual do desenvolvimento

| Funcionalidade | Status |
|---|---|
| Estrutura Spring Boot | ✅ Concluído |
| PostgreSQL + JPA/Hibernate | ✅ Concluído |
| Gestão de alunos | ✅ Concluído |
| Comuns | ✅ Concluído |
| Níveis | ✅ Concluído |
| Cargos ministeriais | ✅ Concluído |
| Instrumentos e tonalidades | ✅ Concluído |
| MTS | ✅ Concluído |
| MSA | ✅ Concluído |
| Método | ✅ Concluído |
| Hinário | ✅ Concluído |
| Escala | ✅ Concluído |
| Histórico | ✅ Concluído |
| Auditoria | ✅ Concluído |
| Notificações | ✅ Concluído |
| Arquivamento | ✅ Concluído |
| Restauração | ✅ Concluído |
| Compartilhamento | ✅ Concluído |
| Alterações restritas | ✅ Concluído |
| Aluno sem movimentação – 60 dias | ✅ Concluído |
| Alertas de inatividade | ✅ Concluído |
| Relatório mensal | ✅ Concluído |
| Dashboard | ✅ Concluído |
| Front-end Angular | ✅ Concluído |
| Autenticação com usuários do banco | ✅ Concluído |
| Login / logout / sessão | ✅ Concluído |
| Controle de sessão e usuários online | ✅ Concluído |
| Bloqueio de autorizador com sessão ativa | ✅ Concluído |
| Permissões por perfil e autorizador por setor | ✅ Concluído |
| Swagger / OpenAPI | ✅ Concluído |
| Scripts do banco | ✅ Versionados |
| Testes manuais finais | ✅ Executados |
| Relatório semanal | ⏳ Evolução posterior |
| Fluxo de solicitação do Instrutor | ⏳ Evolução obrigatória pós-entrega |

---

# 27. Testes realizados

Durante o desenvolvimento foram realizados testes manuais de API com Postman e validações diretas no PostgreSQL.

Entre os cenários já verificados estão:

- autenticação válida;
- autenticação inválida;
- criação de aluno;
- atualização de aluno;
- arquivamento;
- tentativa de arquivar aluno já arquivado;
- restauração;
- tentativa de restaurar aluno já ativo;
- compartilhamento;
- tentativa de compartilhar com a própria Comum;
- tentativa de criar segundo compartilhamento ativo;
- registro de MTS;
- registro de MSA;
- registro de Método;
- registro de Hinário;
- registro de Escala;
- histórico;
- notificações;
- auditoria;
- identificação do usuário autenticado;
- tentativa de falsificar autoria utilizando `usuarioId` no JSON;
- detecção de aluno sem movimentação;
- prevenção de alertas duplicados;
- geração de relatório mensal;
- reconstrução do banco a partir dos scripts versionados.

---

# 28. Limitações conhecidas

No estado atual do projeto:

- o relatório semanal permanece como evolução posterior;
- integrações com sistemas externos dependem de análise técnica e autorização institucional;
- testes automatizados mais abrangentes ainda podem ser adicionados;
- a implantação em ambiente de produção ainda não faz parte da versão acadêmica entregue.

## 28.1 Evolução obrigatória pós-entrega — Instrutor

A versão atual bloqueia o Instrutor quando ele tenta executar diretamente determinadas operações restritas.

Após a entrega acadêmica, deverá ser implementada obrigatoriamente a seguinte regra:

```text
INSTRUTOR
   |
   v
SOLICITA ALTERAÇÃO / EXCLUSÃO
   |
   v
PENDENTE
   |
   v
SECRETARIA ANALISA
   |
   +--> APROVA
   |
   +--> REJEITA
```

O Instrutor não deverá executar diretamente alteração restrita ou exclusão. A solicitação deverá gerar pendência, preservar histórico e produzir notificação/auditoria para análise da Secretaria.

---

# 29. Evoluções futuras

Possíveis evoluções do projeto:

- fluxo obrigatório de solicitação do Instrutor para alteração/exclusão;
- relatório semanal automatizado;
- testes automatizados mais abrangentes;
- integração autorizada com sistemas institucionais;
- envio automatizado de relatórios;
- armazenamento de documentos relacionados a arquivamentos;
- alertas por canais externos;
- filtros avançados;
- exportação de relatórios;
- métricas por Comum;
- indicadores por instrutor;
- monitoramento operacional;
- implantação em ambiente de produção.

---

# Repositórios

## Back-end

```text
https://github.com/MagnoDevbraul/mod-gestao-musical-backend
```

## Front-end

```text
https://github.com/MagnoDevbraul/mod-gestao-musical-frontend
```


# 30. Contexto acadêmico

Projeto desenvolvido por:

**Magno Walério Alves Ferreira**

Curso:

**Análise e Desenvolvimento de Sistemas – UNINTER**

Natureza:

**Projeto acadêmico com aplicação extensionista**

O MOD busca demonstrar a aplicação prática de conhecimentos de:

- levantamento de requisitos;
- modelagem de dados;
- programação orientada a objetos;
- desenvolvimento de APIs;
- persistência;
- segurança;
- autenticação;
- regras de negócio;
- auditoria;
- documentação;
- controle de versão;
- testes;
- integração entre software e necessidades reais de uma organização.

---

## Observação final

Este README é uma documentação viva.

A versão atual reflete o estado funcional alcançado na entrega acadêmica. As evoluções posteriores permanecem separadas do escopo entregue, com destaque para o relatório semanal e para a regra obrigatória de solicitação do Instrutor para alterações/exclusões.

