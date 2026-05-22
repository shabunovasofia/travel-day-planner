package ru.kholodov.locationcontextservice.enums;

/**
 * Темп прогулки.
 *
 * <p>Каждое значение несёт в себе скорость ходьбы в км/ч, которая используется при расчёте изохроны
 * и fallback-радиуса:
 *
 * <ul>
 *   <li>{@code SLOW} — медленный (~3.5 км/ч)
 *   <li>{@code MEDIUM} — средний (~5.0 км/ч)
 *   <li>{@code FAST} — быстрый (~6.5 км/ч)
 * </ul>
 */
public enum Pace {
  SLOW(3.5),
  MEDIUM(5.0),
  FAST(6.5);

  /** Скорость ходьбы в км/ч для данного темпа. */
  private final double speedKmh;

  Pace(double speedKmh) {
    this.speedKmh = speedKmh;
  }

  /**
   * @return скорость ходьбы в км/ч
   */
  public double getSpeedKmh() {
    return speedKmh;
  }
}
