package utils

final case class RateLimitException(private val message: String = "",
                                 private val cause: Throwable = None.orNull)
  extends Exception(message, cause)

final case class InternalServerError(private val message: String = "",
                                    private val cause: Throwable = None.orNull)
  extends Exception(message, cause)

final case class AuthException(private val message: String = "",
                                     private val cause: Throwable = None.orNull)
  extends Exception(message, cause)

final case class OfflineServiceException(private val message: String = "Service is currently offline",
                               private val cause: Throwable = None.orNull)
  extends Exception(message, cause)

final case class ResourceDoesNotExistException(private val message: String = "Resource does not exist",
                                         private val cause: Throwable = None.orNull)
  extends Exception(message, cause)