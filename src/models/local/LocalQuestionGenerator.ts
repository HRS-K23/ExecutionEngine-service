import { v4 as uuidv4 } from 'uuid';
import {
  IQuestionGenerator,
  GenerateQuestionInput,
  GeneratedQuestion,
} from '../interfaces/IModels';
import { Logger } from '../../logging/Logger';

export class LocalQuestionGenerator implements IQuestionGenerator {
  private logger = Logger.getInstance();

  // Template library for different topics/subtopics
  private static readonly TEMPLATES: Record<string, Record<string, string>> = {
    Arrays: {
      'Merge Sort':
        'Given an array of integers, implement the merge sort algorithm to sort it in ascending order. ' +
        'Merge sort is a divide-and-conquer algorithm that divides the array into halves, recursively sorts them, ' +
        'and then merges the sorted halves back together.',
      'Binary Search':
        'Implement a binary search algorithm to find a target value in a sorted array. ' +
        'Return the index of the target if found, otherwise return -1. The array is guaranteed to be sorted.',
      'Two Pointers':
        'Given a sorted array of unique integers and a target sum, find two numbers that add up to the target. ' +
        'Return the indices of the two numbers. You may assume each input has exactly one solution.',
    },
    Graphs: {
      'DFS':
        'Implement Depth-First Search (DFS) to traverse a graph represented as an adjacency list. ' +
        'Visit all reachable vertices from a given starting vertex and return them in DFS order.',
      'BFS':
        'Implement Breadth-First Search (BFS) to traverse a graph represented as an adjacency list. ' +
        'Visit all reachable vertices from a given starting vertex and return them in BFS order.',
      'Shortest Path':
        'Implement Dijkstra\'s algorithm to find the shortest path from a source vertex to all other vertices. ' +
        'Return a dictionary with vertices as keys and their shortest distances from source as values.',
    },
    'Dynamic Programming': {
      'Fibonacci':
        'Implement a function to compute the nth Fibonacci number using dynamic programming. ' +
        'The Fibonacci sequence is defined as: F(n) = F(n-1) + F(n-2), with F(0) = 0 and F(1) = 1.',
      'Longest Substring':
        'Find the longest substring without repeating characters in a given string. ' +
        'Return the length of the longest such substring.',
      'Coin Change':
        'Given an unlimited supply of coins and a target amount, find the minimum number of coins needed to make the amount. ' +
        'Return -1 if it\'s impossible.',
    },
  };

  // Solution templates for different topics
  private static readonly SOLUTION_TEMPLATES: Record<string, Record<string, string>> = {
    Arrays: {
      'Merge Sort': `def merge_sort(arr):
    if len(arr) <= 1:
        return arr
    mid = len(arr) // 2
    left = merge_sort(arr[:mid])
    right = merge_sort(arr[mid:])
    return merge(left, right)

def merge(left, right):
    result = []
    i = j = 0
    while i < len(left) and j < len(right):
        if left[i] <= right[j]:
            result.append(left[i])
            i += 1
        else:
            result.append(right[j])
            j += 1
    result.extend(left[i:])
    result.extend(right[j:])
    return result`,
      'Binary Search': `def binary_search(arr, target):
    left, right = 0, len(arr) - 1
    while left <= right:
        mid = (left + right) // 2
        if arr[mid] == target:
            return mid
        elif arr[mid] < target:
            left = mid + 1
        else:
            right = mid - 1
    return -1`,
      'Two Pointers': `def find_two_sum(arr, target):
    left, right = 0, len(arr) - 1
    while left < right:
        current_sum = arr[left] + arr[right]
        if current_sum == target:
            return [left, right]
        elif current_sum < target:
            left += 1
        else:
            right -= 1
    return None`,
    },
    Graphs: {
      'DFS': `def dfs(graph, start):
    visited = set()
    result = []
    
    def traverse(vertex):
        visited.add(vertex)
        result.append(vertex)
        for neighbor in graph.get(vertex, []):
            if neighbor not in visited:
                traverse(neighbor)
    
    traverse(start)
    return result`,
      'BFS': `from collections import deque

def bfs(graph, start):
    visited = set([start])
    queue = deque([start])
    result = []
    
    while queue:
        vertex = queue.popleft()
        result.append(vertex)
        for neighbor in graph.get(vertex, []):
            if neighbor not in visited:
                visited.add(neighbor)
                queue.append(neighbor)
    
    return result`,
      'Shortest Path': `import heapq

def dijkstra(graph, start):
    distances = {node: float('inf') for node in graph}
    distances[start] = 0
    pq = [(0, start)]
    
    while pq:
        current_dist, current = heapq.heappop(pq)
        if current_dist > distances[current]:
            continue
        for neighbor, weight in graph[current]:
            distance = current_dist + weight
            if distance < distances[neighbor]:
                distances[neighbor] = distance
                heapq.heappush(pq, (distance, neighbor))
    
    return distances`,
    },
  };

