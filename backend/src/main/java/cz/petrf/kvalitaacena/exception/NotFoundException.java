package cz.petrf.kvalitaacena.exception;

/** Entita podle zadaného id neexistuje (GraphQL) — viz GraphQlExceptionHandler. */
public class NotFoundException extends RuntimeException {
  public NotFoundException(String message) {
    super(message);
  }
}
