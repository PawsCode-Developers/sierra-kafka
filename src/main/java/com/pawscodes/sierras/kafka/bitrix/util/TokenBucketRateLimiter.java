package com.pawscodes.sierras.kafka.bitrix.util;

public class TokenBucketRateLimiter {
    private final int capacity;  // Capacidad máxima del cubo
    private final double tokensPerSecond;  // Tasa de reposición (2/segundo)

    private double availableTokens;  // Tokens disponibles actualmente
    private long lastRefillTimestamp;  // Último momento de reposición

    /**
     * Constructor para el limitador de tasa con Token Bucket.
     *
     * @param capacity        Capacidad máxima del cubo
     * @param tokensPerSecond Tasa de reposición de tokens por segundo
     */
    public TokenBucketRateLimiter(int capacity, double tokensPerSecond) {
        this.capacity = capacity;
        this.tokensPerSecond = tokensPerSecond;
        this.availableTokens = capacity;
        this.lastRefillTimestamp = System.currentTimeMillis();
    }

    /**
     * Calcula y añade nuevos tokens basados en el tiempo transcurrido.
     */
    private synchronized void refill() {
        long now = System.currentTimeMillis();
        double timePassedSeconds = (now - lastRefillTimestamp) / 1000.0;

        // Calcula tokens a añadir según tiempo transcurrido
        double newTokens = timePassedSeconds * tokensPerSecond;

        if (newTokens > 0) {
            availableTokens = Math.min(capacity, availableTokens + newTokens);
            lastRefillTimestamp = now;
        }
    }

    /**
     * Calcula cuánto tiempo esperar para la siguiente petición permitida.
     *
     * @return Tiempo a esperar en milisegundos
     */
    public synchronized long getWaitTimeMillis() {
        refill();

        if (availableTokens >= 1) {
            // Hay token disponible, no hay que esperar
            availableTokens -= 1;
            return 0;
        } else {
            // Calcular tiempo necesario para tener un token
            double timeToWaitSeconds = (1 - availableTokens) / tokensPerSecond;
            return (long) (timeToWaitSeconds * 1000);
        }
    }

    /**
     * Espera el tiempo necesario y luego permite realizar la petición.
     *
     * @throws InterruptedException si el thread es interrumpido durante la espera
     */
    public void waitIfNeeded() throws InterruptedException {
        long waitTime = getWaitTimeMillis();
        if (waitTime > 0) {
            Thread.sleep(waitTime);
            // Consumimos el token después de esperar
            synchronized (this) {
                availableTokens -= 1;
            }
        }
    }
}
