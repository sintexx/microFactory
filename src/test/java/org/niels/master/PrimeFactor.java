package org.niels.master;

import org.apache.commons.math3.primes.Primes;
import org.junit.jupiter.api.Test;

public class PrimeFactor {
    @Test
    public void calcPrimeFact() {
        for (int y = 0; y < 10; y++) {
            for (int i = 100; i < 7000; i++) {
                Primes.primeFactors(i);
            }
        }


    }
}
