# Estudo de Complexidade - WagonUnloading.java

## 1. Identificação
**Ficheiro:** `WagonUnloading.java`  
**Descrição:** Classe responsável pela criação e armazenamento de uma instância da classe "Warehouse" e todos os espaços que a compõem, uma lista com todos os possíveis produtos que poderão ser adicionados, e a leitura do processo de descarga de vagões (wagons) e o preenchimento do espaço livre dentro da instância Warehouse. Toda a informação destas funções é retirada de um ficheiro .csv.  

---

## 2. Estrutura Geral do Código
A classe `WagonUnloading` contém:
- Um método que cria a instância da classe "Warehouse"
- Um método que cria uma lista com todos os produtos retirados de um ficheiro .csv
- Um método que lê um ficheiro .csv que contém todos os produtos chegados ao armazém e preenche os espaços da instância da classe "Warehouse"

O código segue uma estrutura **iterativa**, sem recursão explícita.

---

## 3. Análise de Complexidade Temporal

### 3.1 Estrutura Principal (Simulação)
**Complexidade:**

A criação da instância "Warehouse":
- Criar todos os Aisles, e Bays;
- Perceber quando se passa de um Aisle para o outro.

Cada iteração depende da leitura de um ficheiro csv de `n` linhas.

**Complexidade:**
- Melhor caso: O(n) - Se o ficheiro estiver bem organizado.
- Pior caso: O(n²) - O ficheiro não está bem formatado, nem organizado, correr o ficheiro, se possível, será demorado.
- Médio caso: O(n log n) - O ficheiro não está bem organizado, mas está formatado.

A lógica da leitura de wagons.csv envolve percorrer cada uma das linhas dos vagões uma vez, e:
- Verificar quantos produtos estão dentro dos vagões, e armazená-los de forma temporária;
- Verificar quais produtos têm data de expiração, e quais não a têm;

Cada iteração depende linearmente do número de vagões `n`.

**Complexidade:**
- Melhor caso: O(n) — se a sequência já estiver na ordem correta.
- Pior caso: O(n²) — quando há necessidade de múltiplas operações internas por elemento (e.g. inserções e remoções em pilhas).
- Médio caso: O(n log n) — comportamento típico se parte das operações são otimizadas.



A organização dos produtos envolve ler pelo menos 1 vez cada um dos produtos armazenados:
- Realizando uma organização FEFO primeiro caso 1 ou mais produtos tenham data de expiração, e FIFO depois, organizando só de forma FIFO de caso contrário
- Armazenando os produtos ao verificando cada uma das entradas de diferentes tipos de organização pelo menos uma vez

Cada iteração depende linearmente do número de produtos em organização FEFO `n`, e o número de produtos em organização FIFO `n`

**Complexidade:**
- Melhor caso: O(n) - todos os produtos estão organizados só em FEFO ou FIFO, e têm uma quantidade moderadamente baixa 
- Pior caso: O(n) - ambos os produtos estão igualmente organizados em FEFO ou FIFO, e ambos têm uma quantidade moderadamente alta
- Médio caso: O(n log n) - a maioria dos produtos está organizados em FEFO ou FIFO, o resto estando organizado em uma quantidade menor no outro método de organização

### 3.2 Operações sobre Estruturas de Dados
|   | Estrutura                        | Operação  | Complexidade          | Observações |
|:--|----------------------------------|-----------|-----------------------|-------------|
|   | `List.contains()`                | O(n)      | Linear                |
|   | `List.add()` / `List.remove()`   | O(1)–O(n) | Dependente da posição |

### 3.3 Método de Verificação ou Ordenação
Se o método realizar comparações entre elementos ou validações em ciclos aninhados, a complexidade global pode atingir O(n²).

---

## 4. Análise de Complexidade Espacial
O programa utiliza:
- Estruturas auxiliares para armazenar temporariamente os vagões.
- Variáveis de controlo e contadores.

**Complexidade Espacial:**
- Estruturas auxiliares com no máximo `n` elementos: O(n)
- Variáveis simples (contadores, índices): O(1)  
  Total: O(n)

---

## 5. Análise de Casos
| Caso            | Descrição                                             | Complexidade |
|-----------------|-------------------------------------------------------|--------------|
| **Melhor caso** | Sequência já ordenada (mínimas operações)             | O(n)         |
| **Médio caso**  | Parte da sequência requer reorganização               | O(n log n)   |
| **Pior caso**   | Sequência totalmente invertida / operações duplicadas | O(n²)        |

---

## 6. Conclusão
O ficheiro `WagonUnloading.java` apresenta complexidade temporal entre O(n) e O(n²) dependendo do caso e da estrutura usada.  
A complexidade espacial é O(n), proporcional ao número de vagões processados.

O algoritmo é eficiente para conjuntos pequenos/médios e aceitável em contexto de simulação discreta**.  
Para grandes volumes de dados, recomenda-se otimização na forma de acesso e verificação de elementos (ex.: uso de `HashSet` para acesso O(1)).
