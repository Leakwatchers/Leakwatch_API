package com.unifor.br.leakwatch.model;

/**
 * Classificação de nível de gás em ppm.
 *
 *  OK       :    0 –  400 ppm  → concentração normal
 *  ALERTA   :  400 – 1000 ppm  → início de detecção, atenção
 *  CRITICO  : 1000 – 2000 ppm  → alarme, risco à saúde
 *  RISCO    :       > 2000 ppm → risco iminente, possibilidade de explosão
 */
public enum GasStatus {

    OK,
    ALERTA,
    CRITICO,
    RISCO;

    public static GasStatus classify(double ppm) {
        if (ppm < 400)  return OK;
        if (ppm < 1000) return ALERTA;
        if (ppm < 2000) return CRITICO;
        return RISCO;
    }
}