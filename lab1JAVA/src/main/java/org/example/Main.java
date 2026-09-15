package org.example;

import java.util.Arrays;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter number n: ");

        if (!scanner.hasNextInt()) {
            System.out.println("Error: please enter an integer.");
            return;
        }

        int n = scanner.nextInt();

        if (n < 2) {
            System.out.println("There are no primes less than or equal to " + n + ".");
            return;
        }

        // find primes up to n
        boolean[] isPrime = new boolean[n + 1];
        Arrays.fill(isPrime, true);
        isPrime[0] = false;
        isPrime[1] = false;

        for (int p = 2; p * p <= n; p++) {
            if (isPrime[p]) {
                for (int multiple = p * p; multiple <= n; multiple += p) {
                    isPrime[multiple] = false;
                }
            }
        }

        // find the prime with the maximum number of set bits (ones) in binary
        int bestPrime = -1;
        int maxOnes = -1;

        for (int i = 2; i <= n; i++) {
            if (isPrime[i]) {
                int ones = Integer.bitCount(i);
                if (ones > maxOnes) {
                    maxOnes = ones;
                    bestPrime = i;
                }
            }
        }

        // Print the result
        System.out.println("Found prime number: " + bestPrime);
        System.out.println("Binary representation: " + Integer.toBinaryString(bestPrime));
        System.out.println("Number of ones: " + maxOnes);
    }
}