  async generate(input: GenerateQuestionInput): Promise<GeneratedQuestion> {
    try {
      this.logger.info('LocalQuestionGenerator: Generating question', {
        topic: input.topic,
        subtopic: input.subtopic,
        difficulty: input.difficulty,
      });

      // Get template or generate default
      const template =
        LocalQuestionGenerator.TEMPLATES[input.topic]?.[input.subtopic] ||
        this.generateDefaultTemplate(input);

      const solution =
        LocalQuestionGenerator.SOLUTION_TEMPLATES[input.topic]?.[input.subtopic] ||
        this.generateDefaultSolution(input);

      const problemStatement = this.injectAttributes(template, input);
      const enhancedSolution = this.enhanceSolutionByDifficulty(solution, input.difficulty);

      return {
        problemStatement,
        sampleSolution: enhancedSolution,
        sampleSolutionExplanation: {
          approach: this.getApproach(input.topic, input.subtopic),
          timeComplexity: this.getTimeComplexity(input.subtopic, input.difficulty),
          spaceComplexity: this.getSpaceComplexity(input.subtopic, input.difficulty),
          keyPoints: this.getKeyPoints(input.topic),
        },
      };
    } catch (error) {
      this.logger.error('LocalQuestionGenerator: Failed to generate question', { error });
      throw error;
    }
  }

  getName(): string {
    return 'LocalQuestionGenerator (Mock)';
  }

  async isAvailable(): Promise<boolean> {
    return true; // Always available in mock mode
  }

  private generateDefaultTemplate(input: GenerateQuestionInput): string {
    return `Given a ${input.language} problem related to ${input.topic} and ${input.subtopic}, ` +
      `implement a solution of ${input.difficulty} difficulty. ` +
      `${input.description || 'Write efficient code that handles edge cases properly.'}`;
  }

  private generateDefaultSolution(input: GenerateQuestionInput): string {
    return `# Sample solution for ${input.topic} - ${input.subtopic}\n` +
      `# Language: ${input.language}\n` +
      `# Difficulty: ${input.difficulty}\n` +
      `# TODO: Implement solution here`;
  }

  private injectAttributes(template: string, input: GenerateQuestionInput): string {
    let result = template;
    result = result.replace(/{topic}/g, input.topic);
    result = result.replace(/{subtopic}/g, input.subtopic);
    result = result.replace(/{difficulty}/g, input.difficulty);
    result = result.replace(/{language}/g, input.language);
    result = result.replace(/{title}/g, input.title);
    return result;
  }

  private enhanceSolutionByDifficulty(solution: string, difficulty: string): string {
    if (difficulty === 'EASY') {
      return solution;
    }
    if (difficulty === 'MEDIUM') {
      return solution + '\n# Note: Consider optimizing for time/space complexity';
    }
    if (difficulty === 'HARD') {
      return solution + '\n# Optimization: Handle large inputs efficiently\n# Edge cases: Empty input, single element, duplicates';
    }
    return solution;
  }

  private getApproach(topic: string, subtopic: string): string {
    const approaches: Record<string, Record<string, string>> = {
      Arrays: {
        'Merge Sort': 'Divide-and-Conquer approach',
        'Binary Search': 'Two-pointer approach with middle element elimination',
        'Two Pointers': 'Two-pointer technique from opposite ends',
      },
      Graphs: {
        'DFS': 'Recursive depth-first traversal',
        'BFS': 'Queue-based level-order traversal',
        'Shortest Path': 'Priority queue with Dijkstra\'s algorithm',
      },
    };
    return approaches[topic]?.[subtopic] || 'Standard algorithmic approach';
  }

  private getTimeComplexity(subtopic: string, difficulty: string): string {
    const complexities: Record<string, string> = {
      'Merge Sort': 'O(n log n)',
      'Binary Search': 'O(log n)',
      'Two Pointers': 'O(n)',
      'DFS': 'O(V + E)',
      'BFS': 'O(V + E)',
      'Shortest Path': 'O((V + E) log V)',
    };
    return complexities[subtopic] || `O(n) for ${difficulty}`;
  }

  private getSpaceComplexity(subtopic: string, difficulty: string): string {
    const complexities: Record<string, string> = {
      'Merge Sort': 'O(n)',
      'Binary Search': 'O(1)',
      'Two Pointers': 'O(1)',
      'DFS': 'O(V)',
      'BFS': 'O(V)',
      'Shortest Path': 'O(V)',
    };
    return complexities[subtopic] || `O(1) to O(n) for ${difficulty}`;
  }

  private getKeyPoints(topic: string): string[] {
    const keyPoints: Record<string, string[]> = {
      Arrays: [
        'Array indexing and bounds',
        'Time complexity of operations',
        'In-place modifications',
        'Handling edge cases (empty, single element)',
      ],
      Graphs: [
        'Graph representation (adjacency list/matrix)',
        'Visited tracking',
        'Traversal order matters',
        'Connected components',
      ],
      'Dynamic Programming': [
        'Overlapping subproblems',
        'Optimal substructure',
        'Memoization vs Tabulation',
        'Base cases',
      ],
    };
    return keyPoints[topic] || ['Algorithm correctness', 'Edge case handling', 'Performance optimization'];
  }
}
